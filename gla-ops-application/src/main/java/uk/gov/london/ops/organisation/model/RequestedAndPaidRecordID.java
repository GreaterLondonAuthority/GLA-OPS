/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by chris on 21/08/2017.
 */
@Embeddable
public class RequestedAndPaidRecordID implements Serializable {

    @Column(name = "programme_id", nullable = false)
    private Integer programmeId;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Column(name = "strategic_project", nullable = false)
    private Boolean strategicProject;

    public RequestedAndPaidRecordID() {
    }

    public RequestedAndPaidRecordID(Integer programmeId, Integer orgId, Boolean strategicProject) {
        this.programmeId = programmeId;
        this.orgId = orgId;
        this.strategicProject = strategicProject;
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

    public Boolean getStrategicProject() {
        return strategicProject;
    }

    public void setStrategicProject(Boolean strategicProject) {
        this.strategicProject = strategicProject;
    }
}
