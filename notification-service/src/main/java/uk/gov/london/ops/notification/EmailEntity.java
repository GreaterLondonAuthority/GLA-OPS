/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import uk.gov.london.ops.file.AttachmentFile;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "email")
public class EmailEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_seq_gen")
    @SequenceGenerator(name = "email_seq_gen", sequenceName = "email_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "date_sent", nullable = false)
    private OffsetDateTime dateSent;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EmailStatus status = EmailStatus.Ready;

    /** Number of times there has been an attempt to send the email. */
    @Column(name = "nb_attempts")
    private Integer nbAttempts = 0;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "email_attachment",
            joinColumns = @JoinColumn(name = "email_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id", referencedColumnName = "id"))
    Set<AttachmentFile> emailAttachments = new HashSet<>();

    public EmailEntity() {}

    public EmailEntity(String recipient, String subject, String body, OffsetDateTime dateSent) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.dateSent = dateSent;
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

    public OffsetDateTime getDateSent() {
        return dateSent;
    }

    public void setDateSent(OffsetDateTime dateSent) {
        this.dateSent = dateSent;
    }

    public EmailStatus getStatus() {
        return status;
    }

    public void setStatus(EmailStatus status) {
        this.status = status;
    }

    public Integer getNbAttempts() {
        return nbAttempts;
    }

    public void setNbAttempts(Integer nbAttempts) {
        this.nbAttempts = nbAttempts;
    }

    public void incrementNbAttempts() {
        this.nbAttempts++;
    }

    public Set<AttachmentFile> getEmailAttachments() {
        return emailAttachments;
    }

    public void setEmailAttachments(Set<AttachmentFile> emailAttachments) {
        this.emailAttachments = emailAttachments;
    }
}
