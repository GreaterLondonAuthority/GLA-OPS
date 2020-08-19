/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.common.organisation.OrganisationType;
import uk.gov.london.ops.annualsubmission.AnnualSubmission;
import uk.gov.london.ops.annualsubmission.AnnualSubmissionService;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.domain.ProgrammeOrganisationID;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.dto.OrganisationDTOMapper;
import uk.gov.london.ops.organisation.dto.OrganisationUserDTO;
import uk.gov.london.ops.organisation.implementation.repository.*;
import uk.gov.london.ops.organisation.model.*;
import uk.gov.london.ops.permission.PermissionService;
import uk.gov.london.ops.programme.ProgrammeService;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.grant.IndicativeGrantBlock;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.ProjectSubStatus;
import uk.gov.london.ops.project.template.domain.Contract;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.refdata.TenureType;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.service.ContractService;
import uk.gov.london.ops.service.DataAccessControlService;
import uk.gov.london.ops.user.UserFinanceThresholdService;
import uk.gov.london.ops.user.UserMapper;
import uk.gov.london.ops.user.UserService;
import uk.gov.london.ops.user.domain.User;
import uk.gov.london.ops.user.domain.UserModel;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.london.common.GlaUtils.addBigDecimals;
import static uk.gov.london.common.GlaUtils.parseInt;
import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.notification.NotificationType.*;
import static uk.gov.london.ops.organisation.model.OrganisationAction.EDIT;
import static uk.gov.london.ops.organisation.model.OrganisationBudgetEntry.Type.Initial;
import static uk.gov.london.ops.organisation.model.OrganisationStatus.Inactive;
import static uk.gov.london.ops.organisation.model.OrganisationStatus.Rejected;
import static uk.gov.london.ops.permission.PermissionType.*;

/**
 * REST Web Service endpoint for Organisation data.
 * <p>
 * Created by sleach on 17/08/2016.
 */
