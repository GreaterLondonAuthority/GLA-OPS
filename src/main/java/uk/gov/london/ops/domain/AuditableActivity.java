/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain;

import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Record of an auditable activity by a user within the system.
 *
 * @author Steve Leach
 */
@Entity(name="audit_activity")
public class AuditableActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
    @SequenceGenerator(name = "audit_seq_gen", sequenceName = "audit_activity_seq", initialValue = 100001, allocationSize = 1)
    private Integer id = null;

    /**
     * Username for the user that performed the auditable activity.
     *
     * Note that username is NOT a foreign key into users table.
     * This is to allow audit records to remain even if the user is deleted for any reason.
     */
    @Column(name = "username")
    private String userName = null;

    @Column(name = "activity_time")
    private OffsetDateTime timestamp = null;

    @Column(name = "summary")
    private String summary = null;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private EntityType entityType;

    @Column(name = "entity_id")
    @JoinData(joinType = Join.JoinType.Complex,
            comment = "This can reference a number of different objects depending on the entity type field. ")
    private Integer entityId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ActivityType type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

}
