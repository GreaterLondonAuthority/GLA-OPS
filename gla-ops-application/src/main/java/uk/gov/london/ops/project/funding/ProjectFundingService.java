/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.payment.*;
import uk.gov.london.ops.project.BaseProjectFinanceService;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectPaymentGenerator;
import uk.gov.london.ops.project.StandardAttachment;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockStatus;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.claim.Claim;
import uk.gov.london.ops.project.claim.ClaimStatus;
import uk.gov.london.ops.project.claim.ClaimType;
import uk.gov.london.ops.project.implementation.repository.FundingActivityGroupRepository;
import uk.gov.london.ops.project.implementation.repository.FundingActivityRepository;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.refdata.ConfigurableListItem;
import uk.gov.london.ops.refdata.RefDataService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.ops.framework.calendar.OPSCalendar.yearStringShort;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;
import static uk.gov.london.ops.payment.SpendType.CAPITAL;
import static uk.gov.london.ops.payment.SpendType.REVENUE;
import static uk.gov.london.ops.project.claim.ClaimStatus.*;

@Service
@Transactional
public class ProjectFundingService extends BaseProjectFinanceService implements ProjectPaymentGenerator {

    @Autowired
    private FileService fileService;

    @Autowired
    private RefDataService refDataService;

    @Autowired
    private FundingActivityRepository fundingActivityRepository;

    @Autowired
    private FundingActivityGroupRepository fundingActivityGroupRepository;

    @Override
    protected ProjectBlockType getBlockType() {
        return ProjectBlockType.Funding;
    }

    public List<FundingActivity> findActivitiesByBlockId(Integer blockId) {
        return fundingActivityRepository.findAllByBlockId(blockId);
    }

    public List<Integer> getYearsMilestoneUsed(Integer projectId, Integer blockId, String milestoneName) {
        FundingBlock fundingBlock = this.getProjectFundingBlock(projectId, blockId);
        Set<Integer> years = new HashSet<>();

        for (FundingYearBreakdown funding : fundingBlock.getAllProjectFunding().getFundingByYear().values()) {
            for (FundingSection section : funding.getSections()) {
                if ( section.getMilestone(milestoneName) != null) {
                    years.add(funding.getYear());
                }
            }
        }
        return new ArrayList<>(years);
    }

    public FundingBlock getProjectFundingBlock(Integer projectId, Integer blockId) {
        Project project = get(projectId);
        FundingBlock fundingBlock = getFundingBlock(project, blockId);
        FundingBlock previousFundingBlock = (FundingBlock)project.getLatestApprovedBlock(ProjectBlockType.Funding);

        if (previousFundingBlock != null && !fundingBlock.getId().equals(previousFundingBlock.getId()) && fundingBlock.isLatestVersion()) {
            previousFundingBlock = getFundingBlock(project, previousFundingBlock.getId());
            fundingBlock.getAllProjectFunding().setPreviousFundingTotals(
                    previousFundingBlock.getAllProjectFunding().getTotalProjectFunding());

            for (Integer year : fundingBlock.getAllProjectFunding().getFundingByYear().keySet()) {
                FundingYearBreakdown currentYear = fundingBlock.getAllProjectFunding().getFundingByYear().get(year);
                FundingYearBreakdown previousYear = previousFundingBlock.getAllProjectFunding().getFundingByYear().get(year);

                if (currentYear != null && previousYear != null) {
                    currentYear.setPreviousYearlyTotal(previousYear);
                }
            }
        }
        return fundingBlock;
    }

