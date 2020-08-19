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

@Embeddable
public class DefaultAccessControlId implements Serializable {

    @Column(name = "programme_id")
    private Integer programmeId;

    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "organisation_id")
    private Integer organisationId;

    public DefaultAccessControlId() {
    }

    public DefaultAccessControlId(Integer programmeId, Integer templateId, Integer organisationId) {
        this.programmeId = programmeId;
        this.templateId = templateId;
        this.organisationId = organisationId;
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

    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultAccessControlId that = (DefaultAccessControlId) o;
        return Objects.equals(programmeId, that.programmeId)
            && Objects.equals(templateId, that.templateId)
            && Objects.equals(organisationId, that.organisationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programmeId, templateId, organisationId);
    }

}
