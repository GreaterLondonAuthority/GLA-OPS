/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

/**
 * Created by chris on 12/12/2016.
 */
public enum ProjectBlockType {
    Details("Project Details",ProjectDetailsBlock.class),
    Milestones("Milestones",ProjectMilestonesBlock.class),
    ProjectBudgets("Project budgets",ProjectBudgetsBlock.class),
    Budgets("Budgets",NamedProjectBlock.class),
    CalculateGrant("Calculate Grant",CalculateGrantBlock.class),
    NegotiatedGrant("Negotiated Grant",NegotiatedGrantBlock.class),
    DeveloperLedGrant("Developer-led Grant",DeveloperLedGrantBlock.class),
    IndicativeGrant("Indicative Grant",IndicativeGrantBlock.class),
    GrantSource("Grant Source",GrantSourceBlock.class),
    DesignStandards("Design Standards",DesignStandardsBlock.class),
    Risks("Risks and Issues",ProjectRisksBlock.class),
    Questions("Additional Questions",ProjectQuestionsBlock.class),
    Outputs("Outputs",OutputsBlock.class),
    Receipts("Receipts",ReceiptsBlock.class),
    UnitDetails("Unit Details",UnitDetailsBlock.class),;

    private final Class projectBlockClass;
    public String defaultName;

    ProjectBlockType(String defaultName, Class projectBlockClass) {
        this.defaultName = defaultName;
        this.projectBlockClass = projectBlockClass;
    }

    public String getDefaultName() {
        return defaultName;
    }

    /**
     * Creates a new project block instance matching the template block type.
     */
    public NamedProjectBlock newProjectBlockInstance() {
        if (projectBlockClass == null) {
            throw new IllegalStateException("Could not instantiate project block: " + getDefaultName());
        } else {
            try {
                return (NamedProjectBlock)projectBlockClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Error creating instance of" + projectBlockClass.getName(), e);
            }
        }
    }
}