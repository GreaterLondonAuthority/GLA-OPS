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
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.mapper.model.MapResult;
import uk.gov.london.ops.repository.FinanceCategoryRepository;
import uk.gov.london.ops.util.CSVRowSource;

import java.math.BigDecimal;

@Component
public class PCSPaymentMapper extends PCSTransactionMapper {

    @Autowired
    FinanceCategoryRepository financeCategoryRepository;

    @Override
    public MapResult<ProjectLedgerEntry> mapRow(CSVRowSource csv) {
        try {
            Integer pcsProjectId = csv.getInteger("pcs_project_id");
            Project project = projectService.getByLegacyProjectCode(pcsProjectId);
            if (project == null) {
                return new MapResult<>("project with PCD ID "+pcsProjectId+" not found!", false, csv.getRowIndex(), csv.getCurrentRowSource());
            }

            NamedProjectBlock block = project.getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
            if (blockHasImportedPcsData(block.getId(), LedgerType.PAYMENT)) {
                return new MapResult<>("annual spend block "+block.getId()+" already has PCS data", true, csv.getRowIndex(), csv.getCurrentRowSource());
            }

            String paymentYear = csv.getString("payment_year");
            int year = Integer.parseInt(paymentYear.substring(0, 4).trim());

            String paymentPeriod = csv.getString("payment_period");
            int month = Integer.valueOf(paymentPeriod.substring(0, 2).trim());

            // In the file April is month 1, we use calendar month (4)
            if (month <= 9) {
                month = month + 3;
            } else {
                year = year + 1;
                month = month - 9;
            }

            if (isOnOrAfterCutoff(year, month)) {
                return new MapResult<>(String.format("ignoring transaction %s as %02d/%d after cut off date", csv.getString("cfacs_reference"), month, year),
                        true, csv.getRowIndex(), csv.getCurrentRowSource());
            }

            String opsCategory = csv.getString("ops_description");
            if (financeCategoryRepository.findFirstByText(opsCategory) == null) {
                return new MapResult<>(String.format("Unrecognised SAP spend category code: %s", opsCategory),
                        true, csv.getRowIndex(), csv.getCurrentRowSource());
            }

            ProjectLedgerEntry entry = new ProjectLedgerEntry();
            entry.setManagingOrganisation(project.getManagingOrganisation());
            entry.setLedgerSource(LedgerSource.PCS);
            entry.setProjectId(project.getId());
            entry.setBlockId(block.getId());
            entry.setCreatedBy("PCS data import");
            entry.setCreatedOn(environment.now());
            entry.setCategory(opsCategory);
            entry.setVendorName(csv.getString("claimant_name"));
            entry.setYear(year);
            entry.setMonth(month);
            entry.setLedgerStatus(LedgerStatus.ACTUAL);
            entry.setLedgerType(LedgerType.PAYMENT);
            entry.setPcsProjectNumber(pcsProjectId);
            entry.setReference(csv.getString("cfacs_reference"));
            entry.setSpendType(getSpendType(csv.getString("rab_category")));
            entry.updateValue(BigDecimal.ZERO.subtract(new BigDecimal(csv.getString("paid_amount"))));

            return new MapResult<>(entry);
        }
        catch (Exception e) {
            return new MapResult<>(e.getMessage());
        }
    }

    SpendType getSpendType(String rabCategory) {
        if (rabCategory.equals("Resource")) {
            return SpendType.REVENUE;
        } else {
            return SpendType.CAPITAL;
        }
    }

}
