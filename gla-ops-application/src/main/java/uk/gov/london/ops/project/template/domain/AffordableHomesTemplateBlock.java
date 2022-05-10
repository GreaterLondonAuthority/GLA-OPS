/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.project.grant.AffordableHomesOfWhichCategory;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("AFFORDABLE_HOMES")
public class AffordableHomesTemplateBlock extends TemplateBlock {

    @Transient
    private List<AffordableHomesOfWhichCategory> ofWhichCategories = new ArrayList<>();

    @Transient
    private boolean yearsDisabled;

    @Transient
    private int additionalCompletionYears = 0;

    @Transient
    private String startOnSiteHeaderText = "Start on Site";

    @Transient
    private String startOnSiteSubheaderText = "";

    @Transient
    private String completionHeaderText = "Completions";

    @Transient
    private String completionSubheaderText = "";

    @Transient
    private String grantRequestedQuestion;

    @Transient
    private Integer grantRequestedQuestionMaxLength;

    @Transient
    private boolean nilGrantHidden;

    @Transient
    private List<AffordableHomesCostsAndContributionsEntry> costCategories = new ArrayList<>();

    @Transient
    private List<AffordableHomesCostsAndContributionsEntry> contributionCategories = new ArrayList<>();

    @Transient
    private boolean completionOnlyAvailable = false;

    public List<AffordableHomesOfWhichCategory> getOfWhichCategories() {
        return ofWhichCategories;
    }

    public void setOfWhichCategories(List<AffordableHomesOfWhichCategory> ofWhichCategories) {
        this.ofWhichCategories = ofWhichCategories;
    }

    public boolean isYearsDisabled() {
        return yearsDisabled;
    }

    public void setYearsDisabled(boolean yearsDisabled) {
        this.yearsDisabled = yearsDisabled;
    }

    public int getAdditionalCompletionYears() {
        return additionalCompletionYears;
    }

    public void setAdditionalCompletionYears(int additionalCompletionYears) {
        this.additionalCompletionYears = additionalCompletionYears;
    }

    public String getStartOnSiteHeaderText() {
        return startOnSiteHeaderText;
    }

    public void setStartOnSiteHeaderText(String startOnSiteHeaderText) {
        this.startOnSiteHeaderText = startOnSiteHeaderText;
    }

    public String getStartOnSiteSubheaderText() {
        return startOnSiteSubheaderText;
    }

    public void setStartOnSiteSubheaderText(String startOnSiteSubheaderText) {
        this.startOnSiteSubheaderText = startOnSiteSubheaderText;
    }

    public String getCompletionHeaderText() {
        return completionHeaderText;
    }

    public void setCompletionHeaderText(String completionHeaderText) {
        this.completionHeaderText = completionHeaderText;
    }

    public String getCompletionSubheaderText() {
        return completionSubheaderText;
    }

    public void setCompletionSubheaderText(String completionSubheaderText) {
        this.completionSubheaderText = completionSubheaderText;
    }

    public String getGrantRequestedQuestion() {
        return grantRequestedQuestion;
    }

    public void setGrantRequestedQuestion(String grantRequestedQuestion) {
        this.grantRequestedQuestion = grantRequestedQuestion;
    }

    public Integer getGrantRequestedQuestionMaxLength() {
        return grantRequestedQuestionMaxLength;
    }

    public void setGrantRequestedQuestionMaxLength(Integer grantRequestedQuestionMaxLength) {
        this.grantRequestedQuestionMaxLength = grantRequestedQuestionMaxLength;
    }

    public boolean isNilGrantHidden() {
        return nilGrantHidden;
    }

    public void setNilGrantHidden(boolean nilGrantHidden) {
        this.nilGrantHidden = nilGrantHidden;
    }

    public List<AffordableHomesCostsAndContributionsEntry> getCostCategories() {
        return costCategories;
    }

    public void setCostCategories(List<AffordableHomesCostsAndContributionsEntry> costCategories) {
        this.costCategories = costCategories;
    }

    public List<AffordableHomesCostsAndContributionsEntry> getContributionCategories() {
        return contributionCategories;
    }

    public void setContributionCategories(List<AffordableHomesCostsAndContributionsEntry> contributionCategories) {
        this.contributionCategories = contributionCategories;
    }

    public boolean isCompletionOnlyAvailable() {
        return completionOnlyAvailable;
    }

    public void setCompletionOnlyAvailable(boolean completionOnlyAvailable) {
        this.completionOnlyAvailable = completionOnlyAvailable;
    }

    @PostLoad
    void loadBlockData() {
        AffordableHomesTemplateBlock data = JSONUtils.fromJSON(this.blockData,
                AffordableHomesTemplateBlock.class);
        if (data != null) {
            this.setOfWhichCategories(data.getOfWhichCategories());
            this.setYearsDisabled(data.isYearsDisabled());
            this.setAdditionalCompletionYears(data.getAdditionalCompletionYears());
            this.setStartOnSiteHeaderText(data.getStartOnSiteHeaderText());
            this.setStartOnSiteSubheaderText(data.getStartOnSiteSubheaderText());
            this.setCompletionHeaderText(data.getCompletionHeaderText());
            this.setCompletionSubheaderText(data.getCompletionSubheaderText());
            this.setGrantRequestedQuestion(data.getGrantRequestedQuestion());
            this.setGrantRequestedQuestionMaxLength(data.getGrantRequestedQuestionMaxLength());
            this.setNilGrantHidden(data.isNilGrantHidden());
            this.setContributionCategories(data.getContributionCategories());
            this.setCostCategories(data.getCostCategories());
            this.setCompletionOnlyAvailable(data.isCompletionOnlyAvailable());
        }
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        AffordableHomesTemplateBlock cloned = (AffordableHomesTemplateBlock) clone;
        cloned.setOfWhichCategories(getOfWhichCategories());
        cloned.setYearsDisabled(isYearsDisabled());
        cloned.setAdditionalCompletionYears(getAdditionalCompletionYears());
        cloned.setStartOnSiteHeaderText(getStartOnSiteHeaderText());
        cloned.setStartOnSiteSubheaderText(getStartOnSiteSubheaderText());
        cloned.setCompletionHeaderText(getCompletionHeaderText());
        cloned.setCompletionSubheaderText(getCompletionSubheaderText());
        cloned.setGrantRequestedQuestion(getGrantRequestedQuestion());
        cloned.setGrantRequestedQuestionMaxLength(getGrantRequestedQuestionMaxLength());
        cloned.setNilGrantHidden(isNilGrantHidden());
        cloned.setCostCategories(new ArrayList<>(this.getCostCategories()));
        cloned.setContributionCategories(new ArrayList<>(this.getContributionCategories()));
        cloned.setCompletionOnlyAvailable(isCompletionOnlyAvailable());
    }

}
