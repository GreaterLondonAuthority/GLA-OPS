/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.common.CSVFile;
import uk.gov.london.common.CSVRowSource;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.refdata.OutputCategoryConfiguration;
import uk.gov.london.ops.refdata.OutputType;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.project.outputs.OutputsCostsBlock;
import uk.gov.london.ops.domain.template.ProgrammeTemplate;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.payment.*;
import uk.gov.london.ops.repository.ClaimRepository;
import uk.gov.london.ops.repository.OutputCategoryAssumptionRepository;
import uk.gov.london.ops.repository.OutputTableEntryRepository;
import uk.gov.london.ops.refdata.OutputConfigurationService;
import uk.gov.london.ops.service.PermissionService;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.framework.MapResult;
import uk.gov.london.ops.framework.calendar.FinancialCalendar;
import uk.gov.london.ops.framework.exception.ValidationException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.project.Claim.ClaimType.ADVANCE;
import static uk.gov.london.ops.domain.project.Claim.ClaimType.QUARTER;
import static uk.gov.london.ops.domain.project.ClaimStatus.Claimed;
import static uk.gov.london.ops.domain.project.OutputTableEntry.Source.PCS;
import static uk.gov.london.ops.domain.project.SpendType.CAPITAL;
import static uk.gov.london.ops.domain.project.SpendType.REVENUE;
import static uk.gov.london.ops.service.PermissionType.PROJ_OUTPUTS_EDIT_FUTURE;
import static uk.gov.london.ops.service.PermissionType.PROJ_OUTPUTS_EDIT_PAST;

@Service
@Transactional
public class ProjectOutputsService extends BaseProjectService implements PostCloneNotificationListener, ProjectPaymentGenerator, ClaimGenerator<OutputsBlock> {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ClaimRepository claimRepository;

    @Autowired
    OutputTableEntryRepository outputTableEntryRepository;

    @Autowired
    OutputCategoryAssumptionRepository outputCategoryAssumptionRepository;

    @Autowired
    OutputConfigurationService outputConfigurationService;

    @Autowired
    UserService userService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    FinancialCalendar financialCalendar;

    public OutputsBlock getOutputsForFinancialYear(Integer projectId, Integer blockId, Integer financialYear) {
        Project project = get(projectId);
        OutputsBlock outputsBlock = (OutputsBlock) project.getProjectBlockById(blockId);
        OutputsCostsBlock costsBlock = (OutputsCostsBlock) get(projectId).getSingleLatestBlockOfType(ProjectBlockType.OutputsCosts);
        Set<OutputTableEntry> tableData = outputTableEntryRepository.findAllByBlockIdAndFinancialYear(outputsBlock.getId(), financialYear);
        outputsBlock.setTableData(tableData);

        List<OutputTableEntry> tableDataOrdered = tableData.stream()
                .sorted((Comparator.comparingInt(o -> o.getMonth() <= 3 ? o.getMonth() + 12 : o.getMonth())))
                .collect(Collectors.toList());


        boolean advancePaymentEnabled = outputsBlock.getShowAdvancePaymentColumn()
                && costsBlock != null
                && costsBlock.getSelectedRecoveryOutputId() != null
                && costsBlock.getAdvancePayment() != null;

        BigDecimal remainingAdvancePayment = null;

        for (OutputTableEntry outputTableEntry : tableDataOrdered) {
            outputTableEntry.setUnitCost(getUnitCostForOutput(costsBlock, outputTableEntry));
            if (costsBlock != null && advancePaymentEnabled && Objects.equals(costsBlock.getSelectedRecoveryOutputId(), outputTableEntry.getConfig().getId())) {
                if (remainingAdvancePayment == null) {
                    BigDecimal totalUnits = outputTableEntryRepository.getCumulativeAdvancePayment(costsBlock.getSelectedRecoveryOutputId(), financialYear, outputsBlock.getId());
                    BigDecimal cumulativeAdvancePayment = totalUnits == null ? BigDecimal.ZERO : outputTableEntry.getUnitCost().multiply(totalUnits);
                    remainingAdvancePayment = new BigDecimal(costsBlock.getAdvancePayment()).subtract(cumulativeAdvancePayment);
                }
                remainingAdvancePayment = processAdvancePayment(remainingAdvancePayment, outputTableEntry, outputsBlock);
            } else {
                processPayment(outputTableEntry);
            }
        }
        outputsBlock.setTableData(tableData);

        Set<OutputCategoryAssumption> assumptions = outputCategoryAssumptionRepository.findAllByBlockIdAndYear(outputsBlock.getId(), financialYear);
        outputsBlock.setAssumptions(assumptions);

        for (Integer yearMonth : outputTableEntryRepository.findPopulatedYearsForBlock(outputsBlock.getId())) {
            outputsBlock.getPopulatedYears().add(financialCalendar.financialFromYearMonth(yearMonth));
        }

        Claim advanceClaim = outputsBlock.getAdvancePaymentClaim();
        boolean missingAdvancePaymentApproval = (project.getAdvancePaymentAmount() != null && project.getAdvancePaymentAmount() > 0 && (advanceClaim == null || advanceClaim.getClaimStatus() != ClaimStatus.Approved));
        boolean hasEarlierMissingClaims = claimRepository.countPreviouslyUnclaimedOutputEntries(outputsBlock.getId(), financialYear, 4) > 0;

        outputsBlock.setProjectBudgetExceeded(checkOutputsBlockBudgetExceeded(project, outputsBlock.getId(), true));
        outputsBlock.setForecastsExceedingProjectBudget(checkOutputsBlockBudgetExceeded(project, outputsBlock.getId(), false));

        outputsBlock.setQuarters(OutputsQuarter.convertToQuarters(financialYear,
                                                                   tableData,
                                                                   outputsBlock.getOutputsClaims(),
                                                                   hasEarlierMissingClaims,
                                                                   missingAdvancePaymentApproval,
                                                                   project.getProgrammeTemplate().isPaymentsEnabled() && project.isClaimsEnabled(),
                                                                   outputsBlock.isProjectBudgetExceeded()));

        outputsBlock.setNextClaimableQuarter(getNextClaimableQuarter(outputsBlock.getId(), outputsBlock.getOutputsClaims()));


        return outputsBlock;
    }

