/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.domain.attachment.StandardAttachment;
import uk.gov.london.ops.domain.template.Requirement;
import uk.gov.london.ops.util.jpajoins.NonJoin;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import static uk.gov.london.ops.domain.project.ClaimStatus.Approved;
import static uk.gov.london.ops.domain.project.ClaimStatus.Claimed;
import static uk.gov.london.ops.domain.project.MilestoneStatus.ACTUAL;
import static uk.gov.london.ops.domain.project.MilestoneStatus.FORECAST;

/**
 * Entity to represent a project milestone
 */
@Entity(name="milestone")
public class Milestone implements OpsEntity<Integer>, ComparableItem {

    public static final Integer ACQUISITION_ID = 3005;
    public static final Integer START_ON_SITE_ID = 3003;
    public static final Integer COMPLETION_ID = 3004;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "milestone_seq_gen")
    @SequenceGenerator(name = "milestone_seq_gen", sequenceName = "milestone_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name = "external_id")
    @NonJoin("External ID is just a unique key to identify similar milestones across projects.")
    private Integer externalId;

    @Column(name = "summary")
    private String summary;

    @Column(name = "description")
    private String description;

    @Column(name="requirement")
    @Enumerated(EnumType.STRING)
    private Requirement requirement;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name="milestone_date")
    private LocalDate milestoneDate;

    /** This will map with the UI grant payment percentage. */
    @Column(name = "monetary_split")
    private Integer monetarySplit;

    @Column(name = "monetary_value")
    private BigDecimal monetaryValue;

    @Column(name = "monetary")
    private Boolean monetary;

    @Column(name="milestone_status")
    @Enumerated(EnumType.STRING)
    private MilestoneStatus milestoneStatus;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name="modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name="modified_by")
    private String modifiedBy;

    @Column(name = "manually_created", updatable = false)
    private Boolean manuallyCreated = false;

    @Column(name = "conditional", updatable = false)
    private Boolean conditional = false;

    @Column(name="claim_status")
    @Enumerated(EnumType.STRING)
    private ClaimStatus claimStatus;

    @Column(name="claimed_grant")
    private Long claimedGrant;

    @Column(name="claimed_rcgf")
    private Long claimedRcgf;

    @Column(name="claimed_dpf")
    private Long claimedDpf;

   @Column(name="reclaimed_grant")
    private Long reclaimedGrant;

    @Column(name="reclaimed_rcgf")
    private Long reclaimedRcgf;

    @Column(name="reclaimed_dpf")
    private Long reclaimedDpf;

    @Column(name="reclaim_reason")
    private String reclaimReason;

    @Column(name="reclaimed")
    private Boolean reclaimed;

    @Column(name="key_event")
    private boolean keyEvent;

    @Column(name="na_selectable")
    private boolean naSelectable;

    @Column(name="not_applicable")
    private boolean notApplicable;

    @Transient
    private boolean claimedExceeded;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = StandardAttachment.class)
    @JoinColumn(name = "milestone_id")
    private Set<StandardAttachment> attachments = new HashSet<>();


    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public LocalDate getMilestoneDate() {
        return milestoneDate;
    }

    public void setMilestoneDate(LocalDate milestoneDate) {
        this.milestoneDate = milestoneDate;
    }

    public Integer getMonetarySplit() {
        return monetarySplit;
    }

    public void setMonetarySplit(Integer monetarySplit) {
        this.monetarySplit = monetarySplit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMonetaryValue() {
        return monetaryValue;
    }

    public void setMonetaryValue(BigDecimal monetaryValue) {
        this.monetaryValue = monetaryValue;
    }

    public MilestoneStatus getMilestoneStatus() {
        return milestoneStatus;
    }

    public void setMilestoneStatus(MilestoneStatus milestoneStatus) {
        this.milestoneStatus = milestoneStatus;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getMonetary() {
        return monetary;
    }

    public void setMonetary(Boolean monetary) {
        this.monetary = monetary;
    }

    public Boolean isManuallyCreated() {
        return manuallyCreated;
    }

    public void setManuallyCreated(Boolean manuallyCreated) {
        this.manuallyCreated = manuallyCreated;
    }

    public Boolean isConditional() {
        return conditional;
    }

    public void setConditional(Boolean conditional) {
        this.conditional = conditional;
    }

    public Long getReclaimedGrant() {
        return reclaimedGrant;
    }

    public void setReclaimedGrant(Long reclaimedGrant) {
        this.reclaimedGrant = reclaimedGrant;
    }

    public Long getReclaimedRcgf() {
        return reclaimedRcgf;
    }

    public void setReclaimedRcgf(Long reclaimedRcgf) {
        this.reclaimedRcgf = reclaimedRcgf;
    }

    public Long getReclaimedDpf() {
        return reclaimedDpf;
    }

    public void setReclaimedDpf(Long reclaimedDpf) {
        this.reclaimedDpf = reclaimedDpf;
    }

    public String getReclaimReason() {
        return reclaimReason;
    }

    public void setReclaimReason(String reclaimReason) {
        this.reclaimReason = reclaimReason;
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
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
    }

    /**
     * This value is supposed to be populated when the project is about to generate the payments
     * for authorisation, so it should return null until the milestone is Approved.
     * Please see the method getMilestoneGrantClaimed in  {@link ProjectMilestonesBlock} instead
     * @return claimed grant once is persisted
     */
    public Long getClaimedGrant() {
        return claimedGrant;
    }

    public void setClaimedGrant(Long claimedGrant) {
        this.claimedGrant = claimedGrant;
    }

    public Long getClaimedRcgf() {
        return claimedRcgf;
    }

    public void setClaimedRcgf(Long claimedRcgf) {
        this.claimedRcgf = claimedRcgf;
    }

    public Long getClaimedDpf() {
        return claimedDpf;
    }

    public void setClaimedDpf(Long claimedDpf) {
        this.claimedDpf = claimedDpf;
    }

    public boolean isKeyEvent() {
        return keyEvent;
    }

    public void setKeyEvent(boolean keyEvent) {
        this.keyEvent = keyEvent;
    }

    public boolean isNaSelectable() {
        return naSelectable;
    }

    public void setNaSelectable(boolean naSelectable) {
        this.naSelectable = naSelectable;
    }

    public boolean isNotApplicable() {
        return notApplicable;
    }

    public void setNotApplicable(boolean notApplicable) {
        this.notApplicable = notApplicable;
    }

    public void copyDataInto(Milestone target) {
        target.setDisplayOrder(this.getDisplayOrder());
        target.setConditional(this.isConditional());
        target.setMonetary(this.getMonetary());
        target.setManuallyCreated(this.isManuallyCreated());
        target.setExternalId(this.getExternalId());
        target.setSummary(this.getSummary());
        target.setDescription(this.getDescription());
        target.setMilestoneDate(this.getMilestoneDate());
        target.setMilestoneStatus(this.getMilestoneStatus());
        target.setCreatedBy(this.getCreatedBy());
        target.setCreatedOn(this.getCreatedOn());
        target.setMonetarySplit(this.getMonetarySplit());
        target.setMonetaryValue(this.getMonetaryValue());
        target.setRequirement(this.getRequirement());
        target.claimStatus = this.claimStatus;
        target.claimedGrant = this.claimedGrant;
        target.claimedRcgf = this.claimedRcgf;
        target.claimedDpf = this.claimedDpf;
        target.keyEvent = this.keyEvent;
        target.naSelectable = this.naSelectable;
        target.notApplicable = this.notApplicable;
        target.setReclaimed(this.isReclaimed());
        target.setReclaimReason(this.getReclaimReason());
        target.setReclaimedDpf(this.getReclaimedDpf());
        target.setReclaimedRcgf(this.getReclaimedRcgf());
        target.setReclaimedGrant(this.getReclaimedGrant());
        if(this.getAttachments() != null) {
            for (final StandardAttachment attachment : this.getAttachments()) {
                target.getAttachments().add(attachment.copy());
            }
        }
    }

    public boolean isClaimedExceeded() {
        return claimedExceeded;
    }

    public void setClaimedExceeded(boolean claimedExceeded) {
        this.claimedExceeded = claimedExceeded;
    }

    public void autoUpdateMilestoneStatus() {
        if (milestoneDate != null) {
            setMilestoneStatus(milestoneDate.isAfter(LocalDate.now()) ? FORECAST : ACTUAL);
        } else {
            setMilestoneStatus(null);
        }
    }

    public Long getPendingTotalAmountIncludingReclaimsByType(GrantType grantType) {
        Long result = 0L;
        switch (grantType) {
            case Grant:
                result = (claimedGrant == null ? 0 : claimedGrant) - (reclaimedGrant == null ? 0 : reclaimedGrant);
                break;
            case DPF:
                result = (claimedDpf == null ? 0 : claimedDpf) - (reclaimedDpf == null ? 0 : reclaimedDpf);
                break;
            case RCGF:
                result = (claimedRcgf == null ? 0 : claimedRcgf) - (reclaimedRcgf == null ? 0 : reclaimedRcgf);
                break;
        }
        return result;
    }

    /**
     * Returns a calculated comparison ID so UI can match between milestones.
     * @return external ID if present otherwise milestone name
     */
    public String getComparisonId() {
        return externalId == null ? getSummary() : String.valueOf(externalId);
    }

    /**
     * @return the sub type to be used when generating a payment from this milestone.
     */
    public String getPaymentSubType() {
        return (monetaryValue != null ? "Bespoke " : "") + summary;
    }

    public boolean isClaimed() {
        return Claimed.equals(claimStatus) || Approved.equals(claimStatus);
    }

    public boolean isApproved() {
        return Approved.equals(claimStatus);
    }

    public Set<StandardAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<StandardAttachment> attachments) {
        this.attachments = attachments;
    }

    public Boolean isReclaimed() {
        return reclaimed;
    }

    public void setReclaimed(Boolean reclaimed) {
        this.reclaimed = reclaimed;
    }
}
