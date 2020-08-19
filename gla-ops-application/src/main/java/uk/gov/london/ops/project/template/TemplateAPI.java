/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;
import static uk.gov.london.ops.permission.PermissionType.TEMP;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.common.error.ApiError;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.programme.ProgrammeService;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.ProjectSummary;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.migration.BlockMigrationService;
import uk.gov.london.ops.project.migration.MigrationRequest;
import uk.gov.london.ops.project.migration.MigrationResponse;
import uk.gov.london.ops.project.template.domain.DetailsTemplate;
import uk.gov.london.ops.project.template.domain.InternalTemplateBlock;
import uk.gov.london.ops.project.template.domain.MilestonesTemplateBlock;
import uk.gov.london.ops.project.template.domain.ProcessingRoute;
import uk.gov.london.ops.project.template.domain.QuestionsBlockSection;
import uk.gov.london.ops.project.template.domain.RiskRating;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateQuestion;
import uk.gov.london.ops.project.template.domain.TemplateSummary;
import uk.gov.london.ops.user.UserService;

@RestController
@RequestMapping("/api/v1")
@Api("managing project templates")
public class TemplateAPI {

    private final TemplateService templateService;
    private final UserService userService;
    private final Environment environment;
    private final ProjectService projectService;
    private final ProgrammeService programmeService;
    private final BlockMigrationService blockMigrationService;

