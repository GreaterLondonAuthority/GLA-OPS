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
import uk.gov.london.ops.domain.outputs.OutputCategoryConfiguration;
import uk.gov.london.ops.domain.outputs.OutputType;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.mapper.model.MapResult;
import uk.gov.london.ops.repository.OutputTableEntryRepository;
import uk.gov.london.ops.repository.ProjectOutputConfigurationRepository;
import uk.gov.london.ops.service.OutputConfigurationService;
import uk.gov.london.ops.service.PermissionService;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.service.finance.FinancialCalendar;
import uk.gov.london.ops.util.CSVFile;
import uk.gov.london.ops.util.CSVRowSource;
import uk.gov.london.ops.util.GlaOpsUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;

import static uk.gov.london.ops.domain.project.OutputTableEntry.Source.PCS;

@Service
@Transactional
public class ProjectOutputsService extends BaseProjectService implements PostCloneNotificationListener {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    OutputTableEntryRepository outputTableEntryRepository;

    @Autowired
    OutputConfigurationService outputConfigurationService;

    @Autowired
    UserService userService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    ProjectOutputConfigurationRepository projectOutputConfigurationRepository;

    @Autowired
    FinancialCalendar financialCalendar;

    public OutputsBlock getOutputsForFinancialYear(Integer projectId, Integer blockId, Integer financialYear) {
        OutputsBlock outputsBlock = (OutputsBlock) get(projectId).getProjectBlockById(blockId);

        Set<OutputTableEntry> tableData = outputTableEntryRepository.findAllByBlockIdAndFinancialYear(outputsBlock.getId(), financialYear);
        outputsBlock.setTableData(tableData);

        for (Integer yearMonth: outputTableEntryRepository.findPopulatedYearsForBlock(outputsBlock.getId())) {
            outputsBlock.getPopulatedYears().add(financialCalendar.financialFromYearMonth(yearMonth));
        }

        return outputsBlock;
    }


    public OutputTableEntry createOutputEntry(Integer projectId, OutputTableEntry outputTableEntry) {

        OutputsBlock outputsBlock = checkAndConfigureEntry(projectId, outputTableEntry);


        OutputType outputType = outputConfigurationService.findOutputTypeByKey(outputTableEntry.getOutputType().getKey());
        if (outputType == null) {
            throw new ValidationException("Unrecognied output type");
        }
        outputTableEntry.setOutputType(outputType);
        OutputTableEntry oneByDateAndTypeInformation = outputTableEntryRepository.findOneByDateAndTypeInformation(
                outputsBlock.getId(), outputTableEntry.getYear(),
                outputTableEntry.getMonth(), outputTableEntry.getConfig().getId(),
                outputTableEntry.getOutputType().getKey());
        if (oneByDateAndTypeInformation != null) {
            if (outputTableEntry.getForecast() != null) {
                oneByDateAndTypeInformation.setForecast(outputTableEntry.getForecast());
            } else if (outputTableEntry.getActual() != null) {
                oneByDateAndTypeInformation.setActual(outputTableEntry.getActual());
            }
            outputTableEntry = outputTableEntryRepository.save(oneByDateAndTypeInformation);
        } else {
            outputTableEntry =outputTableEntryRepository.save(outputTableEntry);
        }
        return outputTableEntry;


    }


    public void deleteOutputEntry(Integer projectId, Integer entryId) {
        Project project = get(projectId);

        OutputsBlock outputsBlock = (OutputsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Outputs);
        checkForLock(project.getProjectBlockById(outputsBlock.getId()));

        OutputTableEntry entry = outputTableEntryRepository.findOne(entryId);

        checkPermissionsForUpdate(project, entry);
        outputTableEntryRepository.delete(entryId);

        auditService.auditCurrentUserActivity(String.format("deleted output table entry %s/%s from month %d/%d",
                entry.getConfig().getCategory(),entry.getConfig().getSubcategory(),  entry.getMonth(), entry.getYear()));

    }

