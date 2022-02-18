/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.outputs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.framework.OpsEntity;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

/**
 * Stores the output table assumption details
 */
@Entity(name = "OUTPUT_CATEGORY_ASSUMPTION")
public class OutputCategoryAssumption implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assumption_seq_gen")
    @SequenceGenerator(name = "assumption_seq_gen", sequenceName = "assumption_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "project_id")
    @JsonIgnore
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project for this entry")
    protected Integer projectId;

    @Column(name = "block_id")
    @JsonIgnore
    @JoinData(targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The block id for this record. These records will be duplicated per block.")
    protected Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "category")
    private String category;

    @Column(name = "assumption")
    private String assumption;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    public OutputCategoryAssumption() {
    }

    public OutputCategoryAssumption(Integer projectId, Integer blockId, String category, String assumption, Integer year,
            OffsetDateTime createdOn, String createdBy) {
        this.projectId = projectId;
        this.blockId = blockId;
        this.category = category;
        this.assumption = assumption;
        this.year = year;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAssumption() {
        return assumption;
    }

    public void setAssumption(String assumption) {
        this.assumption = assumption;
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
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}

