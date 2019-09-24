/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.payment.FinanceService;
import uk.gov.london.ops.payment.LedgerSource;
import uk.gov.london.ops.payment.LedgerType;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.framework.MapResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

abstract class PCSTransactionMapper implements CSVFile.CSVMapper<MapResult<ProjectLedgerEntry>> {

    @Autowired
    ProjectService projectService;

    @Autowired
    FinanceService financeService;

    @Autowired
    RefDataService refDataService;

    @Autowired
    Environment environment;

    @Value("#{new java.text.SimpleDateFormat('dd/MM/yyyy').parse('${actuals.cutoff.date}')}")
    Date actualsCutoffDate;

    boolean isOnOrAfterCutoff(Integer year, Integer month) throws ParseException {
        Date date = new SimpleDateFormat("dd/MM/yyyy").parse(String.format("01/%02d/%d", month, year));
        return date.equals(actualsCutoffDate) || date.after(actualsCutoffDate);
    }

    boolean blockHasImportedPcsData(Integer blockId, LedgerType type) {
        List<ProjectLedgerEntry> entries = financeService.findAllByBlockIdAndLedgerType(blockId, type);
        for (ProjectLedgerEntry entry: entries) {
            if (LedgerSource.PCS.equals(entry.getLedgerSource())) {
                return true;
            }
        }
        return false;
    }

}
