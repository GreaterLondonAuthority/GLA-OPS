/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.project.ComparableItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 12/01/2017.
 */
public class AnnualSpendSummary implements ComparableItem {

    private BigDecimal annualBudgetRevenue;

    private BigDecimal annualBudgetCapital;

    private OffsetDateTime lastModified;

    private Integer year;

    private Totals totals;

    private List<AnnualSpendMonthlyTotal> annualSpendMonthlyTotals = new ArrayList<>();

    @JsonIgnore
    private AnnualSpendLineItem totalForPastMonths = new AnnualSpendLineItem(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);

    private AnnualSpendLineItem totalForCurrentAndFutureMonths = new AnnualSpendLineItem(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);

    private AnnualSpendLineItem actualSpend = new AnnualSpendLineItem(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);

    @JsonIgnore
    private AnnualSpendLineItem remainingForecast = new AnnualSpendLineItem(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);



    public AnnualSpendSummary() {
    }

    public AnnualSpendSummary(BigDecimal annualBudgetRevenue, BigDecimal annualBudgetCapital, Integer year) {
        this.annualBudgetRevenue = annualBudgetRevenue;
        this.annualBudgetCapital = annualBudgetCapital;
        this.year = year;
    }



    public BigDecimal getAnnualBudgetRevenue() {
        return annualBudgetRevenue;
    }

    public void setAnnualBudgetRevenue(BigDecimal annualBudgetRevenue) {
        this.annualBudgetRevenue = annualBudgetRevenue;
    }

    public BigDecimal getAnnualBudgetCapital() {
        return annualBudgetCapital;
    }

    public void setAnnualBudgetCapital(BigDecimal annualBudgetCapital) {
        this.annualBudgetCapital = annualBudgetCapital;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Totals getTotals() {
        return totals;
    }

    public void setTotals(Totals totals) {
        this.totals = totals;
    }

    public List<AnnualSpendMonthlyTotal> getAnnualSpendMonthlyTotals() {
        return annualSpendMonthlyTotals;
    }

    public void setAnnualSpendMonthlyTotals(List<AnnualSpendMonthlyTotal> annualSpendMonthlyTotals) {
        this.annualSpendMonthlyTotals = annualSpendMonthlyTotals;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(year);
    }

    public class Totals {
        private BigDecimal leftToSpendCapitalInclCurrentMonth = new BigDecimal("0.0");
        private BigDecimal leftToSpendRevenueInclCurrentMonth = new BigDecimal("0.0");
        private BigDecimal availableToForecastCapital = new BigDecimal("0.0");
        private BigDecimal availableToForecastRevenue = new BigDecimal("0.0");

        public Totals() {
        }

        public BigDecimal getLeftToSpendCapitalInclCurrentMonth() {
            return leftToSpendCapitalInclCurrentMonth;
        }

        public void setLeftToSpendCapitalInclCurrentMonth(BigDecimal leftToSpendCapitalInclCurrentMonth) {
            this.leftToSpendCapitalInclCurrentMonth = leftToSpendCapitalInclCurrentMonth;
        }

        public BigDecimal getLeftToSpendRevenueInclCurrentMonth() {
            return leftToSpendRevenueInclCurrentMonth;
        }

        public void setLeftToSpendRevenueInclCurrentMonth(BigDecimal leftToSpendRevenueInclCurrentMonth) {
            this.leftToSpendRevenueInclCurrentMonth = leftToSpendRevenueInclCurrentMonth;
        }

        public BigDecimal getAvailableToForecastCapital() {
            return availableToForecastCapital;
        }

        public void setAvailableToForecastCapital(BigDecimal availableToForecastCapital) {
            this.availableToForecastCapital = availableToForecastCapital;
        }

        public BigDecimal getAvailableToForecastRevenue() {
            return availableToForecastRevenue;
        }

        public void setAvailableToForecastRevenue(BigDecimal availableToForecastRevenue) {
            this.availableToForecastRevenue = availableToForecastRevenue;
        }
    }

    public AnnualSpendLineItem getTotalForCurrentAndFutureMonths() {
        return totalForCurrentAndFutureMonths;
    }

    public AnnualSpendLineItem getTotalForPastMonths() {
        return totalForPastMonths;
    }

    public AnnualSpendLineItem getActualSpend() {
        return actualSpend;
    }

    public AnnualSpendLineItem getRemainingForecast() {
        return remainingForecast;
    }
}
