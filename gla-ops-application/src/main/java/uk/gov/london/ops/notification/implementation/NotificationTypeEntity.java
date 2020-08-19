/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.implementation;

import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.notification.NotificationSubType;
import uk.gov.london.ops.notification.NotificationType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Arrays;

@Entity(name = "notification_type")
public class NotificationTypeEntity implements Serializable {

    @Id
    @Column
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column
    private String trigger;

    @Column
    private String text;

    @Column
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Column
    @Enumerated(EnumType.STRING)
    private NotificationSubType subType;

    @Column
    private String emailSubject;

    @Column
    private String emailTemplate;

    @Column(name = "roles_notified")
    private String rolesNotifiedAsString;

    public NotificationTypeEntity() {}

    NotificationTypeEntity(NotificationType type) {
        this.type = type;
        this.trigger = type.getTrigger();
        this.text = type.getText();
        this.entityType = type.getEntityType();
        this.subType = type.getSubType();
        this.emailSubject = type.getEmailSubject();
        this.emailTemplate = type.getEmailTemplate();
        this.rolesNotifiedAsString = GlaUtils.listToCsString(Arrays.asList(type.getRolesNotified()));
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public NotificationSubType getSubType() {
        return subType;
    }

    public void setSubType(NotificationSubType subType) {
        this.subType = subType;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailTemplate() {
        return emailTemplate;
    }

    public void setEmailTemplate(String emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public String getRolesNotifiedAsString() {
        return rolesNotifiedAsString;
    }

    public void setRolesNotifiedAsString(String rolesNotifiedAsString) {
        this.rolesNotifiedAsString = rolesNotifiedAsString;
    }

}
