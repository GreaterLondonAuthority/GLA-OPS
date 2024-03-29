/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.enums.GrantType;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.notification.EmailService;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.OrganisationGroupService;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.organisation.model.OrganisationGroup;
import uk.gov.london.ops.payment.implementation.ProjectLedgerEntryMapper;
import uk.gov.london.ops.payment.implementation.repository.PaymentGroupRepository;
import uk.gov.london.ops.payment.implementation.repository.PaymentSummaryRepository;
import uk.gov.london.ops.payment.implementation.repository.ProjectLedgerRepository;
import uk.gov.london.ops.permission.PermissionService;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.accesscontrol.DefaultAccessControlSummary;
import uk.gov.london.ops.project.block.FundingSourceProvider;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.claim.Claim;
import uk.gov.london.ops.project.claim.ClaimStatus;
import uk.gov.london.ops.project.claim.ClaimType;
import uk.gov.london.ops.project.funding.FundingActivity;
import uk.gov.london.ops.project.funding.FundingBlock;
import uk.gov.london.ops.project.funding.ProjectFundingService;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.skills.SkillsPaymentScheduler;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.refdata.CategoryValue;
import uk.gov.london.ops.refdata.PaymentSource;
import uk.gov.london.ops.refdata.RefDataServiceImpl;
import uk.gov.london.ops.service.DataAccessControlService;
import uk.gov.london.ops.user.User;
import uk.gov.london.ops.user.UserFinanceThresholdService;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;
import uk.gov.london.ops.user.domain.UserOrgFinanceThreshold;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.london.ops.framework.OPSUtils.toMonetaryString;
import static uk.gov.london.ops.notification.NotificationType.PaymentAuthorisation;
import static uk.gov.london.ops.payment.LedgerStatus.Acknowledged;
import static uk.gov.london.ops.payment.LedgerStatus.Authorised;
import static uk.gov.london.ops.payment.LedgerStatus.Cleared;
import static uk.gov.london.ops.payment.LedgerStatus.Declined;
import static uk.gov.london.ops.payment.LedgerStatus.FORECAST;
import static uk.gov.london.ops.payment.LedgerStatus.Pending;
import static uk.gov.london.ops.payment.LedgerStatus.Sent;
import static uk.gov.london.ops.payment.LedgerStatus.SupplierError;
import static uk.gov.london.ops.payment.LedgerStatus.UnderReview;
import static uk.gov.london.ops.payment.LedgerType.DPF;
import static uk.gov.london.ops.payment.LedgerType.PAYMENT;
import static uk.gov.london.ops.payment.LedgerType.RCGF;
import static uk.gov.london.ops.payment.PaymentFilterOption.ALL_PAYMENTS;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.SUPPLEMENTARY_PAYMENT;
import static uk.gov.london.ops.permission.PermissionType.AUTHORISE_PAYMENT;
import static uk.gov.london.ops.project.skills.SkillsPaymentScheduler.SCHEDULED_PAYMENT_CATEGORY;
import static uk.gov.london.ops.refdata.CategoryValue.Category.PaymentDeclineReason;
import static uk.gov.london.ops.refdata.CategoryValue.Category.ReclaimDeclineReason;

@Service
public class PaymentService {

    private static final String MILESTONE = "Milestone";
    private static final String IMS_CLAIMED_MILESTONE = "IMS Claimed Milestone";
    Logger log = LoggerFactory.getLogger(getClass());

    private static final Set<LedgerStatus> states_that_can_be_changed_from = Stream.of(
            Sent, UnderReview, SupplierError, Acknowledged).collect(Collectors.toSet());

    private static final Set<LedgerStatus> states_that_can_be_changed_to = Stream.of(
            Sent, UnderReview, SupplierError, Acknowledged, Cleared).collect(Collectors.toSet());

    @Autowired
    ProjectLedgerEntryMapper projectLedgerEntryMapper;

    @Autowired
    EntityManager entityManager;

    @Autowired
    PaymentSummaryRepository paymentSummaryRepository;

    @Autowired
    ProjectLedgerRepository projectLedgerRepository;

    @Autowired
    PaymentGroupRepository paymentGroupRepository;

    @Autowired
    FinanceService financeService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    OrganisationServiceImpl organisationService;

    @Autowired
    ProjectService projectService;

    @Autowired
    Environment environment;

    @Autowired
    RefDataServiceImpl refDataService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    UserFinanceThresholdService userFinanceThresholdService;

    @Autowired
    EmailService emailService;

    @Autowired
    AuditService auditService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    AuthorisedPaymentsProcessor processor;

    @Autowired
    OrganisationGroupService organisationGroupService;

    @Autowired
    SkillsPaymentScheduler skillsPaymentScheduler;

    @Autowired
    ProjectFundingService projectFundingService;

    @Autowired
    PaymentAuditService paymentAuditService;

    @Autowired
    DataAccessControlService dataAccessControlService;

    public Page<PaymentSummary> findAll(String projectIdOrName,
                                        String organisationName,
                                        String programmeName,
                                        String sapVendorId,
                                        List<String> paymentSources,
                                        List<LedgerStatus> relevantStatuses,
                                        List<String> categories,
                                        List<String> relevantProgrammes,
                                        List<Integer> managingOrganisations,
                                        OffsetDateTime fromDate,
                                        OffsetDateTime toDate,
                                        List<String> paymentDirection,
                                        Pageable pageable
                                            ) {
        Map<String, PaymentSource> paymentSourceMap = refDataService.getPaymentSourceMap();
        if (paymentSources == null || paymentSources.isEmpty()) {
            paymentSources = paymentSourceMap.values().stream().map(PaymentSource::getName).collect(Collectors.toList());
        }

        UserEntity currentUser = userService.currentUser();
        List<DefaultAccessControlSummary> dac = dataAccessControlService.getDefaultAccessForOrgs(currentUser.getOrganisationIds());

        Page<PaymentSummary> payments = paymentSummaryRepository.findAll(projectIdOrName,
                organisationName,
                programmeName,
                sapVendorId,
                paymentSources,
                relevantStatuses,
                categories,
                relevantProgrammes,
                fromDate,
                toDate,
                currentUser.getOrganisationIds(),
                managingOrganisations,
                dac,
                paymentDirection,
                pageable);

        payments.stream().forEach(p -> enrichPaymentSummary(p, paymentSourceMap));

        return payments;
    }

    private void enrichPaymentSummary(PaymentSummary summary, Map<String, PaymentSource> paymentSourceMap) {
        summary.setCreatorName(userService.getUserFullName(summary.getCreatedBy()));
        summary.setLastModifierName(userService.getUserFullName(summary.getModifiedBy()));
        summary.setResenderName(userService.getUserFullName(summary.getResender()));
        summary.setPaymentSourceDetails(paymentSourceMap.get(summary.getPaymentSource()));
    }

    public List<PaymentGroupEntity> findAllPaymentGroups() {
        return paymentGroupRepository.findAll();
    }

    public Set<PaymentGroupEntity> findAllByBlockId(Integer blockId) {
        return paymentGroupRepository.findAllByBlockId(blockId);
    }

    @Transactional
    public List<PaymentGroupEntity> findAllPaymentGroupsByStatusFast(PaymentFilterOption status) {

        User user = userService.currentUser();
        List<String> statusList = status == null ? ALL_PAYMENTS.getRelevantStatusesAsString(): status.getRelevantStatusesAsString();
        List<PaymentGroupPayment> allWithPayments = paymentGroupRepository.findAllWithPayments(user.getUsername(), user.isOpsAdmin(), user.getAccessibleOrganisationIds(), statusList);
        Map<String, PaymentSource> paymentSourceMap = refDataService.getPaymentSourceMap();
        Map<Integer, UserOrgFinanceThreshold> thresholds = getThresholds();

        Map<Integer, PaymentGroupEntity> paymentGroupMap = new LinkedHashMap<>();
        for (PaymentGroupPayment allWithPayment : allWithPayments) {
            PaymentGroupEntity group = paymentGroupMap.computeIfAbsent(allWithPayment.getPaymentGroupId(),
                            k -> allWithPayment.createPaymentGroup());
            ProjectLedgerEntry projectLedgerEntry = allWithPayment.createProjectLedgerEntry();
            group.getLedgerEntries().add(projectLedgerEntry);
            // update ple vendor ID if required
            if (allWithPayment.getOrgSapId() != null && allWithPayment.getSapVendorId() == null) {
                projectLedgerEntry.setSapVendorId(allWithPayment.getOrgSapId());
                projectLedgerRepository.updateSapVendorId(allWithPayment.getOrgSapId(), allWithPayment.getPaymentId());
            }
            group.getPayments().add(PaymentSummary.createFrom(projectLedgerEntry));
            group.getPayments().forEach(p -> p.setPaymentSourceDetails(paymentSourceMap.get(p.getPaymentSource())));
            setProjectPaymentSuspendFlag(group);

        }

        paymentGroupMap.values().forEach(g -> setPaymentGroupThresholdExceededFlag(g, thresholds, g.getMaxRequestedPayment() ));

        return new ArrayList<>(paymentGroupMap.values());
    }



