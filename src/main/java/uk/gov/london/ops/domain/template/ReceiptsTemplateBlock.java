/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.domain.project.ProjectBlockType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("RECEIPTS")
public class ReceiptsTemplateBlock extends TemplateBlock {

    public ReceiptsTemplateBlock() {
        super(ProjectBlockType.Receipts);
    }

    public ReceiptsTemplateBlock(Integer displayOrder) {
        super(displayOrder, ProjectBlockType.Receipts);
    }

}