    public boolean checkOutputsBlockBudgetExceeded(Project project, Integer blockId, boolean isActual) {
        Set<OutputTableEntry> tableData = outputTableEntryRepository.findAllByBlockId(blockId);
        OutputsCostsBlock outputCostsBlock = (OutputsCostsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.OutputsCosts);
        for (OutputTableEntry outputTableEntry : tableData) {
            outputTableEntry.setUnitCost(getUnitCostForOutput(outputCostsBlock, outputTableEntry));
        }
        if (tableData == null || outputCostsBlock == null) return false;

        BigDecimal spend = tableData.stream()
                .map(isActual ? OutputTableEntry::getActualTotal : OutputTableEntry::getForecastTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return outputCostsBlock.getTotalProjectSpend() != null && spend.compareTo(outputCostsBlock.getTotalProjectSpend()) > 0;
    }

    public OutputsQuarter getNextClaimableQuarter(Integer blockId, Set<Claim> claims) {
        List<Claim> quarterClaims = claims.stream().filter(c -> c.getClaimType() == QUARTER).collect(Collectors.toList());
        List<OutputTableEntry> allOutputBlockEntries = outputTableEntryRepository.findAllByBlockId(blockId)
                .stream().sorted(Comparator.comparing(OutputTableEntry::getYearMonth)).collect(Collectors.toList());

        if (allOutputBlockEntries == null || allOutputBlockEntries.isEmpty()) {
            return null;
        }

        OutputTableEntry firstClaimableOutputEntry = allOutputBlockEntries.stream().findFirst().orElse(null);

        if (quarterClaims != null && !quarterClaims.isEmpty()) {
            Claim latestClaim = Collections.max(quarterClaims, Comparator.comparing(c -> c.getYear() * 100 + c.getClaimTypePeriod()));
            if (latestClaim != null) {
                Integer quarter = latestClaim.getClaimTypePeriod();
                Integer year = quarter.equals(4) ? latestClaim.getYear() + 1 : latestClaim.getYear();
                Integer lastQuarterMonth = GlaUtils.getFirstMonthInQuarter(quarter) + 2;
                firstClaimableOutputEntry = allOutputBlockEntries.stream().filter(ote -> ote.getYearMonth() > (year * 100 + lastQuarterMonth)).findFirst().orElse(null);
            }
        }

        if (firstClaimableOutputEntry != null) {
            Integer fYearStart = firstClaimableOutputEntry.getMonth() < 4 ? firstClaimableOutputEntry.getYear() - 1 : firstClaimableOutputEntry.getYear();
            Integer quarter = GlaUtils.getCurrentQuarter(firstClaimableOutputEntry.getMonth());
            if (OutputsQuarter.isQuarterInThePast(fYearStart, quarter)) {
                return new OutputsQuarter(fYearStart, quarter, true);
            }
        }

        return null;
    }

    private BigDecimal getUnitCostForOutput(OutputsCostsBlock costsBlock, OutputTableEntry outputTableEntry) {
        return costsBlock == null ? null : costsBlock.getUnitCostForOutput(outputTableEntry.getConfig().getId());
    }

    private void processPayment(OutputTableEntry outputTableEntry) {
        outputTableEntry.setAmountClaimed(BigDecimal.ZERO);
        outputTableEntry.setClaimableAmount(outputTableEntry.getActualTotal());
    }

    private BigDecimal processAdvancePayment(BigDecimal remainingAdvancePayment, OutputTableEntry outputTableEntry, OutputsBlock outputsBlock) {

        // advance payment approved
        if (Objects.equals(ClaimStatus.Approved, outputsBlock.getAdvancePaymentClaim() == null ? null : outputsBlock.getAdvancePaymentClaim().getClaimStatus())) {
            // there is remaining advance payment leftover after output calculations
            if (remainingAdvancePayment.compareTo(outputTableEntry.getActualTotal()) > 0) {
                remainingAdvancePayment = remainingAdvancePayment.subtract(outputTableEntry.getActualTotal());
                outputTableEntry.setAmountClaimed(outputTableEntry.getActualTotal());
                outputTableEntry.setClaimableAmount(BigDecimal.ZERO);
            } else {
                outputTableEntry.setAmountClaimed(remainingAdvancePayment);
                outputTableEntry.setClaimableAmount(outputTableEntry.getActualTotal().subtract(remainingAdvancePayment));
                remainingAdvancePayment = BigDecimal.ZERO;
            }
        } else {
            outputTableEntry.setAmountClaimed(BigDecimal.ZERO);
            outputTableEntry.setClaimableAmount(outputTableEntry.getActualTotal());
        }
        outputTableEntry.setRemainingAdvancePayment(remainingAdvancePayment);
        return remainingAdvancePayment;
    }


    public OutputTableEntry createBaselineEntry(OutputsBlock outputsBlock, OutputTableEntry outputTableEntry) {
        OutputTableEntry oneByDateAndTypeInformation = outputTableEntryRepository.findBaselineBy(outputsBlock.getId(), outputTableEntry.getConfig().getId());

        if (oneByDateAndTypeInformation != null) {
            oneByDateAndTypeInformation.setBaseline(outputTableEntry.getBaseline());
            outputTableEntry = outputTableEntryRepository.save(oneByDateAndTypeInformation);
        } else {
            outputTableEntry = outputTableEntryRepository.save(outputTableEntry);
        }
        return outputTableEntry;
    }


    public OutputTableEntry createOutputEntry(Integer projectId, OutputTableEntry outputTableEntry) {
        OutputsBlock outputsBlock = checkAndConfigureEntry(projectId, outputTableEntry);
        if (outputTableEntry.getBaseline() == null) {
            return createBasicOutputTableEntry(outputsBlock, outputTableEntry);
        } else {
            return createBaselineEntry(outputsBlock, outputTableEntry);
        }
    }

    private OutputTableEntry createBasicOutputTableEntry(OutputsBlock outputsBlock, OutputTableEntry outputTableEntry) {
        OutputType outputType = outputConfigurationService.findOutputTypeByKey(outputTableEntry.getOutputType().getKey());
        if (outputType == null) {
            throw new ValidationException("Unrecognised output type");
        }

        outputTableEntry.setOutputType(outputType);
        OutputTableEntry oneByDateAndTypeInformation = outputTableEntryRepository.findOneByDateAndTypeInformation(
                outputsBlock.getId(), outputTableEntry.getYear(),
                outputTableEntry.getMonth(), outputTableEntry.getConfig().getId(),
                outputTableEntry.getOutputType().getKey());

        validateOutputEntry(outputsBlock, outputTableEntry);

        if (oneByDateAndTypeInformation != null) {
            if (outputTableEntry.getForecast() != null) {
                oneByDateAndTypeInformation.setForecast(outputTableEntry.getForecast());
            } else if (outputTableEntry.getActual() != null) {
                oneByDateAndTypeInformation.setActual(outputTableEntry.getActual());
            }
            outputTableEntry = outputTableEntryRepository.save(oneByDateAndTypeInformation);
        } else {
            outputTableEntry = outputTableEntryRepository.save(outputTableEntry);
        }
        return outputTableEntry;
    }

    private void validateOutputEntry(OutputsBlock outputsBlock, OutputTableEntry outputTableEntry) {
        List<Claim> claims = claimRepository.findAllByBlockId(outputsBlock.getId());
        Integer year = outputTableEntry.getMonth() < 4 ? outputTableEntry.getYear() - 1 : outputTableEntry.getYear();
        Integer quarter = GlaUtils.getQuarterFor(outputTableEntry.getMonth());
        if (claims != null && claims.stream().anyMatch(c -> c.getClaimType() == QUARTER && (c.getYear() > year || c.getYear().equals(year) && c.getClaimTypePeriod() >= quarter))) {
            throw new ValidationException("Output cannot be created on or before a claimed quarter");
        }
    }


    public void deleteOutputEntry(Integer projectId, Integer entryId) {
        Project project = get(projectId);

        OutputsBlock outputsBlock = (OutputsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Outputs);
        checkForLock(project.getProjectBlockById(outputsBlock.getId()));

        OutputTableEntry entry = outputTableEntryRepository.getOne(entryId);

        checkPermissionsForUpdate(project, entry);
        outputTableEntryRepository.deleteById(entryId);

        auditService.auditCurrentUserActivity(String.format("deleted output table entry %s/%s from month %d/%d",
                entry.getConfig().getCategory(), entry.getConfig().getSubcategory(), entry.getMonth(), entry.getYear()));

    }

    public OutputTableEntry updateOutputEntry(Integer projectId, OutputTableEntry outputTableEntry) {
        OutputsBlock outputsBlock = checkAndConfigureEntry(projectId, outputTableEntry);
        OutputTableEntry fromDB = outputTableEntryRepository.findById(outputTableEntry.getId()).orElse(null);
        if (fromDB == null) {
            throw new ValidationException("Unable to find table entry with ID: " + outputTableEntry.getId());
        }

        if (!outputsBlock.getId().equals(fromDB.getBlockId())) {
            throw new ValidationException("Attempt to update incorrect project block");
        }
        fromDB.setForecast(outputTableEntry.getForecast());
        fromDB.setActual(outputTableEntry.getActual());
        fromDB.setBaseline(outputTableEntry.getBaseline());
        return outputTableEntryRepository.save(fromDB);
    }

    private OutputsBlock checkAndConfigureEntry(Integer projectId, OutputTableEntry outputTableEntry) {
        Project project = get(projectId);

        OutputsBlock outputsBlock = (OutputsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Outputs);
        checkForLock(project.getProjectBlockById(outputsBlock.getId()));

        outputTableEntry.setProjectId(projectId);
        outputTableEntry.setBlockId(outputsBlock.getId());
        checkPermissionsForUpdate(project, outputTableEntry);
        return outputsBlock;
    }

    private void checkPermissionsForUpdate(Project project, OutputTableEntry entry) {
        if (permissionService.currentUserHasPermissionForOrganisation(PROJ_OUTPUTS_EDIT_FUTURE.getPermissionKey(), project.getOrganisation().getId())) {
            OffsetDateTime now = environment.now();
            Integer year = now.getMonthValue() < 4 ? now.getYear() - 1 : now.getYear();
            Integer financialYearStart = year * 100 + 4; // start of current financial year.
            Integer currentMonth = now.getYear() * 100 + now.getMonthValue(); // current month
            // reject entries in the past
            if (entry.getYearMonth() < financialYearStart) {
                if (permissionService.currentUserHasPermissionForOrganisation(PROJ_OUTPUTS_EDIT_PAST.getPermissionKey(), project.getOrganisation().getId())) {
                    return;
                } else {
                    throw new ValidationException("Unable to update outputs from previous financial years.");
                }
            } else if (entry.getYearMonth() > currentMonth && entry.getActual() != null) {
                throw new ValidationException("Unable to enter actuals in the future.");
            }
        } else {
            throw new ValidationException("Unable to update outputs data.");
        }
    }


    //TODO Temporally fix. This should be done by JPA detachment
    public List<MapResult> loadCSVData(CSVFile csv) {
        final List<MapResult> result = new ArrayList<>();
        final List<Map<String, Object>> tempData = new ArrayList<>();

        //First loop to identify which projects hasn't imported PCS outputs yet
        //In case the project has imported, it's added as MapResult error
        while (csv.nextRow()) {
            try {
                Integer pcsProjectId = csv.getInteger("PCS Project ID");
                Project project = getByLegacyProjectCode(pcsProjectId);

                String category = csv.getString("Category");
                String subCategory = csv.getString("Sub Category");
                getCategoryConfigurationOrThrowException(category, subCategory);

                if (project == null) {
                    throw new RuntimeException("project with PCD ID " + pcsProjectId + " not found!");
                }

                if (outputTableEntryRepository.countByProjectIdAndSource(project.getId(), PCS) > 0) {
                    throw new RuntimeException(
                            "Project with PCD ID " + pcsProjectId + " has imported outputs previously");
                }

                final Map<String, Object> entryMap = new HashMap<>();
                entryMap.put("project", project);
                entryMap.put("category", csv.getString("Category"));
                entryMap.put("subCategory", csv.getString("Sub Category"));
                entryMap.put("year", csv.getString("Year"));
                entryMap.put("month", csv.getInteger("Month"));
                entryMap.put("outputType", csv.getString("Output Type"));
                entryMap.put("actual", csv.getCurrencyValue("Actual"));
                entryMap.put("rowSource", csv.getCurrentRowSource());
                entryMap.put("rowIndex", csv.getRowIndex());
                tempData.add(entryMap);
            } catch (Exception e) {
                result.add(processParserException(e, csv));
            }

        }

        //Second loop to map the outputs. Any error is added as MapResult error
        for (Map<String, Object> entry : tempData) {
            try {
                result.add(mapOutputEntry(
                        (Project) entry.get("project"),
                        (String) entry.get("category"),
                        (String) entry.get("subCategory"),
                        (String) entry.get("year"),
                        (Integer) entry.get("month"),
                        (String) entry.get("outputType"),
                        (BigDecimal) entry.get("actual")
                ));
            } catch (Exception e) {
                result.add(new MapResult(
                        e.getMessage(),
                        true,
                        (Integer) entry.get("rowIndex"),
                        (String) entry.get("rowSource")));
            }
        }
        return result;
    }

    public MapResult loadCSVRow(CSVRowSource csv) {
        try {
            final Project project = getByLegacyProjectCode(csv.getInteger("PCS Project ID"));
            if (project == null) {
                throw new RuntimeException("project with PCD ID " + csv.getString("PCS Project ID") + " not found!");
            }

            return mapOutputEntry(
                    project,
                    csv.getString("Category"),
                    csv.getString("Sub Category"),
                    csv.getString("Year"),
                    csv.getInteger("Month"),
                    csv.getString("Output Type"),
                    csv.getCurrencyValue("Actual"));
        } catch (Exception e) {
            return processParserException(e, csv);
        }
    }


    private MapResult mapOutputEntry(Project project,
                                     String category,
                                     String subcategory,
                                     String yearS,
                                     Integer month,
                                     String outputTypeString,
                                     BigDecimal actual) {

        category = GlaUtils.superTrim(category);
        subcategory = GlaUtils.superTrim(subcategory);


        final OutputCategoryConfiguration opsCategory = getCategoryConfigurationOrThrowException(
                category,
                subcategory);

        //Mapping calendar yearMonth from financial yearMonth in CSV
        final YearMonth calendarYearMonth = financialCalendar.calendarFromFinancialYearMonth(
                financialCalendar.parseFinancialYearString(yearS),
                month);

        final OutputType outputType = outputConfigurationService.findOutputTypeByKey(outputTypeString);
        if (outputType == null) {
            throw new RuntimeException(String.format("OutputType %s doesn't match", outputTypeString));
        }

        final int blockId = getOutputBlockIdFromProject(project);
        OutputTableEntry entry = outputTableEntryRepository.findOneByDateAndTypeInformation(
                blockId,
                calendarYearMonth.getYear(),
                calendarYearMonth.getMonthValue(),
                opsCategory.getId(),
                outputType.getKey());
        if (entry != null) {
            entry.setActual(actual);
            entry.setSource(PCS);
            entry.setModifiedBy("PCS data import");
            entry.setModifiedOn(environment.now());
        } else {
            entry = new OutputTableEntry(
                    project.getId(),
                    blockId,
                    opsCategory,
                    outputType,
                    calendarYearMonth.getYear(),
                    calendarYearMonth.getMonthValue(),
                    BigDecimal.ZERO,
                    actual,
                    PCS);
            entry.setCreatedBy("PCS data import");
            entry.setCreatedOn(environment.now());
        }
        return new MapResult<>(entry);

    }

    private OutputCategoryConfiguration getCategoryConfigurationOrThrowException(String category, String subcategory) {
        OutputCategoryConfiguration opsCategory = outputConfigurationService.findByCategoryAndSubcategory(category, subcategory);
        if (opsCategory == null) {
            throw new RuntimeException("Unable to match outputs category: " + category + "/" + subcategory);
        }
        return opsCategory;
    }

    /**
     * Get the outputs block id from the project
     */
    public int getOutputBlockIdFromProject(final Project project) {
        final int id = project.getId();
        return project.getLatestProjectBlocks().stream()
                .filter(b -> ProjectBlockType.Outputs.equals(b.getBlockType()))
                .map(NamedProjectBlock::getId)
                .filter(i -> i != null)
                .findFirst()
                .orElseThrow(() -> new ValidationException("Project ID " + id + " doesn't have output block"));
    }


    private MapResult processParserException(Exception e, CSVRowSource csv) {
        String rowSource;
        try {
            rowSource = csv.getCurrentRowSource();
        } catch (Exception ex) {
            rowSource = "Not able to get source: " + ex.getMessage();
        }
        return new MapResult<>(
                e.getMessage(),
                true,
                csv.getRowIndex(),
                rowSource);
    }

    @Override
    public void handleProjectClone(Project project, Integer originalBlockId, Project newProject, Integer newBlockId) {
        NamedProjectBlock projectBlockById = project.getProjectBlockById(originalBlockId);
        // check if correct block type
        if (projectBlockById == null || !ProjectBlockType.Outputs.equals(projectBlockById.getBlockType())) {
            return;
        }
        cloneOutputTableEntries(project, originalBlockId, newProject, newBlockId);
        cloneOutputTableAssumptions(project, originalBlockId, newProject, newBlockId);
    }

    private void cloneOutputTableAssumptions(Project project, Integer originalBlockId, Project newProject, Integer newBlockId) {
        Set<OutputCategoryAssumption> toClone = outputCategoryAssumptionRepository.findAllByBlockId(originalBlockId);

        if (toClone == null || toClone.isEmpty()) {
            return;
        }

        Set<OutputCategoryAssumption> toCreate = new HashSet<>();
        for (OutputCategoryAssumption assumption : toClone) {
            OutputCategoryAssumption clone = new OutputCategoryAssumption(
                    newProject.getId(), newBlockId, assumption.getCategory(), assumption.getAssumption(), assumption.getYear(), assumption.getCreatedOn(), assumption.getCreatedBy());
            toCreate.add(clone);
        }
        log.debug(String.format("Cloned %d OutputCategoryAssumptions for project %d", toCreate.size(), project.getId()));
        outputCategoryAssumptionRepository.saveAll(toCreate);


    }

    private void cloneOutputTableEntries(Project project, Integer originalBlockId, Project newProject, Integer newBlockId) {
        Set<OutputTableEntry> toClone = outputTableEntryRepository.findAllByBlockId(originalBlockId);

        if (toClone == null || toClone.isEmpty()) {
            return;
        }

        Set<OutputTableEntry> toCreate = new HashSet<>();
        for (OutputTableEntry ote : toClone) {
            OutputTableEntry clone = new OutputTableEntry(
                    newProject.getId(), newBlockId, ote.getConfig(), ote.getOutputType(),
                    ote.getYear(), ote.getMonth(), ote.getForecast(),
                    ote.getActual(), ote.getSource());
            clone.setSource(ote.getSource());
            clone.setCreatedOn(ote.getCreatedOn());
            clone.setCreatedBy(ote.getCreatedBy());
            toCreate.add(clone);
        }
        log.debug(String.format("Cloned %d OutputTableEntries for project %d", toCreate.size(), project.getId()));
        outputTableEntryRepository.saveAll(toCreate);
    }

    @Override
    public void handleBlockClone(Project project, Integer originalBlockId, Integer newBlockId) {
        handleProjectClone(project, originalBlockId, project, newBlockId);
    }

    public Set<OutputTableEntry> getOutputsForBaseline(Integer id, Integer blockId) {
        Set<OutputTableEntry> tableData = outputTableEntryRepository.findAllBaselineData(blockId);
        return tableData;

    }

    public Set<OutputCategoryAssumption> getOutputAssumptions(Integer blockId, Integer year) {
        return outputCategoryAssumptionRepository.findAllByBlockIdAndYear(blockId, year);
    }

    public void addAssumption(Integer id, Integer blockId, OutputCategoryAssumption assumption) {
        Project project = get(id);
        NamedProjectBlock projectBlockById1 = project.getProjectBlockById(blockId);
        if (projectBlockById1 == null || !(projectBlockById1 instanceof OutputsBlock)) {
            throw new ValidationException("Invalid project block to add assumption to");
        }

        assumption.setBlockId(blockId);
        assumption.setProjectId(id);

        outputCategoryAssumptionRepository.save(assumption);
        this.updateProject(project);


    }

    public void updateAssumption(Integer id, Integer blockId, OutputCategoryAssumption assumption) {
        Project project = get(id);

        OutputCategoryAssumption assumptionDB = outputCategoryAssumptionRepository.getOne(assumption.getId());
        if (!assumptionDB.getBlockId().equals(blockId)) {
            throw new ValidationException("Unable to update this assumption");
        }

        assumptionDB.setAssumption(assumption.getAssumption());
        outputCategoryAssumptionRepository.save(assumptionDB);
        updateProject(project);

    }

    public void deleteAssumption(Integer id, Integer blockId, Integer assumption) {
        Project project = get(id);

        OutputCategoryAssumption assumptionDB = outputCategoryAssumptionRepository.getOne(assumption);
        if (!assumptionDB.getBlockId().equals(blockId)) {
            throw new ValidationException("Unable to delete this assumption");
        }

        outputCategoryAssumptionRepository.deleteById(assumption);
        updateProject(project);
    }


    @Override
    public PaymentGroup generatePaymentsForProject(Project project, String approvalRequestedBy) {
        OutputsBlock outputsBlock = (OutputsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Outputs);
        OutputsCostsBlock costs = (OutputsCostsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.OutputsCosts);
        if (outputsBlock != null && outputsBlock.getApprovalWillCreatePendingPayment()) {
            List<ProjectLedgerEntry> ples = new ArrayList<>();
            for (Claim outputsClaim : outputsBlock.getOutputsClaims()) {
                if (ADVANCE.equals(outputsClaim.getClaimType())) {
                    ples.add(createAdvancePaymentFor(project, outputsBlock, costs.getAdvancePayment()));
                } else {
                    ples.add(createQuarterlyaymentFor(project, outputsBlock, outputsClaim));
                }
            }
            return paymentService.createPaymentGroup(approvalRequestedBy, ples);
        }

        return null;
    }

    @Override
    public void generateClaim(Project project, OutputsBlock block, Claim claim) {
        OutputsBlock outputsBlock = block;
        if (ADVANCE == claim.getClaimType()) {
            checkForLock(block);
            if (project.getAdvancePaymentAmount() == null || project.getAdvancePaymentAmount() == 0) {
                throw new ValidationException("Unable to claim zero advance payment amount.");
            }

            if (block.isAdvancePaymentClaimed()) {
                throw new ValidationException("Existing advance payment claim already exists.");
            }

            claim.setAmount(new BigDecimal(project.getAdvancePaymentAmount()));
        } else {
            outputsBlock = this.getOutputsForFinancialYear(project.getId(), block.getId(), claim.getYear());
            checkForLock(outputsBlock);
            OffsetDateTime now = environment.now();
            int currentQuarter = GlaUtils.getCurrentQuarter(now.getMonthValue());
            int currentYear = now.getYear();
            int currentFinancialYear = currentQuarter == 4 ? currentYear - 1 : currentYear;


            if (currentFinancialYear < claim.getYear() ||
                    (currentFinancialYear == claim.getYear() && currentQuarter <= claim.getClaimTypePeriod())) {
                throw new ValidationException("Only able to claim historic quarters");
            }

            int yearToSearchFor = claim.getClaimTypePeriod() == 4 ? claim.getYear() - 1 : claim.getYear();
            BigDecimal total = outputsBlock.getTableData().stream()
                    .filter(t -> t.getYear().equals(yearToSearchFor) && GlaUtils.getCurrentQuarter(t.getMonth()) == claim.getClaimTypePeriod() && t.getActualTotal() != null)
                    .map(OutputTableEntry::getActualTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            claim.setAmount(total);
            claim.setClaimType(QUARTER);

            Set<Claim> outputsClaims = outputsBlock.getOutputsClaims();
            Claim lastClaim = getLastBlockClaim(outputsClaims);
            if (lastClaim != null) {
                createClaimsForEmptyQuarters(block, lastClaim, claim);
            }
        }

        outputsBlock.getOutputsClaims().add(claim);
    }

    @Override
    public boolean handleClaimDeletion(OutputsBlock block2, Claim claim) {
        Project project = get(block2.getProjectId());
        OutputsBlock block = (OutputsBlock) project.getProjectBlockById(block2.getId());
        Set<Claim> outputsClaims = block.getOutputsClaims();
        if (block.getBlockType() == ProjectBlockType.Outputs && claim.getClaimType() != Claim.ClaimType.ADVANCE) {
//            Claim latestClaim = this.getLastBlockClaim(block.getOutputsClaims());
//            if (!latestClaim.getId().equals(claim.getId())) {
//                throw new ValidationException("Later claims must be canceled first");
//            }

            List<OutputTableEntry> allEntries = outputTableEntryRepository.findAllByBlockId(block.getId())
                    .stream()
                    .sorted(Comparator.comparingInt(OutputTableEntry::getYearMonth))
                    .filter(ote -> ote.getYearMonth() < claim.getClaimYearMonth())
                    .collect(Collectors.toList());


            if (!allEntries.isEmpty()) {
                OutputTableEntry lastManuallyClaimedEntry = allEntries.get(allEntries.size() - 1);

                outputsClaims.removeIf(c -> !Claim.ClaimType.ADVANCE.equals(c.getClaimType())
                        && c.getClaimYearMonth() > lastManuallyClaimedEntry.getYearMonth()
                        && c.getAmount().compareTo(BigDecimal.ZERO) == 0
                        && c.getClaimYearMonth() < claim.getClaimYearMonth());
            }


        }

        outputsClaims.removeIf(c -> Objects.equals(c.getClaimType(), QUARTER) && c.getClaimYearMonth() > claim.getClaimYearMonth());

        if (outputsClaims.contains(claim)){
            claimRepository.deleteById(claim.getId());
            outputsClaims.remove(claim);
        }


        this.updateProject(project);
        return true;
    }

    public Claim getLastBlockClaim(Set<Claim> outputsClaims) {
        return outputsClaims.stream()
                .filter(outputClaim -> !Claim.ClaimType.ADVANCE.equals(outputClaim.getClaimType()))
                .max(Comparator.comparingInt(Claim::getYearPeriod))
                .orElse(null);
    }

    private void createClaimsForEmptyQuarters(OutputsBlock block, Claim lastClaim, Claim newClaim) {
        Integer year = lastClaim.getYear();
        Integer quarter = lastClaim.getClaimTypePeriod();

        year = quarter == 4 ? year + 1 : year;
        quarter = quarter == 4 ? 1 : quarter + 1;

        while (year < newClaim.getYear() || year.equals(newClaim.getYear()) && quarter < newClaim.getClaimTypePeriod()) {
            Claim missingClaim = new Claim(year, quarter, Claimed);
            missingClaim.setClaimType(Claim.ClaimType.QUARTER);
            missingClaim.setAmount(BigDecimal.ZERO);
            missingClaim.setBlockId(block.getId());
            missingClaim.setClaimedOn(environment.now());
            block.getOutputsClaims().add(missingClaim);
            year = quarter == 4 ? year + 1 : year;
            quarter = quarter == 4 ? 1 : quarter + 1;
        }
    }

    private ProjectLedgerEntry createAdvancePaymentFor(Project project, OutputsBlock outputsBlock, Integer grant) {
        return paymentService.createPayment(project,
                outputsBlock.getId(),
                LedgerType.PAYMENT,
                getPaymentSource(outputsBlock),
                LedgerStatus.Pending,
                ProgrammeTemplate.WbsCodeType.Capital.equals(project.getProgrammeTemplate().getDefaultWbsCodeType()) ? CAPITAL : REVENUE,
                "Outputs",
                String.format("Advance Payment"),
                new BigDecimal(grant).negate(),
                environment.now().getYear(),
                environment.now().getMonthValue(),
                null,
                LedgerSource.WebUI);
    }

    private ProjectLedgerEntry createQuarterlyaymentFor(Project project, OutputsBlock outputsBlock, Claim outputsClaim) {
        return paymentService.createPayment(project,
                outputsBlock.getId(),
                LedgerType.PAYMENT,
                getPaymentSource(outputsBlock),
                LedgerStatus.Pending,
                ProgrammeTemplate.WbsCodeType.Capital.equals(project.getProgrammeTemplate().getDefaultWbsCodeType()) ? CAPITAL : REVENUE,
                "Outputs",
                String.format("Q%d %s", outputsClaim.getClaimTypePeriod(), GlaUtils.getFinancialYearFromYear(outputsClaim.getYear())),
                outputsClaim.getAmount().negate(),
                environment.now().getYear(),
                environment.now().getMonthValue(),
                null,
                LedgerSource.WebUI);
    }

    PaymentSource getPaymentSource(OutputsBlock outputsBlock) {
        Set<PaymentSource> paymentSources = outputsBlock.getPaymentSources();
        if (paymentSources.isEmpty()){
            TemplateBlock singleBlockByType = outputsBlock.getProject().getTemplate().getSingleBlockByType(ProjectBlockType.Outputs);
            paymentSources = singleBlockByType.getPaymentSources();
            if (paymentSources.size() != 1) {
                throw new ValidationException("Unable to determine payment Source for this outputs block.");
            }
            return paymentSources.iterator().next();
        }
        if (paymentSources.size() != 1) {
            throw new ValidationException("Too many payments sources specified for outputs block");
        }
        return paymentSources.iterator().next();
    }

}