    public List<PaymentGroupEntity> findAllPaymentGroupsByStatus(PaymentFilterOption status) {
        List<PaymentGroupEntity> paymentGroups;
        if (status != null) {
            paymentGroups = paymentGroupRepository.findAllByStatusIn(Arrays.stream(status.getRelevantStatuses())
                    .map(LedgerStatus::name).toArray(String[]::new));
        } else {
            paymentGroups = paymentGroupRepository.findAll();
        }
        List<PaymentGroupEntity> filtered = restrictAndEnrichPaymentGroups(paymentGroups);

        // should not be any payment groups without payments but remove them just in case
        // sort rules are dependant on the payment status
        List<PaymentGroupEntity> response;
        if (!(PaymentFilterOption.AUTHORISED.equals(status) || PaymentFilterOption.DECLINED.equals(status))) {
            response = filtered.stream().filter(a -> a.getLedgerEntries().size() > 0).sorted(
                    (a, b) -> b.getLedgerEntries().get(0).getCreatedOn().compareTo(a.getLedgerEntries().get(0).getCreatedOn()))
                    .collect(Collectors.toList());
        } else if (PaymentFilterOption.AUTHORISED.equals(status)) {
            response = filtered.stream().filter(a -> a.getLedgerEntries().size() > 0).sorted(
                    (a, b) -> b.getLedgerEntries().get(0).getAuthorisedOn()
                            .compareTo(a.getLedgerEntries().get(0).getAuthorisedOn()))
                    .collect(Collectors.toList());
        } else { // declined
            response = filtered.stream().filter(a -> a.getLedgerEntries().size() > 0).sorted(
                    (a, b) -> b.getLedgerEntries().get(0).getModifiedOn().compareTo(a.getLedgerEntries().get(0).getModifiedOn()))
                    .collect(Collectors.toList());
        }
        return response;
    }

    List<PaymentGroupEntity> restrictAndEnrichPaymentGroups(List<PaymentGroupEntity> paymentGroups) {
        Map<String, PaymentSource> paymentSourceMap = refDataService.getPaymentSourceMap();

        Map<Integer, UserOrgFinanceThreshold> thresholds = getThresholds();

        Map<Integer, OrganisationEntity> organisationMap = getFilteredOrganisationsByPaymentGroups(paymentGroups);

        List<PaymentGroupEntity> filtered = filterAuthPaymentGroups(paymentGroups);
        for (PaymentGroupEntity paymentGroup : filtered) {
            long max = 0L;
            for (ProjectLedgerEntry payment : paymentGroup.getLedgerEntries()) {
                if (!payment.isReclaim() && payment.getValue().negate().compareTo(BigDecimal.valueOf(max)) > 0) {
                    max = payment.getValue().negate().longValue();
                }
                setAuthorisor(payment);
                setLastModifier(payment);
                setCreator(payment);
                setSapVendorId(payment, organisationMap);
                payment.setPaymentSourceDetails(paymentSourceMap.get(payment.getPaymentSource()));
            }
            paymentGroup.getPayments().forEach(p -> p.setPaymentSourceDetails(paymentSourceMap.get(p.getPaymentSource())));

            setPaymentGroupThresholdExceededFlag(paymentGroup, thresholds, max);
            setProjectPaymentSuspendFlag(paymentGroup);
        }
        return filtered;
    }

    private void setProjectPaymentSuspendFlag(PaymentGroupEntity paymentGroup) {
        paymentGroup.setSuspendPayments(projectService.isPaymentsSuspended(paymentGroup.getProjectId()));
    }

    private Map<Integer, UserOrgFinanceThreshold> getThresholds() {
        return userFinanceThresholdService.getFinanceThresholds(userService.currentUsername()).stream()
                .collect(Collectors.toMap(u -> u.getId().getOrganisationId(), Function.identity()));
    }

    private void setPaymentGroupThresholdExceededFlag(PaymentGroupEntity paymentGroup,
                                                      Map<Integer, UserOrgFinanceThreshold> thresholds,
                                                      long max) {
        Integer projectId = paymentGroup.getProjectId();
        UserOrgFinanceThreshold orgThreshold = userFinanceThresholdService.getFinanceThresholdForProject(thresholds, projectId);
        if (orgThreshold != null) {
            paymentGroup.updateLedgerEntriesThresholdDataFrom(orgThreshold);
            if (paymentGroup.isOnlyReclaimPayments()) {
                paymentGroup.setThresholdExceeded(false);
            } else if (orgThreshold.isPending()) {
                paymentGroup.setThresholdExceeded(true);
            } else {
                if (max > orgThreshold.getApprovedThreshold()) {
                    paymentGroup.setThresholdExceeded(true);
                }
            }
        }
    }

    void setSapVendorId(ProjectLedgerEntry payment, Map<Integer, OrganisationEntity> organisationMap) {
        if (LedgerStatus.Pending.equals(payment.getLedgerStatus()) && GlaUtils.isNullOrEmpty(payment.getSapVendorId())) {
            final OrganisationEntity org = organisationMap.get(payment.getOrganisationId());
            if (org != null) {
                payment.setSapVendorId(getProjectSapVendorId(payment.getProjectId(), org));
            } else {
                log.warn(payment.getId() + " doesn't have organisation");
            }
        }
    }

    String getProjectSapVendorId(Integer projectId, OrganisationEntity org) {
        return projectService.getProjectSapId(projectId, org);
    }

