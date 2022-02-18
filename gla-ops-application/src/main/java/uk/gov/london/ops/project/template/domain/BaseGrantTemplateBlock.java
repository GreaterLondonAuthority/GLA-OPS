/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.project.template.domain;

import uk.gov.london.ops.project.block.ProjectBlockType;

import javax.persistence.Transient;
import java.util.List;

public class BaseGrantTemplateBlock extends TemplateBlock {

    BaseGrantTemplateBlock(){}

    BaseGrantTemplateBlock(ProjectBlockType projectBlockType) {
        super(projectBlockType);
    }

    BaseGrantTemplateBlock(int displayOrder, ProjectBlockType projectBlockType) {
        super(displayOrder, projectBlockType);
    }

    @Transient
    List<String> otherAffordableTenureTypes;

    @Transient
    boolean showOtherAffordableQuestion = true;

    public List<String> getOtherAffordableTenureTypes() {
        return otherAffordableTenureTypes;
    }

    public void setOtherAffordableTenureTypes(List<String> otherAffordableTenureTypes) {
        this.otherAffordableTenureTypes = otherAffordableTenureTypes;
    }

    public boolean isShowOtherAffordableQuestion() {
        return showOtherAffordableQuestion;
    }

    public void setShowOtherAffordableQuestion(boolean showOtherAffordableQuestion) {
        this.showOtherAffordableQuestion = showOtherAffordableQuestion;
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        BaseGrantTemplateBlock bgtb = (BaseGrantTemplateBlock) clone;
        bgtb.setOtherAffordableTenureTypes(this.getOtherAffordableTenureTypes());
        bgtb.setShowOtherAffordableQuestion(this.isShowOtherAffordableQuestion());
    }

}
