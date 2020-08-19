/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import static uk.gov.london.common.GlaUtils.addBigDecimals;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FundingSectionTotalsWrapper {

    private final Map<Integer, BigDecimal> capitalTotals = new HashMap<>();
    private final Map<Integer, BigDecimal> revenueTotals = new HashMap<>();

    public Map<Integer, BigDecimal> getCapitalTotals() {
        return capitalTotals;
    }

    public Map<Integer, BigDecimal> getRevenueTotals() {
        return revenueTotals;
    }

    public BigDecimal getCapitalTotalForSection(Integer sectionNumber) {
        return capitalTotals.get(sectionNumber);
    }

    public BigDecimal getRevenueTotalForSection(Integer sectionNumber) {
        return revenueTotals.get(sectionNumber);
    }

    public BigDecimal getCapitalTotal() {
        BigDecimal res = BigDecimal.ZERO;

        for (Map.Entry<Integer, BigDecimal> entry : capitalTotals.entrySet()) {
            res = addBigDecimals(res, entry.getValue());
        }
        return res;
    }

    public BigDecimal getRevenueTotal() {
        BigDecimal res = BigDecimal.ZERO;

        for (Map.Entry<Integer, BigDecimal> entry : revenueTotals.entrySet()) {
            res = addBigDecimals(res, entry.getValue());
        }
        return res;
    }

    protected void addActivity(FundingActivity activity) {
        if (!capitalTotals.containsKey(activity.getQuarter())) {
            capitalTotals.put(activity.getQuarter(), null);
        }

        capitalTotals.put(activity.getQuarter(),
                addBigDecimals(capitalTotals.get(activity.getQuarter()), activity.getTotalCapitalValue()));

        if (!revenueTotals.containsKey(activity.getQuarter())) {
            revenueTotals.put(activity.getQuarter(), null);
        }

        revenueTotals.put(activity.getQuarter(),
                addBigDecimals(revenueTotals.get(activity.getQuarter()), activity.getTotalRevenueValue()));
    }

}
