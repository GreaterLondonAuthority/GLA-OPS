/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import static uk.gov.london.common.GlaUtils.addBigDecimals;
import static uk.gov.london.common.GlaUtils.parseInt;
import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_FINANCE_DESC;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN_DESC;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_PM_DESC;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY_DESC;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.GLA_SPM_DESC;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN_DESC;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN_DESC;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR_DESC;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER_DESC;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN_DESC;
import static uk.gov.london.ops.domain.organisation.OrganisationAction.EDIT;
import static uk.gov.london.ops.domain.organisation.OrganisationBudgetEntry.Type.Initial;
import static uk.gov.london.ops.domain.organisation.OrganisationStatus.Inactive;
import static uk.gov.london.ops.domain.organisation.OrganisationStatus.Rejected;
import static uk.gov.london.ops.notification.NotificationType.OrganisationApproval;
import static uk.gov.london.ops.notification.NotificationType.OrganisationInactivation;
import static uk.gov.london.ops.notification.NotificationType.OrganisationReapproval;
import static uk.gov.london.ops.notification.NotificationType.OrganisationRegistration;
import static uk.gov.london.ops.notification.NotificationType.OrganisationRejection;
import static uk.gov.london.ops.notification.NotificationType.UserAccessApproval;
import static uk.gov.london.ops.notification.NotificationType.UserRequestAccess;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_DETAILS;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_MANAGING_ORG;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_NAME;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_PARENT_ORG;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_REGISTRATION_KEY;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_TYPE;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_VENDOR_SAP_ID;
import static uk.gov.london.ops.service.PermissionType.TEAM_EDIT;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.common.organisation.OrganisationType;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.annualsubmission.AnnualSubmission;
import uk.gov.london.ops.annualsubmission.AnnualSubmissionService;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.ProgrammeOrganisationID;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationBudgetEntry;
import uk.gov.london.ops.domain.organisation.OrganisationChangeStatusReason;
import uk.gov.london.ops.domain.organisation.OrganisationContract;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;
import uk.gov.london.ops.domain.organisation.OrganisationProgramme;
import uk.gov.london.ops.domain.organisation.OrganisationProgrammeSummary;
import uk.gov.london.ops.domain.organisation.OrganisationStatus;
import uk.gov.london.ops.domain.organisation.OrganisationSummary;
import uk.gov.london.ops.domain.organisation.OrganisationTeam;
import uk.gov.london.ops.domain.organisation.RegistrationStatus;
import uk.gov.london.ops.domain.organisation.StrategicPlannedUnitsForTenure;
import uk.gov.london.ops.domain.organisation.Team;
import uk.gov.london.ops.domain.project.AssociatedProjectRequestedAndSOSRecord;
import uk.gov.london.ops.domain.project.AssociatedProjectsRecord;
import uk.gov.london.ops.domain.project.IndicativeGrantBlock;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.ProgrammeRequestedAndPaidRecord;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.project.RequestedAndPaidRecord;
import uk.gov.london.ops.domain.project.RequestedAndPaidRecordID;
import uk.gov.london.ops.domain.project.StrategicPartnershipUnitSummary;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.project.state.ProjectSubStatus;
import uk.gov.london.ops.domain.template.Contract;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.notification.EmailService;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.implementation.OrganisationDTOMapper;
import uk.gov.london.ops.organisation.implementation.UserMapper;
import uk.gov.london.ops.refdata.TenureType;
import uk.gov.london.ops.repository.AssociatedProjectRequestedAndSOSRecordRepository;
import uk.gov.london.ops.repository.AssociatedProjectsRecordRepository;
import uk.gov.london.ops.repository.OrganisationBudgetEntryRepository;
import uk.gov.london.ops.repository.OrganisationGroupRepository;
import uk.gov.london.ops.repository.OrganisationProgrammeRepository;
import uk.gov.london.ops.repository.OrganisationProgrammeSummaryRepository;
import uk.gov.london.ops.repository.OrganisationRepository;
import uk.gov.london.ops.repository.OrganisationSummaryRepository;
import uk.gov.london.ops.repository.ProgrammeRepository;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.repository.RequestedAndPaidRecordRepository;
import uk.gov.london.ops.repository.TeamRepository;
import uk.gov.london.ops.repository.UserRepository;
import uk.gov.london.ops.web.model.AssignableRole;
import uk.gov.london.ops.web.model.ContractModel;
import uk.gov.london.ops.web.model.OrganisationUserDTO;
import uk.gov.london.ops.web.model.UserModel;

