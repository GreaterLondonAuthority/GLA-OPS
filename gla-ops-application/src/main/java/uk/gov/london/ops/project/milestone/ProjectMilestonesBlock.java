/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.milestone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.framework.MilestoneComparator;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.StandardAttachment;
import uk.gov.london.ops.project.block.*;
import uk.gov.london.ops.project.grant.GrantSourceBlock;
import uk.gov.london.ops.project.grant.GrantType;
import uk.gov.london.ops.project.implementation.mapper.MilestoneMapper;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportUtils;
import uk.gov.london.ops.project.template.domain.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;
import static uk.gov.london.ops.payment.PaymentSource.GRANT;
import static uk.gov.london.ops.project.claim.ClaimStatus.*;
import static uk.gov.london.ops.project.grant.GrantType.*;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.ReportPrefix;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.ReportSuffix;
import static uk.gov.london.ops.project.template.domain.Template.MilestoneType.MonetarySplit;
import static uk.gov.london.ops.project.template.domain.Template.MilestoneType.MonetaryValue;

/**
 * The Milestones block in a Project.
 *
 * @author Steve Leach
 */
@Entity(name = "milestones_block")
@DiscriminatorValue("MILESTONES")
@JoinData(sourceTable = "milestones_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
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
        if (!isVisited() || getValidationFailures().size() > 0) {
            return false;
        }

        if (claimedExceeded()) {
            return false;
        }

        if (!isOfTypeMonetaryValue()) {
            return isCompleteForNonMonetaryValueMilestonesBlock();
        }

        return true;
    }

    private boolean isOfTypeMonetaryValue() {
        return Template.MilestoneType.MonetaryValue.equals(project.getTemplate().getMilestoneType());
    }

    private boolean isCompleteForNonMonetaryValueMilestonesBlock() {
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

                if (milestone.getMilestoneStatus() == null) {
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

        return !hasMonetaryMilestones || (totalSplit == 100);
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
        Long grantSourceTotal = grantSource.getTotalGrantRequested();

        Long milestonesTotal = 0L;
        for (Milestone milestone : milestones) {
            Long claims = milestone.calculateTotalValueClaimed();
            if (claims == 0 && milestone.getMonetaryValue()
                    != null) { // if first time claimed and therefore no claim values stored we can use the initial monetary value
                claims = milestone.getMonetaryValue().longValue();
            }
            Long reclaims = milestone.calculateTotalValueReclaimed();
            milestonesTotal = nullSafeAdd(milestonesTotal, claims - reclaims);
        }

        return milestonesTotal > grantSourceTotal;
    }

    /**
     * This method checks if any of the milestones exceeds the max amount to claim by Grant Type. For example, if a milestone
     * claim a right Grant amount, but exceeds the DPF amount, that milestone appears as it has exceeded the claimed amount.
     * <p>
     * This algorithm tries to minimizes the number of milestone that break that limit. For example if we have two milestones, one
     * with value 50 and the other with 51, having a maximum claimed amount of 100. Then this algorithm will show only one of them
     * as exceeded, but doesn't guarantee that it's the lower one. That the depends on the order of the milestones in the block.
     */
    private boolean claimedExceeded() {

        final Map<GrantType, Long> claimed = new HashMap<>();

        claimed.put(GrantType.Grant, 0L);
        claimed.put(GrantType.RCGF, 0L);
        claimed.put(GrantType.DPF, 0L);

        Map<GrantType, BigDecimal> grantsRequested = project.getGrantsRequested();

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
                        if (isOfTypeMonetaryValue()) {
                            milestoneTypedValue = milestone.getPendingTotalAmountIncludingReclaimsByType(type);
                        }
                        break;
                    case DPF:
                    case RCGF:
                        milestoneTypedValue = milestone.getPendingTotalAmountIncludingReclaimsByType(type);
                        break;
                    default:
                        throw new RuntimeException("Unrecognised grant type: " + type);
                }
                //Temporal claimed amount which is the sum of all milestones already processed
                //in this loop(if none of its grant type amounts exceeded the max amount)
                //plus the current one
                claimed.put(type, claimed.get(type) + milestoneTypedValue);

            }

            if (grantsRequested.get(type) != null && claimed.get(type) > grantsRequested.get(type).longValue()) {
                isExceeded = true;
            }
        }

        if (isExceeded) {
            for (Milestone milestone : milestones) {
                if (milestone.getClaimStatus() != null) {
                    switch (milestone.getClaimStatus()) {
                        case Approved:
                            if (milestone.getReclaimedRcgf() != null || milestone.getReclaimedDpf() != null
                                    || milestone.getReclaimedGrant() != null) {
                                milestone.setClaimedExceeded(true);
                            }
                            break;
                        case Claimed:
                            if (milestone.getClaimedRcgf() != null || milestone.getClaimedDpf() != null
                                    || milestone.getClaimedGrant() != null) {
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
        if (isOfTypeMonetaryValue()) {
            if (isMonetaryMilestoneTotalGreaterThanGrantSource()) {
                this.addErrorMessage("GRANT_SOURCE_EXCEEDED", "grant", "grant source exceeded");
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
            if (fromMilestone == null) {
                throw new ValidationException("Invalid id for milestones");
            }

            toMilestone.setMilestoneMarkedCorporate(fromMilestone.isMilestoneMarkedCorporate());

            if (!toMilestone.isClaimed()) {
                boolean needToSetDate = (toMilestone.getMilestoneDate() == null && fromMilestone.getMilestoneDate() != null) || (
                        toMilestone.getMilestoneDate() != null && fromMilestone.getMilestoneDate() == null);
                boolean bothDatesPresent = toMilestone.getMilestoneDate() != null && fromMilestone.getMilestoneDate() != null;
                boolean needToUpdateDate =
                        bothDatesPresent && fromMilestone.getMilestoneDate().compareTo(toMilestone.getMilestoneDate()) != 0;

                if (needToSetDate || needToUpdateDate) {
                    toMilestone.setMilestoneDate(fromMilestone.getMilestoneDate());
                }

                MilestonesTemplateBlock milestonesTemplateBlock = getMilestoneTemplateBlock();
                if (!milestonesTemplateBlock.getAutoCalculateMilestoneState()
                        && fromMilestone.getMilestoneDate() != null
                        && fromMilestone.getMilestoneDate().isBefore(LocalDate.now().plusDays(1))) {
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

    private MilestonesTemplateBlock getMilestoneTemplateBlock() {
        return (MilestonesTemplateBlock ) project.getTemplate().getSingleBlockByType(ProjectBlockType.Milestones);
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

    public Milestone getMilestoneBySummary(String summary) {
        for (Milestone milestone : milestones) {
            if (milestone.getSummary() != null && milestone.getSummary().equalsIgnoreCase(summary)) {
                return milestone;
            }
        }
        return null;
    }

    public Map<GrantType, Long> getAvailableToReclaimByType() {
        Map<GrantType, Long> current = project.getCurrentGrantSourceValuesByType();
        if (current == null) {
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
    public Map<String, Object> simpleDataExtract(final SimpleProjectExportConfig simpleProjectExportConfig) {
        Map<String, Object> map = new HashMap<>();
        for (Milestone milestone : getMilestones()) {
            if (!milestone.isManuallyCreated()) {
                final String msPrefix = milestone.getSummary() != null ? milestone.getSummary() : "id_" + milestone.getId();
                String exportString = ReportPrefix.ms_.name() + SimpleProjectExportUtils.formatForExport(msPrefix);
                map.put(exportString + ReportSuffix._date, milestone.getMilestoneDate());
                map.put(exportString + ReportSuffix._status, milestone.getMilestoneStatus());
                map.put(exportString + ReportSuffix._percentage, milestone.getMonetarySplit());
            }
        }
        if (processingRouteId != null) {
            MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) project.getTemplate()
                    .getSingleBlockByType(ProjectBlockType.Milestones);
            ProcessingRoute pr = milestonesTemplateBlock.getProcessingRoute(processingRouteId);
            if (pr != null) {
                map.put(ReportPrefix.ms_.name() + ReportSuffix.processing_route.name(), pr.getName());
            }
        }
        if (!map.containsKey(ReportPrefix.ms_.name() + ReportSuffix.processing_route.name())) {
            map.put(ReportPrefix.ms_.name() + ReportSuffix.processing_route.name(), "");
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
        if (project.getGrantsRequested() != null) {
            BigDecimal grantRequested = project.getGrantsRequested().get(Grant);
            if (project.getTemplate().getMilestoneType().equals(MonetarySplit)
                    && milestone.getMonetarySplit() != null) {
                return BigDecimal.valueOf((milestone.getMonetarySplit() / 100.0)).multiply(grantRequested)
                        .setScale(0, RoundingMode.HALF_DOWN).longValue();
            }
        }
        return null;
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
                        milestone.setMonetaryValue(new BigDecimal(milestone.calculateTotalValueClaimed()));
                    }
                }
                if (reclaimed) {
                    milestone.setReclaimed(true);
                    milestone.setReclaimedDpf(null);
                    milestone.setReclaimedRcgf(null);
                    milestone.setReclaimedGrant(null);
                }
            }
        }
    }

    @Override
    protected void performPostApprovalActions(String username, OffsetDateTime approvalTime) {
        updateClaimAmounts();
        for (Milestone milestone : milestones) {
            // for approved milestones handle reclaims
            if (milestone.getClaimStatus() == null) {
                milestone.setClaimStatus(
                        Pending);
            }
            if (milestone.getClaimStatus().equals(Claimed)) {
                milestone.setClaimStatus(Approved);
                if (milestone.isKeyEvent()) {
                    project.handleEvent(
                            new OpsEvent(EventType.MilestoneApproval, milestone.getSummary() + " milestone authorised",
                                    milestone.getExternalId()));
                }
            }
        }
    }

    public Map<GrantType, Long> getClaims() {
        long claimedGrant = 0L;
        long claimedRCGF = 0L;
        long claimedDPF = 0L;

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

        Map<GrantType, BigDecimal> grantsRequested = project.getGrantsRequested();
        if (grantsRequested != null) {
            Map<GrantType, Long> claims = getClaims();
            maxClaims.put(Grant, Math.max(0, grantsRequested.get(Grant).longValue() - claims.get(Grant)));
            maxClaims.put(RCGF, Math.max(0, grantsRequested.get(RCGF).longValue() - claims.get(RCGF)));
            maxClaims.put(DPF, Math.max(0, grantsRequested.get(DPF).longValue() - claims.get(DPF)));
        }

        return maxClaims;
    }

    @Override
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

        return getApprovalWillCreatePendingAdjustmentGrantPayment();
    }

    /**
     * @return true if approving this block will create a pending payment only, excluding RCGF and DPF payments.
     */
    @Override
    public Set<String> getPaymentsSourcesCreatedViaApproval() {
        for (Milestone m : milestones) {
            Long milestoneClaimedAmount = getMilestoneGrantClaimed(m.getId());
            if (Claimed.equals(m.getClaimStatus())
                    && (milestoneClaimedAmount != null && milestoneClaimedAmount > 0)) {
                TemplateBlock singleBlockByType = this.getProject().getTemplate()
                        .getSingleBlockByType(ProjectBlockType.GrantSource);
                if (singleBlockByType instanceof GrantSourceTemplateBlock) {
                    GrantSourceTemplateBlock grantSourceTemplateBlock = (GrantSourceTemplateBlock) singleBlockByType;
                    Set<String> paymentSources = grantSourceTemplateBlock.getPaymentSources();
                    return paymentSources.isEmpty() ? Collections.singleton(GRANT) : paymentSources;
                }
                return Collections.singleton(GRANT);
            }
        }

        return getApprovalWillCreatePendingAdjustmentGrantPayment() ? Collections.singleton(GRANT) : Collections.emptySet();
    }

    @Override
    public boolean getApprovalWillCreatePendingReclaim() {
        for (Milestone milestone : milestones) {
            if (milestone.getReclaimedGrant() != null && milestone.getReclaimedGrant() > 0) {
                return true;
            }
            if (milestone.getReclaimedDpf() != null && milestone.getReclaimedDpf() > 0) {
                return true;
            }
            if (milestone.getReclaimedRcgf() != null && milestone.getReclaimedRcgf() > 0) {
                return true;
            }
        }
        // negative indicates new grant is less than old grant
        return project.getTemplate().getMilestoneType().equals(MonetarySplit) && hasApprovedMonetaryMilestones()
                && project.getGrantSourceAdjustmentAmount().signum() < 0;
    }

    /**
     * @return true if approving this block on an Active project will result in a supplemantey or a reclaim pending payment being
     * generated.
     */
    private boolean getApprovalWillCreatePendingAdjustmentGrantPayment() {
        return MonetarySplit.equals(getProject().getTemplate().getMilestoneType())
                && hasApprovedMonetaryMilestones()
                && getProject().getGrantSourceAdjustmentAmount() != null
                && !BigDecimal.ZERO.equals(getProject().getGrantSourceAdjustmentAmount());
    }

    /**
     * @return true if any of the monetary milestones have been claimed with a non zero value.
     */
    public boolean hasClaimedMonetaryMilestones() {
        for (Milestone m : milestones) {
            if (m.isClaimed() && m.hasMonetaryValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if any of the monetary milestones have been claimed with a non zero value.
     */
    public boolean hasApprovedMonetaryMilestones() {
        for (Milestone m : milestones) {
            if (m.isApproved() && m.hasMonetaryValue()) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean areAllMonetaryMilestonesClaimedOrApproved() {
        for (Milestone m : milestones) {
            if (m.getMonetary() && m.getMonetarySplit() > 0 && (m.getClaimStatus() == null || Pending
                    .equals(m.getClaimStatus()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the grant value claimed. If the claim state is Approved the persisted value for that milestone is returned. If it's
     * Claimed, the value is calculated according the split percentage of the milestone. If it's pending, null is returned.
     *
     * @param milestoneId id of the milesto
     * @return Claimed grant for the given milestone
     */
    public Long getMilestoneGrantClaimed(Integer milestoneId) {
        Milestone existingMilestone = getMilestoneById(milestoneId);
        if (Approved.equals(existingMilestone.getClaimStatus()) && BigDecimal.ZERO
                .equals(project.getGrantSourceAdjustmentAmount())) {
            return existingMilestone.getClaimedGrant();
        } else if ((Approved.equals(existingMilestone.getClaimStatus()) || Claimed.equals(existingMilestone.getClaimStatus()))
                && getProject().getGrantSourceBlock() != null) {
            Long grantValue = getProject()
                    .getGrantSourceBlock()
                    .getGrantValue();
            if (Boolean.TRUE.equals(existingMilestone.getMonetary()) && grantValue != null) {
                if (MonetarySplit.equals(getProject().getTemplate().getMilestoneType())) {
                    return getValueForMonetarySplitForMilestone(existingMilestone);
                } else if (MonetaryValue.equals(getProject().getTemplate().getMilestoneType())) {
                    return existingMilestone.getClaimedGrant();
                }
            }
        }
        return null;
    }

    private void compareMilestoneAttachments(ProjectDifferences differences, Milestone thisMilestone, Milestone otherMilestone) {
        Map<Integer, StandardAttachment> thisAttachments = thisMilestone.getAttachments().stream()
                .collect(Collectors.toMap(StandardAttachment::getFileId, Function.identity()));
        Map<Integer, StandardAttachment> otherAttachments = otherMilestone.getAttachments().stream()
                .collect(Collectors.toMap(StandardAttachment::getFileId, Function.identity()));

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

                if (!Objects.equals(StringUtils.trimAllWhitespace(milestone.getSummary()),
                        StringUtils.trimAllWhitespace(otherMilestone.getSummary()))) {
                    differences.add(new ProjectDifference(milestone, "summary"));
                }
                if (!Objects.equals(StringUtils.trimAllWhitespace(milestone.getDescription()),
                        StringUtils.trimAllWhitespace(otherMilestone.getDescription()))) {
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

    public Map<String, Totals> getTotals() {
        Map<String, Totals> tally = new HashMap<>();

        Map<GrantType, BigDecimal> grantsRequested = project.getGrantsRequested();

        Totals approved = new Totals();
        Totals claimed = new Totals();
        Totals balance = new Totals();

        for (Milestone milestone : milestones) {
            if (Claimed.equals(milestone.getClaimStatus())) {
                claimed.addValue(Grant, milestone.getClaimedGrant());
                claimed.addValue(RCGF, milestone.getClaimedRcgf());
                claimed.addValue(DPF, milestone.getClaimedDpf());
            }

            if (Approved.equals(milestone.getClaimStatus())) {
                approved.addValue(Grant, milestone.getClaimedGrant());
                approved.addValue(RCGF, milestone.getClaimedRcgf());
                approved.addValue(DPF, milestone.getClaimedDpf());

                claimed.subtractValue(Grant, milestone.getReclaimedGrant());
                claimed.subtractValue(RCGF, milestone.getReclaimedRcgf());
                claimed.subtractValue(DPF, milestone.getReclaimedDpf());
            }
        }

        if (grantsRequested != null) {
            for (GrantType grantType : grantsRequested.keySet()) {
                balance.addValue(grantType, grantsRequested.get(grantType).longValue());
                balance.subtractValue(grantType, claimed.getValues().get(grantType));
                balance.subtractValue(grantType, approved.getValues().get(grantType));
            }
        }

        tally.put("approved", approved);
        tally.put("claimed", claimed);
        tally.put("balance", balance);

        return tally;
    }

    public static class Totals {

        Map<GrantType, Long> values = new HashMap<>();

        public Map<GrantType, Long> getValues() {
            return values;
        }

        public Long getTotal() {
            return values.values().stream().mapToLong(Long::longValue).sum();
        }

        public void addValue(GrantType grantType, Long value) {
            if (value != null) {
                values.merge(grantType, value, (a, b) -> a + b);
            }
        }

        public void subtractValue(GrantType grantType, Long value) {
            if (value != null) {
                addValue(grantType, -value);
            }
        }
    }

    public boolean isPaymentsEnabled() {
        Project project = getProject();
        if (project != null && project.getProgrammeTemplate() != null) {
            return project.getProgrammeTemplate().isPaymentsEnabled();
        }
        return false;
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

    @Override
    public boolean isSelfContained() {
        return false;
    }

}
