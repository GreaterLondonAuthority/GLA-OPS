/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import uk.gov.london.ops.project.budget.ProjectBudgetsBlock;
import uk.gov.london.ops.project.deliverypartner.DeliveryPartnersBlock;
import uk.gov.london.ops.project.funding.FundingBlock;
import uk.gov.london.ops.project.grant.*;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.outputs.OutputsBlock;
import uk.gov.london.ops.project.outputs.OutputsCostsBlock;
import uk.gov.london.ops.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.project.receipt.ReceiptsBlock;
import uk.gov.london.ops.project.repeatingentity.OtherFundingBlock;
import uk.gov.london.ops.project.repeatingentity.ProjectElementsBlock;
import uk.gov.london.ops.project.repeatingentity.ProjectObjectivesBlock;
import uk.gov.london.ops.project.repeatingentity.UserDefinedOutputBlock;
import uk.gov.london.ops.project.risk.ProjectRisksBlock;
import uk.gov.london.ops.project.skills.FundingClaimsBlock;
import uk.gov.london.ops.project.skills.LearningGrantBlock;
import uk.gov.london.ops.project.template.domain.*;
import uk.gov.london.ops.project.unit.UnitDetailsBlock;

/**
 * Created by chris on 12/12/2016.
 */
public enum ProjectBlockType {
    Details("Project Details", ProjectDetailsTemplateBlock.class, ProjectDetailsBlock.class),
    Milestones("Milestones", MilestonesTemplateBlock.class, ProjectMilestonesBlock.class),
    ProjectBudgets("Spend", TemplateBlock.class, ProjectBudgetsBlock.class),
    Budgets("Budgets", TemplateBlock.class, NamedProjectBlock.class),
    CalculateGrant("Calculate Grant", TemplateBlock.class, CalculateGrantBlock.class),
    NegotiatedGrant("Negotiated Grant", TemplateBlock.class, NegotiatedGrantBlock.class),
    DeveloperLedGrant("Developer-led Grant", TemplateBlock.class, DeveloperLedGrantBlock.class),
    IndicativeGrant("Indicative Grant", IndicativeGrantTemplateBlock.class, IndicativeGrantBlock.class),
    GrantSource("Grant Source", GrantSourceTemplateBlock.class, GrantSourceBlock.class),
    DesignStandards("Design Standards", TemplateBlock.class, DesignStandardsBlock.class),
    Risks("Risks and Issues", RisksTemplateBlock.class, ProjectRisksBlock.class),
    Questions("Additional Questions", QuestionsTemplateBlock.class, ProjectQuestionsBlock.class),
    Outputs("Outputs", OutputsTemplateBlock.class, OutputsBlock.class),
    Receipts("Receipts", ReceiptsTemplateBlock.class, ReceiptsBlock.class),
    UnitDetails("Unit Details", TemplateBlock.class, UnitDetailsBlock.class),
    Funding("Budget", FundingTemplateBlock.class, FundingBlock.class),
    ProgressUpdates("Progress Updates", TemplateBlock.class, ProgressUpdateBlock.class),
    LearningGrant("Learning Grant", LearningGrantTemplateBlock.class, LearningGrantBlock.class),
    OutputsCosts("Outputs Costs", OutputsCostsTemplateBlock.class, OutputsCostsBlock.class),
    DeliveryPartners("Delivery Partners", DeliveryPartnersTemplateBlock.class, DeliveryPartnersBlock.class),
    ProjectObjectives("Project Objectives", ProjectObjectivesTemplateBlock.class, ProjectObjectivesBlock.class),
    FundingClaims("Funding Claims", FundingClaimsTemplateBlock.class, FundingClaimsBlock.class, LearningGrant),
    OtherFunding("Other Funding", OtherFundingTemplateBlock.class, OtherFundingBlock.class),
    UserDefinedOutput("User Defined Output", UserDefinedOutputTemplateBlock.class, UserDefinedOutputBlock.class),
    ProjectElements("Project Elements", ProjectElementsTemplateBlock.class, ProjectElementsBlock.class);

    private final String defaultName;
    private final Class<? extends TemplateBlock> templateBlockClass;
    private final Class<? extends NamedProjectBlock> projectBlockClass;
    private final ProjectBlockType dependsOn;

    ProjectBlockType(String defaultName, Class<? extends TemplateBlock> templateBlockClass,
            Class<? extends NamedProjectBlock> projectBlockClass) {
        this(defaultName, templateBlockClass, projectBlockClass, null);
    }

    ProjectBlockType(String defaultName, Class<? extends TemplateBlock> templateBlockClass,
            Class<? extends NamedProjectBlock> projectBlockClass, ProjectBlockType dependsOn) {
        this.defaultName = defaultName;
        this.templateBlockClass = templateBlockClass;
        this.projectBlockClass = projectBlockClass;
        this.dependsOn = dependsOn;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public Class<? extends TemplateBlock> getTemplateBlockClass() {
        return templateBlockClass;
    }

    public Class<? extends NamedProjectBlock> getProjectBlockClass() {
        return projectBlockClass;
    }

    public TemplateBlock newTemplateBlockInstance() {
        if (templateBlockClass == null) {
            throw new IllegalStateException("Could not instantiate template block: " + getDefaultName());
        } else {
            try {
                TemplateBlock templateBlock = templateBlockClass.newInstance();
                templateBlock.setBlockDisplayName(this.getDefaultName());
                templateBlock.setBlock(this);
                return templateBlock;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Error creating instance of" + templateBlockClass.getName(), e);
            }
        }
    }

    /**
     * Creates a new project block instance matching the template block type.
     */
    public NamedProjectBlock newProjectBlockInstance() {
        if (projectBlockClass == null) {
            throw new IllegalStateException("Could not instantiate project block: " + getDefaultName());
        } else {
            try {
                return projectBlockClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Error creating instance of" + projectBlockClass.getName(), e);
            }
        }
    }

    public ProjectBlockType getDependsOn() {
        return dependsOn;
    }
}