@Transactional
@Service
public class OrganisationService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    OrganisationBudgetEntryRepository organisationBudgetEntryRepository;

    @Autowired
    OrganisationGroupRepository organisationGroupRepository;

    @Autowired
    OrganisationProgrammeRepository organisationProgrammeRepository;

    @Autowired
    OrganisationDTOMapper organisationDTOMapper;

    @Autowired
    OrganisationProgrammeSummaryRepository organisationProgrammeSummaryRepository;

    @Autowired
    RequestedAndPaidRecordRepository requestedAndPaidRecordRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationSummaryRepository organisationSummaryRepository;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProgrammeService programmeService;

    @Autowired
    AssociatedProjectsRecordRepository associatedProjectsRecordRepository;

    @Autowired
    AssociatedProjectRequestedAndSOSRecordRepository associatedProjectRequestedAndSOSRecordRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserFinanceThresholdService userFinanceThresholdService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    AuditService auditService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    Environment environment;

    @Autowired
    ContractService contractService;

    @Autowired
    OrganisationContractRepository organisationContractRepository;

    @Autowired
    DataAccessControlService dataAccessControlService;

    @Autowired
    AnnualSubmissionService annualSubmissionService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    FeatureStatus featureStatus;

    @Value("${user.watch.org.by.default}")
    boolean userWatchOrgByDefault = true;

    /**
     * Returns a list of all defined organisations.
     */
    public List<Organisation> findAll(List<Integer> entityTypes) {
        if (userService.currentUser().isOrgAdmin()) {
            if (!Collections.singletonList(OrganisationType.MANAGING_ORGANISATION.id()).equals(entityTypes)) {
                throw new ValidationException("cannot filter entity types other than managing organisation");
            }

            return organisationRepository.findAllByEntityType(OrganisationType.MANAGING_ORGANISATION.id());
        } else {
            return organisationRepository.findAll();
        }
    }

    public List<OrganisationSummary> findAllByType(OrganisationType type) {
        return organisationSummaryRepository.getOrganisationSummariesByEntityType(type.id());
    }

    public Page<OrganisationSummary> getSummaries(String searchText,
                                                  List<Integer> entityTypes,
                                                  List<OrganisationStatus> orgStatuses,
                                                  List<OrganisationTeam> teams,
                                                  Pageable pageable) {
        if (entityTypes == null || entityTypes.isEmpty()) {
            entityTypes = Stream.of(OrganisationType.values()).map(OrganisationType::id).collect(Collectors.toList());
        }
        entityTypes.removeIf(it -> it.equals(OrganisationType.TEAM.id()));
        User currentUser = userService.loadCurrentUser();
        return organisationSummaryRepository.findAll(currentUser, searchText, entityTypes, orgStatuses, teams, pageable);
    }

    public Organisation findOne(Integer id) {
        return organisationRepository.findById(id).orElse(null);
    }

    public Organisation findByName(String name) {
        return organisationRepository.findFirstByNameIgnoreCase(name);
    }

    public Organisation findByImsNumber(String name) {
        return organisationRepository.findFirstByImsNumber(name);
    }

    public List<Organisation> findAllByRegistrationKeyNull() {
        return organisationRepository.findAllByRegistrationKeyNull();
    }

    public boolean organisationExistsById(Integer id) {
        return organisationRepository.existsById(id);
    }

    public Organisation save(Organisation org) {
        if (org.getId() == null) {
            if (org.getCreatedBy() == null) {
                org.setCreatedBy(userService.currentUsername());
            }
            org.setCreatedOn(environment.now());
        } else {
            org.setModifiedBy(userService.currentUsername());
            org.setModifiedOn(environment.now());
        }
        return organisationRepository.save(org);
    }

    public List<Organisation> saveAll(List<Organisation> orgs) {
        return organisationRepository.saveAll(orgs);
    }

    public Organisation saveAndFlushOrganisation(Organisation org) {
        return organisationRepository.saveAndFlush(org);
    }

    public List<Organisation> findAll() {
        return organisationRepository.findAll();
    }

    public void deleteAllOrganisations(List<Organisation> orgList) {
        organisationRepository.deleteAll(orgList);
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

    public Organisation getEnrichedOrganisation(Integer id) {
        Organisation organisation = find(id);

        if (StringUtils.isNotEmpty(organisation.getApprovedBy())) {
            organisation.setApprovedByName(userService.find(organisation.getApprovedBy()).getFullName());
        }

        if (StringUtils.isNotEmpty(organisation.getCreatedBy())) {
            organisation.setCreatedByName(userService.find(organisation.getCreatedBy()).getFullName());
        }

        if (StringUtils.isNotEmpty(organisation.getRejectedBy())) {
            organisation.setRejectedByName(userService.find(organisation.getRejectedBy()).getFullName());
        }

        if (StringUtils.isNotEmpty(organisation.getInactivatedBy())) {
            organisation.setInactivatedByName(userService.find(organisation.getInactivatedBy()).getFullName());
        }

        if (StringUtils.isNotEmpty(organisation.getContactEmail())) {
            User glaContact = userService.find(organisation.getContactEmail());

            Organisation org = organisation.isManagingOrganisation()
                    ? organisation
                    : organisation.getManagingOrganisation();

            String glaContactEmail = glaContact.getUsername();
            boolean glaContactInOrg = getUsersForOrganisation(org.getId())
                    .stream()
                    .anyMatch(user -> Objects.equals(glaContactEmail, user.getUsername()));

            if (glaContactInOrg) {
                organisation.setGlaContactFullName(glaContact.getFullName());
            }

        }

        organisation.setContracts(getContracts(id));
        organisation.setProgrammes(getProgrammes(id));
        organisation.setAnnualSubmissions(getAnnualSubmissions(id));
        calculateAllowedActions(organisation);
        return organisation;
    }

    private void calculateAllowedActions(Organisation organisation) {
        if (currentUserCanEdit(organisation)) {
            organisation.getAllowedActions().add(EDIT);
        }
    }

    public boolean currentUserCanEdit(Organisation organisation) {
        User currentUser = userService.currentUser();
        return !organisation.isRejected() && !organisation.isInactive()
                && (permissionService.userHasPermissionForOrganisation(currentUser, ORG_EDIT_DETAILS, organisation.getId())
                        || (
                        organisation.getManagingOrganisation() != null
                         &&   currentUser.getRole(organisation.getManagingOrganisation()) != null
                         && currentUser.getRole(organisation.getManagingOrganisation()).getName().equals(GLA_ORG_ADMIN)));
    }

    public ProgrammeRequestedAndPaidRecord getRequestedAndPaidRecord(Integer programmeId, Integer organisationId) {

        Programme programme = programmeService.find(programmeId);
        Organisation organisation = organisationRepository.findById(organisationId).orElse(null);

        if (programme == null || organisation == null) {
            throw new ValidationException("Unrecognised organisation or programme " + programmeId + " " + organisationId);
        }

        RequestedAndPaidRecord strategic = requestedAndPaidRecordRepository.findById(new RequestedAndPaidRecordID(programmeId,
                organisationId, true)).orElse(null);
        RequestedAndPaidRecord nonStrategic = requestedAndPaidRecordRepository.findById(new RequestedAndPaidRecordID(programmeId,
                organisationId, false)).orElse(null);
        AssociatedProjectsRecord associatedProjectsRecord = associatedProjectsRecordRepository.findById(
                new ProgrammeOrganisationID(programmeId, organisationId)).orElse(null);
        Set<AssociatedProjectRequestedAndSOSRecord> associatedRecords =
                associatedProjectRequestedAndSOSRecordRepository.findAllByProgrammeIdAndOrgId(programmeId, organisationId);

        OrganisationProgramme orgProg =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);

        if (orgProg != null && orgProg.isStrategicPartnership()) {

            Set<TenureType> tenureTypes = new HashSet<>();
            for (Template template : programme.getTemplates()) {
                tenureTypes.addAll(template.getTenureTypes().stream().map(t -> t.getTenureType()).collect(Collectors.toSet()));
            }

            for (TenureType tenure : tenureTypes) {
                Optional<AssociatedProjectRequestedAndSOSRecord> first = associatedRecords
                        .stream()
                        .filter(r -> r.getTenureTypeExtId().equals(tenure.getId()))
                        .findFirst();
                if (!first.isPresent()) {
                    AssociatedProjectRequestedAndSOSRecord associatedProjectRequestedAndSOSRecord =
                            new AssociatedProjectRequestedAndSOSRecord();
                    associatedProjectRequestedAndSOSRecord.setOrgId(organisationId);
                    associatedProjectRequestedAndSOSRecord.setProgrammeId(programmeId);
                    associatedProjectRequestedAndSOSRecord.setTenureTypeExtId(tenure.getId());
                    associatedProjectRequestedAndSOSRecord.setTenureTypeName(tenure.getName());
                    associatedRecords.add(associatedProjectRequestedAndSOSRecord);
                }
            }

            if (orgProg.getPlannedUnits() != null && orgProg.getPlannedUnits().size() > 0) {
                for (StrategicPlannedUnitsForTenure tenure : orgProg.getPlannedUnits()) {
                    Optional<AssociatedProjectRequestedAndSOSRecord> first = associatedRecords
                            .stream()
                            .filter(r -> r.getTenureTypeExtId().equals(tenure.getTenureType()))
                            .findFirst();
                    if (first.isPresent()) {
                        first.get().setUnitsPlanned(tenure.getUnitsPlanned());
                    }
                }
            }
        }


        List<Project> indicatives = new ArrayList<>();
        Set<Template> templates = programme.getTemplates();

        for (Template template : templates) {
            if (!template.getBlocksByType(ProjectBlockType.IndicativeGrant).isEmpty()) {
                indicatives.addAll(projectService.findAllByProgrammeAndTemplateAndOrganisation(programme, template, organisation));
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
            if (orgProg != null && orgProg.isStrategicPartnership()) {
                strategic = new RequestedAndPaidRecord(new RequestedAndPaidRecordID(programmeId, organisationId,
                        true));

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
        return new ProgrammeRequestedAndPaidRecord(strategic, nonStrategic, associatedProjectsRecord,
                new StrategicPartnershipUnitSummary(associatedRecords));

    }


    /**
     * Returns details of a list of organisations, given the organisation's IDs.
     * filtered by the user's authorization
     */
    public List<Organisation> find(final Collection<Integer> idList) {
        final List<Organisation> organisation = organisationRepository
                .findAllById(idList);
        final User user = userService.loadCurrentUser();

        return organisation.stream()
                .filter(o -> dataAccessControlService.hasAccess(user, o))
                .collect(Collectors.toList());
    }

    private void setOrganisationToUsers(final User user,
                                        final Organisation organisation) {
        if (user.isOpsAdmin() || permissionService.currentUserHasPermissionForOrganisation(ORG_VIEW_USERS, organisation.getId())) {
            organisation.setUsers(userMapper.mapToModel(organisation.getUserEntities()));
        }
    }

    public Organisation findByOrgCode(String orgCode) {
        return findByOrgCode(orgCode, false);
    }

    public Organisation findByOrgCode(String orgCode, boolean searchOrgIds) {
        Integer orgId = GlaUtils.parseInt(orgCode);

        Organisation organisation = null;

        if (orgId != null) {
            Organisation orgLookedUpById = findOne(orgId);
            if (orgLookedUpById != null && (searchOrgIds || featureStatus.isEnabled(Feature.OrgIdLookup)
                    || dataAccessControlService.currentUserHasAccess(orgLookedUpById))) {
                organisation = orgLookedUpById;
            }
        }

        if (organisation == null) {
            organisation = organisationRepository.findFirstByImsNumber(orgCode);
        }

        if (organisation == null) {
            organisation = organisationRepository.findFirstByRegistrationKeyIgnoreCase(orgCode);
        }

        if (organisation == null || organisation.isRejected() || organisation.isInactive()) {
            throw new NotFoundException("Organisation with code " + orgCode + " not found!");
        }

        return organisation;
    }

    public OrganisationUserDTO register(OrganisationUserDTO organisation) {
        String name = organisation.name;
        log.debug(name + " Create DTO by : " + userService.currentUsername() + "\n " + organisation);

        Organisation organisationFromDTO = organisationDTOMapper.getOrganisationUserDTOFromOrg(organisation);

        Organisation newOrg = create(organisationFromDTO);

        log.debug(name + " Registration is null? : " + organisation.getUserRegistration());

        if (organisation.getUserRegistration() != null) {
            log.debug(name + " creating user ");

            organisation.getUserRegistration().setOrgCode(String.valueOf(newOrg.getRegistrationKey()));
            log.debug(name + " assigned reg key, attempting to register user, key: " + organisation.getUserRegistration());
            User newUser = userService.register(organisation.getUserRegistration());
            log.debug(name + " registered user success " + newUser.getUsername());
            newOrg.setCreatedBy(newUser.getUsername());
            log.debug(name + " created by set " + newOrg.getCreatedBy());

            Organisation updated = save(newOrg);
            log.debug(name + " created by saved " + updated.getCreatedBy());

            if (updated.getCreatedBy() == null) {
                throw new ValidationException("User registration failed for org: " + name);
            }

        } else if (userService.currentUser() == null) {
            log.debug(name + " No current user throwing exception");

            throw new ValidationException("Unable to create a new organisation profile as Organisation Admin Information is null");
        } else {
            log.debug(name + " Has a current user " + userService.currentUsername() + " no need to assign user?");
        }

        return organisationDTOMapper.getOrganisationUserDTOFromOrg(newOrg);
    }

    boolean checkOrgCanBeCreatedByAnonUser(Organisation organisation) {
        return OrganisationType.BOROUGH.id() == organisation.getEntityType()
                || OrganisationType.OTHER.id() == organisation.getEntityType()
                || OrganisationType.PROVIDER.id() == organisation.getEntityType()
                || OrganisationType.LEARNING_PROVIDER.id() == organisation.getEntityType()
                || OrganisationType.SMALL_BUSINESS.id() == organisation.getEntityType();
    }

    public Organisation create(Organisation organisation) {
        Organisation managingOrganisation =
                organisationRepository.findById(organisation.getManagingOrganisation().getId()).orElse(null);
        if (managingOrganisation == null) {
            organisation.setManagingOrganisation(organisationRepository.getOne(Organisation.GLA_HNL_ID));
        }

        if (managingOrganisation.getRegistrationAllowed() != true) {
            throw new ValidationException("Selected managing org does not allow registration.");
        }

        organisation.populateRegistrationKey();
        validateRegistrationKey(organisation);
        validateUkprn(organisation);

        organisation = save(organisation);

        User currentUser = userService.loadCurrentUser();
        if (currentUser == null || !currentUser.isGla()) {
            log.debug(organisation.getName() + " Create Organisation non-gla by : " + userService.currentUsername());

            if (!checkOrgCanBeCreatedByAnonUser(organisation)) {
                throw new ValidationException("Unable to create an organisation of this type.");
            }

            organisation.setStatus(OrganisationStatus.Pending);

            if (currentUser != null) {
                currentUser.addUnapprovedRole(ORG_ADMIN, organisation);
                userService.saveUser(currentUser);
            }

            notificationService.createNotification(OrganisationRegistration, organisation,
                    Collections.singletonMap("managingOrgId", organisation.getManagingOrganisation().getId()));
        } else if (userService.currentUser().isGla()) {
            log.debug(organisation.getName() + " Create Organisation gla by : " + userService.currentUsername());

            organisation.changeStatus(OrganisationStatus.Approved, userService.currentUser().getUsername(), environment.now());
            currentUser.addApprovedRole(GLA_ORG_ADMIN, organisation);
            userService.saveUser(currentUser);
            notificationService.subscribe(currentUser.getUsername(), EntityType.organisation, organisation.getId());
        }

        organisation = save(organisation);
        log.debug(organisation.getName() + " Update org final create check " + organisation.getCreatedBy());

        return organisation;
    }

    public Organisation update(Organisation updated) {
        Organisation current = findOne(updated.getId());

        validateEdits(updated, current);

        Organisation existing = findOne(updated.getId());
        updated.setUserEntities(existing.getUserEntities());
        updated.setContractEntities(existing.getContractEntities());
        updated.setManagedTeams(existing.getManagedTeams());

        auditService.auditCurrentUserActivity("Organisation edited: " + updated.getId());

        Integer updatedManagingOrgId = updated.getManagingOrganisation() != null ? updated.getManagingOrganisation().getId() : null;
        if (updatedManagingOrgId != null) {
            Organisation managingOrganisation = findOne(updatedManagingOrgId);
            if (!(current.getManagingOrganisation().getId().equals(updatedManagingOrgId))
                    && managingOrganisation.getRegistrationAllowed() != true) {
                throw new ValidationException("Selected managing org does not allow registration.");
            }
        }

        if (nameOrIMSCodeChanged(updated, current)) {
            auditService.auditCurrentUserActivity("Organisation name and/or IMS Code changed: " + updated.getId());
        }

        if (!Objects.equals(existing.getTeam(), updated.getTeam())) {
            auditService.auditCurrentUserActivity(String.format("organisation %s team changed from %s to %s",
                    existing.getName(), existing.getTeam(), updated.getTeam()));
        }

        if (!Objects.equals(existing.getRegistrationKey(), updated.getRegistrationKey())) {
            auditService.auditCurrentUserActivity(String.format("Changed registration key for org %s from %s to %s",
                    existing.getName(), existing.getRegistrationKey(), updated.getRegistrationKey()));
        }

        if (!Objects.equals(existing.getContactEmail(), updated.getContactEmail())) {
            if (existing.getContactEmail() != null) {
                String existingContactName = userService.find(existing.getContactEmail()).getFullName();

                if (StringUtils.isEmpty(updated.getContactEmail())) {
                    auditService.auditCurrentUserActivity(String.format("Changed GLA contect for org %s from %s to %s",
                            existing.getName(), existingContactName, "none"));
                } else {
                    String newContactName = userService.find(updated.getContactEmail()).getFullName();
                    auditService.auditCurrentUserActivity(String.format("Changed GLA contect for org %s from %s to %s",
                            existing.getName(), existingContactName, newContactName));
                }
            }
        }

        return save(updated);
    }

    void validateEdits(Organisation updated, Organisation current) {
        if (!currentUserCanEdit(updated)) {
            throw new ForbiddenAccessException("User does not have permission to edit organisation " + updated.getId());
        }

        if (nameOrIMSCodeChanged(updated, current)
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_NAME, updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit organisation name or IMS code for "
                 + updated.getId());
        }

        if (!Objects.equals(updated.getsapVendorId(), current.getsapVendorId())
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_VENDOR_SAP_ID, updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit SAP vendor ID for " + updated.getId());
        }

        if (!Objects.equals(updated.getEntityType(), current.getEntityType())
                && (!getAssignableOrganisationTypes().containsKey(updated.getEntityType())
                || !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_TYPE, updated.getId()))) {
            throw new ForbiddenAccessException("User does not have permission to edit organisation type for " + updated.getId());
        }

        if (!Objects.equals(updated.getManagingOrganisationId(), current.getManagingOrganisationId())
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_MANAGING_ORG, updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit the managing organisation for "
                   + updated.getId());
        }

        if (!Objects.equals(updated.getParentOrganisationId(), current.getParentOrganisationId())
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_PARENT_ORG, updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit the parent organisation for "
                   + updated.getId());
        }

        if (!Objects.equals(updated.getRegistrationKey(), current.getRegistrationKey())) {
            if (!permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_REGISTRATION_KEY, updated.getId())) {
                throw new ForbiddenAccessException("User does not have permission to edit the registration key for "
                        + updated.getId());
            }

            validateRegistrationKey(updated);
        }

        if (!Objects.equals(updated.getUkprn(), current.getUkprn())) {
            validateUkprn(updated);
        }
    }

    private void validateRegistrationKey(Organisation updated) {
        if (updated.getRegistrationKey() == null || updated.getRegistrationKey().length() < 5
                || updated.getRegistrationKey().contains(" ")) {
            // registration key must be at least 5 characters length and contain no space
            throw new ValidationException("Invalid registration key");
        }

        if (organisationRepository.countByRegistrationKey(updated.getRegistrationKey()) > 0) {
            // registration key must be unique
            throw new ValidationException("You have entered a unique registration key that already exists. "
                    + "Please enter a new key.");
        }

        Integer registrationKeyAsInt = parseInt(updated.getRegistrationKey());
        if (registrationKeyAsInt != null && organisationRepository.existsById(registrationKeyAsInt)) {
            // registration key cannot match any organisation OPS ID
            throw new ValidationException("Invalid registration key");
        }

        Set<Organisation> orgsByImsNumber = organisationRepository.findAllByImsNumber(updated.getRegistrationKey());
        if (orgsByImsNumber.size() > 0 && (orgsByImsNumber.size() > 1 || !orgsByImsNumber.iterator().next().equals(updated))) {
            // registration key cannot match any IMS code apart from own organisation
            throw new ValidationException("Invalid registration key");
        }
    }

    public Integer countOccuranceOfUkprn(Integer ukprn) {
        OrganisationStatus[] organisationStatuses = {Inactive, Rejected};
        return organisationRepository.countByUkprnAndStatusNotIn(ukprn, organisationStatuses);
    }

    public void validateUkprn(Organisation organisation) {
        if (organisation.getUkprn() != null && OrganisationType.LEARNING_PROVIDER.id() != organisation.getEntityType()) {
            throw new ValidationException("UKPRN is only used for learning providers");
        }
    }

    public void addUserToOrganisation(Integer id, String username) {
        addUserToOrganisation(id, username, userWatchOrgByDefault);
    }

    public void addUserToOrganisation(Integer id, String username, boolean subscribe) {
        Organisation organisation = find(id);

        User user = userService.find(username);
        user.addApprovedRole(Role.getDefaultForOrganisation(organisation), organisation);
        userService.saveUser(user);

        if (subscribe) {
            notificationService.subscribe(username, EntityType.organisation, organisation.getId());
        }

        auditService.auditCurrentUserActivity(String.format("User %s was added to Organisation %d.", user.getUsername(), id));
    }

    public void linkUserToOrganisation(String orgCode, String username) {
        Organisation organisation = findByOrgCode(orgCode);

        User user = userService.find(username);
        user.addUnapprovedRole(Role.getDefaultForOrganisation(organisation), organisation);
        userService.saveUser(user);

        Map<String, Object> model = new HashMap<String, Object>() {{
            put("organisation", organisation);
        }};

        notificationService.createNotification(UserRequestAccess, user, model);

    }

    public void deleteOrganisation(Integer id) {
        if (environment.initTestData() && (id == -123)) {
            // If we are in an environment that supports artificial test data
            // and we ask to delete the "magic" organisation ID
            // then we delete all test organisations.
            deleteTestOrganisations();
        } else {
            organisationRepository.deleteById(id);
            auditService.auditCurrentUserActivity("Deleted organisation " + id);
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

    public void removeUserFromOrganisation(Integer id, String username, String role) {
        User currentUser = userService.loadCurrentUser();
        User userToBeRemoved = userService.find(username);


        Organisation organisation = find(id);
        Set<Role> roles = userToBeRemoved.getRolesInOrganisation(organisation);
        if (!currentUser.isOpsAdmin() && !(currentUser.getOrganisations().contains(organisation) && !roles.isEmpty())) {
            throw new ValidationException("cannot remove a user from an organisation they don't belong to");
        }


        if (role == null) {
            userToBeRemoved.getRoles().removeAll(roles);
            processLastRoleRemoval(userToBeRemoved, organisation);

        } else {
            userToBeRemoved.getRoles().removeIf(r -> r.getName().equals(role) && r.getOrganisation().getId().equals(id));
            processSingleRoleRemoval(userToBeRemoved, organisation, role);
            // only teams at the moment but could be for all users
            if (organisation.getEntityType().equals(OrganisationType.TEAM.id())) {
                notificationService.createNotificationForUser(OrganisationRemoval, userToBeRemoved, Collections.singletonMap(
                        "organisation", organisation), userToBeRemoved.getUsername());
            }
        }


        if (Boolean.TRUE.equals(roles.stream().anyMatch(Role::isPrimaryOrganisationForUser))) {
            userService.assignDefaultPrimaryOrganisation(userToBeRemoved);
        }
    }

    private void processSingleRoleRemoval(User userToBeRemoved, Organisation organisation, String role) {
        auditService.auditCurrentUserActivity(String.format("User %s had role %s removed from Organisation %d.",
                userToBeRemoved.getUsername(), role, organisation.getId()));
        userService.saveUser(userToBeRemoved);
    }

    private void processLastRoleRemoval(User userToBeRemoved, Organisation organisation) {
        auditService.auditCurrentUserActivity(String.format("User %s was removed from Organisation %d.",
                userToBeRemoved.getUsername(), organisation.getId()));

        String username = userToBeRemoved.getUsername();

        userService.saveUser(userToBeRemoved);

        userFinanceThresholdService.clearFinanceThreshold(username, organisation.getId());

        organisationRepository.clearUserContactForOrganisationsManagedBy(username, organisation.getManagingOrganisationId());

        notificationService.createNotification(UserAccessRejection, userToBeRemoved,
                Collections.singletonMap("organisation", organisation));
        notificationService.unsubscribeFromOrganisation(username, organisation.getId());
    }

    public void approve(Integer organisationId, String username, String role) {
        Organisation organisation = find(organisationId);
        approve(organisation, username, role);
    }

    public void approve(Organisation organisation, String username, String newRole) {
        if (!organisation.isApproved()) {
            throw new ValidationException("This users organisation is pending approval. "
                    + "Approve the organisation to approve this user");
        }

        User user = userService.find(username);
        Role role = user.getRole(organisation);

        if (newRole != null && !newRole.isEmpty()) {
            role.setName(newRole);
        }

        role.approve();
        userService.saveUser(user);

        if (user.getPrimaryOrganisation() == null) {
            role.setPrimaryOrganisationForUser(true);
        }

        notificationService.createNotification(UserAccessApproval, user, Collections.singletonMap("organisation", organisation));

        if (userWatchOrgByDefault) {
            notificationService.subscribe(username, EntityType.organisation, organisation.getId());
        }

        auditService.auditCurrentUserActivity(String.format("User %s was approved on Organisation %d.", user.getUsername(),
                organisation.getId()));
    }

    public void unapprove(Integer organisationId, String username) {
        Organisation organisation = find(organisationId);

        User user = userService.find(username);
        user.getRole(organisation).unapprove();
        userService.saveUser(user);

        notificationService.unsubscribe(username, EntityType.organisation, organisationId);

        auditService.auditCurrentUserActivity(String.format("User %s was unapproved from Organisation %d.", user.getUsername(),
                organisationId));
    }

    /**
     * Returns true if the Organisation's name or IMS Code has changed from what is in the database.
     */
    public boolean nameOrIMSCodeChanged(Organisation updated, Organisation current) {
        if (!GlaUtils.nullSafeEquals(current.getName(), updated.getName())) {
            return true;
        }
        return !GlaUtils.nullSafeEquals(current.getImsNumber(), updated.getImsNumber());
    }

    public Organisation getOrganisationForProject(Project project) {
        if (project.getOrganisationGroupId() != null) {
            OrganisationGroup organisationGroup =
                    organisationGroupRepository.findById(project.getOrganisationGroupId()).orElse(null);
            if (organisationGroup != null && organisationGroup.getLeadOrganisation() != null) {
                return organisationGroup.getLeadOrganisation();
            }
        }

        return project.getOrganisation();
    }

    public Set<ContractModel> getContracts(Integer id) {
        Organisation organisation = find(id);

        Set<ContractModel> contracts = new HashSet<>();

        for (OrganisationContract contract : organisation.getContractEntities()) {
            contracts.add(new ContractModel(contract.getId(), contract.getContract().getId(), contract.getContract().getName(),
                    contract.getStatus(), contract.getOrgGroupType()));
        }

        List<Project> projects = projectService.findAllByOrganisationAndStatusName(organisation, ProjectStatus.Active.name());
        for (Project project : projects) {
            Contract contract = project.getTemplate().getContract();
            if (contract != null) {
                // as we are adding on a Set, any already existing contract (same name and org group type) will not be added
                OrganisationGroup.Type orgGroupType = null;
                if (project.getOrganisationGroupId() != null) {
                    orgGroupType = organisationGroupRepository.getOne(project.getOrganisationGroupId()).getType();
                }
                contracts.add(new ContractModel(null, contract.getId(), contract.getName(), OrganisationContract.Status.Blank,
                        orgGroupType));
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

        save(organisation);

        auditService.auditCurrentUserActivity("contract '" + entity.getContract().getName()
                + "' status updated to " + model.getStatus() + " for organisation " + organisation.getName());
    }

    public void updateContract(Integer id, ContractModel model) {
        Organisation organisation = find(id);

        OrganisationContract entity = organisation.getContractEntities()
                .stream()
                .filter(e -> e.getId().equals(model.getId()))
                .findFirst().get();
        entity.setStatus(model.getStatus());
        entity.setModifiedBy(userService.currentUser().getUsername());
        entity.setModifiedOn(environment.now());

        save(organisation);

        auditService.auditCurrentUserActivity("contract '" + entity.getContract().getName() + "' status updated to "
                + model.getStatus() + " for organisation " + organisation.getName());
    }

    public void deleteAllOrganisationContract() {
        organisationContractRepository.deleteAll();
    }

    public List<OrganisationProgrammeSummary> getProgrammes(Integer organisationId) {
        return organisationProgrammeSummaryRepository.findAllForOrganisation(organisationId);
    }

    public OrganisationProgramme getOrganisationProgramme(Integer organisationId, Integer programmeId) {
        OrganisationProgramme organisationProgramme =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);

        if (organisationProgramme == null) {
            organisationProgramme = new OrganisationProgramme();
            organisationProgramme.setId(new ProgrammeOrganisationID(programmeId, organisationId));
        }

        return populateOrganisationProgrammeData(organisationProgramme);
    }

    private OrganisationProgramme populateOrganisationProgrammeData(OrganisationProgramme organisationProgramme) {
        organisationProgramme.setProgramme(programmeService.getOne(organisationProgramme.getId().getProgrammeId()));

        organisationProgramme.setBudgetEntries(getBudgetEntries(organisationProgramme.getId().getOrgId(),
                organisationProgramme.getId().getProgrammeId()));

        return organisationProgramme;
    }

    /**
     * Return true if the organisation is marked as strategic for that given programme
     */
    public boolean isStrategic(Integer organisationId, Integer programmeId) {
        OrganisationProgramme organisationProgramme =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);
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
                        entry.isStrategic() ? "Strategic " : "", entry.getGrantType(), existingInitialEntry.getAmount(),
                        entry.getAmount()));
                existingInitialEntry.setAmount(entry.getAmount());
                entry = existingInitialEntry;
            }
        } else {
            entry.setType(OrganisationBudgetEntry.Type.Additional);
        }

        return organisationBudgetEntryRepository.save(entry);
    }

    private void validateBudgetEntry(OrganisationBudgetEntry entry) {
        if (entry.getApprovedOn() != null && entry.getApprovedOn().isAfter(environment.now().toLocalDate())) {
            throw new ValidationException("approval date cannot be in the future!");
        }

        if (entry.isStrategic()) {
            OrganisationProgramme organisationProgramme = organisationProgrammeRepository.findById(new ProgrammeOrganisationID(
                    entry.getProgrammeId(), entry.getOrganisationId())).orElse(null);
            if (organisationProgramme == null || !organisationProgramme.isStrategicPartnership()) {
                throw new ValidationException(
                        "cannot save a strategic budget entry if the organisation programme is not marked as strategic!");
            }
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (OrganisationBudgetEntry existingEntry : organisationBudgetEntryRepository.findAllLike(entry)) {
            sum = addBigDecimals(sum, existingEntry.getAmount());
        }
        sum = addBigDecimals(sum, entry.getAmount());
        if (sum.signum() == -1) {
            throw new ValidationException("the budget cannot be reduced by more than the current allocation");
        }
    }

    private OrganisationBudgetEntry findExistingInitialBudgetEntry(OrganisationBudgetEntry entry) {
        return organisationBudgetEntryRepository.findInitial(entry.getOrganisationId(), entry.getProgrammeId(),
                entry.getGrantType(), entry.isStrategic());
    }

    public void updateOrganisationProgramme(Integer organisationId, Integer programmeId,
                                            OrganisationProgramme organisationProgramme) {
        organisationProgramme.setId(new ProgrammeOrganisationID(programmeId, organisationId));
        organisationProgrammeRepository.save(organisationProgramme);
    }

    public void deleteBudgetEntry(Integer entryId) {
        OrganisationBudgetEntry entry = organisationBudgetEntryRepository.getOne(entryId);

        if (Initial.equals(entry.getType())) {
            throw new ValidationException("cannot delete an Initial approval entry!");
        }

        auditService.auditCurrentUserActivity(String.format("deleted budget entry for organisation %d programme %d with value %s",
                entry.getOrganisationId(), entry.getProgrammeId(), entry.getAmount()));

        organisationBudgetEntryRepository.deleteById(entryId);
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
        Long totalIndicativeGrantApproved = null;

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
            nonStrategic.setTotalApproved(nonStrategic.getTotalApproved() + (totalIndicativeGrantApproved != null
                    ? totalIndicativeGrantApproved : 0));
            nonStrategic.setTotalRequested(nonStrategic.getTotalRequested() + (totalIndicativeGrantRequested != null
                    ? totalIndicativeGrantRequested : 0));
        }
    }

    private Long getIndicativeGrantRequested(Project indicative) {
        Long indicativeGrantRequested = null;
        IndicativeGrantBlock indicativeBlock = (IndicativeGrantBlock) indicative.getSingleLatestBlockOfType(
                ProjectBlockType.IndicativeGrant);
        List<ProjectStatus> singleStatus = new ArrayList<ProjectStatus>() {{
            add(ProjectStatus.Assess);
        }};
        List<ProjectStatus> requiresSubStatus = new ArrayList<ProjectStatus>() {{
            add(ProjectStatus.Active);
        }};
        List<ProjectSubStatus> requestedSubStatus = new ArrayList<ProjectSubStatus>() {{
            add(ProjectSubStatus.ApprovalRequested);
            add(ProjectSubStatus.PaymentAuthorisationPending);
        }};

        if (NamedProjectBlock.BlockStatus.UNAPPROVED.equals(indicativeBlock.getBlockStatus())) {
            if (singleStatus.contains(indicative.getStatusType()) || (requiresSubStatus.contains(indicative.getStatusType())
                    && requestedSubStatus.contains(indicative.getSubStatusType()))) {
                indicativeGrantRequested = indicativeBlock.getTotalGrantEligibility();
            }
        }
        return indicativeGrantRequested;
    }

    private Long getIndicativeGrantApproved(Project indicative) {
        Long indicativeGrantApproved = null;
        List<ProjectStatus> singleStatus = new ArrayList<ProjectStatus>() {{
            add(ProjectStatus.Active);
        }};
        List<ProjectStatus> requiresSubStatus = new ArrayList<ProjectStatus>() {{
            add(ProjectStatus.Closed);
        }};
        List<ProjectSubStatus> requestedSubStatus = new ArrayList<ProjectSubStatus>() {{
            add(ProjectSubStatus.Completed);
        }};

        if (singleStatus.contains(indicative.getStatusType()) || (requiresSubStatus.contains(indicative.getStatusType())
                && requestedSubStatus.contains(indicative.getSubStatusType()))) {
            IndicativeGrantBlock indicativeBlock =
                    (IndicativeGrantBlock) indicative.getLatestApprovedBlock(ProjectBlockType.IndicativeGrant);
            indicativeGrantApproved = indicativeBlock.getTotalGrantEligibility();
        }
        return indicativeGrantApproved;
    }

    /**
     * Throws a NotFoundException if the given name is already used by another organisation, case insensitive.
     */
    public void checkOrganisationNameNotUsed(String name, Integer managingOrganisationId) {
        List<Organisation> organisations = organisationRepository.findByNameIgnoreCaseAndManagingOrganisation(name,
                managingOrganisationId);
        for (Organisation org : organisations) {
            if (org != null && !org.isRejected() && !org.isInactive()) {
                throw new NotFoundException("Organisation name " + name + " already in use!");
            }
        }
    }

    public List<AnnualSubmission> getAnnualSubmissions(Integer organisationId) {
        return annualSubmissionService.getAnnualSubmissions(organisationId);
    }

    public List<UserModel> getUsersForOrganisation(Integer organisationId) {
        Organisation org = findOne(organisationId);

        User currentUser = userService.loadCurrentUser();
        if (!currentUser.isManagedBy(org) && !currentUser.inOrganisation(org)) {
            throw new ForbiddenAccessException();
        }

        List<UserModel> usersSorted = userMapper.mapToModel(org.getUserEntities());
        usersSorted.sort(Comparator.comparing(UserModel::getFirstName));
        return usersSorted;
    }

    public ProgrammeRequestedAndPaidRecord updatePlannedUnits(Integer organisationId, Integer programmeId, Integer tenureExtId,
                                                              Integer plannedUnits) {
        OrganisationProgramme orgProgramme =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);

        if (orgProgramme == null) {
            throw new ValidationException("Unable to find record for requested Programme and Organisation");
        }

        Set<StrategicPlannedUnitsForTenure> plannedUnitsList = orgProgramme.getPlannedUnits();
        if (plannedUnitsList == null) {
            orgProgramme.setPlannedUnits(new HashSet<>());
        }

        Optional<StrategicPlannedUnitsForTenure> first = orgProgramme.getPlannedUnits().stream().filter(
                p -> p.getProgrammeId().equals(programmeId)
                        && p.getOrgId().equals(organisationId)
                        && p.getTenureType().equals(tenureExtId)).findFirst();

        if (first.isPresent()) {
            first.get().setUnitsPlanned(plannedUnits);
        } else {
            orgProgramme.getPlannedUnits().add(new StrategicPlannedUnitsForTenure(programmeId, organisationId, tenureExtId,
                    plannedUnits));
        }

        organisationProgrammeRepository.saveAndFlush(orgProgramme);
        return getRequestedAndPaidRecord(programmeId, organisationId);

    }

    public ProgrammeRequestedAndPaidRecord deletePlannedUnits(Integer organisationId, Integer programmeId, Integer tenureExtId) {
        OrganisationProgramme orgProgramme =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);

        if (orgProgramme == null) {
            throw new ValidationException("Unable to find record for requested Programme and Organisation");
        }

        boolean removed = orgProgramme.getPlannedUnits().removeIf(
                p -> p.getProgrammeId().equals(programmeId)
                        && p.getOrgId().equals(organisationId)
                        && p.getTenureType().equals(tenureExtId));

        if (!removed) {
            throw new ValidationException("Unable to find record for requested Programme and Organisation");
        }

        organisationProgrammeRepository.saveAndFlush(orgProgramme);
        return getRequestedAndPaidRecord(programmeId, organisationId);
    }

    public Set<OrganisationTeam> getManagedOrganisationAndTeams() {
        User user = userService.currentUser();
        Set<Organisation> myManagingOrgs = user.getRoles()
                .stream()
                .map(Role::getOrganisation)
                .filter(Organisation::isManagingOrganisation)
                .collect(Collectors.toSet());

        Set<OrganisationTeam> teams = new HashSet<>();
        for (Organisation myManagingOrg : myManagingOrgs) {
            teams.add(new OrganisationTeam(myManagingOrg.getId(), myManagingOrg.getName()));
            myManagingOrg = organisationRepository.getOne(myManagingOrg.getId());
            Set<Team> managedTeams = myManagingOrg.getManagedTeams();
            for (Team managedTeam : managedTeams) {
                teams.add(new OrganisationTeam(myManagingOrg.getId(), myManagingOrg.getName(), managedTeam.getId(),
                        managedTeam.getName()));
            }
        }

        return teams;

    }

    public List<OrganisationType> getAccessibleOrganisationTypes() {
        List<OrganisationType> accessibleOrganisationTypes = new ArrayList<>();

        accessibleOrganisationTypes.add(OrganisationType.BOROUGH);
        accessibleOrganisationTypes.add(OrganisationType.OTHER);
        accessibleOrganisationTypes.add(OrganisationType.PROVIDER);
        accessibleOrganisationTypes.add(OrganisationType.LEARNING_PROVIDER);
        accessibleOrganisationTypes.add(OrganisationType.SMALL_BUSINESS);

        User currentUser = userService.currentUser();
        if (currentUser != null && currentUser.isOpsAdmin()) {
            accessibleOrganisationTypes.add(OrganisationType.MANAGING_ORGANISATION);
            accessibleOrganisationTypes.add(OrganisationType.TECHNICAL_SUPPORT);
        }

        return accessibleOrganisationTypes;
    }

    public Map<Integer, String> getAssignableOrganisationTypes() {
        return getAccessibleOrganisationTypes().stream().collect(Collectors.toMap(OrganisationType::id, OrganisationType::summary));
    }

    public void changeStatus(Integer organisationId, OrganisationStatus status, OrganisationChangeStatusReason reason,
                             String details, Integer duplicateOrgId) {
        Organisation organisation = find(organisationId);

        preStateTransitionAction(organisation, status, reason, details, duplicateOrgId);
        OrganisationStatus previousStatus = organisation.getStatus();
        organisation.changeStatus(status, userService.loadCurrentUser().getUsername(), environment.now());
        organisation.setChangeStatusReason(reason);
        organisation.setChangeStatusReasonDetails(details);
        save(organisation);
        postStateTransitionAction(organisation, previousStatus);
    }

    private void preStateTransitionAction(Organisation organisation, OrganisationStatus status,
                                          OrganisationChangeStatusReason reason, String details, Integer duplicateOrgId) {
        boolean requiresReason = (status == Rejected || status == OrganisationStatus.Inactive);

        if (requiresReason && reason == null) {
            throw new ValidationException("Reason must be specified");
        }

        if (requiresReason && reason == OrganisationChangeStatusReason.Other && StringUtils.isEmpty(details)) {
            throw new ValidationException("Reason details must be specified when 'Other' is chosen.");
        }

        organisation.setDuplicateOrganisationId(null);
        if (reason == OrganisationChangeStatusReason.Duplicate) {
            Organisation duplicateOrganisation = organisationRepository.findById(duplicateOrgId).orElse(null);
            if (duplicateOrganisation == null) {
                throw new ValidationException("Duplicate organisation can't be found");
            }
            organisation.setDuplicateOrganisationId(duplicateOrganisation.getId());
        }
    }

    private void postStateTransitionAction(Organisation organisation, OrganisationStatus previousStatus) {
        String auditMessage = String.format("Organisation %s with ID %d was moved to status of %s.", organisation.getName(),
                organisation.getId(), organisation.getStatus());
        if (organisation.getChangeStatusReasonDetails() != null) {
            auditMessage += String.format(" Reason: %s", organisation.getChangeStatusReasonDetails());
        }
        auditService.auditCurrentUserActivity(auditMessage);

        switch (organisation.getStatus()) {
            case Rejected:
                notificationService.createNotification(OrganisationRejection, organisation, Collections.singletonMap(
                        "managingOrgId", organisation.getManagingOrganisation().getId()));
                for (User user : organisation.getUserEntities()) {
                    user.getRoles().remove(user.getRole(organisation));
                }
                break;
            case Inactive:
                notificationService.createNotification(OrganisationInactivation, organisation, Collections.singletonMap(
                        "managingOrgId", organisation.getManagingOrganisation().getId()));
                break;
            case Approved:
                if (previousStatus == Inactive) {
                    notificationService.createNotification(OrganisationReapproval, organisation, Collections.singletonMap(
                            "managingOrgId", organisation.getManagingOrganisation().getId()));
                } else {
                    for (String orgAdmin : organisation.getUsernames(ORG_ADMIN)) {
                        approve(organisation, orgAdmin, null);
                    }

                    notificationService.createNotification(OrganisationApproval, organisation, Collections.singletonMap(
                            "managingOrgId", organisation.getManagingOrganisation().getId()));
                }
                break;
            default:
        }
    }

    /**
     * Returns a map of all legal statuses for an organisation.
     */
    public Map<String, String> getLegalStatuses() {
        List<LegalStatus> legalStatuses = new ArrayList<>();
        for (LegalStatus ls : LegalStatus.values()) {
            legalStatuses.add(ls);
        }
        return legalStatuses.stream().collect(Collectors.toMap(LegalStatus::getName, LegalStatus::getDescription));
    }
}
