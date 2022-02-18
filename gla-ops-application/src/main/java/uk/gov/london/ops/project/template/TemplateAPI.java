/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.common.error.ApiError;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.programme.ProgrammeServiceImpl;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.ProjectSummary;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.grant.AffordableHomesOfWhichCategory;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.migration.BlockMigrationService;
import uk.gov.london.ops.project.migration.MigrationRequest;
import uk.gov.london.ops.project.migration.MigrationResponse;
import uk.gov.london.ops.project.skills.AllocationType;
import uk.gov.london.ops.project.template.domain.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.OPSUtils.currentUsername;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;
import static uk.gov.london.ops.permission.PermissionType.TEMP_MANAGE;

@RestController
@RequestMapping("/api/v1")
@Api("managing project templates")
public class TemplateAPI {

    public static final String POSTMAN_ONLY = "POSTMAN_ONLY";

    private final TemplateServiceImpl templateService;
    private final Environment environment;
    private final ProjectService projectService;
    private final ProgrammeServiceImpl programmeService;
    private final BlockMigrationService blockMigrationService;

    TemplateAPI(final TemplateServiceImpl templateService,
                final ProjectService projectService,
                final ProgrammeServiceImpl programmeService,
                final Environment environment,
                final BlockMigrationService blockMigrationService) {
        this.templateService = templateService;
        this.projectService = projectService;
        this.programmeService = programmeService;
        this.environment = environment;
        this.blockMigrationService = blockMigrationService;
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/summary", method = RequestMethod.GET)
    public Page<TemplateSummary> getAllTemplatesSummary(@RequestParam(required = false, defaultValue = "") String templateText,
            @RequestParam(required = false, defaultValue = "") String programmeText,
            @RequestParam(required = false) List<Template.TemplateStatus> selectedTemplateStatuses,
            Pageable pageable) {
        return templateService.getTemplateSummaries(templateText, programmeText, selectedTemplateStatuses, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/templates", method = RequestMethod.GET)
    public List<Template> getAll(@RequestParam(value = "programmeId", required = false) Integer programmeId) {
        List<Template> templateList;
        if (programmeId != null) {
            Programme p = programmeService.find(programmeId);
            Collection<Template> templates = p.getTemplates();
            if (templates != null) {
                templateList = templates.stream().collect(Collectors.toList());

            } else {
                return new ArrayList<>();
            }
        } else {
            templateList = templateService.findAll();
        }

        return templateList.stream().sorted(Comparator.comparingInt(Template::getId)).collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/templatesSummaries", method = RequestMethod.GET)
    public List<TemplateSummary> getAllTemplatesSummaries() {
        return templateService.getTemplatesSummaries();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_PROGRAMME_ADMIN, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN, INTERNAL_BLOCK_EDITOR})
    @RequestMapping(value = "/templates/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    String get(@PathVariable Integer id,
               @RequestParam(name = "sanitise", defaultValue = "false") boolean sanitise,
               @RequestParam(name = "jsonclob", defaultValue = "false") boolean jsonClob) throws IOException {
        if (jsonClob) {
            return templateService.getTemplateData(id).getJson();
        } else {
            return templateService.getTemplateJson(id, sanitise);
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_PROGRAMME_ADMIN, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN, INTERNAL_BLOCK_EDITOR})
    @RequestMapping(value = "/templates/{id}/json", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    String getTemplateJson(@PathVariable Integer id) throws IOException {
        return templateService.getTemplateJson(id, false);
    }

    /**
     * @deprecated TODO : remove
     */
    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/legacy", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer create(@Valid @RequestBody Template template, BindingResult bindingResult) {
        if (template.getId() != null) {
            throw new ValidationException("cannot edit existing template!");
        }
        if (template.getStateModel() == null) {
            throw new ValidationException("No state model is present!");
        }

        verifyBinding("Invalid template!", bindingResult);

        template.setCreatedBy(currentUsername());
        template.setCreatedOn(environment.now());

        return templateService.save(template).getId();
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer create(@RequestBody String templateJson) throws IOException {
        return templateService.create(templateJson).getId();
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/draft", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer createDraftTemplate(@RequestBody String templateJson) {
        return templateService.createDraft(templateJson);
    }


    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/validate", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void validateTemplate(@RequestBody String templateJson) {
        templateService.validateTemplate(templateJson);
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/{id}", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateDraftTemplate(@PathVariable Integer id, @RequestBody String templateJson) {
        templateService.updateTemplate(id, templateJson);
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/inUse/{id}", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateInUseTemplate(@PathVariable Integer id, @RequestBody String templateJson) throws JsonProcessingException {
        templateService.updateInUseTemplate(id, templateJson);
    }


    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/clone/{id}", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer cloneTemplate(@PathVariable Integer id, @Valid @RequestBody String newName) {
        Template template = templateService.find(id);
        Template cloned = templateService.cloneTemplate(template, newName);
        return cloned.getId();
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/{id}/displayOrder/{displayOrder}/performCommand", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "displayOrder error", response = ApiError.class))
    public Template performCommand(@PathVariable Integer id,
                                  @PathVariable Integer displayOrder,
                                  @RequestParam String command,
                                  @RequestParam boolean internalBlock,
                                  @Valid @RequestBody String payload) throws Exception {
        return templateService.performCommand(TemplateBlockCommand.valueOf(command), id, internalBlock, displayOrder, payload);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{id}", method = RequestMethod.DELETE)
    public String deleteById(@PathVariable Integer id) {
        templateService.delete(id);
        return "Deleted project " + id;
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{id}/message/warning/", method = RequestMethod.PUT)
    public void updateWarningMessage(@PathVariable Integer id, @RequestBody String newMessage) {
        templateService.updateWarningMessage(id, newMessage);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{id}/message/warning/", method = RequestMethod.DELETE)
    public void deleteWarningMessage(@PathVariable Integer id) {
        templateService.updateWarningMessage(id, null);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{id}/indicative/zeroUnitsAllowed/", method = RequestMethod.PUT)
    public void setZeroUnitsAllowedForIndicativeBlock(@PathVariable Integer id) {
        templateService.setZeroUnitsAllowedForIndicativeBlock(id);
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/{id}/projects", method = RequestMethod.GET)
    public List<ProjectSummary> getTemplateProjects(@PathVariable Integer id) {
        List<ProjectSummary> templateProjects = new LinkedList<>();
        for (ProjectSummary project : projectService.findAll(null, null, null, null,
                null, null, null, false, null, null).getContent()) {
            if (project.getTemplateId().equals(id)) {
                templateProjects.add(project);
            }
        }

        return templateProjects;
    }


    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/block/{blockType}/reorder/from/{blockDisplayOrder}/to/{newDisplayOrder}",
            method = RequestMethod.PUT)
    @Transactional
    public void reorderBlock(@PathVariable("templateId") int templateId,
                             @PathVariable("blockType") ProjectBlockType blockType,
                             @PathVariable("blockDisplayOrder") int blockDisplayOrder,
                             @PathVariable("newDisplayOrder") int newDisplayOrder) throws InterruptedException {

        Template template = templateService.find(templateId);
        if (template == null) {
            throw new ValidationException("Not found template with id " + templateId);
        } else {
            if (template.getBlocksEnabled().stream().anyMatch(b -> b.getDisplayOrder().equals(newDisplayOrder))) {
                throw new ValidationException("Display order already used in template " + templateId);
            } else {
                if (template.getBlocksEnabled().stream()
                        .anyMatch(b -> b.getBlock().equals(blockType) && b.getDisplayOrder().equals(blockDisplayOrder))) {
                    templateService.reorderBlock(templateId, blockType, blockDisplayOrder, newDisplayOrder);
                } else {
                    throw new ValidationException(
                            "Block " + blockType + " with display order " + blockDisplayOrder + " doesn't exist on template "
                                    + templateId);
                }
            }
        }
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/block", method = RequestMethod.POST)
    @Transactional
    @ApiOperation(value = "Adds a block to this template, only certain blocks can be added", tags = POSTMAN_ONLY)
    public void addBlock(@PathVariable("templateId") int templateId, @RequestBody TemplateBlock block)
            throws InterruptedException {

        // validation done here , as async method call
        Template template = templateService.find(templateId);
        if (template == null) {
            throw new ValidationException("Not found template with id " + templateId);
        }

        if (template.getBlocksEnabled().stream().anyMatch(b -> b.getDisplayOrder().equals(block.getDisplayOrder()))) {
            throw new ValidationException("Display order already used in template " + templateId);
        } else {
            templateService.addBlockAsync(templateId, block);
        }
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalBlocks", method = RequestMethod.POST)
    public void addInternalBlock(@PathVariable("templateId") int templateId, @RequestBody InternalTemplateBlock block) {
        templateService.addInternalBlock(templateId, block);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions", method = RequestMethod.POST)
    public void addQuestion(@PathVariable Integer templateId, @PathVariable int displayOrder,
                            @Valid @RequestBody TemplateQuestion templateQuestion, BindingResult bindingResult) {
        verifyBinding("Invalid question creation request!", bindingResult);
        templateService.addQuestion(templateId, displayOrder, templateQuestion);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalBlocks/{displayOrder}/questions",
            method = RequestMethod.POST)
    @ApiOperation(value = "Adds a question to an internal block on this template", tags = POSTMAN_ONLY)
    public void addQuestionToInternalBlock(@PathVariable Integer templateId,
                                           @PathVariable int displayOrder,
                                           @Valid @RequestBody TemplateQuestion templateQuestion, BindingResult bindingResult) {
        verifyBinding("Invalid question creation request!", bindingResult);
        templateService.addQuestionToInternalBlock(templateId, displayOrder, templateQuestion);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/section",
            method = RequestMethod.POST)
    @ApiOperation(value = "Adds a new section to an questions block on this template", tags = POSTMAN_ONLY)
    public void addSection(@PathVariable Integer templateId, @PathVariable int displayOrder,
                           @Valid @RequestBody QuestionsBlockSection section, BindingResult bindingResult) {
        verifyBinding("Invalid section creation request!", bindingResult);
        templateService.addSection(templateId, displayOrder, section);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalBlocks/{displayOrder}/questions/section",
            method = RequestMethod.POST)
    @ApiOperation(value = "Adds a new section to an internal questions block on this template", tags = POSTMAN_ONLY)
    public void addSectionToInternalBlock(@PathVariable Integer templateId, @PathVariable int displayOrder,
                                          @Valid @RequestBody QuestionsBlockSection section, BindingResult bindingResult) {
        verifyBinding("Invalid section creation request!", bindingResult);
        templateService.addInternalQuestionSection(templateId, displayOrder, section);
    }


    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/{questionId}/requirement",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Update question requirement flag for existing question", tags = POSTMAN_ONLY)
    public void updateQuestionRequirement(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                                          @PathVariable Integer questionId, @RequestBody String requirement) {
        templateService.updateQuestionRequirement(templateId, displayOrder,
                questionId, Requirement.valueOf(requirement));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{blockDisplayOrder}/questions/{questionId}/displayOrder",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Update display order for this question on this template", tags = POSTMAN_ONLY)
    public void updateQuestionDisplayOrder(@PathVariable Integer templateId, @PathVariable Integer blockDisplayOrder,
                                           @PathVariable Integer questionId, @RequestBody double newDisplayOrder) {

        templateService.updateQuestionDisplayOrder(templateId, blockDisplayOrder, questionId, newDisplayOrder, false);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalBlocks/{blockDisplayOrder}/questions/{questionId}/displayOrder",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Update display order for this question on this template for an internal block", tags = POSTMAN_ONLY)
    public void updateInternalQuestionDisplayOrder(@PathVariable Integer templateId, @PathVariable Integer blockDisplayOrder,
                                           @PathVariable Integer questionId, @RequestBody double newDisplayOrder) {

        templateService.updateQuestionDisplayOrder(templateId, blockDisplayOrder, questionId, newDisplayOrder, true);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{blockDisplayOrder}/questions/{questionId}/sections",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Update existing question to existing section", tags = POSTMAN_ONLY)
    public void addQuestionToSection(@PathVariable Integer templateId, @PathVariable Integer blockDisplayOrder,
                                     @PathVariable Integer questionId, @RequestBody Integer sectionId) {

        templateService.addQuestionToSection(templateId, blockDisplayOrder, questionId, sectionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalBlocks/{blockDisplayOrder}/questions/{questionId}/sections",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Update existing question to existing section", tags = POSTMAN_ONLY)
    public void addQuestionToInternalSection(@PathVariable Integer templateId, @PathVariable Integer blockDisplayOrder,
                                             @PathVariable Integer questionId, @RequestBody Integer sectionId) {

        templateService.addQuestionToInternalSection(templateId, blockDisplayOrder, questionId, sectionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{blockDisplayOrder}/questions/{questionId}/removeFromSection",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Removes existing question from given section", tags = POSTMAN_ONLY)
    public void removeQuestionFromSection(@PathVariable Integer templateId, @PathVariable Integer blockDisplayOrder,
                                          @PathVariable Integer questionId) {

        templateService.removeQuestionFromSection(templateId, blockDisplayOrder, questionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalBlocks/{blockDisplayOrder}/questions/{questionId}/removeFromSection",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Removes existing internal question from given section", tags = POSTMAN_ONLY)
    public void removeQuestionFromInternalSection(@PathVariable Integer templateId,
                                                  @PathVariable Integer blockDisplayOrder,
                                                  @PathVariable Integer questionId) {

        templateService.removeQuestionFromInternalSection(templateId, blockDisplayOrder, questionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/{questionId}",
            method = RequestMethod.DELETE)
    @ApiOperation(value = "Removes existing  question from given block", tags = POSTMAN_ONLY)
    public void removeQuestion(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                               @PathVariable Integer questionId) {
        templateService.removeQuestion(templateId, displayOrder, questionId, null);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalBlocks/{displayOrder}/questions/{questionId}",
            method = RequestMethod.DELETE)
    @ApiOperation(value = "Removes existing  question from given block", tags = POSTMAN_ONLY)
    public void removeInternalQuestion(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                               @PathVariable Integer questionId) {
        templateService.removeInternalQuestion(templateId, displayOrder, questionId, null);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/replace",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Replaces existing question with new question for given block", tags = POSTMAN_ONLY)
    public void replaceQuestion(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                                @RequestParam Integer oldQuestionId, @RequestParam Integer newQuestionId) {
        templateService.replaceQuestion(templateId, displayOrder, oldQuestionId, newQuestionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/{questionId}/helpText",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Updates help text for given question", tags = POSTMAN_ONLY)
    public void updateQuestionHelpText(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                                       @PathVariable Integer questionId, @RequestBody(required = false) String helpText) {
        templateService.updateQuestionHelpText(templateId, displayOrder, questionId, helpText);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/templates/availableBlocks", method = RequestMethod.GET)
    public List<String> getAvailableBlocks() {
        List<String> blockNames = new LinkedList<>();
        for (ProjectBlockType block : ProjectBlockType.values()) {
            blockNames.add(block.name());
        }
        return blockNames;
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/projectBlockTypes", method = RequestMethod.GET)
    public List<ProjectBlockTypeModel> getProjectBlockTypes() {
        List<ProjectBlockTypeModel> projectBlockTypes = new ArrayList<>();
        for (ProjectBlockType pbt : ProjectBlockType.values()) {
            projectBlockTypes.add(new ProjectBlockTypeModel(
                    pbt,
                    pbt.getDefaultName(),
                    pbt.getTemplateBlockClass().getSimpleName()
            ));
        }
        return projectBlockTypes;
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/projectInternalBlockTypes", method = RequestMethod.GET)
    public List<InternalBlockTypeModel> getProjectInternalBlockTypes() {
        List<InternalBlockTypeModel> internalBlockTypes = new ArrayList<>();
        for (InternalBlockType pbt : InternalBlockType.values()) {
            internalBlockTypes.add(new InternalBlockTypeModel(
                    pbt,
                    pbt.getDefaultName(),
                    pbt.getTemplateBlockClass().getSimpleName()
            ));
        }
        return internalBlockTypes;
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/contract", method = RequestMethod.PUT)
    public void setContract(@PathVariable Integer templateId, @RequestBody Integer contractId) {
        templateService.setTemplateContract(templateId, contractId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/contract", method = RequestMethod.DELETE)
    public void setContract(@PathVariable Integer templateId) {
        templateService.removeTemplateContract(templateId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/detailsTemplate", method = RequestMethod.PUT)
    @ApiOperation(value = "update the requirements on the project details block",
            notes = "update the requirements on the project details block", tags = POSTMAN_ONLY)
    public void updateDetailsTemplate(@PathVariable Integer templateId, @RequestBody DetailsTemplate details) {
        templateService.updateDetailsTemplate(templateId, details);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/detailsTemplate/consortiumRequirements", method = RequestMethod.PUT)
    @ApiOperation(value = "update the consortium requirements on the project details block for two consortium fields",
            notes = "Consortium fields only show on consortium projects and requirements applies where field will be displayed", tags = POSTMAN_ONLY)
    public void updateDetailsTemplateConsortiumRequirements(@PathVariable Integer templateId, @RequestBody DetailsTemplate details) {
        templateService.updateDetailsTemplateConsortiumRequirements(templateId, details);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/projectDetailsTemplate", method = RequestMethod.PUT)
    @ApiOperation(value = "update the configurable fields on the project details block",
            notes = "update the configurable fields on the project details block", tags = POSTMAN_ONLY)
    public void updateProjectDetailsTemplate(@PathVariable Integer templateId, @RequestBody ProjectDetailsTemplateBlock details) {
        templateService.updateProjectDetailsTemplate(templateId, details);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/associatedProjectsEnabled", method = RequestMethod.PUT)
    @ApiOperation(value = "resets the associated projects flag", tags = POSTMAN_ONLY)
    public void updateAssociatedProjectsEnabled(@PathVariable Integer templateId, @RequestBody String enabled) {
        templateService.updateAssociatedProjectsEnabled(templateId, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/milestoneDescriptionHintText", method = RequestMethod.PUT)
    @ApiOperation(value = "updates milestone description hint text", tags = POSTMAN_ONLY)
    public void updateMilestoneDescriptionHintText(@PathVariable Integer templateId, @RequestBody String text) {
        templateService.updateMilestoneDescriptionHintText(templateId, text);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/milestoneType", method = RequestMethod.PUT)
    @ApiOperation(value = "updates milestone monetary flag", tags = POSTMAN_ONLY)
    public void updateMilestoneType(@PathVariable Integer templateId, @RequestBody String type) {
        templateService.updateMilestoneType(templateId, Template.MilestoneType.valueOf(type));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/fundingSpendType/", method = RequestMethod.PUT)
    @ApiOperation(value = "update funding flags for budget (funding) block", tags = POSTMAN_ONLY)
    public void updateFundingType(@PathVariable Integer templateId,
                                  @RequestBody FundingSpendingTypeFlags flags,
                                  @RequestParam(defaultValue = "false") boolean deleteBlockData) {
        templateService.updateFundingType(templateId, flags, deleteBlockData);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/startOnSiteRestrictionText", method = RequestMethod.PUT)
    @ApiOperation(value = "update start on site restriction text", tags = POSTMAN_ONLY)
    public void updateStartOnSiteRestrictionText(@PathVariable Integer templateId, @RequestBody String text) {
        templateService.updateStartOnSiteRestrictionText(templateId, text);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/grantTotalText",
            method = RequestMethod.PUT)
    @ApiOperation(value = "update grant total text", tags = POSTMAN_ONLY)
    public void updateGrantTotalText(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                                     @RequestBody String text) {
        templateService.updateGrantTotalText(templateId, displayOrder, text);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneDescriptionEnabled",
            method = RequestMethod.PUT)
    @ApiOperation(value = "update if milestone description is enabled", tags = POSTMAN_ONLY)
    public void updateMilestoneDescriptionEnabled(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                                                  @RequestBody String enabled) {
        templateService.updateMilestoneDescriptionEnabled(templateId, displayOrder, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/milestones/{milestoneExtId}/makeMonetary",
            method = RequestMethod.PUT)
    @ApiOperation(value = "change non-monetary milestone to monetary on given template", tags = POSTMAN_ONLY)
    public void updateMilestoneToMonetary(@PathVariable Integer templateId, @PathVariable Integer milestoneExtId) {
        templateService.makeMonetary(templateId, milestoneExtId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneEvidenceLimit",
            method = RequestMethod.PUT)
    @ApiOperation(value = "change limit of milestone evidence on given template", tags = POSTMAN_ONLY)
    public void updateMilestoneEvidenceRequirements(@PathVariable Integer templateId,
                                                    @PathVariable Integer displayOrder,
                                                    @RequestBody Integer newLimit) {
        templateService.updateMilestoneAllowableEvidenceDocuments(templateId, displayOrder, newLimit);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneEvidenceApplicability",
            method = RequestMethod.PUT)
    @ApiOperation(value = "change if milestone evidence applicability on given template", tags = POSTMAN_ONLY)
    public void updateMilestoneEvidenceRequirements(@PathVariable Integer templateId,
                                                    @PathVariable Integer displayOrder,
                                                    @RequestBody String applicability) {
        templateService.updateMilestoneAllowableEvidenceDocuments(templateId, displayOrder,
                MilestonesTemplateBlock.EvidenceApplicability.valueOf(applicability));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneStatusShown",
            method = RequestMethod.PUT)
    @ApiOperation(value = "change if milestone status should show on given template", tags = POSTMAN_ONLY)
    public void updateShowMilestoneStatus(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                                          @RequestBody String shown) {
        templateService.updateShowMilestoneStatus(templateId, displayOrder, Boolean.parseBoolean(shown));
    }

    @Secured(OPS_ADMIN)
    @DeleteMapping(value = "/templates/{templateId}/blocks/{displayOrder}/processingRoutes/{processingRouteId}/milestones/{milestoneId}")
    @ApiOperation(value = "delete a milestone from a template and related projects processing route", tags = POSTMAN_ONLY)
    public void deleteMilestone(@PathVariable Integer templateId,
                                @PathVariable Integer displayOrder,
                                @PathVariable Integer processingRouteId,
                                @PathVariable Integer milestoneId,
                                @RequestParam(required = false, defaultValue = "false") boolean updateActiveProjects) {
        templateService.deleteMilestone(templateId, displayOrder, processingRouteId, milestoneId, updateActiveProjects);
    }

    @Secured(OPS_ADMIN)
    @PostMapping(value = "/templates/{templateId}/blocks/{displayOrder}/processingRoutes/{processingRouteId}/milestones")
    @ApiOperation(value = "add a milestone to a template and related projects processing route", tags = POSTMAN_ONLY)
    public void addMilestone(@PathVariable Integer templateId,
                             @PathVariable Integer displayOrder,
                             @PathVariable Integer processingRouteId,
                             @RequestBody MilestoneTemplate milestoneTemplate,
                             @RequestParam(required = false, defaultValue = "false") boolean updateActiveProjects) {
        templateService.addMilestone(templateId, displayOrder, processingRouteId, milestoneTemplate, updateActiveProjects);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{blockDisplayOrder}/blockDisplayName",
            method = RequestMethod.PUT)
    @ApiOperation(value = "update block display name", tags = POSTMAN_ONLY)
    public void updateBlockDisplayName(@PathVariable Integer templateId,
                                       @PathVariable Integer blockDisplayOrder,
                                       @RequestParam String blockType,
                                       @RequestParam String oldName,
                                       @RequestParam String newName,
                                       @RequestParam(required = false, defaultValue = "false")
                                                   boolean isInternalBlock) {
        templateService.updateBlockDisplayName(templateId, blockDisplayOrder, blockType, oldName,
                newName, isInternalBlock);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/replaceTenureType", method = RequestMethod.PUT)
    @ApiOperation(value = "replace given tenute type for specific template", tags = POSTMAN_ONLY)
    public void replaceTenureType(@PathVariable Integer templateId, @RequestParam Integer oldTenureTypeId,
                                  @RequestParam Integer newTenureTypeId,
                                  @RequestParam(required = false, defaultValue = "false") boolean updateActiveProjects) {
        templateService.replaceTenureType(templateId, oldTenureTypeId, newTenureTypeId, updateActiveProjects);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/replaceMarketType", method = RequestMethod.PUT)
    @ApiOperation(value = "replace given market type for tenure type for specific template", tags = POSTMAN_ONLY)
    public void replaceMarketTypeForTenureType(@RequestParam Integer tenureTypeId,
                                               @RequestParam Integer oldMarketType,
                                               @RequestParam Integer newMarketType) {
        templateService.replaceMarketType(tenureTypeId, oldMarketType, newMarketType);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/findUsagesOfTenureType", method = RequestMethod.GET)
    public Set<Integer> findUsagesOfTenureType(@RequestParam Integer tenureTypeId) {
        return templateService.getTemplatesUsingTenureType(tenureTypeId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/stateModel", method = RequestMethod.PUT)
    @ApiOperation(value = "change state model for template", tags = POSTMAN_ONLY)
    public void updateStateModel(@PathVariable Integer templateId,
                                 @RequestBody UpdateStateModelRequest updateStateModelRequest) {
        templateService.updateStateModel(templateId, updateStateModelRequest);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/funding/flags", method = RequestMethod.PUT)
    @ApiOperation(value = "update flags for funding block", tags = POSTMAN_ONLY)
    public void updateFundingBlockFlags(@PathVariable Integer templateId, @RequestBody FundingBlockFlags flags) {
        templateService.updateFundingBlockFlags(templateId, flags);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/outputs/baselined", method = RequestMethod.PUT)
    @ApiOperation(value = "change if baselines for this outputs block are supported", tags = POSTMAN_ONLY)
    public void updateBaselineForOutputsBlock(@PathVariable Integer templateId, @RequestBody String showBaseline) {
        templateService.updateBaselineForOutputsBlock(templateId, Boolean.parseBoolean(showBaseline));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/outputs/assumptions", method = RequestMethod.PUT)
    @ApiOperation(value = "change if assumptions are shown for this outputs block", tags = POSTMAN_ONLY)
    public void updateAssumptionsForOutputsBlock(@PathVariable Integer templateId, @RequestBody String showBaseline) {
        templateService.updateAssumptionsForOutputsBlock(templateId, Boolean.parseBoolean(showBaseline));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/migrate",
            method = RequestMethod.PUT)
    @ApiOperation(value = "Migrate questions progress updates block to new progress updates block",
            notes = "Migrate questions progress updates block to new progress updates block", tags = POSTMAN_ONLY)
    public void migrateQuestionProgressUpdateBlock(
            @PathVariable Integer templateId,
            @PathVariable int displayOrder,
            @RequestParam(required = false, defaultValue = "532") Integer questionId) {
        templateService.migrateQuestionProgressUpdateBlock(templateId, displayOrder, questionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/migrateContactDetails", method = RequestMethod.PUT)
    @ApiOperation(value = "Migrate contact details to questions block",
            notes = "Migrate contact details to questions block", tags = POSTMAN_ONLY)
    public void migrateContactDetails(@PathVariable Integer templateId,
                                      @RequestParam Integer questionsBlockDisplayOrder,
                                      @RequestParam Integer contactNameQuestionId,
                                      @RequestParam Integer contactEmailQuestionId) {
        templateService.migrateContactDetails(templateId, questionsBlockDisplayOrder,
                contactNameQuestionId, contactEmailQuestionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalRisksBlock/riskAdjustedFiguresFlag",
            method = RequestMethod.PUT)
    @ApiOperation(value = "enable the risk adjusted figures for risk block", tags = POSTMAN_ONLY)
    public void enableRiskAdjustedFiguresForRiskBlock(@PathVariable Integer templateId, @RequestBody String enabled) {
        templateService.updateRiskAdjustedFiguresFlag(templateId, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalRisksBlock/ratingList", method = RequestMethod.POST)
    @ApiOperation(value = "add the risk rating to the block ", tags = POSTMAN_ONLY)
    public RiskRating createRiskRating(@PathVariable Integer templateId, @RequestBody RiskRating riskRating) {
        return templateService.addRiskRating(templateId, riskRating);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalRisksBlock/ratingList/{riskRatingId}",
            method = RequestMethod.PUT)
    @ApiOperation(value = "update the risk rating to the block ", tags = POSTMAN_ONLY)
    public void updateRiskRating(@PathVariable Integer templateId, @PathVariable Integer riskRatingId,
                                 @RequestBody RiskRating riskRating) {
        templateService.updateRiskRating(templateId, riskRatingId, riskRating);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalRisksBlock/riskRating/{riskRatingId}",
            method = RequestMethod.DELETE)
    @ApiOperation(value = "delete the risk rating to the block ", tags = POSTMAN_ONLY)
    public void deleteRiskRating(@PathVariable Integer templateId, @PathVariable Integer riskRatingId) {
        templateService.deleteRiskRating(templateId, riskRatingId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/templates/{templateId}/deliverableTypes", method = RequestMethod.GET)
    @ApiOperation(value = "get a list of available deliverable types ")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Map<String, String> getAvailableDeliverableTypes(@PathVariable Integer templateId) {
        return templateService.getAvailableDeliverableTypes(templateId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/paymentSources/",
            method = RequestMethod.PUT)
    @ApiOperation(value = "replace payment source for given template block", tags = POSTMAN_ONLY)
    public void replacePaymentSources(@PathVariable Integer templateId, @PathVariable int displayOrder,
                                      @RequestBody String paymentSources) {
        templateService.replacePaymentSources(templateId, displayOrder, paymentSources);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/funding/monetaryValueScale/{monetaryValueScale}",
            method = RequestMethod.PUT)
    @ApiOperation(value = "update monetary value scale for funding block", tags = POSTMAN_ONLY)
    public void updateFundingBlockMonetaryValueScale(@PathVariable Integer templateId,
                                                     @PathVariable Integer monetaryValueScale) {
        templateService.updateFundingBlockMonetaryValueScale(templateId, monetaryValueScale);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/processingRoutes",
            method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void createProcessingRoute(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
                                      @RequestBody ProcessingRoute processingRoute) {
        templateService.createProcessingRoute(templateId, displayOrder, processingRoute);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/performMigration", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "block migration for moving data between specific block types", tags = POSTMAN_ONLY)
    public MigrationResponse performBlockMigration(@RequestBody MigrationRequest migrationRequest) {
        return blockMigrationService.migrateBlockData(migrationRequest);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/template/{templateId}/disassociateBlock/{blockId}", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "remove block from template", tags = POSTMAN_ONLY)
    public void disassociateBlock(@PathVariable Integer templateId, @PathVariable Integer blockId) {
        templateService.disassociateBlock(templateId, blockId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/template/{templateId}/reattachDisassociateBlock/{blockId}", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "reattach disassociated block from template", tags = POSTMAN_ONLY)
    public void reattachBlock(@PathVariable Integer templateId, @PathVariable Integer blockId) {
        templateService.reattachDisassociateBlock(templateId, blockId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/template/{templateId}/reattachDisassociateInternalBlock/{blockId}", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "reattach disassociated block from template", tags = POSTMAN_ONLY)
    public void reattachInternalBlock(@PathVariable Integer templateId, @PathVariable Integer blockId) {
        templateService.reattachDisassociateInternalBlock(templateId, blockId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/fundingVariationsEnabled",
            method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "FundingClaimsTemplateBlock set if variations are allowed", tags = POSTMAN_ONLY)
    public void updateFundingVariationsEnabled(@PathVariable Integer templateId, @PathVariable int displayOrder,
                                               @RequestBody String enabled) {
        templateService.updateFundingVariationsEnabled(templateId, displayOrder, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/canClaimActivity",
            method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "update if can claim activity on FundingTemplateBlock", tags = POSTMAN_ONLY)
    public void updateCanClaimActivity(@PathVariable Integer templateId, @PathVariable int displayOrder,
                                       @RequestBody String enabled) {
        templateService.updateCanClaimActivity(templateId, displayOrder, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalQuestionsBlock/{displayOrder}/showComments",
            method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "update if commments are enabled on internal questions block", tags = POSTMAN_ONLY)
    public void updateShowComments(@PathVariable Integer templateId, @PathVariable int displayOrder,
                                   @RequestBody String enabled) {
        templateService.updateShowComments(templateId, displayOrder, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/projectSubmissionReminder", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "update reminder for project submission", tags = POSTMAN_ONLY)
    public void updateProjectSubmissionReminder(@PathVariable Integer templateId, @RequestBody String enabled) {
        templateService.updateProjectSubmissionReminder(templateId, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blockType/{blockType}/showOtherAffordableQuestion",
            method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "show other funding question on grant blocks", tags = POSTMAN_ONLY)
    public void updateShowOtherAffordableQuestion(@PathVariable Integer templateId,
                                                  @PathVariable ProjectBlockType blockType,
                                                  @RequestBody String enable) {
        templateService.updateShowOtherAffordableQuestion(templateId, blockType, Boolean.parseBoolean(enable));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/fundingClaimsBlock/{displayOrder}/period/{periodId}/text",
            method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    @ApiOperation(value = "update funding claim period text ", tags = POSTMAN_ONLY)
    public void updateFundingClaimPeriod(@PathVariable Integer templateId, @PathVariable int displayOrder,
                                         @PathVariable Integer periodId, @RequestBody String periodText) {
        templateService.updateFundingClaimPeriod(templateId, displayOrder, periodId, periodText);
    }

    @Secured(OPS_ADMIN)
    @PostMapping(value = "/templates/{templateId}/fundingClaimsBlock/{blockDisplayOrder}/fundingClaimCategories")
    @ApiOperation(value = "add funding claim category order ", tags = POSTMAN_ONLY)
    public void addFundingClaimCategory(@PathVariable Integer templateId,
                                        @PathVariable Integer blockDisplayOrder,
                                        @RequestBody FundingClaimCategory category) {
        templateService.addFundingClaimCategory(templateId, blockDisplayOrder, category);
    }

    @Secured(OPS_ADMIN)
    @PostMapping(value = "/templates/{templateId}/fundingClaimsBlock/{blockDisplayOrder}/fundingClaimCategory/{categoryId}/subcategory")
    @ApiOperation(value = "add funding claim subcategory", tags = POSTMAN_ONLY)
    public void addFundingClaimSubcategory(@PathVariable Integer templateId,
            @PathVariable Integer blockDisplayOrder,
            @PathVariable Integer categoryId,
            @RequestBody FundingClaimCategory subcategory) {
        templateService.addFundingClaimSubCategory(templateId, blockDisplayOrder, categoryId, subcategory);
    }


    @Secured(OPS_ADMIN)
    @PutMapping(value = "/templates/{templateId}/fundingClaimsBlock/{blockDisplayOrder}/fundingClaimCategories/{categoryId}")
    @ApiOperation(value = "update funding claim disaply order ", tags = POSTMAN_ONLY)
    public void updateFundingClaimCategoryDisplayOrder(@PathVariable Integer templateId,
                                                       @PathVariable Integer blockDisplayOrder,
                                                       @PathVariable Integer categoryId,
                                                       @RequestParam Integer newDisplayOrder) {
        templateService.updateFundingClaimCategoryDisplayOrder(templateId, blockDisplayOrder, categoryId, newDisplayOrder);
    }

    @Secured(OPS_ADMIN)
    @PostMapping(value = "/templates/{templateId}/learningGrantBlock/{blockDisplayOrder}/learningGrantAllocationType")
    @ApiOperation(value = "add learning grant allocation to learning grant block", tags = POSTMAN_ONLY)
    public void addLearningGrantAllocationType(@PathVariable Integer templateId,
                                               @PathVariable Integer blockDisplayOrder,
                                               @RequestParam AllocationType allocationType) {
        templateService.addLearningGrantAllocationType(templateId, blockDisplayOrder, allocationType);
    }

    @Secured(OPS_ADMIN)
    @GetMapping("/blockUsage")
    public List<BlockUsage> getBlockUsage(@RequestParam(required = false) ProjectBlockType projectBlockType,
                                          @RequestParam(required = false) InternalBlockType internalBlockType) {
        return templateService.getBlockUsage(projectBlockType, internalBlockType);
    }


    @Secured(OPS_ADMIN)
    @PutMapping(value = "/templates/{templateId}/affordableHomes/{blockDisplayOrder}/ofWhich")
    @ApiOperation(value = "update available of which categories for this template", tags = POSTMAN_ONLY)
    public void specifyOfWhichCategories(@PathVariable Integer templateId,
                                         @PathVariable Integer blockDisplayOrder,
                                         @RequestBody String[] categories) {
        List<AffordableHomesOfWhichCategory> items = Arrays.stream(categories)
                .map(s -> AffordableHomesOfWhichCategory.valueOf(s))
                .collect(Collectors.toList());
        templateService.specifyOfWhichCategories(templateId, blockDisplayOrder, items);
    }

    @Secured(OPS_ADMIN)
    @PutMapping(value = "/templates/{templateId}/affordableHomes/{blockDisplayOrder}/completionOnlyAvailable")
    @ApiOperation(value = "update completionOnlyAvailable for this template", tags = POSTMAN_ONLY)
    public void updateCompletionOnlyAvailable(@PathVariable Integer templateId,
            @PathVariable Integer blockDisplayOrder,
            @RequestBody String completionOnlyAvailable) {
        templateService.updateCompletionOnlyAvailability(templateId, blockDisplayOrder, Boolean.parseBoolean(completionOnlyAvailable));
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/{templateId}/userDefinedOutput/config", method = RequestMethod.PUT)
    public void updateUserDefinedOutputTemplateBlock(@PathVariable Integer templateId,
                                                     @RequestBody UserDefinedOutputTemplateBlock block) {
        templateService.updateUserDefinedOutputTemplateBlock(templateId, block);
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/{templateId}/projectElements/config", method = RequestMethod.PUT)
    public void updateProjectElementsTemplateBlock(@PathVariable Integer templateId,
                                                     @RequestBody ProjectElementsTemplateBlock block) {
        templateService.updateProjectElementsTemplateBlock(templateId, block);
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/{templateId}/projectObjectives/config", method = RequestMethod.PUT)
    public void updateProjectObjectivesTemplateBlock(@PathVariable Integer templateId,
                                                     @RequestBody ProjectObjectivesTemplateBlock block) {
        templateService.updateProjectObjectivesTemplateBlock(templateId, block);
    }

    @PermissionRequired(TEMP_MANAGE)
    @RequestMapping(value = "/templates/{templateId}/otherFunding/config", method = RequestMethod.PUT)
    public void updateOtherFundingTemplateBlock(@PathVariable Integer templateId,
                                                     @RequestBody OtherFundingTemplateBlock block) {
        templateService.updateOtherFundingTemplateBlock(templateId, block);
    }
}
