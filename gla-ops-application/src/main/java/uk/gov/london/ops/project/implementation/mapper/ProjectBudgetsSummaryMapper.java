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
import uk.gov.london.ops.payment.SpendType;
import uk.gov.london.ops.project.budget.AnnualSpendSummaryRecord;
import uk.gov.london.ops.project.budget.ProjectBudgetsBlock;
import uk.gov.london.ops.project.budget.ProjectBudgetsSummaryEntry;
import uk.gov.london.ops.project.budget.ProjectBudgetsYearlySummary;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 03/02/2017.
 */
@Component
public class ProjectBudgetsSummaryMapper {

    public void mapTotalsTiles(ProjectBudgetsBlock projectBudgetsBlock, List<ProjectLedgerEntry> entries) {

        ProjectBudgetsBlock.Totals totals = projectBudgetsBlock.getTotals();

        BigDecimal projectBudgetsBudgetCapital = projectBudgetsBlock.getCapital() == null ? BigDecimal.ZERO : new BigDecimal(projectBudgetsBlock.getCapital());
        BigDecimal projectBudgetsBudgetRevenue = projectBudgetsBlock.getRevenue() == null ? BigDecimal.ZERO : new BigDecimal(projectBudgetsBlock.getRevenue());

        if (projectBudgetsBudgetCapital.compareTo(BigDecimal.ZERO) == 0 && projectBudgetsBudgetRevenue.compareTo(BigDecimal.ZERO) ==0 ) {
            return;
        }

        BigDecimal actualCap    = BigDecimal.ZERO;
        BigDecimal forecastCap  = BigDecimal.ZERO;
        BigDecimal actualRev    = BigDecimal.ZERO;
        BigDecimal forecastRev  = BigDecimal.ZERO;
        for (ProjectLedgerEntry entry : entries) {
            if (SpendType.CAPITAL.equals(entry.getSpendType())) {
                if (LedgerStatus.ACTUAL.equals(entry.getLedgerStatus())) {
                    actualCap = actualCap.add(entry.getValue());
                } else if (LedgerStatus.FORECAST.equals(entry.getLedgerStatus())) {
                    forecastCap = forecastCap.add(entry.getValue());
                }
            } else {
                if (LedgerStatus.ACTUAL.equals(entry.getLedgerStatus())) {
                    actualRev = actualRev.add(entry.getValue());
                } else if (LedgerStatus.FORECAST.equals(entry.getLedgerStatus())) {
                    forecastRev = forecastRev.add(entry.getValue());
                }
            }
        }

        // invert values so -ve become +ve
        actualCap    = actualCap.negate();
        forecastCap  = forecastCap.negate();
        actualRev    = actualRev.negate();
        forecastRev  = forecastRev.negate();

        //new fields
        totals.setAvailableToForecastCapital(BigDecimal.ZERO.max(projectBudgetsBudgetCapital.subtract(forecastCap).subtract(actualCap)));
        totals.setAvailableToForecastRevenue(BigDecimal.ZERO.max(projectBudgetsBudgetRevenue.subtract(forecastRev).subtract(actualRev)));

        // left to spend on project is budget minus spend to date excl current month
        totals.setLeftToSpendOnProjectCapital(projectBudgetsBudgetCapital.subtract(actualCap));
        totals.setLeftToSpendOnProjectRevenue(projectBudgetsBudgetRevenue.subtract(actualRev));

        // approved project forecast is: the lesser of (Budget - Actuals) or forecast
        totals.setApprovedProjectForecastCapital(BigDecimal.ZERO.max(projectBudgetsBudgetCapital.subtract(actualCap).min(forecastCap)));
        totals.setApprovedProjectForecastRevenue(BigDecimal.ZERO.max(projectBudgetsBudgetRevenue.subtract(actualRev).min(forecastRev)));

        // unapproved project forecast is: the larger of 0 and total forecast - budget
        BigDecimal totalForecastCapital = actualCap.add(forecastCap);
        totals.setUnapprovedProjectForecastCapital(BigDecimal.ZERO.max(totalForecastCapital.subtract(projectBudgetsBudgetCapital)));

        BigDecimal totalForecastRevenue = actualRev.add(forecastRev);
        totals.setUnapprovedProjectForecastRevenue(BigDecimal.ZERO.max(totalForecastRevenue.subtract(projectBudgetsBudgetRevenue)));
    }

