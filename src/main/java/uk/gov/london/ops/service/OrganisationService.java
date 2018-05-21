/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.ProgrammeOrganisationID;
import uk.gov.london.ops.domain.notification.NotificationType;
import uk.gov.london.ops.domain.organisation.*;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.template.Contract;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.mapper.UserMapper;
import uk.gov.london.ops.repository.*;
import uk.gov.london.ops.util.GlaOpsUtils;
import uk.gov.london.ops.web.model.AssignableRole;
import uk.gov.london.ops.web.model.ContractModel;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.organisation.OrganisationBudgetEntry.Type.Initial;
import static uk.gov.london.ops.domain.user.Role.*;
import static uk.gov.london.ops.domain.user.Role.GLA_READ_ONLY_DESC;
import static uk.gov.london.ops.util.GlaOpsUtils.addBigDecimals;

/**
 * REST Web Service endpoint for Organisation data.
 *
 * Created by sleach on 17/08/2016.
 */
@Transactional
@Service
public class OrganisationService {

    private static final List<AssignableRole> ops_admin_assignable_roles = Arrays.asList(
            new AssignableRole(OPS_ADMIN, OPS_ADMIN_DESC),
            new AssignableRole(GLA_ORG_ADMIN, GLA_ORG_ADMIN_DESC),
            new AssignableRole(GLA_SPM, GLA_SPM_DESC),
            new AssignableRole(GLA_PM, GLA_PM_DESC, true),
            new AssignableRole(GLA_FINANCE,  GLA_FINANCE_DESC),
            new AssignableRole(GLA_READ_ONLY,  GLA_READ_ONLY_DESC)
    );

    private static final List<AssignableRole> managing_organisation_assignable_roles = Arrays.asList(
            new AssignableRole(GLA_ORG_ADMIN, GLA_ORG_ADMIN_DESC),
            new AssignableRole(GLA_SPM, GLA_SPM_DESC),
            new AssignableRole(GLA_PM, GLA_PM_DESC, true),
            new AssignableRole(GLA_FINANCE,  GLA_FINANCE_DESC),
            new AssignableRole(GLA_READ_ONLY,  GLA_READ_ONLY_DESC)
    );

    private static final List<AssignableRole> organisation_assignable_roles = Arrays.asList(
            new AssignableRole(ORG_ADMIN, ORG_ADMIN_DESC),
            new AssignableRole(PROJECT_EDITOR, PROJECT_EDITOR_DESC, true)
    );

    private static final List<AssignableRole> tech_organisation_assignable_roles = Arrays.asList(
            new AssignableRole(TECH_ADMIN,  TECH_ADMIN_DESC),
            new AssignableRole(ORG_ADMIN, ORG_ADMIN_DESC),
            new AssignableRole(GLA_READ_ONLY,  GLA_READ_ONLY_DESC)
    );

    @Autowired
    OrganisationService organisationService;

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    OrganisationBudgetEntryRepository organisationBudgetEntryRepository;

    @Autowired
    OrganisationGroupRepository organisationGroupRepository;

    @Autowired
    OrganisationProgrammeRepository organisationProgrammeRepository;

    @Autowired
    OrganisationProgrammeSummaryRepository organisationProgrammeSummaryRepository;

    @Autowired
    RequestedAndPaidRecordRepository requestedAndPaidRecordRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationSummaryRepository organisationSummaryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProgrammeRepository programmeRepository;

    @Autowired
    AssociatedProjectsRecordRepository associatedProjectsRecordRepository;

    @Autowired
    UserService userService;

    @Autowired
    AuditService auditService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    Environment environment;

    @Autowired
    EmailService emailService;

    @Autowired
    ContractService contractService;

    @Autowired
    DataAccessControlService dataAccessControlService;

    /**
     * Returns a list of all defined organisations.
     */
    public List<Organisation> findAll(List<Integer> entityTypes) {
        if (userService.currentUser().isOrgAdmin()) {
            if (!Arrays.asList(OrganisationType.MANAGING_ORGANISATION.id()).equals(entityTypes)) {
                throw new ValidationException("cannot filter entity types other than managing organisation");
            }

            return organisationRepository.findAllByEntityType(OrganisationType.MANAGING_ORGANISATION.id());
        }
        else {
            return organisationRepository.findAll();
        }
    }

