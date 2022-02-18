/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.OpsEntity;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.notification.EmailService;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.organisation.OrganisationType;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.permission.PermissionServiceImpl;
import uk.gov.london.ops.role.RoleService;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.role.model.RoleNameAndDescription;
import uk.gov.london.ops.service.DataAccessControlService;
import uk.gov.london.ops.user.domain.*;
import uk.gov.london.ops.user.implementation.UserRepository;
import uk.gov.london.ops.user.implementation.UserRoleSummaryRepository;
import uk.gov.london.ops.user.implementation.UserSummaryRepository;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.london.common.GlaUtils.parseInt;
import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.notification.NotificationType.UserRequestAccess;
import static uk.gov.london.ops.notification.NotificationType.UserTeamAccessApproval;
import static uk.gov.london.ops.permission.PermissionType.*;
import static uk.gov.london.ops.user.User.SYSTEM_DEACTIVATED_USERNAME;

@Transactional
@Service
public class UserServiceImpl implements UserDetailsService, UserService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    UserRoleSummaryRepository userRoleSummaryRepository;

    @Autowired
    UserSummaryRepository userSummaryRepository;

    @Autowired
    OrganisationServiceImpl organisationService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    EmailService emailService;

    @Autowired
    AuditService auditService;

    @Autowired
    DataAccessControlService dataAccessControlService;

    @Autowired
    UserPasswordService userPasswordService;

    @Autowired
    PermissionServiceImpl permissionService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserFinanceThresholdService userFinanceThresholdService;

    @Autowired
    RoleService roleService;

    static final String SYSTEM_USER_NAME = "GLA-OPS system";

    @Autowired
    Environment environment;

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * This method is to be used to load a user in the context of authentication.
     *
     * @throws UsernameNotFoundException if no user with the given username if found. That will result in a 401 http error.
     */
    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findById(username.toLowerCase()).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User " + username + " not found!");
        }
        return user;
    }

    /**
     * This method is to be used to load a user in the context of a API read operation.
     *
     * @throws NotFoundException if no user with the given username if found. That will result in a 404 http error.
     */
    @Override
    public UserEntity find(String userIdOrName) throws NotFoundException {
        UserEntity user = get(userIdOrName);
        if (user == null) {
            throw new NotFoundException(String.format("User with ID %s not found", userIdOrName));
        }
        return user;
    }

    /**
     * This method is to be used to load a user and return null if not existing.
     *
     * @return null if not existing.
     */
    public UserEntity get(String userIdOrName) throws NotFoundException {
        if (isNumeric(userIdOrName)) {
            return userRepository.findByUserId(Integer.valueOf(userIdOrName));
        } else {
            return userRepository.findById(userIdOrName).orElse(null);
        }
    }

    private boolean isNumeric(String userName) {
        if (userName == null) {
            return false;
        }
        try {
            Integer.valueOf(userName);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean canAssignRoles(Set<Role> roles, Integer orgId) {
        for (Role role : roles) {
            if (role.getOrganisation().getId().equals(orgId)) {
                String roleName = role.getName();
                if (roleName.equals(GLA_ORG_ADMIN) || roleName.equals(ORG_ADMIN)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return user's profile with org details filtered to the ones the current user has access to.
     */
    public UserProfile getUserProfile(String userIdOrName) {
        UserEntity user = find(userIdOrName);

        UserProfile userProfile = buildUserProfileFrom(user);

        UserEntity currentUser = currentUser();
        userProfile.setCanResetPassword(canResetUserPassword(user, currentUser));
        boolean isOpsAdmin = currentUser.isOpsAdmin();
        for (Role role : user.getRoles()) {
            processUserRoles(user, currentUser, isOpsAdmin, userProfile, role);
        }

        setupHasSingleRoleFlag(userProfile);

        if (!user.equals(currentUser) && userProfile.getOrganisations().isEmpty() && (!user.getRoles().isEmpty()
                || !isOpsAdmin)) {
            throw new ForbiddenAccessException("you do not have access to this user!");
        }

        return userProfile;
    }

    UserProfile buildUserProfileFrom(UserEntity user) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername(user.getUsername());
        userProfile.setUserId(user.getUserId());
        userProfile.setFirstName(user.getFirstName());
        userProfile.setLastName(user.getLastName());
        userProfile.setEnabled(user.isEnabled());
        userProfile.setDeactivatedBy(user.getDeactivatedBy());
        userProfile.setLastLoggedIn(user.getLastLoggedOn());
        return userProfile;
    }

    private void setupHasSingleRoleFlag(UserProfile userProfile) {
        // need to set any repeated orgs that are used more than once to ensure the UI knows whether to delete the role
        // or dissassociate completely the user
        Map<Integer, Long> counted = userProfile.getOrganisations().stream()
                .collect(groupingBy(UserProfileOrgDetails::getOrgId, counting()));

        counted.entrySet().stream().filter(e -> e.getValue() > 1).forEach(e ->
                userProfile.getOrganisations().stream()
                        .filter(o -> o.getOrgId().equals(e.getKey()))
                        .forEach(o -> o.setHasSingleRoleInThisOrg(false))
        );
    }

    public boolean canResetUserPassword(String username) {
        UserEntity user = find(username);
        UserEntity currentUser = currentUser();
        return canResetUserPassword(user, currentUser);
    }

    boolean canResetUserPassword(UserEntity targetUser, UserEntity currentUser) {
        if (Arrays.asList("local", "dev", "qas", "ppd").contains(environment.shortName().toLowerCase())) {
            return dataAccessControlService.hasPasswordResetAccess(targetUser, currentUser);
        }
        return false;
    }

    private void processUserRoles(UserEntity user, UserEntity currentUser, boolean isOpsAdmin, UserProfile userProfile,
            Role role) {
        if (user.equals(currentUser)
                || dataAccessControlService.hasAccessToOrganisationUsers(currentUser, role.getOrganisation())) {
            UserProfileOrgDetails userOrgDetails = new UserProfileOrgDetails();

            Integer orgId = role.getOrganisation().getId();
            userOrgDetails.setOrgId(orgId);
            userOrgDetails.setOrgName(role.getOrganisation().getName());
            // we don't want to display the managing organisation if the user is in a managing organisation
            if (!role.getOrganisation().isManaging()) {
                userOrgDetails.setManagingOrgId(role.getOrganisation().getManagingOrganisationId());
                userOrgDetails.setManagingOrgName(role.getOrganisation().getManagingOrganisationName());
            }
            userOrgDetails.setRole(role.getDescription());
            userOrgDetails.setRoleName(role.getName());
            userOrgDetails.setApproved(role.isApproved());
            userOrgDetails.setHasOrgAdminRoleRequested(Boolean.TRUE.equals(role.getOrgAdminRequested()));
            userOrgDetails.setPrimary(role.isPrimaryOrganisationForUser() == null ? false : role.isPrimaryOrganisationForUser());
            userOrgDetails.setAuthorisedSignatory(role.getAuthorisedSignatory() == null ? false : role.getAuthorisedSignatory());
            // if current user roles allow him to change...
            if (isOpsAdmin || permissionService.currentUserHasPermissionForOrganisation(USER_APPROVE, orgId)) {
                userOrgDetails.setAssignableRoles(roleService.getAssignableRoles(orgId));
            }

            userProfile.getOrganisations().add(userOrgDetails);

        }

        if (role.isApproved()) {
            OrganisationDetailsDTO organisationDetailsDTO = userProfile.getAssignableOrganisations().stream()
                    .filter(o -> o.getId() == role.getOrganisation().getId()).findFirst().orElse(null);

            if (organisationDetailsDTO == null && currentUser.canAssignRolesInOrganisation(role.getOrganisation().getId(),
                    role.getOrganisation().getManagingOrganisationId())) {

                List<RoleNameAndDescription> assignableRoles = new ArrayList<>(roleService.getAssignableRoles(
                        role.getOrganisation().getId()));

                organisationDetailsDTO = new OrganisationDetailsDTO(role.getOrganisation().getId(),
                        role.getOrganisation().getName(), assignableRoles);

                userProfile.getAssignableOrganisations().add(organisationDetailsDTO);
            }
            if (organisationDetailsDTO != null) {
                organisationDetailsDTO.getAvailableRoles().removeIf(r -> r.getName().equals(role.getName()));
            }
        }
    }

    /**
     * Returns all currently defined user roles.
     */
    public Page<UserRoleSummary> findAll(String organisationNameOrId,
            String userNameOrEmail,
            List<String> registrationStatus,
            List<String> roles,
            List<Integer> orgTypes,
            List<String> spendAuthority,
            Pageable pageable) {
        Page<UserRoleSummary> userRolesPage = userRoleSummaryRepository.findAll(currentUser(),
                organisationNameOrId,
                userNameOrEmail,
                registrationStatus,
                roles,
                orgTypes,
                spendAuthority,
                pageable);

        for (UserRoleSummary roleSummary : userRolesPage) {
            roleSummary.setAssignableRoles(roleService.getAssignableRoles(roleSummary.getOrganisationId()));
        }

        return userRolesPage;
    }

    public List<UserEntity> findAll() {
        if (environment.isTestEnvironment()) {
            return userRepository.findAll();
        }
        return Collections.emptyList();
    }

    /**
     * Returns all currently defined users.
     */
    public Page<UserSummary> findAllUsers(String organisationNameOrId,
            String userNameOrEmail,
            List<String> registrationStatus,
            List<Boolean> userEnabledStatus,
            List<String> roles,
            List<Integer> orgTypes,
            List<String> spendAuthority,
            Pageable pageable) {

        UserEntity currentUser = currentUser();
        Integer orgId = parseInt(organisationNameOrId);
        List<Integer> managingOrgTypes = OrganisationType.getInternalOrganisationTypesIds();

        // To 'skip' some native query filtering conditions when no filters are provided the default value (-1) is set.
        // Query in the repository checks if the default value (-1) is provided and if it is then
        // the 'where' condition evaluates to true and skips further checks for that parameter (returns all)
        Page<UserSummary> usersPage = userSummaryRepository.findAll(currentUser.getUsername(),
                currentUser.isOpsAdmin(),
                currentUser.getOrganisationIds().isEmpty() ? Arrays.asList(-1) : currentUser.getOrganisationIds(),
                organisationNameOrId == null ? "" : organisationNameOrId,
                orgId == null ? -1 : orgId,
                (orgTypes == null || orgTypes.isEmpty()) ? Arrays.asList(-1) : orgTypes,
                currentUser.isGla() ? managingOrgTypes : Arrays.asList(-1),
                userNameOrEmail == null ? "" : userNameOrEmail,
                (userEnabledStatus == null || userEnabledStatus.isEmpty()) ? Arrays.asList(true, false) : userEnabledStatus,
                (roles == null || roles.isEmpty()) ? Arrays.asList("-1") : roles,
                (spendAuthority == null || spendAuthority.isEmpty())
                        ? Arrays.asList("pendingChanges", "notSet", "usersWithSpendAuthority", "N/A")
                        : spendAuthority,
                (registrationStatus == null || registrationStatus.isEmpty())
                        ? Arrays.asList("Approved", "Unapproved", "Pending", "Orphaned") : registrationStatus,
                pageable);

        for (UserSummary userSummary : usersPage.toList()) {
            List<Integer> organisationIds = currentUser().getOrganisationIds();
            Set<UserRoleSummary> accessibleRoles = userSummary.getRoles().stream()
                    .filter(rs -> Boolean.TRUE.equals(rs.getApproved()) && (organisationIds.contains(rs.getOrganisationId())
                            || organisationIds.contains(rs.getManagingOrganisationId())
                            || managingOrgTypes.contains(rs.getEntityTypeId())))
                    .collect(Collectors.toSet());
            userSummary.setAccessibleRoles(accessibleRoles);
        }
        return usersPage;
    }

    @Override
    public Collection<String> findAllUsernamesFor(List<Integer> orgIds, List<String> roles) {
        Set<String> users = new HashSet<>();
        for (UserRoleSummary userRoleSummary : userRoleSummaryRepository.findAll(orgIds, roles)) {
            users.add(userRoleSummary.getUsername());
        }
        return users;
    }

    public UserEntity register(UserRegistration registration) {
        if (userRepository.existsById(registration.getEmail().toLowerCase())) {
            throw new ValidationException("username", "Email address is already registered");
        }

        OrganisationEntity organisation = organisationService.findByOrgCode(registration.getOrgCode());
        if (organisation == null) {
            throw new ValidationException("Invalid provider number");
        }

        if (!EmailValidator.getInstance().isValid(registration.getEmail())) {
            throw new ValidationException("Email", "Email address is invalid.");
        }

        UserEntity user = userMapper.toEntity(registration, organisation);
        userPasswordService.setNewPassword(user, user.getPassword(), false);

        Map<String, Object> model = new HashMap<String, Object>() {{
            put("organisation", organisation);
        }};
        notificationService.createNotification(UserRequestAccess, user, model);
        return user;
    }

    /**
     * @return the current logged in user, or null if no logged in user
     */
    public UserEntity currentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if ((principal != null) && (principal instanceof UserEntity)) {
            return (UserEntity) principal;
        }

        return null;
    }

    public static void withLoggedInUser(UserEntity user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    public void updateUserStatus(String userIdOrName, boolean enabled) {
        updateUserStatus(userIdOrName, enabled, false);
    }

    public void updateUserStatus(String userIdOrName, boolean enabled, boolean bySystem) {
        UserEntity user = find(userIdOrName);

        if (!bySystem) {
            validateCurrentUserCanUpdateStatusFor(user);
        }

        user.setEnabled(enabled);
        if (enabled) {
            user.setDeactivatedBy(null);
            user.setPasswordExpiry(OffsetDateTime.now());
        } else {
            user.setDeactivatedBy(bySystem ? SYSTEM_DEACTIVATED_USERNAME : currentUser().getUsername());
        }
        userRepository.save(user);

        emailService.sendUserStatusUpdatedEmail(user);
        auditService.auditCurrentUserActivity(String.format("%s user %s", enabled ? "Enabled" : "Disabled", user.getUsername()));
    }

    public void updateUserDetails(UserProfile userProfile) {
        UserEntity user = find(userProfile.getUsername());
        if (hasUserChanges(user, userProfile)) {
            validateCurrentUserCanUpdateUserDetails(user);
            user.setFirstName(userProfile.getFirstName());
            user.setLastName(userProfile.getLastName());
            userRepository.save(user);
            auditService.auditCurrentUserActivity(String.format("User first name / last name updated, %s", user.getUsername()));
        }
    }

    boolean hasUserChanges(UserEntity oldUser, UserProfile newUser) {
        return !(oldUser.getFirstName().equals(newUser.getFirstName())
                && oldUser.getLastName().equals(newUser.getLastName()));
    }

    void validateCurrentUserCanUpdateUserDetails(UserEntity user) {
        UserEntity currentUser = currentUser();
        if (!currentUser.isOpsAdmin() && !currentUser.getUsername().equals(user.getUsername())) {
            throw new ValidationException("Either the user itself or OPS Admin can update first name / last name.");
        }
    }

    void validateCurrentUserCanUpdateStatusFor(UserEntity user) {
        UserEntity currentUser = currentUser();

        boolean currentUserManagesAllTargetUserOrgs = user.getOrganisationIds().stream().allMatch(orgId ->
                currentUser.hasRoleInOrganisation(GLA_ORG_ADMIN, orgId) || currentUser.hasRoleInOrganisation(ORG_ADMIN, orgId));

        if (!currentUser.isOpsAdmin() && !currentUserManagesAllTargetUserOrgs) {
            throw new ValidationException(
                    "You cannot deactivate this user as they have roles in an organisation you do not manage.");
        }
    }

    /**
     * assigns a default role using rules specified in GLA-22953
     */
    public void assignDefaultPrimaryOrganisation(UserEntity user) {
        Set<Role> superiorRoles = this.getSuperiorRoles(user);

        user.getRoles().forEach(r -> r.setPrimaryOrganisationForUser(false));
        if (superiorRoles != null && superiorRoles.size() > 0) {
            List<Role> list = superiorRoles.stream().sorted(Comparator.comparingInt(Role::getId)).collect(Collectors.toList());
            list.get(0).setPrimaryOrganisationForUser(true);
        }
        userRepository.save(user);
    }

    Set<Role> getSuperiorRoles(UserEntity user) {
        Set<Role> approvedRoles = user.getApprovedRoles();
        if (approvedRoles.size() <= 1) {
            return user.getRoles();
        }

        Map<String, Set<Role>> roleMap = approvedRoles.stream().collect(groupingBy(Role::getName, Collectors.toSet()));
        String higestPriorityRole = Role.getHighestPriorityRole(roleMap.keySet());
        return roleMap.get(higestPriorityRole);

    }

    /**
     * Returns the username of the currently active user, or null if there isn't one.
     */
    public String currentUsername() {
        UserEntity currentUser = currentUser();
        return currentUser == null ? null : currentUser.getUsername();
    }

    /**
     * Forces loading the current user from the database.
     *
     * @return the loaded user.
     */
    public UserEntity loadCurrentUser() {
        UserEntity currentUser = currentUser();
        if (currentUser != null) {
            return find(currentUser.getUsername());
        } else {
            return null;
        }
    }

    public String getUserFullName(String username) {
        if (username == null) {
            return null;
        }

        if (username.equals(SYSTEM_USER_NAME)) {
            return SYSTEM_USER_NAME;
        }

        UserEntity user = null;
        try {
            user = get(username);
        } catch (NotFoundException e) {
            log.info(String.format("user: %s not found", username));
            // do nothing user could have been removed.
        }

        return user == null ? username : user.getFullName();
    }

    // TODO: temporary while moving user related classes to the user module, remove once all user related class moved to module
    @Deprecated
    @Override
    public void save(User user) {
        saveUser((UserEntity) user);
    }

    public UserEntity saveUser(UserEntity user) {
        return userRepository.saveAndFlush(user);
    }

    public Integer assignRoleToUsers(Set<UserRoleRequest> usernames, Integer organisationId) {
        int count = 0;

        OrganisationEntity organisation = organisationService.find(organisationId);
        Map<String, Object> model = new HashMap<String, Object>() {{
            put("organisation", organisation);
        }};

        for (UserRoleRequest roleRequest : usernames) {
            UserEntity userToUpdate = find(roleRequest.getUsername());
            boolean managingOrgRole = userToUpdate.getRoles().stream()
                    .anyMatch(r -> r.getOrganisation().getEntityType().equals(OrganisationType.MANAGING_ORGANISATION.getId()));
            boolean noExistingTeamRole = userToUpdate.getRoles().stream()
                    .noneMatch(r -> r.getOrganisation().getId().equals(organisationId));
            if (managingOrgRole && noExistingTeamRole) {
                assignRole(roleRequest.getUsername(), roleRequest.getRequestedRole(), organisationId, false);
                notificationService.createNotificationForUser(UserTeamAccessApproval, userToUpdate, model,
                        userToUpdate.getUsername());
                count++;
            }
        }
        return count;
    }

    public void assignRole(String username, String roleName, Integer organisationId, boolean isNewRole) {

        if (isNewRole && !featureStatus.isEnabled(Feature.AllowMultipleRolesProcess)) {
            throw new ValidationException("Unable to add additional role as AllowMultipleRolesProcess is false");
        }

        validateRoleExists(roleName);

        // need to re-inflate current user.
        UserEntity currentUser = currentUser();
        if (currentUser != null) {
            currentUser = userRepository.findById(currentUser.getUsername()).orElse(null);
        }

        if (currentUser == null) { // unlike outside test environment
            throw new ValidationException("Unable to identify user.");
        }

        UserEntity userToUpdate = find(username);

        OrganisationEntity organisation = organisationService.find(organisationId);

        // if not a OPS Admin we need to check we're of the same organisation.
        if (!organisation.getEntityType().equals(OrganisationType.TEAM.getId())
                && !(currentUser.hasRole(OPS_ADMIN) || currentUser.hasRole(GLA_ORG_ADMIN))) {
            if (!currentUser.getOrganisations().contains(organisation) || userToUpdate.getRole(organisation) == null) {
                throw new ValidationException("roleName ",
                        String.format("User %s is not a member of the organisation: %s.", username, organisation.getName()));
            }
        }

        if (!isNewRole) {
            userToUpdate.getRoles().removeIf(r -> Objects.equals(r.getOrganisation(), organisation));
        }

        userToUpdate.addApprovedRole(roleName, organisation);
        userRepository.save(userToUpdate);

        if (shouldAutoSubscribeToOrganisation(username, roleName, organisationId)) {
            notificationService.subscribe(username, EntityType.organisation, organisationId);
        }

        if (!permissionService.userHasPermissionForOrganisation(userToUpdate, AUTHORISE_PAYMENT, organisationId)) {
            userFinanceThresholdService.clearFinanceThreshold(username, organisationId);
        }

        auditService.auditCurrentUserActivity(String.format("Role %s assigned to user %s", roleName, username));
    }

    @Override
    public Set<String> findAllUsernamesWithPasswordExpiryExceedingMonths(int expiryMonths) {
        return userRepository.findAllUsernamesExpiredBefore(Date.from(environment.now().minusMonths(expiryMonths).toInstant()));
    }

    public boolean shouldAutoSubscribeToOrganisation(String username, String roleName, Integer organisationId) {
        return Stream.of(GLA_ORG_ADMIN, ORG_ADMIN).anyMatch(r -> r.equals(roleName))
                && !notificationService.isSubscribed(username, EntityType.organisation, organisationId);
    }

    public void updateSuccessfulUserLogon(UserEntity user) {
        user.setLastLoggedOn(environment.now());
        userRepository.save(user);
    }

    public String getSystemUserName() {
        return SYSTEM_USER_NAME;
    }

    public void deleteUser(String username) {
        userRepository.deleteById(username);
    }

    public void setPrimaryOrgForUser(String userIdOrName, Integer orgId, String role) {
        UserEntity user = find(userIdOrName);
        boolean roleFound = false;
        for (Role approvedRole : user.getApprovedRoles()) {
            if (approvedRole.getOrganisation().getId().equals(orgId) && approvedRole.getName().equals(role)) {
                roleFound = true;
                approvedRole.setPrimaryOrganisationForUser(true);
            } else {
                approvedRole.setPrimaryOrganisationForUser(false);
            }
        }
        if (!roleFound) {
            throw new ValidationException("Unable to find approved role in primary organisation for user " + user.getUsername());
        }
        userRepository.save(user);
    }

    public boolean checkActuatorEndpointAccess(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        UserEntity user = null;
        if ((principal != null) && (principal instanceof UserEntity)) {
            user = (UserEntity) principal;
        }

        if (user != null) {
            Set<String> permissions = permissionService.getPermissionsForUser(user);
            return permissions.contains(SYS_DASHBOARD.getPermissionKey());
        }
        return false;
    }

    private void validateRoleExists(String role) {
        if (!Role.availableRoles().contains(role)) {
            throw new ValidationException("Unknown role: " + role);
        }
    }

    public int promoteUsersRole(String fromRole, String toRole, OrganisationType organisationType, String auditUsername,
            String auditMessage) {
        validateRoleExists(fromRole);
        validateRoleExists(toRole);
        int userUpdated = roleService.updateRoles(fromRole, toRole, organisationType.getId());
        auditService.auditActivityForUser(auditUsername, userUpdated + auditMessage);
        return userUpdated;
    }

    public void delete(UserEntity user) {
        if (environment.isTestEnvironment()) {
            userRepository.delete(user);
        }
    }

    public List<UserEntity> findUsersByRoles(List<String> roles) {
        return userRepository.findByUserRoles(roles);
    }

    @Override
    public Set<User> getOrganisationUsersWithRoles(Integer orgId, String... roles) {
        Set<User> users = userRepository.findByOrganisationIdAndUserRoles(orgId, roles).stream().collect(Collectors.toSet());
        return users;
    }

    @Override
    public Set<User> getAuthorisedSignatories(Integer orgId) {
        return roleService.getAuthorisedSignatories(orgId).stream().map(u -> find(u)).collect(Collectors.toSet());
    }

    public Set<String> getOrganisationUsersWithRolesUsernames(Integer orgId, String... roles) {
        Set<User> usersWithRoles = getOrganisationUsersWithRoles(orgId, roles);
        return usersWithRoles.stream().map(User::getUsername).collect(Collectors.toSet());
    }

    @Override
    public void enrich(OpsEntity opsEntity) {
        if (opsEntity.getCreatedBy() != null) {
            opsEntity.setCreatorName(getUserFullName(opsEntity.getCreatedBy()));
        }
        if (opsEntity.getModifiedBy() != null) {
            opsEntity.setModifierName(getUserFullName(opsEntity.getModifiedBy()));
        }
    }

    public void setAuthorisedSignatory(String userIdOrName, Integer orgId, String role, Boolean signatory) {
        UserEntity user = find(userIdOrName);
        user.getRolesInOrganisation(orgId)
                .forEach(r -> {
                    if (r.getName().equals(role)) {
                        r.setAuthorisedSignatory(signatory);
                    }
                });
        userRepository.save(user);
    }

    public void requestOrgAdminRole(String username, Integer orgId, boolean isOpen) {
        UserEntity aboutUser = find(username);
        Optional<Role> first = aboutUser.getRolesInOrganisation(orgId).stream()
                .filter(r -> r.getName().equals(PROJECT_EDITOR))
                .findFirst();

        if (first.isPresent()) {

            first.get().setOrgAdminRequested(isOpen);

            if (isOpen) {
                auditService.auditCurrentUserActivity("Requested Org Admin role for org: " + orgId);

                OrganisationEntity organisationEntity = organisationService.find(orgId);
                Collection<String> allUsernamesFor = this.findAllUsernamesFor(
                        Stream.of(organisationEntity.getManagingOrganisationId()).collect(Collectors.toList()),
                        Stream.of(GLA_ORG_ADMIN).collect(Collectors.toList()));

                for (String toUserName : allUsernamesFor) {
                    UserEntity toUser = find(toUserName);
                    emailService.sendOrgAdminRequestedEmail(toUser, aboutUser, organisationEntity);
                }
            } else {
                auditService.auditCurrentUserActivity("Closed request for Org Admin role for user: " + username);
            }
        }
    }
}
