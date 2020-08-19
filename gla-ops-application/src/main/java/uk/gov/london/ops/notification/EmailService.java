/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.domain.ScheduledTask;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.framework.clusterlock.ClusterLock;
import uk.gov.london.ops.framework.clusterlock.ClusterLockService;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.notification.implementation.EmailRepository;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.service.ScheduledTaskService;
import uk.gov.london.ops.user.domain.User;

@Transactional
@Service
public class EmailService implements InfoContributor {

    Logger log = LoggerFactory.getLogger(getClass());

    static final String TASK_KEY = "SMTP_SEND";
    static final String EMAIL_LOCK = "EMAIL_LOCK";

    @Value("${smtp.host.primary}")
    String smtpHostPrimary;

    @Value("${smtp.host.secondary}")
    String smtpHostSecondary;

    @Value("${smtp.port}")
    String smtpPort;

    @Value("${email.sending.batch.size}")
    int emailSendingBatchSize = 100;

    int timeout = 60000;

    @Value("${sender.email}")
    String senderEmail;

    @Value("${app.url}")
    String glaOpsUrl;

    String ssl = "true";

    @Autowired
    EmailRepository emailRepository;

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

    OffsetDateTime lastSendTime = null;
    OffsetDateTime lastPollTime = null;
    Boolean lastRunSucessful = null;

    public void sendEmail(NotificationType notificationType, User user, Organisation organisation) {
        String body = generateBody(notificationType.getEmailTemplate(), user, organisation);
        saveEmail(user.getUsername(), notificationType.getEmailSubject(), body);
    }

    private void sendEmail(Email email, Session session, Transport transport) throws MessagingException, IOException {
        log.debug("about to send email {}", email.getId());
        MimeMessage mail = toJavaMail(email, session);
        transport.sendMessage(mail, mail.getAllRecipients());
    }

    public void sendRegistrationRequestEmail(NotificationType notificationType, User requester, User recipient,
        Organisation organisation) {
        String body = generateBody(notificationType.getEmailTemplate(), recipient, requester, organisation);
        saveEmail(recipient.getUsername(), notificationType.getEmailSubject(), body);
    }

    public void sendPaymentAcknowledgementEmail(Map<String, Object> model) {
        String body = generateBody("payment.acknowledgement.template.html", null, model);
        saveEmail(model.get("recipient").toString(), model.get("invoiceNumber").toString(), body);
    }

    public Email sendPasswordResetEmail(User user, Integer id, String token) {
        Map<String, Object> model = new HashMap<>();
        model.put("id", id);
        model.put("token", token);
        String body = generateBody("password.reset.template.html", user, model);
        return saveEmail(user.getUsername(), "GLA-OPS Password Reset", body);
    }

    public void sendUserStatusUpdatedEmail(User user) {
        String templateName = user.isEnabled() ? "user.reactivation.template.html" : "user.deactivation.template.html";
        String subject = user.isEnabled() ? "GLA-OPS Account Reactivation" : "GLA-OPS Account Deactivation";
        String body = generateBody(templateName, user, null);
        saveEmail(user.getUsername(), subject, body);
    }

