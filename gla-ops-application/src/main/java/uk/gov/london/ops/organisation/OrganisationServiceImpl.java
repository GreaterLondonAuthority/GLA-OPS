/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.annualsubmission.AnnualSubmission;
import uk.gov.london.ops.annualsubmission.AnnualSubmissionService;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.contracts.*;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileCategory;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.enums.ContractWorkflowType;
import uk.gov.london.ops.framework.enums.OrganisationContractStatus;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.dto.OrganisationDTOMapper;
import uk.gov.london.ops.organisation.dto.OrganisationUserDTO;
import uk.gov.london.ops.organisation.implementation.repository.OrganisationContractRepository;
import uk.gov.london.ops.organisation.implementation.repository.OrganisationGroupRepository;
import uk.gov.london.ops.organisation.implementation.repository.OrganisationRepository;
import uk.gov.london.ops.organisation.implementation.repository.OrganisationSummaryRepository;
import uk.gov.london.ops.organisation.model.*;
import uk.gov.london.ops.organisation.template.OrganisationBlockQuestionTemplate;
import uk.gov.london.ops.organisation.template.OrganisationTemplate;
import uk.gov.london.ops.organisation.template.OrganisationTemplateService;
import uk.gov.london.ops.permission.PermissionServiceImpl;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.service.DataAccessControlService;
import uk.gov.london.ops.user.UserFinanceThresholdService;
import uk.gov.london.ops.user.UserMapper;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;
import uk.gov.london.ops.user.domain.UserModel;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.apache.commons.io.FileUtils.ONE_MB;
import static uk.gov.london.common.GlaUtils.parseInt;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.ops.notification.NotificationType.*;
import static uk.gov.london.ops.organisation.OrganisationStatus.Inactive;
import static uk.gov.london.ops.organisation.OrganisationStatus.Rejected;
import static uk.gov.london.ops.organisation.model.OrganisationAction.EDIT;
import static uk.gov.london.ops.permission.PermissionType.*;

/**
 * REST Web Service endpoint for Organisation data.
 * <p>
 * Created by sleach on 17/08/2016.
 */
