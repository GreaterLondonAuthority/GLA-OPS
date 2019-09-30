/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.domain.attachment.StandardAttachment;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.project.funding.*;
import uk.gov.london.ops.domain.template.FundingTemplateBlock;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.payment.*;
import uk.gov.london.ops.refdata.ConfigurableListItem;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.repository.FundingActivityGroupRepository;
import uk.gov.london.ops.repository.FundingActivityRepository;
import uk.gov.london.ops.web.model.ProjectLedgerItemRequest;
import uk.gov.london.ops.web.model.project.FundingActivityLineItem;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.project.SpendType.CAPITAL;
import static uk.gov.london.ops.domain.project.SpendType.REVENUE;
import static uk.gov.london.ops.framework.calendar.OPSCalendar.yearStringShort;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;

@Service
@Transactional
public class ProjectFundingService extends BaseProjectFinanceService implements ProjectPaymentGenerator {

    @Autowired
    private FileService fileService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RefDataService refDataService;

    @Autowired
    private FundingActivityRepository fundingActivityRepository;

    @Autowired
    private FundingActivityGroupRepository fundingActivityGroupRepository;

    @Override
    ProjectBlockType getBlockType() {
        return ProjectBlockType.Funding;
    }

    public List<Integer> getYearsMilestoneUsed(Integer projectId, Integer blockId, String milestoneName) {
        FundingBlock fundingBlock = this.getProjectFundingBlock( projectId,  blockId, 2000);
        List<Integer> years = new ArrayList<>();
        FundingByYearAndQuarter fundingByYearAndQuarter = fundingBlock.getFundingByYearAndQuarter();
        for (FundingByYearAndQuarter.FundingYQYear year : fundingByYearAndQuarter.getYears()) {
            if (year.getMilestone(milestoneName) != null) {
                years.add(year.getYear());
            }
        }
        return years;
    }

    public FundingBlock getProjectFundingBlock(Integer projectId, Integer blockId, Integer year) {
        FundingBlock fundingBlock = (FundingBlock) get(projectId).getProjectBlockById(blockId);
        setPopulatedYears(fundingBlock);

        for (Integer populatedYear: fundingBlock.getPopulatedYears()) {
            List<FundingActivity> activities = fundingActivityRepository.findAllByBlockIdAndYear(blockId, populatedYear);
            if (CollectionUtils.isNotEmpty(activities)) {
                fundingBlock.getFundingSummary().addActivities(activities);

                fundingBlock.getFundingByYearAndQuarter().addActivities(activities);

                if (populatedYear.equals(year)) {
                    fundingBlock.setYearBreakdown(new FundingYearBreakdown(year, activities));
                    initialiseClaimDataForSections(fundingBlock, year);
                }
            }
        }


        return fundingBlock;
    }



    void initialiseClaimDataForSections(FundingBlock fundingBlock, Integer year) {
        List<Claim> claimsForQuarter =
                fundingBlock.getClaims().stream().filter(c -> c.getYear().equals(year)).collect(Collectors.toList());
        FundingTemplateBlock fundingTemplateBlock =
                (FundingTemplateBlock) fundingBlock.getProject().getTemplate().getSingleBlockByType(ProjectBlockType.Funding);
        boolean evidenceRequired = fundingTemplateBlock.isBudgetEvidenceAttachmentEnabled();

        for (FundingSection section : fundingBlock.getYearBreakdown().getSections()) {
            final int quarterValue = section.getSectionNumber();

            Claim claim = claimsForQuarter.stream().filter(c -> c.getClaimTypePeriod().equals(quarterValue)).findFirst().orElse(null);

            if (claim != null) {
                section.setClaim(claim);
                switch (claim.getClaimStatus()) {
                    case Claimed:
                        section.setStatus(FundingSection.FundingSectionStatus.Claimed);
                        break;
                    case Approved:
                        section.setStatus(FundingSection.FundingSectionStatus.Approved);
                        break;
                }
            } else {
                OffsetDateTime now = OffsetDateTime.now();
                int currentQuarter = GlaUtils.getCurrentQuarter(now.getMonthValue());
                int currentFinYear = (currentQuarter == 4) ? now.getYear() - 1 : now.getYear();
                int currentFinYearAndQuarter = (currentFinYear * 10) + currentQuarter;
                int requestedFinYearAndQuarter = (year * 10) + quarterValue;
                section.setStatus(FundingSection.FundingSectionStatus.Claimable);


                if(!fundingBlock.getProject().isClaimsEnabled() || !fundingBlock.isFundingBalanceEnforced() || !fundingBlock.isComplete()){
                    section.setStatus(FundingSection.FundingSectionStatus.NotClaimable);
                } else if (currentFinYearAndQuarter <= requestedFinYearAndQuarter) {
                    // check quarter is in the past
                    section.setStatus(FundingSection.FundingSectionStatus.NotClaimable);
                    section.setNotClaimableReason("Claim range is not yet in the past.");
                } else if (!section.isMonetaryClaimRequired()) {
                    // not claimable if nothing to claim
                    section.setStatus(FundingSection.FundingSectionStatus.NotClaimable);
                    section.setNotClaimableReason("Nothing to claim.");
                } else if (evidenceRequired && !section.isEvidenceAttachedToEveryActivity()) {
                    // evidence must be attached if appropriate
                    section.setStatus(FundingSection.FundingSectionStatus.NotClaimable);
                    section.setNotClaimableReason("All evidence must be attached to claim.");
                }
            }
        }
    }