    TemplateAPI(final TemplateService templateService,
            final ProjectService projectService,
            final UserService userService,
            final ProgrammeService programmeService,
            final Environment environment,
            final BlockMigrationService blockMigrationService) {
        this.templateService = templateService;
        this.projectService = projectService;
        this.userService = userService;
        this.programmeService = programmeService;
        this.environment = environment;
        this.blockMigrationService = blockMigrationService;
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/templates/summary", method = RequestMethod.GET)
    public Page<TemplateSummary> getAllTemplatesSummary(@RequestParam(required = false, defaultValue = "") String templateText,
            @RequestParam(required = false, defaultValue = "") String programmeText,
            @RequestParam(required = false) List<Template.TemplateStatus> selectedTemplateStatuses,
            Pageable pageable) {
        return templateService.getTemplateSummaries(templateText, programmeText, selectedTemplateStatuses, pageable);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_REGISTRATION_APPROVER, GLA_FINANCE, GLA_READ_ONLY,
            ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/templates", method = RequestMethod.GET)
    public List<Template> getAll(@RequestParam(value = "programmeId", required = false) Integer programmeId,
            @RequestParam(value = "includeProgrammes", required = false, defaultValue = "false") Boolean showProgrammes) {
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

        if (showProgrammes) {
            for (Template template : templateList) {
                template.setProgrammes(programmeService.getProgrammesByTemplate(template.getId()));
            }
        }
        return templateList.stream().sorted(Comparator.comparingInt(Template::getId)).collect(Collectors.toList());
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/templates/{id}/json", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    String getTemplateJson(@PathVariable Integer id) throws IOException {
        return templateService.getTemplateJson(id, false);
    }

    /**
     * @deprecated TODO : remove
     */
    @Secured(OPS_ADMIN)
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

        template.setCreatedBy(userService.currentUser().getUsername());
        template.setCreatedOn(environment.now());

        return templateService.save(template).getId();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer create(@RequestBody String templateJson) throws IOException {
        return templateService.create(templateJson).getId();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/draft", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer updateDraftTemplate(@RequestBody String templateJson) {
        return templateService.createDraft(templateJson);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/draft/{id}", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateDraftTemplate(@PathVariable Integer id, @RequestBody String templateJson) {
        templateService.saveDraft(id, templateJson);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/clone/{id}", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer cloneTemplate(@PathVariable Integer id, @Valid @RequestBody String newName) {
        Template template = templateService.find(id);
        Template cloned = templateService.cloneTemplate(template, newName);
        return cloned.getId();
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

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{id}/projects", method = RequestMethod.GET)
    public List<ProjectSummary> getTemplateProjects(@PathVariable Integer id) {
        List<ProjectSummary> templateProjects = new LinkedList<>();
        for (ProjectSummary project : projectService.findAll(null, null, null, null, null, null, false, null).getContent()) {
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
    public void addBlock(@PathVariable("templateId") int templateId, @RequestBody TemplateBlock block)
            throws InterruptedException {

        // validation done here , as async method call
        if (!templateService.getAllowedBlockTypesToAdd().contains(block.getBlock())) {
            throw new ValidationException("Not allowed block type " + block.getBlock().name());
        }

        Template template = templateService.find(templateId);
        if (template == null) {
            throw new ValidationException("Not found template with id " + templateId);
        }

        if (template.getBlocksEnabled().stream().anyMatch(b -> b.getDisplayOrder().equals(block.getDisplayOrder()))) {
            throw new ValidationException("Display order already used in template " + templateId);
        } else {
            templateService.addBlock(templateId, block);
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
    @RequestMapping(value = "/templates/{templateId}/internalBlocks/{displayOrder}/questions", method = RequestMethod.POST)
    public void addQuestionToInternalBlock(@PathVariable Integer templateId,
                                           @PathVariable int displayOrder,
                                           @Valid @RequestBody TemplateQuestion templateQuestion, BindingResult bindingResult) {
        verifyBinding("Invalid question creation request!", bindingResult);
        templateService.addQuestionToInternalBlock(templateId, displayOrder, templateQuestion);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/section", method = RequestMethod.POST)
    public void addSection(@PathVariable Integer templateId, @PathVariable int displayOrder,
            @Valid @RequestBody QuestionsBlockSection section, BindingResult bindingResult) {
        verifyBinding("Invalid section creation request!", bindingResult);
        templateService.addSection(templateId, displayOrder, section);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/{questionId}/requirement",
            method = RequestMethod.PUT)
    public void updateQuestionRequirement(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @PathVariable Integer questionId, @RequestBody String requirement) {
        templateService.updateQuestionRequirement(templateId, displayOrder, questionId, Requirement.valueOf(requirement));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{blockDisplayOrder}/questions/{questionId}/displayOrder",
            method = RequestMethod.PUT)
    public void updateQuestionDisplayOrder(@PathVariable Integer templateId, @PathVariable Integer blockDisplayOrder,
            @PathVariable Integer questionId, @RequestBody double newDisplayOrder) {

        templateService.updateQuestionDisplayOrder(templateId, blockDisplayOrder, questionId, newDisplayOrder);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{blockDisplayOrder}/questions/{questionId}/sections",
            method = RequestMethod.PUT)
    public void addQuestionToSection(@PathVariable Integer templateId, @PathVariable Integer blockDisplayOrder,
            @PathVariable Integer questionId, @RequestBody Integer sectionId) {

        templateService.addQuestionToSection(templateId, blockDisplayOrder, questionId, sectionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{blockDisplayOrder}/questions/{questionId}/removeFromSection",
            method = RequestMethod.PUT)
    public void removeQuestionFromSection(@PathVariable Integer templateId, @PathVariable Integer blockDisplayOrder,
            @PathVariable Integer questionId) {

        templateService.removeQuestionFromSection(templateId, blockDisplayOrder, questionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/{questionId}",
            method = RequestMethod.DELETE)
    public void removeQuestion(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @PathVariable Integer questionId) {
        templateService.removeQuestion(templateId, displayOrder, questionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/replace", method = RequestMethod.PUT)
    public void replaceQuestion(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @RequestParam Integer oldQuestionId, @RequestParam Integer newQuestionId) {
        templateService.replaceQuestion(templateId, displayOrder, oldQuestionId, newQuestionId);
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

    @Secured({OPS_ADMIN, TECH_ADMIN})
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
            notes = "update the requirements on the project details block")
    public void updateDetailsTemplate(@PathVariable Integer templateId, @RequestBody DetailsTemplate details) {
        templateService.updateDetailsTemplate(templateId, details);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/associatedProjectsEnabled", method = RequestMethod.PUT)
    public void updateAssociatedProjectsEnabled(@PathVariable Integer templateId, @RequestBody String enabled) {
        templateService.updateAssociatedProjectsEnabled(templateId, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/milestoneDescriptionHintText", method = RequestMethod.PUT)
    public void updateMilestoneDescriptionHintText(@PathVariable Integer templateId, @RequestBody String text) {
        templateService.updateMilestoneDescriptionHintText(templateId, text);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/milestoneType", method = RequestMethod.PUT)
    public void updateMilestoneType(@PathVariable Integer templateId, @RequestBody String type) {
        templateService.updateMilestoneType(templateId, Template.MilestoneType.valueOf(type));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/fundingSpendType/", method = RequestMethod.PUT)
    public void updateFundingType(@PathVariable Integer templateId,
            @RequestBody FundingSpendingTypeFlags flags,
            @RequestParam(defaultValue = "false") boolean deleteBlockData) {
        templateService.updateFundingType(templateId, flags, deleteBlockData);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/startOnSiteRestrictionText", method = RequestMethod.PUT)
    public void updateStartOnSiteRestrictionText(@PathVariable Integer templateId, @RequestBody String text) {
        templateService.updateStartOnSiteRestrictionText(templateId, text);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/grantTotalText", method = RequestMethod.PUT)
    public void updateGrantTotalText(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @RequestBody String text) {
        templateService.updateGrantTotalText(templateId, displayOrder, text);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneDescriptionEnabled", method = RequestMethod.PUT)
    public void updateMilestoneDescriptionEnabled(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @RequestBody String enabled) {
        templateService.updateMilestoneDescriptionEnabled(templateId, displayOrder, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/milestones/{milestoneExtId}/makeMonetary", method = RequestMethod.PUT)
    public void updateMilestoneToMonetary(@PathVariable Integer templateId, @PathVariable Integer milestoneExtId) {
        templateService.makeMonetary(templateId, milestoneExtId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneEvidenceLimit", method = RequestMethod.PUT)
    public void updateMilestoneEvidenceRequirements(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @RequestBody Integer newLimit) {
        templateService.updateMilestoneAllowableEvidenceDocuments(templateId, displayOrder, newLimit);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneEvidenceApplicability",
            method = RequestMethod.PUT)
    public void updateMilestoneEvidenceRequirements(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @RequestBody String applicability) {
        templateService.updateMilestoneAllowableEvidenceDocuments(templateId, displayOrder,
                MilestonesTemplateBlock.EvidenceApplicability.valueOf(applicability));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/milestoneStatusShown", method = RequestMethod.PUT)
    public void updateShowMilestoneStatus(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @RequestBody String shown) {
        templateService.updateShowMilestoneStatus(templateId, displayOrder, Boolean.parseBoolean(shown));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/processingRoutes/{processingRouteId}/milestones/{milestoneId}/naSelectable",
            method = RequestMethod.PUT)
    public void updateMilestoneNaSelectable(@PathVariable Integer templateId,
            @PathVariable Integer displayOrder,
            @PathVariable Integer processingRouteId,
            @PathVariable Integer milestoneId,
            @RequestBody String selectable) {
        templateService.updateMilestoneNaSelectable(templateId, displayOrder, processingRouteId, milestoneId,
                Boolean.parseBoolean(selectable));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{blockDisplayOrder}/blockDisplayName", method = RequestMethod.PUT)
    public void updateBlockDisplayName(@PathVariable Integer templateId,
            @PathVariable Integer blockDisplayOrder,
            @RequestParam ProjectBlockType blockType,
            @RequestParam String oldName,
            @RequestParam String newName) {
        templateService.updateBlockDisplayName(templateId, blockDisplayOrder, blockType, oldName, newName);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/replaceTenureType", method = RequestMethod.PUT)
    public void replaceTenureType(@PathVariable Integer templateId, @RequestParam Integer oldTenureTypeId,
            @RequestParam Integer newTenureTypeId) {
        templateService.replaceTenureType(templateId, oldTenureTypeId, newTenureTypeId);
    }

    /**
     * @deprecated use updateFundingBlockFlags instead
     */
    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/enforceFundingBalance", method = RequestMethod.PUT)
    @Deprecated
    @ApiOperation(value = "Update Enforce-Funding-Balance on funding block", notes = "Deprecated, do not use")
    public void updateEnforceFundingBalance(@PathVariable Integer templateId, @RequestBody String enabled) {
        templateService.updateEnforceFundingBalance(templateId, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/stateModel", method = RequestMethod.PUT)
    public void updateStateModel(@PathVariable Integer templateId, @RequestBody UpdateStateModelRequest updateStateModelRequest) {
        templateService.updateStateModel(templateId, updateStateModelRequest);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/funding/flags", method = RequestMethod.PUT)
    public void updateFundingBlockFlags(@PathVariable Integer templateId, @RequestBody FundingBlockFlags flags) {
        templateService.updateFundingBlockFlags(templateId, flags);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/outputs/baselined", method = RequestMethod.PUT)
    public void updateBaselineForOutputsBlock(@PathVariable Integer templateId, @RequestBody String showBaseline) {
        templateService.updateBaselineForOutputsBlock(templateId, Boolean.parseBoolean(showBaseline));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/outputs/assumptions", method = RequestMethod.PUT)
    public void updateAssumptionsForOutputsBlock(@PathVariable Integer templateId, @RequestBody String showBaseline) {
        templateService.updateAssumptionsForOutputsBlock(templateId, Boolean.parseBoolean(showBaseline));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/questions/migrate", method = RequestMethod.PUT)
    @ApiOperation(value = "Migrate questions progress updates block to new progress updates block",
            notes = "Migrate questions progress updates block to new progress updates block")
    public void migrateQuestionProgressUpdateBlock(@PathVariable Integer templateId, @PathVariable int displayOrder,
            @RequestParam(required = false, defaultValue = "532") Integer questionId) {
        templateService.migrateQuestionProgressUpdateBlock(templateId, displayOrder, questionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/migrateContactDetails", method = RequestMethod.PUT)
    @ApiOperation(value = "Migrate contact details to questions block", notes = "Migrate contact details to questions block")
    public void migrateContactDetails(@PathVariable Integer templateId,
            @RequestParam Integer questionsBlockDisplayOrder,
            @RequestParam Integer contactNameQuestionId,
            @RequestParam Integer contactEmailQuestionId) {
        templateService
                .migrateContactDetails(templateId, questionsBlockDisplayOrder, contactNameQuestionId, contactEmailQuestionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/infoMessage", method = RequestMethod.PUT)
    public void updateInfoMessage(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @RequestBody(required = false) String infoMessage) {
        templateService.updateInfoMessage(templateId, displayOrder, infoMessage);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalRisksBlock/riskAdjustedFiguresFlag", method = RequestMethod.PUT)
    public void enableRiskAdjustedFiguresForRiskBlock(@PathVariable Integer templateId, @RequestBody String enabled) {
        templateService.updateRiskAdjustedFiguresFlag(templateId, Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalRisksBlock/ratingList", method = RequestMethod.POST)
    public RiskRating createRiskRating(@PathVariable Integer templateId, @RequestBody RiskRating riskRating) {
        return templateService.addRiskRating(templateId, riskRating);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalRisksBlock/ratingList/{riskRatingId}", method = RequestMethod.PUT)
    public void updateRiskRating(@PathVariable Integer templateId, @PathVariable Integer riskRatingId,
            @RequestBody RiskRating riskRating) {
        templateService.updateRiskRating(templateId, riskRatingId, riskRating);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/internalRisksBlock/riskRating/{riskRatingId}", method = RequestMethod.DELETE)
    public void deleteRiskRating(@PathVariable Integer templateId, @PathVariable Integer riskRatingId) {
        templateService.deleteRiskRating(templateId, riskRatingId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/templates/{templateId}/deliverableTypes", method = RequestMethod.GET)
    @ApiOperation(value = "get a list of available deliverable types ")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Map<String, String> getAvailableDeliverableTypes(@PathVariable Integer templateId) {
        return templateService.getAvailableDeliverableTypes(templateId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/paymentSources/", method = RequestMethod.PUT)
    public void replacePaymentSources(@PathVariable Integer templateId, @PathVariable int displayOrder,
            @RequestBody String paymentSources) {
        templateService.replacePaymentSources(templateId, displayOrder, paymentSources);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/funding/monetaryValueScale/{monetaryValueScale}",
            method = RequestMethod.PUT)
    public void updateFundingBlockMonetaryValueScale(@PathVariable Integer templateId, @PathVariable Integer monetaryValueScale) {
        templateService.updateFundingBlockMonetaryValueScale(templateId, monetaryValueScale);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/processingRoutes", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void createProcessingRoute(@PathVariable Integer templateId, @PathVariable Integer displayOrder,
            @RequestBody ProcessingRoute processingRoute) {
        templateService.createProcessingRoute(templateId, displayOrder, processingRoute);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/performMigration", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public MigrationResponse performBlockMigration(@RequestBody MigrationRequest migrationRequest) {
        return blockMigrationService.migrateBlockData(migrationRequest);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/template/{templateId}/disassociateBlock/{blockId}", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void disassociateBlock(@PathVariable Integer templateId, @PathVariable Integer blockId) {
        templateService.disassociateBlock(templateId, blockId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/template/{templateId}/reattachDisassociateBlock/{blockId}", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void reattachBlock(@PathVariable Integer templateId, @PathVariable Integer blockId) {
        templateService.reattachDisassociateBlock(templateId, blockId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/fundingVariationsEnabled", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateFundingVariationsEnabled(@PathVariable Integer templateId, @PathVariable int displayOrder,
            @RequestBody String enabled) {
        templateService.updateFundingVariationsEnabled(templateId, displayOrder,  Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/blocks/{displayOrder}/canClaimActivity", method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateCanClaimActivity(@PathVariable Integer templateId, @PathVariable int displayOrder,
            @RequestBody String enabled) {
        templateService.updateCanClaimActivity(templateId, displayOrder,  Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/templates/{templateId}/fundingClaimsBlock/{displayOrder}/period/{periodId}/text",
            method = RequestMethod.PUT)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateFundingClaimPeriod(@PathVariable Integer templateId, @PathVariable int displayOrder,
            @PathVariable Integer periodId, @RequestBody String periodText) {
        templateService.updateFundingClaimPeriod(templateId, displayOrder, periodId, periodText);
    }

}
