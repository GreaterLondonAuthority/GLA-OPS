/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.finance;

import liquibase.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.finance.LedgerSource;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.mapper.AnnualReceiptsSummaryMapper;
import uk.gov.london.ops.mapper.AnnualSpendSummaryMapper;
import uk.gov.london.ops.repository.ProjectLedgerRepository;
import uk.gov.london.ops.repository.ReceiptsTotalRecordRepository;
import uk.gov.london.ops.service.AuditService;
import uk.gov.london.ops.service.PermissionService;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.util.CSVFile;
import uk.gov.london.ops.web.model.AnnualSpendSummary;
import uk.gov.london.ops.web.model.ProjectLedgerItemRequest;
import uk.gov.london.ops.web.model.project.AnnualReceiptsSummary;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.ActivityType.Add;
import static uk.gov.london.ops.domain.EntityType.ledger;
import static uk.gov.london.ops.domain.finance.LedgerStatus.ACTUAL;
import static uk.gov.london.ops.domain.finance.LedgerStatus.FORECAST;
import static uk.gov.london.ops.util.GlaOpsUtils.parseDateString;
import static uk.gov.london.ops.util.GlaOpsUtils.parseInt;

/**
 * Created by chris on 27/01/2017.
 */
@Service
@Transactional
public class FinanceService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    Environment environment;

    @Autowired
    AuditService auditService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectLedgerRepository projectLedgerRepository;

    @Autowired
    AnnualSpendSummaryMapper annualSpendSummaryMapper;

    @Autowired
    AnnualReceiptsSummaryMapper annualReceiptSummaryMapper;

    @Autowired
    FinancialCalendar financialCalendar;

    @Autowired
    ReceiptsTotalRecordRepository receiptsTotalRecordRepository;

    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");



    public void deleteProjectLedgerEntry(Project project, ProjectLedgerItemRequest lineItem) {
        NamedProjectBlock namedBlock = project.getProjectBlockById(lineItem.getBlockId());
        if (namedBlock == null) {
            throw new ValidationException(String.format("Unable to delete entry for category %d as an invalid block id was passed", lineItem.getCategoryId()));
        }

        int yearMonth = financialCalendar.asInt(lineItem.getYear(), lineItem.getMonth());

        List<ProjectLedgerEntry> allByBlockIdAndYearMonthAndCategory = projectLedgerRepository.findAllByBlockIdAndYearMonthAndCategoryId(lineItem.getBlockId(), yearMonth, lineItem.getCategoryId());
        List<ProjectLedgerEntry> toDelete = new ArrayList<>();

        for (ProjectLedgerEntry projectLedgerEntry : allByBlockIdAndYearMonthAndCategory) {
            // actuals can't be deleted ,
            if (ACTUAL.equals(projectLedgerEntry.getLedgerStatus())) {
                throw new ValidationException(String.format("Unable to delete entry for category %d as it contains actual data ", lineItem.getCategoryId()));
            }
            // if credit then delete positive values , else negative.
            if (ProjectLedgerItemRequest.LedgerEntryType.CAPITAL_CREDIT.equals(lineItem.getEntryType()) ||
                    ProjectLedgerItemRequest.LedgerEntryType.REVENUE_CREDIT.equals(lineItem.getEntryType())) {

                if (BigDecimal.ZERO.compareTo(projectLedgerEntry.getValue()) <= 0) {
                    toDelete.add(projectLedgerEntry);
                }
            } else {
                if (BigDecimal.ZERO.compareTo(projectLedgerEntry.getValue()) > 0) {
                    toDelete.add(projectLedgerEntry);
                }
            }
        }

        projectLedgerRepository.delete(toDelete);

        auditService.auditCurrentUserActivity(String.format("deleted ledger entry category %d %d/%d", lineItem.getCategoryId(), lineItem.getYear(), lineItem.getMonth()));

        projectService.updateProject(project);
    }

    public void deleteProjectLedgerEntry(Integer id) {
        ProjectLedgerEntry entry = projectLedgerRepository.findOne(id);

        if (ACTUAL.equals(entry.getLedgerStatus())) {
            throw new ValidationException(String.format("Unable to delete entry with id {}", id));
        }

        projectLedgerRepository.delete(entry);

        auditService.auditCurrentUserActivity(String.format("deleted ledger entry with category id %d value %s project %d for period %d/%d",
                entry.getCategoryId(), entry.getValue().toString(), entry.getProjectId(), entry.getYear(), entry.getMonth()));
    }


    public void addProjectLedgerEntry(ProjectLedgerItemRequest itemRequest) {
        validateLedgerRequest(itemRequest);

        ProjectLedgerEntry entry = findExistingSpend(itemRequest);

        if (entry != null) {
            if (itemRequest.getValue() == null || itemRequest.getValue().compareTo(BigDecimal.ZERO) == 0) { // zero or null delete
                auditService.auditCurrentUserActivity(String.format("deleted ledger entry category %d %d/%d for type %s", itemRequest.getCategoryId(), itemRequest.getYear(), itemRequest.getMonth(), itemRequest.getSpendType().name()));
                projectLedgerRepository.delete(entry);
            }
            else {
                entry.updateValue(itemRequest.getValue());
                projectLedgerRepository.save(entry);
            }
        }
        else {
            entry = new ProjectLedgerEntry(itemRequest.getProjectId(), itemRequest.getBlockId(), itemRequest.getYear(),
                    itemRequest.getMonth(), itemRequest.getLedgerStatus(), itemRequest.getLedgerType(), itemRequest.getSpendType(),
                    itemRequest.getCategoryId(), itemRequest.getValue());
            entry.setLedgerSource(LedgerSource.WebUI);
            projectLedgerRepository.save(entry);

            if (ACTUAL.equals(itemRequest.getLedgerStatus())) {
                entry.setTransactionDate(getTransactionDate(itemRequest));
                auditService.auditCurrentUserActivity(ledger, entry.getId(), Add);
            }
        }
    }

    private ProjectLedgerEntry findExistingSpend(ProjectLedgerItemRequest itemRequest) {
        if (ACTUAL.equals(itemRequest.getLedgerStatus())) {
            return null; // we dont want to update actuals
        }

        int yearMonth = financialCalendar.asInt(itemRequest.getYear(), itemRequest.getMonth());

        List<ProjectLedgerEntry> entries = projectLedgerRepository.findAllByBlockIdAndYearMonthAndCategoryIdAndSpendTypeAndLedgerStatus(
                        itemRequest.getBlockId(), yearMonth, itemRequest.getCategoryId(), itemRequest.getSpendType(), LedgerStatus.FORECAST);

        return extractRelevantRow(entries, itemRequest.getEntryType());
    }

    // works out which row in the list of entries matches the request type, null if none do
    private ProjectLedgerEntry extractRelevantRow(List<ProjectLedgerEntry> entries, ProjectLedgerItemRequest.LedgerEntryType requestType) {
        if (ProjectLedgerItemRequest.LedgerEntryType.CAPITAL_CREDIT.equals(requestType) ||
                ProjectLedgerItemRequest.LedgerEntryType.REVENUE_CREDIT.equals(requestType)) {
            for (ProjectLedgerEntry entry : entries) {
                if (entry.getValue().compareTo(BigDecimal.ZERO) == 1) { // > 0 so return it
                    return entry;
                }
            }
        } else {
            for (ProjectLedgerEntry entry : entries) {
                if (entry.getValue().compareTo(BigDecimal.ZERO) == -1) { // < 0 so return it
                    return entry;
                }
            }
        }
        return null;
    }

    public void addReceiptEntry(ProjectLedgerItemRequest itemRequest) {
        validateLedgerRequest(itemRequest);

        ProjectLedgerEntry entry = findExistingReceipt(itemRequest);

        if (entry != null) {
            updateReceiptEntryValue(entry, itemRequest.getForecastValue());
        }
        else {
            entry = new ProjectLedgerEntry(itemRequest.getProjectId(), itemRequest.getBlockId(), itemRequest.getYear(),
                    itemRequest.getMonth(), itemRequest.getLedgerStatus(), LedgerType.RECEIPT, null,
                    itemRequest.getCategoryId(), itemRequest.getValue());
            entry.setLedgerSource(LedgerSource.WebUI);

            if (ACTUAL.equals(itemRequest.getLedgerStatus())) {
                entry.setTransactionDate(getTransactionDate(itemRequest));
                auditService.auditCurrentUserActivity(ledger, entry.getId(), Add);
            }
            projectLedgerRepository.save(entry);
        }
    }

    private String getTransactionDate(ProjectLedgerItemRequest request) {
        formatter.setLenient(false);
        String fullDate = request.getFullDate();
        try {
            formatter.parse(fullDate);
            return fullDate;
        } catch (ParseException e) {
            throw new ValidationException("Incorrectly formatted actual date: " + fullDate);
        }
    }

    private void validateLedgerRequest(ProjectLedgerItemRequest itemRequest) {
        if (ACTUAL.equals(itemRequest.getLedgerStatus()) && !permissionService.currentUserHasPermission(PermissionService.PROJ_LEDGER_ACTUAL_CREATE)) {
            throw new ForbiddenAccessException();
        }
    }

    private ProjectLedgerEntry findExistingReceipt(ProjectLedgerItemRequest itemRequest) {
        if (ACTUAL.equals(itemRequest.getLedgerStatus())) {
            return null; // we dont want to update actuals
        }

        int yearMonth = financialCalendar.asInt(itemRequest.getYear(), itemRequest.getMonth());

        return projectLedgerRepository.findFirstByBlockIdAndYearMonthAndCategoryIdAndLedgerStatus(itemRequest.getBlockId(),
                yearMonth, itemRequest.getCategoryId(), LedgerStatus.FORECAST);
    }

    public void editReceiptEntry(Integer id, BigDecimal value) {
        ProjectLedgerEntry entry = projectLedgerRepository.findOne(id);
        if (ACTUAL.equals(entry.getLedgerStatus())) {
            throw new ValidationException(String.format("Unable to edit entry with id {}", id));
        }
        updateReceiptEntryValue(entry, value);
    }

    private void updateReceiptEntryValue(ProjectLedgerEntry entry, BigDecimal value) {
        entry.updateValue(value);
        projectLedgerRepository.save(entry);
    }



    public AnnualSpendSummary getAnnualSpendForSpecificYear(Integer blockId, Integer year) {
        List<ProjectLedgerEntry> ledgerEntriesByBlockAndYear = projectLedgerRepository.findAllByBlockIdAndFinancialYear(blockId, year);
        return annualSpendSummaryMapper.getAnnualSpendSummary(ledgerEntriesByBlockAndYear, year);
    }

    public List<AnnualSpendSummary> getAnnualSpendForSpecificYears(Integer blockId, Set<Integer> populatedYears) {
        Integer fromYear = Collections.min(populatedYears);
        Integer toYear = Collections.max(populatedYears);

        List<ProjectLedgerEntry> ledgerEntriesByBlockAndYear = projectLedgerRepository.findAllByBlockIdBetweenFinancialYears(blockId, fromYear, toYear);
        return annualSpendSummaryMapper.getAnnualSpendSummary(ledgerEntriesByBlockAndYear, fromYear, toYear,populatedYears);
    }

    public AnnualReceiptsSummary getAnnualReceiptSummaryForYear(ReceiptsBlock receiptsBlock, Integer year) {
        List<ProjectLedgerEntry> ledgerEntriesByBlockAndYear = projectLedgerRepository.findAllByBlockIdAndFinancialYear(receiptsBlock.getId(), year);
        AnnualReceiptsSummary annualReceiptsSummary = annualReceiptSummaryMapper.toAnnualReceiptSummary(ledgerEntriesByBlockAndYear, year);

        List<ReceiptsTotalRecord> receiptsTotal = receiptsTotalRecordRepository.findByProjectIdAndBlockIdAndFinancialYearOrderByLedgerStatus(receiptsBlock.getProjectId(), receiptsBlock.getId(), year);
        for (ReceiptsTotalRecord receiptsTotalRecord : receiptsTotal) {
            if (FORECAST.equals(receiptsTotalRecord.getLedgerStatus()) && receiptsTotalRecord.getTotal() != null) {
                annualReceiptsSummary.getTotalForCurrentAndFutureMonths().setForecast(receiptsTotalRecord.getTotal());
            } else if (ACTUAL.equals(receiptsTotalRecord.getLedgerStatus()) && receiptsTotalRecord.getTotal() != null) {
                annualReceiptsSummary.getTotalForPastMonths().setActual(receiptsTotalRecord.getTotal());
            }
        }
        return annualReceiptsSummary;
    }

    public Set<Integer> getPopulatedYearsForBlock(Integer blockId) {
        Set<Integer> populatedFinancialYears = new HashSet<>();
        for (Integer yearMonth: projectLedgerRepository.findPopulatedYearsForBlock(blockId)) {
            populatedFinancialYears.add(financialCalendar.financialFromYearMonth(yearMonth));
        }
        return populatedFinancialYears;
    }

    public void cloneLedgerEntriesForBlock(Integer blockId, Integer clonedBlockId) {
        cloneLedgerEntriesForBlock(blockId, null, clonedBlockId);
    }

    public void cloneLedgerEntriesForBlock(Integer blockId, Integer clonedProjectId, Integer clonedBlockId) {
        List<ProjectLedgerEntry> entries = projectLedgerRepository.findAllByBlockId(blockId);
        if (entries == null || entries.isEmpty()) {
            return;
        }
        List<ProjectLedgerEntry> clones = new ArrayList<>();
        for (ProjectLedgerEntry entry : entries) {
            ProjectLedgerEntry clone = clone(entry, clonedProjectId, clonedBlockId);
            clones.add(clone);
        }
        projectLedgerRepository.save(clones);
    }

    public ProjectLedgerEntry clone(ProjectLedgerEntry entry, Integer clonedProjectId, Integer clonedBlockId) {
        ProjectLedgerEntry clone = new ProjectLedgerEntry(clonedProjectId != null ? clonedProjectId : entry.getProjectId(), clonedBlockId, entry.getYear(),
                entry.getMonth(), entry.getLedgerStatus(), entry.getLedgerType(), entry.getSpendType(),
                entry.getCategoryId(), entry.getCategory(), entry.getValue());
        clone.setCategoryId(entry.getCategoryId());
        clone.updateDetailsFrom(entry);
        return clone;
    }



    /**
     * This method only updatea the two budget entries for capital and revenue.
     */
    public List<ProjectLedgerEntry> updateAnnualSpendAndBudgetLedgerEntries(Project project,  Integer year, BigDecimal revenue, BigDecimal capital) {

        ProjectBudgetsBlock block = (ProjectBudgetsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);

        List<ProjectLedgerEntry> allByBlockIdAndFinancialYear = projectLedgerRepository.findAllByBlockIdAndFinancialYearAndLedgerType(block.getId(), year, LedgerType.BUDGET);


        ProjectLedgerEntry revenueEntry = null;
        ProjectLedgerEntry capitalEntry = null;
        for (ProjectLedgerEntry projectLedgerEntry : allByBlockIdAndFinancialYear) {
            if (SpendType.CAPITAL.equals(projectLedgerEntry.getSpendType())) {
                capitalEntry = projectLedgerEntry;
            } else if (SpendType.REVENUE.equals(projectLedgerEntry.getSpendType())) {
                revenueEntry = projectLedgerEntry;
            }
        }

        if (revenueEntry != null) {
            if (revenue == null) {
                allByBlockIdAndFinancialYear.remove(revenueEntry);
                auditService.auditCurrentUserActivity(String.format("deleted annual spend revenue budget entry for project %s year %d ", project.getId(), year));
                projectLedgerRepository.delete(revenueEntry);
            } else {
                revenueEntry.updateValue(revenue);
            }
        }

        if (capitalEntry != null) {
            if (capital == null) {
                allByBlockIdAndFinancialYear.remove(capitalEntry);
                auditService.auditCurrentUserActivity(String.format("deleted annual spend capital budget entry for project %s year %d ", project.getId(), year));
                projectLedgerRepository.delete(capitalEntry);
            } else {
                capitalEntry.updateValue(capital);
            }
        }

        if (revenueEntry == null && revenue != null) {
            revenueEntry = new ProjectLedgerEntry(project, block, year, LedgerType.BUDGET, SpendType.REVENUE, revenue);
            revenueEntry.setLedgerSource(LedgerSource.WebUI);
            allByBlockIdAndFinancialYear.add(revenueEntry);
        }
        if (capitalEntry == null && capital != null) {
            capitalEntry = new ProjectLedgerEntry(project, block, year, LedgerType.BUDGET, SpendType.CAPITAL, capital);
            capitalEntry.setLedgerSource(LedgerSource.WebUI);
            allByBlockIdAndFinancialYear.add(capitalEntry);
        }

        projectLedgerRepository.save(allByBlockIdAndFinancialYear);

        return allByBlockIdAndFinancialYear;
    }

    /**
     * Is complete when the revenue and capital values for current financial year are entered, potentially will need documents in the future as well.
     */
    // TODO REMOVE
    public boolean areBudgetEntriesPresentForCurrentFinancialYear(ProjectBudgetsBlock block) {
        //Integer year = getCurrentFinancialYear();
        int year = financialCalendar.currentFinancialYear();
        List<ProjectLedgerEntry> allByBlockIdAndFinancialYear =
                projectLedgerRepository.findAllByBlockIdAndFinancialYearAndLedgerType(block.getId(), year, LedgerType.BUDGET);

        return !(allByBlockIdAndFinancialYear == null || allByBlockIdAndFinancialYear.size() != 2);

    }


    //TODO these methods should be move to a service like ProjectLedgerService
    public ProjectLedgerEntry save(ProjectLedgerEntry entry) {
        return projectLedgerRepository.saveAndFlush(entry);
    }

    public List<ProjectLedgerEntry> save(List<ProjectLedgerEntry> entry) {
        final List<ProjectLedgerEntry> result =  projectLedgerRepository.save(entry);
        projectLedgerRepository.flush();
        return result;
    }

    public ProjectLedgerEntry findOne(Integer id) {
        return projectLedgerRepository.findOne(id);
    }

    public List<ProjectLedgerEntry> findByStatus(final LedgerStatus status) {
        return projectLedgerRepository.findAllByLedgerStatus(status);
    }

    public List<ProjectLedgerEntry> findByStatusAndTypeIn(final LedgerStatus authorised,
                                                        final LedgerType [] types) {
        return projectLedgerRepository.findByLedgerStatusAndLedgerTypeIn(authorised, types);
    }

    public List<ProjectLedgerEntry> getByBlockIdAndExternalId(Integer blockId, Integer externalId) {
        return projectLedgerRepository.findByBlockIdAndExternalId(blockId, externalId);
    }

    public List<ProjectLedgerEntry> findByLedgerStatusIn(List<LedgerStatus> relevantStatuses) {
        return projectLedgerRepository.findByLedgerStatusIn(relevantStatuses);
    }

    public List<ProjectLedgerEntry> findByIdIn(List<Integer> paymentIdList) {
        return projectLedgerRepository.findByIdIn(paymentIdList);
    }

    public List<ProjectLedgerEntry> findAllByAuthorisedOnBetween(final LedgerType type,
                                                                 final OffsetDateTime dayStart,
                                                                 final OffsetDateTime dayEnd) {
        return projectLedgerRepository.findAllByLedgerTypeAndAuthorisedOnBetween(type, dayStart, dayEnd);
    }

    public List<ProjectLedgerEntry> findAll() {
        return projectLedgerRepository.findAll();
    }

    public void setFinancialCalendar(FinancialCalendar financialCalendar) {
        this.financialCalendar = financialCalendar;
    }

    public void updateFromOriginalLedgerEntry(Integer originalId, Integer copyId) {
        ProjectLedgerEntry original = projectLedgerRepository.findOne(originalId);
        ProjectLedgerEntry copy = projectLedgerRepository.findOne(copyId);

        if (!copy.matchesOriginal(original)) {
            throw new ValidationException(String.format("original (%d) and copy (%d) ledger entries to not match!", originalId, copyId));
        }

        copy.updateDetailsFrom(original);
    }

    public void updateLedgerEntriesFromOriginal() {
        List<ProjectLedgerEntry> entries = projectLedgerRepository.findAll();
        Map<Integer, List<ProjectLedgerEntry>> groupByProject = entries.stream().collect(Collectors.groupingBy(ProjectLedgerEntry::getProjectId));
        for (Integer projectId: groupByProject.keySet()) {
            updateLedgerEntriesFromOriginalForProject(projectId, groupByProject.get(projectId));
        }
    }

    private void updateLedgerEntriesFromOriginalForProject(Integer projectId, List<ProjectLedgerEntry> projectLedgerEntries) {
        log.debug("about to process {} entries for project {}", projectLedgerEntries.size(), projectId);

        List<String> blockUsedOriginalIds = new ArrayList<>();

        for (ProjectLedgerEntry entry: projectLedgerEntries) {

            ProjectLedgerEntry original = null;
            for (ProjectLedgerEntry potentialOriginal: projectLedgerEntries) {
                if (!blockUsedOriginalIds.contains(entry.getBlockId()+"-"+potentialOriginal.getId())
                        && entry.matchesOriginal(potentialOriginal)
                        && (original == null || (potentialOriginal.getId() < original.getId()))) {
                    original = potentialOriginal;
                }
            }

            if (original != null) {
                blockUsedOriginalIds.add(entry.getBlockId()+"-"+original.getId());
                entry.updateDetailsFrom(original);
                projectLedgerRepository.save(entry);
            }
        }
    }

    public void importLedgerEntries(InputStream inputStream) throws IOException {
        CSVFile.CSVMapper<ProjectLedgerEntry> ledgerEntryMapper = csv -> {
            ProjectLedgerEntry entry = new ProjectLedgerEntry();
            entry.setId(csv.getInteger("id"));
            entry.setProjectId(csv.getInteger("project_id"));
            entry.setBlockId(csv.getInteger("block_id"));
            entry.setYear(csv.getInteger("year"));
            entry.setMonth(csv.getInteger("month"));
            entry.setYearMonth(csv.getInteger("year_month"));
            entry.setLedgerStatus(LedgerStatus.valueOf(csv.getString("ledger_status")));
            entry.setLedgerType(StringUtils.isNotEmpty(csv.getString("ledger_type")) ? LedgerType.valueOf(csv.getString("ledger_type")) : null);
            entry.setSpendType(StringUtils.isNotEmpty(csv.getString("spend_type")) ? SpendType.valueOf(csv.getString("spend_type")) : null);
            entry.setCategory(csv.getString("category"));
            // "value"
            entry.setModifiedOn(parseDateString(csv.getString("modified_on"), "yyyy-MM-dd HH:mm:ss"));
            entry.setReference(csv.getString("reference"));
            entry.setPcsPhaseNumber(csv.getString("pcs_phase_number"));
            entry.setVendorName(csv.getString("vendor_name"));
            entry.setTransactionDate(csv.getString("transaction_date"));
            entry.setSapCategoryCode(csv.getString("sap_category_code"));
            entry.setDescription(csv.getString("description"));
            entry.setCostCentreCode(csv.getString("cost_centre_code"));
            entry.setTransactionNumber(csv.getString("transaction_number"));
            entry.updateValue(new BigDecimal(csv.getString("amount")));
            entry.setCreatedOn(parseDateString(csv.getString("created_on"), "yyyy-MM-dd HH:mm:ss"));
            entry.setCreatedBy(csv.getString("created_by"));
//                entry.setModifiedBy(csv.getString("modified_by"));
            entry.setInvoiceDate(csv.getString("invoice_date"));
            entry.setPcsProjectNumber(parseInt(csv.getString("pcs_project_number")));
            entry.setCategoryId(parseInt(csv.getString("category_id")));
            entry.setLedgerSource(StringUtils.isNotEmpty(csv.getString("ledger_source")) ? LedgerSource.valueOf(csv.getString("ledger_source")) : null);
            entry.setWbsCode(csv.getString("wbs_code"));
            entry.setAuthorisedOn(parseDateString(csv.getString("authorised_on"), "yyyy-MM-dd HH:mm:ss"));
            entry.setAuthorisedBy(csv.getString("authorised_by"));
            entry.setSentOn(parseDateString(csv.getString("sent_on"), "yyyy-MM-dd HH:mm:ss"));
            entry.setClearedOn(parseDateString(csv.getString("cleared_on"), "yyyy-MM-dd HH:mm:ss"));
            entry.setAcknowledgedOn(parseDateString(csv.getString("acknowledged_on"), "yyyy-MM-dd HH:mm:ss"));
            entry.setSubCategory(csv.getString("sub_category"));
            entry.setInvoiceFileName(csv.getString("invoice_filename"));
            entry.setSapVendorId(csv.getString("sap_vendor_id"));
            entry.setExternalId(parseInt(csv.getString("external_id")));
            entry.setOrganisationId(parseInt(csv.getString("organisation_id")));
            entry.setProjectName(csv.getString("project_name"));
            entry.setProgrammeName(csv.getString("programme_name"));
            // "previous_block_id"
            if (StringUtils.isNotEmpty(csv.getString("managing_organisation_id"))) entry.setManagingOrganisation(new Organisation(csv.getInteger("managing_organisation_id"), ""));
            entry.setOriginalId(parseInt(csv.getString("original_id")));
//            entry.setSapDataId(parseInt(csv.getString("sap_data_id")));
            return entry;
        };

        List<ProjectLedgerEntry> ledgerEntries = new CSVFile(inputStream).loadData(ledgerEntryMapper);

        log.info("loaded {} ledger entries from CSV file", ledgerEntries.size());

//        projectLedgerRepository.deleteAll();
        projectLedgerRepository.save(ledgerEntries);
    }

}