@Transactional
@Service
public class OrganisationServiceImpl implements OrganisationService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    OrganisationGroupRepository organisationGroupRepository;

    @Autowired
    OrganisationDTOMapper organisationDTOMapper;

    @Autowired
    OrganisationTemplateService organisationTemplateService;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationSummaryRepository organisationSummaryRepository;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserServiceImpl userService;

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
    ContractActionMap contractActionMap;

    @Autowired
    OrganisationContractRepository organisationContractRepository;

    @Autowired
    DataAccessControlService dataAccessControlService;

    @Autowired
    AnnualSubmissionService annualSubmissionService;

    @Autowired
    PermissionServiceImpl permissionService;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    FileService fileService;

    @Value("${user.watch.org.by.default}")
    boolean userWatchOrgByDefault = true;

    /**
     * Returns a list of all defined organisations.
     */
    public List<OrganisationEntity> findAll(List<Integer> entityTypes) {
        if (userService.currentUser().isOrgAdmin()) {
            if (!Collections.singletonList(OrganisationType.MANAGING_ORGANISATION.getId()).equals(entityTypes)) {
                throw new ValidationException("cannot filter entity types other than managing organisation");
            }

            return organisationRepository.findAllByEntityType(OrganisationType.MANAGING_ORGANISATION.getId());
        } else {
            return organisationRepository.findAll();
        }
    }

    public List<OrganisationEntity> findAll() {
        return organisationRepository.findAll();
    }

    public List<OrganisationSummary> findAllByType(OrganisationType type) {
        return organisationSummaryRepository.getOrganisationSummariesByEntityType(type.getId());
    }

    public Page<OrganisationSummary> getSummaries(String orgIdOrName,
            String sapVendorId,
            List<Integer> entityTypes,
            List<OrganisationStatus> orgStatuses,
            List<OrganisationTeam> teams,
            Pageable pageable) {
        if (entityTypes == null || entityTypes.isEmpty()) {
            entityTypes = Stream.of(OrganisationType.values()).map(OrganisationType::getId).collect(Collectors.toList());
        }
        entityTypes.removeIf(it -> it.equals(OrganisationType.TEAM.getId()));
        UserEntity currentUser = userService.loadCurrentUser();
        return organisationSummaryRepository
                .findAll(currentUser, orgIdOrName, sapVendorId, entityTypes, orgStatuses, teams, pageable);
    }

    public OrganisationEntity findOne(Integer id) {
        return organisationRepository.findById(id).orElse(null);
    }

    public OrganisationEntity findByName(String name) {
        return organisationRepository.findFirstByNameIgnoreCase(name);
    }

    public OrganisationEntity findByProviderNumber(String name) {
        return organisationRepository.findFirstByProviderNumber(name);
    }

    public Set<OrganisationEntity> findAllByProviderNumber(String name) {
        return organisationRepository.findAllByProviderNumber(name);
    }

    public List<OrganisationEntity> findAllByRegistrationKeyNull() {
        return organisationRepository.findAllByRegistrationKeyNull();
    }

    public String getOrganisationName(Integer orgId) {
        String orgName = null;
        if (orgId != null) {
            OrganisationEntity org = findOne(orgId);
            if (org != null) {
                orgName = org.getName();
            }
        }
        return orgName;
    }

    public boolean organisationExistsById(Integer id) {
        return organisationRepository.existsById(id);
    }

    public OrganisationEntity save(OrganisationEntity org) {
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

    public List<OrganisationEntity> saveAll(List<OrganisationEntity> orgs) {
        return organisationRepository.saveAll(orgs);
    }

    public OrganisationEntity saveAndFlushOrganisation(OrganisationEntity org) {
        return organisationRepository.saveAndFlush(org);
    }

    public void deleteAllOrganisations(List<OrganisationEntity> orgList) {
        organisationRepository.deleteAll(orgList);
    }

    /**
     * Returns details of a single organisation, given the organisation's ID. if the user has authorization
     */
    public OrganisationEntity find(Integer id) {
        final OrganisationEntity organisation = findOne(id);
        if (organisation == null) {
            throw new NotFoundException();
        }
        final UserEntity user = userService.loadCurrentUser();

        dataAccessControlService.checkAccess(user, organisation);

        setOrganisationToUsers(user, organisation);

        return organisation;
    }

    /**
     * Returns details of a list of organisations, given the organisation's IDs. filtered by the user's authorization
     */
    public List<OrganisationEntity> find(final Collection<Integer> idList) {
        final List<OrganisationEntity> organisation = organisationRepository
                .findAllById(idList);
        final UserEntity user = userService.loadCurrentUser();

        return organisation.stream()
                .filter(o -> dataAccessControlService.hasAccess(user, o))
                .collect(Collectors.toList());
    }

    public OrganisationEntity getEnrichedOrganisation(Integer id) {
        OrganisationEntity organisation = find(id);

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
            UserEntity glaContact = userService.find(organisation.getContactEmail());

            String glaContactEmail = glaContact.getUsername();
            boolean glaContactInOrg = getUsersForOrganisation(organisation.getManagingOrganisationId())
                    .stream()
                    .anyMatch(user -> Objects.equals(glaContactEmail, user.getUsername()));

            if (glaContactInOrg) {
                organisation.setGlaContactFullName(glaContact.getFullName());
            }

        }

        populateSapIdUsedInProject(organisation);
        organisation.setContracts(getContracts(id));
        organisation.setAnnualSubmissions(getAnnualSubmissions(id));
        calculateAllowedActions(organisation);
        return organisation;
    }

    private void populateSapIdUsedInProject(OrganisationEntity organisation) {
        Set<String> sapIdsUsedInProject = projectService.getAllSapIdUsedForOrganisation(organisation.getId());
        organisation.getSapIds().forEach(sapId -> {
            if (sapIdsUsedInProject != null && sapIdsUsedInProject.contains(sapId.getSapId())) {
                sapId.setUsedInProject(true);
            }
        });
    }

    private void calculateAllowedActions(OrganisationEntity organisation) {
        if (currentUserCanEdit(organisation)) {
            organisation.getAllowedActions().add(EDIT);
        }
    }

    public boolean currentUserCanEdit(OrganisationEntity organisation) {
        UserEntity currentUser = userService.currentUser();
        return !organisation.isRejected() && !organisation.isInactive()
                && (permissionService.userHasPermissionForOrganisation(currentUser, ORG_EDIT_DETAILS, organisation.getId())
                || (
                organisation.getManagingOrganisation() != null
                        && currentUser.getRole(organisation.getManagingOrganisation()) != null
                        && currentUser.getRole(organisation.getManagingOrganisation()).getName().equals(GLA_ORG_ADMIN)));
    }

    private void setOrganisationToUsers(UserEntity user, OrganisationEntity organisation) {
        if (user.isOpsAdmin() || permissionService
                .currentUserHasPermissionForOrganisation(ORG_VIEW_USERS, organisation.getId())) {
            organisation.setUsers(userMapper.mapToModel(organisation.getUserEntities()));
        }
    }

    public OrganisationEntity findByOrgCode(String orgCode) {
        return findByOrgCode(orgCode, false);
    }

    public OrganisationEntity findByOrgCode(String orgCode, boolean searchOrgIds) {
        Integer orgId = GlaUtils.parseInt(orgCode);

        OrganisationEntity organisation = null;

        if (orgId != null) {
            OrganisationEntity orgLookedUpById = findOne(orgId);
            if (orgLookedUpById != null && (searchOrgIds || featureStatus.isEnabled(Feature.OrgIdLookup)
                    || dataAccessControlService.currentUserHasAccess(orgLookedUpById))) {
                organisation = orgLookedUpById;
            }
        }

        if (organisation == null) {
            organisation = organisationRepository.findFirstByProviderNumber(orgCode);
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

        OrganisationEntity organisationFromDTO = organisationDTOMapper.getOrganisationUserDTOFromOrg(organisation);

        OrganisationEntity newOrg = create(organisationFromDTO);

        log.debug(name + " Registration is null? : " + organisation.getUserRegistration());

        if (organisation.getUserRegistration() != null) {
            log.debug(name + " creating user ");

            organisation.getUserRegistration().setOrgCode(String.valueOf(newOrg.getRegistrationKey()));
            log.debug(name + " assigned reg key, attempting to register user, key: " + organisation.getUserRegistration());
            //new user should not receive notification telling them to approve themselves
            UserEntity newUser = userService.register(organisation.getUserRegistration());
            log.debug(name + " registered user success " + newUser.getUsername());
            newOrg.setCreatedBy(newUser.getUsername());
            log.debug(name + " created by set " + newOrg.getCreatedBy());

            OrganisationEntity updated = save(newOrg);
            log.debug(name + " created by saved " + updated.getCreatedBy());

            if (updated.getCreatedBy() == null) {
                throw new ValidationException("User registration failed for org: " + name);
            }

        } else if (userService.currentUser() == null) {
            log.debug(name + " No current user throwing exception");

            throw new ValidationException(
                    "Unable to create a new organisation profile as Organisation Admin Information is null");
        } else {
            log.debug(name + " Has a current user " + userService.currentUsername() + " no need to assign user?");
        }

        return organisationDTOMapper.getOrganisationUserDTOFromOrg(newOrg);
    }

    boolean checkOrgCanBeCreatedByAnonUser(OrganisationEntity organisation) {
        return OrganisationType.MANAGING_ORGANISATION.getId() != organisation.getEntityType()
                && OrganisationType.TEAM.getId() != organisation.getEntityType()
                && OrganisationType.TECHNICAL_SUPPORT.getId() != organisation.getEntityType();
    }

    public OrganisationEntity create(OrganisationEntity organisation) {
        OrganisationEntity managingOrganisation =
                organisationRepository.findById(organisation.getManagingOrganisation().getId()).orElse(null);
        if (managingOrganisation == null) {
            organisation.setManagingOrganisation(organisationRepository.getOne(Organisation.GLA_HNL_ORG_ID));
        }

        if (managingOrganisation.getRegistrationAllowed() != true) {
            throw new ValidationException("Selected managing org does not allow registration.");
        }

        organisation.populateRegistrationKey();
        validateRegistrationKey(organisation);

        organisation = save(organisation);

        UserEntity currentUser = userService.loadCurrentUser();
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
        } else {
            log.debug(organisation.getName() + " Create Organisation gla by : " + userService.currentUsername());
            if (organisation.isManaging()) {
                // managing orgs can only be created by OPS ADMIN through UI but request body could
                // be manipulated via API so double check user role here
                if (!currentUser.isOpsAdmin()) {
                    throw new ValidationException("Only OPS ADMIN users can create managing organisations");
                }
                organisation.changeStatus(OrganisationStatus.Approved, userService.currentUser().getUsername(), environment.now());
                currentUser.addApprovedRole(GLA_ORG_ADMIN, organisation);
            } else {
                currentUser.addUnapprovedRole(ORG_ADMIN, organisation);
            }
            userService.saveUser(currentUser);
            notificationService.subscribe(currentUser.getUsername(), EntityType.organisation, organisation.getId());
        }

        organisation = save(organisation);
        log.debug(organisation.getName() + " Update org final create check " + organisation.getCreatedBy());

        return organisation;
    }

    public OrganisationEntity update(OrganisationEntity updated) {
        OrganisationEntity current = findOne(updated.getId());

        validateEdits(updated, current);
        validateTemplateFields(updated);

        OrganisationEntity existing = findOne(updated.getId());
        updated.setUserEntities(existing.getUserEntities());
        updated.setContractEntities(existing.getContractEntities());
        updated.setManagedTeams(existing.getManagedTeams());

        auditService.auditCurrentUserActivity("Organisation edited: " + updated.getId());

        Integer updatedManagingOrgId =
                updated.getManagingOrganisation() != null ? updated.getManagingOrganisation().getId() : null;
        if (updatedManagingOrgId != null) {
            OrganisationEntity managingOrganisation = findOne(updatedManagingOrgId);
            if (!(current.getManagingOrganisation().getId().equals(updatedManagingOrgId))
                    && managingOrganisation.getRegistrationAllowed() != true) {
                throw new ValidationException("Selected managing org does not allow registration.");
            }
        }

        if (nameOrProviderNumberChanged(updated, current)) {
            auditService.auditCurrentUserActivity("Organisation name and/or provider number changed: " + updated.getId());
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

    void validateEdits(OrganisationEntity updated, OrganisationEntity current) {
        if (!currentUserCanEdit(updated)) {
            throw new ForbiddenAccessException("User does not have permission to edit organisation " + updated.getId());
        }

        if (nameOrProviderNumberChanged(updated, current)
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_NAME, updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit organisation name or provider number for "
                    + updated.getId());
        }

        validateSapIdsEdits(updated, current);

        if (!Objects.equals(updated.getEntityType(), current.getEntityType())
                && (!getOrganisationTypes().containsKey(updated.getEntityType())
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
    }

    //TODO loop through fields dynamically with reflection
    void validateTemplateFields(OrganisationEntity updated) {
        OrganisationTemplate orgTemplate = organisationTemplateService.getOrganisationTemplate(updated.getType());
        validateTemplateField(orgTemplate, updated, "isLearningProvider");
    }

    //TODO should use generic approach (reflection) eventually
    private void validateTemplateField(OrganisationTemplate orgTemplate, OrganisationEntity updated, String fieldName) {
        OrganisationBlockQuestionTemplate question = orgTemplate.getQuestion(fieldName);
        // check that the orgTemplate is real, as otherwise can wipe correctly set values.
        if (orgTemplate.getId() != null && (question == null && updated.getIsLearningProvider() != null)) {
            updated.setIsLearningProvider(null);
            log.warn("Attempt to answer learner provider question when it is not defined in organisation template");
        }
    }

    private void validateSapIdsEdits(OrganisationEntity updated, OrganisationEntity current) {
        populateMissingSapIdsData(updated);
        if (sapIdsChanged(updated, current)
                && !permissionService.currentUserHasPermissionForOrganisation(ORG_EDIT_VENDOR_SAP_ID, updated.getId())) {
            throw new ForbiddenAccessException("User does not have permission to edit SAP vendor ID for " + updated.getId());
        }
    }

    private void populateMissingSapIdsData(OrganisationEntity updated) {
        for (SapIdEntity sapId : updated.getSapIds()) {
            sapId.setOrganisationId(updated.getId());
            if (sapId.getCreatedOn() == null) {
                sapId.setCreatedOn(environment.now());
            }
        }
    }

    private boolean sapIdsChanged(OrganisationEntity updated, OrganisationEntity current) {
        return !CollectionUtils.isEqualCollection(updated.getSapIds(), current.getSapIds());
    }

    private void validateRegistrationKey(OrganisationEntity updated) {
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

        Set<OrganisationEntity> orgsByProviderNumber = findAllByProviderNumber(updated.getRegistrationKey());
        if (orgsByProviderNumber.size() > 0 && (orgsByProviderNumber.size() > 1 || !orgsByProviderNumber.iterator().next()
                .equals(updated))) {
            // registration key cannot match any provider code apart from own organisation
            throw new ValidationException("Invalid registration key");
        }
    }

    public Integer countOccuranceOfUkprn(Integer ukprn) {
        OrganisationStatus[] organisationStatuses = {Inactive, Rejected};
        return organisationRepository.countByUkprnAndStatusNotIn(ukprn, organisationStatuses);
    }

    public void addUserToOrganisation(Integer id, String username) {
        addUserToOrganisation(id, username, userWatchOrgByDefault);
    }

    public void addUserToOrganisation(Integer id, String username, boolean subscribe) {
        OrganisationEntity organisation = find(id);

        UserEntity user = userService.find(username);
        user.addApprovedRole(Role.getDefaultForOrganisation(organisation), organisation);
        userService.saveUser(user);

        if (subscribe) {
            notificationService.subscribe(username, EntityType.organisation, organisation.getId());
        }

        auditService.auditCurrentUserActivity(String.format("User %s was added to Organisation %d.", user.getUsername(), id));
    }

    public void linkUserToOrganisation(String orgCode, String username) {
        OrganisationEntity organisation = findByOrgCode(orgCode);

        UserEntity user = userService.find(username);
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

    private boolean isTestOrganisation(OrganisationEntity org) {
        return org.getName().startsWith("A Test");
    }

    public void removeUserFromOrganisation(Integer id, String username, String role) {
        UserEntity currentUser = userService.loadCurrentUser();
        UserEntity userToBeRemoved = userService.find(username);

        OrganisationEntity organisation = find(id);
        Set<Role> roles = userToBeRemoved.getRolesInOrganisation(organisation);
        if (!currentUser.isOpsAdmin() && !(currentUser.getOrganisations().contains(organisation) && !roles.isEmpty())
                  && !canEditProviderOrgUser(organisation, currentUser)) {
            throw new ValidationException("cannot remove a user from an organisation they don't belong to");
        }

        if (role == null) {
            userToBeRemoved.getRoles().removeAll(roles);
            processLastRoleRemoval(userToBeRemoved, organisation);
        } else {
            userToBeRemoved.getRoles().removeIf(r -> r.getName().equals(role) && r.getOrganisation().getId().equals(id));
            processSingleRoleRemoval(userToBeRemoved, organisation, role);
            // only teams at the moment but could be for all users
            if (organisation.getEntityType().equals(OrganisationType.TEAM.getId())) {
                notificationService.createNotificationForUser(OrganisationRemoval, userToBeRemoved, Collections.singletonMap(
                        "organisation", organisation), userToBeRemoved.getUsername());
            }
        }

        if (Boolean.TRUE.equals(roles.stream().anyMatch(Role::isPrimaryOrganisationForUser))) {
            userService.assignDefaultPrimaryOrganisation(userToBeRemoved);
        }
    }

    boolean canEditProviderOrgUser(OrganisationEntity org, UserEntity currentUser) {
        return permissionService.currentUserHasPermission(USER_EDIT_PROVIDER_ROLE) &&
                currentUser.getOrganisations().stream()
                   .filter(o ->  o.isManaging() && o.getId().equals(org.getManagingOrganisationId()))
                   .count() > 0;
    }

    private void processSingleRoleRemoval(UserEntity userToBeRemoved, OrganisationEntity organisation, String role) {
        auditService.auditCurrentUserActivity(String.format("User %s had role %s removed from Organisation %d.",
                userToBeRemoved.getUsername(), role, organisation.getId()));
        userService.saveUser(userToBeRemoved);
    }

    private void processLastRoleRemoval(UserEntity userToBeRemoved, OrganisationEntity organisation) {
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

    public void approve(Integer organisationId, String username, String role, Boolean signatory) {
        OrganisationEntity organisation = find(organisationId);
        approve(organisation, username, role, signatory);
    }

    public void approve(OrganisationEntity organisation, String username, String role) {
        approve(organisation, username, role, false);
    }
    public void approve(OrganisationEntity organisation, String username, String newRole, Boolean signatory) {
        if (!organisation.isApproved()) {
            throw new ValidationException("This users organisation is pending approval. "
                    + "Approve the organisation to approve this user");
        }

        UserEntity user = userService.find(username);
        Role role = user.getRole(organisation);

        if (newRole != null && !newRole.isEmpty()) {
            role.setName(newRole);
        }

        role.approve();
        role.setAuthorisedSignatory(signatory);
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
        OrganisationEntity organisation = find(organisationId);

        UserEntity user = userService.find(username);
        user.getRole(organisation).unapprove();
        userService.saveUser(user);

        notificationService.unsubscribe(username, EntityType.organisation, organisationId);

        auditService.auditCurrentUserActivity(String.format("User %s was unapproved from Organisation %d.", user.getUsername(),
                organisationId));
    }

    /**
     * Returns true if the Organisation's name or provider number has changed from what is in the database.
     */
    public boolean nameOrProviderNumberChanged(OrganisationEntity updated, OrganisationEntity current) {
        if (!GlaUtils.nullSafeEquals(current.getName(), updated.getName())) {
            return true;
        }
        return !GlaUtils.nullSafeEquals(current.getProviderNumber(), updated.getProviderNumber());
    }

    public OrganisationEntity getOrganisationForProject(Project project) {
        if (project.getOrganisationGroupId() != null) {
            OrganisationGroup organisationGroup =
                    organisationGroupRepository.findById(project.getOrganisationGroupId()).orElse(null);
            if (organisationGroup != null && organisationGroup.getLeadOrganisation() != null) {
                return organisationGroup.getLeadOrganisation();
            }
        }

        return project.getOrganisation();
    }

    Set<ContractSummary> getContracts(Integer id) {
        OrganisationEntity organisation = find(id);

        Set<ContractSummary> contracts = new HashSet<>();

        List<Project> projects = projectService.findAllByOrganisationAndStatusName(organisation, ProjectStatus.Active.name());
        projects.addAll(projectService.findAllByOrganisationAndStatusName(organisation, ProjectStatus.Assess.name()));

        for (Project project : projects) {
            ContractModel contractModel = null;
            if (project.getTemplate().getContractId() != null) {
                contractModel = contractService.find(project.getTemplate().getContractId());
            }
            if (contractModel != null && doDisplayContract(project, contractModel.getContractWorkflowType())) {
                String orgGroupType = getProjectOrgGroupType(project);

                OrganisationContract orgContract = getOrgContractForContract(contractModel, organisation, orgGroupType);

                if (orgContract == null) {
                    contracts.add(generateNewOrgContract(project, organisation, contractModel));
                } else {
                   contracts.add(getExistingOrgContract(project, organisation, orgContract));
                   contracts.addAll(getAnyOrgContractVariations(project, organisation, orgContract));
                }
            }
        }
        return contracts;
    }

    private Set<ContractSummary> getAnyOrgContractVariations(Project project, OrganisationEntity organisation,
                                                             OrganisationContract contract) {

        String orgGroupType = contract.getOrgGroupType() != null ? contract.getOrgGroupType().name() : null;
        Set<ContractSummary> variations =  new HashSet<>();

        enrichOrganisationContracts(organisation);
        Set<OrganisationContract> contractVariations =
                organisation.getContractEntities().stream()
                        .filter(oc -> oc.isVariation())
                        .filter(oc -> oc.getContract().getId().equals(contract.getContract().getId()))
                        .filter(oc -> oc.getContract().getContractWorkflowType()
                                .equals(ContractWorkflowType.CONTRACT_OFFER_AND_ACCEPTANCE))
                        .collect(Collectors.toSet());

        contractVariations.forEach(v -> {
            List<ContractActionDetails> allowedActions =
                    getAllowedContractActions(organisation, project, v.getContract(), v.getStatus(),true);

              variations.add(new ContractSummary(v.getId(), v.getContract().getId(), v.getContract().getName(),
                    v.getStatus(), orgGroupType, v.getContract().getContractWorkflowType(), allowedActions,
                    v.getAcceptedOn(), v.getAcceptedBy(), userService.getUserFullName(v.getAcceptedBy()),
                    v.getAcceptedByJobTitle(), v.getVariationName(), v.getVariationReason(), true));
        });

        return variations;
    }

    public void enrichOrganisationContracts(OrganisationEntity organisation) {
        List<OrganisationContract> contractEntities = organisation.getContractEntities();
        for (OrganisationContract organisationContract : contractEntities) {
            ContractModel contractModel = contractService.findById(organisationContract.getContractId());
            if (contractModel != null) {
                organisationContract.setContract(contractModel);
            }
        }
    }


    private ContractSummary getExistingOrgContract(Project project, OrganisationEntity organisation,
                                                   OrganisationContract contract) {
        String orgGroupType = contract.getOrgGroupType() != null ? contract.getOrgGroupType().name() : null;

        List<ContractActionDetails> allowedActions =
                        getAllowedContractActions(organisation, project, contract.getContract(), contract.getStatus(),
                                contract.isVariation());

        return new ContractSummary(contract.getId(), contract.getContract().getId(), contract.getContract().getName(),
                contract.getStatus(), orgGroupType, contract.getContract().getContractWorkflowType(), allowedActions,
                contract.getAcceptedOn(), contract.getAcceptedBy(),
                userService.getUserFullName(contract.getAcceptedBy()), contract.getAcceptedByJobTitle(), null, null,
                contract.isVariation());

    }

    private ContractSummary generateNewOrgContract(Project project, OrganisationEntity organisation, ContractModel contractModel) {
        String orgGroupType = getProjectOrgGroupType(project);
        ContractWorkflowType contractWorkflowType = contractModel.getContractWorkflowType();
        OrganisationContractStatus initialStatus =
                contractWorkflowType.equals(ContractWorkflowType.SIGNED_TO_AUTHORISE_PAYMENTS)
                        ? OrganisationContractStatus.Blank : OrganisationContractStatus.PendingOffer;

        List<ContractActionDetails> allowedActions =
                getAllowedContractActions(organisation, project, contractModel, initialStatus,false);

        return new ContractSummary(null, contractModel.getId(), contractModel.getName(), initialStatus, orgGroupType,
                contractWorkflowType, allowedActions, null, null, null, null, null, null, false);

    }

    private List<ContractActionDetails> getAllowedContractActions(OrganisationEntity organisation, Project project,
                                                                  ContractModel contractModel, OrganisationContractStatus status,
                                                                  boolean variation) {

        List<ContractActionDetails> allowedActions = contractActionMap.getAllowedActions(contractModel.getContractWorkflowType(),
                variation, status, project.getStatusType(), userService.currentUser(), organisation.getId(),
                organisation.getManagingOrganisationId());

        return  excludeSpecificContractActions(organisation, contractModel, allowedActions);
    }

    private List<ContractActionDetails> excludeSpecificContractActions(OrganisationEntity organisation, ContractModel contractModel,
                                                                       List<ContractActionDetails> allowedActions) {
        // Only one in-flight add variation per contract
        allowedActions.removeIf(a -> ("Add Variation".equals(a.getText()) && hasInFlightVariation(organisation, contractModel)));

       return  allowedActions;
    }

    private boolean hasInFlightVariation(OrganisationEntity organisation, ContractModel contractModel) {
       return organisation.getContractEntities().stream()
               .filter(oc -> oc.isVariation())
               .filter(oc -> oc.getContractId().equals(contractModel.getId()))
               .filter(oc -> !OrganisationContractStatus.Accepted.equals(oc.getStatus()))
               .count() > 0;
    }
    private String getProjectOrgGroupType(Project project) {
        if (project.getOrganisationGroupId() != null) {
            OrganisationGroupType type = organisationGroupRepository.getOne(project.getOrganisationGroupId()).getType();
            return type != null ? type.name() : null;
        }
        return null;
    }

    private boolean doDisplayContract(Project project, ContractWorkflowType contractWorkflow) {
        boolean isOfferWorkflowAndInAssess =
                ContractWorkflowType.CONTRACT_OFFER_AND_ACCEPTANCE.equals(contractWorkflow)
            && project.getStatusName().equals(ProjectStatus.Assess.name());
        boolean isProjectActive = project.getStatusName().equals(ProjectStatus.Active.name());
        return isOfferWorkflowAndInAssess || isProjectActive;
    }

    public OrganisationContract getOrgContractForContract(ContractModel contractModel,
                                                          OrganisationEntity organisation,
                                                          String orgGroupType) {
        OrganisationContract match = null;
        enrichOrganisationContracts(organisation);
        for (OrganisationContract organisationContract : organisation.getContractEntities()) {
            String orgContractGroupType = organisationContract.getOrgGroupType() != null
                    ? organisationContract.getOrgGroupType().name() : null;
            Boolean haveSameOrgGroupTypes =
                (orgContractGroupType == null && orgGroupType == null
                        || (orgContractGroupType != null && orgContractGroupType.equals(orgGroupType)));
            if (organisationContract.getContract().equals(contractModel) && haveSameOrgGroupTypes
                    && !organisationContract.isVariation()) {
                match = organisationContract;
                break;
            }
        }
        return match;
    }

    public void createContract(Integer id, ContractSummary model) {
        OrganisationEntity organisation = find(id);
        ContractModel contractModel = contractService.find(model.getContractId() );

        OrganisationContract entity = new OrganisationContract(contractModel, model.getStatus(), model.getOrgGroupType());
        entity.setCreatedBy(userService.currentUser().getUsername());
        entity.setCreatedOn(environment.now());
        entity.setVariationName(model.getVariationName());
        entity.setVariationReason(model.getVariationReason());
        organisation.getContractEntities().add(entity);

        save(organisation);

        auditService.auditCurrentUserActivity("contract '" + entity.getContract().getName()
                + "' status updated to " + model.getStatus() + " for organisation " + organisation.getName());
    }

    public OrganisationContract createContractVariation(Integer id, ContractSummary model) {

        if (!OrganisationContractStatus.Accepted.equals(model.getStatus())) {
            throw new ValidationException(String.format("Variation can only be created on accepted contracts"));
        }

        OrganisationEntity organisation = find(id);
        enrichOrganisationContracts(organisation);
        ContractModel contractModel = contractService.find(model.getContractId());
        OrganisationContract orgContract = getOrganisationContractVariation(organisation, contractModel.getId());

        if (orgContract == null) {
            orgContract = new OrganisationContract(contractModel, OrganisationContractStatus.PendingOffer,
                                                   model.getOrgGroupType());
            orgContract.setCreatedBy(userService.currentUser().getUsername());
            orgContract.setCreatedOn(environment.now());
            orgContract.setVariation(true);
            organisation.getContractEntities().add(orgContract);
            save(organisation);
            auditService.auditCurrentUserActivity("Created variation request for contract: "
                    + orgContract.getContract().getName() + ", organisation: " + organisation.getName());
         }

        return getOrganisationContractVariation(organisation, contractModel.getId());
    }

    public void acceptContract(Integer organisationId, ContractSummary model) {

      if (!OrganisationContractStatus.Accepted.equals(model.getStatus())) {
          throw new ValidationException(String.format("User %s is only allowed to accept contract / variation offered"
                  , userService.currentUser().getUsername()));
      }
        updateContract(organisationId, model);
    }

    OrganisationContract getOrganisationContractVariation(OrganisationEntity organisation, Integer contractId) {
        return   organisation.getContractEntities()
                .stream()
                .filter(oc -> oc.getContractId().equals(contractId))
                .filter(oc -> OrganisationContractStatus.PendingOffer.equals(oc.getStatus()))
                .filter(oc -> oc.isVariation())
                .findFirst()
                .orElse(null);
    }

    public OrganisationContract getContract(Integer orgId, Integer orgContractId) {
        OrganisationEntity organisation = find(orgId);
        enrichOrganisationContracts(organisation);
        OrganisationContract contract = organisation.getContractEntities()
            .stream()
            .filter(e -> e.getId().equals(orgContractId))
            .findFirst().orElseThrow(NotFoundException::new);
        final UserEntity user = userService.loadCurrentUser();
        dataAccessControlService.checkAccess(user, contract);
        return contract;
    }

    public void updateContract(Integer id, ContractSummary model) {
        OrganisationEntity organisation = find(id);
        enrichOrganisationContracts(organisation);
        OrganisationContract entity = organisation.getContractEntities()
                .stream()
                .filter(e -> e.getId().equals(model.getId()))
                .findFirst().get();

        boolean doStatusUpdate = !entity.getStatus().equals(model.getStatus());
        Set<AttachmentFile> existingFiles = entity.getContractFiles();
        Set<AttachmentFile> newFiles = model.getContractFiles();
        List<String> auditSummary = new ArrayList<>();

        if (doStatusUpdate) {
            entity.setStatus(model.getStatus());
            populateContractAcceptFields(entity, model);
            generateUserNotification(organisation, entity);
            auditSummary.add(getContractStatusUpdateAuditMessage(model.getStatus(), entity, model, organisation));
        }

        populateVariationFields(entity, model);

        Set<AttachmentFile> added = new HashSet<>(newFiles);
        added.removeAll(existingFiles);
        Set<AttachmentFile> removed = new HashSet<>(existingFiles);
        removed.removeAll(newFiles);

        String contractType = model.isVariation() ? "variation" : "contract";
        String contractName = model.isVariation() ? model.getVariationName() : entity.getContract().getName();

        if (!added.isEmpty()) {
            for (AttachmentFile file : added) {
                String summary = String.format("new file %s was added to %s '%s' "
                        + "for organisation %s", file.getFileName(), contractType, contractName, organisation.getName());
                auditSummary.add(summary);
            }
        }
        if (!removed.isEmpty()) {
            for (AttachmentFile file : removed) {
                String summary = String.format("file %s was removed"
                        + " from %s '%s' for organisation %s", file.getFileName(), contractType, contractName,
                        organisation.getName());
                auditSummary.add(summary);
            }
        }
        entity.setContractFiles(newFiles);

        entity.setModifiedBy(userService.currentUser().getUsername());
        entity.setModifiedOn(environment.now());

        save(organisation);

        for (String summary : auditSummary) {
            auditService.auditCurrentUserActivity(summary);
        }
    }

    private void populateContractAcceptFields(OrganisationContract orgContract, ContractSummary model) {
        if (OrganisationContractStatus.Accepted.equals(model.getStatus())) {
            orgContract.setAcceptedBy(userService.currentUsername());
            orgContract.setAcceptedOn(OffsetDateTime.now());
            orgContract.setAcceptedByJobTitle(model.getAcceptedByJobTitle());
        }
    }

    private void populateVariationFields(OrganisationContract orgContract, ContractSummary model) {
        if (model.isVariation()) {
            orgContract.setVariationName(model.getVariationName());
            orgContract.setVariationReason(model.getVariationReason());
        }
    }

    private void generateUserNotification(OrganisationEntity org, OrganisationContract orgContract) {
        if (OrganisationContractStatus.Offered.equals(orgContract.getStatus())) {
            Map<String, Object> model = new HashMap<String, Object>() {{
                put("organisationId", org.getId());
                put("organisation", org);
            }};

            List<Project> projects = getProjectsForOrgContract(org, orgContract);

            if (projects != null && projects.size() > 0 ) {
                String projectIds = null;
                String projectTitles = null;
                for (Project project : projects) {
                    if (projectIds == null) {
                        projectIds = project.getId().toString();
                        projectTitles = project.getTitle();
                    } else {
                        projectIds = projectIds.concat(", " + project.getId());
                        projectTitles = projectTitles.concat(", " + project.getTitle());
                    }
                }
                model.put("projectId", projectIds);
                model.put("projectTitle", projectTitles);

                notificationService.createEmailNotification(
                        orgContract.isVariation() ? OrganisationContractVariationOffer : OrganisationContractOffer,
                        org,
                        model);
            }
        }
    }

    List<Project> getProjectsForOrgContract(OrganisationEntity org, OrganisationContract orgContract) {
        return projectService.findAllByOrganisationAndStatusName(org, ProjectStatus.Assess.name()).stream()
                .filter(p -> p.getTemplate().getContractId() != null
                            && p.getTemplate().getContractId().equals(orgContract.getContract().getId())
                            && doDisplayContract(p,orgContract.getContract().getContractWorkflowType())
                            && !Project.Recommendation.RecommendRejection.equals(p.getRecommendation()))
                .collect(Collectors.toList());
    }

    private String getContractStatusUpdateAuditMessage(OrganisationContractStatus status,
                                                       OrganisationContract contract,
                                                       ContractSummary model,
                                                       Organisation organisation) {

        String contractType = model.isVariation() ? "variation" : "contract";
        String contractName = model.isVariation() ? model.getVariationName(): contract.getContract().getName();

        if (status.equals(OrganisationContractStatus.PendingOffer)) {
            return String.format("%s '%s' for organisation %s has been withdrawn, reason: %s",
                contractType, contractName, organisation.getName(), model.getWithdrawReason());

        } else if (status.equals(OrganisationContractStatus.Offered)) {
            return String.format("%s '%s' for organisation %s has been offered",
                    contractType, contractName, organisation.getName());

        } else {
            return String.format("%s '%s' status updated to %s for organisation %s",
                    contractType, contractName, model.getStatus(), organisation.getName());
        }
    }

    public void deleteOrganisationContract(Integer organisationId, Integer contractId) {
        OrganisationEntity organisation = find(organisationId);
        enrichOrganisationContracts(organisation);
        OrganisationContract contract = organisation.getContractEntities()
            .stream()
            .filter(e -> e.getId().equals(contractId))
            .findFirst().orElse(null);
        if (contract != null) {
            organisation.getContractEntities().remove(contract);
            save(organisation);
            if (!contract.isVariation()) {
                OrganisationContractStatus initialStatus = OrganisationContractStatus.Blank;
                if (contract.getContract().getContractWorkflowType().equals(ContractWorkflowType.CONTRACT_OFFER_AND_ACCEPTANCE)) {
                    initialStatus = OrganisationContractStatus.PendingOffer;
                }
                auditService.auditCurrentUserActivity("contract '" + contract.getContract().getName() + "' status "
                    + "updated to " + initialStatus + " for organisation " + organisation.getName());
            } else {
                auditService.auditCurrentUserActivity("variation '" + contract.getContract().getName() + "' removed "
                        + " for organisation " + organisation.getName());
            }
        }
    }

    public void deleteAllOrganisationContract() {
        organisationContractRepository.deleteAll();
    }

    public void validateContractUsage(Integer contractId) {
        List<Integer> orgIds = getContractUsage(contractId).stream()
            .filter(c -> c.getStatus() == OrganisationContractStatus.Signed
                || c.getStatus() == OrganisationContractStatus.NotRequired)
            .map(oc -> oc.getOrganisationId())
            .collect(Collectors.toList());

        if (orgIds.size() > 0) {
            throw new ValidationException(String.format("Work flow type can't be changed as this contract is in use "
                + "with following organisations: %s", orgIds.toString()));
        }
    }

    public List<OrganisationContract> getContractUsage( Integer contractId) {
        return organisationContractRepository.findAllByContractId(contractId);
    }

    public AttachmentFile uploadContractFile(MultipartFile file, Integer contractId,
                                             Integer orgId) throws IOException {
        OrganisationContract contract = getContract(orgId, contractId);
        long totalAttachmentsSize = contract.getTotalAttachmentsSize();
        long allowedFileSize = 5 * ONE_MB - totalAttachmentsSize;
        if (file.getSize() > allowedFileSize) {
            throw new ValidationException(
                "Combined file size exceeds the combined file size limit of 5MB");
        }
        String directoryName = String.format("%d/%d", orgId, contractId);
        return fileService.upload(orgId, file.getOriginalFilename(), file.getContentType(),
            file.getSize(), file.getInputStream(), FileCategory.Attachment, directoryName);
    }

    public void getContractFile(Integer organisationId, Integer contractId, Integer fileId,
                                HttpServletResponse response) throws IOException {
        AttachmentFile file = fileService.getAttachmentFile(fileId);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"");
        response.setContentType(file.getContentType());
        if (file.getFileSize() != null) {
            response.setContentLength(file.getFileSize().intValue());
        }
        fileService.getFileContent(file, response.getOutputStream());
        response.flushBuffer();

    }

    public boolean isManagingOrganisation(Integer orgId) {
        return findOne(orgId).isManaging();
    }

    public boolean isTechSupportOrganisation(Integer orgId) {
        return findOne(orgId).isTechSupportOrganisation();
    }

    public boolean isTeamOrganisation(Integer orgId) {
        return findOne(orgId).isTeamOrganisation();
    }

    /**
     * Throws a NotFoundException if the given name is already used by another organisation, case insensitive.
     */
    public void checkOrganisationNameNotUsed(String name, Integer managingOrganisationId) {
        List<OrganisationEntity> organisations = organisationRepository.findByNameIgnoreCaseAndManagingOrganisation(name,
                managingOrganisationId);
        for (OrganisationEntity org : organisations) {
            if (org != null && !org.isRejected() && !org.isInactive()) {
                throw new NotFoundException("Organisation name " + name + " already in use!");
            }
        }
    }

    public List<AnnualSubmission> getAnnualSubmissions(Integer organisationId) {
        return annualSubmissionService.getAnnualSubmissions(organisationId);
    }

    public List<UserModel> getUsersForOrganisation(Integer organisationId) {
        OrganisationEntity org = findOne(organisationId);

        UserEntity currentUser = userService.loadCurrentUser();
        if (!currentUser.isManagedBy(org) && !currentUser.inOrganisation(org)) {
            throw new ForbiddenAccessException();
        }

        List<UserModel> usersSorted = userMapper.mapToModel(org.getUserEntities());
        usersSorted.sort(comparing(UserModel::getFirstName));
        return usersSorted;
    }

    public Set<OrganisationTeam> getManagedOrganisationAndTeams() {
        UserEntity user = userService.currentUser();
        Set<OrganisationEntity> myManagingOrgs = user.getRoles()
                .stream()
                .map(Role::getOrganisation)
                .filter(OrganisationEntity::isManaging)
                .collect(Collectors.toSet());

        Set<OrganisationTeam> teams = new HashSet<>();
        for (OrganisationEntity myManagingOrg : myManagingOrgs) {
            teams.add(new OrganisationTeam(myManagingOrg.getId(), myManagingOrg.getName()));
            myManagingOrg = organisationRepository.getOne(myManagingOrg.getId());
            Set<TeamEntity> managedTeams = myManagingOrg.getManagedTeams();
            for (TeamEntity managedTeam : managedTeams) {
                teams.add(new OrganisationTeam(myManagingOrg.getId(), myManagingOrg.getName(), managedTeam.getId(),
                        managedTeam.getName()));
            }
        }

        return teams;
    }

    public List<OrganisationType> getAccessibleOrganisationTypes() {
        List<OrganisationType> accessibleOrganisationTypes = new ArrayList<>(Arrays.asList(OrganisationType.values()));
        accessibleOrganisationTypes.remove(OrganisationType.TEAM);

        UserEntity currentUser = userService.currentUser();
        if (currentUser == null || !currentUser.isOpsAdmin()) {
            accessibleOrganisationTypes.remove(OrganisationType.MANAGING_ORGANISATION);
            accessibleOrganisationTypes.remove(OrganisationType.TECHNICAL_SUPPORT);
        }

        return accessibleOrganisationTypes;
    }

    public Map<Integer, OrganisationType> getOrganisationTypes() {
        return getAccessibleOrganisationTypes().stream().collect(Collectors.toMap(OrganisationType::getId, Function.identity()));
    }

    public OrganisationEntity changeStatus(Integer organisationId, OrganisationStatus status, OrganisationChangeStatusReason reason,
                                           String details, Integer duplicateOrgId) {
        OrganisationEntity organisation = find(organisationId);

        UserEntity currentUser = userService.loadCurrentUser();

        if (!currentUser.isOpsAdmin() && currentUser.getUsername().equals(organisation.getCreatedBy())) {
            throw new ValidationException("You cannot approve an organisation that you requested");
        }


        preStateTransitionAction(organisation, status, reason, details, duplicateOrgId);
        organisation.changeStatus(status, currentUser.getUsername(), environment.now());
        organisation.updateChangeStatusReason(reason);
        organisation.updateChangeStatusReasonDetails(details);
        OrganisationEntity savedOrganisation = save(organisation);
        OrganisationStatus previousStatus = organisation.getStatus();
        postStateTransitionAction(organisation, previousStatus);
        return savedOrganisation;
    }

    private void preStateTransitionAction(OrganisationEntity organisation, OrganisationStatus status,
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
            OrganisationEntity duplicateOrganisation = organisationRepository.findById(duplicateOrgId).orElse(null);
            if (duplicateOrganisation == null) {
                throw new ValidationException("Duplicate organisation can't be found");
            }
            organisation.setDuplicateOrganisationId(duplicateOrganisation.getId());
        }
    }

    private void postStateTransitionAction(OrganisationEntity organisation, OrganisationStatus previousStatus) {
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
                for (UserEntity user : organisation.getUserEntities()) {
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
 }
