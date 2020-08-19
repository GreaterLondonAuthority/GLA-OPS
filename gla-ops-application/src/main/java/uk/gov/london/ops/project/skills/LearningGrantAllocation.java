/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

@Entity(name = "learning_grant_allocation")
public class LearningGrantAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "learning_grant_allocation_seq_gen")
    @SequenceGenerator(name = "learning_grant_allocation_seq_gen", sequenceName = "learning_grant_allocation_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "year")
    private Integer year;

    @Column(name = "allocation")
    private BigDecimal allocation;

    @Column(name = "community_allocation")
    private BigDecimal communityAllocation;

    @Column(name = "learner_support_allocation")
    private BigDecimal learnerSupportAllocation;

    @Transient
    private boolean deliveryAllocationEditingInProgress = false;

    @Transient
    private boolean supportAllocationEditingInProgress = false;

    public LearningGrantAllocation() {
    }

    public LearningGrantAllocation(Integer year) {
        this.year = year;
    }

    public LearningGrantAllocation(Integer year, BigDecimal allocation, BigDecimal communityAllocation,
            BigDecimal learnerSupportAllocation) {
        this(year);
        this.allocation = allocation;
        this.communityAllocation = communityAllocation;
        this.learnerSupportAllocation = learnerSupportAllocation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getAllocation() {
        return allocation;
    }

    public void setAllocation(BigDecimal allocation) {
        this.allocation = allocation;
    }

    public BigDecimal getCommunityAllocation() {
        return communityAllocation;
    }

    public void setCommunityAllocation(BigDecimal communityAllocation) {
        this.communityAllocation = communityAllocation;
    }

    public BigDecimal getLearnerSupportAllocation() {
        return learnerSupportAllocation;
    }

    public void setLearnerSupportAllocation(BigDecimal learnerSupportAllocation) {
        this.learnerSupportAllocation = learnerSupportAllocation;
    }

    public boolean isDeliveryAllocationEditingInProgress() {
        return deliveryAllocationEditingInProgress;
    }

    public void setDeliveryAllocationEditingInProgress(boolean deliveryAllocationEditingInProgress) {
        this.deliveryAllocationEditingInProgress = deliveryAllocationEditingInProgress;
    }

    public boolean isSupportAllocationEditingInProgress() {
        return supportAllocationEditingInProgress;
    }

    public void setSupportAllocationEditingInProgress(boolean supportAllocationEditingInProgress) {
        this.supportAllocationEditingInProgress = supportAllocationEditingInProgress;
    }

    public LearningGrantAllocation clone() {
        LearningGrantAllocation clone = new LearningGrantAllocation();
        clone.setYear(year);
        clone.setAllocation(allocation);
        clone.setCommunityAllocation(communityAllocation);
        clone.setLearnerSupportAllocation(learnerSupportAllocation);
        clone.setDeliveryAllocationEditingInProgress(deliveryAllocationEditingInProgress);
        clone.setSupportAllocationEditingInProgress(supportAllocationEditingInProgress);
        return clone;
    }

}
