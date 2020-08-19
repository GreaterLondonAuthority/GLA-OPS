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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MapsId;
import org.jetbrains.annotations.NotNull;

/**
 * Determines the default level of access for an organisation.
 */
@Entity
@IdClass(DefaultAccessControlId.class)
public class DefaultAccessControl implements Serializable, ProjectAccessControlInterface {

    @Id
    @Column(name = "programme_id", insertable = false, updatable = false)
    @MapsId("programme_id")
    private Integer programmeId;

    @Id
    @Column(name = "template_id", insertable = false, updatable = false)
    @MapsId("template_id")
    private Integer templateId;

    @Id
    @Column(name = "organisation_id", insertable = false, updatable = false)
    @MapsId("organisation_id")
    private Integer organisationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type")
    private AccessControlRelationshipType relationshipType = AccessControlRelationshipType.ASSOCIATED;

    public DefaultAccessControl() {
    }

    public DefaultAccessControl(Integer programmeId, Integer templateId, Integer organisationId,
        AccessControlRelationshipType relationshipType) {
        this.programmeId = programmeId;
        this.templateId = templateId;
        this.organisationId = organisationId;
        this.relationshipType = relationshipType;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    @Override
    public Integer getOrganisationId() {
        return organisationId;
    }

    @NotNull
    @Override
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
        if (!(o instanceof DefaultAccessControl)) {
            return false;
        }
        DefaultAccessControl that = (DefaultAccessControl) o;
        return programmeId.equals(that.programmeId)
            && templateId.equals(that.templateId)
            && organisationId.equals(that.organisationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programmeId, templateId, organisationId);
    }
}
