/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.project.block.ProjectDifference;

/**
 * Created by chris on 04/11/2019.
 */
@Entity(name = "project_objective")
public class ProjectObjective implements RepeatingEntity, ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_objective_seq_gen")
    @SequenceGenerator(name = "project_objective_seq_gen", sequenceName = "project_objective_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "summary")
    private String summary;

    @Column
    private String createdBy;

    @Column
    private OffsetDateTime createdOn;

    @JoinColumn(name = "modified_by")
    private String modifiedBy;

    @Column
    private OffsetDateTime modifiedOn;

    public ProjectObjective() {
    }

    public ProjectObjective(String title, String summary) {
        this.title = title;
        this.summary = summary;
    }

    @Column(name = "original_id")
    @JoinData(targetTable = "project_objective", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "A reference to a previous version of this item if this is a cloned via a new block version.")
    private Integer originalId;


    public Integer getId() {
        return id;
    }

    @Override
    public void update(RepeatingEntity fromEntity) {
        ProjectObjective fromPO = (ProjectObjective) fromEntity;
        this.setTitle(fromPO.getTitle());
        this.setSummary(fromPO.getSummary());
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
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getOriginalId() {
        if (originalId == null) {
            return id;
        }
        return originalId;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setOriginalId(Integer originalId) {
        this.originalId = originalId;
    }

    @Override
    public boolean isComplete() {
        return !(StringUtils.isEmpty(title) || StringUtils.isEmpty(summary));
    }

    public ProjectObjective copy() {
        ProjectObjective copy = new ProjectObjective();
        copy.setTitle(getTitle());
        copy.setSummary(getSummary());
        copy.setOriginalId(getOriginalId());
        copy.setCreatedBy(getCreatedBy());
        copy.setCreatedOn(getCreatedOn());
        return copy;
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
    public String getComparisonId() {
        return String.valueOf(getOriginalId());
    }

    List<ProjectDifference> compareWith(ProjectObjective projectObjective) {
        List<ProjectDifference> differences = new ArrayList<>();

        if (!Objects.equals(StringUtils.trimAllWhitespace(this.getTitle()),
                StringUtils.trimAllWhitespace(projectObjective.getTitle()))) {
            differences.add(new ProjectDifference(this, "title"));
        }

        if (!Objects.equals(StringUtils.trimAllWhitespace(this.getSummary()),
                StringUtils.trimAllWhitespace(projectObjective.getSummary()))) {
            differences.add(new ProjectDifference(this, "summary"));
        }

        return differences;
    }

}
