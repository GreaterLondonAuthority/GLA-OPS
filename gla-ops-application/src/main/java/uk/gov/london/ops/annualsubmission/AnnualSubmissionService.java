/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.annualsubmission.implementation.AnnualSubmissionBlockRepository;
import uk.gov.london.ops.annualsubmission.implementation.AnnualSubmissionCategoryRepository;
import uk.gov.london.ops.annualsubmission.implementation.AnnualSubmissionEntryRepository;
import uk.gov.london.ops.annualsubmission.implementation.AnnualSubmissionRepository;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.calendar.FinancialCalendar;
import uk.gov.london.ops.framework.calendar.OPSCalendar;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.permission.PermissionService;
import uk.gov.london.ops.service.LockService;
import uk.gov.london.ops.service.LockableEntityService;
import uk.gov.london.ops.user.UserService;
import uk.gov.london.ops.user.domain.User;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static uk.gov.london.ops.annualsubmission.AnnualSubmissionBlock.Action.EDIT;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionStatus.*;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionStatusType.Actual;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionStatusType.Forecast;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionType.Generated;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionType.Spent;
import static uk.gov.london.ops.notification.NotificationType.AnnualSubmissionApproval;
import static uk.gov.london.ops.notification.NotificationType.AnnualSubmissionAwaitingApproval;
import static uk.gov.london.ops.permission.PermissionType.ANNUAL_SUBMISSION_CREATE;
import static uk.gov.london.ops.permission.PermissionType.ANNUAL_SUBMISSION_REVERT;

@Transactional
@Service
public class AnnualSubmissionService implements LockableEntityService<AnnualSubmissionBlock> {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuditService auditService;

    @Autowired
    private FeatureStatus featureStatus;

    @Autowired
    private LockService lockService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private FinancialCalendar financialCalendar;

    @Autowired
    private AnnualSubmissionRepository annualSubmissionRepository;

    @Autowired
    private AnnualSubmissionBlockRepository annualSubmissionBlockRepository;

    @Autowired
    private AnnualSubmissionCategoryRepository annualSubmissionCategoryRepository;

    @Autowired
    private AnnualSubmissionEntryRepository annualSubmissionEntryRepository;

    @Autowired
    private Environment environment;

    @Value("${annual.submissions.first.year}")
    private Integer annualSubmissionsFirstYear;

    @Value("${annual.submissions.nb.future.years}")
    private Integer annualSubmissionsNbFutureYears;

    @Value("${annual.submissions.dpf.last.year}")
    private Integer annualSubmissionsDpfLastYear;

    public List<AnnualSubmissionCategory> getCategories() {
        return annualSubmissionCategoryRepository.findAll();
    }

    public void updateCategory(Integer id, AnnualSubmissionCategory updated) {
        AnnualSubmissionCategory existing = annualSubmissionCategoryRepository.getOne(id);
        existing.setName(updated.getName());
        annualSubmissionCategoryRepository.save(existing);
    }

    public AnnualSubmissionCategory createCategory(AnnualSubmissionCategory category) {
        if (category == null) {
            throw new ValidationException("Invalid attempt to create category.");
        }

        if (category.getId() != null) {
            throw new ValidationException("ID must not be specified for new categories. ");
        }

        if (category.getType() == null || category.getName() == null || category.getGrant() == null || category.getStatus() == null) {
            throw new ValidationException("Type, name, grant and status are all mandatory for category creation");
        }

        return annualSubmissionCategoryRepository.save(category);
    }

    public void hideCategory(Integer id, boolean hidden) {
        AnnualSubmissionCategory existing = annualSubmissionCategoryRepository.getOne(id);
        existing.setHidden(hidden);
        annualSubmissionCategoryRepository.save(existing);
    }

