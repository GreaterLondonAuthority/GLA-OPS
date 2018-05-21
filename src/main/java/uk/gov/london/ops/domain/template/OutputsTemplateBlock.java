/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.domain.outputs.OutputConfigurationGroup;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * empty subclass as no specific information is currently required.
 * Created by chris on 16/02/2017.
 */
@Entity
@DiscriminatorValue("OUTPUTS")
public class OutputsTemplateBlock extends TemplateBlock {

    @JoinData(joinType = Join.JoinType.OneToOne, sourceTable = "template_block", targetColumn = "id", targetTable = "output_config_group",
            comment = "")
    @OneToOne
    @JoinColumn(name =  "output_config_group_id")
    private OutputConfigurationGroup outputConfigurationGroup;


    public OutputsTemplateBlock() {
        super();
    }

    public OutputsTemplateBlock(Integer displayOrder) {
        super(displayOrder, ProjectBlockType.Outputs);
    }

    public OutputsTemplateBlock(Integer displayOrder,String blockDisplayName) {
        super(displayOrder,  ProjectBlockType.Outputs, blockDisplayName);
    }

    public OutputConfigurationGroup getOutputConfigurationGroup() {
        return outputConfigurationGroup;
    }

    public void setOutputConfigurationGroup(OutputConfigurationGroup outputConfigurationGroup) {
        this.outputConfigurationGroup = outputConfigurationGroup;
    }
}
