/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import static uk.gov.london.common.GlaUtils.getRequestIp;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.ops.notification.NotificationType.PendingSpendAuthorityThresholdApproval;
import static uk.gov.london.ops.notification.NotificationType.UserRequestAccess;
import static uk.gov.london.ops.service.PermissionType.AUTHORISE_PAYMENT;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.audit.ActivityType;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.user.PasswordResetToken;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.domain.user.UserOrgFinanceThreshold;
import uk.gov.london.ops.domain.user.UserOrgKey;
import uk.gov.london.ops.domain.user.UserSummary;
import uk.gov.london.ops.framework.annotations.LogMetrics;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.notification.Email;
import uk.gov.london.ops.notification.EmailService;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.implementation.UserMapper;
import uk.gov.london.ops.repository.PasswordResetTokenRepository;
import uk.gov.london.ops.repository.UserOrgFinanceThresholdRepository;
import uk.gov.london.ops.repository.UserRepository;
import uk.gov.london.ops.repository.UserSummaryRepository;
import uk.gov.london.ops.web.model.CreatePasswordResetTokenResponse;
import uk.gov.london.ops.web.model.UserPasswordReset;
import uk.gov.london.ops.web.model.UserProfile;
import uk.gov.london.ops.web.model.UserProfileOrgDetails;
import uk.gov.london.ops.web.model.UserRegistration;

