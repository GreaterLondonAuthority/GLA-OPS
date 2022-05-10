/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import uk.gov.london.ops.framework.OpsEntity;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity(name = "strategic_units_for_tenure")
public class StrategicPlannedUnitsForTenure implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "strategic_units_for_tenure_seq_gen")
    @SequenceGenerator(name = "strategic_units_for_tenure_seq_gen", sequenceName = "strategic_units_for_tenure_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "programme_id", nullable = false)
    private Integer programmeId;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Column(name = "tenure_ext_id")
    private Integer tenureType;

    @Column(name = "units_planned")
    private Integer unitsPlanned;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    public StrategicPlannedUnitsForTenure() {

    }

    public StrategicPlannedUnitsForTenure(Integer programmeId, Integer orgId, Integer tenureType, Integer unitsPlanned) {
        this.programmeId = programmeId;
        this.orgId = orgId;
        this.tenureType = tenureType;
        this.unitsPlanned = unitsPlanned;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getTenureType() {
        return tenureType;
    }

    public void setTenureType(Integer tenureType) {
        this.tenureType = tenureType;
    }

    public Integer getUnitsPlanned() {
        return unitsPlanned;
    }

    public void setUnitsPlanned(Integer unitsPlanned) {
        this.unitsPlanned = unitsPlanned;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
