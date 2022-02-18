/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import org.apache.commons.collections.CollectionUtils;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.payment.BudgetSummary;
import uk.gov.london.ops.payment.SpendType;
import uk.gov.london.ops.project.block.BaseFinanceBlock;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.project.block.ProjectDifferences;
import uk.gov.london.ops.project.claim.Claim;
import uk.gov.london.ops.project.claim.ClaimStatus;
import uk.gov.london.ops.project.claim.ClaimType;
import uk.gov.london.ops.project.template.domain.FundingTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;
import static uk.gov.london.ops.project.claim.ClaimStatus.Claimed;

@Entity(name = "funding_block")
@DiscriminatorValue("Funding")
@JoinData(sourceTable = "funding_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the funding block is a subclass of the project block and shares a common key")
public class FundingBlock extends BaseFinanceBlock {

    @Column(name = "start_year")
    private Integer startYear = 2018;

    @Column(name = "year_available_to")
    private int yearAvailableTo = 5;

    @Column(name = "max_evidence_attachments")
    private Integer maxEvidenceAttachments;

    @OneToMany(fetch = FetchType.LAZY, cascade = {}, targetEntity = BudgetSummary.class)
    @JoinColumn(name = "block_id")
    private List<BudgetSummary> budgetSummaries = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinData(joinType = Join.JoinType.OneToMany, comment = "join to funding_claim")
    @JoinColumn(name = "block_id")
    private Set<Claim> claims = new HashSet<>();

    @Column(name = "activities_required")
    private boolean activitiesRequired;

    @Column(name = "funding_spend_type")
    @Enumerated(EnumType.STRING)
    private FundingTemplateBlock.FundingSpendType fundingSpendType;

    @Column(name = "capital_gla_funding")
    private Boolean showCapitalGLAFunding;

    @Column(name = "revenue_gla_funding")
    private Boolean showRevenueGLAFunding;

    @Column(name = "capital_other_funding")
    private Boolean showCapitalOtherFunding;

    @Column(name = "revenue_other_funding")
    private Boolean showRevenueOtherFunding;

    @Column(name = "category_external_id")
    private Integer categoriesExternalId;

    @Column(name = "show_milestones")
    private Boolean showMilestones = Boolean.TRUE;

    @Column(name = "show_categories")
    private Boolean showCategories = Boolean.FALSE;

    @Column(name = "monetary_value_scale")
    private Integer monetaryValueScale = 0;

    @Transient
    private ProjectFunding allProjectFunding;

    @Column(name = "wizard_claim_label")
    private String wizardClaimLabel;

    @Column(name = "wizard_other_label")
    private String wizardOtherLabel;

    @Column(name = "cap_claimed_funding")
    private String capClaimedFunding;

    @Column(name = "cap_other_funding")
    private String capOtherFunding;

    @Column(name = "rev_claimed_funding")
    private String revClaimedFunding;

    @Column(name = "rev_other_funding")
    private String revOtherFunding;

    @Column(name = "can_claim_activity")
    private boolean canClaimActivity = false;

    @Column(name = "evidence_attachments_mandatory")
    private boolean evidenceAttachmentsMandatory;

