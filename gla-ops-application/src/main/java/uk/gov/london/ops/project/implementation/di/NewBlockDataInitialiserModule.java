/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.programme.ProgrammeBuilder;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectBuilder;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockStatus;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.project.template.domain.Template;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;

import static uk.gov.london.ops.organisation.Organisation.TEST_ORG_ID_1;
import static uk.gov.london.ops.project.ProjectBuilder.STATUS_ACTIVE;

@Transactional
@Component
public class NewBlockDataInitialiserModule implements DataInitialiserModule {

    @Autowired
    private ProgrammeBuilder programmeBuilder;

    @Autowired
    private ProjectBuilder projectBuilder;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TemplateServiceImpl templateService;

    private Programme programme;

    Template devLedRouteTemplate;

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void addProgrammes() {
        devLedRouteTemplate = templateService.findByName("");

        programme = programmeBuilder.createTestProgramme(
                "", false, true,
                devLedRouteTemplate
        );
    }

    @Override
    public void addProjects() {
        Project activeProject = projectBuilder.createPopulatedTestProject("", programme, devLedRouteTemplate,
                TEST_ORG_ID_1, STATUS_ACTIVE);
        projectBuilder.approveProject(activeProject);
        NamedProjectBlock questionsBlock = activeProject.getBlocksByTypeAndDisplayOrder(ProjectBlockType.Questions, 5).get(0);
        activeProject.getUnitDetailsBlock().setLastModified(OffsetDateTime.now());
        activeProject.getUnitDetailsBlock().setBlockStatus(ProjectBlockStatus.UNAPPROVED);
        questionsBlock.setLastModified(OffsetDateTime.now());
        questionsBlock.setBlockStatus(ProjectBlockStatus.UNAPPROVED);
        projectService.revertProjectBlock(activeProject.getId(), activeProject.getUnitDetailsBlock().getId());
        projectService.revertProjectBlock(activeProject.getId(), questionsBlock.getId());
    }

}
