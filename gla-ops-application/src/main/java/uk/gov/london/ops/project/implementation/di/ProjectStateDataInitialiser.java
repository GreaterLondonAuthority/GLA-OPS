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
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.programme.ProgrammeServiceImpl;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectBuilder;
import uk.gov.london.ops.project.ProjectHistoryEntity;
import uk.gov.london.ops.project.ProjectTransition;
import uk.gov.london.ops.project.implementation.repository.ProjectStateRepository;
import uk.gov.london.ops.project.state.ProjectStateServiceImpl;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.project.template.domain.Template;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.london.ops.organisation.Organisation.TEST_ORG_ID_1;
import static uk.gov.london.ops.project.ProjectBuilder.STATUS_EMPTY;
import static uk.gov.london.ops.project.ProjectBuilder.STATUS_SUBMITTED;
import static uk.gov.london.ops.project.template.TemplateBuilder.MULTI_ASSESSMENT_TEMPLATE_NAME;

@Transactional
@Component
public class ProjectStateDataInitialiser implements DataInitialiserModule {

    @Autowired
    private ProjectBuilder projectBuilder;

    @Autowired
    private ProgrammeServiceImpl programmeService;

    @Autowired
    private ProjectStateRepository projectStateRepository;

    @Autowired
    private TemplateServiceImpl templateService;

    @Autowired
    Environment environment;

    private List<ProjectHistoryEntity> allProjectHistory;

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void addReferenceData() {
        //setup project history
        allProjectHistory = new ArrayList<>();
        ProjectHistoryEntity historyElement = new ProjectHistoryEntity();
        historyElement.setCreatedOn(environment.now());
        historyElement.setCreatedBy("");
        historyElement.setCreatorName("Test User");
        historyElement.setStatusName("Stage 1");
        historyElement.setSubStatusName("Submitted");
        historyElement.setTransition(ProjectTransition.Returned);
        allProjectHistory.add(historyElement);
    }

    @Override
    public void addProjects() {

    }

    @Override
    public void addSupplementalData() {
        projectStateRepository.saveAll(ProjectStateServiceImpl.projectStates);
    }

}