/**
 * REST Web Service endpoint for Organisation data.
 * <p>
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
            new AssignableRole(GLA_FINANCE, GLA_FINANCE_DESC),
            new AssignableRole(GLA_READ_ONLY, GLA_READ_ONLY_DESC)
    );

    private static final List<AssignableRole> managing_organisation_assignable_roles = Arrays.asList(
            new AssignableRole(GLA_ORG_ADMIN, GLA_ORG_ADMIN_DESC),
            new AssignableRole(GLA_SPM, GLA_SPM_DESC),
            new AssignableRole(GLA_PM, GLA_PM_DESC, true),
            new AssignableRole(GLA_FINANCE, GLA_FINANCE_DESC),
            new AssignableRole(GLA_READ_ONLY, GLA_READ_ONLY_DESC)
    );

    private static final List<AssignableRole> organisation_assignable_roles = Arrays.asList(
            new AssignableRole(ORG_ADMIN, ORG_ADMIN_DESC),
            new AssignableRole(PROJECT_EDITOR, PROJECT_EDITOR_DESC, true),
            new AssignableRole(PROJECT_READER, PROJECT_READER_DESC)
    );

    private static final List<AssignableRole> tech_organisation_assignable_roles = Arrays.asList(
            new AssignableRole(TECH_ADMIN, TECH_ADMIN_DESC),
            new AssignableRole(ORG_ADMIN, ORG_ADMIN_DESC),
            new AssignableRole(GLA_READ_ONLY, GLA_READ_ONLY_DESC)
    );

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
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProgrammeRepository programmeRepository;

    @Autowired
    AssociatedProjectsRecordRepository associatedProjectsRecordRepository;

    @Autowired
    AssociatedProjectRequestedAndSOSRecordRepository associatedProjectRequestedAndSOSRecordRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserMapper userMapper;

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
            if (!Arrays.asList(OrganisationType.MANAGING_ORGANISATION.id()).equals(entityTypes)) {
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

    public Page<OrganisationSummary> getSummaries(String searchText, List<Integer> entityTypes, List<OrganisationStatus> orgStatuses, List<RegistrationStatus> userRegStatuses, List<OrganisationTeam> teams, Pageable pageable) {
        return organisationSummaryRepository.findAll(userService.loadCurrentUser(), searchText, entityTypes, orgStatuses, userRegStatuses, teams, pageable);
    }

    public Organisation findOne(Integer id) {
        return organisationRepository.findById(id).orElse(null);
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
        return !organisation.isRejected() && !organisation.isInactive() &&
                (permissionService.userHasPermissionForOrganisation(currentUser, ORG_EDIT_DETAILS.getPermissionKey(), organisation.getId())
                        || (
                        organisation.getManagingOrganisation() != null &&
                                currentUser.getRole(organisation.getManagingOrganisation()) != null &&
                                currentUser.getRole(organisation.getManagingOrganisation()).getName().equals(GLA_ORG_ADMIN)));
    }

    public ProgrammeRequestedAndPaidRecord getRequestedAndPaidRecord(Integer programmeId, Integer organisationId) {

        Programme programme = programmeRepository.findById(programmeId).orElse(null);
        Organisation organisation = organisationRepository.findById(organisationId).orElse(null);

        if (programme == null || organisation == null) {
            throw new ValidationException("Unrecognised organisation or programme " + programmeId + " " + organisationId);
        }

        RequestedAndPaidRecord strategic = requestedAndPaidRecordRepository.findById(new RequestedAndPaidRecordID(programmeId, organisationId, true)).orElse(null);
        RequestedAndPaidRecord nonStrategic = requestedAndPaidRecordRepository.findById(new RequestedAndPaidRecordID(programmeId, organisationId, false)).orElse(null);
        AssociatedProjectsRecord associatedProjectsRecord = associatedProjectsRecordRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId)).orElse(null);
        Set<AssociatedProjectRequestedAndSOSRecord> associatedRecords = associatedProjectRequestedAndSOSRecordRepository.findAllByProgrammeIdAndOrgId(programmeId, organisationId);

        OrganisationProgramme orgProg = organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId)).orElse(null);

        if (orgProg != null && orgProg.isStrategicPartnership()) {

            Set<TenureType> tenureTypes = new HashSet<>();
            for (Template template : programme.getTemplates()) {
                tenureTypes.addAll(template.getTenureTypes().stream().map(t -> t.getTenureType()).collect(Collectors.toSet()));
            }

            for (TenureType tenure : tenureTypes) {
                Optional<AssociatedProjectRequestedAndSOSRecord> first = associatedRecords.stream().filter(r -> r.getTenureTypeExtId().equals(tenure.getId())).findFirst();
                if (!first.isPresent()) {
                    AssociatedProjectRequestedAndSOSRecord associatedProjectRequestedAndSOSRecord = new AssociatedProjectRequestedAndSOSRecord();
                    associatedProjectRequestedAndSOSRecord.setOrgId(organisationId);
                    associatedProjectRequestedAndSOSRecord.setProgrammeId(programmeId);
                    associatedProjectRequestedAndSOSRecord.setTenureTypeExtId(tenure.getId());
                    associatedProjectRequestedAndSOSRecord.setTenureTypeName(tenure.getName());
                    associatedRecords.add(associatedProjectRequestedAndSOSRecord);
                }
            }

            if (orgProg.getPlannedUnits() != null && orgProg.getPlannedUnits().size() > 0) {
                for (StrategicPlannedUnitsForTenure tenure : orgProg.getPlannedUnits()) {
                    Optional<AssociatedProjectRequestedAndSOSRecord> first = associatedRecords.stream().filter(r -> r.getTenureTypeExtId().equals(tenure.getTenureType())).findFirst();
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
        return new ProgrammeRequestedAndPaidRecord(strategic, nonStrategic, associatedProjectsRecord, new StrategicPartnershipUnitSummary(associatedRecords));

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
        if (user.isOpsAdmin() || user.isGlaOrgAdmin() || user.isOrgAdmin()) {
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
            if (orgLookedUpById != null && (searchOrgIds || featureStatus.isEnabled(Feature.OrgIdLookup) || dataAccessControlService.currentUserHasAccess(orgLookedUpById))) {
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

    public OrganisationUserDTO create(OrganisationUserDTO organisation) {
        String name = organisation.getName();
        log.warn(name + " Create DTO by : " + userService.currentUsername() + "\n " + organisation);

        Organisation organisationFromDTO = organisationDTOMapper.getOrganisationUserDTOFromOrg(organisation);

        Organisation newOrg = create(organisationFromDTO);

        log.warn(name + " Registration is null? : " +organisation.getUserRegistration());

        if(organisation.getUserRegistration() != null) {
            log.warn(name + " creating user ");

            organisation.getUserRegistration().setOrgCode(String.valueOf(newOrg.getRegistrationKey()));
            log.warn(name + " assigned reg key, attempting to register user, key: " +organisation.getUserRegistration() );
            User newUser = userService.register(organisation.getUserRegistration());
            log.warn(name + " registered user success " + newUser.getUsername() );
            newOrg.setCreatedBy(newUser.getUsername());
            log.warn(name + " created by set " + newOrg.getCreatedBy() );

            Organisation updated =  organisationRepository.save(newOrg);
            log.warn(name + " created by saved " + updated.getCreatedBy() );

            if (updated.getCreatedBy() == null) {
                throw new ValidationException("User registration failed for org: " + name);

            }

        } else if (userService.currentUser() == null) {
            log.warn(name + " No current user throwing exception" );

            throw new ValidationException("Unable to create a new organisation profile as Organisation Admin Information is null");
        } else {
            log.warn(name + " Has a current user " + userService.currentUsername() + " no need to assign user?");
        }

        return organisationDTOMapper.getOrganisationUserDTOFromOrg(newOrg);

    }

    boolean checkOrgCanBeCreatedByAnonUser(Organisation organisation) {
        return OrganisationType.BOROUGH.id() == organisation.getEntityType() ||
                OrganisationType.OTHER.id() == organisation.getEntityType() ||
                OrganisationType.PROVIDER.id() == organisation.getEntityType() ||
                OrganisationType.LEARNING_PROVIDER.id() == organisation.getEntityType();
    }

    public Organisation create(Organisation organisation) {
        Organisation managingOrganisation = organisationRepository.findById(organisation.getManagingOrganisation().getId()).orElse(null);
        if (managingOrganisation == null) {
            organisation.setManagingOrganisation(organisationRepository.getOne(Organisation.GLA_HNL_ID));
        }

        if (managingOrganisation.getRegistrationAllowed() != true) {
            throw new ValidationException("Selected managing org does not allow registration.");
        }

        organisation.populateRegistrationKey();
        validateRegistrationKey(organisation);
        validateUkprn(organisation);

        organisation = organisationRepository.save(organisation);

        User currentUser = userService.currentUser();
        if (currentUser == null || !currentUser.isGla()) {
            log.warn(organisation.getName() + " Create Organisation non-gla by : " + userService.currentUsername());

            if (!checkOrgCanBeCreatedByAnonUser(organisation)) {
                throw new ValidationException("Unable to create an organisation of this type.");
            }

            organisation.setStatus(OrganisationStatus.Pending);

            if (currentUser != null) {
                currentUser.addUnapprovedRole(ORG_ADMIN, organisation);
                userRepository.save(currentUser);
            }

            notificationService.createNotification(OrganisationRegistration, organisation, Collections.singletonMap("managingOrgId", organisation.getManagingOrganisation().getId()));
        } else if (userService.currentUser().isGla()) {
            log.warn(organisation.getName() + " Create Organisation gla by : " + userService.currentUsername() );

            organisation.setStatus(OrganisationStatus.Approved);
            organisation.setApprovedOn(environment.now());
            organisation.setApprovedBy(userService.currentUser().getUsername());
            currentUser.addApprovedRole(GLA_ORG_ADMIN, organisation);
            userRepository.save(currentUser);
            notificationService.subscribe(currentUser.getUsername(), EntityType.organisation, organisation.getId());
        }

        if (currentUser != null) {
            log.warn(organisation.getName() + " Setting created_by ");
            log.warn(organisation.getName() + " Setting Created_by Organisation gla by : " + userService.loadCurrentUser().getUsername() );
            organisation.setCreatedBy(userService.loadCurrentUser().getUsername());
        }

        organisation.setCreatedOn(environment.now());

        organisation = organisationRepository.save(organisation);
        log.warn(organisation.getName() + " Update org final create check " + organisation.getCreatedBy());


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
            if (!(current.getManagingOrganisation().getId().equals(updatedManagingOrgId)) && managingOrganisation.getRegistrationAllowed() != true) {
                throw new ValidationException("Selected managing org does not allow registration.");
            }
        }

        if (nameOrIMSCOdeChanged(updated, current)) {
            auditService.auditCurrentUserActivity("Organisation name and/or IMS Code changed: " + updated.getId());
        }

        if (!Objects.equals(existing.getTeam(), updated.getTeam())) {
            auditService.auditCurrentUserActivity(String.format("organisation %s team changed from %s to %s",
                    existing.getName(), String.valueOf(existing.getTeam()), String.valueOf(updated.getTeam())));
        }

        if (!Objects.equals(existing.getRegistrationKey(), updated.getRegistrationKey())) {
            auditService.auditCurrentUserActivity(String.format("Changed registration key for org %s from %s to %s", existing.getName(), existing.getRegistrationKey(), updated.getRegistrationKey()));
        }

        if (!Objects.equals(existing.getContactEmail(), updated.getContactEmail())) {
            if (existing.getContactEmail() != null) {
                String existingContactName = userService.find(existing.getContactEmail()).getFullName();

                if (StringUtils.isEmpty(updated.getContactEmail())) {
                    auditService.auditCurrentUserActivity(String.format("Changed GLA contect for org %s from %s to %s", existing.getName(), existingContactName, "none"));
                } else {
                    String newContactName = userService.find(updated.getContactEmail()).getFullName();
                    auditService.auditCurrentUserActivity(String.format("Changed GLA contect for org %s from %s to %s", existing.getName(), existingContactName, newContactName));
                }
            }
        }

        return organisationRepository.save(updated);
    }

    void validateEdits(Organisation updated, Organisation current) {
        if (!currentUserCanEdit(updated)) {
            throw new ForbiddenAccessException("User does not have permission to edit organisation " + updated.getId());
        }

        if (nameOrIMSCOdeChanged(updated, current)
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_NAME.getPermissionKey(), updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit organisation name or IMS code for " + updated.getId());
        }

        if (!Objects.equals(updated.getsapVendorId(), current.getsapVendorId())
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_VENDOR_SAP_ID.getPermissionKey(), updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit SAP vendor ID for " + updated.getId());
        }

        if (!Objects.equals(updated.getEntityType(), current.getEntityType())
                && (!getAssignableOrganisationTypes().keySet().contains(updated.getEntityType())
                || !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_TYPE.getPermissionKey(), updated.getId()))) {
            throw new ForbiddenAccessException("User does not have permission to edit organisation type for " + updated.getId());
        }

        if (!Objects.equals(updated.getManagingOrganisationId(), current.getManagingOrganisationId())
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_MANAGING_ORG.getPermissionKey(), updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit the managing organisation for " + updated.getId());
        }

        if (!Objects.equals(updated.getParentOrganisationId(), current.getParentOrganisationId())
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_PARENT_ORG.getPermissionKey(), updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit the parent organisation for " + updated.getId());
        }

        if (!Objects.equals(updated.getRegistrationKey(), current.getRegistrationKey())) {
            if (!permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_REGISTRATION_KEY.getPermissionKey(), updated.getId())) {
                throw new ForbiddenAccessException("User does not have permission to edit the registration key for " + updated.getId());
            }

            validateRegistrationKey(updated);
        }

        if (!Objects.equals(updated.getUkprn(), current.getUkprn())) {
            validateUkprn(updated);
        }
    }

    private void validateRegistrationKey(Organisation updated) {
        if (updated.getRegistrationKey() == null || updated.getRegistrationKey().length() < 5 || updated.getRegistrationKey().contains(" ")) {
            // registration key must be at least 5 characters length and contain no space
            throw new ValidationException("Invalid registration key");
        }

        if (organisationRepository.countByRegistrationKey(updated.getRegistrationKey()) > 0) {
            // registration key must be unique
            throw new ValidationException("You have entered a unique registration key that already exists. Please enter a new key.");
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

    public Integer countOccuranceOfUkprn(Integer ukprn){
        OrganisationStatus[] organisationStatuses = {Inactive,Rejected};
        return organisationRepository.countByUkprnAndStatusNotIn(ukprn,organisationStatuses);
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
        userRepository.save(user);

        updateOrganisationUserRegStatus(organisation);

        if (subscribe) {
            notificationService.subscribe(username, EntityType.organisation, organisation.getId());
        }

        auditService.auditCurrentUserActivity(String.format("User %s was added to Organisation %d.", user.getUsername(), id));
    }

    public void linkUserToOrganisation(String orgCode, String username) {
        Organisation organisation = findByOrgCode(orgCode);

        User user = userService.find(username);
        user.addUnapprovedRole(Role.getDefaultForOrganisation(organisation), organisation);
        userRepository.save(user);

        updateOrganisationUserRegStatus(organisation);

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

    public void deleteOrganisationIfExists(int id) {
        if (organisationRepository.existsById(id)) {
            organisationRepository.deleteById(id);
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
        Set<Role> roles = userToBeRemoved.getRolesInOrganisation(organisation);
        if (!currentUser.isOpsAdmin() && !(currentUser.getOrganisations().contains(organisation) && !roles.isEmpty())) {
            throw new ValidationException("cannot remove a user from an organisation they don't belong to");
        }

        userToBeRemoved.getRoles().removeAll(roles);
        auditService.auditCurrentUserActivity(String.format("User %s was removed from Organisation %d.",
                userToBeRemoved.getUsername(), id));


        if (Boolean.TRUE.equals(roles.stream().anyMatch(r -> r.isPrimaryOrganisationForUser() ))) {
            userService.assignDefaultPrimaryOrganisation(userToBeRemoved);
        }

        userRepository.save(userToBeRemoved);

        userService.clearFinanceThreshold(username, id);

        updateOrganisationUserRegStatus(organisation);

        organisationRepository.clearUserContactForOrganisationsManagedBy(username, organisation.getManagingOrganisationId());

        notificationService.unsubscribe(username, EntityType.organisation, id);
        List<Project> projects = projectRepository.findAllByOrganisation(organisation);
        for (Project project : projects) {
            notificationService.unsubscribe(username, EntityType.project, project.getId());
        }

        emailService.sendUserRejectionEmail(userToBeRemoved, organisation);
    }

    public void approve(Integer organisationId, String username) {
        Organisation organisation = find(organisationId);
        approve(organisation, username);
    }

    public void approve(Organisation organisation, String username) {
        if (!organisation.isApproved()) {
            throw new ValidationException("This users organisation is pending approval. Approve the organisation to approve this user");
        }

        User user = userService.find(username);
        Role role = user.getRole(organisation);
        role.approve();
        userRepository.save(user);

        if (user.getPrimaryOrganisation() == null) {
            role.setPrimaryOrganisationForUser(true);
        }

        updateOrganisationUserRegStatus(organisation);

        notificationService.createNotification(UserAccessApproval, user, Collections.singletonMap("organisation", organisation));

        if (userWatchOrgByDefault) {
            notificationService.subscribe(username, EntityType.organisation, organisation.getId());
        }

        auditService.auditCurrentUserActivity(String.format("User %s was approved on Organisation %d.", user.getUsername(), organisation.getId()));
    }

    public void unapprove(Integer organisationId, String username) {
        Organisation organisation = find(organisationId);

        User user = userService.find(username);
        user.getRole(organisation).unapprove();
        userRepository.save(user);

        updateOrganisationUserRegStatus(organisation);

        notificationService.unsubscribe(username, EntityType.organisation, organisationId);

        auditService.auditCurrentUserActivity(String.format("User %s was unapproved from Organisation %d.", user.getUsername(), organisationId));
    }

    public void updateOrganisationUserRegStatus(Organisation organisation) {
        List<User> orgUsers = organisation.getUserEntities();
        if ((orgUsers == null) || (orgUsers.isEmpty())) {
            organisation.setUserRegStatus(null);
        } else {
            organisation.setUserRegStatus(RegistrationStatus.Approved);
            orgUsers.stream().filter(user -> user.getRole(organisation.getId()) != null && !user.getRole(organisation).isApproved()).
                    forEach(user -> organisation.setUserRegStatus(RegistrationStatus.Pending));
        }
        organisationRepository.save(organisation);
    }

    /**
     * Returns true if the Organisation's name or IMS Code has changed from what is in the database.
     */
    public boolean nameOrIMSCOdeChanged(Organisation updated, Organisation current) {
        if (!GlaUtils.nullSafeEquals(current.getName(), updated.getName())) {
            return true;
        }
        if (!GlaUtils.nullSafeEquals(current.getImsNumber(), updated.getImsNumber())) {
            return true;
        }
        return false;
    }

    public Organisation getOrganisationForProject(Project project) {
        if (project.getOrganisationGroupId() != null) {
            OrganisationGroup organisationGroup = organisationGroupRepository.findById(project.getOrganisationGroupId()).orElse(null);
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
            contracts.add(new ContractModel(contract.getId(), contract.getContract().getId(), contract.getContract().getName(), contract.getStatus(), contract.getOrgGroupType()));
        }

        List<Project> projects = projectRepository.findAllByOrganisationAndStatusName(organisation, ProjectStatus.Active.name());
        for (Project project : projects) {
            Contract contract = project.getTemplate().getContract();
            if (contract != null) {
                // as we are adding on a Set, any already existing contract (same name and org group type) will not be added
                OrganisationGroup.Type orgGroupType = null;
                if (project.getOrganisationGroupId() != null) {
                    orgGroupType = organisationGroupRepository.getOne(project.getOrganisationGroupId()).getType();
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

        auditService.auditCurrentUserActivity("contract '" + entity.getContract().getName() + "' status updated to " + model.getStatus() + " for organisation " + organisation.getName());
    }

    public void updateContract(Integer id, ContractModel model) {
        Organisation organisation = find(id);

        OrganisationContract entity = organisation.getContractEntities().stream().filter(e -> e.getId().equals(model.getId())).findFirst().get();
        entity.setStatus(model.getStatus());
        entity.setModifiedBy(userService.currentUser().getUsername());
        entity.setModifiedOn(environment.now());

        organisationRepository.save(organisation);

        auditService.auditCurrentUserActivity("contract '" + entity.getContract().getName() + "' status updated to " + model.getStatus() + " for organisation " + organisation.getName());
    }


    public List<OrganisationProgrammeSummary> getProgrammes(Integer organisationId) {
        return organisationProgrammeSummaryRepository.findAllForOrganisation(organisationId);
    }

    public OrganisationProgramme getOrganisationProgramme(Integer organisationId, Integer programmeId) {
        OrganisationProgramme organisationProgramme = organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId)).orElse(null);

        if (organisationProgramme == null) {
            organisationProgramme = new OrganisationProgramme();
            organisationProgramme.setId(new ProgrammeOrganisationID(programmeId, organisationId));
        }

        return populateOrganisationProgrammeData(organisationProgramme);
    }

    private OrganisationProgramme populateOrganisationProgrammeData(OrganisationProgramme organisationProgramme) {
        organisationProgramme.setProgramme(programmeRepository.getOne(organisationProgramme.getId().getProgrammeId()));

        organisationProgramme.setBudgetEntries(getBudgetEntries(organisationProgramme.getId().getOrgId(), organisationProgramme.getId().getProgrammeId()));

        return organisationProgramme;
    }

    /**
     * Return true if the organisation is marked as strategic for that given programme
     */
    public boolean isStrategic(Integer organisationId, Integer programmeId) {
        OrganisationProgramme organisationProgramme = organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId)).orElse(null);
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
                        entry.isStrategic() ? "Strategic " : "", entry.getGrantType(), existingInitialEntry.getAmount(), entry.getAmount()));
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
            OrganisationProgramme organisationProgramme = organisationProgrammeRepository.findById(new ProgrammeOrganisationID(entry.getProgrammeId(), entry.getOrganisationId())).orElse(null);
            if (organisationProgramme == null || !organisationProgramme.isStrategicPartnership()) {
                throw new ValidationException("cannot save a strategic budget entry if the organisation programme is not marked as strategic!");
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
        return organisationBudgetEntryRepository.findInitial(entry.getOrganisationId(), entry.getProgrammeId(), entry.getGrantType(), entry.isStrategic());
    }

    public void updateOrganisationProgramme(Integer organisationId, Integer programmeId, OrganisationProgramme organisationProgramme) {
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
            nonStrategic.setTotalApproved(nonStrategic.getTotalApproved() + (totalIndicativeGrantApproved != null ? totalIndicativeGrantApproved : 0));
            nonStrategic.setTotalRequested(nonStrategic.getTotalRequested() + (totalIndicativeGrantRequested != null ? totalIndicativeGrantRequested : 0));
        }
    }

    private Long getIndicativeGrantRequested(Project indicative) {
        Long indicativeGrantRequested = null;
        IndicativeGrantBlock indicativeBlock = (IndicativeGrantBlock) indicative.getSingleLatestBlockOfType(ProjectBlockType.IndicativeGrant);
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
            if (singleStatus.contains(indicative.getStatusType()) || (requiresSubStatus.contains(indicative.getStatusType()) && requestedSubStatus.contains(indicative.getSubStatusType()))) {
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

        if (singleStatus.contains(indicative.getStatusType()) || (requiresSubStatus.contains(indicative.getStatusType()) && requestedSubStatus.contains(indicative.getSubStatusType()))) {
            IndicativeGrantBlock indicativeBlock = (IndicativeGrantBlock) indicative.getLatestApprovedBlock(ProjectBlockType.IndicativeGrant);
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

    public List<AssignableRole> getAssignableRoles(Integer orgId) {
        if (isManagingOrganisation(orgId)) {
            if (userService.currentUser().isOpsAdmin()) {
                return ops_admin_assignable_roles;
            } else {
                return managing_organisation_assignable_roles;
            }
        } else if (isTechSupportOrganisation(orgId)) {
            return tech_organisation_assignable_roles;
        } else {
            return organisation_assignable_roles;
        }
    }

    public List<AnnualSubmission> getAnnualSubmissions(Integer organisationId) {
        return annualSubmissionService.getAnnualSubmissions(organisationId);
    }

    public Set<Team> getTeams() {
        User user = userService.currentUser();
        Set<Organisation> organisations = user.getOrganisations();
        return teamRepository.findByOrganisationIn(organisations);
    }


    public Set<Team> getTeams(Integer organisationId) {
        Organisation organisation = findOne(organisationId);
        return organisation.getManagedTeams();
    }

    public void createTeam(Integer organisationId, Team team) {
        Organisation organisation = find(organisationId);
        validateTeam(team, organisation);
        organisation.addManagedTeam(team);
        organisationRepository.save(organisation);
    }

    public void updateTeam(Integer organisationId, Integer teamId, Team team) {
        Organisation organisation = find(organisationId);
        validateTeam(team, organisation);
        team.setOrganisation(organisation);
        team.setId(teamId);
        teamRepository.save(team);
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

    public void deleteTeam(Integer organisationId, Integer teamId) {
        Organisation organisation = find(organisationId);

        Team teamToDelete = organisation.getManagedTeams().stream().filter(t -> t.getId().equals(teamId)).findFirst().orElse(null);
        if (teamToDelete != null) {
            Organisation orgUsingTeam = organisationRepository.findFirstByTeam(teamToDelete);
            if (orgUsingTeam != null) {
                throw new ValidationException("Unable to delete team. At least one organisation is assigned to this team.");
            }

            auditService.auditCurrentUserActivity(String.format("Team %s deleted from organisation %s", teamToDelete.getName(), organisation.getName()));

            organisation.getManagedTeams().remove(teamToDelete);
            organisationRepository.save(organisation);
        }
    }

    public ProgrammeRequestedAndPaidRecord updatePlannedUnits(Integer organisationId, Integer programmeId, Integer tenureExtId, Integer plannedUnits) {
        OrganisationProgramme orgProgramme = organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId)).orElse(null);

        if (orgProgramme == null) {
            throw new ValidationException("Unable to find record for requested Programme and Organisation");
        }

        Set<StrategicPlannedUnitsForTenure> plannedUnitsList = orgProgramme.getPlannedUnits();
        if (plannedUnitsList == null) {
            orgProgramme.setPlannedUnits(new HashSet<>());
        }

        Optional<StrategicPlannedUnitsForTenure> first = orgProgramme.getPlannedUnits().stream().filter(
                p -> p.getProgrammeId().equals(programmeId) &&
                        p.getOrgId().equals(organisationId) &&
                        p.getTenureType().equals(tenureExtId)).findFirst();

        if (first.isPresent()) {
            first.get().setUnitsPlanned(plannedUnits);
        } else {
            orgProgramme.getPlannedUnits().add(new StrategicPlannedUnitsForTenure(programmeId, organisationId, tenureExtId, plannedUnits));
        }

        organisationProgrammeRepository.saveAndFlush(orgProgramme);
        return getRequestedAndPaidRecord(programmeId, organisationId);

    }

    public ProgrammeRequestedAndPaidRecord deletePlannedUnits(Integer organisationId, Integer programmeId, Integer tenureExtId) {
        OrganisationProgramme orgProgramme = organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId)).orElse(null);

        if (orgProgramme == null) {
            throw new ValidationException("Unable to find record for requested Programme and Organisation");
        }

        boolean removed = orgProgramme.getPlannedUnits().removeIf(
                p -> p.getProgrammeId().equals(programmeId) &&
                        p.getOrgId().equals(organisationId) &&
                        p.getTenureType().equals(tenureExtId));

        if (!removed) {
            throw new ValidationException("Unable to find record for requested Programme and Organisation");
        }

        organisationProgrammeRepository.saveAndFlush(orgProgramme);
        return getRequestedAndPaidRecord(programmeId, organisationId);
    }

    public Set<OrganisationTeam> getManagedOrganisationAndTeams() {
        User user = userService.currentUser();
        Set<Organisation> myManagingOrgs = user.getRoles().stream().map(Role::getOrganisation).filter(Organisation::isManagingOrganisation).collect(Collectors.toSet());

        Set<OrganisationTeam> teams = new HashSet<>();
        for (Organisation myManagingOrg : myManagingOrgs) {
            teams.add(new OrganisationTeam(myManagingOrg.getId(), myManagingOrg.getName()));
            myManagingOrg = organisationRepository.getOne(myManagingOrg.getId());
            Set<Team> managedTeams = myManagingOrg.getManagedTeams();
            for (Team managedTeam : managedTeams) {
                teams.add(new OrganisationTeam(myManagingOrg.getId(), myManagingOrg.getName(), managedTeam.getId(), managedTeam.getName()));
            }
        }

        return teams;

    }

    public Map<Integer, String> getAssignableOrganisationTypes() {
        Map<Integer, String> map = new HashMap<>();

        map.put(OrganisationType.BOROUGH.id(), OrganisationType.BOROUGH.summary());
        map.put(OrganisationType.OTHER.id(), OrganisationType.OTHER.summary());
        map.put(OrganisationType.PROVIDER.id(), OrganisationType.PROVIDER.summary());
        map.put(OrganisationType.LEARNING_PROVIDER.id(), OrganisationType.LEARNING_PROVIDER.summary());

        User currentUser = userService.currentUser();
        if (currentUser != null && currentUser.isOpsAdmin()) {
            map.put(OrganisationType.MANAGING_ORGANISATION.id(), OrganisationType.MANAGING_ORGANISATION.summary());
            map.put(OrganisationType.TECHNICAL_SUPPORT.id(), OrganisationType.TECHNICAL_SUPPORT.summary());
        }

        return map;
    }


    public void changeStatus(Integer organisationId, OrganisationStatus status, OrganisationChangeStatusReason reason, String details, Integer duplicateOrgId) {
        Organisation organisation = find(organisationId);

        preStateTransitionAction(organisation, status, reason, details, duplicateOrgId);
        OrganisationStatus previousStatus = organisation.getStatus();
        organisation.changeStatus(status, userService.loadCurrentUser().getUsername(), environment.now());
        organisation.setChangeStatusReason(reason);
        organisation.setChangeStatusReasonDetails(details);
        organisationRepository.save(organisation);
        postStateTransitionAction(organisation, previousStatus);
    }

    private void preStateTransitionAction(Organisation organisation, OrganisationStatus status, OrganisationChangeStatusReason reason, String details, Integer duplicateOrgId) {
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
        String auditMessage = String.format("Organisation %s with ID %d was moved to status of %s.", organisation.getName(), organisation.getId(), organisation.getStatus());
        if (organisation.getChangeStatusReasonDetails() != null) {
            auditMessage += String.format(" Reason: %s", organisation.getChangeStatusReasonDetails());
        }
        auditService.auditCurrentUserActivity(auditMessage);

        switch (organisation.getStatus()) {
            case Rejected:
                notificationService.createNotification(OrganisationRejection, organisation, Collections.singletonMap("managingOrgId", organisation.getManagingOrganisation().getId()));
                for (User user : organisation.getUserEntities()) {
                    user.getRoles().remove(user.getRole(organisation));
                }
                break;
            case Inactive:
                notificationService.createNotification(OrganisationInactivation, organisation, Collections.singletonMap("managingOrgId", organisation.getManagingOrganisation().getId()));
                break;
            case Approved:
                if (previousStatus == Inactive) {
                    notificationService.createNotification(OrganisationReapproval, organisation, Collections.singletonMap("managingOrgId", organisation.getManagingOrganisation().getId()));
                } else {
                    for (String orgAdmin : organisation.getUsernames(ORG_ADMIN)) {
                        approve(organisation, orgAdmin);
                    }

                    notificationService.createNotification(OrganisationApproval, organisation, Collections.singletonMap("managingOrgId", organisation.getManagingOrganisation().getId()));
                }
                break;
        }
    }


    private void validateTeam(Team team, Organisation organisation) {
        Set<Team> existingTeams = organisation.getManagedTeams();

        if (!permissionService.currentUserHasPermissionForOrganisation(TEAM_EDIT.getPermissionKey(), organisation.getId())) {
            throw new ValidationException("You have no permission to edit this organisation");
        }

        if (existingTeams != null &&
                existingTeams.size() > 0 &&
                existingTeams.stream().anyMatch(t -> t.getName().equalsIgnoreCase(team.getName()) && !t.getId().equals(team.getId()))) {
            throw new ValidationException("Team name must be unique");
        }
    }
}
