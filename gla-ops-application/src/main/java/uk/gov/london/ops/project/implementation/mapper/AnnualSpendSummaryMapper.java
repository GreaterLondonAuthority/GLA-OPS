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
import uk.gov.london.ops.payment.LedgerType;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.payment.SpendType;
import uk.gov.london.ops.project.budget.AnnualSpendLineItem;
import uk.gov.london.ops.project.budget.AnnualSpendMonthlyTotal;
import uk.gov.london.ops.project.budget.AnnualSpendSummary;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by chris on 12/01/2017.
 */
@Component
public class AnnualSpendSummaryMapper extends BaseAnnualSummaryMapper {

    public List<AnnualSpendSummary> getAnnualSpendSummary(List<ProjectLedgerEntry> ledgerEntriesByBlockAndYear,
                                                          Integer fromYear, Integer toYear, Set<Integer> populatedYears) {
        List<AnnualSpendSummary> response = new ArrayList<>();

        for (int i = fromYear; i <= toYear; i++) {
            if (populatedYears.contains(i)) {
                response.add(getAnnualSpendSummary(ledgerEntriesByBlockAndYear, i));
            }
        }
        return response;
    }

    public AnnualSpendSummary getAnnualSpendSummary(List<ProjectLedgerEntry> entries, Integer year) {

        int currentYearStart = getCurrentYearStart(year);
        int currentYearEnd   = getCurrentYearEnd(year);
        int currentYearMonth = getCurrentYearMonth();

        AnnualSpendSummary annualSpendSummary = new AnnualSpendSummary();
        initialiseAnnualSpendMonthlyTotals(annualSpendSummary, year);
        annualSpendSummary.setYear(year);
        AnnualSpendSummary.Totals totals = annualSpendSummary.new Totals();
        annualSpendSummary.setTotals(totals);

        if (entries.size() == 0) {
            return annualSpendSummary;
        }

        OffsetDateTime lastModified = null;
        for (ProjectLedgerEntry entry : entries) {
            if (entry.getYearMonth() >= currentYearStart && entry.getYearMonth() <=  currentYearEnd) {
                if (LedgerType.BUDGET.equals(entry.getLedgerType())) {
                    if (lastModified == null || lastModified.isBefore(entry.getModifiedOn())) {
                        lastModified = entry.getModifiedOn();
                    }
                    handleBudgetEntries(annualSpendSummary, entry);
                } else {
                    handleOtherEntries(annualSpendSummary, entry);
                }
            }
        }

        annualSpendSummary.setLastModified(lastModified);

        calculateMonthlyTotals(annualSpendSummary);
        calculateYearlyTotals(annualSpendSummary, currentYearMonth);

        calculateLeftToSpendBoxValues(annualSpendSummary);
        calculateAvailableForecast(annualSpendSummary);

        sortResults(annualSpendSummary.getAnnualSpendMonthlyTotals());
        return annualSpendSummary;

    }

    /**
     * Sort by SAP Cat Code (should be in this order DB) but if identical then sort by expenditure / credit
     */
    private void sortResults(List<AnnualSpendMonthlyTotal> results) {
        for (AnnualSpendMonthlyTotal result : results) {
            if (result.getSpendBreakdown() != null && result.getSpendBreakdown().size() > 0) {
                result.getSpendBreakdown().sort((o1, o2) -> {
                    if (o1.getSpendCategory().equals(o2.getSpendCategory())) {
                        return o1.isExpenditure() ? -1 : 1;
                    } else {
                        return o1.getSpendCategory().compareTo(o2.getSpendCategory());
                    }
                });
            }
        }
    }

    /**
     * actuals do not include current month
     * Capital:  Capital  Budget - all capital actuals .
     * Revenue:  Revenue  Budget - all revenue actuals .
     */
    private void calculateLeftToSpendBoxValues(AnnualSpendSummary summary) {
        AnnualSpendLineItem totalForPastMonths = summary.getTotalForPastMonths();

        if (summary.getAnnualBudgetCapital() != null) {
            summary.getTotals().setLeftToSpendCapitalInclCurrentMonth(summary.getAnnualBudgetCapital()
                    .add(totalForPastMonths.getCapitalActual()));
        } else {
            summary.getTotals().setLeftToSpendCapitalInclCurrentMonth(BigDecimal.ZERO);
        }

        if (summary.getAnnualBudgetRevenue() != null) {
            summary.getTotals().setLeftToSpendRevenueInclCurrentMonth(summary.getAnnualBudgetRevenue()
                    .add(totalForPastMonths.getRevenueActual()));
        } else {
            summary.getTotals().setLeftToSpendRevenueInclCurrentMonth(BigDecimal.ZERO);
        }
    }

