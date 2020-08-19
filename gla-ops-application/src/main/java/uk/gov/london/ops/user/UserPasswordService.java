/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.notification.Email;
import uk.gov.london.ops.notification.EmailService;
import uk.gov.london.ops.user.domain.CreatePasswordResetTokenResponse;
import uk.gov.london.ops.user.domain.PasswordResetToken;
import uk.gov.london.ops.user.domain.User;
import uk.gov.london.ops.user.domain.UserPasswordReset;
import uk.gov.london.ops.user.implementation.PasswordResetTokenRepository;

import javax.transaction.Transactional;
import java.security.SecureRandom;
import java.util.List;

import static uk.gov.london.common.GlaUtils.getRequestIp;

@Transactional
@Service
public class UserPasswordService {

    @Autowired
    AuditService auditService;

    @Autowired
    EmailService emailService;

    @Autowired
    UserService userService;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    Environment environment;

    @Value("${password.minimum.complexity}")
    public int minimumPasswordStrength;

    @Value("${password.minimum.length}")
    public int minimumPasswordLength;

    Zxcvbn zxcvbn = new Zxcvbn();   // Password strength utility

    @Value("#{'${invalid.passwords}'.split(',')}")
    List<String> blackList;

    /** Password token expiry in minutes. Default is 24h. */
    @Value("${password.token.expiry}")
    private final long passwordTokenExpiry = 24 * 60;

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public Strength passwordStrength(String password) {
        return zxcvbn.measure(password);
    }

    /**
     * @throws NotFoundException if the given username if not found.
     */
    CreatePasswordResetTokenResponse createPasswordResetToken(String username) {
        User user = userService.get(username);

        if (user == null) {
            auditFailedUserSecurityActivity(username, "attempt to reset password for non-existing user");
            return null;
        }
        else {
            return createPasswordResetToken(user);
        }
    }

    CreatePasswordResetTokenResponse createPasswordResetToken(User user) {
        Email email;
        if (!user.isEnabled()) {
            auditFailedUserSecurityActivity(user.getUsername(), "attempt to reset password for disabled user");
            email = emailService.sendDisabledUserPasswordResetAttemptEmail(user);
        }
        else {
            String token = createRandomPasswordResetToken();
            PasswordResetToken passwordResetToken = storePasswordResetToken(token, user);
            email = emailService.sendPasswordResetEmail(user, passwordResetToken.getId(), token);
        }
        return new CreatePasswordResetTokenResponse(email.getId());
    }

    private String createRandomPasswordResetToken() {
        byte[] randomBytes = new byte[16];
        new SecureRandom().nextBytes(randomBytes);
        return String.valueOf(Hex.encode(randomBytes));
    }

    private PasswordResetToken storePasswordResetToken(String token, User user) {
        String hashedToken = passwordEncoder.encode(token);

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(hashedToken);
        passwordResetToken.setUsername(user.getUsername());
        passwordResetToken.setExpiryDate(environment.now().plusMinutes(passwordTokenExpiry));
        passwordResetTokenRepository.save(passwordResetToken);

        return passwordResetToken;
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

        User user = userService.find(passwordResetToken.getUsername());
        setNewPassword(user, userPasswordReset.getPassword(), false);

        auditService.auditActivityForUser(username, "Password reset using token");

        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    public void setPassword(String username, String password) {
        User user = userService.find(username);
        setNewPassword(user, password, true);
    }

    /**
     * Changes the password for the user and saves the updated user entity to the database.
     * Weak passwords will be rejected with a ValidationException thrown.
     */
    public void setNewPassword(User user, String newPassword, boolean audit) {
        checkPasswordStrength(newPassword);
        if (audit) {
            auditService.auditCurrentUserActivity("Password changed for user " + user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
    }

    void auditFailedUserSecurityActivity(String username, String message) {
        String sourceIp = getRequestIp();
        auditService.auditActivityForUser(username, message+(sourceIp != null ? (" from "+sourceIp) : ""));
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

    public void deleteAllPasswordResetTokenEntry() {
        passwordResetTokenRepository.deleteAll();
    }

}
