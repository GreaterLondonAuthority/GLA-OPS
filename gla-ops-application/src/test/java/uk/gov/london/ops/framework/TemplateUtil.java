/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.skills.AllocationType;
import uk.gov.london.ops.project.state.StateModel;
import uk.gov.london.ops.project.template.domain.DetailsTemplate;
import uk.gov.london.ops.project.template.domain.IndicativeTenureConfiguration;
import uk.gov.london.ops.project.template.domain.InternalRiskTemplateBlock;
import uk.gov.london.ops.project.template.domain.LearningGrantTemplateBlock;
import uk.gov.london.ops.project.template.domain.MilestoneTemplate;
import uk.gov.london.ops.project.template.domain.MilestonesTemplateBlock;
import uk.gov.london.ops.project.template.domain.ProcessingRoute;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.project.template.domain.TemplateTenureType;
import uk.gov.london.ops.refdata.TenureType;

/**
 * Created by chris on 20/10/2016.
 */
public class TemplateUtil {

    public static Template getPopulatedTemplate() {
        Template template = new Template(12, "test");

        template.setStateModel(StateModel.ChangeControlled);

        template.setBlocksEnabled(new ArrayList<>());
        template.addNextBlock(ProjectBlockType.CalculateGrant);
        template.addNextBlock(ProjectBlockType.Details);
        template.addNextBlock(ProjectBlockType.Milestones);
        template.addNextBlock(ProjectBlockType.Questions);
        template.addNextBlock(ProjectBlockType.Receipts);
        template.addNextBlock(ProjectBlockType.ProjectBudgets);
        template.addNextBlock(ProjectBlockType.LearningGrant);

        DetailsTemplate detailsConfig = new DetailsTemplate();
        template.setDetailsConfig(detailsConfig);

        MilestoneTemplate tm1 = new MilestoneTemplate();
        tm1.setSummary("test milestone template 1");
        tm1.setRequirement(Requirement.mandatory);

        ProcessingRoute defaultProcessingRoute = new ProcessingRoute();
        defaultProcessingRoute.setName(ProcessingRoute.DEFAULT_PROCESSING_ROUTE_NAME);
        defaultProcessingRoute.setMilestones(new HashSet<>());
        defaultProcessingRoute.getMilestones().add(tm1);

        MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Milestones);
        milestonesTemplateBlock.setAutoCalculateMilestoneState(true);
        milestonesTemplateBlock.getProcessingRoutes().add(defaultProcessingRoute);

        TemplateTenureType tenureType = new TemplateTenureType(new TenureType(1, "AASAD"));
        tenureType.setTariffRate(120);
        tenureType.setDisplayOrder(1);
        template.setTenureTypes(new HashSet<>());
        template.getTenureTypes().add(tenureType);

        template.setIndicativeTenureConfiguration(new IndicativeTenureConfiguration(2016, 2, null, null));

        LearningGrantTemplateBlock learningGrantTemplateBlock = (LearningGrantTemplateBlock) template.getSingleBlockByType(ProjectBlockType.LearningGrant);
        learningGrantTemplateBlock.setStartYear(2018);
        learningGrantTemplateBlock.setNumberOfYears(1);
        learningGrantTemplateBlock.setAllocationTypes(Arrays.asList(AllocationType.Community, AllocationType.Delivery, AllocationType.ResponseFundStrand1));

        InternalRiskTemplateBlock riskTemplateBlock = new InternalRiskTemplateBlock();
        riskTemplateBlock.setDisplayOrder(1);
        riskTemplateBlock.setType(InternalBlockType.Risk);
        riskTemplateBlock.setRiskAdjustedFiguresFlag(false);
        template.setInternalBlocks(Arrays.asList(riskTemplateBlock));

        return template;
    }
}