    /**
     * actuals do not include current month
     * Capital:  Capital  Budget - all capital actuals - all upcoming forecasts.
     * Revenue:  Revenue  Budget - all revenue actuals - all upcoming forecasts.
     */
    private void calculateAvailableForecast(AnnualSpendSummary summary) {

        AnnualSpendLineItem actualSpend = summary.getActualSpend();
        AnnualSpendLineItem remainingForecast = summary.getRemainingForecast();

        if (summary.getAnnualBudgetCapital() != null) {
            BigDecimal capitalActual =  actualSpend.getCapitalActual() == null ? BigDecimal.ZERO : actualSpend.getCapitalActual();
            BigDecimal capitalForecast = remainingForecast.getCapitalForecast() == null ? BigDecimal.ZERO
                    : remainingForecast.getCapitalForecast();
            BigDecimal actualSpendCapital = summary.getAnnualBudgetCapital().add(capitalActual).add(capitalForecast);
            actualSpendCapital = actualSpendCapital.signum() == -1 ? BigDecimal.ZERO : actualSpendCapital;
            summary.getTotals().setAvailableToForecastCapital(actualSpendCapital);
        } else {
            summary.getTotals().setLeftToSpendCapitalInclCurrentMonth(BigDecimal.ZERO);
        }

        if (summary.getAnnualBudgetRevenue() != null) {
            BigDecimal revenueActual = actualSpend.getRevenueActual() == null ? BigDecimal.ZERO : actualSpend.getRevenueActual();
            BigDecimal revenueForecast = remainingForecast.getRevenueForecast() == null ? BigDecimal.ZERO
                    : remainingForecast.getRevenueForecast();
            BigDecimal actualSpendRevenue = summary.getAnnualBudgetRevenue().add(revenueActual).add(revenueForecast);
            actualSpendRevenue = actualSpendRevenue.signum() == -1 ? BigDecimal.ZERO : actualSpendRevenue;
            summary.getTotals().setAvailableToForecastRevenue(actualSpendRevenue);
        } else {
            summary.getTotals().setLeftToSpendRevenueInclCurrentMonth(BigDecimal.ZERO);
        }
    }

    private void calculateYearlyTotals(AnnualSpendSummary summary, int currentYearMonth) {
        AnnualSpendLineItem totalForPastMonths = summary.getTotalForPastMonths();
        AnnualSpendLineItem currentAndFutureMonths = summary.getTotalForCurrentAndFutureMonths();

        AnnualSpendLineItem actualSpend = summary.getActualSpend();
        AnnualSpendLineItem remainingForecast = summary.getRemainingForecast();


        for (AnnualSpendMonthlyTotal monthlyTotal : summary.getAnnualSpendMonthlyTotals()) {
            AnnualSpendLineItem total = monthlyTotal.getMonthlyTotal();
            if (monthlyTotal.getYearMonth() < currentYearMonth) {
                // add actuals in the past
                actualSpend.addCapitalActual(total.getCapitalActual());
                actualSpend.addRevenueActual(total.getRevenueActual());
                actualSpend.setCapitalForecast(null);
                actualSpend.setRevenueForecast(null);

                // past month data only for data in the past
                totalForPastMonths.addCapitalActual(total.getCapitalActual());
                totalForPastMonths.addCapitalForecast(total.getCapitalForecast());
                totalForPastMonths.addRevenueActual(total.getRevenueActual());
                totalForPastMonths.addRevenueForecast(total.getRevenueForecast());
            } else {

                // forecast includes current month
                remainingForecast.addCapitalForecast(total.getCapitalForecast());
                remainingForecast.addRevenueForecast(total.getRevenueForecast());
                // cannot have future actuals so set to null (is preferred over zero)
                remainingForecast.setCapitalActual(null);
                remainingForecast.setRevenueActual(null);

                // TODO REmove this
                // forecast includes current month
                currentAndFutureMonths.addCapitalForecast(total.getCapitalForecast());
                currentAndFutureMonths.addRevenueForecast(total.getRevenueForecast());
                // cannot have future actuals so set to null (is preferred over zero)
                currentAndFutureMonths.setCapitalActual(null);
                currentAndFutureMonths.setRevenueActual(null);
            }
        }
    }


