/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.domain.attachment.StandardAttachment;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.mapper.MilestoneMapper;
import uk.gov.london.ops.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.spe.SimpleProjectExportUtils;
import uk.gov.london.ops.util.GlaOpsUtils;
import uk.gov.london.ops.util.MilestoneComparator;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.project.ClaimStatus.*;
import static uk.gov.london.ops.domain.project.GrantType.*;
import static uk.gov.london.ops.domain.project.MilestoneStatus.ACTUAL;
import static uk.gov.london.ops.domain.project.MilestoneStatus.FORECAST;
import static uk.gov.london.ops.domain.template.Template.MilestoneType.MonetarySplit;
import static uk.gov.london.ops.domain.template.Template.MilestoneType.MonetaryValue;
import static uk.gov.london.ops.spe.SimpleProjectExportConstants.ReportPrefix;
import static uk.gov.london.ops.spe.SimpleProjectExportConstants.ReportSuffix;
import static uk.gov.london.ops.util.GlaOpsUtils.addBigDecimals;

/**
 * The Milestones block in a Project.
 *
 * @author Steve Leach
 */
@Entity(name = "milestones_block")
@DiscriminatorValue("MILESTONES")
@JoinData(sourceTable = "milestones_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the milestones block is a subclass of the project block and shares a common key")
public class ProjectMilestonesBlock extends NamedProjectBlock {

