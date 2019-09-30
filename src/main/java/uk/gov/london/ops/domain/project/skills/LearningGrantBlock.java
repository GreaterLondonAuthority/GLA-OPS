/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.skills;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.template.LearningGrantTemplateBlock;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.payment.PaymentSource;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;
import static uk.gov.london.common.skills.SkillsGrantType.AEB_GRANT;
import static uk.gov.london.common.skills.SkillsGrantType.AEB_PROCURED;
import static uk.gov.london.ops.domain.project.ClaimStatus.Approved;
import static uk.gov.london.ops.domain.project.ClaimStatus.Claimed;
import static uk.gov.london.ops.domain.project.skills.LearningGrantEntryType.DELIVERY;
import static uk.gov.london.ops.domain.project.skills.LearningGrantEntryType.SUPPORT;

@Entity(name = "learning_grant_block")
@DiscriminatorValue("LearningGrant")
@JoinData(sourceTable = "learning_grant_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
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
    @Column(name="grant_type")
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
        return learningGrantEntries.stream().filter(lge -> lge.getType() == type && lge.getAcademicYear().equals(academicYear)).collect(Collectors.toList());
    }

    public void setLearningGrantEntries(List<LearningGrantEntry> learningGrantEntries) {
        this.learningGrantEntries = learningGrantEntries;
    }

    public Set<Claim> getClaims() {
        return claims;
    }

    public void setClaims(Set<Claim> claims) {
        this.claims = claims;
    }

    @Override
    public boolean isComplete() {

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
                    if(learningGrantEntries.stream().filter(entry -> entry.getPercentage() == null).count() > 0) {
                        return false;
                    }
                }
            }

            if (grantType.equals(AEB_PROCURED) && allocation.getLearnerSupportAllocation() == null) {
              return false;
            } else {
              if (grantType.equals(AEB_PROCURED) && learningGrantEntries != null && !learningGrantEntries.isEmpty()) {
                if(learningGrantEntries.stream().filter(entry -> entry.getPercentage() == null).count() > 0) {
                    return false;
                }
              }
            }
        }

        return true;
    }

    @Override
    protected void generateValidationFailures() {
        if (totalYearlyAllocationExceedsTotalProjectAllocation()) {
            this.addErrorMessage("TOTAL_ALLOCATION_EXCEEDED", "", "The sum of the yearly allocations exceeds the total project allocation. Please amend.");
        }
    }

    boolean totalYearlyAllocationExceedsTotalProjectAllocation() {
        return (totalAllocation != null) && getTotalYearlyAllocation().compareTo(totalAllocation) > 0;
    }

    public BigDecimal getTotalYearlyAllocation() {
        BigDecimal totalYearlyAllocation = BigDecimal.ZERO;
        for (LearningGrantAllocation allocation: allocations) {
            totalYearlyAllocation = nullSafeAdd(totalYearlyAllocation, allocation.getAllocation(), allocation.getLearnerSupportAllocation());
        }
        return totalYearlyAllocation;
    }

    @Override
    public void merge(NamedProjectBlock block) {
        LearningGrantBlock updated = (LearningGrantBlock) block;

        for (LearningGrantAllocation updatedAllocation: updated.getAllocations()) {
            LearningGrantAllocation existingAllocation = this.getAllocation(updatedAllocation.getYear());
            existingAllocation.setAllocation(updatedAllocation.getAllocation());
            existingAllocation.setLearnerSupportAllocation(updatedAllocation.getLearnerSupportAllocation());
        }

        // if its a single year grant block, the total doesnt display, but we still want to copy the allocation for reporting purposes GLA-25807
        if (numberOfYears == 1) {
            this.setTotalAllocation(this.getAllocation(startYear).getAllocation());
        }
        else {
            this.setTotalAllocation(updated.getTotalAllocation());
        }

        for (LearningGrantEntry updatedEntry: updated.getLearningGrantEntries()) {
            LearningGrantEntry existingEntry = this.getLearningGrantEntry(updatedEntry.getAcademicYear(), updatedEntry.getPeriod(), updatedEntry.getType());
            existingEntry.setAllocation(updatedEntry.getAllocation());
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
                    this.getLearningGrantEntries().add(new LearningGrantEntry(year , period, DELIVERY));
                    if(grantType == AEB_PROCURED){
                        this.getLearningGrantEntries().add(new LearningGrantEntry(year, period, SUPPORT));
                    }
                }
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

        for (LearningGrantAllocation allocation: allocations) {
            clonedBlock.getAllocations().add(allocation.clone());
        }

        for (LearningGrantEntry entry: learningGrantEntries) {
            clonedBlock.getLearningGrantEntries().add(entry.clone());
        }

        for (Claim claim : this.getClaims()) {
            Claim clonedClaim = claim.clone(clonedBlock.getId());
            clonedBlock.getClaims().add(clonedClaim);
        }
    }

    public LearningGrantEntry getLearningGrantEntry(Integer academicYear, Integer period, LearningGrantEntryType type) {
        return learningGrantEntries.stream().filter(e ->e.getAcademicYear().equals(academicYear) && e.getPeriod().equals(period) && e.getType().equals(type)).findFirst().orElse(null);
    }

    @Override
    public Map<GrantType, Long> getFundingRequested() {
        Map<GrantType, Long> grantsRequested = new HashMap<>();

        BigDecimal allocation = BigDecimal.ZERO;
        List<LearningGrantAllocation> allocations = this.getAllocations();
        for (LearningGrantAllocation learningGrantAllocation : allocations) {
            allocation = nullSafeAdd(allocation, learningGrantAllocation.getAllocation(), learningGrantAllocation.getLearnerSupportAllocation());
        }

        grantsRequested.put(GrantType.Grant, allocation.longValue());
        grantsRequested.put(GrantType.DPF, 0L);
        grantsRequested.put(GrantType.RCGF, 0L);
        return grantsRequested;
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        LearningGrantBlock otherBlock = (LearningGrantBlock) other;

        LearningGrantAllocation thisAllocation = this.getAllocation(this.startYear);
        LearningGrantAllocation otherAllocation = otherBlock.getAllocation(this.startYear);

        if (!Objects.equals(thisAllocation.getAllocation(), otherAllocation.getAllocation())) {
            differences.add(new ProjectDifference(this,"allocation"));
        }

        if (!Objects.equals(thisAllocation.getLearnerSupportAllocation(), otherAllocation.getLearnerSupportAllocation())) {
            differences.add(new ProjectDifference(this,"learnerSupportAllocation"));
        }
    }

    public Set<LearningGrantEntry> getPaymentsDueForDate(OffsetDateTime month) {
        return learningGrantEntries.stream().filter(e -> e.getPaymentDate() != null)
                .filter(e -> e.getPaymentDate().getYear() == month.getYear() &&
                        e.getPaymentDate().getMonthValue() == month.getMonthValue() &&
                        e.getPaymentDate().getDayOfMonth() == month.getDayOfMonth())
                        .collect(Collectors.toSet());

    }

    public boolean isClaimable() {
        boolean paymentsEnabled = project.getProgrammeTemplate().isPaymentsEnabled();
        return paymentsEnabled && project.getStatusType().equals(ProjectStatus.Active);
    }

    @Override
    public boolean getApprovalWillCreatePendingPayment() {
        return hasClaimsWithStatus(Claimed) || getApprovalWillCreatePendingAdjustmentPayment()  ;
    }

    private boolean getApprovalWillCreatePendingAdjustmentPayment() {
        if (getProject().getGrantSourceAdjustmentAmount().compareTo(BigDecimal.ZERO) > 0) {
            for (LearningGrantEntry learningGrantEntry: learningGrantEntries) {
                if (learningGrantEntry.hasPayments()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean getApprovalWillCreatePendingReclaim() {
        if (getProject().getGrantSourceAdjustmentAmount().compareTo(BigDecimal.ZERO) < 0) {
            for (LearningGrantEntry learningGrantEntry: learningGrantEntries) {
                if (learningGrantEntry.hasPayments()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean getApprovalWillCreatePendingGrantPayment() {
        if (!getApprovalWillCreatePendingPayment()) {
            return false;
        }
        LearningGrantTemplateBlock learningGrantTemplateBlock = (LearningGrantTemplateBlock) this.getProject().getTemplate().getSingleBlockByType(ProjectBlockType.LearningGrant);
        Set<PaymentSource> paymentSources = learningGrantTemplateBlock.getPaymentSources();
        return paymentSources.contains(PaymentSource.Grant);
    }

    public List<Claim> getClaims(ClaimStatus claimStatus) {
        return claims.stream().filter(claim -> claimStatus.equals(claim.getClaimStatus())).collect(Collectors.toList());
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
    }

    @JsonProperty("isDeliveryAllocationEditable")
    public boolean isDeliveryAllocationEditable() {
        return !hasClaimsWithStatus(Claimed);
    }

    public LearningGrantEntry getLearningGrantEntryForClaim(Claim claim) {
        return getLearningGrantEntries().stream().filter(le -> le.getOriginalId().equals(claim.getEntityId())).findFirst().orElse(null);
    }

}
