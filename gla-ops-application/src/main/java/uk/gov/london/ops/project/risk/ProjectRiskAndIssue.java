/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.risk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.refdata.CategoryValue;

import javax.persistence.*;
import java.util.*;

import static uk.gov.london.common.GlaUtils.nullSafeMultiply;
import static uk.gov.london.ops.project.block.ProjectDifference.DifferenceType.Addition;
import static uk.gov.london.ops.project.block.ProjectDifference.DifferenceType.Deletion;

/**
 * Created by chris on 17/08/2017.
 */
@Entity(name = "project_risk")
public class ProjectRiskAndIssue implements ComparableItem {

    public enum Status {
        Open, Closed
    }

    public enum Type {
        Risk, Issue
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_risk_seq_gen")
    @SequenceGenerator(name = "project_risk_seq_gen", sequenceName = "project_risk_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @JoinData(joinType = Join.JoinType.OneToOne, sourceColumn = "risk_category_id", targetColumn = "id",
            targetTable = "category_value", comment = "")
    @OneToOne
    private CategoryValue riskCategory;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = ProjectAction.class)
    @JoinColumn(name = "risk_id")
    @OrderBy("lastModified")
    private List<ProjectAction> actions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    protected Status status = Status.Open;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    protected Type type = Type.Risk;

    @Column(name = "initial_probability_rating")
    private Integer initialProbabilityRating;

    @Column(name = "initial_impact_rating")
    private Integer initialImpactRating;

    @Column(name = "residual_probability_rating")
    private Integer residualProbabilityRating;

    @Column(name = "residual_impact_rating")
    private Integer residualImpactRating;

    @Column(name = "risk_marked_corporate")
    private boolean markedForCorporateReporting;

    @JoinData(joinType = Join.JoinType.MultiColumn,
            comment = "initial_impact_rating and initial_probability_rating join to risk_level impact and probability")
    @ManyToOne(cascade = CascadeType.REFRESH, targetEntity = RiskLevelLookup.class)
    @JoinColumns({
      @JoinColumn(name = "initial_impact_rating", referencedColumnName = "impact", insertable = false, updatable = false),
      @JoinColumn(name = "initial_probability_rating", referencedColumnName = "probability", insertable = false, updatable = false)
    })
    private RiskLevelLookup initialRiskLevel;

    @JoinData(joinType = Join.JoinType.MultiColumn,
            comment = "residual_impact_rating and residual_probability_rating join to risk_level impact and probability")
    @ManyToOne(cascade = CascadeType.REFRESH, targetEntity = RiskLevelLookup.class)
    @JoinColumns({
      @JoinColumn(name = "residual_impact_rating", referencedColumnName = "impact", insertable = false, updatable = false),
      @JoinColumn(name = "residual_probability_rating", referencedColumnName = "probability", insertable = false, updatable = false)
    })
    private RiskLevelLookup residualRiskLevel;

    @JsonIgnore
    @Column(name = "original_id")
    @JoinData(targetTable = "project_risk", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "Self join, if this is a clone of a previous issue caused by editing an approved block")
    private Integer originalId;

    public ProjectRiskAndIssue() {
    }

