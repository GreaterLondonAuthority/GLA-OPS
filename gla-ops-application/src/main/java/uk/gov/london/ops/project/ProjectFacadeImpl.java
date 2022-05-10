/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import org.springframework.stereotype.Service;
import uk.gov.london.ops.project.block.ProjectBlockService;
import uk.gov.london.ops.project.template.TemplateProjectService;

import java.util.Set;

@Service
public class ProjectFacadeImpl implements ProjectFacade {

    final ProjectService projectService;
    final ProjectBlockService projectBlockService;
    final TemplateProjectService templateProjectService;

    public ProjectFacadeImpl(ProjectService projectService, ProjectBlockService projectBlockService,
                             TemplateProjectService templateProjectService) {
        this.projectService = projectService;
        this.projectBlockService = projectBlockService;
        this.templateProjectService = templateProjectService;
    }

    @Override
    public ProjectDetailsSummary getProjectDetailsSummary(Integer projectId) {
        return projectService.getProjectDetailsSummary(projectId);
    }

    @Override
    public ProjectDetailsSummary getProjectDetailsSummary(String projectTitle) {
        return projectService.getProjectDetailsSummary(projectTitle);
    }

    @Override
    public ProjectBlockSummary getInternalAssessmentBlockSummary(Integer projectId) {
        return projectBlockService.getInternalAssessmentBlockSummary(projectId);
    }

    @Override
    public void updateAssumptionsAffectedByCategoryChange(Integer groupId, String oldName, String newName) {
        templateProjectService.updateAssumptionsAffectedByCategoryChange(groupId, oldName, newName);
    }

    @Override
    public Set<String> getProjectAssignees(Integer projectId, Set<String> roles) {
        return projectService.getProjectAssignees(projectId, roles);
    }

    @Override
    public Set<String> getProjectAssignees(Integer projectId) {
        return projectService.getProjectAssignees(projectId);
    }

}