    public void mapProjectBudgets(ProjectBudgetsBlock projectBudgetsBlock, List<AnnualSpendSummaryRecord> records,Integer currentYearMonth, Integer from, Integer to) {

        ArrayList<ProjectBudgetsSummaryEntry> summaryEntries = new ArrayList<>();
        ProjectBudgetsYearlySummary projectBudgetsYearlySummary = new ProjectBudgetsYearlySummary();
        projectBudgetsBlock.setProjectBudgetsYearlySummary(projectBudgetsYearlySummary);
        projectBudgetsYearlySummary.setSummaryEntries(summaryEntries);



        for (int i = from ;  i <= to ; i++ ) {

            BigDecimal capActual = null;
            BigDecimal revActual = null;
            BigDecimal capForecast = null;
            BigDecimal revForecast = null;



            for (AnnualSpendSummaryRecord record : records) {
                if (record.getFinancialYear() == i) {

                    Integer recordYearMonth = (record.getYear() * 100) + record.getMonth();

                    if (recordYearMonth < currentYearMonth) {
                        if (SpendType.CAPITAL.equals(record.getSpendType())) {
                            if (LedgerStatus.ACTUAL.equals(record.getLedgerStatus())) {
                                capActual = capActual == null ? record.getSpend() : capActual.add(record.getSpend());
                            }
                        } else {
                            if (LedgerStatus.ACTUAL.equals(record.getLedgerStatus())) {
                                revActual = revActual == null ? record.getSpend() : revActual.add(record.getSpend());
                            }
                        }
                    } else {
                        if (SpendType.CAPITAL.equals(record.getSpendType())) {
                            if (LedgerStatus.FORECAST.equals(record.getLedgerStatus())) {
                                capForecast = capForecast == null ? record.getSpend() : capForecast.add(record.getSpend());
                            }
                        } else {
                            if (LedgerStatus.FORECAST.equals(record.getLedgerStatus())) {
                                revForecast = revForecast == null ? record.getSpend() : revForecast.add(record.getSpend());
                            }
                        }
                    }
                }
            }

            ProjectBudgetsSummaryEntry capEntry = null;
            ProjectBudgetsSummaryEntry revEntry = null;

            if (capActual != null || capForecast != null) {
                capEntry = new ProjectBudgetsSummaryEntry();
                capEntry.setFinancialYear(i);
                capEntry.setActualValue(capActual == null ? BigDecimal.ZERO : capActual);
                capEntry.setForecastValue(capForecast == null ? BigDecimal.ZERO : capForecast);
                capEntry.setSpendType(SpendType.CAPITAL);
            }

            if (revActual != null || revForecast != null) {
                revEntry = new ProjectBudgetsSummaryEntry();
                revEntry.setFinancialYear(i);
                revEntry.setActualValue(revActual == null ? BigDecimal.ZERO : revActual);
                revEntry.setForecastValue(revForecast == null ? BigDecimal.ZERO : revForecast);
                revEntry.setSpendType(SpendType.REVENUE);
            }

            if (capEntry != null || revEntry != null) {
                if (capEntry == null) {
                    capEntry = new ProjectBudgetsSummaryEntry(i, SpendType.CAPITAL, BigDecimal.ZERO, BigDecimal.ZERO);
                } else if (revEntry == null) {
                    revEntry = new ProjectBudgetsSummaryEntry(i, SpendType.REVENUE, BigDecimal.ZERO, BigDecimal.ZERO);
                }
                summaryEntries.add(capEntry);
                summaryEntries.add(revEntry);
            }
        }
    }



}
