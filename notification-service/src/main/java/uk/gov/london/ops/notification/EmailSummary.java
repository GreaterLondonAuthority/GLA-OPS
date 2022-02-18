/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import com.querydsl.core.annotations.QueryEntity;
import java.time.OffsetDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import uk.gov.london.ops.framework.jpa.NonJoin;

@Entity(name = "v_email_summaries")
@QueryEntity
@NonJoin("Summary entity, does not provide join information")
public class EmailSummary {

    @Id
    private Integer id;
    private String recipient;
    private String subject;
    private String body;
    private OffsetDateTime date;
    private String status = "Ready";
    private Integer attempts = 0;

    public EmailSummary() {
    }

    public EmailSummary(Integer id, String recipient, String subject, String body, OffsetDateTime date, String status,
            Integer attempts) {
        this.id = id;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.date = date;
        this.status = status;
        this.attempts = attempts;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }
}
