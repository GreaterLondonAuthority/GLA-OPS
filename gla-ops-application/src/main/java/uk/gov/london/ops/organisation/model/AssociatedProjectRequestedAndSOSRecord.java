/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Project summary for use by API.
 *
 * @author Chris
 */
@Entity(name = "v_associated_sos_and_requested")
public class AssociatedProjectRequestedAndSOSRecord {

    @Id
    private String id;

    @Column(name = "programme_id", nullable = false)
    private Integer programmeId;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Transient
    Integer unitsPlanned;

    @Column(name = "total_units_requested")
    Integer requestedUnits;

    @Column(name = "starts_else_completions_requested")
    Integer requestedAtSoS;

    @Column(name = "total_units_approved")
    Integer approvedUnits;

    @Column(name = "starts_else_completions_approved")
    Integer approvedAtSoS;

    @Column(name = "TENURE_TYPE_EXT_ID")
    Integer tenureTypeExtId;

    @Column(name = "TENURE_TYPE_NAME")
    String tenureTypeName;

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

    public Integer getRequestedUnits() {
        return requestedUnits;
    }

    public void setRequestedUnits(Integer requestedUnits) {
        this.requestedUnits = requestedUnits;
    }

    public Integer getUnitsAtSoS() {
        return -1;
    }

    public Integer getTenureTypeExtId() {
        return tenureTypeExtId;
    }

    public void setTenureTypeExtId(Integer tenureTypeExtId) {
        this.tenureTypeExtId = tenureTypeExtId;
    }

    public String getTenureTypeName() {
        return tenureTypeName;
    }

    public void setTenureTypeName(String tenureTypeName) {
        this.tenureTypeName = tenureTypeName;
    }

    public Integer getUnitsPlanned() {
        return unitsPlanned;
    }

    public void setUnitsPlanned(Integer unitsPlanned) {
        this.unitsPlanned = unitsPlanned;
    }

    public Integer getRequestedAtSoS() {
        return requestedAtSoS;
    }

    public void setRequestedAtSoS(Integer requestedAtSoS) {
        this.requestedAtSoS = requestedAtSoS;
    }

    public Integer getApprovedUnits() {
        return approvedUnits;
    }

    public void setApprovedUnits(Integer approvedUnits) {
        this.approvedUnits = approvedUnits;
    }

    public Integer getApprovedAtSoS() {
        return approvedAtSoS;
    }

    public void setApprovedAtSoS(Integer approvedAtSoS) {
        this.approvedAtSoS = approvedAtSoS;
    }
}
