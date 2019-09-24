/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

import static uk.gov.london.common.user.BaseRole.*;

@RestController
@RequestMapping("/api/v1")
@Api(description="email api")
public class EmailAPI {

    @Autowired
    EmailService emailService;

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/emails/{id}", method = RequestMethod.GET)
    public Email get(@PathVariable Integer id) {
        return emailService.find(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/emails/test", method = RequestMethod.GET)
    public String test(@RequestParam String to, @RequestParam String subject, @RequestParam String body, @RequestParam(required = false) boolean sendSync) throws Exception {
        if (sendSync) {
            return sendMailSynchronously(to, subject, body);
        }
        else {
            emailService.saveEmail(to, subject, body);
            return "Request accepted";
        }
    }

    private String sendMailSynchronously(String to, String subject, String body) throws IOException, MessagingException {
        Session session = emailService.createSession();
        Transport transport = session.getTransport("smtp");
        transport.connect();

        MimeMessage mail = emailService.toJavaMail(new Email(to, subject, body, null), session);

        transport.sendMessage(mail, mail.getAllRecipients());

        transport.close();

        return "Sent synchronously";
    }

}
