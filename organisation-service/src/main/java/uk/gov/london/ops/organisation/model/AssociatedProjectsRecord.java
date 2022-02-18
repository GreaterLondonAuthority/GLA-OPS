/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Project summary for use by API.
 *
 * @author Chris
 */
@Entity(name = "v_associated_projects")
public class AssociatedProjectsRecord {

    @EmbeddedId
    @JsonIgnore
    private ProgrammeOrganisationID id;

    @Column(name = "project_count")
    Integer projectCount;

    @Column(name = "strategic_requested")
    Long strategicRequested;

    @Transient
    Long requestedVariance;

    @Transient
    Long varianceBetweenPaidAndSoSClaimed;

    @Column(name = "started_on_site")
    Integer startedOnSite;

    public AssociatedProjectsRecord() {
    }

    public AssociatedProjectsRecord(ProgrammeOrganisationID id) {
        this.id = id;
    }

    public Integer getProgrammeId() {
        return id.getProgrammeId();
    }

    public Integer getOrganisationId() {
        return id.getOrgId();
    }

    public ProgrammeOrganisationID getId() {
        return id;
    }

    public void setId(ProgrammeOrganisationID id) {
        this.id = id;
    }

    public Integer getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Integer projectCount) {
        this.projectCount = projectCount;
    }

    public Long getStrategicRequested() {
        return strategicRequested;
    }

    public void setStrategicRequested(Long strategicRequested) {
        this.strategicRequested = strategicRequested;
    }

    public Integer getStartedOnSite() {
        return startedOnSite;
    }

    public void setStartedOnSite(Integer startedOnSite) {
        this.startedOnSite = startedOnSite;
    }

    public Long getRequestedVariance() {
        return requestedVariance;
    }

    public void setRequestedVariance(Long requestedVariance) {
        this.requestedVariance = requestedVariance;
    }

    public Long getVarianceBetweenPaidAndSoSClaimed() {
        return varianceBetweenPaidAndSoSClaimed;
    }

    public void setVarianceBetweenPaidAndSoSClaimed(Long varianceBetweenPaidAndSoSClaimed) {
        this.varianceBetweenPaidAndSoSClaimed = varianceBetweenPaidAndSoSClaimed;
    }
}
