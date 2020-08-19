/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import static javax.persistence.CascadeType.ALL;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.block.ProjectBlockType;

/**
 * Created by chris on 04/11/2019.
 */
@Entity(name = "project_objectives")
@DiscriminatorValue("PROJECTOBJECTIVES")
@JoinData(sourceTable = "project_objectives", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the project objectives block is a subclass of the project block and shares a common key")
public class ProjectObjectivesBlock extends RepeatingEntityBlock<ProjectObjective> {

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "block_id",
            targetTable = "project_objective", comment = "")
    @OneToMany(fetch = FetchType.LAZY, cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    @OrderBy("id")
    List<ProjectObjective> projectObjectives = new ArrayList<>();

    @Override
    public ProjectBlockType getProjectBlockType() {
        return ProjectBlockType.ProjectObjectives;
    }

    @Override
    public List<ProjectObjective> getRepeatingEntities() {
        return projectObjectives;
    }

    @Override
    public String getRootPath() {
        return "projectObjective";
    }

    public List<ProjectObjective> getProjectObjectives() {
        return projectObjectives;
    }

    public void setProjectObjectives(List<ProjectObjective> projectObjectives) {
        this.projectObjectives = projectObjectives;
    }

    @Override
    public ProjectObjective getNewEntityInstance() {
        return new ProjectObjective();
    }

}
