/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by chris on 21/08/2017.
 */
@Embeddable
public class ProgrammeOrganisationID implements Serializable {

    @Column(name = "programme_id", nullable = false)
    private Integer programmeId;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    public ProgrammeOrganisationID() {
    }

    public ProgrammeOrganisationID(Integer programmeId, Integer orgId) {
        this.programmeId = programmeId;
        this.orgId = orgId;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgrammeOrganisationID that = (ProgrammeOrganisationID) o;

        if (programmeId != null ? !programmeId.equals(that.programmeId) : that.programmeId != null) return false;
        return orgId != null ? orgId.equals(that.orgId) : that.orgId == null;
    }

    @Override
    public int hashCode() {
        int result = programmeId != null ? programmeId.hashCode() : 0;
        result = 31 * result + (orgId != null ? orgId.hashCode() : 0);
        return result;
    }

}
