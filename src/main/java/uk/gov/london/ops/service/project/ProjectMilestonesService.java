/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.domain.attachment.StandardAttachment;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.project.funding.FundingBlock;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.template.MilestonesTemplateBlock;
import uk.gov.london.ops.domain.template.ProcessingRoute;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.payment.PaymentGroup;
import uk.gov.london.ops.payment.PaymentService;
import uk.gov.london.ops.project.implementation.MilestoneMapper;
import uk.gov.london.ops.framework.exception.ValidationException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.london.ops.domain.project.ClaimStatus.Claimed;
import static uk.gov.london.ops.domain.project.ClaimStatus.Pending;

@Service
@Transactional
public class ProjectMilestonesService extends BaseProjectService implements PostCloneNotificationListener, ProjectPaymentGenerator {

    @Autowired
    MilestoneMapper milestoneMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProjectFundingService fundingService;

    @Autowired
    private PaymentService paymentService;

    public ProjectMilestonesBlock createNewMilestone(Integer projectId, Integer blockId, Milestone milestone, boolean autosave) {
        Project project = get(projectId);
        ProjectMilestonesBlock block = (ProjectMilestonesBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.Milestones, blockId);
        checkForLock(block);

        if (milestone.getSummary() == null || milestone.getSummary().length() == 0) {
            throw new ValidationException("summary", "Summary is required when creating a milestone.");
        }
        if (milestone.getSummary().length() > 50) {
            throw new ValidationException("summary", "Maximum length for summary field is 50 characters.");
        }
        if (milestone.getMilestoneDate() == null) {
            throw new ValidationException("milestoneDate", "Milestone Date is required when creating a milestone.");
        }
        if (block.getMilestones().stream().anyMatch(m -> m.getSummary().equals(milestone.getSummary()))) {
            throw new ValidationException("summary", "Milestone already exists.");
        }

        User currentUser = userService.currentUser();

        milestone.setCreatedOn(environment.now());
        milestone.setCreatedBy(currentUser.getUsername());
        milestone.setManuallyCreated(Boolean.TRUE);
        milestone.setMonetary(project.getTemplate().getAllowMonetaryMilestones());

        if (currentUser.isGla() && ProjectStatus.Assess.equals(project.getStatusType())) {
            milestone.setConditional(true);
            milestone.setRequirement(Requirement.mandatory);
        }
        else {
            milestone.setConditional(false);
            milestone.setRequirement(Requirement.optional);
        }
        milestone.autoUpdateMilestoneStatus();

        block.getMilestones().add(milestone);
        releaseOrRefreshLock(block, !autosave);

        updateProject(project);

        return block;
    }

    public Project updateProjectMilestones(Project project, ProjectMilestonesBlock milestonesBlock, Integer blockId, boolean autosave) {
        ProjectMilestonesBlock block = (ProjectMilestonesBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.Milestones, blockId);
        checkForLock(block);
        block.merge(milestonesBlock);
        releaseOrRefreshLock(block, !autosave);
        return this.updateProject(project);
    }

    public Project updateProcessingRoute(Project project, Integer processingRouteId, Integer blockId, boolean autosave) {
        checkForLock(project.getSingleBlockByType(ProjectBlockType.Milestones));

        ProjectMilestonesBlock block = (ProjectMilestonesBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.Milestones, blockId);

        if (block.hasClaimedMilestones()) {
            throw new ValidationException("Unable to change processing route on project with claimed or approved milestones.");
        }

        MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.Milestones);
        ProcessingRoute processingRoute = milestonesTemplateBlock.getProcessingRoute(processingRouteId);

        block.setProcessingRouteId(processingRoute.getId());
        block.getMilestones().clear();
        block.getMilestones().addAll(milestoneMapper.toProjectMilestones(processingRoute.getMilestones(), project.getTemplate()));

