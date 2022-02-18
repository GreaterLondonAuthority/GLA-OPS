/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectModel;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDetailsBlock;
import uk.gov.london.ops.project.funding.FundingBlock;
import uk.gov.london.ops.project.grant.*;
import uk.gov.london.ops.project.implementation.mapper.MilestoneMapper;
import uk.gov.london.ops.project.implementation.repository.*;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.internalblock.InternalProjectBlock;
import uk.gov.london.ops.project.internalblock.InternalQuestionsBlock;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.outputs.OutputCategoryAssumption;
import uk.gov.london.ops.project.question.Answer;
import uk.gov.london.ops.project.question.ProjectQuestion;
import uk.gov.london.ops.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.project.question.QuestionsBlock;
import uk.gov.london.ops.project.risk.InternalRiskBlock;
import uk.gov.london.ops.project.skills.AllocationType;
import uk.gov.london.ops.project.skills.FundingClaimsBlock;
import uk.gov.london.ops.project.skills.LearningGrantBlock;
import uk.gov.london.ops.project.state.ProjectState;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.template.domain.*;
import uk.gov.london.ops.project.unit.UnitDetailsBlock;
import uk.gov.london.ops.project.unit.UnitDetailsTableEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.ops.project.ProjectBuilder.createBlockFromTemplate;

