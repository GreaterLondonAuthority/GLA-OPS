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

public class FundingTotals {

    private BigDecimal totalCapitalValue;
    private BigDecimal totalCapitalMatchFund;
    private BigDecimal totalRevenueValue;
    private BigDecimal totalRevenueMatchFund;

    FundingTotals(BigDecimal totalCapitalValue, BigDecimal totalCapitalMatchFund, BigDecimal totalRevenueValue, BigDecimal totalRevenueMatchFund) {
        this.totalCapitalValue = totalCapitalValue;
        this.totalCapitalMatchFund = totalCapitalMatchFund;
        this.totalRevenueValue = totalRevenueValue;
        this.totalRevenueMatchFund = totalRevenueMatchFund;
    }

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

    public BigDecimal getTotalProjectBudget() {
        return addBigDecimals(totalCapitalValue, totalCapitalMatchFund, totalRevenueValue, totalRevenueMatchFund);
    }

}
