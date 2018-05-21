/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model.project;

import uk.gov.london.ops.domain.project.ComparableItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AnnualReceiptsSummary implements Comparable, ComparableItem {

    private Integer financialYear;
    private List<AnnualReceiptsMonthlyTotal> annualReceiptMonthlyTotals = new ArrayList<>();
    private AnnualReceiptsLineItem totalForPastMonths = new AnnualReceiptsLineItem(null, BigDecimal.ZERO, BigDecimal.ZERO);
    private AnnualReceiptsLineItem totalForCurrentAndFutureMonths = new AnnualReceiptsLineItem(null, BigDecimal.ZERO, BigDecimal.ZERO);

    public List<AnnualReceiptsMonthlyTotal> getAnnualReceiptMonthlyTotals() {
        return annualReceiptMonthlyTotals;
    }

    public void setAnnualReceiptMonthlyTotals(List<AnnualReceiptsMonthlyTotal> annualReceiptMonthlyTotals) {
        this.annualReceiptMonthlyTotals = annualReceiptMonthlyTotals;
    }

    public AnnualReceiptsLineItem getTotalForPastMonths() {
        return totalForPastMonths;
    }

    public void setTotalForPastMonths(AnnualReceiptsLineItem totalForPastMonths) {
        this.totalForPastMonths = totalForPastMonths;
    }

    public AnnualReceiptsLineItem getTotalForCurrentAndFutureMonths() {
        return totalForCurrentAndFutureMonths;
    }

    public void setTotalForCurrentAndFutureMonths(AnnualReceiptsLineItem totalForCurrentAndFutureMonths) {
        this.totalForCurrentAndFutureMonths = totalForCurrentAndFutureMonths;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }



    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }
        if (o instanceof AnnualReceiptsSummary) {
            AnnualReceiptsSummary other = (AnnualReceiptsSummary) o;
            if (this.getFinancialYear() != null) {
                return this.getFinancialYear().compareTo(other.getFinancialYear());
            }
        }
        return 0;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(this.getFinancialYear());
    }
}
