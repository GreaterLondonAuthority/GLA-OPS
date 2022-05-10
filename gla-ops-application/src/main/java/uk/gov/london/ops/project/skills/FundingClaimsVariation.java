/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity(name = "funding_claims_variation")
public class FundingClaimsVariation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_claims_variation_seq_gen")
    @SequenceGenerator(name = "funding_claims_variation_seq_gen", sequenceName = "funding_claims_variation_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "original_id")
    private Integer originalId;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "block_id")
    private Integer blockId;

    @Column(name = "allocation")
    private BigDecimal allocation;

    @Column(name = "description")
    private String description;


    public FundingClaimsVariation() {}

    public FundingClaimsVariation(BigDecimal allocation, String description) {
        this.allocation = allocation;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOriginalId() {
        if (originalId == null) {
            return id;
        }
        return originalId;
    }

    public void setOriginalId(Integer originalId) {
        this.originalId = originalId;
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

    public BigDecimal getAllocation() {
        return allocation;
    }

    public void setAllocation(BigDecimal allocation) {
        this.allocation = allocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FundingClaimsVariation clone() {
        FundingClaimsVariation clone = new FundingClaimsVariation();
        clone.setOriginalId(this.getOriginalId());
        clone.setProjectId(this.getProjectId());
        clone.setBlockId(this.getBlockId());
        clone.setAllocation(this.getAllocation());
        clone.setDescription(this.getDescription());
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FundingClaimsVariation that = (FundingClaimsVariation) o;
        return Objects.equals(id, that.id)
            && Objects.equals(originalId, that.originalId)
            && Objects.equals(projectId, that.projectId)
            && Objects.equals(blockId, that.blockId)
            && Objects.equals(allocation, that.allocation)
            && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, originalId, projectId, blockId, allocation, description);
    }
}