@Transactional
@Service
public class UserService implements UserDetailsService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserSummaryRepository userSummaryRepository;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    UserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailService emailService;

    @Autowired
    AuditService auditService;

    @Autowired
    DataAccessControlService dataAccessControlService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserOrgFinanceThresholdRepository userOrgFinanceThresholdRepository;

    static final String SYSTEM_USER_NAME = "GLA-OPS system";

    @Value("${password.minimum.complexity}")
    public int minimumPasswordStrength;
    @Value("${password.minimum.length}")
    public int minimumPasswordLength;

    @Autowired
    Environment environment;

    Zxcvbn zxcvbn = new Zxcvbn();   // Password strength utility

    @Value("#{'${invalid.passwords}'.split(',')}")
    List<String> blackList;

    /** Password token expiry in minutes. Default is 24h. */
    @Value("${password.token.expiry}")
    private long passwordTokenExpiry = 24 * 60;

    /**
     * This method is to be used to load a user in the context of authentication.
     * @param username
     * @return
     * @throws UsernameNotFoundException if no user with the given username if found. That will result in a 401 http error.
     */
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findById(username.toLowerCase()).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User "+username+" not found!");
        }
        return user;
    }

    /**
     * This method is to be used to load a user in the context of a API read operation.
     * @param username
     * @return
     * @throws NotFoundException if no user with the given username if found. That will result in a 404 http error.
     */
    public User find(String username) throws NotFoundException {
        User user = userRepository.findById(username).orElse(null);
        if (user == null) {
            throw new NotFoundException("Username not found:" + username);
        }
        return user;
    }

     /**
     * This method is to be used to load a user and return null if not existing.
     * @param username
     * @return null if not existing.
     */
    public User get(String username) throws NotFoundException {
        return userRepository.findById(username).orElse(null);
    }


    boolean canAssignRoles(Set<Role> roles, Integer orgId){
        for(Role role : roles){
            if(role.getOrganisation().getId().equals(orgId)){
                String roleName = role.getName();
                if(roleName.equals(GLA_ORG_ADMIN) || roleName.equals(ORG_ADMIN)){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return user's profile with org details filtered to the ones the current user has access to.
     */
    public UserProfile getUserProfile(String username) {
        User user = find(username);

        User currentUser = currentUser();
        boolean isOpsAdmin = currentUser.isOpsAdmin();
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername(user.getUsername());
        userProfile.setFirstName(user.getFirstName());
        userProfile.setLastName(user.getLastName());

        for (Role role: user.getRoles()) {
            if (user.equals(currentUser) || dataAccessControlService.hasAccess(currentUser, role.getOrganisation())) {
                UserProfileOrgDetails userOrgDetails = new UserProfileOrgDetails();
                Integer orgId = role.getOrganisation().getId();
                userOrgDetails.setOrgId(orgId);
                userOrgDetails.setOrgName(role.getOrganisation().getName());
                // we don't want to display the managing organisation if the user is in a managing organisation
                if (!role.getOrganisation().isManagingOrganisation()) {
                    userOrgDetails.setManagingOrgName(role.getOrganisation().getManagingOrganisationName());
                }
                userOrgDetails.setRole(role.getDescription());
                userOrgDetails.setRoleName(role.getName());
                userOrgDetails.setApproved(role.isApproved());
                userOrgDetails.setPrimary(role.isPrimaryOrganisationForUser() == null? false : role.isPrimaryOrganisationForUser());
                // if current user roles allow him to change...
                if(isOpsAdmin || this.canAssignRoles(currentUser.getRoles(), orgId)){
                    userOrgDetails.setAssignableRoles(organisationService.getAssignableRoles(orgId));
                }

                userProfile.getOrganisations().add(userOrgDetails);

            }
        }

        if (!user.equals(currentUser) && userProfile.getOrganisations().isEmpty()) {
            throw new ForbiddenAccessException("you do not have access to this user!");
        }

        return userProfile;
    }

    /**
     * Returns all currently defined users.
     */
    public Page<UserSummary> findAll(String organisationNameOrId,
                                     String userNameOrEmail,
                                     List<String> registrationStatus,
                                     List<String> roles,
                                     List<Integer> orgTypes,
                                     List<String> spendAuthority,
                                     Pageable pageable) {
        return userSummaryRepository.findAll(currentUser(),
                organisationNameOrId,
                userNameOrEmail,
                registrationStatus,
                roles,
                orgTypes,
                spendAuthority,
                pageable);
    }

    public User register(UserRegistration registration) {
        if (userRepository.existsById(registration.getEmail().toLowerCase())) {
            throw new ValidationException("username", "Email address is already registered");
        }

        Organisation organisation = organisationService.findByOrgCode(registration.getOrgCode());
        if (organisation == null) {
            throw new ValidationException("Invalid IMS number");
        }

        if (!EmailValidator.getInstance().isValid(registration.getEmail())) {
            throw new ValidationException("Email", "Email address is invalid.");
        }

        User user = userMapper.toEntity(registration, organisation);
        saveNewPassword(user,user.getPassword(), false);

        organisationService.updateOrganisationUserRegStatus(organisation);

        Map<String, Object> model = new HashMap<String, Object>() {{
            put("organisation", organisation);
        }};
        notificationService.createNotification(UserRequestAccess, user, model);
        return user;
    }

    public void setPassword(String username, String password) {
        User user = userRepository.findById(username).orElse(null);
        if (user == null) {
            throw new NotFoundException("User not found: " + username);
        }
        saveNewPassword(user, password, true);
    }

    /**
     * @return the current logged in user, or null if no logged in user
     */
    @LogMetrics
    public User currentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if ((principal != null) && (principal instanceof User)) {
            return (User)principal;
        }

        return null;
    }

    /**
     * Find users without primary role
     */
    public Set<User> getUsersWithoutAssignedPrimaryOrganisation() {
        return userRepository.findAllUsersWithoutPrimaryRole();
    }

    /**
     * assigns a default role using rules specified in GLA-22953
     */
    public void assignDefaultPrimaryOrganisation(User user) {
        Set<Role> superiorRoles = this.getSuperiorRoles(user);

        user.getRoles().forEach(r -> r.setPrimaryOrganisationForUser(false));
        if (superiorRoles != null && superiorRoles.size() > 0) {
            List<Role> list = superiorRoles.stream().sorted(Comparator.comparingInt(Role::getId)).collect(Collectors.toList());
            list.get(0).setPrimaryOrganisationForUser(true);
        }
        userRepository.save(user);
    }

    Set<Role> getSuperiorRoles(User user) {
        Set<Role> approvedRoles = user.getApprovedRoles();
        if (approvedRoles.size() <= 1) {
            return user.getRoles();
        }

        Map<String, Set<Role>> roleMap = approvedRoles.stream().collect(Collectors.groupingBy(Role::getName,  Collectors.toSet()));
        String higestPriorityRole = Role.getHighestPriorityRole(roleMap.keySet());
        return roleMap.get(higestPriorityRole);

    }

    /**
     * Returns the username of the currently active user, or null if there isn't one.
     */
    public String currentUsername() {
        User currentUser = currentUser();
        return currentUser == null ? null : currentUser.getUsername();
    }

    /**
     * Forces loading the current user from the database.
     * @return the loaded user.
     */
    public User loadCurrentUser() {
        User currentUser = currentUser();
        if (currentUser != null) {
            return find(currentUser.getUsername());
        }
        else {
            return null;
        }
    }

    @LogMetrics
    public String getUserFullName(String username) {
        if (username == null) {
            return null;
        }

        if (username.equals(SYSTEM_USER_NAME)) {
            return SYSTEM_USER_NAME;
        }

        User user = null;
        try {
            user = get(username);
        }
        catch (NotFoundException e) {
            log.info(String.format("user %s not found", username));
            // do nothing user could have been removed.
        }

        return  user == null ? username : user.getFullName();
    }

    /**
     * @param username
     * @throws NotFoundException if the given username if not found.
     */
    public CreatePasswordResetTokenResponse createPasswordResetToken(String username) {
        User user = userRepository.findById(username).orElse(null);

        // only send the message if we recognise the user
        if (user != null) {

            byte[] randomBytes = new byte[16];
            new SecureRandom().nextBytes(randomBytes);
            String token = String.valueOf(Hex.encode(randomBytes));

            String hashedToken = passwordEncoder.encode(token);

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(hashedToken);
            passwordResetToken.setUsername(username);
            passwordResetToken.setExpiryDate(environment.now().plusMinutes(passwordTokenExpiry));
            passwordResetTokenRepository.save(passwordResetToken);

            Email email = emailService.sendPasswordResetEmail(user, passwordResetToken.getId(), token);

            return new CreatePasswordResetTokenResponse(email.getId());
        } else {
            auditFailedUserSecurityActivity(username, "attempt to reset password for non-existing user");
            return null;
        }

    }

    /**
     * @throws NotFoundException if no password reset entity associated with the given token is found.
     * @throws ValidationException if the reset token is expired.
     */
    @Transactional(dontRollbackOn = {NotFoundException.class, ValidationException.class})
    public PasswordResetToken getPasswordResetToken(Integer id, String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findById(id).orElse(null);
        if (passwordResetToken == null) {
            auditFailedUserSecurityActivity("anonymous", "token with id "+id+" not found");
            throw new NotFoundException();
        }

        if (!passwordEncoder.matches(token, passwordResetToken.getToken())) {
            auditFailedUserSecurityActivity(passwordResetToken.getUsername(), "token mismatch");
            throw new NotFoundException();
        }

        if (passwordResetToken.getExpiryDate().isBefore(environment.now())) {
            auditFailedUserSecurityActivity(passwordResetToken.getUsername(), "password reset token expired");
            throw new ValidationException("password reset token expired");
        }

        if (passwordResetToken.isUsed()) {
            auditFailedUserSecurityActivity(passwordResetToken.getUsername(), "password reset token already used");
            throw new ValidationException("password reset token already used");
        }

        return passwordResetToken;
    }

    @Transactional(dontRollbackOn = {NotFoundException.class, ValidationException.class})
    public void resetUserPassword(String username, UserPasswordReset userPasswordReset) {
        PasswordResetToken passwordResetToken = getPasswordResetToken(userPasswordReset.getId(), userPasswordReset.getToken());

        if (!passwordResetToken.getUsername().equals(username)) {
            auditFailedUserSecurityActivity(username, "username does not match while resetting password");
            throw new ValidationException("username does not match");
        }

        User user = find(passwordResetToken.getUsername());
        saveNewPassword(user, userPasswordReset.getPassword(), false);

        auditService.auditActivityForUser(username, "Password reset using token");

        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    private void auditFailedUserSecurityActivity(String username, String message) {
        String sourceIp = getRequestIp();
        auditService.auditActivityForUser(username, message+(sourceIp != null ? (" from "+sourceIp) : ""));
    }

    /**
     * Changes the password for the user and saves the updated user entity to the database.
     * Weak passwords will be rejected with a ValidationException thrown.
     */
    public void saveNewPassword(User user, String newPassword, boolean audit) {
        checkPasswordStrength(newPassword);
        if (audit) {
            auditService.auditCurrentUserActivity("Password changed for user " + user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);
    }

    void checkPasswordStrength(String newPassword) {
        if (blackList != null && blackList.contains(newPassword.toLowerCase())) {
            throw new ValidationException("password","The specified password is not valid. Please choose another.");
        }

        if (passwordStrength(newPassword).getScore() < minimumPasswordStrength) {
            throw new ValidationException("password","The specified password is not strong enough");
        }
        if (newPassword.length() < minimumPasswordLength) {
            throw new ValidationException("password","The specified password is not long enough");
        }
    }

    public void assignRole(String username, String roleName, Integer organisationId) {
        if (!Role.availableRoles().contains(roleName)) {
            throw new ValidationException("unknown role: "+roleName);
        }

        // need to re-inflate current user.
        User currentUser = currentUser();
        if (currentUser != null) {
            currentUser = userRepository.findById(currentUser.getUsername()).orElse(null);
        }

        if (currentUser == null) { // unlike outside test environment
            throw new ValidationException("Unable to identify user.");
        }

        User userToUpdate = find(username);

        Organisation organisation = organisationService.find(organisationId);

        // if not a OPS Admin we need to check we're of the same organisation.
        if (!currentUser.hasRole(OPS_ADMIN)) {
            if (!currentUser.getOrganisations().contains(organisation) || userToUpdate.getRole(organisation) == null) {
                throw new ValidationException("roleName ", String.format("User %s is not a member of the organisation: %s.", username, organisation.getName()));
            }
        }

        userToUpdate.getRoles().removeIf(r -> r.getOrganisation().equals(organisation));
        userToUpdate.addApprovedRole(roleName, organisation);
        userRepository.save(userToUpdate);

        if (shouldAutoSubscribeToOrganisation(username, roleName, organisationId)) {
            notificationService.subscribe(username, EntityType.organisation, organisationId);
        }

        organisationService.updateOrganisationUserRegStatus(organisation);

        if (!permissionService.userHasPermissionForOrganisation(userToUpdate, AUTHORISE_PAYMENT.getPermissionKey(), organisationId)) {
            clearFinanceThreshold(username, organisationId);
        }

        auditService.auditCurrentUserActivity(String.format("Role %s assigned to user %s", roleName, username));
    }

    public boolean shouldAutoSubscribeToOrganisation(String username, String roleName, Integer organisationId){
        return Stream.of(GLA_ORG_ADMIN, ORG_ADMIN).anyMatch(r -> r.equals(roleName)) &&
                !notificationService.isSubscribed(username, EntityType.organisation, organisationId);
    }

    public Strength passwordStrength(String password) {
        return zxcvbn.measure(password);
    }

    public void updateSuccessfulUserLogon(User user) {
        user.setLastLoggedOn(environment.now());
        userRepository.save(user);
    }

    public String getSystemUserName() {
        return SYSTEM_USER_NAME;
    }

    public void deleteUser(String username) {
        userRepository.deleteById(username);
    }

    public void deleteUserIfExists(String username) {
        if (userRepository.existsById(username)) {
            deleteUser(username);
        }
    }


    public UserOrgFinanceThreshold createPendingThreshold(String username, Integer orgId, Long pendingThreshold) {
        User user = this.find(username);
        User currentUser = currentUser();
        Organisation organisation = organisationService.find(orgId);


        if (!user.getOrganisations().contains(organisation)) {
            throw new ValidationException("Unable to assign threshold to organisation the user is not a member of");
        }

        if (!organisation.isManagingOrganisation()) {
            throw new ValidationException("Unable to assign threshold to organisation that is not a managing organisation");
        }

        UserOrgFinanceThreshold existing = userOrgFinanceThresholdRepository.findById(new UserOrgKey(username, orgId)).orElse(null);


        if (existing == null) {
            existing = new UserOrgFinanceThreshold(username, orgId);
        }

        auditService.auditCurrentUserActivity(String.format("User %s created a pending threshold of %d for org: %d for user %s",
                currentUser.getUsername(), pendingThreshold, orgId, username ),
                ActivityType.Requested, username, orgId, new BigDecimal(pendingThreshold));


        existing.setPendingThreshold(pendingThreshold == null ? 0L : pendingThreshold);
        existing.setRequesterUsername(currentUser.getUsername());

        existing = userOrgFinanceThresholdRepository.save(existing);

        return existing;

    }

    public UserOrgFinanceThreshold getFinanceThreshold(String username, Integer orfId) {
        return userOrgFinanceThresholdRepository.findById(new UserOrgKey(username, orfId)).orElse(null);
    }

    public Set<UserOrgFinanceThreshold> getFinanceThresholds(String username) {
        User user = this.get(username);
        Set<Role> approvedRoles = user.getApprovedRoles();
        Set<UserOrgFinanceThreshold> thresholds = userOrgFinanceThresholdRepository.findByIdUsername(username);


        for (Role approvedRole : approvedRoles) {
            if (approvedRole.getOrganisation().isManagingOrganisation() && approvedRole.isThresholdRole()) {
                boolean found = false;
                for (UserOrgFinanceThreshold threshold : thresholds) {
                    if (threshold.getId().getOrganisationId().equals(approvedRole.getOrganisation().getId())) {
                        found = true;
                    }
                }

                if (!found) { // if none existing create placeholder
                    thresholds.add(new UserOrgFinanceThreshold(username, approvedRole.getOrganisation().getId()));
                }
            }
        }
        return thresholds;
    }

    public UserOrgFinanceThreshold approvePendingThreshold(String username, Integer orgId) {
        UserOrgFinanceThreshold existing = userOrgFinanceThresholdRepository.findById(new UserOrgKey(username, orgId)).orElse(null);
        User currentUser = currentUser();

        validateThresholdApproval(existing, currentUser);

        auditService.auditCurrentUserActivity(String.format(
                "Approved a pending threshold for %s of %d for org: %d, that was originally requested by %s",
                username, existing.getPendingThreshold(), orgId, existing.getRequesterUsername()),
                ActivityType.Approved,
                username, orgId, new BigDecimal(existing.getPendingThreshold()));

        existing.setApprovedThreshold(existing.getPendingThreshold());
        existing.setApproverUsername(currentUser.getUsername());
        existing.setRequesterUsername(null);
        existing.setPendingThreshold(null);

        User requester = this.get(existing.getId().getUsername());
        Organisation organisation = organisationService.find(orgId);

        Map<String, Object> model = new HashMap<String, Object>() {{
            put("organisation", organisation);
            put("approvedThreshold", String.format("%,d", existing.getApprovedThreshold()));
        }};

        notificationService.createNotification(PendingSpendAuthorityThresholdApproval, requester, model);

        return userOrgFinanceThresholdRepository.save(existing);
    }

    private void validateThresholdApproval(UserOrgFinanceThreshold existing, User currentUser) {
        if (existing == null || existing.getPendingThreshold() == null) {
            throw new ValidationException("Unable to approve threshold as no existing threshold found.");
        }

        if (existing.getRequesterUsername() == null) {
            throw new ValidationException("Unable to approve threshold as no requester found.");
        }

        User requester = this.get(existing.getRequesterUsername());
        if (requester == null) {
            throw new ValidationException("Unable to approve threshold requester not found.");
        }

        if (currentUser == requester) {
            throw new ValidationException("Requester and approver must be different users.");
        }

        if (currentUser.getUsername().equals(existing.getId().getUsername())) {
            throw new ValidationException("User cannot approve changes to their own pending record.");
        }
    }

    public UserOrgFinanceThreshold declineThreshold(String username, Integer orgId) {
        UserOrgFinanceThreshold existing = userOrgFinanceThresholdRepository.findById(new UserOrgKey(username, orgId)).orElse(null);
        if (existing == null || existing.getPendingThreshold() == null) {
            throw new ValidationException("Unable to decline pending threshold as no existing threshold found.");
        }

        auditService.auditCurrentUserActivity(String.format(
                "Declined a pending threshold for %s of %d for org: %d, that was originally requested by %s",
                username, existing.getPendingThreshold(), orgId, existing.getRequesterUsername()),
                ActivityType.Declined, username, orgId, null);

        existing.setPendingThreshold(null);
        existing.setRequesterUsername(null);
        return userOrgFinanceThresholdRepository.save(existing);
    }

    public void clearFinanceThreshold(String username, Integer orgId) {
        UserOrgFinanceThreshold existing = userOrgFinanceThresholdRepository.findById(new UserOrgKey(username, orgId)).orElse(null);
        if (existing != null) {
            existing.clear();
            userOrgFinanceThresholdRepository.save(existing);
        }
    }

    public void setPrimaryOrgForUser(String username, Integer orgId, String role) {
        User user = get(username);
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
            throw new ValidationException("Unable to find approved role in primary organisation for user "+username);
        }
        userRepository.save(user);
    }
}
