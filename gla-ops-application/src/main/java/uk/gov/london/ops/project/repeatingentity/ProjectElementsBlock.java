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
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.template.domain.ProjectElementsTemplateBlock;

/**
 * Created by carmina on 02/12/2019.
 */
@Entity(name = "project_elements")
@DiscriminatorValue("PROJECTELEMENTS")
@JoinData(sourceTable = "project_elements", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the project elements block is a subclass of the project block and shares a common key")
public class ProjectElementsBlock extends RepeatingEntityBlock<ProjectElement> {

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "block_id",
            targetTable = "project_element", comment = "")
    @OneToMany(fetch = FetchType.LAZY, cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    @OrderBy("id")
    List<ProjectElement> projectElements = new ArrayList<>();

    @Override
    public ProjectBlockType getProjectBlockType() {
        return ProjectBlockType.ProjectElements;
    }

    @Override
    public List<ProjectElement> getRepeatingEntities() {
        return projectElements;
    }

    @Override
    public String getRootPath() {
        return "projectElement";
    }

    public List<ProjectElement> getProjectElements() {
        return projectElements;
    }

    public void setProjectElements(List<ProjectElement> projectElements) {
        this.projectElements = projectElements;
    }

    @Override
    public ProjectElement getNewEntityInstance() {
        return new ProjectElement();
    }

    @Override
    public boolean isComplete() {
        if (project == null) {
            return false;
        }
        boolean incorrectNumberOfEntities = false;
        ProjectElementsTemplateBlock blockTemplate = (ProjectElementsTemplateBlock) project.getTemplate()
                .getSingleBlockByType(ProjectBlockType.ProjectElements);

        if (blockTemplate != null) {
            if (blockTemplate.getMinNumberOfEntities() != null && blockTemplate.getMaxNumberOfEntities() != null) {
                incorrectNumberOfEntities = getProjectElements().size() < blockTemplate.getMinNumberOfEntities()
                        || getProjectElements().size() > blockTemplate.getMaxNumberOfEntities();
            }
        }
        return isNotRequired() || (super.isComplete() && !incorrectNumberOfEntities);
    }

    @Override
    public void merge(NamedProjectBlock block) {
        super.merge(block);
        ProjectElementsBlock updated = (ProjectElementsBlock) block;
        this.getProjectElements().clear();
        this.getProjectElements().addAll(updated.getProjectElements());
    }
}
