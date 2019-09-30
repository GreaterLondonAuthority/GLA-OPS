/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.funding;

import uk.gov.london.ops.payment.BudgetSummary;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class FundingTotalBudget {
    // TODO Should the "totals" be moved here too ?
    private Map<Integer, FundingTotalBudgetYear> years = new HashMap<>();



    public FundingTotalBudget(List<BudgetSummary> budgetSummaries) {
        for(BudgetSummary bs: budgetSummaries){
            this.addSummary(bs);
        }
    }


    protected void addSummary(BudgetSummary bs) {
        Integer year = bs.getYear();
        if (!years.containsKey(year)) {
            years.put(year, new FundingTotalBudgetYear(year));
        }
        years.get(year).addSummary(bs);
    }

    public List<FundingTotalBudgetYear> getYears(){
        return years.values().stream().sorted(Comparator.comparing(FundingTotalBudgetYear::getYear)).collect(Collectors.toList());
    }

    public FundingTotalBudgetYear getYear(Integer year) {
        return years.get(year);
    }

}