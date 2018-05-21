/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.project.ProjectSummary;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ApiError;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.ProgrammeService;
import uk.gov.london.ops.service.TemplateService;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.service.project.ProjectService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing project templates")
public class TemplateAPI {
    private final TemplateService templateService;
    private final UserService userService;
    private final Environment environment;
    private final ProjectService projectService;
    private final ProgrammeService programmeService;

    TemplateAPI(final TemplateService templateService,
                final ProjectService projectService,
                final UserService userService,
                final ProgrammeService programmeService,
                final Environment environment) {
        this.templateService = templateService;
        this.projectService = projectService;
        this.userService = userService;
        this.programmeService = programmeService;
        this.environment = environment;
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/templates", method = RequestMethod.GET)
    public List<Template> getAll(@RequestParam(value = "programmeId", required = false) Integer programmeId) {
        if (programmeId != null) {
            Programme p = programmeService.find(programmeId);
            Collection<Template> templates = p.getTemplates();
            if(templates != null) {
                return templates.stream().collect(Collectors.toList());
            }
            else {
                return new ArrayList<>();
            }
        }
        else {
            return templateService.findAll();
        }
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/templates/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody String get(@PathVariable Integer id, @RequestParam(name="jsonclob", defaultValue = "false") boolean jsonClob) throws IOException {
        if (jsonClob) {
            return templateService.getTemplateData(id).getJson();
        } else {
            return templateService.getTemplateJson(id);
        }
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/templates/{id}/json", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody String getTemplateJson(@PathVariable Integer id) throws IOException {
        return templateService.getTemplateJson(id);
    }

    /**
     * @deprecated TODO : remove
     */
    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/legacy", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code=400, message="validation error", response=ApiError.class))
    public Integer create(@Valid @RequestBody Template template, BindingResult bindingResult) {
        if (template.getId() != null) {
            throw new ValidationException("cannot edit existing template!");
        }

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid template!", bindingResult.getFieldErrors());
        }

        template.setCreatedBy(userService.currentUser().getUsername());
        template.setCreatedOn(environment.now());

        return templateService.save(template).getId();
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code=400, message="validation error", response=ApiError.class))
    public Integer create(@RequestBody String templateJson) throws IOException {
        return templateService.create(templateJson).getId();
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/clone/{id}", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code=400, message="validation error", response=ApiError.class))
    public Integer cloneTemplate(@PathVariable Integer id, @Valid @RequestBody String newName, BindingResult bindingResult) {
        Template template = templateService.find(id);
        Template cloned = templateService.cloneTemplate(template, newName);
        return cloned.getId();
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{id}", method = RequestMethod.DELETE)
    public String deleteById(@PathVariable Integer id) {
        templateService.delete(id);
        return "Deleted project " + id;
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{id}/projects", method = RequestMethod.GET)
    public List<ProjectSummary> getTemplateProjects(@PathVariable Integer id) {
        List<ProjectSummary> templateProjects = new LinkedList<>();
        for (ProjectSummary project : projectService.getProjectSummaries(null, null, null, null, null)) {
            if (project.getTemplateId().equals(id)) {
                templateProjects.add(project);
            }
        }
        return templateProjects;
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/block", method = RequestMethod.POST)
    public void addBlock(@PathVariable("templateId") int templateId, @RequestBody TemplateBlock block) throws InterruptedException {
        templateService.addBlock(templateId, block);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions", method = RequestMethod.POST)
    public void addQuestion(@PathVariable Integer templateId, @PathVariable int displayOrder,
                            @Valid @RequestBody TemplateQuestion templateQuestion, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid question creation request!", bindingResult.getFieldErrors());
        }
        templateService.addQuestion(templateId, displayOrder, templateQuestion);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/{questionId}/requirement", method = RequestMethod.PUT)
    public void updateQuestionRequirement(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                                          @PathVariable Integer questionId, @RequestBody String requirement) {
        templateService.updateQuestionRequirement(templateId, displayOrder, questionId, Requirement.valueOf(requirement));
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/{questionId}", method = RequestMethod.DELETE)
    public void removeQuestion(@PathVariable Integer templateId, @PathVariable Integer displayOrder, @PathVariable Integer questionId) {
        templateService.removeQuestion(templateId, displayOrder, questionId);
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/templates/availableBlocks", method = RequestMethod.GET)
    public List<String> getAvailableBlocks() {
        List<String> blockNames = new LinkedList<>();
        for (ProjectBlockType block : ProjectBlockType.values()) {
            blockNames.add(block.name());
        }
        return blockNames;
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/contract", method = RequestMethod.PUT)
    public void setContract(@PathVariable Integer templateId, @RequestBody Integer contractId)  {
        templateService.setTemplateContract(templateId, contractId);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/contract", method = RequestMethod.DELETE)
    public void setContract(@PathVariable Integer templateId)  {
        templateService.removeTemplateContract(templateId);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/associatedProjectsEnabled", method = RequestMethod.PUT)
    public void updateAssociatedProjectsEnabled(@PathVariable Integer templateId, @RequestBody String enabled)  {
        templateService.updateAssociatedProjectsEnabled(templateId, Boolean.valueOf(enabled));
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/milestoneDescriptionHintText", method = RequestMethod.PUT)
    public void updateMilestoneDescriptionHintText(@PathVariable Integer templateId, @RequestBody String text)  {
        templateService.updateMilestoneDescriptionHintText(templateId, text);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/milestoneType", method = RequestMethod.PUT)
    public void updateMilestoneType(@PathVariable Integer templateId, @RequestBody String type)  {
        templateService.updateMilestoneType(templateId, Template.MilestoneType.valueOf(type));
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/startOnSiteRestrictionText", method = RequestMethod.PUT)
    public void updateStartOnSiteRestrictionText(@PathVariable Integer templateId, @RequestBody String text)  {
        templateService.updateStartOnSiteRestrictionText(templateId, text);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/grantTotalText", method = RequestMethod.PUT)
    public void updateGrantTotalText(@PathVariable Integer templateId, @PathVariable Integer displayOrder, @RequestBody String text)  {
        templateService.updateGrantTotalText(templateId, displayOrder, text);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneDescriptionEnabled", method = RequestMethod.PUT)
    public void updateMilestoneDescriptionEnabled(@PathVariable Integer templateId, @PathVariable Integer displayOrder, @RequestBody String enabled)  {
        templateService.updateMilestoneDescriptionEnabled(templateId, displayOrder, Boolean.valueOf(enabled));
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneEvidenceLimit", method = RequestMethod.PUT)
    public void updateMilestoneEvidenceRequirements(@PathVariable Integer templateId, @PathVariable Integer displayOrder, @RequestBody Integer newLimit)  {
        templateService.updateMilestoneAllowableEvidenceDocuments(templateId, displayOrder, newLimit);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneEvidenceApplicability", method = RequestMethod.PUT)
    public void updateMilestoneEvidenceRequirements(@PathVariable Integer templateId, @PathVariable Integer displayOrder, @RequestBody String applicability)  {
        templateService.updateMilestoneAllowableEvidenceDocuments(templateId, displayOrder, MilestonesTemplateBlock.EvidenceApplicability.valueOf(applicability));
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/processingRoutes/{processingRouteId}/milestones/{milestoneId}/naSelectable", method = RequestMethod.PUT)
    public void updateMilestoneNaSelectable(@PathVariable Integer templateId,
                                            @PathVariable Integer displayOrder,
                                            @PathVariable Integer processingRouteId,
                                            @PathVariable Integer milestoneId,
                                            @RequestBody String selectable)  {
        templateService.updateMilestoneNaSelectable(templateId, displayOrder, processingRouteId, milestoneId, Boolean.valueOf(selectable));
    }

}
