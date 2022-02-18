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
import java.util.List;
import java.util.stream.Collectors;

@Entity
@DiscriminatorValue("PROJECT_DETAILS")
public class ProjectDetailsTemplateBlock  extends TemplateBlock {

    @Transient
    private Integer maxBoroughs = 1;

    @Transient
    private String allocationQuestion = "Do you want to use your allocation to fund this project?";


    @PostLoad
    @PostPersist
    @PostUpdate
    public void loadBlockData() {
        ProjectDetailsTemplateBlock data = JSONUtils.fromJSON(this.blockData, ProjectDetailsTemplateBlock.class);
        if (data != null) {
            this.maxBoroughs = data.maxBoroughs;
            this.allocationQuestion = data.allocationQuestion;
        }
    }

    public Integer getMaxBoroughs() {
        return maxBoroughs;
    }

    public void setMaxBoroughs(Integer maxBoroughs) {
        this.maxBoroughs = maxBoroughs;
    }

    public String getAllocationQuestion() {
        return allocationQuestion;
    }

    public void setAllocationQuestion(String allocationQuestion) {
        this.allocationQuestion = allocationQuestion;
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        ProjectDetailsTemplateBlock cloned = (ProjectDetailsTemplateBlock) clone;
        cloned.setMaxBoroughs(this.getMaxBoroughs());
        cloned.setAllocationQuestion(this.getAllocationQuestion());
    }

    @Override
    public List<TemplateBlockCommand> getTemplateBlockCommands() {
        return super.getTemplateBlockCommands().stream()
                .filter(b -> b != TemplateBlockCommand.REMOVE_BLOCK && b != TemplateBlockCommand.UPDATE_DISPLAY_NAME)
                .collect(Collectors.toList());
    }

}