@Service
@Transactional
public class TemplateProjectService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    Environment environment;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    OutputCategoryAssumptionRepository outputCategoryAssumptionRepository;

    @Autowired
    ProjectBlockRepository projectBlockRepository;

    @Autowired
    InternalProjectBlockRepository internalProjectBlockRepository;

    @Autowired
    TemplateRepository templateRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private MilestoneMapper milestoneMapper;

    /**
     * * Add a block(based on a template) to a list of projects by template
     * <p>
     * Notes: - Assumes  projectBlocks in all projects are not null - There is not synchronization, so in case of two simultaneous
     * requests, some projects can experiment display order duplications
     */
    public void addBlockToProjectsByTemplate(final Template template,
            final TemplateBlock templateBlock) {

        final List<ProjectModel> projects = findAllProjectModelByTemplate(template);
        List<NamedProjectBlock> projectBlocks = new ArrayList<>();

        for (final ProjectModel project : projects) {
            NamedProjectBlock blockFromTemplate = createBlockFromTemplate(project, templateBlock);
            projectBlocks.add(blockFromTemplate);
        }
        projectBlockRepository.saveAll(projectBlocks);

        Set<Integer> blockIds = projectBlocks.stream()
                                    .map(block -> block.getId())
                                    .collect(Collectors.toSet());

        projectBlockRepository.updateBlocksLatestForProject(blockIds);
    }

    public List<Project> findAllByTemplate(final Template template) {
        return projectRepository.findAllByTemplate(template);
    }

    List<ProjectModel> findAllProjectModelByTemplate(final Template template) {
        return projectRepository.findAllProjectModelByTemplateId(template.getId());
    }

    public Integer[] findAllIdByTemplate(final Template template) {
        return projectRepository.findAllIdByTemplateId(template.getId());
    }

    public void addInternalBlock(Template template, InternalTemplateBlock block) {
        Integer[] projectIds = findAllIdByTemplate(template);
        List<InternalProjectBlock> internalProjectBlocks = new ArrayList<>();
        for (Integer projectId : projectIds) {
            InternalProjectBlock internalProjectBlock = block.getType().newBlockInstance();
            internalProjectBlock.initFromTemplate(block);
            internalProjectBlock.setProject(new Project(projectId, ""));
            internalProjectBlocks.add(internalProjectBlock);
        }
        internalProjectBlockRepository.saveAll(internalProjectBlocks);
    }

    public void addSection(Template template, int blockDisplayOrder, QuestionsBlockSection section) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {

            List<NamedProjectBlock> lastApprovedAndUnapproved = project
                    .getLastApprovedAndUnapproved(ProjectBlockType.Questions, blockDisplayOrder);

            for (NamedProjectBlock block : lastApprovedAndUnapproved) {
                ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;
                QuestionsBlockSection toAdd = new QuestionsBlockSection(section.getExternalId(), section.getDisplayOrder(),
                        section.getText());
                questionsBlock.getSections().add(toAdd);
            }
        }
        projectRepository.saveAll(projects);
    }

    public void addSectionToInternalBlock(Template template, int blockDisplayOrder, QuestionsBlockSection section) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {

            InternalProjectBlock internalBlock =
                    project.getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions, blockDisplayOrder);

            InternalQuestionsBlock questionsBlock = (InternalQuestionsBlock) internalBlock;
            QuestionsBlockSection toAdd = new QuestionsBlockSection(section.getExternalId(), section.getDisplayOrder(),
                    section.getText());
            questionsBlock.getSections().add(toAdd);
        }
        projectRepository.saveAll(projects);
    }

    public void addQuestion(Template template, int blockDisplayOrder, TemplateQuestion templateQuestion) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        List<NamedProjectBlock> questionsBlocks = new ArrayList<>();
        for (Project project : projects) {

            List<NamedProjectBlock> lastApprovedAndUnapproved = project
                    .getLastApprovedAndUnapproved(ProjectBlockType.Questions, blockDisplayOrder);

            for (NamedProjectBlock block : lastApprovedAndUnapproved) {
                ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;
                addQuestionToBlock(questionsBlock, templateQuestion, project.getProjectState());
                questionsBlocks.add(questionsBlock);
            }
        }
        projectBlockRepository.saveAll(questionsBlocks);
    }

    public void addQuestionToInternalBlock(Template template, int blockDisplayOrder, TemplateQuestion templateQuestion) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        List<InternalQuestionsBlock> internalQuestionsBlocks = new ArrayList<>();
        for (Project project : projects) {
            InternalQuestionsBlock questionsBlock = (InternalQuestionsBlock) project
                    .getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions, blockDisplayOrder);
            addQuestionToBlock(questionsBlock, templateQuestion, project.getProjectState());
            internalQuestionsBlocks.add(questionsBlock);
        }
        internalProjectBlockRepository.saveAll(internalQuestionsBlocks);
    }

    private void addQuestionToBlock(QuestionsBlock questionsBlock, TemplateQuestion templateQuestion, ProjectState projectState) {
        questionsBlock.getAnswers().add(new Answer(templateQuestion.getQuestion()));
        questionsBlock.addTemplateQuestion(templateQuestion);
        ProjectQuestion projectQuestionByQuestionId = questionsBlock
                .getProjectQuestionByQuestionId(templateQuestion.getQuestion().getId());
        if (templateQuestion.appearsOnState(projectState)) {
            projectQuestionByQuestionId.setHidden(false);
        }
    }

    public void replaceQuestion(Template template, Integer blockDisplayOrder, Integer oldQuestionId, Question newQuestion) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            List<NamedProjectBlock> blocks = project
                    .getBlocksByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
            for (NamedProjectBlock block : blocks) {
                ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;
                Answer answer = questionsBlock.getAnswerByQuestionId(oldQuestionId);
                if (answer != null) {
                    answer.setQuestion(newQuestion);
                }
            }
        }
        projectRepository.saveAll(projects);
    }

    public void removeQuestion(Template template, Integer blockDisplayOrder, Integer questionId) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            List<NamedProjectBlock> lastApprovedAndUnapproved = project
                    .getLastApprovedAndUnapproved(ProjectBlockType.Questions, blockDisplayOrder);

            for (NamedProjectBlock block : lastApprovedAndUnapproved) {
                ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;
                questionsBlock.getAnswers().removeIf(a -> a.getQuestion().getId().equals(questionId));
                questionsBlock.getQuestions().removeIf(q -> q.getTemplateQuestion().getQuestion().getId().equals(questionId));
            }
        }
        projectRepository.saveAll(projects);
    }

    public void removeInternalQuestion(Template template, Integer blockDisplayOrder, Integer questionId) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            InternalProjectBlock block = project
                    .getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions, blockDisplayOrder);

            InternalQuestionsBlock questionsBlock = (InternalQuestionsBlock) block;
            questionsBlock.getAnswers().removeIf(a -> a.getQuestion().getId().equals(questionId));
            questionsBlock.getQuestions().removeIf(q -> q.getTemplateQuestion().getQuestion().getId().equals(questionId));

        }
        projectRepository.saveAll(projects);
    }

    public void updateAssociatedProjectsEnabled(Template template, Boolean enabled) {
        for (Project project : projectRepository.findAllByTemplate(template)) {
            project.setAssociatedProjectsEnabled(enabled);
            projectRepository.save(project);
        }
    }

    public void updateMilestoneDescriptionEnabled(Template template, Integer blockDisplayOrder, boolean enabled) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            for (NamedProjectBlock block : project
                    .getBlocksByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder)) {
                ((ProjectMilestonesBlock) block).setDescriptionEnabled(enabled);
            }
        }
        projectRepository.saveAll(projects);
    }

    public void updateMilestoneEvidentialStatus(Template template, Integer blockDisplayOrder, Integer newMaximum,
            MilestonesTemplateBlock.EvidenceApplicability evidenceApplicability) {
        List<Project> allByTemplate = projectRepository.findAllByTemplate(template);
        for (Project project : allByTemplate) {
            List<NamedProjectBlock> blocksByTypeAndDisplayOrder = project
                    .getBlocksByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
            for (NamedProjectBlock namedProjectBlock : blocksByTypeAndDisplayOrder) {
                ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) namedProjectBlock;
                milestonesBlock.setMaxEvidenceAttachments(newMaximum);
                milestonesBlock.setEvidenceApplicability(evidenceApplicability);
            }
        }
    }

    public void updateBlockDisplayName(Template template, Integer blockDisplayOrder, String blockType, String newName,
                                       boolean isInternalBlock) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            if (isInternalBlock) {
                project.getInternalBlockByTypeAndDisplayOrder(InternalBlockType.valueOf(blockType), blockDisplayOrder)
                        .setBlockDisplayName(newName);
            } else {
                project.getBlocksByTypeAndDisplayOrder(ProjectBlockType.valueOf(blockType), blockDisplayOrder)
                        .forEach(b -> b.setBlockDisplayName(newName));
            }
        }
        projectRepository.saveAll(projects);
    }

    public void replaceTenureType(Template template, Integer oldTenureTypeId, Integer newTenureTypeId) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            for (NamedProjectBlock block : project.getBlocksByType(ProjectBlockType.UnitDetails)) {
                UnitDetailsBlock unitDetailsBlock = (UnitDetailsBlock) block;
                for (UnitDetailsTableEntry entry : unitDetailsBlock.getTableEntries()) {
                    entry.setTenureId(newTenureTypeId);
                }
            }
            Set<NamedProjectBlock> blocks = project.getProjectBlocks();
            for (NamedProjectBlock block : blocks) {
                if (block.getBlockType().equals(ProjectBlockType.UnitDetails)) {
                    UnitDetailsBlock unitDetailsBlock = (UnitDetailsBlock) block;
                    for (UnitDetailsTableEntry entry : unitDetailsBlock.getTableEntries()) {
                        entry.setTenureId(newTenureTypeId);
                    }
                }
                if (block.getBlockType().equals(ProjectBlockType.AffordableHomes)) {
                    AffordableHomesBlock affordableHomesBlock = (AffordableHomesBlock) block;
                    for (AffordableHomesEntry entry : affordableHomesBlock.getEntries()) {
                        if (entry.getTenureTypeId() == oldTenureTypeId) {
                            entry.setTenureTypeId(newTenureTypeId);
                        }
                    }
                    for (IndicativeGrantRequestedEntry entry : affordableHomesBlock.getGrantRequestedEntries()) {
                        if (entry.getTenureTypeId() == oldTenureTypeId) {
                            entry.setTenureTypeId(newTenureTypeId);
                        }
                    }
                }
            }
        }
        projectRepository.saveAll(projects);
    }

    public void updateStateModel(Template template, UpdateStateModelRequest updateStateModelRequest) {
        List<Project> projects = findAllByTemplate(template);
        for (Project project : projects) {
            project.setStateModel(updateStateModelRequest.getStateModel());
            ProjectState toState = updateStateModelRequest.getToState(project.getProjectState());
            if (toState != null) {
                project.setProjectState(toState);
            }
        }
        projectRepository.saveAll(projects);
    }

    public void updateAssumptionsAffectedByCategoryChange(Integer groupId, String oldName, String newName) {
        Set<Template> templates = templateRepository.findAllUsingConfigGroup(groupId);
        for (Template template : templates) {
            Set<OutputCategoryAssumption> allAffectedByNameChange = outputCategoryAssumptionRepository
                    .findAllAffectedByNameChange(template.getId(), oldName);
            allAffectedByNameChange.forEach(a -> a.setCategory(newName));
            outputCategoryAssumptionRepository.saveAll(allAffectedByNameChange);
        }
    }

    public void updateInfoMessage(Template template, Integer displayOrder, String infoMessage) {
        List<Project> projects = findAllByTemplate(template);

        List<NamedProjectBlock> projectBlocks = new ArrayList<>();
        for (Project project : projects) {
            NamedProjectBlock projectBlock = project.getSingleBlockByDisplayOrder(displayOrder);

            if (null != projectBlock) {
                projectBlock.setInfoMessage(infoMessage);
                projectBlocks.add(projectBlock);
            }
        }
        projectBlockRepository.saveAll(projectBlocks);
    }

    public void updateInternalBlockInfoMessage(Template template, Integer displayOrder, String infoMessage) {
        List<Project> projects = findAllByTemplate(template);

        List<InternalProjectBlock> internalProjectBlocks = new ArrayList<>();
        for (Project project : projects) {
            InternalProjectBlock projectBlock = project.getSingleInternalBlockByDisplayOrder(displayOrder);

            if (null != projectBlock) {
                projectBlock.setInfoMessage(infoMessage);
                internalProjectBlocks.add(projectBlock);
            }
        }
        internalProjectBlockRepository.saveAll(internalProjectBlocks);
    }

    public void updateRiskAdjustedFiguresFlag(Template template, boolean enabled) {
        List<Project> projects = findAllByTemplate(template);

        for (Project project : projects) {
            InternalRiskBlock projectBlock = (InternalRiskBlock) project
                    .getInternalBlockByType(InternalBlockType.Risk);

            if (null != projectBlock) {
                projectBlock.setRiskAdjustedFiguresFlag(enabled);
            }
        }
        projectRepository.saveAll(projects);
    }

    public Boolean isRiskRatingUsed(Integer riskRatingId) {
        return projectRepository.isRiskRatingUsedForProject(riskRatingId);
    }

    public void replacePaymentSources(Template template, Integer displayOrder, String paymentSources) {
        List<Project> projectsForTemplate = projectRepository.findAllByTemplate(template);

        if (!projectsForTemplate.isEmpty()) {
            for (Project project : projectsForTemplate) {
                project.getProjectBlocks()
                        .stream()
                        .filter(block -> block.getDisplayOrder().equals(displayOrder))
                        .forEach(block -> block.setPaymentSourcesString(paymentSources));
            }
        }
        projectRepository.saveAll(projectsForTemplate);
    }

    public void updateFundingBlockMonetaryValueScale(Template template, Integer monetaryValueScale) {
        List<Project> projectsForTemplate = projectRepository.findAllByTemplate(template);
        if (!projectsForTemplate.isEmpty()) {
            for (Project project : projectsForTemplate) {
                project.getProjectBlocks()
                        .stream()
                        .filter(block -> ProjectBlockType.Funding.equals(block.getBlockType()))
                        .forEach((fundingBlock -> ((FundingBlock) fundingBlock).setMonetaryValueScale(monetaryValueScale)));
            }
        }
        projectRepository.saveAll(projectsForTemplate);
    }

    public void migrateContactDetails(Template template, Integer questionsBlockDisplayOrder, Integer contactNameQuestionId,
            Integer contactEmailQuestionId) {
        List<Project> projectsForTemplate = projectRepository.findAllByTemplate(template);
        for (Project project : projectsForTemplate) {
            for (NamedProjectBlock block : project
                    .getBlocksByTypeAndDisplayOrder(ProjectBlockType.Questions, questionsBlockDisplayOrder)) {
                ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;

                Answer nameAnswer = questionsBlock.getAnswerByQuestionId(contactNameQuestionId);
                if (nameAnswer == null) {
                    nameAnswer = new Answer(questionsBlock.getTemplateQuestionByQuestionId(contactNameQuestionId).getQuestion());
                    questionsBlock.getAnswers().add(nameAnswer);
                }
                nameAnswer.setAnswer(project.getDetailsBlock().getMainContact());

                Answer emailAnswer = questionsBlock.getAnswerByQuestionId(contactEmailQuestionId);
                if (emailAnswer == null) {
                    emailAnswer = new Answer(
                            questionsBlock.getTemplateQuestionByQuestionId(contactEmailQuestionId).getQuestion());
                    questionsBlock.getAnswers().add(emailAnswer);
                }
                emailAnswer.setAnswer(project.getDetailsBlock().getMainContactEmail());
            }
        }
        projectRepository.saveAll(projectsForTemplate);
    }

    public void disassociateBlock(Integer[] ids, Integer displayOrder) {
        projectBlockRepository.copyProjectIdToDetachedID(ids, displayOrder);
        projectBlockRepository.deleteProjectIdFromProjectBlock(ids, displayOrder);

    }

    public void disassociateInternalBlock(Integer[] ids, Integer displayOrder) {
        internalProjectBlockRepository.copyProjectIdToDetachedID(ids, displayOrder);
        internalProjectBlockRepository.deleteProjectIdFromProjectBlock(ids, displayOrder);

    }

    public void reattachRemovedBlock(Integer[] ids, Integer displayOrder) {
        projectBlockRepository.reattachRemovedBlock(ids, displayOrder);
        projectBlockRepository.reattachLatestBlock(ids, displayOrder);
    }

    public void reattachRemovedInternalBlock(Integer[] ids, Integer displayOrder) {
        internalProjectBlockRepository.reattachRemovedInternalBlock(ids, displayOrder);
    }

    public int updateBlockDisplayOrder(Integer templateId, ProjectBlockType blockType, Integer blockDisplayOrder,
            Integer newDisplayOrder) {
        return jdbcTemplate.update(" update project_block "
                + " set display_order = ? "
                + " where id in ( "
                + " select pb.id from project_block pb "
                + " inner join project p on p.id = pb.project_id "
                + " inner join template t on t.id = p.template_id "
                + " where t.id = ? "
                + " and pb.project_block_type = ? "
                + " and pb.display_order = ?) ", newDisplayOrder, templateId, blockType.toString(), blockDisplayOrder);
    }

    public void addFundingClaimCategory(Template template, FundingClaimsTemplateBlock fundingClaimsTemplateBlock,
                                        FundingClaimCategory category) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            for (NamedProjectBlock block: project.getBlocksByType(ProjectBlockType.FundingClaims)) {
                FundingClaimsBlock fundingClaimsBlock = (FundingClaimsBlock) block;
                fundingClaimsBlock.addFundingClaimEntriesForCategory(fundingClaimsTemplateBlock, category);
            }
        }
        projectRepository.saveAll(projects);
    }

    public void addFundingClaimSubCategory(Template template, FundingClaimsTemplateBlock fundingClaimsTemplateBlock, FundingClaimCategory category,
            FundingClaimCategory subcategory) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            for (NamedProjectBlock block: project.getBlocksByType(ProjectBlockType.FundingClaims)) {
                FundingClaimsBlock fundingClaimsBlock = (FundingClaimsBlock) block;
                fundingClaimsBlock.addFundingClaimEntriesForCategorySubcategory(fundingClaimsTemplateBlock, category, subcategory);
            }
        }
        projectRepository.saveAll(projects);
    }

    public void updateFundingClaimCategoryDisplayOrder(Template template, Integer categoryId, Integer newDisplayOrder) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            for (NamedProjectBlock block: project.getBlocksByType(ProjectBlockType.FundingClaims)) {
                FundingClaimsBlock fundingClaimsBlock = (FundingClaimsBlock) block;
                fundingClaimsBlock.getFundingClaimEntriesForCategory(categoryId).forEach(e -> e.setDisplayOrder(newDisplayOrder));
            }
        }
        projectRepository.saveAll(projects);
    }

    public void addLearningGrantAllocationType(Template template, LearningGrantTemplateBlock learningGrantTemplateBlock,
            AllocationType allocationType) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            for (NamedProjectBlock block: project.getBlocksByType(ProjectBlockType.LearningGrant)) {
                LearningGrantBlock learningGrantBlock = (LearningGrantBlock) block;
                Integer startYear = learningGrantTemplateBlock.getStartYear();
                Integer numberOfYears = learningGrantTemplateBlock.getNumberOfYears();
                for (Integer year = startYear; year < startYear + numberOfYears; year++) {
                    learningGrantBlock.addLearningGrantAllocationEntriesForAllocationType(year, allocationType);
                }
            }
        }
        projectRepository.saveAll(projects);
    }

    public void deleteMilestone(Template template, Integer blockDisplayOrder, Integer processingRouteId, Integer milestoneId,
                                boolean updateActiveProjects) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            if (!updateActiveProjects) {
                validateProjectNotActive(project);
            }
            for (NamedProjectBlock projectBlock : project
                    .getBlocksByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder)) {
                ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) projectBlock;
                if (milestonesBlock.getProcessingRouteId() == null // projects with default processing route wont store its ID
                        || milestonesBlock.getProcessingRouteId().equals(processingRouteId)) {
                    milestonesBlock.getMilestones().removeIf(m -> m.getExternalId().equals(milestoneId));
                }
            }
        }
        projectRepository.saveAll(projects);
    }

    public void updateOWhichCategories(Template template, Integer blockDisplayOrder,
                                       List<AffordableHomesOfWhichCategory> existing,
                                       List<AffordableHomesOfWhichCategory> newCategories) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        AffordableHomesTemplateBlock templateBlock = (AffordableHomesTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.AffordableHomes);

        for (Project project : projects) {
            for (NamedProjectBlock projectBlock : project.getBlocksByTypeAndDisplayOrder(
                    ProjectBlockType.AffordableHomes, blockDisplayOrder)) {
                AffordableHomesBlock indBlock = (AffordableHomesBlock) projectBlock;

                for (AffordableHomesOfWhichCategory category : AffordableHomesOfWhichCategory.values()) {
                    if (newCategories.contains(category) && !existing.contains(category)) {
                        // cat added
                        for (TemplateTenureType tenureType : template.getTenureTypes()) {
                            indBlock.getEntries().add(
                                    new AffordableHomesEntry(tenureType.getExternalId(),
                                            AffordableHomesType.StartOnSite, category));
                            indBlock.getEntries().add(
                                    new AffordableHomesEntry(tenureType.getExternalId(),
                                            AffordableHomesType.Completion, category));
                        }
                    } else if (!newCategories.contains(category) && existing.contains(category)) {
                        // remove cat
                        indBlock.getEntries().removeIf(e -> e.getOfWhichCategory() != null
                                && !templateBlock.getOfWhichCategories().contains(e.getOfWhichCategory()));
                    }
                }
            }
        }
        projectRepository.saveAll(projects);

    }

    public void updateCompletionOnlyAvailability(Template template, Integer blockDisplayOrder,
            boolean completionOnly) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            for (NamedProjectBlock projectBlock : project.getBlocksByTypeAndDisplayOrder(
                    ProjectBlockType.AffordableHomes, blockDisplayOrder)) {
                AffordableHomesBlock affBlock = (AffordableHomesBlock) projectBlock;
                if (completionOnly) {
                    affBlock.setCompletionOnly(false); // default is false when available on template (GLA-37709)
                }
            }
        }
        projectRepository.saveAll(projects);
    }

    public void addMilestone(Template template, Integer blockDisplayOrder, Integer processingRouteId,
                             MilestoneTemplate milestoneTemplate, boolean updateActiveProjects) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            if (!updateActiveProjects) {
                validateProjectNotActive(project);
            }
            for (NamedProjectBlock projectBlock : project
                    .getBlocksByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder)) {
                ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) projectBlock;
                if (milestonesBlock.getProcessingRouteId() == null // projects with default processing route wont store its ID
                        || milestonesBlock.getProcessingRouteId().equals(processingRouteId)) {
                    Milestone projectMilestone = milestoneMapper.toProjectMilestone(milestoneTemplate, template, null, null);
                    milestonesBlock.getMilestones().add(projectMilestone);
                }
            }
        }
        projectRepository.saveAll(projects);
    }

    private void validateProjectNotActive(Project project) {
        if (ProjectStatus.Active.equals(project.getStatusType()) || ProjectStatus.Closed.equals(project.getStatusType())) {
            throw new ValidationException("cannot update active project " + project.getId());
        }
    }

    public void updateDetailsTemplateConsortiumRequirements(Template template, DetailsTemplate details) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            for (NamedProjectBlock projectBlock : project.getBlocksByType(ProjectBlockType.Details)) {
                ProjectDetailsBlock detailsBlock = (ProjectDetailsBlock) projectBlock;
                if ((details.getDevelopmentLiabilityOrganisationRequirement().equals(Requirement.mandatory)
                        && detailsBlock.getDevelopmentLiabilityOrganisationId() == null)
                        || (details.getPostCompletionLiabilityOrganisationRequirement().equals(Requirement.mandatory)
                        && detailsBlock.getPostCompletionLiabilityOrganisationId() == null)) {
                    detailsBlock.setBlockMarkedComplete(false);
                }
            }
        }
        projectRepository.saveAll(projects);
    }

    void updateMilestonesBlockMilestone(Template template, Integer blockDisplayOrder, Integer processingRouteId,
            Integer milestoneId, String milestoneSummary, Integer milestoneDisplayOrder, Requirement requirement,
            Boolean naSelectable) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        List<NamedProjectBlock> milestoneBlocks = new ArrayList<>();
        for (Project project : projects) {
            for (NamedProjectBlock projectBlock : project
                    .getBlocksByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder)) {
                ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) projectBlock;
                if (milestonesBlock.getProcessingRouteId() == null
                        || milestonesBlock.getProcessingRouteId().equals(processingRouteId)) {
                    Milestone milestone = milestonesBlock.getMilestoneBySummary(milestoneSummary);
                    if (milestone != null) {
                        milestone.setDisplayOrder(milestoneDisplayOrder);
                        milestone.setRequirement(requirement);
                        milestone.setNaSelectable(naSelectable);
                        milestoneBlocks.add(milestonesBlock);
                    }
                }
            }
        }
        projectBlockRepository.saveAll(milestoneBlocks);
    }
}
