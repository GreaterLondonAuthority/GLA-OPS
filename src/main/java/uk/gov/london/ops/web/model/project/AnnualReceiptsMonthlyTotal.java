/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnnualReceiptsMonthlyTotal {

    private String monthName;
    private int yearMonth;

    private AnnualReceiptsLineItem monthlyTotal = new AnnualReceiptsLineItem();

    private List<AnnualReceiptsLineItem> breakdown = new ArrayList<>();

    public AnnualReceiptsMonthlyTotal() {
    }

    public AnnualReceiptsMonthlyTotal(String monthName, int yearMonth) {
        this.monthName = monthName;
        this.yearMonth = yearMonth;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public AnnualReceiptsLineItem getMonthlyTotal() {
        return monthlyTotal;
    }

    public void setMonthlyTotal(AnnualReceiptsLineItem monthlyTotal) {
        this.monthlyTotal = monthlyTotal;
    }

    public List<AnnualReceiptsLineItem> getBreakdown() {
        return breakdown;
    }

    public int getYearMonth() {
        return yearMonth;
    }

    public AnnualReceiptsLineItem getLineItemByCategory(String category) {
        Optional<AnnualReceiptsLineItem> optional = breakdown.stream().filter(item -> category.equals(item.getCategory())).findFirst();
        return optional.isPresent() ? optional.get() : null;
    }
}