    public FundingBlock getFundingBlock(Project project, Integer blockId) {

        FundingBlock fundingBlock = (FundingBlock) project.getProjectBlockById(blockId);

        Integer startYear = project.getProgramme().getStartYear();
        Integer endYear = project.getProgramme().getEndYear();
        setPopulatedYears(fundingBlock);

        ProjectFunding pf = new ProjectFunding();
        fundingBlock.setAllProjectFunding(pf);

        List<FundingActivity> blockActivities = findActivitiesByBlockId(blockId);
        Set<Integer> yearsWithActivities;
        if(startYear != null && endYear != null) {
            yearsWithActivities = blockActivities.stream()
                    .filter(activity -> (startYear <= activity.getYear()) && (activity.getYear() <= endYear))
                    .map(a -> a.getYear()).collect(Collectors.toSet());
        } else {
            yearsWithActivities = blockActivities.stream().map(a -> a.getYear()).collect(Collectors.toSet());
        }


        for (Integer populatedYear : yearsWithActivities) {
            List<FundingActivity> activities = blockActivities.stream().filter(a -> a.getYear().equals(populatedYear))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(activities)) {
                pf.getFundingByYear().put(populatedYear, new FundingYearBreakdown(populatedYear, activities));
                initialiseClaimDataForAllSections(fundingBlock);
            }
        }
        return fundingBlock;
    }

    void initialiseClaimDataForAllSections(FundingBlock fundingBlock) {
        List<ProjectLedgerEntry> blockPayments = findAllForBlockId(fundingBlock.getId());


        Map<Integer, FundingYearBreakdown> allProjectFunding = fundingBlock.getAllProjectFunding().getFundingByYear();

        for (Integer year : allProjectFunding.keySet()) {
            FundingYearBreakdown fundingYearBreakdown = allProjectFunding.get(year);
            fundingYearBreakdown.getSections().forEach(section -> {
                initialiseClaimForSection(fundingBlock, year, blockPayments, section);
            });
        }
    }

    // TODO: Delete
    void initialiseClaimDataForSections(FundingBlock fundingBlock, Integer year) {
        List<ProjectLedgerEntry> blockPayments = findAllForBlockId(fundingBlock.getId());

        for (FundingSection section : fundingBlock.getAllProjectFunding().getFundingByYear().get(2017).getSections()) {
            initialiseClaimForSection(fundingBlock, year, blockPayments, section);
        }
    }

    private void initialiseClaimForSection(FundingBlock fundingBlock, Integer year, List<ProjectLedgerEntry> blockPayments,
                                           FundingSection section) {
        Claim claim = fundingBlock.getClaim(year, section.getSectionNumber(), ClaimType.QUARTER);
        initialiseClaimDataFor(fundingBlock, year, section, claim, blockPayments);
        if (Boolean.TRUE.equals(fundingBlock.getCanClaimActivity())) {
            initialiseClaimDataForActivities(fundingBlock, year, section, blockPayments);
        }
    }

    void initialiseClaimDataForActivities(FundingBlock fundingBlock, Integer year, FundingSection section,
                                          List<ProjectLedgerEntry> blockPayments) {
        for (FundingActivityLineItem activity : section.getActivities()) {
            Claim claim = fundingBlock.getClaim(year, section.getSectionNumber(), ClaimType.ACTIVITY, activity.getOriginalId());
            initialiseClaimDataFor(fundingBlock, year, activity, claim, blockPayments);
        }
    }

    void initialiseClaimDataFor(FundingBlock fundingBlock, Integer year, ClaimableFundingEntity claimableEntity, Claim claim,
                                List<ProjectLedgerEntry> blockPayments) {
        Integer quarterValue = claimableEntity.getSectionNumber();

        if (claim != null) {
            claimableEntity.setClaim(claim);
            if (Claimed.equals(claim.getClaimStatus())) {
                List<ProjectLedgerEntry> pendingPayments = blockPayments.stream()
                        .filter(payment -> LedgerStatus.Pending.equals(payment.getLedgerStatus())
                                && payment.getClaimId().equals(claim.getOriginalId()))
                        .collect(Collectors.toList());

                if (pendingPayments.isEmpty()) {
                    claimableEntity.setStatus(FundingClaimStatus.Claimed);
                } else {
                    claimableEntity.setStatus(FundingClaimStatus.Processing);
                }
            } else if (Approved.equals(claim.getClaimStatus())) {
                claimableEntity.setStatus(FundingClaimStatus.Paid);
                claimableEntity.setAuthorisedBy(claim.getAuthorisedBy());
                claimableEntity.setAuthorisedOn(claim.getAuthorisedOn());
            } else if (Withdrawn.equals(claim.getClaimStatus())) {
                //cancelled approved payments should appear like new claims
                initialiseClaimableData(fundingBlock, year, claimableEntity, quarterValue);
            }
        } else {
            initialiseClaimableData(fundingBlock, year, claimableEntity, quarterValue);
        }
    }

    private void initialiseClaimableData(FundingBlock fundingBlock, Integer year, ClaimableFundingEntity claimableEntity, Integer quarterValue) {
        OffsetDateTime now = OffsetDateTime.now();
        int currentQuarter = GlaUtils.getCurrentQuarter(now.getMonthValue());
        int currentFinYear = (currentQuarter == 4) ? now.getYear() - 1 : now.getYear();
        int currentFinYearAndQuarter = (currentFinYear * 10) + currentQuarter;
        int requestedFinYearAndQuarter = (year * 10) + quarterValue;
        claimableEntity.setStatus(FundingClaimStatus.Claimable);

        if (!fundingBlock.getProject().isClaimsEnabled() || !fundingBlock.isComplete()) {
            claimableEntity.setStatus(FundingClaimStatus.NotClaimable);
        } else if (currentFinYearAndQuarter < requestedFinYearAndQuarter || (!fundingBlock.getCanClaimActivity()
                && currentFinYearAndQuarter == requestedFinYearAndQuarter)) {
            // check quarter is in the past
            claimableEntity.setStatus(FundingClaimStatus.NotClaimable);
            claimableEntity.setNotClaimableReason("Claim range is not yet in the past.");
        } else if (!claimableEntity.isMonetaryClaimRequired()) {
            // not claimable if nothing to claim
            claimableEntity.setStatus(FundingClaimStatus.NotClaimable);
            claimableEntity.setNotClaimableReason("Nothing to claim.");
        } else if (fundingBlock.isEvidenceAttachmentsMandatory() && !claimableEntity.isEvidenceAttached()) {
            // evidence must be attached if appropriate
            claimableEntity.setStatus(FundingClaimStatus.NotClaimable);
            claimableEntity.setNotClaimableReason("All evidence must be attached to claim.");
        }
    }

    boolean checkForAnyExistingClaims(FundingBlock fundingBlock, Integer year, Integer quarter) {
        return fundingBlock.getClaims().stream()
                .anyMatch(c -> c.getClaimTypePeriod().equals(quarter)
                        && c.getYear().equals(year)
                        && !Withdrawn.equals(c.getClaimStatus())
                        && !ClaimType.ACTIVITY.equals(c.getClaimType()));
    }

    public FundingActivity createOrUpdateFundingActivity(Integer projectId, Integer blockId,
                                                         FundingActivityLineItem activityRequest) {

        FundingBlock fundingBlock = validateFundingBlockLocked(projectId, blockId);
        checkForCorrectSpendType(fundingBlock, activityRequest);

        boolean existingClaim = checkForAnyExistingClaims(fundingBlock, activityRequest.getYear(), activityRequest.getQuarter());

        if (existingClaim) {
            throw new ValidationException("Unable to update an activity with an existing claim.");
        }

        if (fundingBlock.getShowCategories() != null && fundingBlock.getShowCategories()) {
            // the external id of the list is the collection of items that are available, once one is selected for adding
            // to the funding block the external id of the
            List<ConfigurableListItem> listItems = refDataService
                    .getConfigurableListItemsByExtID(fundingBlock.getCategoriesExternalId());
            if (listItems.stream().noneMatch(l -> l.getId().equals(activityRequest.getExternalId()))) {
                throw new ValidationException("Unable to recognise the category for the specified item");
            }
        } else {
            ProjectMilestonesBlock milestonesBlock = fundingBlock.getProject().getMilestonesBlock();
            if (milestonesBlock != null) {
                if (milestonesBlock.getMilestones().stream()
                        .noneMatch(m -> m.getSummary().equals(activityRequest.getCategoryDescription()))) {
                    throw new ValidationException("Unable to find milestone matching given name");
                }
                // TODO ADD BACK IN WHEN EXTERNAL ID (25805) issue is fixed
                // if (activityRequest.getReferencedId() != null
                // && milestonesBlock.getMilestoneById(activityRequest.getReferencedId()) == null) {
                //     throw new ValidationException("Unable to find milestone matching given external id");
                // }
            }
        }

        FundingActivityGroup activityGroup = null;
        FundingActivity fundingActivity;
        if (activityRequest.getId() != null) {
            fundingActivity = getFundingActivity(blockId, activityRequest.getId());
        } else {
            fundingActivity = new FundingActivity(blockId, activityRequest.getYear(), activityRequest.getQuarter(),
                    activityRequest.getExternalId(), activityRequest.getCategoryDescription());

            activityGroup = fundingActivityGroupRepository
                    .findByBlockIdAndYearAndQuarter(blockId, activityRequest.getYear(), activityRequest.getQuarter());
            if (activityGroup == null) {
                activityGroup = fundingActivityGroupRepository
                        .save(new FundingActivityGroup(blockId, activityRequest.getYear(), activityRequest.getQuarter()));
            }

            activityGroup.getActivities().add(fundingActivity);
        }

        fundingActivity.setName(activityRequest.getName());
        updateFundingActivityValue(fundingActivity, projectId, blockId, activityRequest, activityRequest.getCapitalValue(),
                CAPITAL, null);
        updateFundingActivityValue(fundingActivity, projectId, blockId, activityRequest,
                activityRequest.getCapitalMatchFundValue(), CAPITAL, MATCH_FUND_CATEGORY);
        updateFundingActivityValue(fundingActivity, projectId, blockId, activityRequest, activityRequest.getRevenueValue(),
                REVENUE, null);
        updateFundingActivityValue(fundingActivity, projectId, blockId, activityRequest,
                activityRequest.getRevenueMatchFundValue(), REVENUE, MATCH_FUND_CATEGORY);

        updateFundingActivityYearQuarterAndMilestone(fundingActivity, activityRequest);

        fundingActivity = fundingActivityRepository.save(fundingActivity);

        if (activityGroup != null) {
            fundingActivityGroupRepository.save(activityGroup);
        }

        return fundingActivity;
    }

    private void checkForCorrectSpendType(FundingBlock fundingBlock, FundingActivityLineItem activityRequest) {

        if (!fundingBlock.getShowRevenueGLAFunding() && (activityRequest.getRevenueValue() != null
                && activityRequest.getRevenueValue().compareTo(BigDecimal.ZERO) != 0)) {
            throw new ValidationException("Unable to have Revenue GLA funding on this project");
        }
        if (!fundingBlock.getShowRevenueOtherFunding() && (activityRequest.getRevenueMatchFundValue() != null
                && activityRequest.getRevenueMatchFundValue().compareTo(BigDecimal.ZERO) != 0)) {
            throw new ValidationException("Unable to have Revenue Matched funding on this project");
        }
        if (!fundingBlock.getShowCapitalGLAFunding() && (activityRequest.getCapitalValue() != null
                && activityRequest.getCapitalValue().compareTo(BigDecimal.ZERO) != 0)) {
            throw new ValidationException("Unable to have Capital GLA funding on this project");
        }
        if (!fundingBlock.getShowCapitalOtherFunding() && (activityRequest.getCapitalMatchFundValue() != null
                && activityRequest.getCapitalMatchFundValue().compareTo(BigDecimal.ZERO) != 0)) {
            throw new ValidationException("Unable to have Capital Matched funding on this project");
        }
    }

    private FundingBlock validateFundingBlockLocked(Integer projectId, Integer blockId) {
        Project project = get(projectId);
        NamedProjectBlock fundingBlock = project.getProjectBlockById(blockId);
        if (fundingBlock instanceof FundingBlock) {
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

    private void updateFundingActivityYearQuarterAndMilestone(FundingActivity fundingActivity, FundingActivityLineItem activityRequest) {
        fundingActivity.setYear(activityRequest.getYear());
        fundingActivity.setQuarter(activityRequest.getQuarter());
        fundingActivity.setCategoryDescription(activityRequest.getCategoryDescription());
        fundingActivity.setExternalId(activityRequest.getExternalId());
    }

    private void updateFundingActivityValue(FundingActivity fundingActivity, Integer projectId, Integer blockId,
                                            FundingActivityLineItem activityRequest,
                                            BigDecimal value, SpendType spendType, String category) {
        if (value != null) { // CREATE
            if (fundingActivity.getLedgerEntry(spendType, category) == null) {
                ProjectLedgerItemRequest ledgerItemRequest = toLedgerItemRequest(projectId, blockId, activityRequest, spendType,
                        category, value);
                ProjectLedgerEntry ledgerEntry = financeService.createOrUpdateProjectLedgerEntry(ledgerItemRequest);
                fundingActivity.getLedgerEntries().add(ledgerEntry);
            } else { // UPDATE
                fundingActivity.getLedgerEntry(spendType, category).updateValue(value);
            }
        } else if (fundingActivity.getLedgerEntry(spendType, category) != null) { // DELETE
            fundingActivity.getLedgerEntries().remove(fundingActivity.getLedgerEntry(spendType, category));
        }
    }

    private ProjectLedgerItemRequest toLedgerItemRequest(Integer projectId, Integer blockId,
                                                         FundingActivityLineItem fundingActivityRequest,
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

    public List<StandardAttachment> addFundingActivityEvidence(Integer projectId, Integer blockId, Integer activityId,
                                                               Integer fileId) {
        FundingBlock fundingBlock = validateFundingBlockLocked(projectId, blockId);

        FundingActivity activity = getFundingActivity(blockId, activityId);

        if (fundingBlock.getMaxEvidenceAttachments() != null && activity.getAttachments().size() >= fundingBlock
                .getMaxEvidenceAttachments()) {
            throw new ValidationException("Cannot add more than " + fundingBlock.getMaxEvidenceAttachments() + " attachments!");
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

        auditService.auditCurrentUserActivity(
                String.format("Deleted funding activity %s on project %d block %d", activity.getName(), projectId, blockId));
    }

    public void removeFundingActivityEvidence(Integer projectId, Integer blockId, Integer activityId, Integer attachmentId) {
        validateFundingBlockLocked(projectId, blockId);

        FundingActivity activity = getFundingActivity(blockId, activityId);

        StandardAttachment attachment = activity.getAttachments().stream().filter(a -> a.getId().equals(attachmentId)).findFirst()
                .orElse(null);
        if (attachment != null) {
            activity.getAttachments().remove(attachment);
            fundingActivityRepository.save(activity);
            auditService.auditCurrentUserActivity(
                    String.format("Attachment %s was deleted from Activity %d on project: %d", attachment.getFileName(),
                            activityId, projectId));
        } else {
            throw new ValidationException("Attachment with id " + attachmentId + " not found!");
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

            for (FundingActivityGroup activityGroup : fundingActivityGroupRepository.findAllByBlockId(fromBlockId)) {
                FundingActivityGroup clonedGroup = activityGroup.clone(toProjectId, toBlockId);
                fundingActivityGroupRepository.save(clonedGroup);
            }
        }
    }

    public void addClaim(Integer projectId, Integer blockId, Integer financialYear, Integer quarter, List<Integer> activityIds) {
        Project project = get(projectId);
        FundingBlock projectFundingBlock = this.getProjectFundingBlock(projectId, blockId);
        projectFundingBlock.getAllProjectFunding().getFundingByYear().get(financialYear);
        checkForLock(projectFundingBlock);

        List<Claim> claims = new ArrayList<>();
        if (Boolean.TRUE.equals(projectFundingBlock.getCanClaimActivity()) && CollectionUtils.isNotEmpty(activityIds)) {
            for (Integer activityId : activityIds) {
                Claim claim = createAuditedClaim(blockId, financialYear, quarter, Claimed, ClaimType.ACTIVITY);
                FundingActivity activity = fundingActivityRepository.findById(activityId).orElse(null);
                BigDecimal capitalClaimValue = activity.getCapitalMainValue() != null ? activity.getCapitalMainValue() : BigDecimal.ZERO;
                BigDecimal revenueClaimValue = activity.getRevenueMainValue() != null ? activity.getRevenueMainValue() : BigDecimal.ZERO;
                claim.setAmount(capitalClaimValue.add(revenueClaimValue));
                claim.setEntityId(activity.getOriginalId());
                claims.add(claim);
            }
        } else {
            Claim claim = createAuditedClaim(blockId, financialYear, quarter, Claimed, ClaimType.QUARTER);
            claim.setAmount(getClaimValue(blockId, financialYear, quarter));
            claims.add(claim);
        }

        project.getFundingBlock().getClaims().addAll(claims);

        paymentService.updateFundingBlockRecordsWithClaim(projectFundingBlock, claims);

        this.updateProject(project);
    }

    BigDecimal getClaimValue(Integer blockId, Integer year, Integer quarter) {
        List<FundingActivity> activities = fundingActivityRepository.findAllByBlockIdAndYearAndQuarter(blockId, year, quarter);
        BigDecimal value = BigDecimal.ZERO;
        for (FundingActivity activity : activities) {
            if (activity.getCapitalMainValue() != null) {
                value = value.add(activity.getCapitalMainValue());
            }
            if (activity.getRevenueMainValue() != null) {
                value = value.add(activity.getRevenueMainValue());
            }
        }
        return value;
    }

    public void updateClaimStatuses(Integer projectId, List<Integer> claimIds, ClaimStatus targetStatus, String reason) {
        Project project = get(projectId);
        FundingBlock fundingBlock = project.getFundingBlock();

        if (!environment.isTestEnvironment() && targetStatus == Approved) {
            throw new ValidationException("Should not be approving claims using this method");
        }

        for (Integer claimId : claimIds) {
            fundingBlock.getClaims().stream().forEach(c -> {
                if (c.getId().equals(claimId)) {
                    c.setClaimStatus(targetStatus);
                    //deals with legacy claims where amount was not set
                    if (targetStatus == Withdrawn && c.getClaimType().equals(ClaimType.ACTIVITY) && c.getAmount() == null) {
                        FundingActivity activity = fundingActivityRepository.findById(c.getEntityId()).orElse(null);
                        BigDecimal capitalClaimValue = activity.getCapitalMainValue() != null ? activity.getCapitalMainValue() : BigDecimal.ZERO;
                        BigDecimal revenueClaimValue = activity.getRevenueMainValue() != null ? activity.getRevenueMainValue() : BigDecimal.ZERO;
                        c.setAmount(capitalClaimValue.add(revenueClaimValue));
                    }
                    if (targetStatus == Withdrawn && c.getClaimType().equals(ClaimType.QUARTER) && c.getAmount() == null) {
                        c.setAmount(getClaimValue(fundingBlock.getId(), c.getYear(), c.getClaimTypePeriod()));
                    }
                }
            });
        }

        this.updateProject(project);

        if (targetStatus == Withdrawn) {
            project.handleEvent(new OpsEvent(EventType.CancelApprovedClaim, "Claim cancelled", null, reason, userService.currentUser()));
            performProjectUpdate(project);
            auditService.auditCurrentUserActivity("Claim cancelled - " + reason);
        }
    }

    private Claim createAuditedClaim(Integer blockId, Integer financialYear, Integer quarter, ClaimStatus status,
                                     ClaimType type) {
        Claim claim = new Claim(blockId, financialYear, quarter, status, type);
        claim.setClaimedOn(environment.now());
        claim.setClaimedBy(userService.currentUsername());
        return claim;
    }

    public void deleteClaim(Integer projectId, List<Integer> claimIDs) {
        Project project = get(projectId);
        FundingBlock fundingBlock = project.getFundingBlock();
        checkForLock(fundingBlock);

        for (Integer claimID : claimIDs) {
            Claim toRemove = fundingBlock.getClaims().stream().filter(c -> c.getId().equals(claimID)).findFirst().orElse(null);
            if (toRemove == null) {
                throw new ValidationException("Unable to find claim to cancel.");
            } else if (Withdrawn.equals(toRemove.getClaimStatus()) || Approved.equals(toRemove.getClaimStatus())) {
                throw new ValidationException("`Should not delete previously approved claims`");
            }

            fundingBlock.getClaims().remove(toRemove);
            paymentService.updateFundingBlockRecordsFollowingDeletedClaim(fundingBlock, claimID);
        }

        this.updateProject(project);
    }

    @Override
    public PaymentGroup generatePaymentsForProject(Project project, String approvalRequestedBy) {
        FundingBlock fundingBlock = project.getFundingBlock();
        if (fundingBlock != null && (fundingBlock.getApprovalWillCreatePendingPayment() || fundingBlock.getApprovalWillCreatePendingReclaim())) {
            List<ProjectLedgerEntry> ples = new ArrayList<>();
            for (Claim claim : fundingBlock.getClaimed()) {
                Set<ProjectLedgerEntry> allForClaim = paymentService.findAllForClaim(fundingBlock.getId(), claim.getOriginalId());
                Set<ProjectLedgerEntry> historicEntriesForClaim = getHistoricEntriesForClaim(project, fundingBlock, claim);
                allForClaim.addAll(historicEntriesForClaim);

                BigDecimal totalRevenue = BigDecimal.ZERO;
                BigDecimal totalCapital = BigDecimal.ZERO;
                for (ProjectLedgerEntry projectLedgerEntry : allForClaim) {
                    if (CAPITAL.equals(projectLedgerEntry.getSpendType()) && (projectLedgerEntry.getCategory() == null || projectLedgerEntry.getCategory().equals("Activity") || projectLedgerEntry.getCategory().equals("Quarterly"))) {
                        totalCapital = totalCapital.add(projectLedgerEntry.getValue());
                    } else if (REVENUE.equals(projectLedgerEntry.getSpendType()) && (projectLedgerEntry.getCategory() == null || projectLedgerEntry.getCategory().equals("Activity") || projectLedgerEntry.getCategory().equals("Quarterly"))) {
                        totalRevenue = totalRevenue.add(projectLedgerEntry.getValue());
                    }
                }
                if (totalCapital.compareTo(BigDecimal.ZERO) > 0) {
                    ples.add(createPaymentFor(project, fundingBlock, claim, totalCapital, CAPITAL));
                }
                if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                    ples.add(createPaymentFor(project, fundingBlock, claim, totalRevenue, REVENUE));
                }

                ProjectLedgerEntry initialClaim = historicEntriesForClaim.stream().findFirst().orElse(null);
                if (totalCapital.compareTo(BigDecimal.ZERO) < 0) {
                    ples.add(paymentService.createReclaim(historicEntriesForClaim, totalCapital.negate(), claim.getId(), fundingBlock.getId(), CAPITAL));
                }
                if (totalRevenue.compareTo(BigDecimal.ZERO) < 0) {
                    ples.add(paymentService.createReclaim(historicEntriesForClaim, totalRevenue.negate(), claim.getId(), fundingBlock.getId(), REVENUE));
                }
            }
            return paymentService.createPaymentGroup(approvalRequestedBy, ples);
        }
        return null;
    }

    /**
     *
     * check historic payments that could have been paid against this claim before it was cancelled and add
     * them to the total claim. Paid claims will have negative values so adding as they are will work out difference
     * @param project project
     * @param fundingBlock the current version of the funding block
     * @param claim claim
     */
    Set<ProjectLedgerEntry> getHistoricEntriesForClaim(Project project, FundingBlock fundingBlock, Claim claim) {
        Set<ProjectLedgerEntry> historicPayments = new HashSet<>();
        List<FundingBlock> historicBlocks = project.getBlocksByTypeAndDisplayOrder(ProjectBlockType.Funding, fundingBlock.getDisplayOrder()).stream()
                .filter(b -> b instanceof FundingBlock)
                .map(FundingBlock.class::cast)
                .collect(Collectors.toList());
        for (FundingBlock historicBlock : historicBlocks) {
            if (historicBlock != null) {
                List<Claim> allPreviouslyApprovedClaims = historicBlock.getAllPreviouslyApprovedClaims(claim);
                for (Claim previousClaim : allPreviouslyApprovedClaims) {
                    if (previousClaim != null) {
                        Set<ProjectLedgerEntry> previousPLEs = paymentService.findAllForClaim(historicBlock.getId(), previousClaim.getOriginalId());
                        Set<ProjectLedgerEntry> previouslyApprovedPLEs = previousPLEs.stream()
                                .filter(ple -> LedgerStatus.getApprovedPaymentStatuses().contains(ple.getLedgerStatus()))
                                .collect(Collectors.toSet());
                        historicPayments.addAll(previouslyApprovedPLEs);
                    }
                }
            }
        }
        return historicPayments;
    }

    private ProjectLedgerEntry createPaymentFor(Project project, FundingBlock fundingBlock, Claim claim, BigDecimal grant,
                                                SpendType spendType) {
        TemplateBlock singleBlockByType = project.getTemplate().getSingleBlockByType(ProjectBlockType.Funding);
        // use MOPAC if available otherwise assume grant
        Set<String> paymentSources = singleBlockByType.getPaymentSources();
        if (paymentSources.size() != 1) {
            throw new ValidationException("Unable to determine payment Source for this project. ");
        }
        String paymentSource = paymentSources.iterator().next();
        ProjectLedgerEntry payment = paymentService.createPayment(project,
                fundingBlock.getId(),
                LedgerType.PAYMENT,
                paymentSource,
                LedgerStatus.Pending,
                spendType,
                ClaimType.ACTIVITY.equals(claim.getClaimType()) ? LedgerCategory.Activity.name()
                        : LedgerCategory.Quarterly.name(),
                String.format("Q%d %s", claim.getClaimTypePeriod(), yearStringShort(claim.getYear())),
                grant.negate(),
                environment.now().getYear(),
                environment.now().getMonthValue(),
                null,
                null,
                LedgerSource.WebUI);
        payment.setClaimId(claim.getId());
        payment.setComments(paymentService.getPaymentComments(claim.getId()));
        return payment;
    }

    private List<ProjectLedgerEntry> findAllForBlockId(Integer blockId) {
        return paymentService.findAllForBlockId(blockId);
    }

}
