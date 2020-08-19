/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

@Entity(name = "funding_activity_group")
public class FundingActivityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_activity_group_seq_gen")
    @SequenceGenerator(name = "funding_activity_group_seq_gen", sequenceName = "funding_activity_group_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "original_id")
    private Integer originalId;

    @Column(name = "block_id")
    private Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "quarter")
    private Integer quarter;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = FundingActivity.class)
    @JoinColumn(name = "funding_activity_group_id")
    private List<FundingActivity> activities = new ArrayList<>();

    public FundingActivityGroup() {
    }

    public FundingActivityGroup(Integer blockId, Integer year, Integer quarter) {
        this.blockId = blockId;
        this.year = year;
        this.quarter = quarter;
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

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public List<FundingActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<FundingActivity> activities) {
        this.activities = activities;
    }

    public FundingActivityGroup clone(Integer clonedProjectId, Integer clonedBlockId) {
        FundingActivityGroup clone = new FundingActivityGroup();
        clone.setOriginalId(this.getOriginalId());
        clone.setBlockId(clonedBlockId);
        clone.setYear(this.getYear());
        clone.setQuarter(this.getQuarter());
        for (FundingActivity activity : this.getActivities()) {
            clone.getActivities().add(activity.clone(clonedProjectId, clonedBlockId));
        }
        return clone;
    }

}
