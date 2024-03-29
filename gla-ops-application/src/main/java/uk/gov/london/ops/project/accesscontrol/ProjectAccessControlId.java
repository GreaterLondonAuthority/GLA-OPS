/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.accesscontrol;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class ProjectAccessControlId implements Serializable {

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "organisation_id")
    private Integer organisationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type")
    private AccessControlRelationshipType relationshipType;

    public ProjectAccessControlId() {
    }

    public ProjectAccessControlId(Integer projectId, Integer organisationId, AccessControlRelationshipType relationshipType) {
        this.projectId = projectId;
        this.organisationId = organisationId;
        this.relationshipType = relationshipType;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    public AccessControlRelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(AccessControlRelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectAccessControlId that = (ProjectAccessControlId) o;
        return Objects.equals(projectId, that.projectId)
                && Objects.equals(organisationId, that.organisationId)
                && relationshipType == that.relationshipType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, organisationId, relationshipType);
    }

}
