/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.framework.clusterlock.ClusterLock;
import uk.gov.london.ops.framework.clusterlock.ClusterLockService;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTask;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTaskService;
import uk.gov.london.ops.notification.implementation.repository.EmailRepository;
import uk.gov.london.ops.notification.implementation.repository.EmailSummaryRepository;
import uk.gov.london.ops.notification.implementation.repository.NotificationTypeRepository;
import uk.gov.london.ops.organisation.Organisation;
import uk.gov.london.ops.user.User;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@Transactional
@Service
public class EmailServiceImpl implements EmailService, InfoContributor {

    Logger log = LoggerFactory.getLogger(getClass());

    static final String TASK_KEY = "SMTP_SEND";
    static final String EMAIL_LOCK = "EMAIL_LOCK";

    @Value("${smtp.host.primary}")
    String smtpHostPrimary;

    @Value("${smtp.host.secondary}")
    String smtpHostSecondary;

    @Value("${smtp.port:25}")
    String smtpPort;

    @Value("${email.sending.batch.size:100}")
    int emailSendingBatchSize = 100;

    int timeout = 60000;

    @Value("${sender.email:noreply@london.gov.uk}")
    String senderEmail;

    @Value("${app.url}")
    String glaOpsUrl;

    @Value("${max.nb.email.sending.attempts:10}")
    int maxNbEmailSendingAttempts = 10;

    @Autowired
    EmailRepository emailRepository;

    @Autowired
    EmailSummaryRepository emailSummaryRepository;

    @Autowired
    FileService fileService;

    @Autowired
    Environment environment;

    @Autowired
    ScheduledTaskService scheduledTaskService;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    ClusterLockService clusterLockService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NotificationTypeRepository notificationTypeRepository;

    OffsetDateTime lastSendTime = null;
    OffsetDateTime lastPollTime = null;
    Boolean lastRunSucessful = null;

    /**
     * Returns a paged list of the email summaries on the system.
     */
    public Page<EmailSummary> getEmails(String recipient, String subject, String bodyText, Pageable pageable) {
        return emailSummaryRepository.findAll(recipient, subject, bodyText, pageable);
    }

    public void sendEmail(NotificationType notificationType, NotificationTargetEntity targetEntity, User user,
                          Organisation organisation) {
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(notificationType);
        String body = generateBody(notificationTypeEntity.getEmailTemplate(), user, organisation, targetEntity);
        saveEmail(user.getUsername(), notificationTypeEntity.getEmailSubject(), body);
    }

    private void sendEmail(EmailEntity email, Session session, Transport transport) throws MessagingException, IOException {
        log.debug("about to send email {}", email.getId());
        MimeMessage mail = toJavaMail(email, session);
        transport.sendMessage(mail, mail.getAllRecipients());
    }

    public void sendRegistrationRequestEmail(NotificationType notificationType, User requester, User recipient,
                                             Organisation organisation) {
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(notificationType);
        String body = generateBody(notificationTypeEntity.getEmailTemplate(), recipient, requester, null, organisation);
        saveEmail(recipient.getUsername(), notificationTypeEntity.getEmailSubject(), body);
    }

    public void sendPaymentAcknowledgementEmail(Map<String, Object> model) {
        String body = generateBody("payment.acknowledgement.template.html", null, model);
        saveEmail(model.get("recipient").toString(), model.get("invoiceNumber").toString(), body);
    }

    public void sendEmailNotification(NotificationType notificationType, Map<String, Object> model) {
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(notificationType);
        String body = generateBody(notificationTypeEntity.getEmailTemplate(), null, model);
        String subject = generateSubject(notificationTypeEntity.getEmailSubject(), model);
        saveEmail(model.get("recipient").toString(), subject, body);
    }

    public void sendPaymentSchedulerSummaryEmail(NotificationType notificationType, User user, Map<String, Object> model) {
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(notificationType);
        String body = generateBody(notificationTypeEntity.getEmailTemplate(), user, model);
        saveEmail(user.getUsername(), notificationTypeEntity.getEmailSubject(), body);
    }

    public Integer sendPasswordResetEmail(User user, Integer id, String token) {
        Map<String, Object> model = new HashMap<>();
        model.put("id", id);
        model.put("token", token);
        String body = generateBody("password.reset.template.html", user, model);
        EmailEntity sentEmail = saveEmail(user.getUsername(), "GLA-OPS Password Reset", body);
        return sentEmail != null ? sentEmail.getId() : null;
    }

    public void sendUserStatusUpdatedEmail(User user) {
        String templateName;

        if (!user.isEnabled() && user.wasDeactivatedBySystem()) {
            templateName = "system.user.deactivation.template.html";
        } else {
            templateName = user.isEnabled() ? "user.reactivation.template.html" : "user.deactivation.template.html";
        }

        String subject = user.isEnabled() ? "GLA-OPS Account Reactivation" : "GLA-OPS Account Deactivation";
        String body = generateBody(templateName, user, null);
        saveEmail(user.getUsername(), subject, body);
    }

