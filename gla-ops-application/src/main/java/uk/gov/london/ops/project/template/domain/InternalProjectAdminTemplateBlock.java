/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import uk.gov.london.ops.project.internalblock.InternalBlockType;

import javax.persistence.*;

@Entity
@DiscriminatorValue("PROJECT_ADMIN")
public class InternalProjectAdminTemplateBlock extends InternalTemplateBlock {

    public InternalProjectAdminTemplateBlock() {
        super(InternalBlockType.ProjectAdmin);
    }

}
