/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import static uk.gov.london.common.GlaUtils.addBigDecimals;
import static uk.gov.london.common.skills.SkillsGrantType.AEB_GRANT;
import static uk.gov.london.common.skills.SkillsGrantType.AEB_LEARNER_SUPPORT;
import static uk.gov.london.common.skills.SkillsGrantType.AEB_PROCURED;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.RECLAIMED_PAYMENT;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.SUPPLEMENTARY_PAYMENT;
import static uk.gov.london.ops.project.claim.ClaimStatus.Claimed;
import static uk.gov.london.ops.project.claim.Claimable.CLAIM_STATUS_OVER_PAID;
import static uk.gov.london.ops.project.claim.Claimable.CLAIM_STATUS_PARTLY_PAID;
import static uk.gov.london.ops.project.skills.LearningGrantEntryType.DELIVERY;
import static uk.gov.london.ops.project.skills.LearningGrantEntryType.SUPPORT;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.payment.LedgerSource;
import uk.gov.london.ops.payment.LedgerStatus;
import uk.gov.london.ops.payment.LedgerType;
import uk.gov.london.ops.payment.PaymentGroup;
import uk.gov.london.ops.payment.PaymentSummary;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.project.BaseProjectService;
import uk.gov.london.ops.project.EnrichmentRequiredListener;
import uk.gov.london.ops.project.PostCloneNotificationListener;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectPaymentGenerator;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockService;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.claim.Claim;
import uk.gov.london.ops.project.claim.ClaimGenerator;
import uk.gov.london.ops.project.claim.ClaimType;
import uk.gov.london.ops.project.template.domain.FundingClaimCategory;
import uk.gov.london.ops.project.template.domain.FundingClaimCategoryMatchingRule;
import uk.gov.london.ops.project.template.domain.FundingClaimsTemplateBlock;
import uk.gov.london.ops.project.template.domain.LearningGrantTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;

