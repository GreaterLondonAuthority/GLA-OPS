/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
import javax.persistence.Transient;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.framework.calendar.OPSCalendar;
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

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    @JoinData(joinType = Join.JoinType.OneToOne, comment = "join to v_project_funding_summary")
    private Set<FundingYearSummary> fundingYearSummaries = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    @JoinData(joinType = Join.JoinType.OneToMany, comment = "join to v_funding_budget_summary")
    private Set<FundingBudgetSummary> fundingBudgetSummaries = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinData(joinType = Join.JoinType.OneToMany, comment = "join to funding_claim")
    @JoinColumn(name = "block_id")
    private Set<Claim> claims = new HashSet<>();

    @Column(name = "activities_required")
    private boolean activitiesRequired;

    @Column(name = "enforce_funding_balance")
    private boolean fundingBalanceEnforced;

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
    private FundingYearBreakdown yearBreakdown;

    @Transient
    private FundingSummary fundingSummary = new FundingSummary();

    @Transient
    private FundingByYearAndQuarter fundingByYearAndQuarter = new FundingByYearAndQuarter();

    @Column(name = "total_cap_available_funding")
    private String totalCapAvailableFunding;

    @Column(name = "total_cap_other_funding")
    private String totalCapOtherFunding;

    @Column(name = "total_rev_available_funding")
    private String totalRevAvailableFunding;

    @Column(name = "total_rev_other_funding")
    private String totalRevOtherFunding;

    @Column(name = "wizard_claim_label")
    private String wizardClaimLabel;

    @Column(name = "wizard_other_label")
    private String wizardOtherLabel;

    @Column(name = "annual_budget_help_text")
    private String annualBudgetHelpText;

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

    public FundingYearBreakdown getYearBreakdown() {
        return yearBreakdown;
    }

    public void setYearBreakdown(FundingYearBreakdown yearBreakdown) {
        this.yearBreakdown = yearBreakdown;
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

    public FundingSummary getFundingSummary() {
        return fundingSummary;
    }

    public void setFundingSummary(FundingSummary fundingSummary) {
        this.fundingSummary = fundingSummary;
    }

    public FundingByYearAndQuarter getFundingByYearAndQuarter() {
        return fundingByYearAndQuarter;
    }

    public void setFundingByYearAndQuarter(FundingByYearAndQuarter fundingByYearAndQuarter) {
        this.fundingByYearAndQuarter = fundingByYearAndQuarter;
    }

    public boolean isFundingBalanceEnforced() {
        return fundingBalanceEnforced;
    }

    public void setFundingBalanceEnforced(boolean fundingBalanceEnforced) {
        this.fundingBalanceEnforced = fundingBalanceEnforced;
    }

    public FundingTemplateBlock.FundingSpendType getFundingSpendType() {
        return fundingSpendType;
    }

    public void setFundingSpendType(FundingTemplateBlock.FundingSpendType fundingSpendType) {
        this.fundingSpendType = fundingSpendType;
    }

    public String getTotalCapAvailableFunding() {
        return totalCapAvailableFunding;
    }

    public void setTotalCapAvailableFunding(String totalCapAvailableFunding) {
        this.totalCapAvailableFunding = totalCapAvailableFunding;
    }

    public String getTotalCapOtherFunding() {
        return totalCapOtherFunding;
    }

    public void setTotalCapOtherFunding(String totalCapOtherFunding) {
        this.totalCapOtherFunding = totalCapOtherFunding;
    }

    public String getTotalRevAvailableFunding() {
        return totalRevAvailableFunding;
    }

    public void setTotalRevAvailableFunding(String totalRevAvailableFunding) {
        this.totalRevAvailableFunding = totalRevAvailableFunding;
    }

    public String getTotalRevOtherFunding() {
        return totalRevOtherFunding;
    }

    public void setTotalRevOtherFunding(String totalRevOtherFunding) {
        this.totalRevOtherFunding = totalRevOtherFunding;
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

    public String getAnnualBudgetHelpText() {
        return annualBudgetHelpText;
    }

    public void setAnnualBudgetHelpText(String annualBudgetHelpText) {
        this.annualBudgetHelpText = annualBudgetHelpText;
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

    public Set<FundingBudgetSummary> getFundingBudgetSummaries() {
        return fundingBudgetSummaries;
    }

    public void setFundingBudgetSummaries(Set<FundingBudgetSummary> fundingBudgetSummaries) {
        this.fundingBudgetSummaries = fundingBudgetSummaries;
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

        fb.setFundingBalanceEnforced(this.isFundingBalanceEnforced());

        fb.setShowRevenueOtherFunding(this.getShowRevenueOtherFunding());
        fb.setShowRevenueGLAFunding(this.getShowRevenueGLAFunding());

        fb.setShowCapitalOtherFunding(this.getShowCapitalOtherFunding());
        fb.setShowCapitalGLAFunding(this.getShowCapitalGLAFunding());

        fb.setAnnualBudgetHelpText(this.annualBudgetHelpText);
        fb.setCapClaimedFunding(this.capClaimedFunding);
        fb.setCapOtherFunding(this.capOtherFunding);
        fb.setRevClaimedFunding(this.revClaimedFunding);
        fb.setRevOtherFunding(this.revOtherFunding);
        fb.setTotalCapAvailableFunding(this.totalCapAvailableFunding);
        fb.setTotalCapOtherFunding(this.totalCapOtherFunding);
        fb.setTotalRevAvailableFunding(this.totalRevAvailableFunding);
        fb.setTotalRevOtherFunding(this.totalRevOtherFunding);
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
            this.setStartYear(fundingTemplateBlock.getStartYear());
            this.setYearAvailableTo(fundingTemplateBlock.getYearAvailableTo());
            this.setMaxEvidenceAttachments(fundingTemplateBlock.getMaxEvidenceAttachments());
            this.setActivitiesRequired(fundingTemplateBlock.isActivitiesRequired());
            this.setFundingBalanceEnforced(fundingTemplateBlock.isFundingBalanceEnforced());
            this.setFundingSpendType(fundingTemplateBlock.getFundingSpendType());

            this.setShowRevenueGLAFunding(fundingTemplateBlock.getShowRevenueGLAFunding());
            this.setShowRevenueOtherFunding(fundingTemplateBlock.getShowRevenueOtherFunding());

            this.setShowCapitalGLAFunding(fundingTemplateBlock.getShowCapitalGLAFunding());
            this.setShowCapitalOtherFunding(fundingTemplateBlock.getShowCapitalOtherFunding());

            // Initialise the configurable block labels from the funding template
            this.setAnnualBudgetHelpText(fundingTemplateBlock.getAnnualBudgetHelpText());
            this.setCapClaimedFunding(fundingTemplateBlock.getCapClaimedFunding());
            this.setCapOtherFunding(fundingTemplateBlock.getCapOtherFunding());
            this.setRevClaimedFunding(fundingTemplateBlock.getRevClaimedFunding());
            this.setRevOtherFunding(fundingTemplateBlock.getRevOtherFunding());
            this.setTotalCapAvailableFunding(fundingTemplateBlock.getTotalCapAvailableFunding());
            this.setTotalCapOtherFunding(fundingTemplateBlock.getTotalCapOtherFunding());
            this.setTotalRevAvailableFunding(fundingTemplateBlock.getTotalRevAvailableFunding());
            this.setTotalRevOtherFunding(fundingTemplateBlock.getTotalRevOtherFunding());
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
    protected void generateValidationFailures() {
        super.generateValidationFailures();

        if (activitiesRequired) {
            Set<String> errorMessages = new HashSet<>();

            for (BudgetSummary budgetSummary : budgetSummaries) {
                if (budgetSummary.getNbActivities() <= 0) {
                    errorMessages.add(String
                            .format("You must add %s spend to at least one milestone in year %s to complete this section",
                                    budgetSummary.getSpendType().name().toLowerCase(),
                                    OPSCalendar.yearStringShort(budgetSummary.getYear())));
                }
            }

            for (String message : errorMessages) {
                this.addErrorMessage("Block1", "", message);
            }
        }

        if (isFundingBalanceEnforced()) {
            ArrayList<String> years = new ArrayList<>();
            getFundingYearSummaries().stream().sorted(Comparator.comparing(a -> a.getId().getFinancialYear()))
                    .filter(f -> !(f.isYearValid())).forEach(f ->
                    years.add(f.getFinancialYearForDisplay()));

            if (years.size() > 0) {
                StringBuilder yearsAffected = new StringBuilder();
                for (int i = 0; i < years.size(); i++) {

                    if (i == years.size() - 1) {
                        yearsAffected.append(years.get(i));
                    } else if (i == years.size() - 2) {
                        yearsAffected.append(years.get(i)).append(" and ");
                    } else {
                        yearsAffected.append(years.get(i)).append(", ");
                    }
                }
                this.addErrorMessage("Block", "",
                        String.format("The budget values for %s are different to the quarterly spend values profiled. "
                                        + "Either amend the annual budget or adjust the quarterly spend values.", yearsAffected));
            }
        }
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


    public Set<FundingYearSummary> getFundingYearSummaries() {
        return fundingYearSummaries;
    }

    public void setFundingYearSummaries(Set<FundingYearSummary> fundingYearSummaries) {
        this.fundingYearSummaries = fundingYearSummaries;
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

    @JsonIgnore
    public List<Integer> getYearsMilestoneUsed(String milestoneName) {
        List<Integer> years = new ArrayList<>();
        FundingByYearAndQuarter fundingByYearAndQuarter = this.getFundingByYearAndQuarter();
        for (FundingByYearAndQuarter.FundingYQYear year : fundingByYearAndQuarter.getYears()) {
            if (year.getMilestone(milestoneName) != null) {
                years.add(year.getYear());
            }
        }
        return years;
    }

    @Override
    public boolean getApprovalWillCreatePendingPayment() {
        for (Claim claim : getClaimed()) {
            if (ClaimStatus.Claimed.equals(claim.getClaimStatus())) {
                return true;
            }
        }
        return false;
    }

    public List<Claim> getClaimed() {
        return claims.stream().filter(claim -> ClaimStatus.Claimed.equals(claim.getClaimStatus())).collect(Collectors.toList());
    }

    public Claim getClaim(Integer year, Integer quarterValue, ClaimType claimType) {
        return getClaim(year, quarterValue, claimType, null);
    }

    public Claim getClaim(Integer year, Integer quarterValue, ClaimType claimType, Integer entityId) {
        return getClaims().stream()
                .filter(c -> c.getYear().equals(year))
                .filter(c -> Objects.equals(c.getClaimTypePeriod(), quarterValue))
                .filter(c -> Objects.equals(c.getClaimType(), claimType))
                .filter(c -> entityId == null || Objects.equals(c.getEntityId(), entityId))
                .findFirst().orElse(null);
    }

    @Override
    public Set<String> getPaymentsSourcesCreatedViaApproval() {
        FundingTemplateBlock fundingTemplateBlock = getFundingTemplateBlock();

        return fundingTemplateBlock.getPaymentSources();
    }

    @Override
    protected void performPostApprovalActions(String username, OffsetDateTime approvalTime) {
        for (Claim claim : claims) {
            if (ClaimStatus.Claimed.equals(claim.getClaimStatus())) {
                claim.setClaimStatus(ClaimStatus.Approved);
                project.handleEvent(new OpsEvent(EventType.QuarterApproval,
                        String.format("%s Q%d authorised", GlaUtils.getFinancialYearFromYear(claim.getYear()),
                                claim.getClaimTypePeriod())));
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

}