    public List<Integer> getAvailableYearsForCreation(Integer organisationId) {
        List<Integer> availableYears = new ArrayList<>();
        int yearTo = financialCalendar.currentYear();
        if (featureStatus.isEnabled(Feature.AllowCreateAnnualReturnInTheFuture)) {
            yearTo += annualSubmissionsNbFutureYears;
        }
        for (Integer year = annualSubmissionsFirstYear; year < yearTo; year++) {
            availableYears.add(year);
        }

        List<AnnualSubmission> annualSubmissions = getAnnualSubmissions(organisationId);
        for (AnnualSubmission annualSubmission: annualSubmissions) {
            availableYears.remove(annualSubmission.getFinancialYear());
        }

        List<AnnualSubmission> approvedSubmissions = annualSubmissionRepository.findAllByOrganisationIdAndStatus(organisationId, Approved);
        if (CollectionUtils.isNotEmpty(approvedSubmissions)) {
            Integer minApprovedYear = approvedSubmissions.stream().map(AnnualSubmission::getFinancialYear).max(Integer::compareTo).get();
            availableYears.removeIf(year -> year < minApprovedYear);
        }

        return availableYears;
    }

    public List<AnnualSubmission> getAnnualSubmissions(Integer organisationId) {
        return annualSubmissionRepository.findAllByOrganisationId(organisationId);
    }

    public AnnualSubmission get(Integer id) {
        AnnualSubmission annualSubmission = annualSubmissionRepository.getOne(id);

        calculateOpeningBalance(annualSubmission.getOrganisationId(), annualSubmission.getFinancialYear(), annualSubmission.getAnnualRcgf());
        calculateOpeningBalance(annualSubmission.getOrganisationId(), annualSubmission.getFinancialYear(), annualSubmission.getAnnualDpf());
        calculateClosingBalances(annualSubmission);
        calculateAllowedTransitions(annualSubmission);
        for (AnnualSubmissionBlock block : annualSubmission.getBlocks()) {
            calculateAllowedActions(annualSubmission, block);
        }
        return annualSubmission;
    }

    void calculateAllowedTransitions(AnnualSubmission annualSubmission) {
        User currentUser = userService.currentUser();

        if (annualSubmission.getStatus().equals(Draft) && annualSubmission.isComplete() && !annualSubmission.anyBlocksLocked() && !currentUser.isReadOnly(annualSubmission.getOrganisationId())) {
            annualSubmission.getAllowedTransitions().add(new AnnualSubmissionTransition(Submitted, true));
        }
        else if (annualSubmission.getStatus().equals(Submitted) && permissionService.currentUserHasPermissionForOrganisation(ANNUAL_SUBMISSION_REVERT, annualSubmission.getOrganisationId())) {
            if (currentUser.isOpsAdmin() || annualSubmission.getFinancialYear() >= financialCalendar.currentYear() - 1) {
                annualSubmission.getAllowedTransitions().add(new AnnualSubmissionTransition(Draft, false));
            }

            annualSubmission.getAllowedTransitions().add(new AnnualSubmissionTransition(Approved, true));
        }
        else if (annualSubmission.getStatus().equals(Approved) && permissionService.currentUserHasPermissionForOrganisation(ANNUAL_SUBMISSION_REVERT, annualSubmission.getOrganisationId())) {
            if (currentUser.isOpsAdmin() || annualSubmission.getFinancialYear() >= financialCalendar.currentYear() - 1) {
                annualSubmission.getAllowedTransitions().add(new AnnualSubmissionTransition(Submitted, false));
            }
        }
    }

    private void calculateAllowedActions(AnnualSubmission annualSubmission, AnnualSubmissionBlock block) {
        if (permissionService.currentUserHasPermissionForOrganisation(ANNUAL_SUBMISSION_CREATE, annualSubmission.getOrganisationId())) {
            if (annualSubmission.getStatus().equals(Draft)) {
                block.getAllowedActions().add(EDIT);
            }
        }
    }

