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
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.attachment.ProjectBudgetsAttachment;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.project.implementation.AnnualSpendSummaryMapper;
import uk.gov.london.ops.project.implementation.ProjectBudgetsSummaryMapper;
import uk.gov.london.ops.repository.AnnualSpendSummaryRecordRepository;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.web.model.AnnualSpendSummary;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ProjectBudgetsService extends BaseProjectFinanceService implements EnrichmentRequiredListener  {

    static final int MAX_WBS_CODES = 10;

    @Autowired
    AnnualSpendSummaryRecordRepository annualSpendSummaryRecordRepository;

    @Autowired
    AnnualSpendSummaryMapper annualSpendSummaryMapper;

    @Autowired
    private ProjectBudgetsSummaryMapper projectBudgetsSummaryMapper;

    @Autowired
    private Environment environment;

    @Override
    ProjectBlockType getBlockType() {
        return ProjectBlockType.ProjectBudgets;
    }

    public ProjectBudgetsBlock getProjectBudgets(Project project, Integer blockid) {
        ProjectBudgetsBlock projectBlockById = (ProjectBudgetsBlock) project.getProjectBlockById(blockid);
        return getProjectBudgetsBlockIncludingCalculatedData(project,projectBlockById);
    }

    private ProjectBudgetsBlock getProjectBudgetsBlockIncludingCalculatedData(Project project, ProjectBudgetsBlock projectBudgetsBlock ) {
        // temporary update from annual spend

        int currentYearMonth = environment.now().getYear() * 100 + environment.now().getMonthValue();

        setPopulatedYears(projectBudgetsBlock);
        Integer from = projectBudgetsBlock.getFromFinancialYear();
        if (from != null) {
            Integer to = projectBudgetsBlock.getToFinancialYear();
            if (to == null) {
                to = from + 40; // use all annual spend data
            }

            List<AnnualSpendSummaryRecord> records = annualSpendSummaryRecordRepository.
                    // adding one here as we want to include the final year
                    findByProjectIdAndBlockIdAndFinancialYearBetweenOrderByFinancialYearAsc(
                            project.getId(), projectBudgetsBlock.getId(), from, to +1);
            projectBudgetsSummaryMapper.mapProjectBudgets(projectBudgetsBlock, records,currentYearMonth, from, to);

            int fromYearMonth = from * 100 +4;
            int toYearMonth = (to + 1) * 100 +4;
            OffsetDateTime now = environment.now();
            int nowYearMonth = now.getYear() * 100 + now.getMonthValue();


            List<ProjectLedgerEntry> entries = financeService
                    .findHistoricActualsAndFutureForecasts(
                            projectBudgetsBlock.getId(),
                            fromYearMonth,
                            nowYearMonth,
                            toYearMonth);
            projectBudgetsSummaryMapper.mapTotalsTiles(projectBudgetsBlock, entries);

        }
        return projectBudgetsBlock;
    }

    public Project updateProjectBudgets(Project project, ProjectBudgetsBlock projectBudgetsBlock, boolean releaseLock) {
        checkForLock(project.getSingleLatestBlockOfType(projectBudgetsBlock.getBlockType()));

        validateWbsCodes(projectBudgetsBlock, project.getId());

        Set<ProjectBudgetsAttachment> deletedAttachments = new HashSet<>(project.getProjectBudgetsBlock().getAttachments());
        deletedAttachments.removeAll(projectBudgetsBlock.getAttachments());
        for (ProjectBudgetsAttachment attachment: deletedAttachments) {
            auditService.auditCurrentUserActivity("Attachment "+attachment.getFileName()+" was deleted on project Spend block "+projectBudgetsBlock.getId());
        }

        // init data for new attachments
        projectBudgetsBlock.getAttachments().stream().filter(attachment -> attachment.getId() == null).forEach(attachment -> {
            attachment.setCreatedOn(environment.now());
            attachment.setCreator(userService.currentUser());
            auditService.auditCurrentUserActivity(String.format("document %s with ID %d of type %s attached with total revenue %d and total capital %d",
                    attachment.getFileName(), attachment.getFileId(), attachment.getDocumentType(), projectBudgetsBlock.getRevenue(), projectBudgetsBlock.getCapital()));
        });

        project.getProjectBudgetsBlock().merge(projectBudgetsBlock);

        releaseOrRefreshLock(project.getSingleLatestBlockOfType(projectBudgetsBlock.getBlockType()), releaseLock);

        return this.updateProject(project);
    }

    private void validateWbsCodes(ProjectBudgetsBlock projectBudgetsBlock, Integer projectId) {
        Set<WbsCode> capitalWbsCodes = projectBudgetsBlock.getWbsCodes(SpendType.CAPITAL);
        Set<WbsCode> revenueWbsCodes = projectBudgetsBlock.getWbsCodes(SpendType.REVENUE);

        if (capitalWbsCodes.size() > MAX_WBS_CODES) {
            throw new ValidationException("cannot enter more than "+MAX_WBS_CODES+" capital wbs codes");
        }

        if (revenueWbsCodes.size() > MAX_WBS_CODES) {
            throw new ValidationException("cannot enter more than "+MAX_WBS_CODES+" revenue wbs codes");
        }

        for (WbsCode wbsCode: projectBudgetsBlock.getWbsCodes()) {
            if (wbsCode.getId() == null && isWbsCodeUsedInProjectsOtherThan(wbsCode.getCode(), projectId)) {
                throw new ValidationException("WBS code "+wbsCode.getCode()+" already used in a different project");
            }
        }
    }

    public AnnualSpendSummary getAnnualSpendForSpecificYear(Integer blockId, Integer year) {
        return financeService.getAnnualSpendForSpecificYear(blockId, year);
    }

    @Override
    public void enrichProject(Project project, boolean enrichmentForComparison) {
        if (enrichmentForComparison) {
            Optional<NamedProjectBlock> first = project.getProjectBlocksSorted().stream().filter(pb -> pb.getBlockType().equals(ProjectBlockType.ProjectBudgets)).findFirst();
            if (first.isPresent()) {

                ProjectBudgetsBlock block = (ProjectBudgetsBlock) first.get();

                if (block.getPopulatedYears() != null && !block.getPopulatedYears().isEmpty()) {
                    // prevent duplicated enrichment
                    return;
                }

                ProjectBudgetsBlock pbBlock = this.getProjectBudgetsBlockIncludingCalculatedData(project, block);
                Set<Integer> populatedYears = pbBlock.getPopulatedYears();

                if (populatedYears == null || populatedYears.isEmpty()) {
                    return;
                }


                List<AnnualSpendSummary> annualSpendForSpecificYears =
                        financeService.getAnnualSpendForSpecificYears(pbBlock.getId(), populatedYears);
                for (AnnualSpendSummary annualSpendForSpecificYear : annualSpendForSpecificYears) {
                    // can reenable this if required later
                    annualSpendForSpecificYear.setAnnualSpendMonthlyTotals(null);
                }

                pbBlock.setAnnualSpendSummaries(annualSpendForSpecificYears);
            }

        }
    }
}
