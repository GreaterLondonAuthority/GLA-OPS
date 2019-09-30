/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.funding;

import java.math.BigDecimal;

import static uk.gov.london.common.GlaUtils.addBigDecimals;

public class FundingTotalsWrapper {

    protected BigDecimal totalCapitalValue = new BigDecimal(0);
    protected BigDecimal totalCapitalMatchFund = new BigDecimal(0);
    protected BigDecimal totalRevenueValue = new BigDecimal(0);
    protected BigDecimal totalRevenueMatchFund = new BigDecimal(0);

    public BigDecimal getTotalCapitalValue() {
        return totalCapitalValue;
    }

    public BigDecimal getTotalCapitalMatchFund() {
        return totalCapitalMatchFund;
    }

    public BigDecimal getTotalRevenueValue() {
        return totalRevenueValue;
    }

    public BigDecimal getTotalRevenueMatchFund() {
        return totalRevenueMatchFund;
    }

    public BigDecimal getTotal() {
        return addBigDecimals(totalCapitalValue, totalCapitalMatchFund, totalRevenueValue, totalRevenueMatchFund);
    }

    protected void addActivity(FundingActivity activity) {
        totalCapitalValue = addBigDecimals(totalCapitalValue, activity.getCapitalMainValue());
        totalCapitalMatchFund = addBigDecimals(totalCapitalMatchFund, activity.getCapitalMatchFundValue());
        totalRevenueValue = addBigDecimals(totalRevenueValue, activity.getRevenueMainValue());
        totalRevenueMatchFund = addBigDecimals(totalRevenueMatchFund, activity.getRevenueMatchFundValue());
    }

}