    @Override
    public void sendOrgAdminRequestedEmail(User toUser, User aboutUser, Organisation organisation) {
        String body = generateBody("user.orgadmin.requested.template.html", toUser, aboutUser,  organisation, null);
        saveEmail(toUser.getUsername(), "OPS Project Editor has requested Organisation Admin role", body);
    }

    public void sendBroadcastEmail(String recipient, String recipientName, String heading,
                                   String body, String signOff, String subject) {
        String templateName = "broadcast.template.html";
        Map<String, Object> model = new HashMap<>();
        model.put("recipientName", recipientName);
        model.put("heading", heading);
        model.put("body", body);
        model.put("signOff", signOff);
        String emailBody = generateBody(templateName, null, model);
        saveEmail(recipient, subject, emailBody);
    }

    public Integer sendDisabledUserPasswordResetAttemptEmail(User user) {
        String body = generateBody("user.deactivation.template.html", user, null);
        EmailEntity sentEmail = saveEmail(user.getUsername(), "GLA-OPS Password Reset", body);
        return sentEmail != null ? sentEmail.getId() : null;
    }

    public void sendAuthorisedPaymentsReportEmail(String financeGroupEmail, OffsetDateTime day, String csvReport) {
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(day);
        String fileName = "opspaymentfile" + DateTimeFormatter.ofPattern("ddMMyyyy").format(day) + ".csv";
        saveEmail(financeGroupEmail, "OPS Payment report for GLA " + date, "<p>Daily email report for "
                + date + "</p>", fileName, csvReport);
    }

    public void sendNoAuthorisedPaymentsReportEmail(String financeGroupEmail, OffsetDateTime day) {
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(day);
        saveEmail(financeGroupEmail, "OPS Payment report for GLA " + date, "No payments for " + date);
    }