    public FundingBlock() {
        setBlockType(ProjectBlockType.Funding);
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public int getYearAvailableTo() {
        return yearAvailableTo;
    }

    public void setYearAvailableTo(int yearAvailableTo) {
        this.yearAvailableTo = yearAvailableTo;
    }

    public Integer getMaxEvidenceAttachments() {
        return maxEvidenceAttachments;
    }

    public void setMaxEvidenceAttachments(Integer maxEvidenceAttachments) {
        this.maxEvidenceAttachments = maxEvidenceAttachments;
    }

    public List<BudgetSummary> getBudgetSummaries() {
        return budgetSummaries;
    }

    public void setBudgetSummaries(List<BudgetSummary> budgetSummaries) {
        this.budgetSummaries = budgetSummaries;
    }

    public boolean isActivitiesRequired() {
        return activitiesRequired;
    }

    public void setActivitiesRequired(boolean activitiesRequired) {
        this.activitiesRequired = activitiesRequired;
    }

    public Set<Claim> getClaims() {
        return claims;
    }

    public Set<Claim> getClaims(Integer year) {
        return getClaims().stream().filter(c -> c.getYear().equals(year)).collect(Collectors.toSet());
    }

    public void setClaims(Set<Claim> claims) {
        this.claims = claims;
    }

    public FundingTemplateBlock.FundingSpendType getFundingSpendType() {
        return fundingSpendType;
    }

    public void setFundingSpendType(FundingTemplateBlock.FundingSpendType fundingSpendType) {
        this.fundingSpendType = fundingSpendType;
    }

    public String getWizardClaimLabel() {
        return wizardClaimLabel;
    }

    public void setWizardClaimLabel(String wizardClaimLabel) {
        this.wizardClaimLabel = wizardClaimLabel;
    }

    public String getWizardOtherLabel() {
        return wizardOtherLabel;
    }

    public void setWizardOtherLabel(String wizardOtherLabel) {
        this.wizardOtherLabel = wizardOtherLabel;
    }

    public String getCapClaimedFunding() {
        return capClaimedFunding;
    }

    public void setCapClaimedFunding(String capClaimedFunding) {
        this.capClaimedFunding = capClaimedFunding;
    }

    public String getCapOtherFunding() {
        return capOtherFunding;
    }

    public void setCapOtherFunding(String capOtherFunding) {
        this.capOtherFunding = capOtherFunding;
    }

    public String getRevClaimedFunding() {
        return revClaimedFunding;
    }

    public void setRevClaimedFunding(String revClaimedFunding) {
        this.revClaimedFunding = revClaimedFunding;
    }

    public String getRevOtherFunding() {
        return revOtherFunding;
    }

    public void setRevOtherFunding(String revOtherFunding) {
        this.revOtherFunding = revOtherFunding;
    }

    public Integer getCategoriesExternalId() {
        return categoriesExternalId;
    }

    public void setCategoriesExternalId(Integer categoriesExternalId) {
        this.categoriesExternalId = categoriesExternalId;
    }

    public Boolean getShowMilestones() {
        return showMilestones;
    }

    public void setShowMilestones(Boolean showMilestones) {
        this.showMilestones = showMilestones;
    }

    public Boolean getShowCategories() {
        return showCategories;
    }

    public void setShowCategories(Boolean showCategories) {
        this.showCategories = showCategories;
    }

    public Integer getMonetaryValueScale() {
        return monetaryValueScale;
    }

    public void setMonetaryValueScale(Integer monetaryValueScale) {
        this.monetaryValueScale = monetaryValueScale;
    }

    public Boolean getShowCapitalGLAFunding() {
        return showCapitalGLAFunding == null
                ? getFundingSpendType() != FundingTemplateBlock.FundingSpendType.REVENUE_ONLY
                : showCapitalGLAFunding;
    }

    public void setShowCapitalGLAFunding(Boolean showCapitalGLAFunding) {
        this.showCapitalGLAFunding = showCapitalGLAFunding;
    }

    public Boolean getShowRevenueGLAFunding() {
        return showRevenueGLAFunding == null
                ? getFundingSpendType() != FundingTemplateBlock.FundingSpendType.CAPITAL_ONLY
                : showRevenueGLAFunding;
    }

    public void setShowRevenueGLAFunding(Boolean showRevenueGLAFunding) {
        this.showRevenueGLAFunding = showRevenueGLAFunding;
    }

    public Boolean getShowCapitalOtherFunding() {
        return showCapitalOtherFunding == null
                ? getFundingSpendType() != FundingTemplateBlock.FundingSpendType.REVENUE_ONLY
                : showCapitalOtherFunding;
    }

    public void setShowCapitalOtherFunding(Boolean showCapitalOtherFunding) {
        this.showCapitalOtherFunding = showCapitalOtherFunding;
    }

    public ProjectFunding getAllProjectFunding() {
        return allProjectFunding;
    }

    public void setAllProjectFunding(ProjectFunding allProjectFunding) {
        this.allProjectFunding = allProjectFunding;
    }


    public Boolean getShowRevenueOtherFunding() {
        return showRevenueOtherFunding == null
                ? getFundingSpendType() != FundingTemplateBlock.FundingSpendType.CAPITAL_ONLY
                : showRevenueOtherFunding;
    }

    public void setShowRevenueOtherFunding(Boolean showRevenueOtherFunding) {
        this.showRevenueOtherFunding = showRevenueOtherFunding;
    }

    public boolean getCanClaimActivity() {
        return canClaimActivity;
    }

    public void setCanClaimActivity(Boolean canClaimActivity) {
        this.canClaimActivity = canClaimActivity;
    }

    public boolean isEvidenceAttachmentsMandatory() {
        return evidenceAttachmentsMandatory;
    }

    public void setEvidenceAttachmentsMandatory(boolean evidenceAttachmentsMandatory) {
        this.evidenceAttachmentsMandatory = evidenceAttachmentsMandatory;
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        FundingBlock fb = (FundingBlock) target;
        fb.setStartYear(this.getStartYear());
        fb.setYearAvailableTo(this.getYearAvailableTo());
        fb.setMaxEvidenceAttachments(this.getMaxEvidenceAttachments());
        fb.setActivitiesRequired(this.isActivitiesRequired());
        fb.setFundingSpendType(this.getFundingSpendType());

        fb.setShowRevenueOtherFunding(this.getShowRevenueOtherFunding());
        fb.setShowRevenueGLAFunding(this.getShowRevenueGLAFunding());

        fb.setShowCapitalOtherFunding(this.getShowCapitalOtherFunding());
        fb.setShowCapitalGLAFunding(this.getShowCapitalGLAFunding());

        fb.setCapClaimedFunding(this.capClaimedFunding);
        fb.setCapOtherFunding(this.capOtherFunding);
        fb.setRevClaimedFunding(this.revClaimedFunding);
        fb.setRevOtherFunding(this.revOtherFunding);
        fb.setWizardClaimLabel(this.wizardClaimLabel);
        fb.setWizardOtherLabel(this.wizardOtherLabel);
        fb.setShowMilestones(this.showMilestones);
        fb.setShowCategories(this.showCategories);
        fb.setCategoriesExternalId(this.categoriesExternalId);
        fb.setMonetaryValueScale(this.monetaryValueScale);
        fb.setCanClaimActivity(this.canClaimActivity);
        fb.setEvidenceAttachmentsMandatory(this.evidenceAttachmentsMandatory);

        for (Claim claim : this.getClaims()) {
            Claim fc = claim.clone(fb.getId());
            fb.getClaims().add(fc);
        }
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        if (templateBlock instanceof FundingTemplateBlock) {
            FundingTemplateBlock fundingTemplateBlock = (FundingTemplateBlock) templateBlock;
            Integer startYear = project.getProgramme().getStartYear();
            Integer endYear = project.getProgramme().getEndYear();

            if (startYear != null && endYear != null) {
                this.setStartYear(startYear);
                this.setYearAvailableTo(endYear - startYear);
            } else {
                this.setStartYear(fundingTemplateBlock.getStartYear());
                this.setYearAvailableTo(fundingTemplateBlock.getYearAvailableTo());
            }
            this.setMaxEvidenceAttachments(fundingTemplateBlock.getMaxEvidenceAttachments());
            this.setActivitiesRequired(fundingTemplateBlock.isActivitiesRequired());
            this.setFundingSpendType(fundingTemplateBlock.getFundingSpendType());

            this.setShowRevenueGLAFunding(fundingTemplateBlock.getShowRevenueGLAFunding());
            this.setShowRevenueOtherFunding(fundingTemplateBlock.getShowRevenueOtherFunding());

            this.setShowCapitalGLAFunding(fundingTemplateBlock.getShowCapitalGLAFunding());
            this.setShowCapitalOtherFunding(fundingTemplateBlock.getShowCapitalOtherFunding());

            // Initialise the configurable block labels from the funding template
            this.setCapClaimedFunding(fundingTemplateBlock.getCapClaimedFunding());
            this.setCapOtherFunding(fundingTemplateBlock.getCapOtherFunding());
            this.setRevClaimedFunding(fundingTemplateBlock.getRevClaimedFunding());
            this.setRevOtherFunding(fundingTemplateBlock.getRevOtherFunding());
            this.setWizardClaimLabel(fundingTemplateBlock.getWizardClaimLabel());
            this.setWizardOtherLabel(fundingTemplateBlock.getWizardOtherLabel());

            this.setShowMilestones(fundingTemplateBlock.getShowMilestones());
            this.setShowCategories(fundingTemplateBlock.getShowCategories());
            this.setCategoriesExternalId(fundingTemplateBlock.getCategoriesExternalId());
            this.setMonetaryValueScale(fundingTemplateBlock.getMonetaryValueScale());
            this.setCanClaimActivity(fundingTemplateBlock.getCanClaimActivity());
            this.setEvidenceAttachmentsMandatory(fundingTemplateBlock.isEvidenceAttachmentsMandatory());
        }
    }

    @Override
    public boolean isComplete() {
        return isVisited() && CollectionUtils.isNotEmpty(budgetSummaries) && getValidationFailures().size() == 0;
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        FundingBlock otherFundingBlock = (FundingBlock) other;

        List<BudgetSummary> thisBudgetSummaries = this.getBudgetSummaries();
        List<BudgetSummary> otherBudgetSummaries = otherFundingBlock.getBudgetSummaries();

        // additions
        for (BudgetSummary thisBudgetSummary : thisBudgetSummaries) {
            if (otherFundingBlock.getBudgetSummary(thisBudgetSummary.getYear(), thisBudgetSummary.getSpendType(),
                    thisBudgetSummary.getCategory()) == null) {
                differences.add(new ProjectDifference(thisBudgetSummary, ProjectDifference.DifferenceType.Addition));
            }
        }

        // deletions
        for (BudgetSummary otherBudgetSummary : otherBudgetSummaries) {
            if (this.getBudgetSummary(otherBudgetSummary.getYear(), otherBudgetSummary.getSpendType(),
                    otherBudgetSummary.getCategory()) == null) {
                differences.add(new ProjectDifference(otherBudgetSummary, ProjectDifference.DifferenceType.Deletion));
            }
        }

        // changes
        for (BudgetSummary thisBudgetSummary : thisBudgetSummaries) {
            BudgetSummary otherBudgetSummary = otherFundingBlock
                    .getBudgetSummary(thisBudgetSummary.getYear(), thisBudgetSummary.getSpendType(),
                            thisBudgetSummary.getCategory());
            if (otherBudgetSummary != null && !Objects.equals(thisBudgetSummary.getValue(), otherBudgetSummary.getValue())) {
                differences.add(new ProjectDifference(thisBudgetSummary, ProjectDifference.DifferenceType.Change));
            }
        }
    }

    private BudgetSummary getBudgetSummary(Integer year, SpendType spendType, String category) {
        return budgetSummaries.stream()
                .filter(bs -> Objects.equals(year, bs.getYear()) && Objects.equals(spendType, bs.getSpendType())
                        && Objects.equals(category, bs.getCategory())).findFirst().orElse(null);
    }

    public FundingTotals getTotals() {
        return new FundingTotals(
                getBudgetsTotal(SpendType.CAPITAL, null),
                getBudgetsTotal(SpendType.CAPITAL, MATCH_FUND_CATEGORY),
                getBudgetsTotal(SpendType.REVENUE, null),
                getBudgetsTotal(SpendType.REVENUE, MATCH_FUND_CATEGORY)
        );
    }

    private BigDecimal getBudgetsTotal(SpendType spendType, String category) {
        return budgetSummaries.stream()
                .filter(bs -> Objects.equals(spendType, bs.getSpendType()) && Objects.equals(category, bs.getCategory()))
                .map(BudgetSummary::getValue).reduce(BigDecimal::add).orElse(null);
    }

    @Override
    public boolean isBlockRevertable() {
        return false;
    }


    public FundingTotalBudget getFundingTotalBudget() {

        return new FundingTotalBudget(budgetSummaries);
    }

    public boolean isPaymentsEnabled() {
        if (getProject() != null && getProject().getProgrammeTemplate() != null) {
            return getProject().getProgrammeTemplate().isPaymentsEnabled();
        }
        return true;
    }

    @Override
    public boolean getApprovalWillCreatePendingPayment() {
        for (Claim claim : getClaimed()) {
            if (Claimed.equals(claim.getClaimStatus())) {
                Claim previouslyApprovedClaim = getMostRecentlyApprovedAssociatedClaim(claim);
                if (previouslyApprovedClaim == null) {
                    //at least one claim is new so payment will be generated
                    return true;
                }
                if (claim.getAmount() != null && claim.getAmount().compareTo(previouslyApprovedClaim.getAmount()) > 0) {
                    //at least one claim is larger than it was previously so payment will be generated
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean getApprovalWillCreatePendingReclaim() {
        List<Claim> claims = getClaimed();
        for (Claim claim : claims) {
            if (Claimed.equals(claim.getClaimStatus())) {
                Claim previouslyApprovedClaim = getMostRecentlyApprovedAssociatedClaim(claim);
                if (previouslyApprovedClaim != null
                        && claim.getAmount() != null
                        && claim.getAmount().compareTo(previouslyApprovedClaim.getAmount()) < 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Claim getMostRecentlyApprovedAssociatedClaim(Claim current) {
        if (ClaimType.ACTIVITY.equals(current.getClaimType())) {
            return claims.stream()
                    .filter(c -> c.getEntityId() != null
                            && current.getEntityId().equals(c.getEntityId())
                            && (ClaimStatus.Approved.equals(c.getClaimStatus()) || ClaimStatus.Withdrawn.equals(c.getClaimStatus()))
                            && c.getClaimedOn() != null)
                    .sorted(Comparator.comparing(Claim::getClaimedOn).reversed())
                    .findFirst().orElse(null);
        } else {
            return claims.stream()
                    .filter(c -> current.getYear().equals(c.getYear())
                            && current.getClaimTypePeriod().equals(c.getClaimTypePeriod())
                            && (ClaimStatus.Approved.equals(c.getClaimStatus()) || ClaimStatus.Withdrawn.equals(c.getClaimStatus()))
                            && c.getClaimedOn() != null)
                    .sorted(Comparator.comparing(Claim::getClaimedOn).reversed())
                    .findFirst().orElse(null);
        }
    }

    public List<Claim> getAllPreviouslyApprovedClaims(Claim current) {
        if (ClaimType.ACTIVITY.equals(current.getClaimType())) {
            return claims.stream()
                    .filter(c -> c.getEntityId() != null
                            && current.getEntityId().equals(c.getEntityId())
                            && (ClaimStatus.Approved.equals(c.getClaimStatus()) || ClaimStatus.Withdrawn.equals(c.getClaimStatus()))
                    )
                    .collect(Collectors.toList());
        } else if (ClaimType.QUARTER.equals(current.getClaimType())) {
            return claims.stream()
                    .filter(c -> current.getYear().equals(c.getYear())
                            && current.getClaimTypePeriod().equals(c.getClaimTypePeriod())
                            && (ClaimStatus.Approved.equals(c.getClaimStatus()) || ClaimStatus.Withdrawn.equals(c.getClaimStatus()))
                    )
                    .collect(Collectors.toList());
        } else {
            return claims.stream()
                    .filter(c -> (ClaimStatus.Approved.equals(c.getClaimStatus()) || ClaimStatus.Withdrawn.equals(c.getClaimStatus())))
                    .collect(Collectors.toList());
        }
    }

    public List<Claim> getClaimed() {
        return claims.stream().filter(claim -> Claimed.equals(claim.getClaimStatus())).collect(Collectors.toList());
    }

    public Claim getClaim(Integer year, Integer quarterValue, ClaimType claimType) {
        return getClaim(year, quarterValue, claimType, null);
    }

    public Claim getClaim(Integer year, Integer quarterValue, ClaimType claimType, Integer entityId) {
        List<Claim> allClaims = getClaims().stream()
                .filter(c -> c.getYear().equals(year)
                        && Objects.equals(c.getClaimTypePeriod(), quarterValue)
                        && Objects.equals(c.getClaimType(), claimType)
                        && (entityId == null || Objects.equals(c.getEntityId(), entityId)))
                .collect(Collectors.toList());
        // return the most recently approved claim. Some legacy claims will have null values for claimedOn
        // if this is the case and there's only one, then that's the target claim. Otherwise we can ignore null value
        // claims because if there are multiple claims then at least one will have been generated with this populated
        if (allClaims.size() == 1) {
            return allClaims.get(0);
        }
        return allClaims.stream()
                .filter(c -> c.getClaimedOn() != null)
                .sorted(Comparator.comparing(Claim::getClaimedOn).reversed())
                .findFirst().orElse(null);
    }

    @Override
    public Set<String> getPaymentsSourcesCreatedViaApproval() {
        FundingTemplateBlock fundingTemplateBlock = getFundingTemplateBlock();

        return fundingTemplateBlock.getPaymentSources();
    }

    @Override
    public void performPostApprovalActions(String username, OffsetDateTime approvalTime) {
        for (Claim claim : claims) {
            if (Claimed.equals(claim.getClaimStatus())) {
                claim.setClaimStatus(ClaimStatus.Approved);
                claim.setAuthorisedBy(username);
                claim.setAuthorisedOn(approvalTime);
                project.handleEvent(new OpsEvent(EventType.QuarterApproval,
                        String.format("%s Q%d authorised", GlaUtils.getFinancialYearFromYear(claim.getYear()),
                                claim.getClaimTypePeriod())));
            }
        }
    }

    public void reportSuccessfulPayments(String paymentReason, boolean isPaymentsOnlyApproval) {
        for (Claim claim : claims) {
            if (Claimed.equals(claim.getClaimStatus())) {
                String postfix = isPaymentsOnlyApproval ? " - Payments Only Approved" : " - Project and Payments Approved";
                project.handleEvent(new OpsEvent(EventType.QuarterApproval,
                        String.format("%s Q%d authorised %s", GlaUtils.getFinancialYearFromYear(claim.getYear()),
                                claim.getClaimTypePeriod(), postfix), null, paymentReason));
            }
        }
    }


    public boolean isBudgetEvidenceAttachmentEnabled() {
        return getFundingTemplateBlock().isBudgetEvidenceAttachmentEnabled();
    }

    FundingTemplateBlock getFundingTemplateBlock() {
        return (FundingTemplateBlock) getProject().getTemplate().getSingleBlockByType(ProjectBlockType.Funding);
    }

    @Override
    public boolean isSelfContained() {
        return false;
    }

    @Override
    protected boolean canShowYear(Integer year) {
        return year >= startYear && year <= startYear + yearAvailableTo;
    }

    public boolean isPaymentsOnlyApprovalPossible() {
        return this.getClaims().stream().anyMatch(c -> Claimed.equals(c.getClaimStatus()));
    }
}
