/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import javax.persistence.PostLoad;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.project.block.ProjectBlockType;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by carmina on 03/12/2019.
 */
public abstract class RepeatingEntityTemplateBlock extends TemplateBlock {

    public static final Integer MIN_NUMBER_OF_ENTITIES = 1;
    public static final Integer MAX_NUMBER_OF_ENTITIES = 50;

    @Transient
    private Integer minNumberOfEntities = MIN_NUMBER_OF_ENTITIES;

    @Transient
    private Integer maxNumberOfEntities = MAX_NUMBER_OF_ENTITIES;

    @Transient
    private boolean hasBlockRequiredOption;

    @Transient
    private String blockRequiredOptionText;

    public Integer getMinNumberOfEntities() {
        return minNumberOfEntities;
    }

    public void setMinNumberOfEntities(Integer minNumberOfEntities) {
        this.minNumberOfEntities = minNumberOfEntities;
    }

    public Integer getMaxNumberOfEntities() {
        return maxNumberOfEntities;
    }

    public void setMaxNumberOfEntities(Integer maxNumberOfEntities) {
        this.maxNumberOfEntities = maxNumberOfEntities;
    }

    public RepeatingEntityTemplateBlock() {
    }

    public RepeatingEntityTemplateBlock(ProjectBlockType projectBlockType) {
        super(projectBlockType);
    }

    public RepeatingEntityTemplateBlock(Integer displayOrder, ProjectBlockType block) {
        super(displayOrder, block);
    }

    public boolean getHasBlockRequiredOption() {
        return hasBlockRequiredOption;
    }

    public void setHasBlockRequiredOption(boolean hasBlockRequiredOption) {
        this.hasBlockRequiredOption = hasBlockRequiredOption;
    }

    public String getBlockRequiredOptionText() {
        return blockRequiredOptionText;
    }

    public void setBlockRequiredOptionText(String blockRequiredOptionText) {
        this.blockRequiredOptionText = blockRequiredOptionText;
    }

    @PostLoad
    public void loadBlockData() {
        RepeatingEntityTemplateBlock data = JSONUtils.fromJSON(this.blockData, RepeatingEntityTemplateBlock.class);
        if (data != null) {
            this.setMinNumberOfEntities(data.getMinNumberOfEntities());
            this.setMaxNumberOfEntities(data.getMaxNumberOfEntities());
            this.setHasBlockRequiredOption(data.getHasBlockRequiredOption());
            this.setBlockRequiredOptionText(data.getBlockRequiredOptionText());
        }
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        if (clone instanceof RepeatingEntityTemplateBlock) {
            RepeatingEntityTemplateBlock cloned = (RepeatingEntityTemplateBlock) clone;
            cloned.setMinNumberOfEntities(getMinNumberOfEntities());
            cloned.setMaxNumberOfEntities(getMaxNumberOfEntities());
            cloned.setHasBlockRequiredOption(getHasBlockRequiredOption());
            cloned.setBlockRequiredOptionText(getBlockRequiredOptionText());
        }
    }

    @Override
    public void mergeConfig(TemplateBlock updatedBlock) {
        super.mergeConfig(updatedBlock);
        RepeatingEntityTemplateBlock updated = (RepeatingEntityTemplateBlock) updatedBlock;

        this.setHasBlockRequiredOption(updated.getHasBlockRequiredOption());
        this.minNumberOfEntities = updated.minNumberOfEntities;
        this.maxNumberOfEntities = updated.maxNumberOfEntities;

        if (updated.getBlockRequiredOptionText() != null) {
            this.setBlockRequiredOptionText(updated.getBlockRequiredOptionText());
        }
    }
}