    String generateSubject(String subject, Object model) {
        try {
            return new Handlebars().compileInline(subject).apply(Context.newBuilder(model).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String generateBody(String templateName, User user, Object model) {
        return generateBody(templateName, null, user, null, model);
    }

    String generateBody(String templateName, User user, Organisation organisation, Object model) {
        return generateBody(templateName, null, user, organisation, model);
    }

    String generateBody(String templateName, User recipient, User user, Organisation organisation, Object model) {
        try {
            Template template = new Handlebars().compileInline(IOUtils.toString(
                    this.getClass().getResourceAsStream(templateName), UTF_8));

            Context.Builder context = Context.newBuilder(model)
                    .combine("recipient", recipient)
                    .combine("user", user)
                    .combine("glaOpsUrl", glaOpsUrl);
            if (model instanceof Organisation) {
                context.combine("organisation", model);
            } else if (organisation != null) {
                context.combine("organisation", organisation);
            }
            context.resolver(JavaBeanValueResolver.INSTANCE, MapValueResolver.INSTANCE);

            return template.apply(context.build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EmailEntity saveEmail(String recipient, String subject, String body) {
        return saveEmail(recipient, subject, body, null, null);
    }

    private EmailEntity saveEmail(String recipient, String subject, String body, String fileName, String filePayload) {
        if (StringUtils.isEmpty(recipient)) {
            log.error("attempt to send an email with no recipient and subject [{}] and body [{}]", subject, body);
            return null;
        }

        EmailEntity email = new EmailEntity(recipient, subject, body, environment.now());

        if (filePayload != null) {
            AttachmentFile attachmentFile = new AttachmentFile();
            attachmentFile.setFileName(fileName);
            attachmentFile.setFileSize((long) filePayload.getBytes().length);
            try {
                fileService.save(attachmentFile, new ByteArrayInputStream(filePayload.getBytes(UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            email.getEmailAttachments().add(attachmentFile);
        }

        return emailRepository.save(email);
    }

    @Scheduled(fixedDelay = 30000)
    public void processPendingEmails() throws MessagingException {
        if (smtpHostPrimary.equals("N/A")) {
            log.debug("Skipping SMTP email send as no server configured");
            scheduledTaskService.update("SMTP_SEND", ScheduledTask.SKIPPED, "No SMTP server configured");
            return;
        }

        lastPollTime = environment.now();

        if (!featureStatus.isEnabled(Feature.EmailSending)) {
            markReadyEmailsAsToggledOff();
            return;
        }

        String lockId = GlaUtils.generateRandomId();
        try {
            if (clusterLockService.takeLock(lockId, ClusterLock.Type.EMAIL)) {
                List<EmailEntity> emailsToBeSent = findAllEmailsToBeSent();
                processPendingEmails(emailsToBeSent);
            } else {
                scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "Could not get lock: " + EMAIL_LOCK);
                lastRunSucessful = Boolean.FALSE;
            }
        } finally {
            clusterLockService.releaseLock(ClusterLock.Type.EMAIL);
        }
    }

    private void processPendingEmails(List<EmailEntity> emailsToBeSent) throws MessagingException {
        if (emailsToBeSent.isEmpty()) {
            log.debug("no email to be sent");
            scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "No email to send");
        } else {
            log.debug("about to send pending emails ...");
            Session session = createSession();
            Transport transport = session.getTransport("smtp");
            transport.connect();

            EmailSendingResults emailSendingResults = processPendingEmails(emailsToBeSent, session, transport);

            transport.close();
            lastSendTime = environment.now();

            if (emailSendingResults.sendError == null) {
                scheduledTaskService.update(TASK_KEY, ScheduledTask.SUCCESS, "Emails sent: " + emailSendingResults.emailsSent);
                lastRunSucessful = Boolean.TRUE;
            } else {
                scheduledTaskService.update(TASK_KEY, emailSendingResults.sendError);
                lastRunSucessful = Boolean.FALSE;
            }
            log.debug("finished sending pending emails.");
        }
    }

    private EmailSendingResults processPendingEmails(List<EmailEntity> emailsToBeSent, Session session, Transport transport) {
        EmailSendingResults result = new EmailSendingResults();
        for (EmailEntity email : emailsToBeSent) {
            if (StringUtils.isEmpty(email.getRecipient())) {
                log.error("attempt to send an email with no recipient, with subject [{}] and body [{}]",
                        email.getSubject(), email.getBody());
                email.setStatus(EmailStatus.Failed);
            } else if (email.getNbAttempts() > maxNbEmailSendingAttempts) {
                email.setStatus(EmailStatus.Failed);
            } else {
                try {
                    email.incrementNbAttempts();
                    sendEmail(email, session, transport);
                    email.setStatus(EmailStatus.Sent);
                    result.emailsSent++;
                } catch (Exception e) {
                    log.error("[{}] unable to send email for [{}], with subject [{}]",
                            email.getNbAttempts(), email.getRecipient(), email.getSubject(), e);
                    result.sendError = e;
                }
            }
            performJdbcEmailUpdateAfterSendingAttempt(email);
        }
        return result;
    }

    private void markReadyEmailsAsToggledOff() {
        log.debug("Updating email statuses to " + EmailStatus.ToggledOff + " as emails sending feature is off ...");
        jdbcTemplate.update("update email set status = ? where status = ?",
                EmailStatus.ToggledOff.name(), EmailStatus.Ready.name());
    }

    private List<EmailEntity> findAllEmailsToBeSent() {
        return emailRepository.findAllByStatus(EmailStatus.Ready, PageRequest.of(0, emailSendingBatchSize));
    }

    private void performJdbcEmailUpdateAfterSendingAttempt(EmailEntity email) {
        jdbcTemplate.update("update email set status = ?, nb_attempts = ? where id = ?",
                email.getStatus().name(), email.getNbAttempts(), email.getId());
    }

    public Session createSession() {
        // See http://www.oracle.com/technetwork/java/javamail/faq/index.html#commonmistakes
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", smtpHostPrimary);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.connectiontimeout", timeout);
        properties.put("mail.smtp.timeout", timeout);
        properties.put("mail.smtp.writetimeout", timeout);
        return Session.getInstance(properties);
    }

    public MimeMessage toJavaMail(EmailEntity email, Session session) throws IOException, MessagingException {
        MimeMessage mail = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setFrom(new InternetAddress(senderEmail, senderEmail));
        helper.setTo(new InternetAddress(email.getRecipient()));
        helper.setSubject(email.getSubject());
        helper.setText(email.getBody(), true);

        if (email.getEmailAttachments() != null && !email.getEmailAttachments().isEmpty()) {
            AttachmentFile attachment = email.getEmailAttachments().iterator().next();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            fileService.getFileContent(attachment, out);
            byte[] fileContent = out.toByteArray();
            helper.addAttachment(attachment.getFileName(), new ByteArrayResource(fileContent));
        }
        return mail;
    }

    public EmailEntity find(Integer id) {
        return emailRepository.findById(id).orElse(null);
    }

    /**
     * Spring Boot Actuator information contributor.
     */
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> data = new TreeMap<>();
        data.put("smtpPrimary", smtpHostPrimary);
        data.put("smtpSecondary", smtpHostSecondary);
        data.put("lastSendTime", lastSendTime);
        data.put("sentEmails", emailRepository.countAllByStatus(EmailStatus.Sent));
        data.put("pendingEmails", emailRepository.countAllByStatus(EmailStatus.Ready));
        builder.withDetail("emailService", data);
    }

    static class EmailSendingResults {
        Throwable sendError = null;
        int emailsSent = 0;
    }

}
