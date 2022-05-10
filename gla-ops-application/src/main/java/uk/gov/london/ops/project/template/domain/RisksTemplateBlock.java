/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.project.block.ProjectBlockType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

/**
 * Created by chris on 19/12/2016.
 */
@Entity
@DiscriminatorValue("RISKS")
public class RisksTemplateBlock extends TemplateBlock {

    @Transient
    private boolean showIssuesOnlyAfterProjectActive = false;

    @Transient
    private boolean showResidualRiskRatings = true;

    @Transient
    private String initialProbabilityRatingQuestionLabel = "Initial Probability Rating";

    @Transient
    private String initialProbabilityRatingQuestionHintText = "Select an Initial Probability";

    @Transient
    private String initialImpactRatingQuestionLabel = "Initial Impact Rating";

    @Transient
    private String initialImpactRatingQuestionHintText = "Select an Initial Impact";

    @Transient
    private String initialRiskRatingColumnHeading = "INITIAL RISK RATING";

    public RisksTemplateBlock() {
        super(ProjectBlockType.Risks);
    }

    public boolean isShowIssuesOnlyAfterProjectActive() {
        return showIssuesOnlyAfterProjectActive;
    }

    public void setShowIssuesOnlyAfterProjectActive(boolean showIssuesOnlyAfterProjectActive) {
        this.showIssuesOnlyAfterProjectActive = showIssuesOnlyAfterProjectActive;
    }

    public boolean isShowResidualRiskRatings() {
        return showResidualRiskRatings;
    }

    public void setShowResidualRiskRatings(boolean showResidualRiskRatings) {
        this.showResidualRiskRatings = showResidualRiskRatings;
    }

    public String getInitialProbabilityRatingQuestionLabel() {
        return initialProbabilityRatingQuestionLabel;
    }

    public void setInitialProbabilityRatingQuestionLabel(String initialProbabilityRatingQuestionLabel) {
        this.initialProbabilityRatingQuestionLabel = initialProbabilityRatingQuestionLabel;
    }

    public String getInitialProbabilityRatingQuestionHintText() {
        return initialProbabilityRatingQuestionHintText;
    }

    public void setInitialProbabilityRatingQuestionHintText(String initialProbabilityRatingQuestionHintText) {
        this.initialProbabilityRatingQuestionHintText = initialProbabilityRatingQuestionHintText;
    }

    public String getInitialImpactRatingQuestionLabel() {
        return initialImpactRatingQuestionLabel;
    }

    public void setInitialImpactRatingQuestionLabel(String initialImpactRatingQuestionLabel) {
        this.initialImpactRatingQuestionLabel = initialImpactRatingQuestionLabel;
    }

    public String getInitialImpactRatingQuestionHintText() {
        return initialImpactRatingQuestionHintText;
    }

    public void setInitialImpactRatingQuestionHintText(String initialImpactRatingQuestionHintText) {
        this.initialImpactRatingQuestionHintText = initialImpactRatingQuestionHintText;
    }

    public String getInitialRiskRatingColumnHeading() {
        return initialRiskRatingColumnHeading;
    }

    public void setInitialRiskRatingColumnHeading(String initialRiskRatingColumnHeading) {
        this.initialRiskRatingColumnHeading = initialRiskRatingColumnHeading;
    }

    @PostLoad
    void loadBlockData() {
        RisksTemplateBlock data = JSONUtils.fromJSON(this.blockData, RisksTemplateBlock.class);
        if (data != null) {
            this.showIssuesOnlyAfterProjectActive = data.showIssuesOnlyAfterProjectActive;
            this.showResidualRiskRatings = data.showResidualRiskRatings;
            this.initialProbabilityRatingQuestionLabel = data.initialProbabilityRatingQuestionLabel;
            this.initialProbabilityRatingQuestionHintText = data.initialProbabilityRatingQuestionHintText;
            this.initialImpactRatingQuestionLabel = data.initialImpactRatingQuestionLabel;
            this.initialImpactRatingQuestionHintText = data.initialImpactRatingQuestionHintText;
            this.initialRiskRatingColumnHeading = data.initialRiskRatingColumnHeading;
        }
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        RisksTemplateBlock cloned = (RisksTemplateBlock) clone;
        cloned.showIssuesOnlyAfterProjectActive = this.showIssuesOnlyAfterProjectActive;
        cloned.showResidualRiskRatings = this.showResidualRiskRatings;
        cloned.initialProbabilityRatingQuestionLabel = this.initialProbabilityRatingQuestionLabel;
        cloned.initialProbabilityRatingQuestionHintText = this.initialProbabilityRatingQuestionHintText;
        cloned.initialImpactRatingQuestionLabel = this.initialImpactRatingQuestionLabel;
        cloned.initialImpactRatingQuestionHintText = this.initialImpactRatingQuestionHintText;
        cloned.initialRiskRatingColumnHeading = this.initialRiskRatingColumnHeading;
    }

}
