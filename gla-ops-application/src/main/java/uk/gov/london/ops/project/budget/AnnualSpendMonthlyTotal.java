/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.budget;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chris on 20/01/2017.
 */
public class AnnualSpendMonthlyTotal {

    private String monthName;
    private int yearMonth;

    private AnnualSpendLineItem monthlyTotal = new AnnualSpendLineItem();

    private final List<AnnualSpendLineItem> spendBreakdown = new ArrayList<>();

    public AnnualSpendMonthlyTotal() {
    }

    public AnnualSpendMonthlyTotal(String monthName, int yearMonth) {
        this.monthName = monthName;
        this.yearMonth = yearMonth;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public AnnualSpendLineItem getMonthlyTotal() {
        return monthlyTotal;
    }

    public void setMonthlyTotal(AnnualSpendLineItem monthlyTotal) {
        this.monthlyTotal = monthlyTotal;
    }

    public List<AnnualSpendLineItem> getSpendBreakdown() {
        return spendBreakdown;
    }

    public int getYearMonth() {
        return yearMonth;
    }

    public List<AnnualSpendLineItem> getSpendBreakdownBySpendCategory(Integer categoryId) {
        return spendBreakdown.stream().filter(item -> categoryId.equals(item.getCategoryId())).collect(Collectors.toList());
    }

}
