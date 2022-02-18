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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private AllocationType type;

    @Transient
    private boolean deliveryAllocationEditingInProgress = false;

    @Transient
    private boolean supportAllocationEditingInProgress = false;

    public LearningGrantAllocation() {
    }

    public LearningGrantAllocation(Integer year) {
        this.year = year;
    }

    public LearningGrantAllocation(Integer year, BigDecimal allocation, AllocationType type) {
        this(year);
        this.allocation = allocation;
        this.type = type;
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

    public AllocationType getType() {
        return type;
    }

    public void setType(AllocationType type) {
        this.type = type;
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
        clone.setType(type);
        clone.setDeliveryAllocationEditingInProgress(deliveryAllocationEditingInProgress);
        clone.setSupportAllocationEditingInProgress(supportAllocationEditingInProgress);
        return clone;
    }

}