    boolean checkForAnyExistingClaims(FundingBlock fundingBlock, Integer year, Integer quarter) {
        return fundingBlock.getClaims().stream()
                .anyMatch(c -> c.getClaimTypePeriod().equals(quarter) && c.getYear().equals(year));
    }

    public FundingActivity createOrUpdateFundingActivity(Integer projectId, Integer blockId, FundingActivityLineItem activityRequest) {

        FundingBlock fundingBlock = validateFundingBlockLocked(projectId, blockId);
        boolean existingClaim = checkForAnyExistingClaims(fundingBlock, activityRequest.getYear(), activityRequest.getQuarter());

        if (existingClaim) {
            throw new ValidationException("Unable to update an activity with an existing claim.");
        }

        if (fundingBlock.getShowCategories() != null && fundingBlock.getShowCategories()) {
            // the external id of the list is the collection of items that are available, once one is selected for adding
            // to the funding block the external id of the
            List<ConfigurableListItem> listItems = refDataService.getConfigurableListItemsByExtID(fundingBlock.getCategoriesExternalId());
            if (listItems.stream().noneMatch(l -> l.getId().equals(activityRequest.getExternalId()))) {
                throw new ValidationException("Unable to recognise the category for the specified item");
            }
        } else {
            ProjectMilestonesBlock milestonesBlock = fundingBlock.getProject().getMilestonesBlock();
            if (milestonesBlock != null) {
                if (milestonesBlock.getMilestones().stream().noneMatch(m -> m.getSummary().equals(activityRequest.getCategoryDescription()))) {
                    throw new ValidationException("Unable to find milestone matching given name");
                }
                // TODO ADD BACK IN WHEN EXTERNAL ID (25805) issue is fixed
//                if (activityRequest.getReferencedId() != null && milestonesBlock.getMilestoneById(activityRequest.getReferencedId()) == null) {
//                    throw new ValidationException("Unable to find milestone matching given external id");
//                }
            }
        }


        FundingActivityGroup activityGroup = null;
        FundingActivity fundingActivity;
        if (activityRequest.getId() != null) {
            fundingActivity = getFundingActivity(blockId, activityRequest.getId());
        }
        else {
            fundingActivity = new FundingActivity(blockId, activityRequest.getYear(), activityRequest.getQuarter(), activityRequest.getExternalId(), activityRequest.getCategoryDescription());

            activityGroup = fundingActivityGroupRepository.findByBlockIdAndYearAndQuarter(blockId, activityRequest.getYear(), activityRequest.getQuarter());
            if (activityGroup == null) {
                activityGroup = fundingActivityGroupRepository.save(new FundingActivityGroup(blockId, activityRequest.getYear(), activityRequest.getQuarter()));
            }

            activityGroup.getActivities().add(fundingActivity);
        }

        fundingActivity.setName(activityRequest.getName());
        updateFundingActivityValue(fundingActivity, projectId, blockId, activityRequest, activityRequest.getCapitalValue(), CAPITAL, null);
        updateFundingActivityValue(fundingActivity, projectId, blockId, activityRequest, activityRequest.getCapitalMatchFundValue(), CAPITAL, MATCH_FUND_CATEGORY);
        updateFundingActivityValue(fundingActivity, projectId, blockId, activityRequest, activityRequest.getRevenueValue(), REVENUE, null);
        updateFundingActivityValue(fundingActivity, projectId, blockId, activityRequest, activityRequest.getRevenueMatchFundValue(), REVENUE, MATCH_FUND_CATEGORY);

        fundingActivity = fundingActivityRepository.save(fundingActivity);

        if (activityGroup != null) {
            fundingActivityGroupRepository.save(activityGroup);
        }

        return fundingActivity;
    }

    private FundingBlock validateFundingBlockLocked(Integer projectId, Integer blockId) {
        Project project = get(projectId);
        NamedProjectBlock fundingBlock = project.getProjectBlockById(blockId);
        if(fundingBlock instanceof FundingBlock) {
            checkForLock(fundingBlock);
        }

        return (FundingBlock) fundingBlock;
    }

