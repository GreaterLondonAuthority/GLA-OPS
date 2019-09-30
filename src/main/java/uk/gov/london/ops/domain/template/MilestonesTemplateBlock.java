/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("MILESTONES")
public class MilestonesTemplateBlock extends TemplateBlock {

    public enum EvidenceApplicability {NOT_APPLICABLE, NEW_MILESTONES_ONLY, ALL_MILESTONES}

    @JoinData(joinType = Join.JoinType.OneToMany, sourceTable = "template_block", sourceColumn = "id",
            targetColumn = "template_block_id", targetTable = "processing_route", comment = "")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = ProcessingRoute.class)
    @JoinColumn(name="template_block_id")
    private Set<ProcessingRoute> processingRoutes = new HashSet<>();

    @Column(name = "max_evidence_attachments")
    private Integer maxEvidenceAttachments;

    @Column(name = "milestone_description_enabled")
    private boolean descriptionEnabled;

    @Column(name = "show_milestone_status")
    private Boolean showMilestoneStatus;

    @Column(name = "evidence_applicability")
    @Enumerated(EnumType.STRING)
    private EvidenceApplicability evidenceApplicability;

    public MilestonesTemplateBlock() {
        super(ProjectBlockType.Milestones);
    }

    public MilestonesTemplateBlock(Integer displayOrder) {
        super(displayOrder, ProjectBlockType.Milestones);
    }

    public Set<ProcessingRoute> getProcessingRoutes() {
        return processingRoutes;
    }

    public void setProcessingRoutes(Set<ProcessingRoute> processingRoutes) {
        this.processingRoutes = processingRoutes;
    }

    public Integer getMaxEvidenceAttachments() {
        return maxEvidenceAttachments;
    }

    public void setMaxEvidenceAttachments(Integer maxEvidenceAttachments) {
        this.maxEvidenceAttachments = maxEvidenceAttachments;
    }

    public boolean isDescriptionEnabled() {
        return descriptionEnabled;
    }

    public void setDescriptionEnabled(boolean descriptionEnabled) {
        this.descriptionEnabled = descriptionEnabled;
    }

    public ProcessingRoute getProcessingRoute(Integer processingRouteId) {
        for (ProcessingRoute pr: processingRoutes) {
            if (pr.getId().equals(processingRouteId)) {
                return pr;
            }
        }
        return null;
    }

    public ProcessingRoute getDefaultProcessingRoute() {
        for (ProcessingRoute processingRoute: processingRoutes) {
            if (ProcessingRoute.DEFAULT_PROCESSING_ROUTE_NAME.equals(processingRoute.getName())) {
                return processingRoute;
            }
        }
        return null;
    }

    public EvidenceApplicability getEvidenceApplicability() {
        return evidenceApplicability;
    }

    public void setEvidenceApplicability(EvidenceApplicability evidenceApplicability) {
        this.evidenceApplicability = evidenceApplicability;
    }

    public Boolean getShowMilestoneStatus() {
        return showMilestoneStatus;
    }

    public void setShowMilestoneStatus(Boolean showMilestoneStatus) {
        this.showMilestoneStatus = showMilestoneStatus;
    }

    @JsonIgnore
    public boolean hasDefaultProcessingRoute() {
        return getDefaultProcessingRoute() != null;
    }

    public Set<MilestoneTemplate> getAllMilestones() {
        Set<MilestoneTemplate> allMilestones = new HashSet<>();
        for (ProcessingRoute processingRoute: processingRoutes) {
            allMilestones.addAll(processingRoute.getMilestones());
        }
        return allMilestones;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        MilestonesTemplateBlock cloned = (MilestonesTemplateBlock) clone;
        cloned.setMaxEvidenceAttachments(this.getMaxEvidenceAttachments());
        cloned.setDescriptionEnabled(this.isDescriptionEnabled());
        cloned.setShowMilestoneStatus(this.getShowMilestoneStatus());
        cloned.setEvidenceApplicability(this.getEvidenceApplicability());
        Set<ProcessingRoute> source = this.getProcessingRoutes();
        for (ProcessingRoute processingRoute : source) {
            ProcessingRoute cloneRoute = new ProcessingRoute();
            cloneRoute.setExternalId(processingRoute.getExternalId());
            cloneRoute.setName(processingRoute.getName());
            cloneRoute.setMilestones(new HashSet<>());
            for (MilestoneTemplate milestone : processingRoute.getMilestones()) {
                MilestoneTemplate clonedMilestone = new MilestoneTemplate();
                clonedMilestone.setDisplayOrder(milestone.getDisplayOrder());
                clonedMilestone.setExternalId(milestone.getExternalId());
                clonedMilestone.setMonetary(milestone.getMonetary());
                clonedMilestone.setMonetarySplit(milestone.getMonetarySplit());
                clonedMilestone.setRequirement(milestone.getRequirement());
                clonedMilestone.setSummary(milestone.getSummary());
                clonedMilestone.setKeyEvent(milestone.isKeyEvent());
                clonedMilestone.setNaSelectable(milestone.isNaSelectable());
                cloneRoute.getMilestones().add(clonedMilestone);
            }
            cloned.getProcessingRoutes().add(cloneRoute);
        }


    }

}
