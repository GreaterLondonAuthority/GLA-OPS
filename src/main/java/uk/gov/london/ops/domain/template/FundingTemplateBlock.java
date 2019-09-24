/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.domain.project.ProjectBlockType;

import javax.persistence.*;

@Entity
@DiscriminatorValue("FUNDING")
public class FundingTemplateBlock extends TemplateBlock {

    public enum FundingSpendType {REVENUE_ONLY, CAPITAL_ONLY, REVENUE_AND_CAPITAL}

    @Column(name = "start_year")
    private Integer startYear = 2018;

    @Column(name = "year_available_to")
    private int yearAvailableTo = 5;

    @Column(name = "max_evidence_attachments")
    private Integer maxEvidenceAttachments;

    @Column(name = "default_activity_name")
    private String defaultActivityName = "Projected cost";

    @Column(name = "activities_required")
    private boolean activitiesRequired;

    @Column(name = "enforce_funding_balance")
    private boolean fundingBalanceEnforced;

    @Column(name = "funding_spend_type")
    @Enumerated(EnumType.STRING)
    private FundingSpendType fundingSpendType = FundingSpendType.REVENUE_AND_CAPITAL;

    @Column(name = "capital_gla_funding")
    private Boolean showCapitalGLAFunding;

    @Column(name = "revenue_gla_funding")
    private Boolean showRevenueGLAFunding;

    @Column(name = "capital_other_funding")
    private Boolean showCapitalOtherFunding;

    @Column(name = "revenue_other_funding")
    private Boolean showRevenueOtherFunding;

    @Column(name = "multiple_bespoke_activities_enabled")
    private boolean multipleBespokeActivitiesEnabled = false;

    @Column(name = "budget_evidence_attachment_enabled")
    private boolean budgetEvidenceAttachmentEnabled = false;

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

    @Column(name = "category_external_id")
    private Integer categoriesExternalId;

    @Column(name = "show_milestones")
    private Boolean showMilestones = Boolean.TRUE;

    @Column(name = "show_categories")
    private Boolean showCategories = Boolean.FALSE;

    @Column(name = "monetary_value_scale")
    private Integer monetaryValueScale = 0;

    public FundingTemplateBlock() {
        super(ProjectBlockType.Funding);
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

    public String getDefaultActivityName() {
        return defaultActivityName;
    }

    public void setDefaultActivityName(String defaultActivityName) {
        this.defaultActivityName = defaultActivityName;
    }

    public boolean isActivitiesRequired() {
        return activitiesRequired;
    }

    public void setActivitiesRequired(boolean activitiesRequired) {
        this.activitiesRequired = activitiesRequired;
    }

    public FundingSpendType getFundingSpendType() {
        return fundingSpendType;
    }

    public void setFundingSpendType(FundingSpendType fundingSpendType) {
        this.fundingSpendType = fundingSpendType;
    }

    public boolean isFundingBalanceEnforced() {
        return fundingBalanceEnforced;
    }

    public void setFundingBalanceEnforced(boolean fundingBalanceEnforced) {
        this.fundingBalanceEnforced = fundingBalanceEnforced;
    }

    public boolean isMultipleBespokeActivitiesEnabled() {
        return multipleBespokeActivitiesEnabled;
    }

    public void setMultipleBespokeActivitiesEnabled(boolean multipleBespokeActivitiesEnabled) {
        this.multipleBespokeActivitiesEnabled = multipleBespokeActivitiesEnabled;
    }

    public boolean isBudgetEvidenceAttachmentEnabled() {
        return budgetEvidenceAttachmentEnabled;
    }

    public void setBudgetEvidenceAttachmentEnabled(boolean budgetEvidenceAttachmentEnabled) {
        this.budgetEvidenceAttachmentEnabled = budgetEvidenceAttachmentEnabled;
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

    public Boolean getShowCapitalGLAFunding() {
        return showCapitalGLAFunding ;
    }

    public void setShowCapitalGLAFunding(Boolean showCapitalGLAFunding) {
        this.showCapitalGLAFunding = showCapitalGLAFunding;
    }

    public Boolean getShowRevenueGLAFunding() {
        return showRevenueGLAFunding;
    }

    public void setShowRevenueGLAFunding(Boolean showRevenueGLAFunding) {
        this.showRevenueGLAFunding = showRevenueGLAFunding;
    }

    public Boolean getShowCapitalOtherFunding() {
        return showCapitalOtherFunding;
    }

    public void setShowCapitalOtherFunding(Boolean showCapitalOtherFunding) {
        this.showCapitalOtherFunding = showCapitalOtherFunding;
    }

    public Boolean getShowRevenueOtherFunding() {
        return showRevenueOtherFunding;
    }

    public void setShowRevenueOtherFunding(Boolean showRevenueOtherFunding) {
        this.showRevenueOtherFunding = showRevenueOtherFunding;
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

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        FundingTemplateBlock cloned = (FundingTemplateBlock) clone;
        cloned.setStartYear(this.getStartYear());
        cloned.setFundingBalanceEnforced(this.isFundingBalanceEnforced());
        cloned.setYearAvailableTo(this.getYearAvailableTo());
        cloned.setMaxEvidenceAttachments(this.getMaxEvidenceAttachments());
        cloned.setDefaultActivityName(this.getDefaultActivityName());
        cloned.setActivitiesRequired(this.isActivitiesRequired());
        cloned.setFundingSpendType(this.getFundingSpendType());
        cloned.setMultipleBespokeActivitiesEnabled(this.isMultipleBespokeActivitiesEnabled());
        cloned.setBudgetEvidenceAttachmentEnabled(this.isBudgetEvidenceAttachmentEnabled());

        cloned.setAnnualBudgetHelpText(this.getAnnualBudgetHelpText());
        cloned.setCapClaimedFunding(this.getCapClaimedFunding());
        cloned.setCapOtherFunding(this.getCapOtherFunding());
        cloned.setRevClaimedFunding(this.getRevClaimedFunding());
        cloned.setRevOtherFunding(this.getRevOtherFunding());
        cloned.setTotalCapAvailableFunding(this.getTotalCapAvailableFunding());
        cloned.setTotalCapOtherFunding(this.getTotalCapOtherFunding());
        cloned.setTotalRevAvailableFunding(this.getTotalRevAvailableFunding());
        cloned.setTotalRevOtherFunding(this.getTotalRevOtherFunding());
        cloned.setWizardClaimLabel(this.getWizardClaimLabel());
        cloned.setWizardOtherLabel(this.getWizardOtherLabel());

        cloned.setShowMilestones(this.getShowMilestones());
        cloned.setShowCategories(this.getShowCategories());
        cloned.setCategoriesExternalId(this.getCategoriesExternalId());
        cloned.setMonetaryValueScale(this.getMonetaryValueScale());

        cloned.setShowCapitalOtherFunding(this.getShowCapitalOtherFunding());
        cloned.setShowCapitalGLAFunding(this.getShowCapitalGLAFunding());
        cloned.setShowRevenueGLAFunding(this.getShowRevenueGLAFunding());
        cloned.setShowRevenueOtherFunding(this.getShowRevenueOtherFunding());
    }

}
