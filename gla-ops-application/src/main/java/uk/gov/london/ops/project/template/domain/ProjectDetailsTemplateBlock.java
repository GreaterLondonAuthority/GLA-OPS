/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.project.template.domain;

import uk.gov.london.ops.framework.JSONUtils;

import javax.persistence.*;

@Entity
@DiscriminatorValue("PROJECT_DETAILS")
public class ProjectDetailsTemplateBlock  extends TemplateBlock {

    @Transient
    private Integer maxBoroughs =1;

    @PostLoad
    @PostPersist
    @PostUpdate
    public void loadBlockData() {
        ProjectDetailsTemplateBlock data = JSONUtils.fromJSON(this.blockData, ProjectDetailsTemplateBlock.class);
        if (data != null) {
            this.maxBoroughs = data.maxBoroughs;
        }
    }

    public Integer getMaxBoroughs() {
        return maxBoroughs;
    }

    public void setMaxBoroughs(Integer maxBoroughs) {
        this.maxBoroughs = maxBoroughs;
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        ProjectDetailsTemplateBlock cloned = (ProjectDetailsTemplateBlock) clone;
        cloned.setMaxBoroughs(this.getMaxBoroughs());
    }
}
