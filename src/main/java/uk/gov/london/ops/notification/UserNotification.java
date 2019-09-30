/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.domain.user.User;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity(name="user_notification")
public class UserNotification implements OpsEntity<Integer> {

    public enum Status { Active, Deleted }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_notification_seq_gen")
    @SequenceGenerator(name = "user_notification_seq_gen", sequenceName = "user_notification_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name="username")
    private String username;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Column(name="time_read")
    private OffsetDateTime timeRead;

    @Column(name="created_by", updatable = false)
    private String createdBy;

    @Column(name="created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name="modified_by")
    private String modifiedBy;

    @Column(name="modified_on")
    private OffsetDateTime modifiedOn;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private Status status = Status.Active;


    public UserNotification() {}

    public UserNotification(String username, Notification notification) {
        this.username = username;
        this.notification = notification;
    }

    public UserNotification(User user, Notification notification) {
        this(user.getUsername(), notification);
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public OffsetDateTime getTimeRead() {
        return timeRead;
    }

    public void setTimeRead(OffsetDateTime timeRead) {
        this.timeRead = timeRead;
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

}