    private void calculateMonthlyTotals(AnnualSpendSummary summary) {

        for (AnnualSpendMonthlyTotal annualSpendMonthlyTotal : summary.getAnnualSpendMonthlyTotals()) {
            AnnualSpendLineItem monthlyTotal = annualSpendMonthlyTotal.getMonthlyTotal();

            for (AnnualSpendLineItem lineItem : annualSpendMonthlyTotal.getSpendBreakdown()) {
                monthlyTotal.addCapitalActual(lineItem.getCapitalActual());
                monthlyTotal.addCapitalForecast(lineItem.getCapitalForecast());
                monthlyTotal.addRevenueActual(lineItem.getRevenueActual());
                monthlyTotal.addRevenueForecast(lineItem.getRevenueForecast());

            }

        }
    }

    private void handleOtherEntries(AnnualSpendSummary annualSpendSummary, ProjectLedgerEntry entry) {
        BigDecimal value = entry.getValue();

        AnnualSpendMonthlyTotal annualSpendMonthlyTotal = annualSpendSummary.getAnnualSpendMonthlyTotals()
                .get(getArrayPositionByMonth(entry.getMonth()));

        List<AnnualSpendLineItem> relevantLineItems = annualSpendMonthlyTotal
                .getSpendBreakdownBySpendCategory(entry.getCategoryId());
        AnnualSpendLineItem relevantLineItem = null;

        for (AnnualSpendLineItem lineItem : relevantLineItems) {
            if (lineItem.isExpenditure() && BigDecimal.ZERO.compareTo(entry.getValue()) >= 0) {
                relevantLineItem = lineItem;
            } else if (!lineItem.isExpenditure() && BigDecimal.ZERO.compareTo(entry.getValue()) < 0) {
                relevantLineItem = lineItem;
            }
        }

        if (relevantLineItem == null) {
            String category = refDataService.getFinanceCategory(entry.getCategoryId()).getText();
            relevantLineItem = new AnnualSpendLineItem(entry.getCategoryId(), category);
            annualSpendMonthlyTotal.getSpendBreakdown().add(relevantLineItem);
            annualSpendMonthlyTotal.getSpendBreakdown().sort((o1, o2) -> o1.getSpendCategory().compareTo(o2.getSpendCategory()));
        }

        if (LedgerStatus.FORECAST.equals(entry.getLedgerStatus())) {

            if (SpendType.REVENUE.equals(entry.getSpendType())) {
                relevantLineItem.addRevenueForecast(value);
            } else {
                relevantLineItem.addCapitalForecast(value);
            }
        } else if (LedgerStatus.ACTUAL.equals(entry.getLedgerStatus())) {

            if (SpendType.REVENUE.equals(entry.getSpendType())) {
                relevantLineItem.addRevenueActual(value);
            } else {
                relevantLineItem.addCapitalActual(value);
            }
        }
    }

    private void handleBudgetEntries(AnnualSpendSummary annualSpendSummary, ProjectLedgerEntry entry) {
        BigDecimal value = entry.getValue();

        if (SpendType.REVENUE.equals(entry.getSpendType())) {
            annualSpendSummary.setAnnualBudgetRevenue(value);
        } else if (SpendType.CAPITAL.equals(entry.getSpendType())) {
            annualSpendSummary.setAnnualBudgetCapital(value);

        }
    }


    private void initialiseAnnualSpendMonthlyTotals(AnnualSpendSummary summary, int year) {
        int yearMonth = (year * 100) + 4;
        List<AnnualSpendMonthlyTotal> annualSpendMonthlyTotals = summary.getAnnualSpendMonthlyTotals();
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("APR", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("MAY", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("JUN", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("JUL", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("AUG", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("SEP", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("OCT", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("NOV", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("DEC", yearMonth++));
        yearMonth =  ((year + 1) * 100) + 1;
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("JAN", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("FEB", yearMonth++));
        annualSpendMonthlyTotals.add(new AnnualSpendMonthlyTotal("MAR", yearMonth));
    }


}