    /***
     * This method will filter the payments groups based on the user access.
     *
     *  An user has access to payments on multiple ways:
     *   1. User has right role on the organisation of payments
     *   2. User has right role on the managing organisation of payments
     *   3. User was granted a default access control to specific template so can see all payments
     *     for that specific programme-template-organisation/team combination
     *
     * @param paymentGroups list of payment groups
     * @return filtered paymentGroups
     */
    List<PaymentGroupEntity> filterAuthPaymentGroups(List<PaymentGroupEntity> paymentGroups) {
        if (paymentGroups != null) {
            List<Integer> currentUserOrganisations = userService.currentUser().getOrganisationIds();
            List<DefaultAccessControlSummary> dac = dataAccessControlService.getDefaultAccessForOrgs(currentUserOrganisations);

            return paymentGroups.stream()
                .filter(p -> {
                    boolean hasAccess = !p.getPayments().isEmpty();
                    if (hasAccess) {
                        PaymentSummary payment = p.getPayments().get(0);
                        hasAccess = currentUserOrganisations.contains(payment.getOrganisationId())
                            || currentUserOrganisations.contains(payment.getManagingOrganisationId());

                        for (DefaultAccessControlSummary accessControl : dac) {
                            hasAccess = hasAccess
                                    || (payment.getManagingOrganisationId().equals(accessControl.getManagingOrganisationId())
                                    && payment.getProgrammeId().equals(accessControl.getProgrammeId())
                                    && payment.getTemplateId().equals(accessControl.getTemplateId()));
                        }
                    }
                    return hasAccess;
                }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Map<Integer, OrganisationEntity> getFilteredOrganisationsByPaymentGroups(List<PaymentGroupEntity> paymentGroups) {
        ///Gets the list of id for the given list of payments
        Set<Integer> orgIdList = new HashSet<>();
        for (PaymentGroupEntity paymentGroup : paymentGroups) {
            if (!paymentGroup.getLedgerEntries().isEmpty()) {
                orgIdList.add(paymentGroup.getLedgerEntries().get(0).getOrganisationId());
            }
        }

        // Return a list of organisation filtered by user's accessibility
        //based on id list
        List<OrganisationEntity> orgList = organisationService.find(orgIdList);

        return orgList.stream().collect(Collectors.toMap(OrganisationEntity::getId, Function.identity()));
    }


    void setAuthorisor(ProjectLedgerEntry payment) {
        if (payment.getAuthorisedBy() != null) {
            payment.setAuthorisor(userService.find(payment.getAuthorisedBy()).getFullName());
        }
    }

    void setLastModifier(ProjectLedgerEntry payment) {
        if (payment.getModifiedBy() != null) {
            payment.setModifiedBy(userService.find(payment.getModifiedBy()).getUsername());
            payment.setLastModifierName(userService.getUserFullName(payment.getModifiedBy()));
        }
    }

    void setCreator(ProjectLedgerEntry payment) {
        if (payment.getCreatedBy() != null) {
            payment.setCreator(userService.find(payment.getCreatedBy()).getFullName());
        }
    }

    public ProjectLedgerEntry createReclaim(final Set<ProjectLedgerEntry> previousPayments, final BigDecimal amount,
                                            final Integer claimId, final Integer blockId, final SpendType spendType) {
        ProjectLedgerEntry payment = previousPayments.stream().findFirst().orElse(null);

        validateBudgetBlockReclaim(amount, payment, previousPayments);
        ProjectLedgerEntry reclaim = createReclaimPayment(amount, payment, claimId, blockId, spendType);
        ProjectLedgerEntry saved = financeService.save(reclaim);
        paymentAuditService.recordPaymentAuditItem(saved, PaymentAuditItemType.Created);
        return saved;
    }

    public ProjectLedgerEntry createReclaim(final Integer paymentId, final BigDecimal amount) {

        ProjectLedgerEntry payment = projectLedgerRepository.findById(paymentId).orElse(null);

        validateManualReclaim(amount, payment);
        ProjectLedgerEntry reclaim = createReclaimPayment(amount, payment, null, null, null);
        ProjectLedgerEntry saved = financeService.save(reclaim);
        PaymentGroupEntity pg = new PaymentGroupEntity();
        pg.getLedgerEntries().add(saved);
        pg.setApprovalRequestedBy(userService.currentUser().getUsername());
        paymentGroupRepository.save(pg);
        paymentAuditService.recordPaymentAuditItem(saved, PaymentAuditItemType.Created);
        return saved;
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
     * @return reclaim entry
     */
    public ProjectLedgerEntry createReclaim(Project project,
        ProjectLedgerEntry originalPayment,
        LedgerType type,
        String paymentSource,
        LedgerStatus status,
        String category,
        String subCategory,
        BigDecimal value,
        Integer year,
        Integer month,
        Integer externalId,
        LedgerSource source) {
        return createReclaim(project, originalPayment, project.getMilestonesBlock().getId(), type, paymentSource,
            status, category, subCategory, value, year, month, externalId, source);
    }

    public ProjectLedgerEntry createReclaim(Project project,
        ProjectLedgerEntry originalPayment,
        Integer blockId,
        LedgerType type,
        String paymentSource,
        LedgerStatus status,
        String category,
        String subCategory,
        BigDecimal value,
        Integer year,
        Integer month,
        Integer externalId,
        LedgerSource source) {
        final UserEntity user = userService.currentUser();
        OrganisationEntity orgForRepayment = getOrganisationForRepayment(originalPayment);
        ProjectLedgerEntry newReclaim = initialisePayment(project, blockId, type, paymentSource, status, category, subCategory,
            value, year, month, externalId, source, user, orgForRepayment);
        newReclaim.setReclaimOfPaymentId(originalPayment.getId());

        return newReclaim;
    }

    private ProjectLedgerEntry createReclaimPayment(BigDecimal amount, ProjectLedgerEntry payment, Integer reclaimId,
                                                    Integer blockId, SpendType spendType) {
        OffsetDateTime now = environment.now();
        ProjectLedgerEntry reclaim = new ProjectLedgerEntry(
                payment.getProjectId(), now.getYear(), now.getMonthValue(),
                payment.getCategory(), "Reclaimed " + payment.getSubCategory(), amount, Pending);
        reclaim.setBlockId(blockId != null ? blockId : payment.getBlockId());
        reclaim.setLedgerSource(LedgerSource.WebUI);
        reclaim.setManagingOrganisation(payment.getManagingOrganisation());
        reclaim.setProgrammeName(payment.getProgrammeName());
        reclaim.setCostCentreCode(payment.getCostCentreCode());
        reclaim.setDescription("Reclaim");
        reclaim.setLedgerType(payment.getLedgerType());
        reclaim.setPaymentSource(payment.getPaymentSource());
        reclaim.setCreatedOn(now);
        reclaim.setCreatedBy(userService.currentUser().getUsername());
        reclaim.setExternalId(payment.getExternalId());
        reclaim.setPcsPhaseNumber(payment.getPcsPhaseNumber());
        reclaim.setPcsProjectNumber(payment.getPcsProjectNumber());
        reclaim.setTransactionDate(new SimpleDateFormat("dd/MM/yyyy").format(Date.from(now.toInstant())));
        reclaim.setReclaimOfPaymentId(payment.getId());
        OrganisationEntity orgForRepayment = getOrganisationForRepayment(payment);
        reclaim.setOrganisationId(orgForRepayment.getId());
        reclaim.setVendorName(orgForRepayment.getName());
        reclaim.setSapVendorId(getProjectSapVendorId(payment.getProjectId(), orgForRepayment));
        reclaim.setProjectName(payment.getProjectName());
        reclaim.setWbsCode(payment.getWbsCode());
        reclaim.setCompanyName(payment.getCompanyName());
        reclaim.setSpendType(spendType);
        reclaim.updateValue(amount);
        reclaim.setClaimId(reclaimId != null ? reclaimId : payment.getClaimId());
        return reclaim;
    }

    private OrganisationEntity getOrganisationForRepayment(ProjectLedgerEntry payment) {
        Project project = projectService.get(payment.getProjectId());
        OrganisationEntity repaymentOrg;
        if (project != null) {
            OrganisationGroup organisationGroup = project.getOrganisationGroup();

            if (organisationGroup != null && organisationGroup.getLeadOrganisationId() != null) {
                repaymentOrg = organisationGroup.getLeadOrganisation();
            } else {
                repaymentOrg = project.getOrganisation();
            }
        } else {
            // shouldn't be possible?
            repaymentOrg = organisationService.find(payment.getOrganisationId());
        }
        return repaymentOrg;

    }

    private void validateBudgetBlockReclaim(BigDecimal amount, ProjectLedgerEntry payment,
                                            Set<ProjectLedgerEntry> previousPayments) {
        if (payment == null) {
            throw new ValidationException("Unable to find matching original payment");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Reclaim amounts must be positive");
        }

        if (!LedgerStatus.getApprovedPaymentStatuses().contains(payment.getLedgerStatus())) {
            throw new ValidationException("Unable to perform reclaim on an unauthorised payment");
        }

        BigDecimal totalReclaims = payment.getValue(); // should be negative
        List<ProjectLedgerEntry> existingReclaims = projectLedgerRepository.findAllByReclaimOfPaymentId(payment.getId());

        for (ProjectLedgerEntry existingReclaim : existingReclaims) {
            if (!Declined.equals(existingReclaim.getLedgerStatus()) && !existingReclaim.isInterestPayment()) {
                totalReclaims = totalReclaims.add(existingReclaim.getValue());
            }
        }
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
        if (!ProjectStatus.Closed.equals(project.getStatusType())) {
            throw new ValidationException("Unable to create manual reclaims for projects that are not closed");
        }

        BigDecimal totalReclaims = payment.getValue(); // should be negative
        List<ProjectLedgerEntry> existingReclaims = projectLedgerRepository.findAllByReclaimOfPaymentId(payment.getId());

        for (ProjectLedgerEntry existingReclaim : existingReclaims) {
            if (!Declined.equals(existingReclaim.getLedgerStatus()) && !existingReclaim.isInterestPayment()) {
                totalReclaims = totalReclaims.add(existingReclaim.getValue());
            }
        }
        if (amount.add(totalReclaims).compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException("Unable to create reclaim as total reclaims would exceed total initial payment");
        }
    }

    public PaymentGroupEntity save(PaymentGroupEntity paymentGroup) {
        return paymentGroupRepository.save(paymentGroup);

    }

    public boolean anyExistingScheduledPayments(Integer projectId, Integer year, Integer month) {
        return projectLedgerRepository.countAllByProjectIdAndCategoryAndYearMonth(projectId, SCHEDULED_PAYMENT_CATEGORY,
            (year * 100) + month) > 0;
    }

    public void createAndAuthorisePaymentGroup(PaymentGroupEntity paymentGroup) {
        for (ProjectLedgerEntry ledgerEntry : paymentGroup.getLedgerEntries()) {
            projectLedgerRepository.saveAndFlush(ledgerEntry);
        }
        PaymentGroupEntity updated = paymentGroupRepository.saveAndFlush(paymentGroup);

        this.authoriseGroup(updated, false);
    }

    //TODO This is milestone specific. We have LearningGrant payments as well now
    public ProjectLedgerEntry create(final ProjectLedgerEntry projectLedgerEntry) {
        final Project project = validateProject(projectLedgerEntry);
        final ProjectLedgerEntry toSave = createPayment(
                project,
                project.getMilestonesBlock().getId(),
                projectLedgerEntry.getLedgerType(),
                projectLedgerEntry.getPaymentSource(),
                projectLedgerEntry.getLedgerStatus(),
                projectLedgerEntry.getCategory(),
                projectLedgerEntry.getSubCategory(),
                projectLedgerEntry.getValue(),
                projectLedgerEntry.getYear(),
                projectLedgerEntry.getMonth(),
                projectLedgerEntry.getExternalId(),
                projectLedgerEntry.getLedgerSource());
        toSave.setAuthorisedBy(projectLedgerEntry.getAuthorisedBy());
        toSave.setAuthorisedOn(projectLedgerEntry.getAuthorisedOn());
        ProjectLedgerEntry saved = financeService.save(toSave);
        paymentAuditService.recordPaymentAuditItem(saved, PaymentAuditItemType.Created);
        return saved;
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
     * @param ledgerSource Optional. Default value webUI
     * @return new payment
     */
    public ProjectLedgerEntry createPayment(Project project,
                                            Integer blockId,
                                            LedgerType type,
                                            String paymentSource,
                                            LedgerStatus status,
                                            String category,
                                            String subCategory,
                                            BigDecimal value,
                                            Integer year,
                                            Integer month,
                                            Integer externalId,
                                            LedgerSource ledgerSource) {
        return createPayment(project, blockId, type, paymentSource, status, null, category, subCategory, value,
            year, month, null, externalId, ledgerSource);
    }

    public ProjectLedgerEntry createPayment(Project project,
                                            Integer blockId,
                                            LedgerType type,
                                            String paymentSource,
                                            LedgerStatus status,
                                            SpendType spendType,
                                            String category,
                                            String subCategory,
                                            BigDecimal value,
                                            Integer year,
                                            Integer month,
                                            Integer quarter,
                                            Integer externalId,
                                            LedgerSource ledgerSource) {
        final UserEntity user = userService.currentUser();
        final OrganisationEntity organisation = getValidatedOrganisationForPayment(project);
        return initialisePayment(project, blockId, type, paymentSource, status, spendType, category, subCategory, value,
            year, month, quarter, externalId, ledgerSource, user, organisation);
    }

    public PaymentGroupEntity createPaymentGroup(String approvalRequestedBy, List<ProjectLedgerEntry> ledgerEntries) {
        PaymentGroupEntity paymentGroup = new PaymentGroupEntity();
        paymentGroup.setApprovalRequestedBy(approvalRequestedBy);
        paymentGroup.getLedgerEntries().addAll(ledgerEntries);
        projectLedgerRepository.saveAll(ledgerEntries);
        return paymentGroupRepository.save(paymentGroup);
    }

    private ProjectLedgerEntry initialisePayment(Project project,
                                                 Integer blockId,
                                                 LedgerType type,
                                                 String paymentSource,
                                                 LedgerStatus status,
                                                 String category,
                                                 String subCategory,
                                                 BigDecimal value,
                                                 Integer year,
                                                 Integer month,
                                                 Integer externalId,
                                                 LedgerSource source,
                                                 UserEntity user,
                                                 OrganisationEntity organisation) {
        return initialisePayment(project, blockId, type, paymentSource, status, null, category, subCategory, value,
            year, month, null, externalId, source, user, organisation);
    }

    private ProjectLedgerEntry initialisePayment(Project project,
                                                 Integer blockId,
                                                 LedgerType type,
                                                 String paymentSource,
                                                 LedgerStatus status,
                                                 SpendType spendType,
                                                 String category,
                                                 String subCategory,
                                                 BigDecimal value,
                                                 Integer year,
                                                 Integer month,
                                                 Integer quarter,
                                                 Integer externalId,
                                                 LedgerSource ledgerSource,
                                                 UserEntity user,
                                                 OrganisationEntity organisation) {
        final ProjectLedgerEntry payment = new ProjectLedgerEntry();
        payment.setManagingOrganisation(project.getManagingOrganisation());
        payment.setOrganisationId(organisation.getId());
        payment.setProjectId(project.getId());
        payment.setBlockId(blockId);
        payment.setYear(year != null ? year : environment.now().getYear());
        payment.setMonth(month != null ? month : environment.now().getMonthValue());
        payment.setQuarter(quarter);
        payment.setLedgerStatus(status);
        payment.setLedgerType(type == null ?  PAYMENT : type);
        payment.setPaymentSource(paymentSource);
        payment.setSpendType(spendType);
        payment.setCategory(category);
        payment.setSubCategory(subCategory);
        payment.updateValue(value);
        payment.setLedgerSource(ledgerSource != null ? ledgerSource : LedgerSource.WebUI);
        payment.setVendorName(organisation.getName());
        payment.setSapVendorId(getProjectSapVendorId(project.getId(), organisation));
        payment.setExternalId(externalId);
        payment.setCreatedBy(user != null ? user.getUsername() : null);
        payment.setCreatedOn(environment.now());
        payment.setProgrammeName(project.getProgramme().getName());
        payment.setProjectName(project.getTitle());
        payment.setCompanyName(project.getProgramme().getCompanyName());
        payment.setSupplierProductCode(project.getSupplierProductCode());
        if (spendType != null) {
            payment.setWbsCode(project.getProgramme().getWbsCodeForTemplate(project.getTemplateId(), spendType));
        } else {
            payment.setWbsCode(project.getProgramme().getWbsCodeForTemplate(project.getTemplateId()));
        }
        payment.setCeCode(project.getProgramme().getCeCodeForTemplate(project.getTemplateId()));
        return payment;
    }

    @Transactional(readOnly = true)
    public PaymentSummary getById(Integer id) {
        PaymentSummary paymentSummary = paymentSummaryRepository.findById(id).orElse(null);
        if (paymentSummary == null) {
            throw new NotFoundException("No payment found with ID " + id);
        }
        return  paymentSummary;
    }

    @Transactional(readOnly = true)
    public ProjectLedgerEntry getLedgerEntryById(Integer id) {
        ProjectLedgerEntry entry = projectLedgerRepository.findById(id).orElse(null);
        if (entry == null) {
            throw new NotFoundException("No payment found with ID " + id);
        }
        return  entry;
    }

    public ProjectLedgerEntry getBySupplierInvoiceNumber(String supplierInvoiceNumber) {
        try {
            Integer paymentId = Integer.parseInt(supplierInvoiceNumber.split("-")[1]);
            return financeService.findOne(paymentId);
        } catch (Exception e) {
            return null;
        }
    }

    private OrganisationEntity getValidatedOrganisationForPayment(final Project project) {
        final OrganisationEntity organisation = organisationService.getOrganisationForProject(project);

        if (organisation == null) {
            throw new ValidationException("payment must have a valid organisation ID!");
        }
        return organisation;
    }

    private Project validateProject(ProjectLedgerEntry projectLedgerEntry) {
        Project project = projectService.get(projectLedgerEntry.getProjectId());

        if (project == null) {
            throw new ValidationException("payment must have a valid project ID!");
        }

        return project;
    }


    /**
     * Authorise all the payments for the given group
     * @param groupId id of the group of payments
     * @return list of authorised payments
     */
    @Transactional
    public List<ProjectLedgerEntry> authoriseByGroupId(final int groupId) throws ForbiddenAccessException {
        PaymentGroupEntity group = getPaymentGroupOnlyPending(groupId);
        List<PaymentGroupEntity> groups = new ArrayList<>();
        groups.add(group);
        restrictAndEnrichPaymentGroups(groups);
        return authoriseByGroup(group);
    }

    @Transactional
    public List<ProjectLedgerEntry> authoriseByGroup(PaymentGroupEntity paymentGroup) throws ForbiddenAccessException {
        checkAuthorisePermission(paymentGroup);

        return authoriseGroup(paymentGroup, true);

    }

    private List<ProjectLedgerEntry> authoriseGroup(PaymentGroupEntity paymentGroup, boolean updatePaymentToAuthorised) {
        paymentGroup = authorisePaymentGroup(paymentGroup, updatePaymentToAuthorised);
        final List<ProjectLedgerEntry> authorisedPayments = paymentGroup.getLedgerEntries();

        final Set<Integer> authorisedPaymentIdsSet = authorisedPayments.stream()
                .map(ProjectLedgerEntry::getId)
                .collect(Collectors.toSet());
        final Set<Integer> expectedAuthorisedPaymentIdsSet = paymentGroup.getLedgerEntries().stream()
                .map(ProjectLedgerEntry::getId)
                .collect(Collectors.toSet());
        if (!expectedAuthorisedPaymentIdsSet.equals(authorisedPaymentIdsSet)) {
            throw new RuntimeException("No all payments can be authorised. Rollback");
        }
        final Set<Integer> projectIdSet = authorisedPayments.stream()
                .map(ProjectLedgerEntry::getProjectId)
                .collect(Collectors.toSet());
        projectService.refreshProjectStatus(projectIdSet, EventType.PaymentAuthorised);

        ProjectLedgerEntry projectLedgerEntry = paymentGroup.getLedgerEntries().get(0);
        OrganisationEntity organisation = organisationService.findOne(projectLedgerEntry.getOrganisationId());

        Map<String, Object> model = new HashMap<String, Object>() {{
            put("projectId", projectLedgerEntry.getProjectId());
            put("organisation", organisation);
        }};

        notificationService.createNotification(PaymentAuthorisation, paymentGroup, model);
        return authorisedPayments;
    }

    void checkAuthorisePermission(PaymentGroupEntity group) {

        if (group.isSuspendPayments()) {
            throw new ValidationException("You can't authorise this payment as project payments have been suspended");
        }

        if (!permissionService.currentUserHasPermissionForOrganisation(AUTHORISE_PAYMENT,
            group.getLedgerEntries().get(0).getManagingOrganisationId())) {
            throw new ForbiddenAccessException("User does not have permission to authorise payments for organisation "
                + group.getId());
        }

        if (group.getPayments().size() >= 1) {
            String modifiedBy = (group.getPayments().get(0).getModifiedBy() != null)
                ? group.getPayments().get(0).getModifiedBy() : group.getPayments().get(0).getCreatedBy();
            if (modifiedBy.equalsIgnoreCase(userService.currentUser().getUsername())) {
                throw new ValidationException(
                        "You can't authorise this payment because you are the user who requested the payment: " + group.getId());
            }
        }

        if (group.isThresholdExceeded()) {
            throw new ValidationException("You can't authorise this payment as your authorised spend limit is below this amount");
        }
    }

    private PaymentGroupEntity getPaymentGroupOnlyPending(int groupId) {
        final PaymentGroupEntity paymentGroup = paymentGroupRepository.findById(groupId).orElse(null);
        if (paymentGroup == null) {
            throw new ValidationException("Unable to retrieve payment group with id: " + groupId);
        }

        if (paymentGroup.getLedgerEntries() != null) {
            final boolean noPending = paymentGroup.getLedgerEntries().stream()
                    .anyMatch(p -> !Pending.equals(p.getLedgerStatus()));
            if (noPending) {
                throw new ValidationException("This payment group has non pending payments");
            }

        }
        return paymentGroup;
    }


    PaymentGroupEntity authorisePaymentGroup(PaymentGroupEntity paymentGroup) {
        return authorisePaymentGroup(paymentGroup, true);
    }

    PaymentGroupEntity authorisePaymentGroup(PaymentGroupEntity paymentGroup, boolean updatePaymentToAuthorised) {
        List<ProjectLedgerEntry> list = paymentGroup.getLedgerEntries();
        if (CollectionUtils.isEmpty(list)) {
            throw new ValidationException("There are no payments");
        }

        final Project project = projectService.get(list.get(0).getProjectId());

        if (paymentGroup.hasReclaim()) {
            if (!paymentGroup.getInterestAssessed()) {
                throw new ValidationException("Interest must be considered for payment reclaims");
            }
            validateReclaim(project, list);
        } else {
            validatePayment(project);
        }

        if (updatePaymentToAuthorised) {
            final UserEntity user = userService.currentUser();
            list.forEach(e -> setStatus(e, Authorised, user));
            financeService.save(list);
        }
        return paymentGroupRepository.getOne(paymentGroup.getId());
    }

    private void validateReclaim(Project project, List<ProjectLedgerEntry> list) {
        String wbsRevenueForInterestPayments = project.getProgramme().getRevenueWbsCodeForTemplate(project.getTemplateId());
        for (ProjectLedgerEntry thisReclaim : list) {
            checkCompanyNameIsPopulated(thisReclaim);
            if (thisReclaim.getLedgerType().equals(PAYMENT) && thisReclaim.isReclaim())  {
                checkSAPVendorId(project);
            }
            if (thisReclaim.isInterestPayment()) {
                if (wbsRevenueForInterestPayments == null || wbsRevenueForInterestPayments.isEmpty()) {
                    ArrayList<FieldError> fieldErrors = new ArrayList<>();
                    fieldErrors.add(new FieldError("programme", String.valueOf(project.getProgrammeId()), "programme id"));
                    fieldErrors.add(new FieldError("code", "REVENUE_WBS_CODE_MISSING", "error code"));
                    throw new ValidationException(
                            "Unable to authorise payments for this group as the revenue WBS code is not set.", fieldErrors);
                }
                thisReclaim.setWbsCode(wbsRevenueForInterestPayments);
            }
            /*
            As part of GLA-37698 we have decided with the business team to remove this validation as its logic is not correct
            anyways when there are more than 1 payments for a milestones (check initial payment and supplementary for P10731)

            if (thisReclaim.getReclaimOfPaymentId() != null) {
                List<ProjectLedgerEntry> allApprovedReclaims = projectLedgerRepository
                        .findAllByReclaimOfPaymentId(thisReclaim.getReclaimOfPaymentId());
                ProjectLedgerEntry originalPayment = projectLedgerRepository.getOne(thisReclaim.getReclaimOfPaymentId());
                Set<LedgerStatus> approvedPaymentStatuses = LedgerStatus.getApprovedPaymentStatuses();
                BigDecimal totalExistingApprovedReclaims = allApprovedReclaims.stream()
                        .filter(p -> approvedPaymentStatuses.contains(p.getLedgerStatus()))
                    .map(ProjectLedgerEntry::getValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                // original payment is negative, add all existing reclaims and this reclaim to ensure is not greater than 0
                if (originalPayment.getValue().add(totalExistingApprovedReclaims)
                    .add(thisReclaim.getValue()).compareTo(BigDecimal.ZERO) > 0) {
                    throw new ValidationException("Unable to approve this reclaim as it would exceed the original payment amount");
                }
            }
             */
        }
    }

    private void validatePayment(Project project) {
        final Map<GrantType, BigDecimal> amounts = new HashMap<GrantType, BigDecimal>() { {
            put(GrantType.Grant, new BigDecimal(0));
            put(GrantType.DPF, new BigDecimal(0));
            put(GrantType.RCGF, new BigDecimal(0));
        }};
        boolean fundingBalanceRequired = false;
        boolean sapVendorKeyRequired = false;
        for (final ProjectLedgerEntry e: projectLedgerRepository
                .findAllByProjectIdAndLedgerStatusIn(project.getId(), LedgerStatus.getPaymentStatuses())) {
            checkCompanyNameIsPopulated(e);

            if (e.isReconcilliationPayment() && project.ignoreReconcilliationAmountValidation()) {
                continue;
            }

            if (!LedgerStatus.Declined.equals(e.getLedgerStatus())) {
                fundingBalanceRequired = fundingBalanceRequired || project.getProjectBlockById(e.getBlockId())
                    instanceof FundingSourceProvider;
                switch (e.getLedgerType()) {
                    case PAYMENT:
                        amounts.put(GrantType.Grant, amounts.get(GrantType.Grant).add(e.getValue()));
                        sapVendorKeyRequired = sapVendorKeyRequired
                                || refDataService.getPaymentSourceMap().get(e.getPaymentSource()).shouldPaymentSourceBeSentToSAP();
                        break;
                    case DPF:
                        amounts.put(GrantType.DPF, amounts.get(GrantType.DPF).add(e.getValue()));
                        break;
                    case RCGF:
                        amounts.put(GrantType.RCGF, amounts.get(GrantType.RCGF).add(e.getValue()));
                        break;
                    default:
                        throw new RuntimeException("Unrecognised ledger type: " + e.getLedgerType());
                }
            }
        }

        // for Grant payments as we send it to SAP we want to validate the organisation has a SAP ID set
        if (sapVendorKeyRequired && amounts.get(GrantType.Grant).compareTo(BigDecimal.ZERO) > 0) {
            checkSAPVendorId(project);
        }

        if (fundingBalanceRequired) {
            Map<GrantType, BigDecimal> maxAmounts = project.getGrantsRequested();
            for (final GrantType type : GrantType.values()) {
                if (amounts.get(type).negate().compareTo(maxAmounts.get(type)) > 0) {
                    throw new
                        ValidationException("Payment cannot be authorised as the payment amount is greater than the remaining "
                        + toMonetaryString(maxAmounts.get(type)) + " balance");
                }
            }
        }
    }

    private void checkSAPVendorId(Project project) {
        final OrganisationEntity organisation = organisationService.getOrganisationForProject(project);
        if (GlaUtils.isNullOrEmpty(organisation.getDefaultSapVendorId())) {
            throw new ValidationException("There is no SAP vendor id");
        }
    }

    private void checkCompanyNameIsPopulated(ProjectLedgerEntry payment) {
        if (GlaUtils.isNullOrEmpty(payment.getCompanyName())) {
            log.info("Company name not specified for payment: " + payment.getId() + " Project : " + payment.getProjectId());
            throw new ValidationException("There is no Company Name against the programme for this payment");
        }
    }


    public void updateStatus(Integer paymentId, LedgerStatus status) {
        ProjectLedgerEntry payment = financeService.findOne(paymentId);

        if (payment == null) {
            throw new NotFoundException("No payment found with ID " + paymentId);
        }

        if (!states_that_can_be_changed_from.contains(payment.getLedgerStatus())) {
            throw new ValidationException("cannot change payment in status " + payment.getLedgerStatus());
        }

        if (!states_that_can_be_changed_to.contains(status)) {
            throw new ValidationException("cannot change payment status to " + status);
        }

        auditService.auditCurrentUserActivity(String.format("updated payment %d status from %s to %s",
            payment.getId(), payment.getLedgerStatus(), status));
        setStatus(payment, status, null);
    }

    /**
     * Set the status of a payment request, including authorisations.
     */
    public ProjectLedgerEntry setStatus(ProjectLedgerEntry projectLedgerEntry, LedgerStatus newStatus, UserEntity authorisingUser) {
        return setStatus(projectLedgerEntry, newStatus, authorisingUser, null);
    }

    public ProjectLedgerEntry setStatus(ProjectLedgerEntry projectLedgerEntry, LedgerStatus newStatus, UserEntity authorisingUser,
                                        String xmlContent) {
        PaymentAuditItemType auditType = null;
        if (newStatus.equals(Authorised)) {

            if (!projectLedgerEntry.getLedgerStatus().equals(LedgerStatus.Pending)) {
                throw new ValidationException("Only pending payment requests can be authorised");
            }

            if (authorisingUser == null) {
                throw new ValidationException("Authorising user must be specified");
            }
            projectLedgerEntry.setAuthorisedOn(environment.now());
            projectLedgerEntry.setAuthorisedBy(authorisingUser.getUsername());
            auditType = PaymentAuditItemType.Authorised;
        } else if (newStatus.equals(LedgerStatus.Acknowledged)) {

            if (projectLedgerEntry.getLedgerStatus().equals(LedgerStatus.Pending)) {
                throw new ValidationException("Pending payment requests cannot be acknowledged");
            }

            projectLedgerEntry.setAcknowledgedOn(environment.now());
            projectLedgerEntry.setLedgerStatus(newStatus);
            sendPaymentAcknowledgement(projectLedgerEntry);
            auditType = PaymentAuditItemType.Acknowledged;

        } else if (newStatus.equals(LedgerStatus.Cleared)) {

            if (projectLedgerEntry.getLedgerStatus().equals(LedgerStatus.Pending)) {
                throw new ValidationException("Pending payment requests cannot be cleared");
            }

            projectLedgerEntry.setClearedOn(environment.now());
            auditType = PaymentAuditItemType.Cleared;

        } else if (newStatus.equals(LedgerStatus.UnderReview)) {
            auditType = PaymentAuditItemType.UnderReview;
        } else if (newStatus.equals(LedgerStatus.SupplierError)) {
            auditType = PaymentAuditItemType.SupplierError;
        } else {
            throw new ValidationException("Status transition not currently supported: " + newStatus);
        }

        projectLedgerEntry.setLedgerStatus(newStatus);
        ProjectLedgerEntry saved = financeService.save(projectLedgerEntry);
        paymentAuditService.recordPaymentAuditItem(projectLedgerEntry, auditType, xmlContent);
        return saved;
    }

    public void sendPaymentAcknowledgement(ProjectLedgerEntry projectLedgerEntry) {
        if (projectLedgerEntry.getLedgerStatus().equals(LedgerStatus.Acknowledged)) {
            OrganisationEntity org = organisationService.findOne(projectLedgerEntry.getOrganisationId());

            if (org != null) {
                if (org.getFinanceContactEmail() != null) {
                    Map<String, Object> model = new HashMap<>();
                    model.put("organisationName", org.getName());
                    model.put("projectId", projectLedgerEntry.getProjectId());
                    model.put("projectName", projectLedgerEntry.getProjectName());
                    model.put("programmeName", projectLedgerEntry.getProgrammeName());
                    model.put("paymentCategory", projectLedgerEntry.getCategory());
                    model.put("paymentSubCategory", projectLedgerEntry.getSubCategory());
                    model.put("paymentAmount", projectLedgerEntry.getTotalIncludingInterest() == null
                        ? null : projectLedgerEntry.getTotalIncludingInterest().negate());
                    model.put("managingOrganisationName", projectLedgerEntry.getManagingOrganisation() == null
                            ? "" : projectLedgerEntry.getManagingOrganisation().getName());
                    model.put("invoiceNumber", projectLedgerEntry.getOpsInvoiceNumber());
                    for (String email: org.getFinanceContactEmail().split(",")) {
                        model.put("recipient", email.trim());
                        emailService.sendPaymentAcknowledgementEmail(model);
                    }
                } else {
                    log.info("sendPaymentAcknowledgement: no finance contact email defined for organisationId:" + org.getId()
                            + " name: " + org.getName());
                }
            }
        }
    }

    public List<ProjectLedgerEntry> getPendingLedgerEntries() {
        return  financeService.findByStatus(Pending);
    }

    public boolean hasPendingPayments(Integer projectId) {
        return projectLedgerRepository.countByProjectIdAndLedgerStatus(projectId, Pending) > 0;
    }

    public boolean hasReclaims(Integer projectId) {
        return projectLedgerRepository.countByProjectIdAndReclaimOfPaymentIdNotNull(projectId) > 0;
    }

    public boolean hasPayments(Integer projectId) {
        return projectLedgerRepository.countByProjectIdAndLedgerStatusIn(projectId, ALL_PAYMENTS.getRelevantStatusesAsList()) > 0;
    }

    public PaymentGroupEntity declinePaymentsByGroupId(int groupToUpdateId, PaymentGroupEntity group) {

        PaymentGroupEntity groupToUpdate = getPaymentGroupOnlyPending(groupToUpdateId);
        return declinePaymentsByGroup(groupToUpdate, group);

    }


    public PaymentGroupEntity declinePaymentsByGroup(PaymentGroupEntity groupToUpdate, final PaymentGroupEntity group)
            throws ForbiddenAccessException {
        if (!permissionService.currentUserHasPermissionForOrganisation(AUTHORISE_PAYMENT,
                groupToUpdate.getLedgerEntries().get(0).getManagingOrganisationId())) {
            throw new ForbiddenAccessException("User does not have permission to decline payments for organisation "
                    + groupToUpdate.getId());
        }

        if (group.getDeclineReason() == null || group.getDeclineReason().getCategory() == null
            || group.getDeclineReason().getId() == null) {
            throw new ValidationException("Decline reason is mandatory when declining payments");
        }

        final CategoryValue categoryValue = refDataService.getCategoryValue(group.getDeclineReason().getId());
        if (categoryValue == null
            || !Arrays.asList(PaymentDeclineReason, ReclaimDeclineReason).contains(categoryValue.getCategory())) {
            throw new ValidationException("Decline reason is not valid");
        }

        groupToUpdate.getLedgerEntries().forEach(ple -> ple.setLedgerStatus(Declined));
        projectLedgerRepository.saveAll(groupToUpdate.getLedgerEntries());
        groupToUpdate.setDeclineReason(categoryValue);
        groupToUpdate.setDeclineComments(group.getDeclineComments());
        paymentGroupRepository.save(groupToUpdate);
        final Set<Integer> projectIdSet = groupToUpdate.getLedgerEntries().stream()
                .map(ProjectLedgerEntry::getProjectId)
                .collect(Collectors.toSet());
        projectService.refreshProjectStatus(projectIdSet, EventType.PaymentDeclined);
        auditService.auditCurrentUserActivity(String.format("Declined payment for payment group: %d, with reason: %s ",
                        groupToUpdate.getId(), groupToUpdate.getDeclineComments()));
        groupToUpdate.getLedgerEntries().forEach(p -> paymentAuditService.recordPaymentAuditItem(p, PaymentAuditItemType.Declined));
        return groupToUpdate;
    }

    // TODO / TechDebt to be fixed as part of GLA-23993
    public PaymentGroupEntity generatePaymentsForClaimedMilestones(final Project project, String approvalRequestedBy) {
        PaymentGroupEntity paymentGroup = new PaymentGroupEntity();
        paymentGroup.setApprovalRequestedBy(approvalRequestedBy);

        boolean approvalWillCreatePendingReclaim = project.getApprovalWillCreatePendingReclaim();
        boolean monetaryValueReclaimRequired = project.getMonetaryValueReclaimRequired();

        BigDecimal grantSourceAdjustmentAmount = project.getGrantSourceAdjustmentAmount();
        List<ProjectLedgerEntry> adjustments = new ArrayList<>();
        List<Milestone> milestones = project.getMilestonesBlock().getMilestones().stream()
                .filter(m -> !ClaimStatus.Pending.equals(m.getClaimStatus()))
                .collect(Collectors.toList());

        for (Milestone milestone : milestones) {
            if (ClaimStatus.Claimed.equals(milestone.getClaimStatus())) {
                Long milestoneGrantClaimed = project.getMilestonesBlock().getMilestoneGrantClaimed(milestone.getId());
                milestone.setClaimedGrant(milestoneGrantClaimed);
                List<ProjectLedgerEntry> entries = projectLedgerEntryMapper.map(project, project.getMilestonesBlock(),
                        milestone.getId());
                paymentGroup.getLedgerEntries().addAll(entries);
            }

            List<ProjectLedgerEntry> approvedPayments = projectLedgerRepository.findByProjectIdAndSubCategoryAndLedgerStatusIn(
                    project.getId(), milestone.getPaymentSubType(), LedgerStatus.getApprovedPaymentStatuses());

            boolean milestoneWithdrawn = ClaimStatus.Withdrawn.equals(milestone.getClaimStatus());


            //rdgf/dpf reclaims and monetary value reclaims or withdrawn milestones
            if (!approvedPayments.isEmpty()
                    && ((approvalWillCreatePendingReclaim || monetaryValueReclaimRequired)
                            || milestoneWithdrawn)) {
                if (milestone.getReclaimedDpf() != null && milestone.getReclaimedDpf() != 0) {
                    createReclaimForType(project, adjustments, milestone, approvedPayments, DPF);
                }

                if (milestone.getReclaimedRcgf() != null && milestone.getReclaimedRcgf() != 0) {
                    createReclaimForType(project, adjustments, milestone, approvedPayments, RCGF);
                }

                if (milestoneWithdrawn || (milestone.getReclaimedGrant() != null
                        && milestone.getMonetaryValue() != null && milestone.getReclaimedGrant() != 0)) {
                    createReclaimForType(project, adjustments, milestone, approvedPayments, PAYMENT);
                }
            }

            // grant reclaims for monetary split
            if (isPaymentAdjustmentRequired(grantSourceAdjustmentAmount, milestone)) {
                Long currentValue = milestone.getClaimedGrant() != null ? milestone.getClaimedGrant() : 0;
                Long newValue = project.getMilestonesBlock().getValueForMonetarySplitForMilestone(milestone);

                long adjustmentValue = currentValue - newValue;
                ProjectLedgerEntry projectLedgerEntry = null;

                if (adjustmentValue < 0) {
                    projectLedgerEntry = projectLedgerEntryMapper
                            .generateSupplementalPaymentForMilestone(project, milestone, adjustmentValue);
                } else {
                    ProjectLedgerEntry originalPaymentForReclaim = getOriginalPaymentForReclaim(approvedPayments, PAYMENT);
                    projectLedgerEntry = projectLedgerEntryMapper.generateReclaimPaymentForMilestone(
                            project, milestone, milestone.getReclaimedGrant(), originalPaymentForReclaim, PAYMENT);
                }

                if (projectLedgerEntry != null) {
                    adjustments.add(projectLedgerEntry);
                }
            }

        }

        // supplemental payments are added after all milestone payments
        paymentGroup.getLedgerEntries().addAll(adjustments);
        if (paymentGroup.getLedgerEntries().isEmpty()) {
            throw new ValidationException("Unable to create payments for this transition, please contact the OPS team.");
        }

        if (paymentGroup.getLedgerEntries().size() > 0
                && Template.MilestoneType.MonetarySplit.equals(project.getTemplate().getMilestoneType())
                && project.getMilestonesBlock().areAllMonetaryMilestonesClaimedOrApproved()
                && project.getGrantsRequested().get(GrantType.Grant) != null) {
            adjustPaymentGroupForRoundingErrors(project, paymentGroup);
        }

        for (ProjectLedgerEntry ple  : paymentGroup.getLedgerEntries()) {
            projectLedgerRepository.save(ple);
        }

        return getRefreshedPaymentGroup(paymentGroup);
    }

    @NotNull
    private PaymentGroupEntity getRefreshedPaymentGroup(PaymentGroupEntity paymentGroup) {
        // retrieve detached group so payment summaries are populated
        PaymentGroupEntity updated = paymentGroupRepository.saveAndFlush(paymentGroup);
        entityManager.detach(updated);
        return paymentGroupRepository.getOne(updated.getId());
    }

    /*
     * Examine the provided payment group and instead of using calc'd
     * percentage of grant source, use remainder of grant source.
     */
    protected void adjustPaymentGroupForRoundingErrors(Project project, PaymentGroupEntity group) {
        BigDecimal totalToClaim = BigDecimal.ZERO;
        for (ProjectLedgerEntry ple : group.getLedgerEntries()) {
            if (PAYMENT.equals(ple.getLedgerType())) {
                totalToClaim = totalToClaim.add(ple.getValue());
            }
        }

        List<ProjectLedgerEntry> allByProjectId = projectLedgerRepository.findAllByProjectId(project.getId());
        BigDecimal totalPayments = BigDecimal.ZERO;
        for (ProjectLedgerEntry ple : allByProjectId) {
            if (PAYMENT.equals(ple.getLedgerType()) && !Declined.equals(ple.getLedgerStatus())
                    && ple.getInterestForPaymentId() == null) {
                totalPayments = totalPayments.add(ple.getValue());
            }
        }

        long difference = totalToClaim.add(totalPayments).add(project.getGrantsRequested().get(GrantType.Grant))
                .longValue();
        if (difference != 0) {

            List<ProjectLedgerEntry> supplementaryPayments = group.getLedgerEntries().stream().filter(
                    ple -> PAYMENT.equals(ple.getLedgerType()) && ple.getCategory().equals(SUPPLEMENTARY_PAYMENT))
                    .collect(Collectors.toList());
            List<ProjectLedgerEntry> normalPayments = group.getLedgerEntries().stream().filter(
                    ple -> PAYMENT.equals(ple.getLedgerType()) && !ple.getCategory().equals(SUPPLEMENTARY_PAYMENT))
                    .collect(Collectors.toList());
            boolean isSupplemental = false;
            Optional<ProjectLedgerEntry> first;
            if (!supplementaryPayments.isEmpty()) {
                isSupplemental = true;
                first = supplementaryPayments.stream().max(Comparator.comparing(ProjectLedgerEntry::getValue));
            } else {
                first = normalPayments.stream().max(Comparator.comparing(ProjectLedgerEntry::getValue));
            }

            if (first.isPresent()) {
                ProjectLedgerEntry ledgerEntry = first.get();
                BigDecimal diff = BigDecimal.valueOf(difference).negate();
                ledgerEntry.updateValue(ledgerEntry.getValue().add(diff));
                for (Milestone milestone : project.getMilestonesBlock().getMilestones()) {
                    if (milestone.getSummary().equals(ledgerEntry.getSubCategory())) {
                        if (isSupplemental) {
                            Long claimedGrant = milestone.getClaimedGrant() != null ? milestone.getClaimedGrant() : 0;
                            milestone.setClaimedGrant(claimedGrant + ledgerEntry.getValue().negate().longValue());
                        } else {
                            milestone.setClaimedGrant(ledgerEntry.getValue().negate().longValue());
                        }
                    }
                }
            } else {
                throw new ValidationException("Unable to calculate payment info correctly, unable to find relevant payment");
            }
        }

    }

    private void createReclaimForType(Project project, List<ProjectLedgerEntry> adjustments, Milestone milestone,
                                      List<ProjectLedgerEntry> approvedPayments, LedgerType type) {
        ProjectLedgerEntry originalPaymentForReclaim = getOriginalPaymentForReclaim(approvedPayments, type);

        ProjectLedgerEntry projectLedgerEntry = null;
        switch (type) {
            case DPF:
                projectLedgerEntry = projectLedgerEntryMapper.generateReclaimPaymentForMilestone(project, milestone,
                        milestone.getReclaimedDpf(), originalPaymentForReclaim, type);
                break;
            case RCGF:
                projectLedgerEntry = projectLedgerEntryMapper.generateReclaimPaymentForMilestone(project, milestone,
                        milestone.getReclaimedRcgf(), originalPaymentForReclaim, type);
                break;
            case PAYMENT:
                projectLedgerEntry = projectLedgerEntryMapper.generateReclaimPaymentForMilestone(project, milestone,
                        milestone.getReclaimedGrant(), originalPaymentForReclaim, type);
                break;
            default:
                throw new RuntimeException("Unrecognised project ledger entry: " + type);
        }

        if (projectLedgerEntry != null) {
            adjustments.add(projectLedgerEntry);
        }
    }

    private ProjectLedgerEntry getOriginalPaymentForReclaim(List<ProjectLedgerEntry> approvedPayments, LedgerType ledgerType) {
        Optional<ProjectLedgerEntry> first = approvedPayments.stream()
                .filter(p -> LedgerStatus.getApprovedPaymentStatuses().contains(p.getLedgerStatus()))
                .filter(p -> MILESTONE.equals(p.getCategory()) || IMS_CLAIMED_MILESTONE.equals(p.getCategory()))
                .filter(p -> ledgerType.equals(p.getLedgerType()))
                .findFirst();
        if (!first.isPresent()) {
            throw new ValidationException("Unable to find original payment record for reclaim.");
        }
        return first.get();
    }

    private boolean isPaymentAdjustmentRequired(BigDecimal grantSourceAdjustmentAmount, Milestone milestone) {
        return !grantSourceAdjustmentAmount.equals(BigDecimal.ZERO) && ClaimStatus.Approved.equals(milestone.getClaimStatus())
            && milestone.getMonetary() && milestone.getMonetarySplit() != null && milestone.getMonetarySplit() > 0;
    }

    @Transactional(readOnly = true)
    public PaymentGroupEntity getGroupById(Integer id) {
        final PaymentGroupEntity paymentGroup = paymentGroupRepository.findById(id).orElse(null);
        if (paymentGroup == null) {
            throw new ValidationException("Payment group not found for id " + id);
        }

        List<PaymentGroupEntity> list = new ArrayList<>();
        list.add(paymentGroup);
        List<PaymentGroupEntity> paymentGroups = restrictAndEnrichPaymentGroups(list);

        if (paymentGroups.size() == 0) {
            throw new ValidationException("Payment group not found for id " + id);
        }

        return paymentGroups.get(0);
    }

    public PaymentGroupEntity getGroupByPaymentId(Integer paymentId) {

        PaymentGroupEntity group = paymentGroupRepository.findPaymentGroupByPaymentId(paymentId);
        List<PaymentGroupEntity> response = new ArrayList<>();
        response.add(group);
        response = restrictAndEnrichPaymentGroups(response);

        if (response != null && response.size() == 1 && group != null) {
            group.getPayments().forEach(p -> {
                Map<String, PaymentSource> paymentSourceMap = refDataService.getPaymentSourceMap();
                enrichPaymentSummary(p, paymentSourceMap);
            });
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
        ProjectLedgerEntry reclaim = projectLedgerRepository.getOne(reclaimPaymentId);
        ProjectLedgerEntry interest = new ProjectLedgerEntry();
        if (!reclaim.isReclaim()) {
            throw new ValidationException("Can only add interest to reclaim payment types");
        }

       if (!reclaim.getLedgerStatus().equals(Pending)) {
            throw new ValidationException("Can only add interest to a pending reclaim payment");
        }

        if (value.compareTo(BigDecimal.ZERO) < 0)  {
            throw new ValidationException("Interest amount must be positive");
        }
        // check if already an interest payment
        List<ProjectLedgerEntry> interestPayments = projectLedgerRepository.findAllByInterestForPaymentId(reclaimPaymentId);
        boolean interestPaymentExists = interestPayments != null && !interestPayments.isEmpty();
        if (interestPaymentExists) {
            if (interestPayments.size() == 1) {
                interest = interestPayments.get(0);
            } else {
                throw new ValidationException("More than one interest payment found for payment: " + reclaimPaymentId);
            }
        } else {
            interest.createInterestRecordFrom(reclaim);
        }

        PaymentGroupEntity groupByPaymentId = paymentGroupRepository.findPaymentGroupByPaymentId(reclaim.getId());
        groupByPaymentId.setInterestAssessed(true);
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            if (interestPaymentExists) {
                groupByPaymentId.getLedgerEntries().remove(interest);
                paymentGroupRepository.save(groupByPaymentId);
                projectLedgerRepository.delete(interest);

                auditService.auditCurrentUserActivity(String.format(
                        "deleted interest project ledger entry %d for project %d reclaim payment %d",
                        interest.getId(), interest.getProjectId(), reclaimPaymentId));
                interest = null;
            }
        } else {
            interest.updateValue(value);
            interest = projectLedgerRepository.save(interest);

            if (!interestPaymentExists) {
                groupByPaymentId.getLedgerEntries().add(groupByPaymentId.getLedgerEntries().indexOf(reclaim) + 1,  interest);
                paymentAuditService.recordPaymentAuditItem(interest, PaymentAuditItemType.Created);
            }
        }
        paymentGroupRepository.save(groupByPaymentId);
        return interest;
    }

    public void clonePaymentGroupsForBlock(Integer originalBlockId, Integer newProjectId, Integer newBlockId) {
        Set<PaymentGroupEntity> paymentGroups = paymentGroupRepository.findAllByBlockId(originalBlockId);
        for (PaymentGroupEntity originalPaymentGroup: paymentGroups) {
            List<ProjectLedgerEntry> entries = new ArrayList<>();
            for (ProjectLedgerEntry entry: originalPaymentGroup.getLedgerEntries()) {
                entries.add(entry.clone(newProjectId, newBlockId));
            }
            projectLedgerRepository.saveAll(entries);

            PaymentGroupEntity clonePaymentGroup = new PaymentGroupEntity();
            clonePaymentGroup.setDeclineReason(originalPaymentGroup.getDeclineReason());
            clonePaymentGroup.setDeclineComments(originalPaymentGroup.getDeclineComments());
            clonePaymentGroup.setApprovalRequestedBy(originalPaymentGroup.getApprovalRequestedBy());
            clonePaymentGroup.setLedgerEntries(entries);
            paymentGroupRepository.save(clonePaymentGroup);
        }
    }

    public PaymentGroupEntity recordInterestAssessed(int groupId) {
        PaymentGroupEntity paymentGroup = paymentGroupRepository.getOne(groupId);
        paymentGroup.setInterestAssessed(true);
        return paymentGroupRepository.save(paymentGroup);
    }

    public void executeScheduler(String schedulerName) {
        switch (schedulerName) {
            case "scheduleSkillsPayment":
                skillsPaymentScheduler.schedulePayments();
                break;
            default:
                throw new RuntimeException("Unrecognised scheduler name: " + schedulerName);
        }
    }

    public Set<ProjectLedgerEntry> findAllForClaim(Integer blockId, Integer claimId) {
        return projectLedgerRepository.findAllByBlockIdAndClaimId(blockId, claimId);
    }

    public List<ProjectLedgerEntry> findAllForBlockId(Integer blockId) {
        return projectLedgerRepository.findAllByBlockId(blockId);
    }


    public void updateFundingBlockRecordsWithClaim(FundingBlock projectFundingBlock, Collection<Claim> claims) {
        for (Claim claim: claims) {
            List<ProjectLedgerEntry> ledgerEntriesForClaim = findLedgerEntriesForClaim(projectFundingBlock.getId(), claim);
            ledgerEntriesForClaim.forEach(ple -> ple.setClaimId(claim.getId()));
            projectLedgerRepository.saveAll(ledgerEntriesForClaim);
        }
    }

    private List<ProjectLedgerEntry> findLedgerEntriesForClaim(Integer blockId, Claim claim) {
        if (claim.getClaimType() == ClaimType.ACTIVITY) {
            FundingActivity claimedActivity = projectFundingService.findActivitiesByBlockId(blockId)
                    .stream()
                    .filter(a -> a.getOriginalId().equals(claim.getEntityId()))
                    .findFirst()
                    .orElse(null);

            return claimedActivity == null ? new ArrayList<>() : claimedActivity.getLedgerEntries();
        } else {
            return projectLedgerRepository.findAllByBlockId(blockId).stream()
                    .filter(ple -> claim.getYear().equals(ple.getYear()))
                    .filter(ple -> claim.getClaimTypePeriod().equals(claim.getClaimType() == ClaimType.QUARTER
                        ? ple.getQuarter() : ple.getMonth()))
                    .filter(ple -> FORECAST.equals(ple.getLedgerStatus()))
                    .collect(Collectors.toList());
        }
    }

    public void updateFundingBlockRecordsFollowingDeletedClaim(FundingBlock projectFundingBlock, Integer claimID) {
        Set<ProjectLedgerEntry> allByBlockId = projectLedgerRepository
                .findAllByBlockIdAndClaimId(projectFundingBlock.getId(), claimID);
        allByBlockId.forEach(ple -> ple.setClaimId(null));
        projectLedgerRepository.saveAll(allByBlockId);
    }

    public List<PaymentSummary> getApprovedPaymentsForProject(Integer projectId) {
        return paymentSummaryRepository.findAllByProjectIdAndLedgerStatusIn(projectId, LedgerStatus.getApprovedPaymentStatuses());
    }

    public List<ProjectLedgerEntry> getProjectLedgerEntriesForBlock(NamedProjectBlock block) {
        return projectLedgerRepository.findAllByBlockId(block.getId());
    }

    public ProjectLedgerEntry getPaymentLedgerEntry(Integer projectLedgerEntryId) {
        return projectLedgerRepository.findById(projectLedgerEntryId).orElse(null);
    }

    public String getPaymentComments(Integer claimId) {
        List<String> activities =  projectLedgerRepository.getAllActivitiesNameByClaimId(claimId);
        return String.join(", ", activities);
    }

}
