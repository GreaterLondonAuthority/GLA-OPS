/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.domain.project.InternalBlockType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ASSESSMENT")
public class InternalAssessmentTemplateBlock extends InternalTemplateBlock {

    public InternalAssessmentTemplateBlock() {
        super(InternalBlockType.Assessment);
        this.displayOrder = 100; // this is to ensure the assessment block always displays last
    }

    public InternalAssessmentTemplateBlock(String blockDisplayName) {
        this();
        this.blockDisplayName = blockDisplayName;
    }

    @Override
    public InternalAssessmentTemplateBlock clone() {
        InternalAssessmentTemplateBlock clone = (InternalAssessmentTemplateBlock) super.clone();
        return clone;
    }

}