    public Email sendDisabledUserPasswordResetAttemptEmail(User user) {
        String body = generateBody("user.deactivation.template.html", user, null);
        return saveEmail(user.getUsername(), "GLA-OPS Password Reset", body);
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

    String generateBody(String templateName, User user, Object model) {
        return generateBody(templateName, null, user, model);
    }

    String generateBody(String templateName, User recipient, User user, Object model) {
        try {
            Template template = new Handlebars().compileInline(IOUtils.toString(
                this.getClass().getResourceAsStream(templateName), UTF_8));

            Context.Builder context = Context.newBuilder(model)
                .combine("recipient", recipient)
                .combine("user", user)
                .combine("glaOpsUrl", glaOpsUrl);
            if (model instanceof Organisation) {
                context = context.combine("organisation", model);
            }
            context.resolver(JavaBeanValueResolver.INSTANCE, MapValueResolver.INSTANCE);

            return template.apply(context.build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Email saveEmail(String recipient, String subject, String body) {
        return saveEmail(recipient, subject, body, null, null);
    }

    private Email saveEmail(String recipient, String subject, String body, String fileName, String filePayload) {
        if (StringUtils.isEmpty(recipient)) {
            log.error("attempt to send an email with no recipient and subject [{}] and body [{}]", subject, body);
            return null;
        }

        Email email = new Email(recipient, subject, body, environment.now());

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
                List<Email> emailsToBeSent = findAllEmailsToBeSent();
                processPendingEmails(emailsToBeSent);
            } else {
                scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "Could not get lock: " + EMAIL_LOCK);
                lastRunSucessful = Boolean.FALSE;
            }
        } finally {
            clusterLockService.releaseLock(ClusterLock.Type.EMAIL);
        }
    }

    private void processPendingEmails(List<Email> emailsToBeSent) throws MessagingException {
        if (emailsToBeSent.isEmpty()) {
            log.debug("no email to be sent");
            scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "No email to send");
        } else {
            log.debug("about to send pending emails ...");
            Session session = createSession();
            Transport transport = session.getTransport("smtp");
            transport.connect();

            Throwable sendError = null;
            int emailsSent = 0;

            for (Email email : emailsToBeSent) {
                if (StringUtils.isEmpty(email.getRecipient())) {
                    log.error("attempt to send an email with no recipient, with subject [{}] and body [{}]",
                        email.getSubject(), email.getBody());
                } else {
                    try {
                        email.incrementNbAttempts();
                        sendEmail(email, session, transport);
                        email.setStatus(Email.Status.Sent);
                        emailsSent++;
                    } catch (Exception e) {
                        log.error("[{}] unable to send email for [{}], with subject [{}]",
                            email.getNbAttempts(), email.getRecipient(), email.getSubject(), e);
                        sendError = e;
                    }
                    performJdbcEmailUpdateAfterSendingAttempt(email);
                }
            }
            transport.close();
            lastSendTime = environment.now();

            if (sendError == null) {
                scheduledTaskService.update(TASK_KEY, ScheduledTask.SUCCESS, "Emails sent: " + emailsSent);
                lastRunSucessful = Boolean.TRUE;
            } else {
                scheduledTaskService.update(TASK_KEY, sendError);
                lastRunSucessful = Boolean.FALSE;
            }
            log.debug("finished sending pending emails.");
        }
    }

    private void markReadyEmailsAsToggledOff() {
        log.debug("Updating email statuses to " + Email.Status.ToggledOff + " as emails sending feature is off ...");
        jdbcTemplate.update("update email set status = ? where status = ?",
            Email.Status.ToggledOff.name(), Email.Status.Ready.name());
    }

    private List<Email> findAllEmailsToBeSent() {
        return emailRepository.findAllByStatus(Email.Status.Ready, PageRequest.of(0, emailSendingBatchSize));
    }

    private void performJdbcEmailUpdateAfterSendingAttempt(Email email) {
        jdbcTemplate.update("update email set status = ?, nb_attempts = ? where id = ?",
            email.getStatus().name(), email.getNbAttempts(), email.getId());
    }

    public Session createSession() {
        // See http://www.oracle.com/technetwork/java/javamail/faq/index.html#commonmistakes
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", smtpHostPrimary);
        properties.put("mail.smtp.port", smtpPort);
        //properties.put("mail.smtp.ssl.enable", ssl);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.connectiontimeout", timeout);
        properties.put("mail.smtp.timeout", timeout);
        properties.put("mail.smtp.writetimeout", timeout);
        return Session.getInstance(properties);
    }

    public MimeMessage toJavaMail(Email email, Session session) throws IOException, MessagingException {
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
            if (fileContent != null) {
                helper.addAttachment(attachment.getFileName(), new ByteArrayResource(fileContent));
            }
        }
        return mail;
    }

    public Email find(Integer id) {
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
        data.put("sentEmails", emailRepository.countAllByStatus(Email.Status.Sent));
        data.put("pendingEmails", emailRepository.countAllByStatus(Email.Status.Ready));
        builder.withDetail("emailService", data);
    }

}
