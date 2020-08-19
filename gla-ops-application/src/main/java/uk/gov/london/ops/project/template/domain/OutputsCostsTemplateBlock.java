/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import uk.gov.london.ops.project.block.ProjectBlockType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("OUTPUTS_COSTS")
public class OutputsCostsTemplateBlock extends TemplateBlock {

    @Column(name = "default_recovery_output_id")
    private Integer defaultRecoveryOutputId;

    @Column(name = "hide_advance_payment")
    private boolean hideAdvancePayment;

    public OutputsCostsTemplateBlock() {
        super(ProjectBlockType.OutputsCosts);
    }

    public Integer getDefaultRecoveryOutputId() {
        return defaultRecoveryOutputId;
    }

    public void setDefaultRecoveryOutputId(Integer defaultRecoveryOutputId) {
        this.defaultRecoveryOutputId = defaultRecoveryOutputId;
    }

    public boolean isHideAdvancePayment() {
        return hideAdvancePayment;
    }

    public void setHideAdvancePayment(boolean hideAdvancePayment) {
        this.hideAdvancePayment = hideAdvancePayment;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        OutputsCostsTemplateBlock block = (OutputsCostsTemplateBlock) clone;
        block.setHideAdvancePayment(this.isHideAdvancePayment());
        block.setDefaultRecoveryOutputId(this.getDefaultRecoveryOutputId());
    }

}