    public ProjectRiskAndIssue(Type type, String title, String description, Integer initialProbabilityRating,
            Integer initialImpactRating, Integer residualProbabilityRating, Integer residualImpactRating,
            CategoryValue riskCategory) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.initialProbabilityRating = initialProbabilityRating;
        this.initialImpactRating = initialImpactRating;
        this.residualProbabilityRating = residualProbabilityRating;
        this.residualImpactRating = residualImpactRating;
        this.riskCategory = riskCategory;
    }

    public Integer getId() {
        return id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CategoryValue getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(CategoryValue riskCategory) {
        this.riskCategory = riskCategory;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getInitialProbabilityRating() {
        return initialProbabilityRating;
    }

    public void setInitialProbabilityRating(Integer initialProbabilityRating) {
        this.initialProbabilityRating = initialProbabilityRating;
    }

    public boolean isMarkedForCorporateReporting() {
        return markedForCorporateReporting;
    }

    public void setMarkedForCorporateReporting(boolean markedForCorporateReporting) {
        this.markedForCorporateReporting = markedForCorporateReporting;
    }

    public Integer getInitialImpactRating() {
        return initialImpactRating;
    }

    public void setInitialImpactRating(Integer initialImpactRating) {
        this.initialImpactRating = initialImpactRating;
    }

    public Integer getResidualProbabilityRating() {
        return residualProbabilityRating;
    }

    public void setResidualProbabilityRating(Integer residualProbabilityRating) {
        this.residualProbabilityRating = residualProbabilityRating;
    }

    public Integer getResidualImpactRating() {
        return residualImpactRating;
    }

    public void setResidualImpactRating(Integer residualImpactRating) {
        this.residualImpactRating = residualImpactRating;
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

    @Override
    public String getComparisonId() {
        return String.valueOf(getOriginalId());
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public RiskLevelLookup getInitialRiskLevel() {
        return initialRiskLevel;
    }

    public void setInitialRiskLevel(RiskLevelLookup initialRiskLevel) {
        this.initialRiskLevel = initialRiskLevel;
    }

    public RiskLevelLookup getResidualRiskLevel() {
        return residualRiskLevel;
    }

    public void setResidualRiskLevel(RiskLevelLookup residualRiskLevel) {
        this.residualRiskLevel = residualRiskLevel;
    }

    public List<ProjectAction> getActions() {
        return actions;
    }

    public void setActions(List<ProjectAction> actions) {
        this.actions = actions;
    }

    public void merge(ProjectRiskAndIssue other) {
        this.title = other.title;
        this.riskCategory = other.riskCategory;
        this.description = other.description;
        this.initialProbabilityRating = other.initialProbabilityRating;
        this.initialImpactRating = other.initialImpactRating;
        this.residualProbabilityRating = other.residualProbabilityRating;
        this.residualImpactRating = other.residualImpactRating;
        this.markedForCorporateReporting = other.markedForCorporateReporting;

        if (other.getActions() != null) {
            for (ProjectAction action : other.actions) {
                Optional<ProjectAction> first = this.getActions().stream().filter(a -> a.getId().equals(action.getId()))
                        .findFirst();
                first.ifPresent(
                        projectAction -> {
                            projectAction.setOwner(action.getOwner());
                            projectAction.setAction(action.getAction());
                            projectAction.setLastModified(action.getLastModified());
                            projectAction.setMarkedForCorporateReporting(action.isMarkedForCorporateReporting());
                        });
            }
        }

    }

    public ProjectRiskAndIssue copy() {
        ProjectRiskAndIssue copy = new ProjectRiskAndIssue();
        copy.setInitialImpactRating(getInitialImpactRating());
        copy.setInitialProbabilityRating(getInitialProbabilityRating());
        copy.setResidualImpactRating(getResidualImpactRating());
        copy.setResidualProbabilityRating(getResidualProbabilityRating());
        copy.setRiskCategory(getRiskCategory());
        copy.setDescription(getDescription());
        copy.setTitle(getTitle());
        copy.setType(getType());
        copy.setStatus(getStatus());
        copy.setOriginalId(getOriginalId());
        copy.setMarkedForCorporateReporting(isMarkedForCorporateReporting());

        for (ProjectAction action : actions) {
            copy.getActions().add(action.copy());
        }

        return copy;
    }

    List<ProjectDifference> compareWith(ProjectRiskAndIssue otherRisk) {
        List<ProjectDifference> differences = new ArrayList<>();

        if (!Objects
                .equals(StringUtils.trimAllWhitespace(this.getTitle()), StringUtils.trimAllWhitespace(otherRisk.getTitle()))) {
            differences.add(new ProjectDifference(this, "title"));
        }

        if (!Objects.equals(StringUtils.trimAllWhitespace(this.getDescription()),
                StringUtils.trimAllWhitespace(otherRisk.getDescription()))) {
            differences.add(new ProjectDifference(this, "description"));
        }

        if (!Objects.equals(
                nullSafeMultiply(this.getInitialImpactRating(), this.getInitialProbabilityRating()),
                nullSafeMultiply(otherRisk.getInitialImpactRating(), otherRisk.getInitialProbabilityRating()))) {
            differences.add(new ProjectDifference(this, "computedInitialRating"));
        }

        if (!Objects.equals(
                nullSafeMultiply(this.getResidualImpactRating(), this.getResidualProbabilityRating()),
                nullSafeMultiply(otherRisk.getResidualImpactRating(), otherRisk.getResidualProbabilityRating()))) {
            differences.add(new ProjectDifference(this, "computedResidualRating"));
        }

        if (!Objects.equals(this.getStatus(), otherRisk.getStatus())) {
            differences.add(new ProjectDifference(this, "status"));
        }

        if (!Objects.equals(this.getRiskCategory(), otherRisk.getRiskCategory())) {
            differences.add(new ProjectDifference(this, "riskCategory"));
        }

        if (!Objects.equals(this.getResidualRiskLevel(), otherRisk.getResidualRiskLevel())) {
            differences.add(new ProjectDifference(this, "residualRiskLevel"));
        }

        if (!Objects.equals(this.getInitialRiskLevel(), otherRisk.getInitialRiskLevel())) {
            differences.add(new ProjectDifference(this, "initialRiskLevel"));
        }

        Map<Integer, ProjectAction> thisMap = this.getEntriesFromList(getActions());
        Map<Integer, ProjectAction> otherMap = this.getEntriesFromList(otherRisk.getActions());

        Collection<Integer> inBoth = CollectionUtils.intersection(otherMap.keySet(), thisMap.keySet());
        Collection<Integer> inLeft = CollectionUtils.subtract(otherMap.keySet(), thisMap.keySet());
        Collection<Integer> inRight = CollectionUtils.subtract(thisMap.keySet(), otherMap.keySet());

        for (Integer actionId : inBoth) {
            ProjectAction thisAction = thisMap.get(actionId);
            ProjectAction otherAction = otherMap.get(actionId);
            differences.addAll(thisAction.compareWith(otherAction));
        }

        for (Integer key : inLeft) {
            ProjectAction thisAction = otherMap.get(key);
            differences.add(new ProjectDifference(thisAction, Deletion));
        }

        for (Integer key : inRight) {
            ProjectAction otherAction = thisMap.get(key);
            differences.add(new ProjectDifference(otherAction, Addition));
        }

        return differences;
    }

    private Map<Integer, ProjectAction> getEntriesFromList(List<ProjectAction> actions) {
        Map<Integer, ProjectAction> map = new HashMap<>();
        for (ProjectAction action : actions) {
            map.put(action.getOriginalId(), action);
        }
        return map;
    }

}
