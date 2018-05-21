/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.finance;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.aop.LogMetrics;
import uk.gov.london.ops.domain.finance.*;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.refdata.CategoryValue;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.mapper.ProjectLedgerEntryMapper;
import uk.gov.london.ops.repository.PaymentGroupRepository;
import uk.gov.london.ops.repository.PaymentSummaryRepository;
import uk.gov.london.ops.repository.ProjectLedgerRepository;
import uk.gov.london.ops.service.*;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.util.GlaOpsUtils;
import uk.gov.london.ops.web.api.finance.PaymentFilterOption;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.finance.LedgerStatus.*;
import static uk.gov.london.ops.domain.finance.LedgerType.*;
import static uk.gov.london.ops.domain.refdata.CategoryValue.Category.PaymentDeclineReason;
import static uk.gov.london.ops.domain.refdata.CategoryValue.Category.ReclaimDeclineReason;
import static uk.gov.london.ops.web.api.finance.PaymentFilterOption.ALL_PAYMENTS;

@Service
public class PaymentService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Value("${default.wbs.code}")
    String defaultWbsCode = "";

    @Autowired
    ProjectLedgerEntryMapper projectLedgerEntryMapper;

    @Autowired
    PaymentSummaryRepository paymentSummaryRepository;

    @Autowired
    ProjectLedgerRepository projectLedgerRepository;

    @Autowired
    PaymentGroupRepository paymentGroupRepository;

    @Autowired
    FinanceService financeService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    ProjectService projectService;

    @Autowired
    Environment environment;

    @Autowired
    RefDataService refDataService;

    @Autowired
    UserService userService;
    
    @Autowired
    AuditService auditService;

    @Autowired
    PermissionService permissionService;

    public Page<PaymentSummary> findAll(String projectIdOrName,
                                        String organisationName,
                                        List<LedgerType> relevantSources,
                                        List<LedgerStatus> relevantStatuses,
                                        List<String> categories,
                                        List<String> relevantProgrammes,
                                        OffsetDateTime fromDate,
                                        OffsetDateTime toDate,
                                        List<String> paymentDirection,
                                        Pageable pageable
                                            ) {
        User user = userService.currentUser();
        final List<Integer> relevantOrgIds = user.getOrganisationIds();

        Page<PaymentSummary> payments = paymentSummaryRepository.findAll(projectIdOrName,
                organisationName,
                relevantSources,
                relevantStatuses,
                categories,
                relevantProgrammes,
                fromDate,
                toDate,
                relevantOrgIds,
                paymentDirection,
                pageable);

        return payments;
        // TODO move this down one level
//        final List<Organisation> filteredOrgList = getFilteredOrganisations(payments);
//        final List<PaymentSummary> filtered = filterAuth(payments, filteredOrgList);
//
//        return sortByIdDesc(filtered);
    }

    public List<PaymentGroup> findAllPaymentGroupsByStatus(PaymentFilterOption status) {
        List<PaymentGroup> paymentGroups;
        if (status != null) {
            List<String> statusList = Arrays.stream(status.getRelevantStatuses()).map(LedgerStatus::name).collect(Collectors.toList());

            paymentGroups = paymentGroupRepository.findAllByStatusIn(statusList.toArray(new String [statusList.size()]));
        } else {
            paymentGroups = paymentGroupRepository.findAll();
        }

        List<PaymentGroup> filtered = restrictAndEnrichPaymentGroups(paymentGroups);

        // should not be any payment groups without payments but remove them just in case
        // sort rules are dependant on the payment status
        List<PaymentGroup> response;
        if (status == null || !(PaymentFilterOption.AUTHORISED.equals(status) || PaymentFilterOption.DECLINED.equals(status))) {
            response = filtered.stream().filter(a -> a.getLedgerEntries().size() > 0).sorted(
                    (a,b) -> b.getLedgerEntries().get(0).getCreatedOn().compareTo(a.getLedgerEntries().get(0).getCreatedOn())).collect(Collectors.toList());
        } else if (PaymentFilterOption.AUTHORISED.equals(status)){
            response = filtered.stream().filter(a -> a.getLedgerEntries().size() > 0).sorted(
                    (a,b) -> b.getLedgerEntries().get(0).getAuthorisedOn().compareTo(a.getLedgerEntries().get(0).getAuthorisedOn())).collect(Collectors.toList());
        } else { // declined
            response = filtered.stream().filter(a -> a.getLedgerEntries().size() > 0).sorted(
                    (a,b) -> b.getLedgerEntries().get(0).getModifiedOn().compareTo(a.getLedgerEntries().get(0).getModifiedOn())).collect(Collectors.toList());
        }


        return response;

    }

    private List<PaymentGroup> restrictAndEnrichPaymentGroups(List<PaymentGroup> paymentGroups) {
        final List<Organisation> filteredOrgList = getFilteredOrganisationsByPaymentGroups(paymentGroups);
        final List<PaymentGroup> filtered = filterAuthPaymentGroups(paymentGroups);

        final Map<Integer, Organisation> organisationMap = filteredOrgList
                .stream().collect(Collectors.toMap(Organisation::getId, Function.identity()));

        for (PaymentGroup paymentGroup : paymentGroups) {
            for (ProjectLedgerEntry payment : paymentGroup.getLedgerEntries()) {
                setAuthorisor(payment);
                setLastModifier(payment);
                setCreator(payment);
                setSapVendorId(payment, organisationMap);
            }
        }
        return filtered;
    }

    private ProjectLedgerEntry setSapVendorId(final ProjectLedgerEntry payment,
                                              final Map<Integer, Organisation> organisationMap) {
        if(LedgerStatus.Pending.equals(payment.getLedgerStatus())) {
            final Organisation org = organisationMap.get(payment.getOrganisationId());
            if(org != null) {
                payment.setSapVendorId(org.getsapVendorId());
            } else {
                log.warn(payment.getId()+" doesn't have organisation");
            }
        }
        return payment;
    }

    private List<PaymentSummary> filterAuth(
            final List<PaymentSummary> payments,
            final List<Organisation> filteredOrgList) {
        if(payments != null) {
            final Set<Integer> orgFilteredByAuth = filteredOrgList
                    .stream()
                    .map(Organisation::getId)
                    .collect(Collectors.toSet());

            //Returns all the payments which the user can access
            return payments.stream()
                    .filter(p-> orgFilteredByAuth.contains(p.getOrganisationId()))
                    .collect(Collectors.toList());

        }
        return null;
    }

    private List<PaymentGroup> filterAuthPaymentGroups(List<PaymentGroup> paymentGroups) {
        if(paymentGroups != null) {
            List<Integer> currentUserOrganisations = userService.currentUser().getOrganisationIds();

            //Returns all the payment groups which the user can access
            return paymentGroups.stream()
                    .filter(p -> currentUserOrganisations.contains(p.getLedgerEntries().get(0).getOrganisationId())
                            || currentUserOrganisations.contains(p.getLedgerEntries().get(0).getManagingOrganisationId()))
                    .collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    private List<Organisation> getFilteredOrganisations(List<PaymentSummary> payments) {
        ///Gets the list of id for the given list of payments
        final List<Integer> orgIdList = payments.stream()
                .map(PaymentSummary::getOrganisationId)
                .distinct()
                .filter(i-> i != null)
                .collect(Collectors.toList());

        // Return a list of organisation filtered by user's accessibility
        //based on id list
        return organisationService.find(orgIdList);
    }

    private List<Organisation> getFilteredOrganisationsByPaymentGroups(List<PaymentGroup> paymentGroups) {
        ///Gets the list of id for the given list of payments
        Set<Integer> orgIdList = new HashSet();
        for (PaymentGroup paymentGroup : paymentGroups) {
            if (!paymentGroup.getLedgerEntries().isEmpty()) {
                orgIdList.add(paymentGroup.getLedgerEntries().get(0).getOrganisationId());
            }
        }

        // Return a list of organisation filtered by user's accessibility
        //based on id list
        return organisationService.find(orgIdList);
    }


    ProjectLedgerEntry setAuthorisor(ProjectLedgerEntry payment) {
        if (payment.getAuthorisedBy() != null) {
            payment.setAuthorisor(userService.find(payment.getAuthorisedBy()).getFullName());
        }
        return payment;
    }

    ProjectLedgerEntry setLastModifier(ProjectLedgerEntry payment) {
        if (payment.getModifiedBy() != null) {
            payment.setModifiedByUser(userService.find(payment.getModifiedBy()));
        }
        return payment;
    }

    ProjectLedgerEntry setCreator(ProjectLedgerEntry payment) {
        if (payment.getCreatedBy() != null) {
            payment.setCreator(userService.find(payment.getCreatedBy()).getFullName());
        }
        return payment;
    }


    List<PaymentSummary> sortByIdDesc(List<PaymentSummary> requests) {
        requests.sort((req1, req2) -> req2.getId().compareTo(req1.getId()));
        return requests;
    }

    public ProjectLedgerEntry createReclaim(final Integer paymentId, final BigDecimal amount) {

        ProjectLedgerEntry payment = projectLedgerRepository.findOne(paymentId);



        validateManualReclaim(amount, payment);
        ProjectLedgerEntry reclaim = createReclaimPayment(amount, payment);
        ProjectLedgerEntry saved = financeService.save(reclaim);
        PaymentGroup pg = new PaymentGroup();
        pg.getLedgerEntries().add(saved);
        pg.setApprovalRequestedBy(userService.currentUser().getUsername());
        paymentGroupRepository.save(pg);
        return saved;
    }

    private ProjectLedgerEntry createReclaimPayment(BigDecimal amount, ProjectLedgerEntry payment) {
        OffsetDateTime now = environment.now();

        ProjectLedgerEntry reclaim = new ProjectLedgerEntry(
                payment.getProjectId(), now.getYear(), now.getMonthValue(),
                payment.getCategory(), "Reclaimed " + payment.getSubCategory(), amount, Pending);
        reclaim.setVendorName(payment.getVendorName());
        reclaim.setBlockId(payment.getBlockId());
        reclaim.setLedgerSource(LedgerSource.WebUI);
        reclaim.setManagingOrganisation(payment.getManagingOrganisation());
        reclaim.setProgrammeName(payment.getProgrammeName());
        reclaim.setSapVendorId(payment.getSapVendorId());
        reclaim.setCostCentreCode(payment.getCostCentreCode());
        reclaim.setDescription("Reclaim");
        reclaim.setLedgerType(payment.getLedgerType());
        reclaim.setCreatedOn(now);
        reclaim.setCreatedBy(userService.currentUser().getUsername());
        reclaim.setExternalId(payment.getExternalId());
        reclaim.setPcsPhaseNumber(payment.getPcsPhaseNumber());
        reclaim.setPcsProjectNumber(payment.getPcsProjectNumber());
        reclaim.setTransactionDate(new SimpleDateFormat("dd/MM/yyyy").format(Date.from(now.toInstant())));
        reclaim.setReclaimOfPaymentId(payment.getId());
        reclaim.setOrganisationId(payment.getOrganisationId());
        reclaim.setProjectName(payment.getProjectName());
        reclaim.setWbsCode(payment.getWbsCode());




        reclaim.updateValue(amount);
        return reclaim;
    }

    private void validateManualReclaim(BigDecimal amount, ProjectLedgerEntry payment) {


        if (payment == null) {
            throw new ValidationException("Unable to find matching original payment");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Reclaim amounts must be positive");
        }

        if (!LedgerStatus.getApprovedPaymentStatuses().contains(payment.getLedgerStatus())) {
            throw new ValidationException("Unable to perform reclaim on an unauthorised payment");
        }

        Project project = projectService.get(payment.getProjectId());
        if (!Project.Status.Closed.equals(project.getStatus())) {
            throw new ValidationException("Unable to create manual reclaims for projects that are not closed");
        }


        BigDecimal totalReclaims = payment.getValue(); // should be negative
        List<ProjectLedgerEntry> existingReclaims = projectLedgerRepository.findAllByReclaimOfPaymentId(payment.getId());

        for (ProjectLedgerEntry existingReclaim : existingReclaims) {
            if (!Declined.equals(existingReclaim.getLedgerStatus())) {
                totalReclaims = totalReclaims.add(existingReclaim.getValue());
            }
        }
        if (amount.add(totalReclaims).compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException("Unable to create reclaim as total reclaims would exceed total initial payment");
        }
    }

    public ProjectLedgerEntry create(final ProjectLedgerEntry projectLedgerEntry) {
        final Project project = validateProject(projectLedgerEntry);
        final ProjectLedgerEntry toSave = createPayment(
                project,
                projectLedgerEntry.getLedgerType(),
                projectLedgerEntry.getLedgerStatus(),
                projectLedgerEntry.getCategory(),
                projectLedgerEntry.getSubCategory(),
                projectLedgerEntry.getValue(),
                projectLedgerEntry.getYear(),
                projectLedgerEntry.getMonth(),
                projectLedgerEntry.getExternalId(),
                projectLedgerEntry.getLedgerSource());
        return financeService.save(toSave);
    }

    /**
     *
     * @param project Mandatory
     * @param type Optional. Default value PAYMENT
     * @param status Mandatory
     * @param category Mandatory
     * @param subCategory Mandatory
     * @param value Mandatory. Monetary amount
     * @param year Optional. Default now()
     * @param month Optional. Default now()
     * @param externalId Mandatory
     * @param source Optional. Default value webUI
     * @return
     */
    public ProjectLedgerEntry createPayment(final Project project,
                                            final LedgerType type,
                                            final LedgerStatus status,
                                            final String category,
                                            final String subCategory,
                                            final BigDecimal value,
                                            final Integer year,
                                            final Integer month,
                                            final Integer externalId,
                                            final LedgerSource source) {
        final User user = userService.currentUser();
        final Organisation organisation = getValidatedOrganisationForPayment(project);
        return initialisePayment(project, type, status, category, subCategory, value, year, month, externalId, source, user, organisation);
    }

    /**
     *
     * @param project Mandatory
     * @param type Optional. Default value PAYMENT
     * @param status Mandatory
     * @param category Mandatory
     * @param subCategory Mandatory
     * @param value Mandatory. Monetary amount
     * @param year Optional. Default now()
     * @param month Optional. Default now()
     * @param externalId Mandatory
     * @param source Optional. Default value webUI
     * @return
     */
    public ProjectLedgerEntry createReclaim(final Project project,
                                            final ProjectLedgerEntry originalPayment,
                                            final LedgerType type,
                                            final LedgerStatus status,
                                            final String category,
                                            final String subCategory,
                                            final BigDecimal value,
                                            final Integer year,
                                            final Integer month,
                                            final Integer externalId,
                                            final LedgerSource source) {
        final User user = userService.currentUser();
        final Organisation organisation = project.getOrganisation();
        ProjectLedgerEntry newReclaim = initialisePayment(project, type, status, category, subCategory, value, year, month, externalId, source, user, organisation);
        newReclaim.setReclaimOfPaymentId(originalPayment.getId());
        return newReclaim;
    }

    private ProjectLedgerEntry initialisePayment(Project project, LedgerType type, LedgerStatus status, String category, String subCategory, BigDecimal value, Integer year, Integer month, Integer externalId, LedgerSource source, User user, Organisation organisation) {
        final ProjectLedgerEntry payment = new ProjectLedgerEntry();
        payment.setManagingOrganisation(project.getManagingOrganisation());
        payment.setOrganisationId(organisation.getId());
        payment.setProjectId(project.getId());
        payment.setBlockId(project.getMilestonesBlock().getId());
        payment.setYear(year != null ? year : environment.now().getYear());
        payment.setMonth(month != null ? month : environment.now().getMonthValue());
        payment.setLedgerStatus(status);
        payment.setLedgerType(type == null ?  PAYMENT : type);
        payment.setCategory(category);
        payment.setSubCategory(subCategory);
        payment.updateValue(value);
        payment.setLedgerSource(source != null ? source : LedgerSource.WebUI);
        payment.setVendorName(organisation.getName());
        payment.setSapVendorId(organisation.getsapVendorId());
        payment.setExternalId(externalId);
        payment.setCreatedBy(user!=null? user.getUsername() : null);
        payment.setCreatedOn(environment.now());
        payment.setProgrammeName(project.getProgramme().getName());
        payment.setProjectName(project.getTitle());
        payment.setWbsCode(project.getProgramme().hasWbsCode() ? project.getProgramme().getWbsCode() : defaultWbsCode);
        return payment;
    }

    @Transactional(readOnly = true)
    public PaymentSummary getById(Integer id) {
        PaymentSummary paymentSummary = paymentSummaryRepository.findOne(id);
        if (paymentSummary == null) {
            throw new NotFoundException("No payment found with ID " + id);
        }
        return  paymentSummary;
    }

    public ProjectLedgerEntry getBySupplierInvoiceNumber(String supplierInvoiceNumber) {
        try {
            Integer paymentId = Integer.parseInt(supplierInvoiceNumber.split("-")[1]);
            return financeService.findOne(paymentId);
        }
        catch (Exception e) {
            return null;
        }
    }

    private Organisation getValidatedOrganisationForPayment(final Project project) {
        final Organisation organisation = organisationService.getOrganisationForProject(project);

        if (organisation == null) {
            throw new ValidationException("payment must have a valid organisation ID!");
        }
        return organisation;
    }

    private Project validateProject(ProjectLedgerEntry ProjectLedgerEntry) {
        Project project = projectService.get(ProjectLedgerEntry.getProjectId());

        if (project == null) {
            throw new ValidationException("payment must have a valid project ID!");
        }

        return project;
    }


    /**
     * Authorise all the payments for the given group
     * @param groupId id of the grou of payments
     * @return list of authorised payments
     */
    @Transactional
    public List<ProjectLedgerEntry> authoriseByGroupId(final int groupId) throws ForbiddenAccessException {
        PaymentGroup group = getPaymentGroupOnlyPending(groupId);
        return authoriseByGroup(group);
    }

    @Transactional
    public List<ProjectLedgerEntry> authoriseByGroup(PaymentGroup paymentGroup) throws ForbiddenAccessException {
        checkAuthorisePermission(paymentGroup);

        paymentGroup = authorisePaymentGroup(paymentGroup);
        final List<ProjectLedgerEntry> authorisedPayments = paymentGroup.getLedgerEntries();

        final Set<Integer> authorisedPaymentIdsSet = authorisedPayments.stream()
                .map(ProjectLedgerEntry::getId)
                .collect(Collectors.toSet());
        final Set<Integer> expectedAuthorisedPaymentIdsSet = paymentGroup.getLedgerEntries().stream()
                .map(ProjectLedgerEntry::getId)
                .collect(Collectors.toSet());
        if(!expectedAuthorisedPaymentIdsSet.equals(authorisedPaymentIdsSet)) {
            throw new RuntimeException("No all payments can be authorised. Rollback");
        }
        final Set<Integer> projectIdSet = authorisedPayments.stream()
                .map(ProjectLedgerEntry::getProjectId)
                .collect(Collectors.toSet());
        projectService.refreshProjectStatus(projectIdSet, EventType.PaymentAuthorised);
        auditService.auditCurrentUserActivity("Authorised payments for payment group: " + paymentGroup.getId());
        return authorisedPayments;

    }

    void checkAuthorisePermission(PaymentGroup group) {
        if (!permissionService.currentUserHasPermissionForOrganisation(PermissionService.AUTHORISE_PAYMENT, group.getLedgerEntries().get(0).getManagingOrganisationId())) {
            throw new ForbiddenAccessException("User does not have permission to authorise payments for organisation " + group.getId());
        }
    }

    private PaymentGroup getPaymentGroupOnlyPending(int groupId) {
        final PaymentGroup paymentGroup = paymentGroupRepository.findOne(groupId);
        if(paymentGroup == null) {
            throw new ValidationException("Unable to retrieve payment group with id: " + groupId);
        }

        if(paymentGroup.getLedgerEntries() != null) {
            final boolean noPending = paymentGroup.getLedgerEntries().stream()
                    .anyMatch(p-> !Pending.equals(p.getLedgerStatus()));
            if(noPending) {
                throw new ValidationException("This payment group has non pending payments");
            }

        }
        return paymentGroup;
    }


    PaymentGroup authorisePaymentGroup(PaymentGroup paymentGroup) {
        List<ProjectLedgerEntry> list = paymentGroup.getLedgerEntries();
        if(CollectionUtils.isEmpty(list)) {
            throw new ValidationException("There are no payments");
        }

        final Project project = projectService.get(list.get(0).getProjectId());

        if (list.get(0).isReclaim()) {
            validateReclaim(project ,list);
        } else {
            validatePayment(project);
        }

        final User user = userService.currentUser();
        list.forEach(e-> setStatus(e, Authorised, user));
        financeService.save(list);
        return paymentGroupRepository.findOne(paymentGroup.getId());
    }

    private void validateReclaim(Project project, List<ProjectLedgerEntry> list) {

        // should only ever be one
        for (ProjectLedgerEntry thisReclaim : list) {
            if (thisReclaim.getLedgerType().equals(PAYMENT) && thisReclaim.isReclaim())  {
                checkSAPVendorId(project);
            }

            if (thisReclaim.getLedgerType().equals(PAYMENT) && (thisReclaim.getInterest() == null || thisReclaim.getInterest().compareTo(BigDecimal.ZERO) < 0)) {
                throw new ValidationException("A positive interest amount must be set for reclaim payments");
            }
            List<ProjectLedgerEntry> allApprovedReclaims = projectLedgerRepository.findAllByReclaimOfPaymentId(thisReclaim.getReclaimOfPaymentId());
            ProjectLedgerEntry originalPayment = projectLedgerRepository.findOne(thisReclaim.getReclaimOfPaymentId());

            Set<LedgerStatus> approvedPaymentStatuses = LedgerStatus.getApprovedPaymentStatuses();

            BigDecimal totalExistingApprovedReclaims = allApprovedReclaims.stream().filter(p -> approvedPaymentStatuses.contains(p.getLedgerStatus())).
                    map(ProjectLedgerEntry::getValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);


            // orginal payment is negative, add all existing reclaims and this reclaim to ensure is not greater than 0
            if (originalPayment.getValue().add(totalExistingApprovedReclaims).add(thisReclaim.getValue()).compareTo(BigDecimal.ZERO) > 0 ) {
                throw new ValidationException("Unable to approve this reclaim as it would exceed the original payment amount");
            }

        }
    }

    private void validatePayment(Project project) {
        final Map<GrantType, BigDecimal> amounts = new HashMap<GrantType, BigDecimal>(){{
            put(GrantType.Grant, new BigDecimal(0));
            put(GrantType.DPF, new BigDecimal(0));
            put(GrantType.RCGF, new BigDecimal(0));
        }};
        for(final ProjectLedgerEntry e: projectLedgerRepository.findAllByProjectId(project.getId())) {
            if (!LedgerStatus.Declined.equals(e.getLedgerStatus())) {
                switch (e.getLedgerType()) {
                    case PAYMENT:
                        amounts.put(GrantType.Grant, amounts.get(GrantType.Grant).add(e.getValue()));
                        break;
                    case DPF:
                        amounts.put(GrantType.DPF, amounts.get(GrantType.DPF).add(e.getValue()));
                        break;
                    case RCGF:
                        amounts.put(GrantType.RCGF, amounts.get(GrantType.RCGF).add(e.getValue()));
                        break;
                }
            }

        }

        // for Grant payments as we send it to SAP we want to validate the organisation has a SAP ID set
        if (amounts.get(GrantType.Grant).compareTo(BigDecimal.ZERO) > 0) {
            checkSAPVendorId(project);
        }

        final Map<GrantType, BigDecimal> maxAmounts = new HashMap<GrantType, BigDecimal>(){{
            put(GrantType.Grant, new BigDecimal(project.getGrantsRequested().get(GrantType.Grant)));
            put(GrantType.DPF, new BigDecimal(project.getGrantsRequested().get(GrantType.DPF)));
            put(GrantType.RCGF, new BigDecimal(project.getGrantsRequested().get(GrantType.RCGF)));
        }};
        for(final GrantType type : GrantType.values()) {
            if (amounts.get(type).negate().compareTo(maxAmounts.get(type)) > 0) {
                throw new ValidationException(
                        "Payment cannot be authorised as the payment amount is greater than the remaining " +
                                maxAmounts.get(type)+" balance");
            }
        }
    }

    private void checkSAPVendorId(Project project) {
        final Organisation organisation = organisationService.getOrganisationForProject(project);
        if(GlaOpsUtils.isNullOrEmpty(organisation.getsapVendorId())) {
            throw new ValidationException("There is no SAP vendor id");
        }
    }


    public ProjectLedgerEntry setStatus(Integer paymentId, String requestedStatus, User authorisingUser) {
        ProjectLedgerEntry ProjectLedgerEntry = financeService.findOne(paymentId);
        if (ProjectLedgerEntry == null) {
            throw new NotFoundException("No payment found with ID " + paymentId);
        }
        return setStatus(ProjectLedgerEntry, LedgerStatus.valueOf(requestedStatus), authorisingUser);
    }

    /**
     * Set the status of a payment request, including authorisations.
     */

    public ProjectLedgerEntry setStatus(ProjectLedgerEntry projectLedgerEntry, LedgerStatus newStatus, User authorisingUser) {
        if (newStatus.equals(Authorised)) {

            if (!projectLedgerEntry.getLedgerStatus().equals(LedgerStatus.Pending)) {
                throw new ValidationException("Only pending payment requests can be authorised");
            }

            if (authorisingUser == null) {
                throw new ValidationException("Authorising user must be specified");
            }
            projectLedgerEntry.setAuthorisedOn(environment.now());
            projectLedgerEntry.setAuthorisedBy(authorisingUser.getUsername());

        } else if (newStatus.equals(LedgerStatus.Acknowledged)) {

            if (projectLedgerEntry.getLedgerStatus().equals(LedgerStatus.Pending)) {
                throw new ValidationException("Pending payment requests cannot be acknowledged");
            }

            projectLedgerEntry.setAcknowledgedOn(environment.now());

        } else if (newStatus.equals(LedgerStatus.Cleared)) {

            if (projectLedgerEntry.getLedgerStatus().equals(LedgerStatus.Pending)) {
                throw new ValidationException("Pending payment requests cannot be cleared");
            }

            projectLedgerEntry.setClearedOn(environment.now());

        } else if (newStatus.equals(LedgerStatus.UnderReview)) {
            // Nothing extra to do
        } else if (newStatus.equals(LedgerStatus.SupplierError)) {
            // Nothing extra to do
        } else {
            throw new ValidationException("Status transition not currently supported: " + newStatus);
        }

        projectLedgerEntry.setLedgerStatus(newStatus);
        return financeService.save(projectLedgerEntry);
    }

    public List<ProjectLedgerEntry> getPendingLedgerEntries() {
        return  financeService.findByStatus(Pending);
    }

    @LogMetrics
    public boolean hasPendingPayments(Integer projectId) {
        return projectLedgerRepository.countByProjectIdAndLedgerStatus(projectId, Pending) > 0;
    }

    public boolean hasReclaims(Integer projectId) {
        return projectLedgerRepository.countByProjectIdAndReclaimOfPaymentIdNotNull(projectId) > 0;
    }

    public boolean hasPayments(Integer projectId) {
        return projectLedgerRepository.countByProjectIdAndLedgerStatusIn(projectId, ALL_PAYMENTS.getRelevantStatusesAsList()) > 0;
    }

    public PaymentGroup declinePaymentsByGroupId(int groupToUpdateId, PaymentGroup group) {

        PaymentGroup groupToUpdate = getPaymentGroupOnlyPending(groupToUpdateId);
        return declinePaymentsByGroup(groupToUpdate, group);

    }


    public PaymentGroup declinePaymentsByGroup(PaymentGroup groupToUpdate, final PaymentGroup group) throws ForbiddenAccessException {

        if (!permissionService.currentUserHasPermissionForOrganisation(PermissionService.AUTHORISE_PAYMENT, groupToUpdate.getLedgerEntries().get(0).getManagingOrganisationId())) {
            throw new ForbiddenAccessException("User does not have permission to decline payments for organisation " + groupToUpdate.getId());
        }

        if (group.getDeclineReason() == null || group.getDeclineReason().getCategory() == null ||
                group.getDeclineReason().getId() == null) {
            throw new ValidationException("Decline reason is mandatory when declining payments");
        }

        final CategoryValue categoryValue = refDataService.getCategoryValue(group.getDeclineReason().getId());

        if (categoryValue == null || !Arrays.asList(PaymentDeclineReason, ReclaimDeclineReason).contains(categoryValue.getCategory())) {
            throw new ValidationException("Decline reason is not valid");
        }

        groupToUpdate.getLedgerEntries().forEach(ple -> ple.setLedgerStatus(Declined));
        projectLedgerRepository.save(groupToUpdate.getLedgerEntries());
        groupToUpdate.setDeclineReason(categoryValue);
        groupToUpdate.setDeclineComments(group.getDeclineComments());
        paymentGroupRepository.save(groupToUpdate);

        final Set<Integer> projectIdSet = groupToUpdate.getLedgerEntries().stream()
                .map(ProjectLedgerEntry::getProjectId)
                .collect(Collectors.toSet());
        projectService.refreshProjectStatus(projectIdSet, EventType.PaymentDeclined);
        auditService.auditCurrentUserActivity(String.format("Declined payment for payment group: %d, with reason: %s ",
                        groupToUpdate.getId(), groupToUpdate.getDeclineComments()));
        return groupToUpdate;
    }


    public PaymentGroup generatePaymentsForClaimedMilestones(final Project project, String approvalRequestedBy) {
        PaymentGroup paymentGroup = new PaymentGroup();
        paymentGroup.setApprovalRequestedBy(approvalRequestedBy);

        boolean approvalWillCreatePendingReclaim = project.getApprovalWillCreatePendingReclaim();

        Long grantSourceAdjustmentAmount = project.getGrantSourceAdjustmentAmount();
        List<ProjectLedgerEntry> adjustments = new ArrayList<>();
        project.getMilestonesBlock().getMilestones().stream().
                filter(m -> !ClaimStatus.Pending.equals(m.getClaimStatus())).
                sorted(Comparator.comparing(Milestone::getMilestoneDate)).forEach( milestone -> {
            if (ClaimStatus.Claimed.equals(milestone.getClaimStatus())) {

                Long milestoneGrantClaimed = project.getMilestonesBlock().getMilestoneGrantClaimed(milestone.getId());
                milestone.setClaimedGrant(milestoneGrantClaimed);
                List<ProjectLedgerEntry> entries = projectLedgerEntryMapper.map(project, project.getMilestonesBlock(), milestone.getId());
                for (ProjectLedgerEntry entry : entries) {
                    projectLedgerRepository.save(entry); // not using save all as through AOP creation data will be set
                }
                paymentGroup.getLedgerEntries().addAll(entries);
            }

            List<ProjectLedgerEntry> approvedPayments =
                    projectLedgerRepository.findByProjectIdAndSubCategoryAndLedgerStatusIn(project.getId(), milestone.getSummary(), LedgerStatus.getApprovedPaymentStatuses());

            if (approvalWillCreatePendingReclaim) { // safety check for creating incorrect reclaims
                    if (milestone.getReclaimedDpf() != null) {
                        createReclaimForType(project, adjustments, milestone, approvedPayments, DPF);
                    }

                if (milestone.getReclaimedRcgf() != null) {
                    createReclaimForType(project, adjustments, milestone, approvedPayments, RCGF);
                }
            }

            if (isPaymentAdjustmentRequired(grantSourceAdjustmentAmount, milestone)) {

                Long currentValue = milestone.getClaimedGrant();
                Long newValue = project.getMilestonesBlock().getValueForMonetarySplitForMilestone(milestone);

                long adjustmentValue = currentValue - newValue;
                ProjectLedgerEntry projectLedgerEntry = null;

                if (adjustmentValue < 0) {
                    projectLedgerEntry = projectLedgerEntryMapper.generateSupplementalPaymentForMilestone(project, milestone, adjustmentValue);
                } else {

                    ProjectLedgerEntry originalPaymentForReclaim = getOriginalPaymentForReclaim(approvedPayments, PAYMENT);
                    projectLedgerEntry = projectLedgerEntryMapper.generateReclaimPaymentForMilestone(project, milestone, milestone.getReclaimedGrant(), originalPaymentForReclaim, PAYMENT);

                }

                if (projectLedgerEntry != null) {
                    projectLedgerRepository.save(projectLedgerEntry);
                    adjustments.add(projectLedgerEntry);
                }
            }

        });

        // supplemental payments are added after all milestone payments
        paymentGroup.getLedgerEntries().addAll(adjustments);

        return paymentGroupRepository.save(paymentGroup);
    }

    private void createReclaimForType(Project project, List<ProjectLedgerEntry> adjustments, Milestone milestone, List<ProjectLedgerEntry> approvedPayments, LedgerType type) {
        ProjectLedgerEntry originalPaymentForReclaim = getOriginalPaymentForReclaim(approvedPayments, type);

        ProjectLedgerEntry projectLedgerEntry = null;
        switch (type) {
            case DPF:
                projectLedgerEntry = projectLedgerEntryMapper.generateReclaimPaymentForMilestone(project, milestone, milestone.getReclaimedDpf(), originalPaymentForReclaim, type);
                break;
            case RCGF:
                projectLedgerEntry = projectLedgerEntryMapper.generateReclaimPaymentForMilestone(project, milestone, milestone.getReclaimedRcgf(), originalPaymentForReclaim, type);
                break;
        }

        if (projectLedgerEntry != null) {
            projectLedgerRepository.save(projectLedgerEntry);
            adjustments.add(projectLedgerEntry);
        }
    }

    private ProjectLedgerEntry getOriginalPaymentForReclaim(List<ProjectLedgerEntry> approvedPayments, LedgerType ledgerType) {
        Optional<ProjectLedgerEntry> first = approvedPayments.stream()
                .filter(p -> LedgerStatus.getApprovedPaymentStatuses().contains(p.getLedgerStatus()))
                .filter(p -> "Milestone".equals(p.getCategory()))
                .filter(p -> ledgerType.equals(p.getLedgerType()))
                .findFirst();
        if (!first.isPresent()) {
            throw new ValidationException("Unable to find original payment record for reclaim.");
        }
        return first.get();
    }

    private boolean isPaymentAdjustmentRequired(Long grantSourceAdjustmentAmount, Milestone milestone) {
        return grantSourceAdjustmentAmount != 0 && ClaimStatus.Approved.equals(milestone.getClaimStatus()) &&
                milestone.getMonetary() && milestone.getMonetarySplit() !=null && milestone.getMonetarySplit() > 0;
    }

    @Transactional(readOnly = true)
    public PaymentGroup getGroupById(Integer id) {
        final PaymentGroup paymentGroup = paymentGroupRepository.findOne(id);
        if(paymentGroup == null) {
            throw new ValidationException("Payment group not found for id "+id);
        }

        List<PaymentGroup> list = new ArrayList<>();
        list.add(paymentGroup);
        List<PaymentGroup> paymentGroups = restrictAndEnrichPaymentGroups(list);

        if(paymentGroups.size() == 0) {
            throw new ValidationException("Payment group not found for id "+id);
        }

        return paymentGroups.get(0);
    }

    public PaymentGroup getGroupByPaymentId(Integer paymentId) {

        PaymentGroup group = paymentGroupRepository.findPaymentGroupByPaymentId(paymentId);
        List<PaymentGroup> response = new ArrayList<>();
        response.add(group);
        response = restrictAndEnrichPaymentGroups(response);
        if (response != null && response.size() == 1) {
            return response.get(0);
        }
        return null;
    }


    public void setReclaimInterestForPayments(Map<Integer, BigDecimal> map) {
        for (Integer paymentId : map.keySet()) {
            this.setReclaimInterest(paymentId, map.get(paymentId));
        }

    }

    public ProjectLedgerEntry setReclaimInterest(Integer reclaimPaymentId, BigDecimal value) {

        ProjectLedgerEntry reclaim = projectLedgerRepository.findOne(reclaimPaymentId);
        if (!reclaim.isReclaim()) {
            throw new ValidationException("Can only add interest to reclaim payment types");
        }

       if (!reclaim.getLedgerStatus().equals(Pending)) {
            throw new ValidationException("Can only add interest to a pending reclaim payment");
        }

        if (value.compareTo(BigDecimal.ZERO) < 0)  {
            throw new ValidationException("Interest amount must be positive");
        }

        reclaim.setInterest(value);
        return projectLedgerRepository.save(reclaim);
    }

    public void clonePaymentGroupsForBlock(Integer originalBlockId, Integer newProjectId, Integer newBlockId) {
        List<PaymentGroup> paymentGroups = paymentGroupRepository.findAllByBlockId(originalBlockId);
        for (PaymentGroup originalPaymentGroup: paymentGroups) {
            List<ProjectLedgerEntry> entries = new ArrayList<>();
            for (ProjectLedgerEntry entry: originalPaymentGroup.getLedgerEntries()) {
                entries.add(financeService.clone(entry, newProjectId, newBlockId));
            }
            projectLedgerRepository.save(entries);

            PaymentGroup clonePaymentGroup = new PaymentGroup();
            clonePaymentGroup.setDeclineReason(originalPaymentGroup.getDeclineReason());
            clonePaymentGroup.setDeclineComments(originalPaymentGroup.getDeclineComments());
            clonePaymentGroup.setApprovalRequestedBy(originalPaymentGroup.getApprovalRequestedBy());
            clonePaymentGroup.setLedgerEntries(entries);
            paymentGroupRepository.save(clonePaymentGroup);
        }
    }

}