    public AnnualSubmission create(AnnualSubmission annualSubmission) {
        User currentUser = userService.currentUser();
        if (!currentUser.isOpsAdmin() && !currentUser.inOrganisation(annualSubmission.getOrganisationId())) {
            throw new ValidationException("user do not have permission to create submission for this organisation!");
        }

        Organisation organisation = organisationService.findOne(annualSubmission.getOrganisationId());

        if (!organisation.isAnnualReturnsEnabled()) {
            throw new ValidationException("Annual reports are not enabled for this organisation type");
        }

        if (annualSubmissionRepository.findByOrganisationIdAndFinancialYear(annualSubmission.getOrganisationId(), annualSubmission.getFinancialYear()) != null) {
            throw new ValidationException("annual submission already exists for the given year!");
        }

        if (annualSubmissionRepository.findByOrganisationIdAndStatusAndFinancialYearGreaterThan(annualSubmission.getOrganisationId(), Approved, annualSubmission.getFinancialYear()) != null) {
            throw new ValidationException("Cannot create an annual submission for a year prior to an existing approved submission");
        }

        annualSubmission.getBlocks().add(new AnnualSubmissionBlock(Actual, AnnualSubmissionGrantType.RCGF));
        annualSubmission.getBlocks().add(new AnnualSubmissionBlock(Forecast, AnnualSubmissionGrantType.RCGF));

        if (annualSubmission.getFinancialYear() <= annualSubmissionsDpfLastYear) {
            annualSubmission.getBlocks().add(new AnnualSubmissionBlock(Actual, AnnualSubmissionGrantType.DPF));
            annualSubmission.getBlocks().add(new AnnualSubmissionBlock(Forecast, AnnualSubmissionGrantType.DPF));
        }

        return annualSubmissionRepository.save(annualSubmission);
    }

    public void update(Integer submissionId, AnnualSubmission updated) {
        AnnualSubmission existing = annualSubmissionRepository.getOne(submissionId);

        existing.merge(updated);

        annualSubmissionRepository.save(existing);
    }

    public void updateStatus(Integer submissionId, AnnualSubmissionStatus status, String agreementText) {
        AnnualSubmission annualSubmission = get(submissionId);

        User currentUser = userService.currentUser();

        if (Submitted.equals(annualSubmission.getStatus()) && Draft.equals(status)) {
            annualSubmission.setSubmissionComments(null);
            annualSubmission.setApprovalComments(null);
            annualSubmission.setDpfRollover(null);
            annualSubmission.setRcgfRollover(null);
            annualSubmission.setDpfRolloverInterest(null);
            annualSubmission.setRcgfRolloverInterest(null);
            annualSubmission.setDpfWithdrawal(null);
            annualSubmission.setRcgfWithdrawal(null);
            annualSubmission.setDpfWithdrawalInterest(null);
            annualSubmission.setRcgfWithdrawalInterest(null);
            annualSubmission.setAgreementSigned(false);
            annualSubmission.setSubmittedBy(null);
            annualSubmission.setSubmittedOn(null);
            annualSubmission.setAgreementText(null);
        }
        if (status != null && !status.equals(annualSubmission.getStatus())) {
            if (annualSubmission.anyBlocksLocked()) {
                throw new ValidationException("Can't submit as a page is being edited");
            }

            AnnualSubmissionTransition transition = annualSubmission.getAllowedTransitions().stream().filter(t -> t.getStatus().equals(status)).findFirst().orElse(null);
            if (transition == null) {
                throw new ValidationException("Transition not allowed");
            }

            Map<String, Object> model = new HashMap<String, Object>() {{
                put("financialyear", OPSCalendar.financialYearString(annualSubmission.getFinancialYear()));
                put("organisation", organisationService.findOne(annualSubmission.getOrganisationId()));
                put("rollover", buildSubmissionRolloverText(annualSubmission));
            }};

            if (Draft.equals(annualSubmission.getStatus()) && Submitted.equals(status)) {
                if (agreementText == null || agreementText.isEmpty()) {
                    throw new ValidationException("Agreement text is mandatory for submission");
                }
                annualSubmission.setSubmittedBy(currentUser);
                annualSubmission.setSubmittedOn(environment.now());
                annualSubmission.setAgreementSigned(true);
                annualSubmission.setAgreementText(agreementText);

                notificationService.createNotification(AnnualSubmissionAwaitingApproval, annualSubmission, model);
            }
            if (Submitted.equals(annualSubmission.getStatus()) && Approved.equals(status)) {
                annualSubmission.setApprovedBy(currentUser.getFullName());
                annualSubmission.setApprovedOn(LocalDate.now());
                notificationService.createNotification(AnnualSubmissionApproval, annualSubmission, model);
            }

            auditService.auditCurrentUserActivity(String.format("annual submission %d status changed from %s to %s", annualSubmission.getId(), annualSubmission.getStatus(), status));

            annualSubmission.setStatus(status);
        }

        annualSubmissionRepository.save(annualSubmission);
    }

