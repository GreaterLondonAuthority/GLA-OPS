/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.Email;
import uk.gov.london.ops.domain.ScheduledTask;
import uk.gov.london.ops.domain.attachment.AttachmentFile;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.repository.EmailRepository;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.Lock;

@Service
@Component
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

    int timeout = 60000;

    @Value("${sender.email}")
    String senderEmail;

    @Value("${app.url}")
    String glaOpsUrl;

    @Value("${financegroup.email}")
    String financeGroupEmail;

    String ssl = "true";

    @Autowired
    EmailRepository emailRepository;

    @Autowired
    Environment environment;

    @Autowired
    JdbcLockRegistry lockRegistry;

    @Autowired
    ScheduledTaskService scheduledTaskService;

    OffsetDateTime lastSendTime = null;
    OffsetDateTime lastPollTime = null;
    Boolean lastRunSucessful = null;

    public void sendApprovalEmail(User user) {
        try {
            String body = generateBody("registration.approval.template.html", user);
            saveEmail(user.getUsername(), "GLA Open Project System registration approved", body);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to send registration approval email.", e);
        }
    }

    public void sendRejectionEmail(User user, Organisation organisation) {
        try {
            String body = generateBody("registration.rejection.template.html", user, organisation);
            saveEmail(user.getUsername(), "GLA Open Project System user registration rejected", body);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to send registration rejection email.", e);
        }
    }

    public Email sendPasswordResetEmail(User user, Integer id, String token) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("id", id);
            model.put("token", token);
            String body = generateBody("password.reset.template.html", user, model);
            return saveEmail(user.getUsername(), "GLA-OPS Password Reset", body);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to send password reset email.", e);
        }
    }

    public void sendAuthorisedPaymentsReportEmail(OffsetDateTime day, String csvReport) {
        try {
            String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(day);
            String fileName = "opspaymentfile"+DateTimeFormatter.ofPattern("ddMMyyyy").format(day)+".csv";
            saveEmail(financeGroupEmail, "OPS Payment report for GLA "+date, "<p>Daily email report for "+date+"</p>", fileName, csvReport);
        }
        catch (Exception e) {
            log.error("Unable to create authorised payments report email", e);
        }
    }

    public void sendNoAuthorisedPaymentsReportEmail(OffsetDateTime day) {
        try {
            String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(day);
            saveEmail(financeGroupEmail, "OPS Payment report for GLA "+date, "No payments for "+date);
        }
        catch (Exception e) {
            log.error("Unable to create no authorised payments report email", e);
        }
    }

    String generateBody(String templateName, User user) throws IOException {
        return generateBody(templateName, user, null);
    }

    String generateBody(String templateName, User user, Object model) throws IOException {
        Template template = new Handlebars().compileInline(IOUtils.toString(
                this.getClass().getResourceAsStream(templateName), "UTF-8"));

        Context.Builder context = Context.newBuilder(model)
                .combine("user", user)
                .combine("glaOpsUrl", glaOpsUrl)
                .resolver(JavaBeanValueResolver.INSTANCE, MapValueResolver.INSTANCE);

        return template.apply(context.build());
    }

    public Email saveEmail(String recipient, String subject, String body) {
        return saveEmail(recipient, subject, body, null, null);
    }

    private Email saveEmail(String recipient, String subject, String body, String fileName, String filePayload) {
        Email email = new Email(recipient, subject, body, environment.now());

        if (filePayload != null) {
            AttachmentFile attachmentFile = new AttachmentFile();
            attachmentFile.setFileName(fileName);
            attachmentFile.setFileContent(filePayload.getBytes());
            email.getEmailAttachments().add(attachmentFile);
        }

        return emailRepository.save(email);
    }

    @Scheduled(fixedDelay = 30000)
    public void sendPendingEmails() throws UnsupportedEncodingException, MessagingException {
        if (smtpHostPrimary.equals("N/A")) {
            log.debug("Skipping SMTP email send as no server configured");
            scheduledTaskService.update("SMTP_SEND", ScheduledTask.SKIPPED, "No SMTP server configured");
            return;
        }

        lastPollTime = environment.now();

        Lock lock = lockRegistry.obtain(EMAIL_LOCK);
        try {
            if (lock != null && lock.tryLock()) {

                List<Email> emailsToBeSent = emailRepository.findAllByStatus(Email.Status.Ready);
                if (emailsToBeSent == null || emailsToBeSent.isEmpty()) {
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
                        try {
                            log.debug("about to send email {}", email.getId());
                            email.incrementNbAttempts();
                            MimeMessage mail = toJavaMail(email, session);
                            transport.sendMessage(mail, mail.getAllRecipients());
                            email.setStatus(Email.Status.Sent);
                            emailsSent++;
                        } catch (Exception e) {
                            log.error("[{}] unable to send email for [{}], with subject [{}]", email.getNbAttempts(), email.getRecipient(), email.getSubject(), e);
                            sendError = e;
                        }

                        emailRepository.save(email);
                    }

                    transport.close();

                    lastSendTime = environment.now();

                    if (sendError != null) {
                        scheduledTaskService.update(TASK_KEY, ScheduledTask.SUCCESS, "Emails sent: " + emailsSent);
                        lastRunSucessful = Boolean.TRUE;
                    } else {
                        scheduledTaskService.update(TASK_KEY, sendError);
                        lastRunSucessful = Boolean.FALSE;
                    }

                    log.debug("finished sending pending emails.");
                }
            } else {
                scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "Could not get lock: " + EMAIL_LOCK);
                lastRunSucessful = Boolean.FALSE;
            }
        }
        finally {
            lock.unlock();
        }
    }

    public Session createSession() throws MessagingException, UnsupportedEncodingException {
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
            helper.addAttachment(attachment.getFileName(), new ByteArrayResource(attachment.getFileContent()));
        }

        return mail;
    }

    public Email find(Integer id) {
        return emailRepository.findOne(id);
    }

    /**
     * Spring Boot Actuator information contributor.
     */
    @Override
    public void contribute(Info.Builder builder) {
        Map<String,Object> data = new TreeMap<>();
        data.put("smtpPrimary", smtpHostPrimary);
        data.put("smtpSecondary", smtpHostSecondary);
        data.put("lastSendTime", lastSendTime);
        data.put("sentEmails", emailRepository.countAllByStatus(Email.Status.Sent));
        data.put("pendingEmails", emailRepository.countAllByStatus(Email.Status.Ready));
        builder.withDetail("emailService", data);
    }
}
