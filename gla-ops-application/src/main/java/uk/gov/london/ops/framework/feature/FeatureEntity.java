/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.feature;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.user.domain.User;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity(name = "feature")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FeatureEntity {

    @Id
    @Column
    private String name;

    @Column
    private boolean enabled;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "modified_by")
    private User modifier;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    public FeatureEntity() {}

    public FeatureEntity(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public String getId() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifier != null ? modifier.getUsername() : null;
    }

    public void setModifiedBy(User modifier) {
        this.modifier = modifier;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getModifiedByName() {
        return modifier != null ? modifier.getFullName() : null;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

}