    public OutputTableEntry updateOutputEntry(Integer projectId, OutputTableEntry outputTableEntry) {
        OutputsBlock outputsBlock = checkAndConfigureEntry(projectId, outputTableEntry);
        OutputTableEntry fromDB = outputTableEntryRepository.findOne(outputTableEntry.getId());
        if (fromDB == null) {
            throw new ValidationException("Unable to find table entry with ID: " + outputTableEntry.getId());
        }

        if (!outputsBlock.getId().equals(fromDB.getBlockId())) {
            throw new ValidationException("Attempt to update incorrect project block");
        }
        fromDB.setForecast(outputTableEntry.getForecast());
        fromDB.setActual(outputTableEntry.getActual());
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
        if (permissionService.currentUserHasPermissionForOrganisation(PermissionService.PROJ_OUTPUTS_EDIT_FUTURE, project.getOrganisation().getId())) {
            OffsetDateTime now = environment.now();
            Integer year = now.getMonthValue() < 4 ? now.getYear() -1 : now.getYear();
            Integer financialYearStart = year * 100 + 4; // start of current financial year.
            Integer currentMonth =  now.getYear() * 100 + now.getMonthValue(); // current month
            // reject entries in the past
            if (entry.getYearMonth() < financialYearStart) {
                if (permissionService.currentUserHasPermissionForOrganisation(PermissionService.PROJ_OUTPUTS_EDIT_PAST, project.getOrganisation().getId())) {
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
        while(csv.nextRow()) {
            try{
                Integer pcsProjectId = csv.getInteger("PCS Project ID");
                Project project = getByLegacyProjectCode(pcsProjectId);

                String category = csv.getString("Category");
                String subCategory = csv.getString("Sub Category");
                getCategoryConfigurationOrThrowException(category, subCategory);

                if (project == null) {
                    throw new RuntimeException("project with PCD ID "+pcsProjectId+" not found!");
                }

                if(outputTableEntryRepository.countByProjectIdAndSource(project.getId(), PCS) > 0) {
                    throw new RuntimeException(
                            "Project with PCD ID "+ pcsProjectId +" has imported outputs previously");
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
            }catch(Exception e) {
                result.add(processParserException(e, csv));
            }

        }

        //Second loop to map the outputs. Any error is added as MapResult error
        for(Map<String, Object> entry : tempData) {
            try{
                result.add(mapOutputEntry(
                        (Project) entry.get("project"),
                        (String)entry.get("category"),
                        (String)entry.get("subCategory"),
                        (String)entry.get("year"),
                        (Integer) entry.get("month"),
                        (String)entry.get("outputType"),
                        (BigDecimal) entry.get("actual")
                ));
            }catch(Exception e) {
                result.add(new MapResult(
                        e.getMessage(),
                        true,
                        (Integer)entry.get("rowIndex"),
                        (String)entry.get("rowSource")));
            }
        }
        return result;
    }

    public MapResult loadCSVRow(CSVRowSource csv) {
        try{
            final Project project = getByLegacyProjectCode(csv.getInteger("PCS Project ID"));
            if (project == null) {
                throw new RuntimeException("project with PCD ID "+csv.getString("PCS Project ID")+" not found!");
            }

            return mapOutputEntry(
                    project,
                    csv.getString("Category"),
                    csv.getString("Sub Category"),
                    csv.getString("Year"),
                    csv.getInteger("Month"),
                    csv.getString("Output Type"),
                    csv.getCurrencyValue("Actual"));
        } catch(Exception e) {
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

        category = GlaOpsUtils.superTrim(category);
        subcategory = GlaOpsUtils.superTrim(subcategory);


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

    private OutputCategoryConfiguration getCategoryConfigurationOrThrowException(final String category,
                                                                                 final String subcategory) {
        final String categoryTrim = GlaOpsUtils.superTrim(category);
        final String subCategoryTrim = GlaOpsUtils.superTrim(subcategory);
        final OutputCategoryConfiguration opsCategory = projectOutputConfigurationRepository
                .findByCategoryAndSubcategory(
                        GlaOpsUtils.superTrim(categoryTrim),
                        GlaOpsUtils.superTrim(subCategoryTrim));
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
                .filter(b-> ProjectBlockType.Outputs.equals(b.getBlockType()))
                .map(NamedProjectBlock::getId)
                .filter(i-> i!= null)
                .findFirst()
                .orElseThrow(()-> new ValidationException("Project ID "+ id + " doesn't have output block"));
    }



    private MapResult processParserException(Exception e, CSVRowSource csv) {
        String rowSource;
        try{
            rowSource = csv.getCurrentRowSource();
        } catch (Exception ex) {
            rowSource = "Not able to get source: "+ex.getMessage();
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
        outputTableEntryRepository.save(toCreate);
    }

    @Override
    public void handleBlockClone(Project project, Integer originalBlockId, Integer newBlockId) {
        handleProjectClone(project, originalBlockId, project, newBlockId);
    }
}

