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
import uk.gov.london.ops.project.block.ProjectBlockType;

/**
 * Created by chris on 19/12/2016.
 */
@Entity
@DiscriminatorValue("RISKS")
public class RisksTemplateBlock extends TemplateBlock {

    public RisksTemplateBlock() {
        super(ProjectBlockType.Risks);
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
    }

}