        releaseOrRefreshLock(project.getSingleBlockByType(ProjectBlockType.Milestones), !autosave);
        return this.updateProject(project);
    }

    public Project deleteMilestoneFromProject(Project project, Milestone milestone, Integer blockId, boolean autosave) {
        ProjectMilestonesBlock block = (ProjectMilestonesBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.Milestones, blockId);

        checkForLock(block);

        if (milestone.isManuallyCreated()) {
            FundingBlock fundingBlock = project.getFundingBlock();
            if (fundingBlock != null) {
                List<Integer> yearsMilestoneUsed = fundingService.getYearsMilestoneUsed(project.getId(), fundingBlock.getId(), milestone.getSummary());
                if (!yearsMilestoneUsed.isEmpty()) {
                    String message = "You can't delete this milestone as payments are profiled against it in " + GlaUtils.getFinancialYearList(yearsMilestoneUsed, ", ") +
                            ". You must delete the profiled payment(s) before you can delete the milestone.";
                    throw new ValidationException(message);
                }
            }

            if (milestone.isClaimed()) {
                throw new ValidationException("Cannot delete a "+milestone.getClaimStatus()+" milestone");
            }

            if (block.getMilestones() != null) {
                if (!block.getMilestones().remove(milestone)) {
                    throw new ValidationException("Milestone is not assigned to the specified project. ");
                }
            }
            releaseOrRefreshLock(block, !autosave);

            this.updateProject(project);
            auditService.auditCurrentUserActivity(String.format("Removed milestone %d: %s from project with id: %d", milestone.getId(), milestone.getSummary(), project.getId()));
            return project;
        } else {
            throw new ValidationException("Unable to delete milestones that are not manually added.");
        }
    }

    public void claim(Integer projectId, Integer milestoneId, Milestone updatedMilestone) {
        Project project = get(projectId);

        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();

        checkForLock(milestonesBlock);

        Milestone existingMilestone = milestonesBlock.getMilestoneById(milestoneId);

        if (!Pending.equals(existingMilestone.getClaimStatus())) {
            throw new ValidationException("cannot claim milestone in status "+existingMilestone.getClaimStatus());
        }

        if (!milestonesBlock.isPaymentsEnabled() && existingMilestone.getMonetary()) {
            throw new ValidationException("cannot claim monetary milestone when payments are disabled for this template. ");
        }

        Map<GrantType, Long> maxClaims = milestonesBlock.getMaxClaims();
        if (Template.MilestoneType.MonetaryValue.equals(project.getTemplate().getMilestoneType())) { // as for monetary split the amount is calculated dynamically, we don't need to validate it here
            validateAmountClaimed(updatedMilestone.getClaimedGrant(), GrantType.Grant, maxClaims);
        }
        validateAmountClaimed(updatedMilestone.getClaimedRcgf(), GrantType.RCGF, maxClaims);
        validateAmountClaimed(updatedMilestone.getClaimedDpf(), GrantType.DPF, maxClaims);

//        Long milestoneGrantClaimed = milestonesBlock.getMilestoneGrantClaimed(existingMilestone.getId());
//        existingMilestone.setClaimedGrant(milestoneGrantClaimed);
        existingMilestone.setClaimedGrant(updatedMilestone.getClaimedGrant());
        existingMilestone.setClaimedRcgf(updatedMilestone.getClaimedRcgf());
        existingMilestone.setClaimedDpf(updatedMilestone.getClaimedDpf());
        existingMilestone.setClaimStatus(Claimed);

        if (existingMilestone.getMonetaryValue() != null) {
            existingMilestone.setMonetaryValue(new BigDecimal(updatedMilestone.calculateTotalValueClaimed()));
        }

        updateProject(project);
    }

    public void validateAmountClaimed(Long amountClaimed, GrantType grantType, Map<GrantType, Long> maxClaims) {
        if (amountClaimed != null && amountClaimed > maxClaims.get(grantType)) {
            throw new ValidationException("cannot claim more than "+maxClaims.get(grantType)+" for "+grantType);
        }
    }

    public void cancelClaim(Integer projectId, Integer milestoneId) {
        Project project = get(projectId);

        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();

        checkForLock(milestonesBlock);
        Milestone existingMilestone = milestonesBlock.getMilestoneById(milestoneId);

        if (!Claimed.equals(existingMilestone.getClaimStatus())) {
            throw new ValidationException("cannot cancel claim for milestone in status "+existingMilestone.getClaimStatus());
        }

        existingMilestone.setClaimedGrant(null);
        existingMilestone.setClaimedRcgf(null);
        existingMilestone.setClaimedDpf(null);
        existingMilestone.setClaimStatus(ClaimStatus.Pending);
        updateProject(project);
    }

    public void cancelReclaim(Integer projectId, Integer milestoneId) {
        Project project = get(projectId);

        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();

        checkForLock(milestonesBlock);
        Milestone existingMilestone = milestonesBlock.getMilestoneById(milestoneId);

        existingMilestone.setReclaimedGrant(null);
        existingMilestone.setReclaimedRcgf(null);
        existingMilestone.setReclaimedDpf(null);
        existingMilestone.setReclaimReason(null);
        updateProject(project);
    }

    public ProjectMilestonesBlock attachMilestoneEvidence(Integer projectId, Integer blockId, Integer milestoneId, Integer fileId, boolean releaseLock) {

        Project project = get(projectId);

        NamedProjectBlock projectBlockById = project.getProjectBlockById(blockId);

        if (projectBlockById == null || !(projectBlockById instanceof ProjectMilestonesBlock)) {
            throw new ValidationException("Incorrect block specified for update");
        }
        ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) projectBlockById;

        checkForLock(milestonesBlock);

        MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.Milestones);
        Integer maxEvidenceAttachments = milestonesTemplateBlock.getMaxEvidenceAttachments();
        Milestone existingMilestone = milestonesBlock.getMilestoneById(milestoneId);

        if (maxEvidenceAttachments != null && maxEvidenceAttachments.equals(existingMilestone.getAttachments().size())) {
            throw new ValidationException("Unable to add more attachments as the limit has been reached.");
        }

        AttachmentFile file = fileService.get(fileId);

        StandardAttachment standardAttachment = new StandardAttachment(file);

        existingMilestone.getAttachments().add(standardAttachment);
        releaseOrRefreshLock(milestonesBlock, releaseLock);
        project = updateProject(project);



        return project.getMilestonesBlock();
    }

    public ProjectMilestonesBlock removeMilestoneEvidence(Integer projectId, Integer blockId, Integer milestoneId, Integer attachmentId, boolean releaseLock) {
        Project project = get(projectId);

        NamedProjectBlock projectBlockById = project.getProjectBlockById(blockId);

        if (projectBlockById == null || !(projectBlockById instanceof ProjectMilestonesBlock)) {
            throw new ValidationException("Incorrect block specified for update");
        }
        ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) projectBlockById;

        checkForLock(milestonesBlock);


        Milestone existingMilestone = milestonesBlock.getMilestoneById(milestoneId);
        if (existingMilestone == null) {
            throw new ValidationException("Requested milestone not found.");
        }

        Optional<StandardAttachment> first = existingMilestone.getAttachments().stream().filter(m -> m.getId().equals(attachmentId)).findFirst();
        if (first.isPresent()) {
            StandardAttachment attachment = first.get();
            existingMilestone.getAttachments().remove(attachment);
            auditService.auditCurrentUserActivity("Attachment " + attachment.getFileName() + " was deleted from Milestone " + milestoneId + " on project: " + project.getId());
        } else {
            throw new ValidationException("Unable to remove attachment with specified id.");
        }



        releaseOrRefreshLock(milestonesBlock, releaseLock);
        project = updateProject(project);



        return project.getMilestonesBlock();

    }

    @Override
    public void handleBlockClone(Project project, Integer originalBlockId, Integer newBlockId) {
        // nothing as we don't copy ledger entries with new block versions for the same project
    }

    @Override
    public void handleProjectClone(Project oldProject, Integer originalBlockId, Project newProject, Integer newBlockId) {
        NamedProjectBlock projectBlockById = oldProject.getProjectBlockById(originalBlockId);
        // check if correct block type
        if (projectBlockById != null && (ProjectBlockType.Milestones.equals(projectBlockById.getBlockType()))) {
            paymentService.clonePaymentGroupsForBlock(originalBlockId, newProject.getId(), newBlockId);
        }
    }

    @Override
    public PaymentGroup generatePaymentsForProject(Project project, String approvalRequestedBy) {
        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();
        if (milestonesBlock != null && (milestonesBlock.getApprovalWillCreatePendingPayment() || milestonesBlock.getApprovalWillCreatePendingReclaim())) {
            // TODO / TechDebt to be fixed as part of GLA-23993
            return paymentService.generatePaymentsForClaimedMilestones(project, approvalRequestedBy);
        }
        else {
            return null;
        }
    }

}
