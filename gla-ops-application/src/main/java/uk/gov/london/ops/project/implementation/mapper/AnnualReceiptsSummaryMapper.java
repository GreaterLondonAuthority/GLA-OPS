/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.mapper;

import org.springframework.stereotype.Component;
import uk.gov.london.ops.payment.LedgerStatus;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.project.receipt.AnnualReceiptsLineItem;
import uk.gov.london.ops.project.receipt.AnnualReceiptsMonthlyTotal;
import uk.gov.london.ops.project.receipt.AnnualReceiptsSummary;

import java.util.List;

@Component
public class AnnualReceiptsSummaryMapper extends BaseAnnualSummaryMapper {

    public AnnualReceiptsSummary toAnnualReceiptSummary(List<ProjectLedgerEntry> entries, Integer year) {

        AnnualReceiptsSummary summary = new AnnualReceiptsSummary();
        summary.setFinancialYear(year);

        initialiseAnnualReceiptMonthlyTotals(summary, year);

        for (ProjectLedgerEntry entry: entries) {
            AnnualReceiptsMonthlyTotal monthlyTotal = summary.getAnnualReceiptMonthlyTotals().get(getArrayPositionByMonth(entry.getMonth()));
            String category = refDataService.getFinanceCategory(entry.getCategoryId()).getText();
            AnnualReceiptsLineItem relevantLineItem = monthlyTotal.getLineItemByCategory(category);
            if (relevantLineItem == null) {
                relevantLineItem = new AnnualReceiptsLineItem(entry.getCategoryId(), category);
                monthlyTotal.getBreakdown().add(relevantLineItem);
            }

            if (LedgerStatus.FORECAST.equals(entry.getLedgerStatus())) {
                relevantLineItem.setForecastId(entry.getId());
                relevantLineItem.addForecast(entry.getValue());
            }
            else {
                relevantLineItem.addActual(entry.getValue());
            }
        }

        calculateMonthlyTotals(summary);
        return summary;
    }

    private void calculateMonthlyTotals(AnnualReceiptsSummary summary) {
        for (AnnualReceiptsMonthlyTotal annualReceiptsMonthlyTotal: summary.getAnnualReceiptMonthlyTotals()) {
            AnnualReceiptsLineItem monthlyTotal = annualReceiptsMonthlyTotal.getMonthlyTotal();
            for (AnnualReceiptsLineItem lineItem : annualReceiptsMonthlyTotal.getBreakdown()) {
                monthlyTotal.addActual(lineItem.getActual());
                monthlyTotal.addForecast(lineItem.getForecast());
            }
        }
    }



    private void initialiseAnnualReceiptMonthlyTotals(AnnualReceiptsSummary summary, int year) {
        int yearMonth = (year * 100) + 4;
        List<AnnualReceiptsMonthlyTotal> annualReceiptsMonthlyTotals = summary.getAnnualReceiptMonthlyTotals();
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("APR", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("MAY", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("JUN", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("JUL", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("AUG", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("SEP", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("OCT", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("NOV", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("DEC", yearMonth++));
        yearMonth =  ((year+1) * 100) + 1;
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("JAN", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("FEB", yearMonth++));
        annualReceiptsMonthlyTotals.add(new AnnualReceiptsMonthlyTotal("MAR", yearMonth));
    }

}