    /**
     * @deprecated use getSummaries() instead and remove once we switch to managing org filter
     */
    public Page<Organisation> findAll(List<RegistrationStatus> userRegStatuses, Pageable pageable) {
        if (userService.currentUser().isGla()) {
            return getAllOrganisationsPaged(userRegStatuses, pageable);
        } else {
            return getCurrentUserOrganisationPaged();
        }
    }

    /**
     * @deprecated use getSummaries() instead and remove once we switch to managing org filter
     */
    public Page<Organisation> getCurrentUserOrganisationPaged() {
        return new PageImpl<>(new ArrayList<>(userService.loadCurrentUser().getOrganisations()));
    }

    /**
     * @deprecated use getSummaries() instead and remove once we switch to managing org filter
     */
    public Page<Organisation> getAllOrganisationsPaged(List<RegistrationStatus> userRegStatuses, Pageable pageable) {
        List<Sort.Order> orderList = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            orderList.add(new Sort.Order(order.getDirection(), order.getProperty(), Sort.NullHandling.NULLS_LAST).ignoreCase());
        }
        pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(orderList));
        if (userRegStatuses != null && !userRegStatuses.isEmpty()) {
            return organisationRepository.findByUserRegStatus(userRegStatuses.get(0), pageable);
        } else {
            return organisationRepository.findAll(pageable);
        }
    }

    public Page<OrganisationSummary> getSummaries(String searchText, List<Integer> entityTypes, List<OrganisationStatus> orgStatuses, List<RegistrationStatus> userRegStatuses, Pageable pageable) {
        return organisationSummaryRepository.findAll(userService.currentUser(), searchText, entityTypes, orgStatuses, userRegStatuses, pageable);
    }

    public Organisation findOne(Integer id) {
        return organisationRepository.findOne(id);
    }

    /**
     * Returns details of a single organisation, given the organisation's ID.
     * if the user has authorization
     */
    public Organisation find(Integer id) {
        final Organisation organisation = findOne(id);
        if (organisation == null) {
            throw new NotFoundException();
        }
        final User user = userService.loadCurrentUser();

        dataAccessControlService.checkAccess(user, organisation);

        setOrganisationToUsers(user, organisation);

        return organisation;
    }

    public ProgrammeRequestedAndPaidRecord getRequestedAndPaidRecord(Integer programmeId, Integer organisationId) {

        Programme programme = programmeRepository.findOne(programmeId);
        Organisation organisation = organisationRepository.getOne(organisationId);

        if (programme == null || organisation == null) {
            throw new ValidationException("Unrecognised organisation or programme " + programmeId + " " + organisationId);
        }

        RequestedAndPaidRecord strategic = requestedAndPaidRecordRepository.findOne(new RequestedAndPaidRecordID(programmeId, organisationId, true));
        RequestedAndPaidRecord nonStrategic = requestedAndPaidRecordRepository.findOne(new RequestedAndPaidRecordID(programmeId, organisationId, false));
        AssociatedProjectsRecord associatedProjectsRecord = associatedProjectsRecordRepository.findOne(new ProgrammeOrganisationID(programmeId, organisationId));


        List<Project> indicatives = new ArrayList<>();
        Set<Template> templates = programme.getTemplates();

        for (Template template : templates) {
            if (!template.getBlocksByType(ProjectBlockType.IndicativeGrant).isEmpty()) {
                indicatives.addAll(projectRepository.findAllByProgrammeAndTemplateAndOrganisation(programme, template, organisation));
            }
        }

        if (indicatives.size() > 0) {
            if (nonStrategic == null) {
                nonStrategic = new RequestedAndPaidRecord();
            }
            updateIndicativeCostsForOrganisationProgramme(indicatives, nonStrategic);
        }


        // if not strategic response then return skeleton for UI
        if (strategic == null) {
            OrganisationProgramme orgProg = organisationProgrammeRepository.findOne(new ProgrammeOrganisationID(programmeId, organisationId));
            if (orgProg != null && orgProg.isStrategicPartnership()) {
                strategic = new RequestedAndPaidRecord(new RequestedAndPaidRecordID(programmeId, organisationId, true));

            }
        }

        if (strategic != null && associatedProjectsRecord != null) {
            if (strategic.getTotalRequested() != null) {
                long requestedVariance = strategic.getTotalRequested() - associatedProjectsRecord.getStrategicRequested();
                associatedProjectsRecord.setRequestedVariance(requestedVariance);
            }
            if (strategic.getTotalPaid() != null) {
                long sosVariance = strategic.getTotalPaid() - associatedProjectsRecord.getStartedOnSite();
                associatedProjectsRecord.setVarianceBetweenPaidAndSoSClaimed(sosVariance);
            }
        }

        return new ProgrammeRequestedAndPaidRecord(strategic, nonStrategic, associatedProjectsRecord);
    }



    /**
     * Returns details of a list of organisations, given the organisation's IDs.
     * filtered by the user's authorization
     */
    public List<Organisation> find(final Collection<Integer> idList) {
        final List<Organisation> organisation = organisationRepository
                .findAll(idList);
        final User user = userService.loadCurrentUser();

        return organisation.stream()
                .filter(o-> dataAccessControlService.hasAccess(user, o))
                .collect(Collectors.toList());
    }


    private void setOrganisationToUsers(final User user,
                                        final Organisation organisation) {
        if (user.isOpsAdmin() || user.isGlaOrgAdmin() || user.isOrgAdmin()) {
            organisation.setUsers(UserMapper.mapToModel(organisation.getUserEntities()));
        }
    }

    public Organisation findByOrgIdOrImsNumber(String orgCode) {
        Integer orgId = GlaOpsUtils.parseInt(orgCode);

        Organisation organisation = null;

        if (orgId != null) {
            organisation = findOne(orgId);
        }

        if (organisation == null) {
            organisation = organisationRepository.findFirstByImsNumber(orgCode);
        }

        if (organisation == null) {
            throw new NotFoundException("Organisation with IMS number "+orgCode+" not found!");
        }

        return organisation;
    }

    public Organisation create(Organisation organisation) {
        if (organisation.getManagingOrganisation() == null) {
            organisation.setManagingOrganisation(organisationRepository.findOne(Organisation.GLA_HNL_ID));
        }

        if (userService.currentUser().isGla()) {
            organisation.setStatus(OrganisationStatus.Approved);
        }

        organisation = organisationRepository.save(organisation);

        if (!userService.currentUser().isGla()) {
            organisation.setStatus(OrganisationStatus.Pending);

            User currentUser = userService.currentUser();
            currentUser.addUnapprovedRole(Role.ORG_ADMIN, organisation);
            userRepository.save(currentUser);

            String text = "Pending organisation profile request for "+organisation.getName()+" requires approval";
            Organisation managingOrganisation = organisationRepository.findOne(organisation.getManagingOrganisationId());
            List<String> glaOrgAdmins = managingOrganisation.getUsernames(Role.GLA_ORG_ADMIN);
            notificationService.createNotification(NotificationType.Action, text, organisation, glaOrgAdmins);
        }

        return organisation;
    }

    public Organisation update(Organisation organisation) {
        Organisation existingEntity = findOne(organisation.getId());
        organisation.setUserEntities(existingEntity.getUserEntities());
        organisation.setContractEntities(existingEntity.getContractEntities());

        auditService.auditCurrentUserActivity("Organisation edited: " + organisation.getId());

        return organisationRepository.save(organisation);
    }

    public void addUserToOrganisation(Integer id, String username) {
        Organisation organisation = find(id);

        User user = userService.find(username);
        user.addApprovedRole(Role.getDefaultForOrganisation(organisation), organisation);
        userRepository.save(user);

        updateOrganisationUserRegStatus(organisation);

        auditService.auditCurrentUserActivity(String.format("User %s was added to Organisation %d.", user.getUsername(), id));
    }

    public void linkUserToOrganisation(String orgCode, String username) {
        Organisation organisation = findByOrgIdOrImsNumber(orgCode);

        User user = userService.find(username);
        user.addUnapprovedRole(Role.getDefaultForOrganisation(organisation), organisation);
        userRepository.save(user);

        updateOrganisationUserRegStatus(organisation);
    }

    public void deleteOrganisation(Integer id) {
        if (environment.initTestData() && (id == -123)) {
            // If we are in an environment that supports artificial test data
            // and we ask to delete the "magic" organisation ID
            // then we delete all test organisations.
            deleteTestOrganisations();
        } else {
            organisationRepository.delete(id);
            auditService.auditCurrentUserActivity("Deleted organisation " + id);
        }
    }

    public void deleteOrganisationIfExists(int id) {
        if (organisationRepository.exists(id)) {
            organisationRepository.delete(id);
        }
    }

    private void deleteTestOrganisations() {
        final AtomicInteger count = new AtomicInteger();
        organisationRepository.findAll().stream().filter(this::isTestOrganisation).forEach(org -> {
            count.incrementAndGet();
            organisationRepository.delete(org);
        });
        auditService.auditCurrentUserActivity("Deleted " + count.get() + " test organisations");
    }

    private boolean isTestOrganisation(Organisation org) {
        return org.getName().startsWith("A Test");
    }

    public void removeUserFromOrganisation(Integer id, String username) {
        User currentUser = userService.loadCurrentUser();
        User userToBeRemoved = userService.find(username);

        Organisation organisation = find(id);
        if (!currentUser.isOpsAdmin() && !(currentUser.getOrganisations().contains(organisation) && userToBeRemoved.getRole(organisation) != null)) {
            throw new ValidationException("cannot remove a user from an organisation he doesn't belong to");
        }

        userToBeRemoved.getRoles().remove(userToBeRemoved.getRole(organisation));
        auditService.auditCurrentUserActivity(String.format("User %s was removed from Organisation %d.",
                userToBeRemoved.getUsername(), id));

        userRepository.save(userToBeRemoved);

        updateOrganisationUserRegStatus(organisation);

        notificationService.unsubscribe(username, EntityType.organisation, id);
        List<Project> projects = projectRepository.findAllByOrganisation(organisation);
        for (Project project: projects) {
            notificationService.unsubscribe(username, EntityType.project, project.getId());
        }

        emailService.sendRejectionEmail(userToBeRemoved, organisation);
    }

    public void approve(Integer organisationId, String username) {
        Organisation organisation = find(organisationId);
        approve(organisation, username);
    }

    public void approve(Organisation organisation, String username) {
        User user = userService.find(username);
        user.getRole(organisation).approve();
        userRepository.save(user);

        updateOrganisationUserRegStatus(organisation);

        emailService.sendApprovalEmail(user);

        auditService.auditCurrentUserActivity(String.format("User %s was approved on Organisation %d.", user.getUsername(), organisation.getId()));
    }

    public void unapprove(Integer organisationId, String username) {
        Organisation organisation = find(organisationId);

        User user = userService.find(username);
        user.getRole(organisation).unapprove();
        userRepository.save(user);

        updateOrganisationUserRegStatus(organisation);

        auditService.auditCurrentUserActivity(String.format("User %s was unapproved from Organisation %d.", user.getUsername(), organisationId));
    }

    public void updateOrganisationUserRegStatus(Organisation organisation) {
        List<User> orgUsers = organisation.getUserEntities();
        if ((orgUsers == null) || (orgUsers.isEmpty())) {
            organisation.setUserRegStatus(null);
        }
        else {
            organisation.setUserRegStatus(RegistrationStatus.Approved);
            orgUsers.stream().filter(user -> user.getRole(organisation) != null && !user.getRole(organisation).isApproved()).forEach(user -> organisation.setUserRegStatus(RegistrationStatus.Pending));
        }
        organisationRepository.save(organisation);
    }

    /**
     * Returns true if the Organisation's name or IMS Code has changed from what is in the database.
     */
    public boolean nameOrIMSCOdeChanged(Organisation organisation) {
        if (organisation.getId() == null) {
            throw new IllegalArgumentException("Cannot check organisation without ID");
        }
        Organisation current = organisationRepository.getOne(organisation.getId());
        if (!GlaOpsUtils.nullSafeEquals(current.getName(),organisation.getName())) {
            return true;
        }
        if (!GlaOpsUtils.nullSafeEquals(current.getImsNumber(),organisation.getImsNumber())) {
            return true;
        }
        return false;
    }

    public Organisation getOrganisationForProject(Project project) {
        if (project.getOrganisationGroupId() != null) {
            OrganisationGroup organisationGroup = organisationGroupRepository.findOne(project.getOrganisationGroupId());
            if (organisationGroup != null && organisationGroup.getLeadOrganisation() != null) {
                return organisationGroup.getLeadOrganisation();
            }
        }

        return project.getOrganisation();
    }

    public Set<ContractModel> getContracts(Integer id) {
        Organisation organisation = find(id);

        Set<ContractModel> contracts = new HashSet<>();

        for (OrganisationContract contract: organisation.getContractEntities()) {
            contracts.add(new ContractModel(contract.getId(), contract.getContract().getId(), contract.getContract().getName(), contract.getStatus(), contract.getOrgGroupType()));
        }

        List<Project> projects = projectRepository.findAllByOrganisationAndStatus(organisation, Project.Status.Active);
        for (Project project: projects) {
            Contract contract = project.getTemplate().getContract();
            if (contract != null) {
                // as we are adding on a Set, any already existing contract (same name and org group type) will not be added
                OrganisationGroup.Type orgGroupType = null;
                if (project.getOrganisationGroupId() != null) {
                    orgGroupType = organisationGroupRepository.findOne(project.getOrganisationGroupId()).getType();
                }
                contracts.add(new ContractModel(null, contract.getId(), contract.getName(), OrganisationContract.Status.Blank, orgGroupType));
            }
        }

        return contracts;
    }

    public void createContract(Integer id, ContractModel model) {
        Organisation organisation = find(id);
        Contract contract = contractService.find(model.getContractId());

        OrganisationContract entity = new OrganisationContract(contract, model.getStatus(), model.getOrgGroupType());
        entity.setCreatedBy(userService.currentUser().getUsername());
        entity.setCreatedOn(environment.now());
        organisation.getContractEntities().add(entity);

        organisationRepository.save(organisation);

        auditService.auditCurrentUserActivity("contract '"+entity.getContract().getName()+"' status updated to "+model.getStatus()+" for organisation "+organisation.getName());
    }

    public void updateContract(Integer id, ContractModel model) {
        Organisation organisation = find(id);

        OrganisationContract entity = organisation.getContractEntities().stream().filter(e -> e.getId().equals(model.getId())).findFirst().get();
        entity.setStatus(model.getStatus());
        entity.setModifiedBy(userService.currentUser().getUsername());
        entity.setModifiedOn(environment.now());

        organisationRepository.save(organisation);

        auditService.auditCurrentUserActivity("contract '"+entity.getContract().getName()+"' status updated to "+model.getStatus()+" for organisation "+organisation.getName());
    }

    public void updateStatus(Integer organisationId, OrganisationStatus status) {
        Organisation organisation = find(organisationId);

        organisation.setStatus(status);
        organisationRepository.save(organisation);

        if (OrganisationStatus.Approved.equals(status)) {
            List<String> orgAdmins = organisation.getUsernames(Role.ORG_ADMIN);
            for (String orgAdmin: orgAdmins) {
                approve(organisation, orgAdmin);
            }

            String notificationText = String.format("The profile for %s has been approved by GLA and can now be used to create projects for %s", organisation.getName(), organisation.getManagingOrganisationName());
            notificationService.createNotification(NotificationType.Info, notificationText, organisation, orgAdmins);
        }
    }

    public List<OrganisationProgrammeSummary> getProgrammes(Integer organisationId) {
        return organisationProgrammeSummaryRepository.findAllForOrganisation(organisationId);
    }

    public OrganisationProgramme getOrganisationProgramme(Integer organisationId, Integer programmeId) {
        OrganisationProgramme organisationProgramme = organisationProgrammeRepository.findOne(new ProgrammeOrganisationID(programmeId, organisationId));

        if (organisationProgramme == null) {
            organisationProgramme = new OrganisationProgramme();
        }

        organisationProgramme.setProgramme(programmeRepository.findOne(programmeId));

        organisationProgramme.setBudgetEntries(getBudgetEntries(organisationId, programmeId));

        return organisationProgramme;
    }

    /**
     * Return true if the organisation is marked as strategic for that given programme
     */
    public boolean isStrategic(Integer organisationId, Integer programmeId) {
        OrganisationProgramme organisationProgramme = organisationProgrammeRepository.findOne(new ProgrammeOrganisationID(programmeId, organisationId));
        return organisationProgramme != null && organisationProgramme.isStrategicPartnership();
    }

    private List<OrganisationBudgetEntry> getBudgetEntries(Integer organisationId, Integer programmeId) {
        return organisationBudgetEntryRepository.findAllByOrganisationIdAndProgrammeId(organisationId, programmeId);
    }

    public OrganisationBudgetEntry saveBudgetEntry(Integer organisationId, Integer programmeId, OrganisationBudgetEntry entry) {
        entry.setOrganisationId(organisationId);
        entry.setProgrammeId(programmeId);

        validateBudgetEntry(entry);

        if (Initial.equals(entry.getType())) {
            OrganisationBudgetEntry existingInitialEntry = findExistingInitialBudgetEntry(entry);
            if (existingInitialEntry != null) {
                auditService.auditCurrentUserActivity(String.format("Initial %s%s approval value changed from %s to %s",
                        entry.isStrategic() ? "Strategic ": "", entry.getGrantType(), existingInitialEntry.getAmount(), entry.getAmount()));
                existingInitialEntry.setAmount(entry.getAmount());
                entry = existingInitialEntry;
            }
        }
        else {
            entry.setType(OrganisationBudgetEntry.Type.Additional);
        }

        return organisationBudgetEntryRepository.save(entry);
    }

    private void validateBudgetEntry(OrganisationBudgetEntry entry) {
        if (entry.getApprovedOn() != null && entry.getApprovedOn().isAfter(environment.now().toLocalDate())) {
            throw new ValidationException("approval date cannot be in the future!");
        }

        if (entry.isStrategic()) {
            OrganisationProgramme organisationProgramme = organisationProgrammeRepository.findOne(new ProgrammeOrganisationID(entry.getProgrammeId(), entry.getOrganisationId()));
            if (organisationProgramme == null || !organisationProgramme.isStrategicPartnership()) {
                throw new ValidationException("cannot save a strategic budget entry if the organisation programme is not marked as strategic!");
            }
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (OrganisationBudgetEntry existingEntry: organisationBudgetEntryRepository.findAllLike(entry)) {
            sum = addBigDecimals(sum, existingEntry.getAmount());
        }
        sum = addBigDecimals(sum, entry.getAmount());
        if (sum.signum() == -1) {
            throw new ValidationException("the budget cannot be reduced by more than the current allocation");
        }
    }

    private OrganisationBudgetEntry findExistingInitialBudgetEntry(OrganisationBudgetEntry entry) {
        return organisationBudgetEntryRepository.findInitial(entry.getOrganisationId(), entry.getProgrammeId(), entry.getGrantType(), entry.isStrategic());
    }

    public void updateOrganisationProgramme(Integer organisationId, Integer programmeId, OrganisationProgramme organisationProgramme) {
        organisationProgramme.setId(new ProgrammeOrganisationID(programmeId, organisationId));
        organisationProgrammeRepository.save(organisationProgramme);
    }

    public void deleteBudgetEntry(Integer entryId) {
        OrganisationBudgetEntry entry = organisationBudgetEntryRepository.findOne(entryId);

        if (Initial.equals(entry.getType())) {
            throw new ValidationException("cannot delete an Initial approval entry!");
        }

        auditService.auditCurrentUserActivity(String.format("deleted budget entry for organisation %d programme %d with value %s",
                entry.getOrganisationId(), entry.getProgrammeId(), entry.getAmount()));

        organisationBudgetEntryRepository.delete(entryId);
    }

    public boolean isManagingOrganisation(Integer orgId) {
        return findOne(orgId).isManagingOrganisation();
    }

    public boolean isTechSupportOrganisation(Integer orgId) {
        return findOne(orgId).isTechSupportOrganisation();
    }


    // this should be in project service but autowiring causes issues.
    private void updateIndicativeCostsForOrganisationProgramme(List<Project> indicatives, RequestedAndPaidRecord nonStrategic) {


        Long totalIndicativeGrantRequested = null;
        Long totalIndicativeGrantApproved  = null;

        for (Project indicative : indicatives) {

            Long indicativeGrantApproved = getIndicativeGrantApproved(indicative);
            if (indicativeGrantApproved != null) {
                totalIndicativeGrantApproved = totalIndicativeGrantApproved == null ? indicativeGrantApproved :
                        totalIndicativeGrantApproved + indicativeGrantApproved;
            }

            Long indicativeGrantRequested = getIndicativeGrantRequested(indicative);
            // default requested to approved
            if (indicativeGrantRequested != null) {
                totalIndicativeGrantRequested = totalIndicativeGrantRequested == null ? indicativeGrantRequested :
                        totalIndicativeGrantRequested + indicativeGrantRequested;
            } else if (indicativeGrantApproved != null) {
                totalIndicativeGrantRequested = totalIndicativeGrantRequested == null ? indicativeGrantApproved :
                        totalIndicativeGrantApproved + indicativeGrantApproved;
            }
        }

        if (nonStrategic != null) {
            nonStrategic.setIndicativeGrantApproved(totalIndicativeGrantApproved);
            nonStrategic.setIndicativeGrantRequested(totalIndicativeGrantRequested);
        }
    }

    private Long getIndicativeGrantRequested(Project indicative) {
        Long indicativeGrantRequested = null;
        IndicativeGrantBlock indicativeBlock = (IndicativeGrantBlock) indicative.getSingleLatestBlockOfType(ProjectBlockType.IndicativeGrant);
        List<Project.Status> singleStatus = new ArrayList<Project.Status>(){{  add(Project.Status.Assess); }};
        List<Project.Status> requiresSubStatus = new ArrayList<Project.Status>(){{ add(Project.Status.Active);  }};
        List<Project.SubStatus> requestedSubStatus = new ArrayList<Project.SubStatus>(){{ add(Project.SubStatus.ApprovalRequested); add(Project.SubStatus.PaymentAuthorisationPending); }};

        if (NamedProjectBlock.BlockStatus.UNAPPROVED.equals(indicativeBlock.getBlockStatus())) {
            if (singleStatus.contains(indicative.getStatus()) || (requiresSubStatus.contains(indicative.getStatus()) && requestedSubStatus.contains(indicative.getSubStatus()))) {
                indicativeGrantRequested = indicativeBlock.getTotalGrantEligibility();
            }
        }
        return indicativeGrantRequested;
    }

    private Long getIndicativeGrantApproved(Project indicative) {
        Long indicativeGrantApproved = null;
        List<Project.Status> singleStatus = new ArrayList<Project.Status>(){{  add(Project.Status.Active);  }};
        List<Project.Status> requiresSubStatus = new ArrayList<Project.Status>(){{  add(Project.Status.Closed); }};
        List<Project.SubStatus> requestedSubStatus = new ArrayList<Project.SubStatus>(){{ add(Project.SubStatus.Completed); }};

        if (singleStatus.contains(indicative.getStatus()) || (requiresSubStatus.contains(indicative.getStatus()) && requestedSubStatus.contains(indicative.getSubStatus()))) {
            IndicativeGrantBlock indicativeBlock = (IndicativeGrantBlock) indicative.getLatestApprovedBlock(ProjectBlockType.IndicativeGrant);
            indicativeGrantApproved = indicativeBlock.getTotalGrantEligibility();
        }
        return indicativeGrantApproved;
    }

    /**
     * Throws a NotFoundException if the given name is already used by another organisation, case insensitive.
     */
    public void checkOrganisationNameNotUsed(String name) {
        if (organisationRepository.findFirstByNameIgnoreCase(name) != null) {
            throw new NotFoundException("Organisation name "+name+" already in use!");
        }
    }

    public List<AssignableRole> getAssignableRoles(Integer orgId) {
        if (organisationService.isManagingOrganisation(orgId)) {
            if (userService.currentUser().isOpsAdmin()) {
                return ops_admin_assignable_roles;
            }
            else {
                return managing_organisation_assignable_roles;
            }
        }
        else if (organisationService.isTechSupportOrganisation(orgId)) {
            return tech_organisation_assignable_roles;
        }
        else {
            return organisation_assignable_roles;
        }
    }

}
