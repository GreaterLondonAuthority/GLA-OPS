/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;
import static uk.gov.london.common.skills.SkillsGrantType.AEB_GRANT;
import static uk.gov.london.common.skills.SkillsGrantType.AEB_PROCURED;
import static uk.gov.london.ops.project.claim.ClaimStatus.Approved;
import static uk.gov.london.ops.project.claim.ClaimStatus.Claimed;
import static uk.gov.london.ops.project.claim.Claimable.CLAIM_STATUS_OVER_PAID;
import static uk.gov.london.ops.project.claim.Claimable.CLAIM_STATUS_PARTLY_PAID;
import static uk.gov.london.ops.project.skills.LearningGrantEntryType.DELIVERY;
import static uk.gov.london.ops.project.skills.LearningGrantEntryType.SUPPORT;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.block.FundingSourceProvider;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.project.block.ProjectDifferences;
import uk.gov.london.ops.project.claim.Claim;
import uk.gov.london.ops.project.claim.ClaimStatus;
import uk.gov.london.ops.project.grant.GrantType;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.template.domain.LearningGrantTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;

@Entity(name = "learning_grant_block")
@DiscriminatorValue("LearningGrant")
@JoinData(sourceTable = "learning_grant_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the learning grant block is a subclass of the project block and shares a common key")
public class LearningGrantBlock extends NamedProjectBlock implements FundingSourceProvider {

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "number_of_years")
    private Integer numberOfYears;

    @Column(name = "total_allocation")
    private BigDecimal totalAllocation;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = LearningGrantAllocation.class)
    @JoinColumn(name = "block_id")
    private List<LearningGrantAllocation> allocations = new ArrayList<>();

    @NotNull
    @Column(name = "grant_type")
    @Enumerated(EnumType.STRING)
    private SkillsGrantType grantType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = LearningGrantEntry.class)
    @JoinColumn(name = "block_id")
    private List<LearningGrantEntry> learningGrantEntries = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinData(joinType = Join.JoinType.OneToMany, comment = "join to claim")
    @JoinColumn(name = "block_id")
    private Set<Claim> claims = new HashSet<>();

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getNumberOfYears() {
        return numberOfYears;
    }

    public void setNumberOfYears(Integer numberOfYears) {
        this.numberOfYears = numberOfYears;
    }

    public BigDecimal getTotalAllocation() {
        return totalAllocation;
    }

    public void setTotalAllocation(BigDecimal totalAllocation) {
        this.totalAllocation = totalAllocation;
    }

    public List<LearningGrantAllocation> getAllocations() {
        return allocations;
    }

    public LearningGrantAllocation getAllocation(Integer year) {
        return allocations.stream().filter(e -> e.getYear().equals(year)).findFirst().orElse(null);
    }

    public void setAllocations(List<LearningGrantAllocation> allocations) {
        this.allocations = allocations;
    }

    public void setAllocation(Integer year, BigDecimal allocation) {
        getAllocation(year).setAllocation(allocation);
    }

    public void setLearnerSupportAllocation(Integer year, BigDecimal allocation) {
        getAllocation(year).setLearnerSupportAllocation(allocation);
    }

    public SkillsGrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(SkillsGrantType grantType) {
        this.grantType = grantType;
    }

    public List<LearningGrantEntry> getLearningGrantEntries() {
        return learningGrantEntries;
    }

    @JsonIgnore
    public List<LearningGrantEntry> getLearningGrantEntries(LearningGrantEntryType type, Integer academicYear) {
        return learningGrantEntries.stream().filter(lge -> lge.getType() == type && lge.getAcademicYear().equals(academicYear))
                .collect(Collectors.toList());
    }

    public void setLearningGrantEntries(List<LearningGrantEntry> learningGrantEntries) {
        this.learningGrantEntries = learningGrantEntries;
    }

    public Set<Claim> getClaims() {
        return claims;
    }

    public List<Claim> getClaims(ClaimStatus claimStatus) {
        return claims.stream().filter(claim -> claimStatus.equals(claim.getClaimStatus())).collect(Collectors.toList());
    }

    public void setClaims(Set<Claim> claims) {
        this.claims = claims;
    }

    @Override
    public boolean isComplete() {

        if (!isValid()) {
            return false;
        }

        if (numberOfYears > 1 && totalAllocation == null) {
            return false;
        }

        if (totalYearlyAllocationExceedsTotalProjectAllocation()) {
            return false;
        }

        LearningGrantAllocation allocation = getAllocation(this.startYear);
        if (allocation != null) {
            if (allocation.getAllocation() == null || allocation.getAllocation().equals(BigDecimal.ZERO)) {
                return false;
            } else {
                if (grantType.equals(AEB_GRANT) && learningGrantEntries != null && !learningGrantEntries.isEmpty()) {
                    if (learningGrantEntries.stream().filter(entry -> !entry.isReturn())
                            .filter(entry -> entry.getPercentage() == null).count() > 0) {
                        return false;
                    }
                }
            }

            if (grantType.equals(AEB_PROCURED) && allocation.getLearnerSupportAllocation() == null) {
                return false;
            } else {
                if (grantType.equals(AEB_PROCURED) && learningGrantEntries != null && !learningGrantEntries.isEmpty()) {
                    return learningGrantEntries.stream().filter(entry -> !entry.isReturn())
                            .filter(entry -> entry.getPercentage() == null).count() <= 0;
                }
            }
        }

        return true;
    }

    @Override
    protected void generateValidationFailures() {
        if (totalYearlyAllocationExceedsTotalProjectAllocation()) {
            this.addErrorMessage("TOTAL_ALLOCATION_EXCEEDED", "",
                    "The sum of the yearly allocations exceeds the total project allocation. Please amend.");
        }
        if (AEB_GRANT.equals(grantType) && communityAllocationExceedsDeliveryAllocation()) {
            this.addErrorMessage("COMMUNITY_ALLOCATION_EXCEEDED", "communityAllocation",
                    "Community Learning Allocation cannot exceed Delivery Allocation.");
        }
    }

    private boolean communityAllocationExceedsDeliveryAllocation() {
        BigDecimal deliveryAllocationTotal = BigDecimal.ZERO;
        BigDecimal communityAllocationTotal = BigDecimal.ZERO;
        for (LearningGrantAllocation allocation : allocations) {
            deliveryAllocationTotal = nullSafeAdd(deliveryAllocationTotal, allocation.getAllocation());
            communityAllocationTotal = nullSafeAdd(communityAllocationTotal, allocation.getCommunityAllocation());
        }
        return communityAllocationTotal.compareTo(deliveryAllocationTotal) > 0;
    }

    boolean totalYearlyAllocationExceedsTotalProjectAllocation() {
        return (totalAllocation != null) && getTotalYearlyAllocation().compareTo(totalAllocation) > 0;
    }

    public BigDecimal getTotalYearlyAllocation() {
        return nullSafeAdd(getTotalAllocations(), getTotalLearningSupportAllocations());
    }

    private BigDecimal getTotalAllocations() {
        BigDecimal totalAllocation = BigDecimal.ZERO;
        for (LearningGrantAllocation allocation : allocations) {
            totalAllocation = nullSafeAdd(totalAllocation, allocation.getAllocation());
        }
        return totalAllocation;
    }

    private BigDecimal getTotalLearningSupportAllocations() {
        BigDecimal totalLearningSupportAllocation = BigDecimal.ZERO;
        for (LearningGrantAllocation allocation : allocations) {
            totalLearningSupportAllocation = nullSafeAdd(totalLearningSupportAllocation,
                    allocation.getLearnerSupportAllocation());
        }
        return totalLearningSupportAllocation;
    }


    @Override
    public void merge(NamedProjectBlock block) {
        LearningGrantBlock updated = (LearningGrantBlock) block;

        for (LearningGrantAllocation updatedAllocation : updated.getAllocations()) {
            LearningGrantAllocation existingAllocation = this.getAllocation(updatedAllocation.getYear());
            existingAllocation.setAllocation(updatedAllocation.getAllocation());
            existingAllocation.setCommunityAllocation(updatedAllocation.getCommunityAllocation());
            existingAllocation.setLearnerSupportAllocation(updatedAllocation.getLearnerSupportAllocation());
            existingAllocation.setDeliveryAllocationEditingInProgress(updatedAllocation.isDeliveryAllocationEditingInProgress());
            existingAllocation.setSupportAllocationEditingInProgress(updatedAllocation.isSupportAllocationEditingInProgress());
        }

        // if its a single year grant block, the total doesnt display,
        // but we still want to copy the allocation for reporting purposes GLA-25807
        if (numberOfYears == 1) {
            this.setTotalAllocation(this.getAllocation(startYear).getAllocation());
        } else {
            this.setTotalAllocation(updated.getTotalAllocation());
        }

        for (LearningGrantEntry updatedEntry : updated.getLearningGrantEntries()) {
            LearningGrantEntry existingEntry = this
                    .getLearningGrantEntry(updatedEntry.getAcademicYear(), updatedEntry.getPeriod(), updatedEntry.getType());
            existingEntry.setAllocation(updatedEntry.getAllocation());
            existingEntry.setPaymentDue(updatedEntry.getPaymentDue());
        }
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        if (templateBlock instanceof LearningGrantTemplateBlock) {
            LearningGrantTemplateBlock learningGrantTemplateBlock = (LearningGrantTemplateBlock) templateBlock;
            setStartYear(learningGrantTemplateBlock.getStartYear());
            setNumberOfYears(learningGrantTemplateBlock.getNumberOfYears());
            setGrantType(learningGrantTemplateBlock.getGrantType());

            for (Integer year = startYear; year < startYear + numberOfYears; year++) {
                this.getAllocations().add(new LearningGrantAllocation(year));

                for (int period = 1; period <= 12; period++) {
                    this.getLearningGrantEntries().add(new LearningGrantEntry(year, period, DELIVERY));
                    if (grantType == AEB_PROCURED) {
                        this.getLearningGrantEntries().add(new LearningGrantEntry(year, period, SUPPORT));
                    }
                }

                LearningGrantEntry lge = new LearningGrantEntry(year, 13, DELIVERY);
                lge.setActualMonth(null);
                this.getLearningGrantEntries().add(lge);

                lge = new LearningGrantEntry(year, 14, DELIVERY);
                lge.setActualMonth(null);
                this.getLearningGrantEntries().add(lge);
            }
        }
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        LearningGrantBlock clonedBlock = (LearningGrantBlock) target;

        clonedBlock.setStartYear(this.startYear);
        clonedBlock.setNumberOfYears(this.numberOfYears);
        clonedBlock.setTotalAllocation(this.totalAllocation);
        clonedBlock.setGrantType(this.grantType);

        for (LearningGrantAllocation allocation : allocations) {
            clonedBlock.getAllocations().add(allocation.clone());
        }

        for (LearningGrantEntry entry : learningGrantEntries) {
            clonedBlock.getLearningGrantEntries().add(entry.clone());
        }

        for (Claim claim : this.getClaims()) {
            Claim clonedClaim = claim.clone(clonedBlock.getId());
            clonedBlock.getClaims().add(clonedClaim);
        }
    }

    public LearningGrantEntry getLearningGrantEntry(Integer academicYear, Integer period, LearningGrantEntryType type) {
        return learningGrantEntries.stream()
                .filter(e -> e.getAcademicYear().equals(academicYear) && e.getPeriod().equals(period) && e.getType().equals(type))
                .findFirst().orElse(null);
    }

    @Override
    public Map<GrantType, BigDecimal> getFundingRequested() {
        Map<GrantType, BigDecimal> grantsRequested = new HashMap<>();

        BigDecimal allocation = BigDecimal.ZERO;
        List<LearningGrantAllocation> allocations = this.getAllocations();
        for (LearningGrantAllocation learningGrantAllocation : allocations) {
            allocation = nullSafeAdd(allocation, learningGrantAllocation.getAllocation(),
                    learningGrantAllocation.getLearnerSupportAllocation());
        }

        grantsRequested.put(GrantType.Grant, allocation);
        grantsRequested.put(GrantType.DPF, BigDecimal.ZERO);
        grantsRequested.put(GrantType.RCGF, BigDecimal.ZERO);
        return grantsRequested;
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        LearningGrantBlock otherBlock = (LearningGrantBlock) other;

        LearningGrantAllocation thisAllocation = this.getAllocation(this.startYear);
        LearningGrantAllocation otherAllocation = otherBlock.getAllocation(this.startYear);

        if (!Objects.equals(thisAllocation.getAllocation(), otherAllocation.getAllocation())) {
            differences.add(new ProjectDifference(this, "allocation"));
        }
        if (!Objects.equals(thisAllocation.getCommunityAllocation(), otherAllocation.getCommunityAllocation())) {
            differences.add(new ProjectDifference(this, "communityAllocation"));
        }

        if (!Objects.equals(thisAllocation.getLearnerSupportAllocation(), otherAllocation.getLearnerSupportAllocation())) {
            differences.add(new ProjectDifference(this, "learnerSupportAllocation"));
        }
    }

    public Set<LearningGrantEntry> getPaymentsDueForDate(OffsetDateTime month) {
        return learningGrantEntries.stream().filter(e -> e.getPaymentDate() != null)
                .filter(e -> e.getPaymentDate().getYear() == month.getYear()
                        && e.getPaymentDate().getMonthValue() == month.getMonthValue()
                        && e.getPaymentDate().getDayOfMonth() == month.getDayOfMonth())
                .collect(Collectors.toSet());

    }

    public boolean isClaimable() {
        boolean paymentsEnabled = project.getProgrammeTemplate().isPaymentsEnabled();
        return paymentsEnabled && project.getStatusType().equals(ProjectStatus.Active);
    }

    @Override
    public boolean getApprovalWillCreatePendingPayment() {
        return hasClaimsWithStatus(Claimed) || getApprovalWillCreatePendingAdjustmentOrReclaim(CLAIM_STATUS_PARTLY_PAID);
    }

    public boolean getApprovalWillCreatePendingReclaim() {
        return getApprovalWillCreatePendingAdjustmentOrReclaim(CLAIM_STATUS_OVER_PAID);
    }

    /**
     * The method will say if this approval will generate any supplementary or reclaim payment.
     * The status parameter of learning grant entry tells the type of payment as follows:
     *  CLAIM_STATUS_PARTLY_PAID - will generate adjustment/supplementary
     *  CLAIM_STATUS_OVER_PAID - will generate reclaim
     *
     * */
    public boolean getApprovalWillCreatePendingAdjustmentOrReclaim(String status) {
        for (LearningGrantEntry learningGrantEntry : learningGrantEntries) {
            if(learningGrantEntry.hasPaymentStatus(status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getPaymentsSourcesCreatedViaApproval() {
        if (!getApprovalWillCreatePendingPayment()) {
            return Collections.emptySet();
        }
        LearningGrantTemplateBlock learningGrantTemplateBlock = (LearningGrantTemplateBlock) this.getProject().getTemplate()
                .getSingleBlockByType(ProjectBlockType.LearningGrant);
        return learningGrantTemplateBlock.getPaymentSources();
    }

    private boolean hasClaimsWithStatus(ClaimStatus claimStatus) {
        return claims.stream().anyMatch(claim -> claimStatus.equals(claim.getClaimStatus()));
    }

    @Override
    protected void performPostApprovalActions(String username, OffsetDateTime approvalTime) {
        for (Claim claim : claims) {
            if (Claimed.equals(claim.getClaimStatus())) {
                claim.setClaimStatus(Approved);
            }
        }
        LearningGrantTemplateBlock lgtb = (LearningGrantTemplateBlock) this.getProject().getTemplate()
                .getSingleBlockByType(ProjectBlockType.LearningGrant);
        if (lgtb.getCanManuallyClaimP14()) {
            this.getLearningGrantEntries().stream()
                    .filter(p -> p.getPeriod() == 14)
                    .forEach(p -> p.setPaymentDue(null));
        }

    }

    @JsonProperty("isDeliveryAllocationEditable")
    public boolean isDeliveryAllocationEditable() {
        return !hasClaimsWithStatus(Claimed);
    }

    public LearningGrantEntry getLearningGrantEntryForClaim(Claim claim) {
        return getLearningGrantEntries().stream().filter(le -> le.getOriginalId().equals(claim.getEntityId())).findFirst()
                .orElse(null);
    }

    @Override
    public boolean isSelfContained() {
        return false;
    }

    @JsonIgnore
    @Override
    public boolean hasMonetaryValueChanged(NamedProjectBlock other) {
        if (other == null) {
            return true;
        }
        LearningGrantBlock otherLearningGrantBlock = (LearningGrantBlock) other;
        return !(Objects.equals(getTotalAllocations(), otherLearningGrantBlock.getTotalAllocations())
                && Objects.equals(getTotalLearningSupportAllocations(),
                otherLearningGrantBlock.getTotalLearningSupportAllocations()));
    }

    @JsonIgnore
    @Override
    public Long getValueToBeCheckedAgainstFinanceThresholdOnApproval() {
        return totalAllocation != null ? totalAllocation.longValue() : null;
    }

    @Override
    public boolean shouldRecordLastMonetaryApprover() {
        return true;
    }
}
