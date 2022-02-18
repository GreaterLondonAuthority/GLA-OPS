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

@Entity
@DiscriminatorValue("NEGOTIATED_GRANT")
public class NegotiatedGrantTemplateBlock extends BaseGrantTemplateBlock {

    @Transient
    private boolean showSpecialisedUnits = true;

    @Transient
    private boolean showDevelopmentCost = true;

    @Transient
    private boolean showPercentageCosts = true;

    public NegotiatedGrantTemplateBlock() {
        super(ProjectBlockType.NegotiatedGrant);
    }

    public NegotiatedGrantTemplateBlock(int displayOrder) {
        super(displayOrder, ProjectBlockType.NegotiatedGrant);
    }

    public boolean isShowSpecialisedUnits() {
        return showSpecialisedUnits;
    }

    public void setShowSpecialisedUnits(boolean showSpecialisedUnits) {
        this.showSpecialisedUnits = showSpecialisedUnits;
    }

    public boolean isShowDevelopmentCost() {
        return showDevelopmentCost;
    }

    public void setShowDevelopmentCost(boolean showDevelopmentCost) {
        this.showDevelopmentCost = showDevelopmentCost;
    }

    public boolean isShowPercentageCosts() {
        return showPercentageCosts;
    }

    public void setShowPercentageCosts(boolean showPercentageCosts) {
        this.showPercentageCosts = showPercentageCosts;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        NegotiatedGrantTemplateBlock ngb = (NegotiatedGrantTemplateBlock) clone;
        ngb.setShowSpecialisedUnits(this.isShowSpecialisedUnits());
        ngb.setShowDevelopmentCost(this.isShowDevelopmentCost());
        ngb.setShowPercentageCosts(this.isShowPercentageCosts());
        ngb.setOtherAffordableTenureTypes(this.getOtherAffordableTenureTypes());
        ngb.setShowOtherAffordableQuestion(this.isShowOtherAffordableQuestion());
    }

    @PostLoad
    void loadBlockData() {
        NegotiatedGrantTemplateBlock data = JSONUtils.fromJSON(this.blockData, NegotiatedGrantTemplateBlock.class);
        if (data != null) {
            this.setShowSpecialisedUnits(data.isShowSpecialisedUnits());
            this.setShowDevelopmentCost(data.isShowDevelopmentCost());
            this.setShowPercentageCosts(data.isShowPercentageCosts());
            this.setOtherAffordableTenureTypes(data.getOtherAffordableTenureTypes());
            this.setShowOtherAffordableQuestion(data.isShowOtherAffordableQuestion());
        }
    }
}
