/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.OpsEntity;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity(name = "notification")
public class NotificationEntity implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq_gen")
    @SequenceGenerator(name = "notification_seq_gen", sequenceName = "notification_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_type")
    private NotificationSubType subType = NotificationSubType.Info;

    @Column(name = "text")
    private String text;

    @Column(name = "actioned")
    private boolean actioned;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_entity_type")
    private EntityType targetEntityType;

    @Column(name = "target_entity_id")
    @JoinData(joinType = Join.JoinType.Complex,
            comment = "This can reference a number of different objects depending on the target entity type field. ")
    private String targetEntityId;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    public NotificationEntity() {}

    public NotificationEntity(String text) {
        this.text = text;
    }

    public NotificationEntity(NotificationTypeEntity type, String text, String targetEntityId) {
        this.type = type.getType();
        this.subType = type.getSubType();
        this.text = text;
        this.targetEntityType = type.getEntityType();
        this.targetEntityId = targetEntityId;
    }

    @Deprecated
    public NotificationEntity(NotificationSubType subType, String text, EntityType targetEntityType, String targetEntityId) {
        this.subType = subType;
        this.text = text;
        this.targetEntityType = targetEntityType;
        this.targetEntityId = targetEntityId;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationSubType getSubType() {
        return subType;
    }

    public void setSubType(NotificationSubType subType) {
        this.subType = subType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isActioned() {
        return actioned;
    }

    public void setActioned(boolean actioned) {
        this.actioned = actioned;
    }

    public EntityType getTargetEntityType() {
        return targetEntityType;
    }

    public void setTargetEntityType(EntityType targetEntityType) {
        this.targetEntityType = targetEntityType;
    }

    public String getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(String targetEntityId) {
        this.targetEntityId = targetEntityId;
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