    public String buildSubmissionRolloverText(AnnualSubmission annualSubmission) {
        boolean rcgfBalanceRolloverConfirmed =
            annualSubmission.getAnnualRcgf().isBalanceRolloverConfirmed() != null && annualSubmission.getAnnualRcgf().isBalanceRolloverConfirmed();
        boolean dpfBalanceRolloverConfirmed =
            annualSubmission.getAnnualDpf() != null && annualSubmission.getAnnualDpf().isBalanceRolloverConfirmed() != null && annualSubmission.getAnnualDpf().isBalanceRolloverConfirmed();

        if (rcgfBalanceRolloverConfirmed && dpfBalanceRolloverConfirmed) {
            return " and includes a requested rollover of RCGF and DPF";
        }
        else if (rcgfBalanceRolloverConfirmed) {
            return " and includes a requested rollover of RCGF";
        }
        else if (dpfBalanceRolloverConfirmed) {
            return " and includes a requested rollover of DPF";
        }
        else {
            return "";
        }
    }

    public AnnualSubmissionBlock getBlock(Integer submissionId, Integer blockId) {
        AnnualSubmission annualSubmission = get(submissionId);

        AnnualSubmissionBlock block = annualSubmission.getBlockById(blockId);
        block.setFinancialYear(annualSubmission.getFinancialYear());
        calculateAllowedActions(annualSubmission, block);

        Integer organisationId = annualSubmission.getOrganisationId();
        Integer financialYear = annualSubmission.getFinancialYear();
        AnnualSubmissionStatusType statusType = block.getStatusType();
        AnnualSubmissionGrantType grantType = block.getGrantType();

        if (Actual.equals(statusType)) {
            AnnualSubmissionBlock blockYearMinus3 = getBlockAndSetOpeningBalance(organisationId, financialYear - 3, statusType, grantType);
            AnnualSubmissionBlock blockYearMinus2 = getBlockAndSetOpeningBalance(organisationId, financialYear - 2, statusType, grantType);
            AnnualSubmissionBlock blockYearMinus1 = getBlockAndSetOpeningBalance(organisationId, financialYear - 1, statusType, grantType);
            if (blockYearMinus1 != null) {
                block.setComputedOpeningBalance(blockYearMinus1.getClosingBalance());
            }

            List<AnnualSubmissionBlockTotals> totals = new ArrayList<>();
            totals.add(new AnnualSubmissionBlockTotals(financialYear - 3, blockYearMinus3));
            totals.add(new AnnualSubmissionBlockTotals(financialYear - 2, blockYearMinus2));
            totals.add(new AnnualSubmissionBlockTotals(financialYear - 1, blockYearMinus1));
            block.setTotals(totals);
        }
        else if (Forecast.equals(statusType)) {
            // the closing balance and total unspent grant is keyed in the Actual block and shown in the Forecast block
            AnnualSubmissionBlock actualBlock = getBlock(organisationId, financialYear, Actual, grantType);
            if (actualBlock != null) {
                block.setClosingBalance(actualBlock.getClosingBalance());

                if (actualBlock.getTotalUnspentGrant() != null) {
                    block.setTotalUnspentGrant(actualBlock.getTotalUnspentGrant());
                }
                if (actualBlock.getInterestedAccumulated() != null) {
                    block.setInterestedAccumulated(actualBlock.getInterestedAccumulated());
                }
                else {
                    block.setTotalUnspentGrant(0);
                }
            }

            List<AnnualSubmissionBlockYearBreakdown> breakDown = new ArrayList<>();
            for (int i = 1; i <= block.getTemplate().getAmountOfForecastYears(); i++) {
                breakDown.add(new AnnualSubmissionBlockYearBreakdown(financialYear + i, block.getEntries(financialYear + i), block.getTotal(financialYear + i)));
            }
            block.setYearBreakdown(breakDown);
        }

        return block;
    }

