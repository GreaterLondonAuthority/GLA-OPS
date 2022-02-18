/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.accesscontrol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

import org.jetbrains.annotations.Nullable;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.project.Project;

/**
 * Determines which organisation has access to the project and the level of access.
 */
@Entity
public class ProjectAccessControl implements Serializable, ProjectAccessControlInterface {

    @EmbeddedId
    private ProjectAccessControlId id;

    @JsonIgnore
    @JoinData(sourceTable = "project_access_control", sourceColumn = "project_id", targetTable = "project",
        targetColumn = "id", joinType = Join.JoinType.ManyToOne, comment = "part of compound primary key.")
    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @JsonIgnore
    @JoinData(sourceTable = "project_access_control", sourceColumn = "organisation_id", targetTable = "organisation",
        targetColumn = "id", joinType = Join.JoinType.ManyToOne, comment = "part of compound primary key.")
    @MapsId("organisationId")
    @ManyToOne(fetch = FetchType.LAZY)
    private OrganisationEntity organisation;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(name = "grant_access_trigger")
    private GrantAccessTrigger grantAccessTrigger;

    public ProjectAccessControl() {
    }

    public ProjectAccessControl(Project project, Integer organisationId, AccessControlRelationshipType relationshipType,
        GrantAccessTrigger grantAccessTrigger) {
        this.id = new ProjectAccessControlId(project.getId(), organisationId, relationshipType);
        this.project = project;
        this.grantAccessTrigger = grantAccessTrigger;
    }

    public ProjectAccessControl(Project project, OrganisationEntity organisation, AccessControlRelationshipType relationshipType,
                                GrantAccessTrigger grantAccessTrigger) {
        this.id = new ProjectAccessControlId(project.getId(), organisation.getId(), relationshipType);
        this.project = project;
        this.organisation = organisation;
        this.grantAccessTrigger = grantAccessTrigger;
    }

    public ProjectAccessControlId getId() {
        return id;
    }

    public void setId(ProjectAccessControlId id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public OrganisationEntity getOrganisation() {
        return organisation;
    }

    @Override
    public Integer getOrganisationId() {
        return organisation != null ? organisation.getId() : null;
    }

    public void setOrganisation(OrganisationEntity organisation) {
        this.organisation = organisation;
    }

    public GrantAccessTrigger getGrantAccessTrigger() {
        return grantAccessTrigger;
    }

    public void setGrantAccessTrigger(
        GrantAccessTrigger grantAccessTrigger) {
        this.grantAccessTrigger = grantAccessTrigger;
    }

    @Nullable
    @Override
    public AccessControlRelationshipType getRelationshipType() {
        return id != null ? id.getRelationshipType() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectAccessControl that = (ProjectAccessControl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
