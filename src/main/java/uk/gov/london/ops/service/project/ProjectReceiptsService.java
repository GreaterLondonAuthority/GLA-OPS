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
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.finance.FinanceService;
import uk.gov.london.ops.web.model.ProjectLedgerItemRequest;
import uk.gov.london.ops.web.model.project.AnnualReceiptsSummary;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ProjectReceiptsService extends BaseProjectService implements PostCloneNotificationListener, EnrichmentRequiredListener {

    static final int MAX_WBS_CODES = 10;

    @Autowired
    FinanceService financeService;

    public ReceiptsBlock getProjectReceiptsBlock(Integer id, Integer blockId, Integer year) {
        ReceiptsBlock receiptsBlock = (ReceiptsBlock) get(id).getProjectBlockById(blockId);

        AnnualReceiptsSummary annualReceiptSummary = financeService.getAnnualReceiptSummaryForYear(receiptsBlock, year);
        Set<Integer> populatedYears = financeService.getPopulatedYearsForBlock(receiptsBlock.getId());

        // TODO Remove single summary
        receiptsBlock.setAnnualReceiptsSummary(annualReceiptSummary);
        receiptsBlock.getAnnualReceiptsSummaries().add(annualReceiptSummary);
        receiptsBlock.setPopulatedYears(populatedYears);

        return receiptsBlock;
    }

    public ReceiptsBlock updateProjectReceiptsBlock(Integer projectId, ReceiptsBlock updatedBlock, boolean autosave) {
        validateChanges(updatedBlock);

        Project project = get(projectId);

        checkForLock(project.getReceiptsBlock());

        project.getReceiptsBlock().merge(updatedBlock);

        releaseOrRefreshLock(project.getReceiptsBlock(), !autosave);

        return updateProject(project).getReceiptsBlock();
    }

    public ReceiptsBlock addReceiptEntry(Integer projectId, Integer year, ProjectLedgerItemRequest itemRequest) {
        ReceiptsBlock receiptsBlock = get(projectId).getReceiptsBlock();
        checkForLock(receiptsBlock);

        itemRequest.setBlockId(receiptsBlock.getId());
        itemRequest.setProjectId(projectId);

        financeService.addReceiptEntry(itemRequest);

        return getProjectReceiptsBlock(projectId, receiptsBlock.getId(), year);
    }

    public void editReceiptForecast(Integer projectId, Integer forecastId, BigDecimal value) {
        checkForLock(get(projectId).getReceiptsBlock());
        financeService.editReceiptEntry(forecastId, value);
    }

    public List<SAPMetaData> getReceiptsMetaData(Integer projectId, Integer blockId, Integer categoryId, Integer yearMonth) {
        return projectLedgerRepository.getSapMetaData(projectId, blockId, yearMonth, LedgerType.RECEIPT, LedgerStatus.ACTUAL, categoryId);
    }

    private void validateChanges(ReceiptsBlock updatedBlock) {
        if (updatedBlock.getWbsCodes().size() > MAX_WBS_CODES) {
            throw new ValidationException("cannot have more than "+MAX_WBS_CODES+" WBS codes in the receipts block!");
        }

        for (WbsCode wbsCode: updatedBlock.getWbsCodes()) {
            if (updatedBlock.getWbsCodes().stream().filter(c -> c.getCode().equals(wbsCode.getCode())).count() > 1) {
                throw new ValidationException("cannot have duplicate WBS codes in the receipts block! code: "+wbsCode.getCode());
            }
        }
    }

    @Override
    public void handleBlockClone(Project project, Integer originalBlockId, Integer newBlockId) {
        NamedProjectBlock projectBlockById = project.getProjectBlockById(originalBlockId);
        // check if correct block type
        if (projectBlockById != null && (ProjectBlockType.Receipts.equals(projectBlockById.getBlockType()))) {
            financeService.cloneLedgerEntriesForBlock(originalBlockId, newBlockId);
        }
    }

    @Override
    public void handleProjectClone(Project oldProject, Integer originalBlockId, Project newProject, Integer newBlockId) {
        NamedProjectBlock projectBlockById = oldProject.getProjectBlockById(originalBlockId);
        // check if correct block type
        if (projectBlockById != null && (ProjectBlockType.Receipts.equals(projectBlockById.getBlockType()))) {
            financeService.cloneLedgerEntriesForBlock(originalBlockId, newProject.getId(), newBlockId);
        }
    }

    @Override
    public void enrichProject(Project project, boolean enrichmentForComparison) {
        if (enrichmentForComparison) {
            Optional<NamedProjectBlock> first = project.getProjectBlocksSorted().stream().filter(pb -> pb.getBlockType().equals(ProjectBlockType.Receipts)).findFirst();
            if (first.isPresent()) {
                ReceiptsBlock receiptsBlock = (ReceiptsBlock) first.get();
                if (receiptsBlock.getPopulatedYears() != null && !receiptsBlock.getPopulatedYears().isEmpty()) {
                    return; // prevent duplicate enrichment
                }
                Set<Integer> populatedYears = financeService.getPopulatedYearsForBlock(receiptsBlock.getId());
                for (Integer year : populatedYears) {
                    AnnualReceiptsSummary annualReceiptSummary = financeService.getAnnualReceiptSummaryForYear(receiptsBlock, year);
                    receiptsBlock.getAnnualReceiptsSummaries().add(annualReceiptSummary);
                }
                Collections.sort(receiptsBlock.getAnnualReceiptsSummaries());
            }
        }
    }
}