    private AnnualSubmissionBlock getBlock(Integer organisationId, Integer financialYear, AnnualSubmissionStatusType statusType, AnnualSubmissionGrantType grantType) {
        AnnualSubmission annualSubmission = annualSubmissionRepository.findByOrganisationIdAndFinancialYear(organisationId, financialYear);
        if (annualSubmission != null) {
            return annualSubmission.getBlockByType(statusType, grantType);
        }

        return null;
    }

    void calculateClosingBalances(AnnualSubmission annualSubmission) {
        AnnualSubmissionBlock annualRCGF = annualSubmission.getAnnualRcgf();
        AnnualSubmissionBlock forecastRCGF = annualSubmission.getForecastRcgf();
        if (annualRCGF != null && forecastRCGF != null) {
            annualRCGF.recalculateClosingBalance();
            forecastRCGF.setClosingBalance(annualRCGF.getClosingBalance());
        }

        AnnualSubmissionBlock annualDPF = annualSubmission.getAnnualDpf();
        AnnualSubmissionBlock forecastDPF = annualSubmission.getForecastDpf();
        if (annualDPF != null && forecastDPF != null) {
            annualDPF.recalculateClosingBalance();
            forecastDPF.setClosingBalance(annualDPF.getClosingBalance());
        }

    }

    private void calculateOpeningBalance(Integer organisationId, Integer financialYear, AnnualSubmissionBlock block) {
        if (block != null) {
            List<AnnualSubmissionBlock> blocks = annualSubmissionBlockRepository.findAll(organisationId, financialYear, block.getStatusType().name(), block.getGrantType().name());
            for (int i = 0; i < blocks.size() - 1; i++) {
                blocks.get(i + 1).setComputedOpeningBalance(blocks.get(i).getClosingBalance());
            }
            AnnualSubmissionBlock previousYearBlock = blocks.isEmpty() ? null : blocks.get(blocks.size() - 1);
            if (previousYearBlock != null) {
                block.setComputedOpeningBalance(previousYearBlock.getClosingBalance());
            }
        }
    }

    private AnnualSubmissionBlock getBlockAndSetOpeningBalance(Integer organisationId, Integer financialYear, AnnualSubmissionStatusType statusType, AnnualSubmissionGrantType grantType) {
        AnnualSubmissionBlock block = getBlock(organisationId, financialYear, statusType, grantType);
        calculateOpeningBalance(organisationId, financialYear, block);
        return block;
    }

    public void updateBlock(Integer submissionId, Integer blockId, AnnualSubmissionBlock block) {
        AnnualSubmissionBlock existing = annualSubmissionBlockRepository.getOne(blockId);

        AnnualSubmission submission = annualSubmissionRepository.getOne(submissionId);

        lockService.validateLockedByCurrentUser(existing);

        existing.merge(block);

        if (Actual.equals(block.getStatusType())) {
            AnnualSubmissionBlock forecast = AnnualSubmissionGrantType.DPF.equals(block.getGrantType()) ?
                    submission.getForecastDpf() : submission.getForecastRcgf();
            if (block.getTotalUnspentGrant() != null) {
                forecast.setTotalUnspentGrant(block.getTotalUnspentGrant());
            }
            if (block.getInterestedAccumulated() != null) {
                forecast.setInterestedAccumulated(block.getInterestedAccumulated());
            }
            else {
                forecast.setTotalUnspentGrant(0);
            }
            annualSubmissionBlockRepository.save(forecast);

        }

        annualSubmissionBlockRepository.save(existing);
    }

