/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.framework.OpsEntity;

@Entity(name = "user_notification")
public class UserNotificationEntity implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_notification_seq_gen")
    @SequenceGenerator(name = "user_notification_seq_gen", sequenceName = "user_notification_seq",
        initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name = "username")
    private String username;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "notification_id")
    private NotificationEntity notification;

    @Column(name = "time_read")
    private OffsetDateTime timeRead;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserNotificationStatus status = UserNotificationStatus.Active;


    public UserNotificationEntity() {}

    public UserNotificationEntity(String username, NotificationEntity notification) {
        this.username = username;
        this.notification = notification;
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

    public NotificationEntity getNotification() {
        return notification;
    }

    public void setNotification(NotificationEntity notification) {
        this.notification = notification;
    }

    public OffsetDateTime getTimeRead() {
        return timeRead;
    }

    public void setTimeRead(OffsetDateTime timeRead) {
        this.timeRead = timeRead;
    }

    public UserNotificationStatus getStatus() {
        return status;
    }

    public void setStatus(UserNotificationStatus status) {
        this.status = status;
    }

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
