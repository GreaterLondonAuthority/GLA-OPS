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
@Entity(name = "v_payments_and_requested_grant_values")
public class RequestedAndPaidRecord {

    @EmbeddedId
    @JsonIgnore
    private RequestedAndPaidRecordID id;

    @Column(name = "project_count")
    Integer projectCount;

    @Column(name = "dpf_requested")
    Long dpfRequested;

    @Column(name = "grant_requested")
    Long grantRequested;

    @Column(name = "rcgf_requested")
    Long rcgfRequested;

    @Column(name = "dpf_approved")
    Long dpfApproved;

    @Column(name = "grant_approved")
    Long grantApproved;

    @Column(name = "rcgf_approved")
    Long rcgfApproved;

    @Transient
    Long indicativeGrantRequested;

    @Transient
    Long indicativeGrantApproved;

    @Column(name = "project_requested_total")
    Long projectRequestedTotal;

    @Column(name = "project_approved_total")
    Long projectApprovedTotal;

    @Column(name = "total_requested",  updatable = false)
    Long totalRequested;

    @Column(name = "total_approved",  updatable = false)
    Long totalApproved;

    @Column(name = "rcgf_paid")
    Long rcgfPaid;

    @Column(name = "dpf_paid")
    Long dpfPaid;

    @Column(name = "grant_paid")
    Long grantPaid;

    @Column(name = "payment_total")
    Long totalPaid;

    public RequestedAndPaidRecord() {
    }

    public RequestedAndPaidRecord(RequestedAndPaidRecordID id) {
        this.id = id;
    }

    public Integer getProgrammeId() {
        return id.getProgrammeId();
    }

    public Integer getOrganisationId() {
        return id.getOrgId();
    }

    public RequestedAndPaidRecordID getId() {
        return id;
    }

    public Integer getProjectCount() {
        return projectCount;
    }

    public Long getDpfRequested() {
        return dpfRequested;
    }

    public Long getGrantRequested() {
        return grantRequested;
    }

    public Long getRcgfRequested() {
        return rcgfRequested;
    }

    public Long getIndicativeGrantRequested() {
        return indicativeGrantRequested;
    }

    public Long getProjectRequestedTotal() {
        return projectRequestedTotal;
    }

    public Long getTotalRequested() {
        return totalRequested;
    }

    public Long getRcgfPaid() {
        return rcgfPaid;
    }

    public Long getDpfPaid() {
        return dpfPaid;
    }

    public Long getGrantPaid() {
        return grantPaid;
    }

    public Long getTotalPaid() {
        return totalPaid;
    }

    public Long getDpfApproved() {
        return dpfApproved;
    }

    public Long getGrantApproved() {
        return grantApproved;
    }

    public Long getRcgfApproved() {
        return rcgfApproved;
    }

    public Long getIndicativeGrantApproved() {
        return indicativeGrantApproved;
    }

    public Long getTotalApproved() {
        return totalApproved;
    }

    public Long getProjectApprovedTotal() {
        return projectApprovedTotal;
    }

    public void setIndicativeGrantRequested(Long indicativeGrantRequested) {
        this.indicativeGrantRequested = indicativeGrantRequested;
    }

    public void setIndicativeGrantApproved(Long indicativeGrantApproved) {
        this.indicativeGrantApproved = indicativeGrantApproved;
    }

    public void setTotalRequested(Long totalRequested) {
        this.totalRequested = totalRequested;
    }

    public void setTotalApproved(Long totalApproved) {
        this.totalApproved = totalApproved;
    }
}