    private FundingActivity getFundingActivity(Integer blockId, Integer activityId) {
        FundingActivity activity = fundingActivityRepository.getOne(activityId);
        if (!blockId.equals(activity.getBlockId())) {
            throw new ValidationException("Activity does not belong to the given block");
        }
        return activity;
    }

    private void updateFundingActivityValue(FundingActivity fundingActivity, Integer projectId, Integer blockId, FundingActivityLineItem activityRequest,
                                            BigDecimal value, SpendType spendType, String category) {
        if (value != null) {
            // CREATE
            if (fundingActivity.getLedgerEntry(spendType, category) == null) {
                ProjectLedgerItemRequest ledgerItemRequest = toLedgerItemRequest(projectId, blockId, activityRequest, spendType, category, value);
                ProjectLedgerEntry ledgerEntry = financeService.createOrUpdateProjectLedgerEntry(ledgerItemRequest);
                fundingActivity.getLedgerEntries().add(ledgerEntry);
            }
            // UPDATE
            else {
                fundingActivity.getLedgerEntry(spendType, category).updateValue(value);
            }
        }
        // DELETE
        else if (fundingActivity.getLedgerEntry(spendType, category) != null) {
            fundingActivity.getLedgerEntries().remove(fundingActivity.getLedgerEntry(spendType, category));
        }
    }

    private ProjectLedgerItemRequest toLedgerItemRequest(Integer projectId, Integer blockId, FundingActivityLineItem fundingActivityRequest,
                                                         SpendType spendType, String category, BigDecimal value) {
        ProjectLedgerItemRequest ledgerItemRequest = new ProjectLedgerItemRequest();
        ledgerItemRequest.setProjectId(projectId);
        ledgerItemRequest.setBlockId(blockId);
        ledgerItemRequest.setLedgerType(LedgerType.PAYMENT);
        ledgerItemRequest.setYear(fundingActivityRequest.getYear());
        ledgerItemRequest.setQuarter(fundingActivityRequest.getQuarter());
        ledgerItemRequest.setSpendType(spendType);
        ledgerItemRequest.setExternalId(fundingActivityRequest.getExternalId());
        ledgerItemRequest.setCategory(category);
        ledgerItemRequest.setSubCategory(fundingActivityRequest.getCategoryDescription());
        ledgerItemRequest.setValue(value);
        return ledgerItemRequest;
    }

    public List<StandardAttachment> addFundingActivityEvidence(Integer projectId, Integer blockId, Integer activityId, Integer fileId) {
        FundingBlock fundingBlock = validateFundingBlockLocked(projectId, blockId);

        FundingActivity activity = getFundingActivity(blockId, activityId);

        if (fundingBlock.getMaxEvidenceAttachments() != null && activity.getAttachments().size() >= fundingBlock.getMaxEvidenceAttachments()) {
            throw new ValidationException("Cannot add more than "+fundingBlock.getMaxEvidenceAttachments()+" attachments!");
        }

        AttachmentFile file = fileService.getAttachmentFile(fileId);
        activity.getAttachments().add(new StandardAttachment(file));
        fundingActivityRepository.save(activity);
        return activity.getAttachments();
    }

    public void deleteFundingActivity(Integer projectId, Integer blockId, Integer activityId) {
        FundingBlock fundingBlock = validateFundingBlockLocked(projectId, blockId);

        FundingActivity activity = getFundingActivity(blockId, activityId);

        boolean existingClaim = checkForAnyExistingClaims(fundingBlock, activity.getYear(), activity.getQuarter());

        if (existingClaim) {
            throw new ValidationException("Unable to delete an activity with an existing claim.");
        }

        fundingActivityRepository.delete(activity);

        auditService.auditCurrentUserActivity(String.format("Deleted funding activity %s on project %d block %d", activity.getName(), projectId, blockId));
    }

    public void removeFundingActivityEvidence(Integer projectId, Integer blockId, Integer activityId, Integer attachmentId) {
        validateFundingBlockLocked(projectId, blockId);

        FundingActivity activity = getFundingActivity(blockId, activityId);

        StandardAttachment attachment = activity.getAttachments().stream().filter(a -> a.getId().equals(attachmentId)).findFirst().orElse(null);
        if (attachment != null) {
            activity.getAttachments().remove(attachment);
            fundingActivityRepository.save(activity);
            auditService.auditCurrentUserActivity(String.format("Attachment %s was deleted from Activity %d on project: %d", attachment.getFileName(), activityId, projectId));
        }
        else {
            throw new ValidationException("Attachment with id "+attachmentId+" not found!");
        }
    }

    @Override
    public void handleBlockClone(Project project, Integer originalBlockId, Integer newBlockId) {
        handleProjectClone(project, originalBlockId, null, newBlockId);
    }

