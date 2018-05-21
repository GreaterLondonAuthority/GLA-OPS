/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.domain.finance.LedgerSource;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectLedgerEntry;
import uk.gov.london.ops.domain.project.ReceiptsBlock;
import uk.gov.london.ops.domain.refdata.FinanceCategory;
import uk.gov.london.ops.mapper.model.MapResult;
import uk.gov.london.ops.repository.FinanceCategoryRepository;
import uk.gov.london.ops.util.CSVRowSource;

import java.time.LocalDate;

@Component
public class PCSReceiptMapper extends PCSTransactionMapper {

    @Autowired
    FinanceCategoryRepository financeCategoryRepository;

    @Override
    public MapResult<ProjectLedgerEntry> mapRow(CSVRowSource csv) {
        try {
            Integer pcsProjectId = csv.getInteger("PCS_Number");
            Project project = projectService.getByLegacyProjectCode(pcsProjectId);
            if (project == null) {
                return new MapResult<>("project with PCD ID "+pcsProjectId+" not found!", false, csv.getRowIndex(), csv.getCurrentRowSource());
            }

            FinanceCategory category = financeCategoryRepository.findFirstByText(csv.getString("OPS Receipt Category"));
            if (category == null) {
                return new MapResult<>("category "+csv.getString("OPS Receipt Category")+" not found!", true, csv.getRowIndex(), csv.getCurrentRowSource());
            }

            ReceiptsBlock block = project.getReceiptsBlock();
            if (blockHasImportedPcsData(block.getId(), LedgerType.RECEIPT)) {
                return new MapResult<>("receipts block "+block.getId()+" already has PCS data", true, csv.getRowIndex(), csv.getCurrentRowSource());
            }

            LocalDate receiptDate = csv.getDate("Receipt_Date","dd/MM/yyyy");
            Integer year = receiptDate.getYear();
            Integer month = receiptDate.getMonthValue();
            if (isOnOrAfterCutoff(year, month)) {
                return new MapResult<>(String.format("ignoring transaction %d as %02d/%d after cut off date", csv.getRowIndex(), month, year),
                        true, csv.getRowIndex(), csv.getCurrentRowSource());
            }

            ProjectLedgerEntry ple = new ProjectLedgerEntry();
            ple.setManagingOrganisation(project.getManagingOrganisation());
            ple.setProjectId(project.getId());
            ple.setBlockId(block.getId());
            ple.setLedgerType(LedgerType.RECEIPT);
            ple.setLedgerStatus(LedgerStatus.ACTUAL);
            ple.setLedgerSource(LedgerSource.PCS);
            ple.setCategoryId(category.getId());
            ple.setCategory(category.getText());
            ple.updateValue(csv.getCurrencyValue("Receipt_amount").negate());

            ple.setTransactionDate(csv.getString("Receipt_Date"));
            ple.setYear(year);
            ple.setMonth(month);

            return new MapResult<>(ple);
        }
        catch (Exception e) {
            return new MapResult<>(e.getMessage());
        }
    }

}
