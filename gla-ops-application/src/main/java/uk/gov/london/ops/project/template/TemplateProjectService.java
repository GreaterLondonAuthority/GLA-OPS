/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.project.BaseProjectService;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.funding.FundingBlock;
import uk.gov.london.ops.project.implementation.repository.OutputCategoryAssumptionRepository;
import uk.gov.london.ops.project.implementation.repository.ProjectBlockRepository;
import uk.gov.london.ops.project.implementation.repository.TemplateRepository;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.internalblock.InternalQuestionsBlock;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.outputs.OutputCategoryAssumption;
import uk.gov.london.ops.project.question.Answer;
import uk.gov.london.ops.project.question.ProjectQuestion;
import uk.gov.london.ops.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.project.question.QuestionsBlock;
import uk.gov.london.ops.project.risk.InternalRiskBlock;
import uk.gov.london.ops.project.state.ProjectState;
import uk.gov.london.ops.project.template.domain.InternalTemplateBlock;
import uk.gov.london.ops.project.template.domain.MilestonesTemplateBlock;
import uk.gov.london.ops.project.template.domain.Question;
import uk.gov.london.ops.project.template.domain.QuestionsBlockSection;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateQuestion;
import uk.gov.london.ops.project.unit.UnitDetailsBlock;
import uk.gov.london.ops.project.unit.UnitDetailsTableEntry;
import uk.gov.london.ops.refdata.OutputConfigurationGroup;

@Service
@Transactional
public class TemplateProjectService extends BaseProjectService {

    @Autowired
    OutputCategoryAssumptionRepository outputCategoryAssumptionRepository;

    @Autowired
    ProjectBlockRepository projectBlockRepository;

    @Autowired
    TemplateRepository templateRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * * Add a block(based on a template) to a list of projects by template
     * <p>
     * Notes: - Assumes  projectBlocks in all projects are not null - There is not synchronization, so in case of two simultaneous
     * requests, some projects can experiment display order duplications
     */
    public List<Project> addBlockToProjectsByTemplate(final Template template,
            final TemplateBlock templateBlock) {

        final List<Project> projects = projectRepository.findAllByTemplate(template);
        for (final Project project : projects) {
            NamedProjectBlock blockFromTemplate = createBlockFromTemplate(project, templateBlock);
            project.addBlockToProject(blockFromTemplate);
            project.getLatestProjectBlocks().add(blockFromTemplate);
            project.setLastModified(environment.now());
        }
        return projectRepository.saveAll(projects);
    }

    public List<Project> findAllByTemplate(final Template template) {
        return projectRepository.findAllByTemplate(template);
    }

    public Integer[] findAllIdByTemplate(final Template template) {
        return projectRepository.findAllIdByTemplate(template);
    }

    public void addInternalBlock(Template template, InternalTemplateBlock block) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            addInternalBlockToProject(project, block);
        }
        projectRepository.saveAll(projects);
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

    public void addQuestion(Template template, int blockDisplayOrder, TemplateQuestion templateQuestion) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {

            List<NamedProjectBlock> lastApprovedAndUnapproved = project
                    .getLastApprovedAndUnapproved(ProjectBlockType.Questions, blockDisplayOrder);

            for (NamedProjectBlock block : lastApprovedAndUnapproved) {
                ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;
                addQuestionToBlock(questionsBlock, templateQuestion, project.getProjectState());
            }
        }
        projectRepository.saveAll(projects);
    }

    public void addQuestionToInternalBlock(Template template, int blockDisplayOrder, TemplateQuestion templateQuestion) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            InternalQuestionsBlock questionsBlock = (InternalQuestionsBlock) project
                    .getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions, blockDisplayOrder);
            addQuestionToBlock(questionsBlock, templateQuestion, project.getProjectState());
        }
        projectRepository.saveAll(projects);
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
                ProjectQuestionsBlock qBlock = (ProjectQuestionsBlock) block;
                Answer answer = qBlock.getAnswerByQuestionId(oldQuestionId);
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

    public void updateMilestoneNaSelectable(Template template, Integer blockDisplayOrder, Integer processingRouteId,
            Integer milestoneId, Boolean naSelectable) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            for (NamedProjectBlock projectBlock : project
                    .getBlocksByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder)) {
                ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) projectBlock;
                if (milestonesBlock.getProcessingRouteId() == null // projects with default processing route wont store its ID
                        || milestonesBlock.getProcessingRouteId().equals(processingRouteId)) {
                    if (milestonesBlock.getMilestoneByExternalId(milestoneId)
                            != null) { //null check for projects that haven't selected a processing route yet
                        milestonesBlock.getMilestoneByExternalId(milestoneId).setNaSelectable(naSelectable);
                    }
                }
            }
        }
        projectRepository.saveAll(projects);
    }

    public void updateBlockDisplayName(Template template, Integer blockDisplayOrder, ProjectBlockType blockType, String newName) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            project.getBlocksByTypeAndDisplayOrder(blockType, blockDisplayOrder).forEach(b -> b.setBlockDisplayName(newName));
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
        }
        projectRepository.saveAll(projects);
    }

    public void updateEnforceFundingBalance(Template template, Boolean enforce) {
        List<Project> allProjectsByTemplate = findAllByTemplate(template);

        for (Project p : allProjectsByTemplate) {
            List<NamedProjectBlock> allBlocksByType = p.getBlocksByType(ProjectBlockType.Funding);
            for (NamedProjectBlock fundingBlock : allBlocksByType) {
                ((FundingBlock) fundingBlock).setFundingBalanceEnforced(enforce);
            }
        }
        projectRepository.saveAll(allProjectsByTemplate);
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

    public void updateAssessmentsAffectedByCategoryChange(OutputConfigurationGroup group, String oldName, String newName) {
        Set<Template> templates = templateRepository.findAllUsingConfigGroup(group.getId());
        for (Template template : templates) {
            Set<OutputCategoryAssumption> allAffectedByNameChange = outputCategoryAssumptionRepository
                    .findAllAffectedByNameChange(template.getId(), oldName);
            allAffectedByNameChange.forEach(a -> a.setCategory(newName));
            outputCategoryAssumptionRepository.saveAll(allAffectedByNameChange);
        }
    }

    public void updateInfoMessage(Template template, Integer displayOrder, String infoMessage) {
        List<Project> projects = findAllByTemplate(template);

        for (Project project : projects) {
            NamedProjectBlock projectBlock = project.getSingleBlockByDisplayOrder(displayOrder);

            if (null != projectBlock) {
                projectBlock.setInfoMessage(infoMessage);
            }
        }
        projectRepository.saveAll(projects);
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

    public void reattachRemovedBlock(Integer[] ids, Integer displayOrder) {
        projectBlockRepository.reattachRemovedBlock(ids, displayOrder);
        projectBlockRepository.reattachLatestBlock(ids, displayOrder);
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
}
