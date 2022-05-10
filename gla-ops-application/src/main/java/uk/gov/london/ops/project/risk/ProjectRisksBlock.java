/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.risk;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.project.block.ProjectDifferences;
import uk.gov.london.ops.project.template.domain.TemplateBlock;

import javax.persistence.*;
import java.util.*;

import static uk.gov.london.ops.project.block.ProjectDifference.DifferenceType.Addition;
import static uk.gov.london.ops.project.block.ProjectDifference.DifferenceType.Deletion;

/**
 * The Milestones block in a Project.
 *
 * @author Antonio Perez Dieppa
 */
@Entity(name = "risks_block")
@DiscriminatorValue("RISK")
@JoinData(sourceTable = "risks_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the risks block is a subclass of the project block and shares a common key")
public class ProjectRisksBlock extends NamedProjectBlock {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 3;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "rating_explanation")
    private String ratingExplanation;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = ProjectRiskAndIssue.class)
    @JoinColumn(name = "block_id")
    private Set<ProjectRiskAndIssue> projectRiskAndIssues = new HashSet<>();

    public ProjectRisksBlock() {
    }

    public ProjectRisksBlock(Project project) {
        super(project);
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getRatingExplanation() {
        return ratingExplanation;
    }

    public void setRatingExplanation(String ratingExplanation) {
        this.ratingExplanation = ratingExplanation;
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.Risks;
    }

    @Override
    public boolean isComplete() {
        if (!isVisited()) {
            return false;
        }
        return rating != null && ratingExplanation != null && !"".equals(ratingExplanation);
    }

    @Override
    public void copyBlockContentInto(NamedProjectBlock target) {
        super.copyBlockContentInto(target);

        ProjectRisksBlock riskBlock = (ProjectRisksBlock) target;
        riskBlock.setRating(this.getRating());
        riskBlock.setRatingExplanation(this.getRatingExplanation());

        for (ProjectRiskAndIssue risk : projectRiskAndIssues) {
            riskBlock.getProjectRiskAndIssues().add(risk.copy());
        }
    }

    public Set<ProjectRiskAndIssue> getProjectRiskAndIssues() {
        return projectRiskAndIssues;
    }

    public void setProjectRiskAndIssues(Set<ProjectRiskAndIssue> projectRiskAndIssues) {
        this.projectRiskAndIssues = projectRiskAndIssues;
    }

    @Override
    public void generateValidationFailures() {
        // do nothing
    }

    @Override
    public void merge(NamedProjectBlock block) {
        ProjectRisksBlock riskBlock = (ProjectRisksBlock) block;
        validate(riskBlock);
        this.setRating(riskBlock.getRating());
        this.setRatingExplanation(riskBlock.getRatingExplanation());
    }

    private void validate(ProjectRisksBlock riskBlock) {
        if (riskBlock.getRating() != null && (riskBlock.getRating() < MIN_RATING || riskBlock.getRating() > MAX_RATING)) {
            throw new ValidationException(String.format("Rating must be between %d and %d", MIN_RATING, MAX_RATING));
        }
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        ProjectRisksBlock other = (ProjectRisksBlock) otherBlock;

        if (!Objects.equals(StringUtils.trimAllWhitespace(ratingExplanation),
                StringUtils.trimAllWhitespace(other.ratingExplanation))) {
            differences.add(new ProjectDifference(this, "ratingExplanation"));
        }
        if (!Objects.equals(rating, other.rating)) {
            differences.add(new ProjectDifference(this, "rating"));
        }

        Map<Integer, ProjectRiskAndIssue> thisMap = this.getEntriesFromList(getProjectRiskAndIssues());
        Map<Integer, ProjectRiskAndIssue> otherMap = this.getEntriesFromList(other.getProjectRiskAndIssues());

        Collection<Integer> inBoth = CollectionUtils.intersection(otherMap.keySet(), thisMap.keySet());
        Collection<Integer> inLeft = CollectionUtils.subtract(otherMap.keySet(), thisMap.keySet());
        Collection<Integer> inRight = CollectionUtils.subtract(thisMap.keySet(), otherMap.keySet());

        for (Integer riskId : inBoth) {
            ProjectRiskAndIssue thisRisk = thisMap.get(riskId);
            ProjectRiskAndIssue otherRisk = otherMap.get(riskId);
            differences.addAll(thisRisk.compareWith(otherRisk));
        }

        for (Integer key : inLeft) {
            ProjectRiskAndIssue thisRisk = otherMap.get(key);
            differences.add(new ProjectDifference(thisRisk, Deletion));
        }

        for (Integer key : inRight) {
            ProjectRiskAndIssue otherRisk = thisMap.get(key);
            differences.add(new ProjectDifference(otherRisk, Addition));
        }
    }

    private Map<Integer, ProjectRiskAndIssue> getEntriesFromList(Set<ProjectRiskAndIssue> entries) {
        Map<Integer, ProjectRiskAndIssue> map = new HashMap<>();
        for (ProjectRiskAndIssue entry : entries) {
            map.put(entry.getOriginalId(), entry);
        }
        return map;
    }

    public ProjectRiskAndIssue getRisk(Integer id) {
        return projectRiskAndIssues.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

}