@Service
@Transactional
public class ProjectSkillsService extends BaseProjectService implements EnrichmentRequiredListener,
        ClaimGenerator<LearningGrantBlock>, ProjectPaymentGenerator, PostCloneNotificationListener {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    SkillsPaymentScheduler scheduler;

    @Autowired
    private SkillsService skillsService;

    @Autowired
    private ProjectBlockService projectBlockService;

    public LearningGrantBlock getLearningGrant(Integer projectId, Integer blockId) {
        Project project = get(projectId);
        LearningGrantBlock learningGrantBlock = (LearningGrantBlock) project.getProjectBlockById(blockId);
        enrichLearningGrantBlock(learningGrantBlock, project);
        return learningGrantBlock;
    }

    private void loadPaymentsFor(Project project, LearningGrantBlock learningGrantBlock) {
        List<PaymentSummary> payments = paymentService.getApprovedPaymentsForProject(project.getId());
        for (LearningGrantEntry entry : learningGrantBlock.getLearningGrantEntries()) {
            List<PaymentSummary> entryPayments = payments.stream()
                    .filter(p -> Objects.equals(entry.getOriginalId(), p.getExternalId())).collect(Collectors.toList());
            entry.setPayments(entryPayments);
        }
    }

    public FundingClaimsBlock getFundingClaims(Integer projectId) {
        Project project = get(projectId);
        FundingClaimsBlock fundingClaimsBlock = project.getFundingClaimsBlock();
        LearningGrantBlock learningGrantBlock = project.getLearningGrantBlock();
        fundingClaimsBlock.enrichFromBlock(learningGrantBlock);
        enrichFundingClaimsBlock(project, fundingClaimsBlock);

        return fundingClaimsBlock;
    }

    void enrichFundingClaimsBlock(Project project, FundingClaimsBlock block) {

        FundingClaimsTemplateBlock templateBlock = (FundingClaimsTemplateBlock)
                block.getProject().getTemplate().getSingleBlockByType(ProjectBlockType.FundingClaims);

        Set<SkillsFundingSummaryEntity> allMatching = skillsService
                .findAllMatching(project.getOrganisation().getUkprn(), block.getGrantType());

        for (FundingClaimsEntry fundingClaimsEntry : block.getFundingClaimsEntries()) {
            FundingClaimCategory fundingClaimCategoryById = templateBlock
                    .getFundingClaimCategoryById(fundingClaimsEntry.getCategoryId());
            fundingClaimCategoryById.setActualsEditable(fundingClaimCategoryById.isActualsEditable());

            if (!fundingClaimCategoryById.isActualsEditable() && fundingClaimCategoryById.getMatchingRules() != null) {
                fundingClaimsEntry.setActualsEditable(false);
                BigDecimal total = sumMatchingValues(fundingClaimsEntry, allMatching,
                        fundingClaimCategoryById.getMatchingRules());
                fundingClaimsEntry.setActualDelivery(total);
            } else {
                fundingClaimsEntry.setActualsEditable(true);

            }
        }
    }

    BigDecimal sumMatchingValues(FundingClaimsEntry fundingClaimsEntry, Set<SkillsFundingSummaryEntity> allMatching,
            Set<FundingClaimCategoryMatchingRule> matchingRules) {
        Set<SkillsFundingSummaryEntity> relevantEntries = allMatching.stream()
                .filter(m -> m.getAcademicYear().equals(fundingClaimsEntry.getAcademicYear())
                        && m.getPeriod().equals(fundingClaimsEntry.getPeriod())).collect(Collectors.toSet());
        Set<FundingClaimCategoryMatchingRule> positiveMatches = matchingRules.stream()
                .filter(FundingClaimCategoryMatchingRule::isPositiveMatch).collect(Collectors.toSet());
        Set<FundingClaimCategoryMatchingRule> negativeMatches = matchingRules.stream().filter(f -> !f.isPositiveMatch())
                .collect(Collectors.toSet());

        if (!negativeMatches.isEmpty() && !positiveMatches.isEmpty()) {
            throw new ValidationException("Check template configuration can't have both positive and negative matches");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (SkillsFundingSummaryEntity relevantEntry : relevantEntries) {
            String source = relevantEntry.getSource();
            String category = relevantEntry.getCategory();
            boolean shouldAdd = false;
            if (!positiveMatches.isEmpty()) {
                shouldAdd = isRuleMatched(positiveMatches, source, category);
            } else if (!negativeMatches.isEmpty()) {
                shouldAdd = !isRuleMatched(negativeMatches, source, category);
            }

            if (shouldAdd) {
                total = total.add(relevantEntry.getTotalPayment());
            }
        }
        return total;
    }

    boolean isRuleMatched(Set<FundingClaimCategoryMatchingRule> matches, String source, String category) {
        boolean ruleMatched = false;
        for (FundingClaimCategoryMatchingRule match : matches) {
            if (match.getSource().equals(source) && match.getPatternToMatch().equals(category)) {
                ruleMatched = true;
            }
        }
        return ruleMatched;
    }


    public LearningGrantBlock updateLearningGrant(Integer projectId, Integer blockId, Integer year,
            LearningGrantBlock updatedBlock, boolean releaseLock) {
        Project project = get(projectId);
        LearningGrantBlock existingBlock = project.getLearningGrantBlock();
        validateOnlyOneAllocationChangedPerApproval(existingBlock, updatedBlock);
        checkForLock(existingBlock);
        existingBlock.merge(updatedBlock);

        // this will allow to update values like percentage, payment due, etc.
        enrichLearningGrantBlock(existingBlock, project);

        releaseOrRefreshLock(existingBlock, releaseLock);
        updateProject(project);

        return existingBlock;
    }

    private void validateOnlyOneAllocationChangedPerApproval(LearningGrantBlock existingBlock, LearningGrantBlock updatedBlock) {
        if (existingBlock.getGrantType().equals(AEB_PROCURED)) {
            for (LearningGrantAllocation updated : updatedBlock.getAllocations()) {
                LearningGrantAllocation existing = existingBlock.getAllocation(updated.getYear());
                boolean deliveryChanged = ObjectUtils.compare(existing.getAllocation(), updated.getAllocation()) != 0;
                boolean supportChanged = ObjectUtils.compare(existing.getLearnerSupportAllocation(),
                        updated.getLearnerSupportAllocation()) != 0;

                if (deliveryChanged && supportChanged) {
                    throw new ValidationException("Cannot change both allocations per approval.");
                } else {
                    updated.setDeliveryAllocationEditingInProgress(deliveryChanged);
                    updated.setSupportAllocationEditingInProgress(supportChanged);
                }
            }
        }
    }

    public FundingClaimsBlock updateFundingClaimsEntry(Integer projectId, FundingClaimsEntry updatedEntry, boolean releaseLock) {
        Project project = get(projectId);
        FundingClaimsBlock existingBlock = project.getFundingClaimsBlock();

        existingBlock.getFundingClaimsEntries().stream().filter(entry -> entry.getId().equals(updatedEntry.getId())).findFirst()
                .ifPresent(fundingClaimsEntry -> {
                    fundingClaimsEntry.setActualDelivery(updatedEntry.getActualDelivery());
                    fundingClaimsEntry.setForecastDelivery(updatedEntry.getForecastDelivery());

                    fundingClaimsEntry.getContractTypeFundingEntries().clear();
                    fundingClaimsEntry.getContractTypeFundingEntries().addAll(updatedEntry.getContractTypeFundingEntries());

                });

        releaseOrRefreshLock(existingBlock, releaseLock);
        updateProject(project);

        return existingBlock;
    }

    public FundingClaimsBlock updateFundingClaimsBlock(Integer projectId, FundingClaimsBlock updatedBlock, boolean releaseLock) {
        Project project = get(projectId);
        FundingClaimsBlock existingBlock = project.getFundingClaimsBlock();
        checkForLock(existingBlock);
        existingBlock.merge(updatedBlock);

        releaseOrRefreshLock(existingBlock, releaseLock);
        updateProject(project);

        return existingBlock;
    }

    public void enrichLearningGrantBlock(LearningGrantBlock learningGrantBlock, Project project) {
        Optional<NamedProjectBlock> previousBlock = Optional.ofNullable(projectBlockService.getLearningGrantBlockByProjectIdVersionNumber(
                project.getId(), learningGrantBlock.getVersionNumber() - 1, learningGrantBlock.getDisplayOrder()));
        if (previousBlock.isPresent()) {
            validateOnlyOneAllocationChangedPerApproval((LearningGrantBlock) previousBlock.get(), learningGrantBlock);
        }

        Integer startYear = learningGrantBlock.getStartYear();
        for (int j = 0; j < learningGrantBlock.getNumberOfYears(); j++) {
            LearningGrantAllocation yearAllocation = learningGrantBlock.getAllocation(startYear + j);

            List<LearningGrantEntry> deliveryEntries = learningGrantBlock.getLearningGrantEntries(DELIVERY, startYear + j);
            Map<Integer, SkillsPaymentProfile> profiles = getPaymentProfilesAsMap(learningGrantBlock.getGrantType(),
                    startYear + j);
            Map<Integer, SkillsFundingGroupedSummary> fundingSummaryMap = getFundingSummaryMapForAcademicYear(
                    project.getOrganisation().getUkprn(), startYear + j, learningGrantBlock.getGrantType());
            enrichLearningGrantEntries(deliveryEntries, profiles, fundingSummaryMap, yearAllocation.getAllocation(),
                    learningGrantBlock.getGrantType());

            if (learningGrantBlock.getGrantType().equals(AEB_PROCURED)) {
                List<LearningGrantEntry> learnerSupportEntries = learningGrantBlock
                        .getLearningGrantEntries(SUPPORT, startYear + j);
                Map<Integer, SkillsPaymentProfile> learnerSupportProfiles = getPaymentProfilesAsMap(AEB_LEARNER_SUPPORT,
                        startYear + j);
                enrichLearningGrantEntries(learnerSupportEntries, learnerSupportProfiles, null,
                        yearAllocation.getLearnerSupportAllocation(), AEB_PROCURED);
            }

            checkForExistingReturn(project, yearAllocation, deliveryEntries);
        }
        loadPaymentsFor(project, learningGrantBlock);
    }

    void checkForExistingReturn(Project project, LearningGrantAllocation yearAllocation,
            List<LearningGrantEntry> deliveryEntries) {
        boolean canManuallyClaimP14 = ((LearningGrantTemplateBlock) project.getTemplate()
                .getSingleBlockByType(ProjectBlockType.LearningGrant)).getCanManuallyClaimP14();

        if (canManuallyClaimP14) {
            deliveryEntries.stream()
                    .filter(l -> l.getPeriod() == 14)
                    .findFirst()
                    .ifPresent(
                            learningGrantEntry -> {
                                learningGrantEntry.setCanManuallyClaimValue(
                                        learningGrantEntry.getCumulativeEarnings() != null);
                            });
        }
    }

    private Map<Integer, SkillsPaymentProfile> getPaymentProfilesAsMap(SkillsGrantType grantType, Integer selectedYear) {
        return skillsService.getSkillsPaymentProfiles(grantType, selectedYear).stream()
                .collect(Collectors.toMap(SkillsPaymentProfile::getPeriod, Function.identity()));
    }

    private Map<Integer, SkillsFundingGroupedSummary> getFundingSummaryMapForAcademicYear(Integer ukprn, Integer academicYear,
            SkillsGrantType grantType) {
        return skillsService.getGroupedSummaries(ukprn, academicYear, grantType).stream()
                .collect(Collectors.toMap(SkillsFundingGroupedSummary::getPeriod, Function.identity()));
    }

    void enrichLearningGrantEntries(List<LearningGrantEntry> entries,
            Map<Integer, SkillsPaymentProfile> profiles,
            Map<Integer, SkillsFundingGroupedSummary> fundingSummaryMap,
            BigDecimal allocation,
            SkillsGrantType grantType) {
        boolean profileValid = isProfileValid(profiles);

        for (LearningGrantEntry entry : entries) {
            entry.setGrantType(grantType);

            if (profileValid) {
                if (profiles.get(entry.getPeriod()) != null) {
                    entry.setPercentage(profiles.get(entry.getPeriod()).getPercentage());
                }
                if (entry.getPaymentDate() == null && profiles.get(entry.getPeriod()) != null) {
                    entry.setPaymentDate(profiles.get(entry.getPeriod()).getPaymentDate());
                }
            }

            if (allocation == null) {
                entry.setAllocation(null);
            } else if (entry.getPercentage() != null) {
                entry.setAllocation(entry.getPercentage().divide(new BigDecimal(100)).multiply(allocation));
            }

            if (fundingSummaryMap != null && fundingSummaryMap.get(entry.getPeriod()) != null) {
                entry.setCumulativeEarnings(fundingSummaryMap.get(entry.getPeriod()).getTotalPayment());
            }
        }

        updateCumulativeValues(entries, grantType);
    }

    private boolean isProfileValid(Map<Integer, SkillsPaymentProfile> profiles) {
        return profiles.values().stream()
                .map(SkillsPaymentProfile::getPercentage)
                .filter(bd -> bd != null)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO)
                .compareTo(new BigDecimal(100)) == 0;
    }


    private void updateCumulativeValues(List<LearningGrantEntry> entries, SkillsGrantType grantType) {
        entries.sort(Comparator.comparing(LearningGrantEntry::getPeriod));

        for (int i = 0; i < entries.size(); i++) {
            LearningGrantEntry entry = entries.get(i);
            entry.setCumulativeAllocation(
                    i > 0 ? addBigDecimals(entries.get(i - 1).getCumulativeAllocation(), entry.getAllocation())
                            : entry.getAllocation());
            entry.setCumulativePayment(
                    i > 0 ? addBigDecimals(entries.get(i - 1).getPaymentDue(), entries.get(i - 1).getCumulativePayment())
                            : new BigDecimal(0));

            if (AEB_PROCURED.equals(grantType) && DELIVERY.equals(entry.getType())) {
                setProcuredDeliveryPaymentDue(entries, i);
            } else {
                if (!entry.isReturn()) {
                    entry.setPaymentDue(entry.getAllocation());
                }
            }
        }
    }

    /**
     * The procured delivery allocation will have 2 caps amount to be used in calculation of payment due as follows: - month 8
     * cumulative allocation as a financial year cap - month 12 cumulative allocation as an academic year cap The payment due
     * amount must not exceed the financial year cap amount for months 1-8 and must not exceed the academic year cap amount for
     * months 9-12.
     **/
    private void setProcuredDeliveryPaymentDue(List<LearningGrantEntry> entries, int i) {
        BigDecimal financialYearCAP = entries.stream().filter(e -> e.getPeriod().equals(8)).findFirst().get()
                .getCumulativeAllocation();
        BigDecimal academicYearCAP = entries.stream().filter(e -> e.getPeriod().equals(12)).findFirst().get()
                .getCumulativeAllocation();

        LearningGrantEntry previousEntry = i > 0 ? entries.get(i - 1) : null;
        LearningGrantEntry entry = entries.get(i);
        if (i < 8) {
            entry.setPaymentDue(getCappedPaymentDue(financialYearCAP, previousEntry, entry));
        } else {
            entry.setPaymentDue(getCappedPaymentDue(academicYearCAP, previousEntry, entry));
        }
    }

    BigDecimal getCappedPaymentDue(BigDecimal cap, LearningGrantEntry previousEntry, LearningGrantEntry entry) {
        BigDecimal paymentDueInTheory = null;
        if (previousEntry == null) {
            paymentDueInTheory = entry.getCumulativeEarnings();
        } else if (entry.getCumulativeEarnings() != null) {
            paymentDueInTheory = entry.getCumulativeEarnings().subtract(entry.getCumulativePayment());
        }

        BigDecimal maxCappedPaymentDue = cap != null ? cap.subtract(entry.getCumulativePayment()) : null;
        BigDecimal cappedPaymentDue =
                (paymentDueInTheory != null && maxCappedPaymentDue != null) ? paymentDueInTheory.min(maxCappedPaymentDue) : null;
        return cappedPaymentDue;
    }

    @Override
    public void enrichProject(Project project, boolean enrichmentForComparison) {

        Optional<NamedProjectBlock> learningGrantBlock = project.getProjectBlocksSorted().stream()
                .filter(pb -> pb.getBlockType().equals(ProjectBlockType.LearningGrant)).findFirst();
        if (learningGrantBlock.isPresent()) {
            enrichLearningGrantBlock((LearningGrantBlock) learningGrantBlock.get(), project);
        }

        Optional<NamedProjectBlock> fundingClaimsNamedProjectBlock = project.getProjectBlocksSorted().stream()
                .filter(pb -> pb.getBlockType().equals(ProjectBlockType.FundingClaims)).findFirst();
        if (fundingClaimsNamedProjectBlock.isPresent()) {
            FundingClaimsBlock fundingClaimsBlock = (FundingClaimsBlock) fundingClaimsNamedProjectBlock.get();
            fundingClaimsBlock.enrichFromBlock(learningGrantBlock.get());
            enrichFundingClaimsBlock(project, fundingClaimsBlock);
        }
    }

    public void runScheduler(String date) {
        scheduler.schedulePayments(date);
    }

    @Override
    public void generateClaim(Project project, LearningGrantBlock learningGrantBlock, Claim claim) {
        if (!learningGrantBlock.isClaimable()) {
            throw new ValidationException("Claims are not enabled at this stage");
        }

        if (AEB_PROCURED != learningGrantBlock.getGrantType() && AEB_GRANT != learningGrantBlock.getGrantType()) {
            throw new ValidationException("Claiming grant type is not supported " + learningGrantBlock.getGrantType());
        }

        if (claim.getEntityId() == null) {
            throw new ValidationException("Missing entity id");
        }

        enrichLearningGrantBlock(learningGrantBlock, project);

        LearningGrantEntry entry = learningGrantBlock.getLearningGrantEntries().stream()
                .filter(e -> e.getOriginalId().equals(claim.getEntityId())).findFirst().orElse(null);

        if (entry == null || !entry.getActualYear().equals(claim.getYear()) || !GlaUtils
                .nullSafeEquals(entry.getActualMonth(), claim.getClaimTypePeriod())) {
            throw new ValidationException("Learning grant entry not found: " + claim.getEntityId());
        }

        Set<Claim> existingClaims = learningGrantBlock.getClaims().stream()
                .filter(c -> c.getEntityId().equals(claim.getEntityId())).collect(Collectors.toSet());

        if (!entry.isReturn() && !existingClaims.isEmpty()) {
            throw new ValidationException("Claim already exists");
        }

        if (entry.getPaymentDue() == null || entry.getPaymentDue().equals(BigDecimal.ZERO)) {
            throw new ValidationException("Can't create claim for: " + claim.getYear() + "-" + claim.getClaimTypePeriod());
        }
        claim.setClaimType(ClaimType.MONTH);
        claim.setAmount(entry.getPaymentDue());
        learningGrantBlock.getClaims().add(claim);
    }

    @Override
    public boolean handleClaimDeletion(LearningGrantBlock block, Claim claim) {
        return false;
    }

    @Override
    public PaymentGroup generatePaymentsForProject(Project project, String approvalRequestedBy) {
        LearningGrantBlock learningGrantBlock = project.getLearningGrantBlock();
        if (learningGrantBlock != null && (learningGrantBlock.getApprovalWillCreatePendingPayment() || learningGrantBlock
                .getApprovalWillCreatePendingReclaim())) {

            List<ProjectLedgerEntry> ples = new ArrayList<>();
            for (Claim claim : learningGrantBlock.getClaims(Claimed)) {

                LearningGrantEntry learningGrantEntry = learningGrantBlock.getLearningGrantEntryForClaim(claim);
                learningGrantEntry.setGrantType(learningGrantBlock.getGrantType());
                ples.add(createPaymentFor(project, learningGrantBlock, claim, learningGrantEntry));
            }

            // Generate an extra payment or a reclaim if is needed due to increase or decrease of allocation amount
            if (project.getGrantSourceAdjustmentAmount() != null) {
                ples = generateExtraPaymentOrReclaim(project, learningGrantBlock, ples);
            }

            return paymentService.createPaymentGroup(approvalRequestedBy, ples);
        }

        return null;
    }

    private List<ProjectLedgerEntry> generateExtraPaymentOrReclaim(Project project, LearningGrantBlock learningGrantBlock,
            List<ProjectLedgerEntry> ples) {
        learningGrantBlock.getLearningGrantEntries().stream().filter(l -> !l.isCanManuallyClaimValue())
                .forEach(learningGrantEntry -> {
                    if (learningGrantEntry.getPaymentStatus() != null && learningGrantEntry.getPaymentStatus()
                            .equalsIgnoreCase(CLAIM_STATUS_PARTLY_PAID)) {
                        BigDecimal adjustment = learningGrantEntry.getPaymentDue()
                                .subtract(learningGrantEntry.getPaymentsTotal());
                        String category = SUPPLEMENTARY_PAYMENT + " " + learningGrantEntry.getType().getDisplayName();
                        ples.add(createPayment(project, learningGrantBlock, learningGrantEntry, category, adjustment));
                    }

                    if (learningGrantEntry.getPaymentStatus() != null && learningGrantEntry.getPaymentStatus()
                            .equalsIgnoreCase(CLAIM_STATUS_OVER_PAID)) {
                        BigDecimal adjustment = learningGrantEntry.getPaymentDue()
                                .subtract(learningGrantEntry.getPaymentsTotal());
                        String category = RECLAIMED_PAYMENT + " " + learningGrantEntry.getType().getDisplayName();
                        ples.add(createReclaim(project, learningGrantBlock, learningGrantEntry, category, adjustment));
                    }
                });
        return ples;
    }

    private ProjectLedgerEntry createPaymentFor(Project project, LearningGrantBlock learningGrantBlock, Claim claim,
            LearningGrantEntry entry) {
        String category = getPaymentCategory(claim, entry);
        return createPayment(project, learningGrantBlock, entry, category, claim.getAmount());
    }

    private String getPaymentCategory(Claim claim, LearningGrantEntry entry) {
        if (entry.isReturn()) {
            return "Reconciliation amount";
        }
        return entry.isMissedPayment() && claim != null ? "Missed Payment" : entry.getType().getDisplayName();
    }

    private ProjectLedgerEntry createPayment(Project project, LearningGrantBlock learningGrantBlock, LearningGrantEntry entry,
            String category, BigDecimal amount) {
        return paymentService.createPayment(project,
                learningGrantBlock.getId(),
                LedgerType.PAYMENT,
                getPaymentSource(project),
                LedgerStatus.Pending,
                category,
                entry.buildPaymentSubCategory(),
                amount.negate(),
                entry.getActualYear(),
                entry.getActualMonth(),
                entry.getOriginalId(),
                LedgerSource.WebUI);
    }

    private ProjectLedgerEntry createReclaim(Project project, LearningGrantBlock learningGrantBlock, LearningGrantEntry entry,
            String category, BigDecimal amount) {
        String paymentSource = getPaymentSource(project);
        String subCategory = entry.buildPaymentSubCategory();

        List<PaymentSummary> approvedPaymentsForProject = paymentService.getApprovedPaymentsForProject(project.getId());
        PaymentSummary paymentSummary = approvedPaymentsForProject.stream()
                .filter(p -> p.getExternalId().equals(entry.getOriginalId())).findFirst().orElse(null);
        ProjectLedgerEntry existingPayment = null;
        if (paymentSummary != null) {
            existingPayment = paymentService.getLedgerEntryById(paymentSummary.getId());
        }

        if (existingPayment == null) {
            throw new ValidationException("Unable to find existing payment for reclaim: " + project.getId());
        }

        return paymentService.createReclaim(project,
                existingPayment,
                learningGrantBlock.getId(),
                LedgerType.PAYMENT,
                paymentSource,
                LedgerStatus.Pending,
                category,
                subCategory,
                amount.negate(),
                entry.getActualYear(),
                entry.getActualMonth(),
                entry.getOriginalId(),
                LedgerSource.WebUI);
    }

    private String getPaymentSource(Project project) {
        TemplateBlock singleBlockByType = project.getTemplate().getSingleBlockByType(ProjectBlockType.LearningGrant);
        Set<String> paymentSources = singleBlockByType.getPaymentSources();
        if (paymentSources.size() != 1) {
            throw new ValidationException("Unable to determine payment Source for this learning grant block.");
        }
        return paymentSources.iterator().next();
    }


    @Override
    public void handleBlockClone(Project project, Integer originalBlockId, Integer newBlockId) {
        // nothing as we don't copy ledger entries with new block versions for the same project
    }

    @Override
    public void handleProjectClone(Project oldProject, Integer originalBlockId, Project newProject, Integer newBlockId) {
        NamedProjectBlock projectBlockById = oldProject.getProjectBlockById(originalBlockId);
        // check if correct block type
        if (projectBlockById != null && (ProjectBlockType.LearningGrant.equals(projectBlockById.getBlockType()))) {
            paymentService.clonePaymentGroupsForBlock(originalBlockId, newProject.getId(), newBlockId);
        }
    }


}
