/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.notification;

import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity(name="notification")
public class Notification implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq_gen")
    @SequenceGenerator(name = "notification_seq_gen", sequenceName = "notification_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private NotificationType type = NotificationType.Info;

    @Column(name="text")
    private String text;

    @Column(name="actioned")
    private boolean actioned;

    @Enumerated(EnumType.STRING)
    @Column(name="target_entity_type")
    private EntityType targetEntityType;

    @Column(name="target_entity_id")
    @JoinData(joinType = Join.JoinType.Complex,
            comment = "This can reference a number of different objects depending on the target entity type field. ")
    private Integer targetEntityId;

    @Column(name="created_by", updatable = false)
    private String createdBy;

    @Column(name="created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name="modified_by")
    private String modifiedBy;

    @Column(name="modified_on")
    private OffsetDateTime modifiedOn;

    public Notification() {}

    public Notification(String text) {
        this.text = text;
    }

    public Notification(NotificationType type, String text) {
        this.type = type;
        this.text = text;
    }

    public Notification(NotificationType type, String text, EntityType targetEntityType, Integer targetEntityId) {
        this.type = type;
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

    public Integer getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(Integer targetEntityId) {
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
