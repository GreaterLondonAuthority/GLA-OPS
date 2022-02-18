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

@Entity
@DiscriminatorValue("CALCULATE_GRANT")
public class CalculateGrantTemplateBlock extends BaseGrantTemplateBlock {

    public CalculateGrantTemplateBlock() {
        super(ProjectBlockType.CalculateGrant);
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        CalculateGrantTemplateBlock ngb = (CalculateGrantTemplateBlock) clone;
        ngb.setOtherAffordableTenureTypes(this.getOtherAffordableTenureTypes());
        ngb.setShowOtherAffordableQuestion(this.isShowOtherAffordableQuestion());
    }

    @PostLoad
    void loadBlockData() {
        CalculateGrantTemplateBlock data = JSONUtils.fromJSON(this.blockData, CalculateGrantTemplateBlock.class);
        if (data != null) {
            this.setOtherAffordableTenureTypes(data.getOtherAffordableTenureTypes());
            this.setShowOtherAffordableQuestion(data.isShowOtherAffordableQuestion());
        }
    }
}