    @Column(name = "processing_route_id")
    @JoinData(targetTable = "processing_route", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "the currently selected processing route for this milestones block.")
    private Integer processingRouteId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = Milestone.class)
    @JoinColumn(name = "milestones_block", nullable = false)
    private Set<Milestone> milestones = new HashSet<>();

    @Column(name = "max_evidence_attachments")
    private Integer maxEvidenceAttachments;

    @Column(name = "evidence_applicability")
    @Enumerated(EnumType.STRING)
    private MilestonesTemplateBlock.EvidenceApplicability evidenceApplicability;

    @Column(name = "milestone_description_enabled")
    private boolean descriptionEnabled;

    public ProjectMilestonesBlock() {
    }

    public ProjectMilestonesBlock(Project project) {
        super(project);
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.Milestones;
    }

    @Override
    public boolean isComplete() {
        if (!isVisited()) {
            return false;
        }

        if (!project.getTemplate().isBlockPresent(ProjectBlockType.Milestones)) {
            return true;
        }

        if (Template.MilestoneType.MonetaryValue.equals(project.getTemplate().getMilestoneType())) {
            return !isMonetaryMilestoneTotalGreaterThanGrantSource();
        }

        if (claimedExceeded()) {
            return false;
        }

        Set<TemplateBlock> blocksByType = project.getTemplate().getBlocksByType(ProjectBlockType.Milestones);
        MilestonesTemplateBlock milestonesTemplateBlock = null;

        for (TemplateBlock templateBlock : blocksByType) {
            if (templateBlock.getDisplayOrder().equals(this.getDisplayOrder())) {
                milestonesTemplateBlock = (MilestonesTemplateBlock) templateBlock;
            }
        }

        Set<MilestoneTemplate> milestoneTemplates = null;
        boolean processingRouteSelected = false;
        if (processingRouteId != null) {
            milestoneTemplates = milestonesTemplateBlock.getProcessingRoute(processingRouteId).getMilestones();
            processingRouteSelected = true;
        } else {
            if (milestonesTemplateBlock.hasDefaultProcessingRoute()) {
                processingRouteSelected = true;
                milestoneTemplates = milestonesTemplateBlock.getDefaultProcessingRoute().getMilestones();
            }
        }

        if (processingRouteSelected && (milestoneTemplates == null || milestoneTemplates.isEmpty())) {
            return true;
        } else if (!processingRouteSelected) {
            return false;
        }

        for (Milestone milestone : getApplicableMilestones()) {

            if (Requirement.isRequired(milestone.getRequirement())) {
                if (milestone.getMilestoneDate() == null) {
                    return false;
                }

                if (Boolean.TRUE.equals(milestone.getMonetary()) && milestone.getMonetarySplit() == null) {
                    return false;
                }
                MilestoneStatus milestoneStatus = milestone.getMilestoneStatus();

                if(milestone.getMilestoneStatus() == null){
                    return false;
                }

            }
        }

        int totalSplit = 0;
        boolean hasMonetaryMilestones = false;
        for (Milestone milestone : getApplicableMilestones()) {
            if (Boolean.TRUE.equals(milestone.getMonetary())) {
                hasMonetaryMilestones = true;
                totalSplit += milestone.getMonetarySplit() == null ? 0 : milestone.getMonetarySplit();
            }
        }

        if (hasMonetaryMilestones && (totalSplit != 100)) {
            return false;
        }

        return true;
    }

    @JsonIgnore
    public List<Milestone> getApplicableMilestones() {
        return milestones.stream().filter(m -> !m.isNotApplicable()).collect(Collectors.toList());
    }

    private boolean isMonetaryMilestoneTotalGreaterThanGrantSource() {
        GrantSourceBlock grantSource = (GrantSourceBlock) project.getSingleLatestBlockOfType(ProjectBlockType.GrantSource);
        if (grantSource == null) {
            return false;
        }
        long sum = grantSource.getTotalGrantRequested();

        if (grantSource != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (Milestone milestone : milestones) {
                total = GlaOpsUtils.addBigDecimals(total, milestone.getMonetaryValue());
            }

            if (new BigDecimal(sum).subtract(total).compareTo(BigDecimal.ZERO) == -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if any of the milestones exceeds the max amount to claim by Grant Type.
     * For example, if a milestone claim a right Grant amount, but exceeds the DPF amount, that milestone
     * appears as it has exceeded the claimed amount.
     * <p>
     * This algorithm tries to minimizes the number of milestone that break that limit.
     * For example if we have two milestones, one with value 50 and the other with 51, having a maximum
     * claimed amount of 100. Then this algorithm will show only one of them as exceeded, but doesn't
     * guarantee that it's the lower one. That the depends on the order of the milestones in the block.
     *
     * @return
     */
    private boolean claimedExceeded() {

        final Map<GrantType, Long> claimed = new HashMap<>();

        claimed.put(GrantType.Grant, 0L);
        claimed.put(GrantType.RCGF, 0L);
        claimed.put(GrantType.DPF, 0L);

        Map<GrantType, Long> grantsRequested = project.getGrantsRequested();

        if (grantsRequested == null) {
            return false;
        }

        boolean isExceeded = false;


        //loop over the grant types
        for (GrantType type : GrantType.values()) {

            for (Milestone milestone : milestones) {

                //Variable which contains the grant type amount for the given milestone
                long milestoneTypedValue = 0L;
                switch (type) {
                    case Grant:
                        Long milestoneClaimedGrant = this.getMilestoneGrantClaimed(milestone.getId());
                        milestoneTypedValue = milestoneClaimedGrant != null ? milestoneClaimedGrant : 0;
                        break;
                    case DPF:
                    case RCGF:
                        milestoneTypedValue = milestone.getPendingTotalAmountIncludingReclaimsByType(type);
                        break;
                }
                //Temporal claimed amount which is the sum of all milestones already processed
                //in this loop(if none of its grant type amounts exceeded the max amount)
                //plus the current one
                claimed.put(type, claimed.get(type) + milestoneTypedValue);

            }

            if (grantsRequested.get(type) != null &&
                    claimed.get(type) > grantsRequested.get(type)) {
                isExceeded = true;
            }
        }


        if (isExceeded) {
            for (Milestone milestone : milestones) {
                if (milestone.getClaimStatus() != null) {
                    switch (milestone.getClaimStatus()) {
                        case Approved:
                            if (milestone.getReclaimedRcgf() != null || milestone.getReclaimedDpf() != null || milestone.getReclaimedGrant() != null) {
                                milestone.setClaimedExceeded(true);
                            }
                            break;
                        case Claimed:
                            if (milestone.getClaimedRcgf() != null || milestone.getClaimedDpf() != null || milestone.getClaimedGrant() != null) {
                                milestone.setClaimedExceeded(true);
                            }
                            break;
                    }
                }
            }
        }

        return isExceeded;
    }

    @Override
    public void generateValidationFailures() {

        if (Template.MilestoneType.MonetaryValue.equals(project.getTemplate().getMilestoneType())) {
            if (isMonetaryMilestoneTotalGreaterThanGrantSource()) {
                this.addErrorMessage("Block", "monetaryValue", "The total milestone value must not exceed the amount requested in the Grant Source block");
            }
        }

    }

    public boolean hasClaimedMilestones() {
        for (Milestone milestone : milestones) {
            if (milestone.isClaimed()) {
                return true;
            }
        }
        return false;
    }

    public void merge(ProjectMilestonesBlock milestonesBlock) {
        if ((milestonesBlock.milestones == null || milestones == null) || milestonesBlock.milestones.size() > milestones.size()) {
            throw new ValidationException("Provided milestones don't match expected milestones for project!");
        }

        for (Milestone toMilestone : milestones) {
            Milestone fromMilestone = milestonesBlock.getMilestoneById(toMilestone.getId());
            if (!toMilestone.isClaimed()) {

                if (fromMilestone == null) {
                    throw new ValidationException("Invalid id for milestones");
                }

                boolean needToSetDate = (toMilestone.getMilestoneDate() == null && fromMilestone.getMilestoneDate() != null) || (toMilestone.getMilestoneDate() != null && fromMilestone.getMilestoneDate() == null);
                boolean bothDatesPresent = toMilestone.getMilestoneDate() != null && fromMilestone.getMilestoneDate() != null;
                boolean needToUpdateDate = bothDatesPresent && fromMilestone.getMilestoneDate().compareTo(toMilestone.getMilestoneDate()) != 0;

                if (needToSetDate || needToUpdateDate) {
                    toMilestone.setMilestoneDate(fromMilestone.getMilestoneDate());
                }

                // status is set automatically for housing projects and manually for land projects when date is in the present or past
                if (project.isAutoApproval() && fromMilestone.getMilestoneDate() != null && fromMilestone.getMilestoneDate().isBefore(LocalDate.now().plusDays(1))) {
                    toMilestone.setMilestoneStatus(fromMilestone.getMilestoneStatus());
                } else {
                    toMilestone.autoUpdateMilestoneStatus();
                }

                toMilestone.setDescription(fromMilestone.getDescription());
                toMilestone.setMonetarySplit(fromMilestone.getMonetarySplit());
                toMilestone.setMonetaryValue(fromMilestone.getMonetaryValue());


                if (toMilestone.isNaSelectable() && !toMilestone.isClaimed()) {
                    toMilestone.setNotApplicable(fromMilestone.isNotApplicable());
                }

                if (toMilestone.isNotApplicable()) {
                    toMilestone.setMonetarySplit(null);
                    toMilestone.setMonetaryValue(null);
                    toMilestone.setMilestoneDate(null);
                    toMilestone.setMilestoneStatus(null);
                }
            } else if (toMilestone.isApproved()) {

                toMilestone.setReclaimedRcgf(fromMilestone.getReclaimedRcgf());
                toMilestone.setReclaimedDpf(fromMilestone.getReclaimedDpf());
                toMilestone.setReclaimedGrant(fromMilestone.getReclaimedGrant());
                toMilestone.setReclaimReason(fromMilestone.getReclaimReason());
            }
        }
    }

    public Milestone getMilestoneById(Integer id) {
        for (Milestone milestone : milestones) {
            if (Objects.equals(milestone.getId(), id)) {
                return milestone;
            }
        }
        return null;
    }

    public Milestone getMilestoneByExternalId(Integer id) {
        for (Milestone milestone : milestones) {
            if (milestone.getExternalId() != null && id.equals(milestone.getExternalId())) {
                return milestone;
            }
        }
        return null;
    }

    public Map<GrantType, Long> getAvailableToReclaimByType() {
        Map<GrantType, Long> current = project.getCurrentGrantSourceValuesByType();
        if (current == null ) {
            return null;
        }
        Map<GrantType, Long> map = new HashMap<>();
        Map<GrantType, Long> existingClaims = new HashMap<>();
        existingClaims.put(Grant, 0L);
        existingClaims.put(RCGF, 0L);
        existingClaims.put(DPF, 0L);
        for (GrantType grantType : current.keySet()) {

            for (Milestone milestone : this.getMilestones()) {

                if (milestone.getClaimStatus() != null && milestone.isApproved()) {

                    if (Grant.equals(grantType)) {
                        Long milestoneClaimedGrant = getMilestoneGrantClaimed(milestone.getId());
                        Long claimedGrant = existingClaims.get(Grant);
                        claimedGrant += milestoneClaimedGrant != null ? milestoneClaimedGrant : 0;
                        claimedGrant -= milestone.getReclaimedGrant() != null ? milestone.getReclaimedGrant() : 0;
                        existingClaims.put(Grant, claimedGrant);
                    } else {
                        Long claimed = existingClaims.get(grantType);
                        claimed += milestone.getPendingTotalAmountIncludingReclaimsByType(grantType);
                        existingClaims.put(grantType, claimed);
                    }
                }

            }


            Long grant = existingClaims.get(grantType);
            Long currentValue = current.get(grantType);

            map.put(grantType, Math.max(0, (grant == null ? 0 : grant) - (currentValue == null ? 0 : currentValue)));
        }

        return map;
    }

    public Integer getProcessingRouteId() {
        return processingRouteId;
    }

    public void setProcessingRouteId(Integer processingRouteId) {
        this.processingRouteId = processingRouteId;
    }

    public Set<Milestone> getMilestones() {
        return milestones;
    }

    @JsonProperty("milestones")
    public List<Milestone> getSortedMilestones() {
        if (milestones != null) {
            return milestones.stream().sorted(new MilestoneComparator()).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public void setMilestones(Set<Milestone> milestones) {
        this.milestones = milestones;
    }

    public boolean isDescriptionEnabled() {
        return descriptionEnabled;
    }

    public void setDescriptionEnabled(boolean descriptionEnabled) {
        this.descriptionEnabled = descriptionEnabled;
    }

    @Override
    public Map<String, Object> simpleDataExtract(
            final SimpleProjectExportConfig simpleProjectExportConfig) {
        Map<String, Object> map = new HashMap<>();
        for (Milestone milestone : getMilestones()) {
            if (!milestone.isManuallyCreated()) {
                final String msPrefix = milestone.getSummary() != null
                        ? milestone.getSummary()
                        : "id_" + milestone.getId();
                String exportString = ReportPrefix.ms_.name() +
                        SimpleProjectExportUtils.formatForExport(msPrefix);
                map.put(exportString + ReportSuffix._date,
                        milestone.getMilestoneDate());
                map.put(exportString + ReportSuffix._status,
                        milestone.getMilestoneStatus());
                map.put(exportString + ReportSuffix._percentage,
                        milestone.getMonetarySplit());
            }
        }
        if (processingRouteId != null) {
            MilestonesTemplateBlock milestonesTemplateBlock =
                    (MilestonesTemplateBlock) project.getTemplate()
                            .getSingleBlockByType(ProjectBlockType.Milestones);
            ProcessingRoute pr = milestonesTemplateBlock
                    .getProcessingRoute(processingRouteId);
            if (pr != null) {
                map.put(ReportPrefix.ms_.name()
                        + ReportSuffix.processing_route.name(), pr.getName());
            }
        }
        if (!map.containsKey(ReportPrefix.ms_.name()
                + ReportSuffix.processing_route.name())) {
            map.put(ReportPrefix.ms_.name()
                    + ReportSuffix.processing_route.name(), "");
        }
        return map;
    }


    @Override
    public void copyBlockContentInto(NamedProjectBlock target) {
        ProjectMilestonesBlock clone = (ProjectMilestonesBlock) target;
        clone.setProcessingRouteId(this.getProcessingRouteId());

        Set<Milestone> clonedMilestones = new HashSet<>();
        for (Milestone milestone : this.getMilestones()) {
            Milestone clonedMilestone = new Milestone();
            milestone.copyDataInto(clonedMilestone);
            clonedMilestones.add(clonedMilestone);
        }
        clone.setMilestones(clonedMilestones);
        clone.setMaxEvidenceAttachments(this.getMaxEvidenceAttachments());
        clone.setEvidenceApplicability(this.getEvidenceApplicability());
        clone.setDescriptionEnabled(this.isDescriptionEnabled());
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {

        MilestonesTemplateBlock mtb = (MilestonesTemplateBlock) templateBlock;

        if (mtb.hasDefaultProcessingRoute()) {
            ProcessingRoute dpr = mtb.getDefaultProcessingRoute();

            Template template = project.getTemplate();
            Set<MilestoneTemplate> milestones = dpr.getMilestones();
            String createdBy = project.getCreatedBy();
            OffsetDateTime createdOn = project.getCreatedOn();

            setMilestones(new MilestoneMapper().toProjectMilestones(milestones, template, createdBy, createdOn));
        }

        setDescriptionEnabled(mtb.isDescriptionEnabled());
        setEvidenceApplicability(mtb.getEvidenceApplicability());
        setMaxEvidenceAttachments(mtb.getMaxEvidenceAttachments());
    }

    public Long getValueForMonetarySplitForMilestone(Milestone milestone) {
        if(project.getGrantsRequested() != null) {
            Long grantRequested = project.getGrantsRequested().get(Grant);
            if (project.getTemplate().getMilestoneType().equals(MonetarySplit)
                    && milestone.getMonetarySplit() != null) {
                return  Math.round((milestone.getMonetarySplit() / 100.0) * grantRequested);
            }
        }
        return null;
    }

    @Override
    public boolean allowMultipleVersions() {
        return true;
    }

    // this could be called outside the auth cycle
    public void updateClaimAmounts() {
        for (Milestone milestone : milestones) {

            if (Approved.equals(milestone.getClaimStatus())) {
                boolean reclaimed = false;

                Long monetarySplitForMilestone = getValueForMonetarySplitForMilestone(milestone);
                if (monetarySplitForMilestone != null) {
                    milestone.setClaimedGrant(monetarySplitForMilestone);
                }

                if (milestone.getReclaimedDpf() != null && milestone.getReclaimedDpf() > 0) {
                    reclaimed = true;
                    milestone.setClaimedDpf(milestone.getClaimedDpf() - milestone.getReclaimedDpf());
                }
                if (milestone.getReclaimedRcgf() != null && milestone.getReclaimedRcgf() > 0) {
                    reclaimed = true;
                    milestone.setClaimedRcgf(milestone.getClaimedRcgf() - milestone.getReclaimedRcgf());
                }
                if (milestone.getReclaimedGrant() != null && milestone.getReclaimedGrant() > 0) {
                    reclaimed = true;
                    if (!project.getTemplate().getMilestoneType().equals(MonetarySplit)) {
                        milestone.setClaimedGrant(milestone.getClaimedGrant() - milestone.getReclaimedGrant());
                    }
                }
                if (reclaimed) {
                    milestone.setReclaimed(reclaimed);
                    milestone.setReclaimedDpf(null);
                    milestone.setReclaimedRcgf(null);
                    milestone.setReclaimedGrant(null);
                }
            }
        }
    }

    @Override
    public void approve(String username, OffsetDateTime approvalTime) {
        super.approve(username, approvalTime);
        updateClaimAmounts();
        for (Milestone milestone : milestones) {
            // for approved milestones handle reclaims
            if (milestone.getClaimStatus() == null) {
                milestone.setClaimStatus(Pending);
            }
            if (milestone.getClaimStatus().equals(Claimed)) {
                milestone.setClaimStatus(Approved);
                if (milestone.isKeyEvent()) {
                    project.handleEvent(new OpsEvent(EventType.MilestoneApproval, milestone.getSummary() + " milestone authorised", milestone.getExternalId()));
                }
            }
        }
    }

    public Map<GrantType, Long> getClaims() {
        Long claimedGrant = 0L;
        Long claimedRCGF = 0L;
        Long claimedDPF = 0L;

        for (Milestone milestone : milestones) {
            Long milestoneClaimedGrant = getMilestoneGrantClaimed(milestone.getId());
            claimedGrant += milestoneClaimedGrant != null ? milestoneClaimedGrant : 0;
            claimedGrant -= milestone.getReclaimedGrant() != null ? milestone.getReclaimedGrant() : 0;

            claimedRCGF += milestone.getPendingTotalAmountIncludingReclaimsByType(RCGF);
            claimedDPF += milestone.getPendingTotalAmountIncludingReclaimsByType(DPF);
        }

        Map<GrantType, Long> claims = new HashMap<>();
        claims.put(Grant, claimedGrant);
        claims.put(RCGF, claimedRCGF);
        claims.put(DPF, claimedDPF);
        return claims;
    }

    public Map<GrantType, Long> getMaxClaims() {
        Map<GrantType, Long> maxClaims = new HashMap<>();

        Map<GrantType, Long> grantsRequested = project.getGrantsRequested();
        if (grantsRequested != null) {
            Map<GrantType, Long> claims = getClaims();
            maxClaims.put(Grant, Math.max(0, grantsRequested.get(Grant) - claims.get(Grant)));
            maxClaims.put(RCGF, Math.max(0, grantsRequested.get(RCGF) - claims.get(RCGF)));
            maxClaims.put(DPF, Math.max(0, grantsRequested.get(DPF) - claims.get(DPF)));
        }

        return maxClaims;
    }

    public boolean getApprovalWillCreatePendingPayment() {
        for (Milestone m : milestones) {
            Long milestoneClaimedAmount = getMilestoneGrantClaimed(m.getId());
            if (Claimed.equals(m.getClaimStatus())
                    && ((milestoneClaimedAmount != null && milestoneClaimedAmount > 0)
                    || (m.getClaimedRcgf() != null && m.getClaimedRcgf() > 0)
                    || (m.getClaimedDpf() != null && m.getClaimedDpf() > 0))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if approving this block will create a pending payment only, excluding RCGF and DPF payments.
     */
    public boolean getApprovalWillCreatePendingGrantPayment() {
        for (Milestone m : milestones) {
            Long milestoneClaimedAmount = getMilestoneGrantClaimed(m.getId());
            if (Claimed.equals(m.getClaimStatus())
                    && (milestoneClaimedAmount != null && milestoneClaimedAmount > 0)) {
                return true;
            }
        }
        return false;
    }

    public boolean anyClaimedOrApprovedMilestones() {
        for (Milestone m : milestones) {
            if (m.getClaimStatus() != null && (Claimed.equals(m.getClaimStatus()) || Approved.equals(m.getClaimStatus()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the grant value claimed. If the claim state is Approved the persisted value for that
     * milestone is returned. If it's Claimed, the value is calculated according the split
     * percentage of the milestone. If it's pending, null is returned.
     *
     * @param milestoneId id of the milesto
     * @return Claimed grant for the given milestone
     */
    public Long getMilestoneGrantClaimed(Integer milestoneId) {
        Milestone existingMilestone = getMilestoneById(milestoneId);
        if (Approved.equals(existingMilestone.getClaimStatus()) && project.getGrantSourceAdjustmentAmount() == 0) {
            return existingMilestone.getClaimedGrant();
        } else if (Approved.equals(existingMilestone.getClaimStatus()) || Claimed.equals(existingMilestone.getClaimStatus())
                && getProject().getGrantSourceBlock() != null) {
            Long grantValue = getProject()
                    .getGrantSourceBlock()
                    .getGrantValue();
            if (Boolean.TRUE.equals(existingMilestone.getMonetary()) && grantValue != null) {
                if (MonetarySplit.equals(getProject().getTemplate().getMilestoneType())) {
                    return grantValue * existingMilestone.getMonetarySplit() / 100;
                } else if (MonetaryValue.equals(getProject().getTemplate().getMilestoneType())) {
                    return existingMilestone.getClaimedGrant();
                }
            }
        }
        return null;
    }

    private void compareMilestoneAttachments(ProjectDifferences differences, Milestone thisMilestone, Milestone otherMilestone) {
        Map<Integer, StandardAttachment> thisAttachments = thisMilestone.getAttachments().stream().
                collect(Collectors.toMap(StandardAttachment::getFileId, Function.identity()));
        Map<Integer, StandardAttachment> otherAttachments = otherMilestone.getAttachments().stream().
                collect(Collectors.toMap(StandardAttachment::getFileId, Function.identity()));


        for (Iterator<Integer> iterator = thisAttachments.keySet().iterator(); iterator.hasNext(); ) {
            Integer key = iterator.next();
            if (otherAttachments.get(key) != null) {
                iterator.remove();
                otherAttachments.remove(key);
            }
        }

        // added in this unapproved version
        for (StandardAttachment record : thisAttachments.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Addition));
        }
        // deleted by this unapproved version
        for (StandardAttachment record : otherAttachments.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Deletion));
        }
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        ProjectMilestonesBlock otherMilestonesBlock = (ProjectMilestonesBlock) otherBlock;

        for (Milestone milestone : this.getMilestones()) {
            Milestone otherMilestone = otherMilestonesBlock.getMilestoneByComparison(milestone);
            if (otherMilestone == null) { // added milestone as not in 'left' side
                ProjectDifference difference = new ProjectDifference(milestone);
                difference.setDifferenceType(ProjectDifference.DifferenceType.Addition);
                differences.add(difference);
            } else { // present so compare
                compareMilestoneAttachments(differences, milestone, otherMilestone);

                if (!Objects.equals(StringUtils.trimAllWhitespace(milestone.getSummary()), StringUtils.trimAllWhitespace(otherMilestone.getSummary()))) {
                    differences.add(new ProjectDifference(milestone, "summary"));
                }
                if (!Objects.equals(StringUtils.trimAllWhitespace(milestone.getDescription()), StringUtils.trimAllWhitespace(otherMilestone.getDescription()))) {
                    differences.add(new ProjectDifference(milestone, "description"));
                }
                if (!Objects.equals(milestone.getMilestoneDate(), otherMilestone.getMilestoneDate())) {
                    differences.add(new ProjectDifference(milestone, "milestoneDate"));
                }
                if (!Objects.equals(milestone.getClaimStatus(), otherMilestone.getClaimStatus())) {
                    differences.add(new ProjectDifference(milestone, "claimStatus"));
                }
                if (!Objects.equals(milestone.getMilestoneStatus(), otherMilestone.getMilestoneStatus())) {
                    differences.add(new ProjectDifference(milestone, "milestoneStatus"));
                }
                if (!Objects.equals(milestone.getMonetarySplit(), otherMilestone.getMonetarySplit())) {
                    differences.add(new ProjectDifference(milestone, "monetarySplit"));
                }
                if (!Objects.equals(milestone.getMonetaryValue(), otherMilestone.getMonetaryValue())) {
                    differences.add(new ProjectDifference(milestone, "monetaryValue"));
                }
                if (milestone.isNotApplicable() != otherMilestone.isNotApplicable()) {
                    differences.add(new ProjectDifference(milestone, "notApplicable"));
                }
            }
        }

        for (Milestone otherMilestone : otherMilestonesBlock.getMilestones()) {
            if (this.getMilestoneByComparison(otherMilestone) == null) {
                // no longer present so must have been deleted
                ProjectDifference difference = new ProjectDifference(otherMilestone);
                difference.setDifferenceType(ProjectDifference.DifferenceType.Deletion);
                differences.add(difference);
            }
        }
    }

    private Milestone getMilestoneByComparison(ComparableItem comparableItem) {
        if (comparableItem == null) {
            return null;
        }
        for (Milestone milestone : milestones) {
            if (ComparableItem.areEqual(comparableItem, milestone)) {
                return milestone;
            }
        }
        return null;
    }

    public Map<String, BigDecimal> getTally() {
        BigDecimal forecast = new BigDecimal(0);
        BigDecimal claimed = new BigDecimal(0);
        BigDecimal authorised = new BigDecimal(0);

        for (Milestone milestone : milestones) {
            if (FORECAST.equals(milestone.getMilestoneStatus())
                    || (ACTUAL.equals(milestone.getMilestoneStatus()) && (milestone.getClaimStatus() == null || Pending.equals(milestone.getClaimStatus())))) {
                forecast = addBigDecimals(forecast, milestone.getMonetaryValue());
            }

            if (Claimed.equals(milestone.getClaimStatus())) {
                claimed = addBigDecimals(claimed, milestone.getMonetaryValue());
            }

            if (Approved.equals(milestone.getClaimStatus())) {
                authorised = addBigDecimals(authorised, milestone.getMonetaryValue());
            }
        }

        Map<String, BigDecimal> tally = new HashMap<>();
        tally.put("forecast", forecast);
        tally.put("claimed", claimed);
        tally.put("authorised", authorised);
        tally.put("total", forecast.add(claimed).add(authorised));
        return tally;
    }

    public Integer getMaxEvidenceAttachments() {
        return maxEvidenceAttachments;
    }

    public void setMaxEvidenceAttachments(Integer maxEvidenceAttachments) {
        this.maxEvidenceAttachments = maxEvidenceAttachments;
    }

    public MilestonesTemplateBlock.EvidenceApplicability getEvidenceApplicability() {
        return evidenceApplicability;
    }

    public void setEvidenceApplicability(MilestonesTemplateBlock.EvidenceApplicability evidenceApplicability) {
        this.evidenceApplicability = evidenceApplicability;
    }
}
