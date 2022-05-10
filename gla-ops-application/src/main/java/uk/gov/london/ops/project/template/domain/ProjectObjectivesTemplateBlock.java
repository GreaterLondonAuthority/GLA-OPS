/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.JSONUtils;

/**
 * empty subclass as no specific information is currently required. Created by chris on 16/02/2017.
 */
@Entity
@DiscriminatorValue("PROJECTOBJECTIVES")
public class ProjectObjectivesTemplateBlock extends RepeatingEntityTemplateBlock {

    @Transient
    private String objectiveTextSingular = "Objective";

    @Transient
    private String objectiveTextPlural = "Objectives";

    public String getObjectiveTextSingular() {
        return objectiveTextSingular;
    }

    public void setObjectiveTextSingular(String objectiveTextSingular) {
        this.objectiveTextSingular = objectiveTextSingular;
    }

    public String getObjectiveTextPlural() {
        return objectiveTextPlural;
    }

    public void setObjectiveTextPlural(String objectiveTextPlural) {
        this.objectiveTextPlural = objectiveTextPlural;
    }

    @PostLoad
    public void loadBlockData() {
        super.loadBlockData();
        ProjectObjectivesTemplateBlock data = JSONUtils.fromJSON(this.blockData, ProjectObjectivesTemplateBlock.class);
        if (data != null) {
            this.setObjectiveTextPlural(data.getObjectiveTextPlural());
            this.setObjectiveTextSingular(data.getObjectiveTextSingular());
        }
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        ProjectObjectivesTemplateBlock cloned = (ProjectObjectivesTemplateBlock) clone;
        cloned.setObjectiveTextPlural(getObjectiveTextPlural());
        cloned.setObjectiveTextSingular(getObjectiveTextSingular());
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }

}
