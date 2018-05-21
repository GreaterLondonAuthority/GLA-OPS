/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import org.springframework.util.StringUtils;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by chris on 30/08/2017.
 */
@Entity(name="project_action")
public class ProjectAction implements ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "action_seq_gen")
    @SequenceGenerator(name = "action_seq_gen", sequenceName = "action_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name="action_title")
    private String action;

    @Column
    private String owner;

    @Column(name = "last_modified")
    private OffsetDateTime lastModified;

    @Column(name = "original_id")
    @JoinData(targetTable = "project_action", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "A reference to a previous version of this item if this is a cloned via a new block version.")
    private Integer originalId;

    public ProjectAction() {}

    public ProjectAction(String action, String owner, OffsetDateTime lastModified) {
        this.action = action;
        this.owner = owner;
        this.lastModified = lastModified;
    }

    public ProjectAction(Integer id, String action, String owner) {
        this.action = action;
        this.owner = owner;
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
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

    public ProjectAction copy() {
        ProjectAction copy = new ProjectAction();
        copy.setAction(getAction());
        copy.setOwner(getOwner());
        copy.setLastModified(getLastModified());
        copy.setOriginalId(getOriginalId());
        return copy;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(getOriginalId());
    }

    List<ProjectDifference> compareWith(ProjectAction otherAction) {
        List<ProjectDifference> differences = new ArrayList<>();

        if (!Objects.equals(StringUtils.trimAllWhitespace(this.getAction()), StringUtils.trimAllWhitespace(otherAction.getAction()))) {
            differences.add(new ProjectDifference(this, "action"));
        }

        if (!Objects.equals(StringUtils.trimAllWhitespace(this.getOwner()), StringUtils.trimAllWhitespace(otherAction.getOwner()))) {
            differences.add(new ProjectDifference(this, "owner"));
        }

        return differences;
    }

}