    public AnnualSubmissionEntry createEntry(Integer submissionId, Integer blockId, AnnualSubmissionEntry entry) {
        AnnualSubmission annualSubmission = get(submissionId);
        AnnualSubmissionBlock block = annualSubmission.getBlockById(blockId);

        lockService.validateLockedByCurrentUser(block);

        AnnualSubmissionCategory category = annualSubmissionCategoryRepository.getOne(entry.getCategoryId());

        if (!block.getGrantType().equals(category.getGrant()) || !block.getStatusType().equals(category.getStatus())) {
            throw new ValidationException("invalid category for this block!");
        }

        entry.setBlockId(blockId);
        entry.setCategory(category);

        if ((block.isNoGeneratedData() && entry.getCategory().getType().equals(Generated))
                || (block.isNoSpentData() && entry.getCategory().getType().equals(Spent))) {
            throw new ValidationException("cannot create new entry when no data checkbox has been selected");
        }

        if (block.getEntryByCategory(entry.getCategory(), entry.getFinancialYear()) != null) {
            throw new ValidationException("category already in use");
        }

        return annualSubmissionEntryRepository.save(entry);
    }

    public void updateEntry(Integer submissionId, Integer blockId, Integer entryId, AnnualSubmissionEntry updated) {
        AnnualSubmission annualSubmission = get(submissionId);
        AnnualSubmissionBlock block = annualSubmission.getBlockById(blockId);

        lockService.validateLockedByCurrentUser(block);

        AnnualSubmissionEntry existing = annualSubmissionEntryRepository.getOne(entryId);

        auditService.auditCurrentUserActivity(String.format("annual submission entry %d value was changed from %d to %d", entryId, existing.getValue(), updated.getValue()));

        existing.setValue(updated.getValue());
        existing.setComments(updated.getComments());
        annualSubmissionEntryRepository.save(existing);
    }

    public void deleteEntry(Integer submissionId, Integer blockId, Integer entryId) {
        AnnualSubmission annualSubmission = get(submissionId);
        AnnualSubmissionBlock block = annualSubmission.getBlockById(blockId);

        lockService.validateLockedByCurrentUser(block);

        AnnualSubmissionEntry entry = annualSubmissionEntryRepository.getOne(entryId);

        auditService.auditCurrentUserActivity(String.format("deleted annual submission entry %d - %s with value %d", entry.getCategory().getId(), entry.getCategory().getName(), entry.getValue()));
        block.getEntries().removeIf(e -> e.getId().equals(entryId));
        annualSubmissionBlockRepository.save(block);
    }

    @Override
    public void validateLockable(AnnualSubmissionBlock annualSubmissionBlock) throws ValidationException {
        AnnualSubmission annualSubmission = annualSubmissionRepository.findByBlockId(annualSubmissionBlock.getId());
        calculateAllowedActions(annualSubmission, annualSubmissionBlock);
        if (!annualSubmissionBlock.getAllowedActions().contains(EDIT)) {
            throw new ValidationException("cannot lock as block is not editable!");
        }
    }

    @Override
    public AnnualSubmissionBlock getLockable(Integer entityId) {
        return annualSubmissionBlockRepository.getOne(entityId);
    }

    @Override
    public void saveLockable(AnnualSubmissionBlock annualSubmissionBlock) {
        annualSubmissionBlockRepository.save(annualSubmissionBlock);
    }

    @Override
    public void saveLockables(Collection<AnnualSubmissionBlock> annualSubmissionBlocks) {
        annualSubmissionBlockRepository.saveAll(annualSubmissionBlocks);
    }

    @Override
    public Collection<AnnualSubmissionBlock> findAllByLockTimeoutTimeBefore(OffsetDateTime now) {
        return annualSubmissionBlockRepository.findAllByLockTimeoutTimeBefore(now);
    }

    public void deleteAllTestData() {
        if (environment.isTestEnvironment()) {
            annualSubmissionRepository.deleteAll();
            annualSubmissionBlockRepository.deleteAll();
            annualSubmissionEntryRepository.deleteAll();
            annualSubmissionCategoryRepository.deleteAll();
        }
        else {
            log.error("attempting to delete test data in a non test environment!");
        }
    }
}