    @Override
    public void handleProjectClone(Project fromProject, Integer fromBlockId, Project toProject, Integer toBlockId) {
        FundingBlock fundingBlock = fromProject.getFundingBlock();
        if (fundingBlock != null) {
            Integer toProjectId = toProject != null ? toProject.getId() : null;

            financeService.cloneLedgerEntriesForBlock(fromBlockId, toProjectId, toBlockId, LedgerType.BUDGET);

            for (FundingActivityGroup activityGroup: fundingActivityGroupRepository.findAllByBlockId(fromBlockId)) {
                FundingActivityGroup clonedGroup = activityGroup.clone(toProjectId, toBlockId);
                fundingActivityGroupRepository.save(clonedGroup);
            }
        }
    }

    public void addClaim(Integer projectId, Integer blockId, Integer financialYear, Integer quarter) {
        Project project = get(projectId);
        FundingBlock projectFundingBlock = this.getProjectFundingBlock(projectId, blockId, financialYear);
        checkForLock(projectFundingBlock);

        Claim claim = new Claim();
        claim.setBlockId(blockId);
        claim.setClaimStatus(ClaimStatus.Claimed);
        claim.setClaimTypePeriod(quarter);
        claim.setClaimType(Claim.ClaimType.QUARTER);
        claim.setYear(financialYear);
        project.getFundingBlock().getClaims().add(claim);

        paymentService.updateFundingBlockRecordsWithClaim(projectFundingBlock, claim);

        this.updateProject(project);
    }

    public void deleteClaim(Integer projectId, Integer claimID) {


        Project project = get(projectId);
        FundingBlock fundingBlock = project.getFundingBlock();
        checkForLock(fundingBlock);

        boolean removed = fundingBlock.getClaims().removeIf(c -> c.getId().equals(claimID));

        if (!removed) {
            throw new ValidationException("Unable to find claim to cancel.");
        }
        paymentService.updateFundingBlockRecordsFollowingDeletedClaim(fundingBlock, claimID);

        this.updateProject(project);
    }

    @Override
    public PaymentGroup generatePaymentsForProject(Project project, String approvalRequestedBy) {
        FundingBlock fundingBlock = project.getFundingBlock();
        if (fundingBlock != null && fundingBlock.getApprovalWillCreatePendingPayment()) {
            List<ProjectLedgerEntry> ples = new ArrayList<>();
            for (Claim claim: fundingBlock.getClaimed()) {

                Set<ProjectLedgerEntry> allForClaim = paymentService.findAllForClaim(fundingBlock.getId(), claim.getOriginalId());

                BigDecimal totalRevenue = BigDecimal.ZERO;
                BigDecimal totalCapital = BigDecimal.ZERO;
                for (ProjectLedgerEntry projectLedgerEntry : allForClaim) {
                    if (CAPITAL.equals(projectLedgerEntry.getSpendType()) && projectLedgerEntry.getCategory() == null) {
                        totalCapital = totalCapital.add(projectLedgerEntry.getValue());
                    } else if (REVENUE.equals(projectLedgerEntry.getSpendType()) && projectLedgerEntry.getCategory() == null) {
                        totalRevenue = totalRevenue.add(projectLedgerEntry.getValue());
                    }
                }

                if (totalCapital.compareTo(BigDecimal.ZERO) > 0) {
                    ples.add(createPaymentFor(project, fundingBlock, claim, totalCapital.intValue(), CAPITAL));
                }

                if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                    ples.add(createPaymentFor(project, fundingBlock, claim, totalRevenue.intValue(), REVENUE));
                }
            }

            return paymentService.createPaymentGroup(approvalRequestedBy, ples);
        }

        return null;
    }

    private ProjectLedgerEntry createPaymentFor(Project project, FundingBlock fundingBlock, Claim claim, Integer grant, SpendType spendType) {
        TemplateBlock singleBlockByType = project.getTemplate().getSingleBlockByType(ProjectBlockType.Funding);

        // use MOPAC if available otherwise assume grant
        Set<PaymentSource> paymentSources = singleBlockByType.getPaymentSources();
        if (paymentSources.size() != 1) {
            throw new ValidationException("Unable to determine payment Source for this project. ");
        }
        PaymentSource paymentSource = paymentSources.iterator().next();
        return paymentService.createPayment(project,
                fundingBlock.getId(),
                LedgerType.PAYMENT,
                paymentSource,
                LedgerStatus.Pending,
                spendType,
                "Quarterly",
                String.format("Q%d %s", claim.getClaimTypePeriod(), yearStringShort(claim.getYear())),
                new BigDecimal(grant).negate(),
                environment.now().getYear(),
                environment.now().getMonthValue(),
                null,
                LedgerSource.WebUI);
    }

}
