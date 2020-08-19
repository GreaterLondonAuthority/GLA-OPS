/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;

import java.math.BigDecimal;
import uk.gov.london.ops.payment.BudgetSummary;
import uk.gov.london.ops.payment.SpendType;

public class FundingTotalBudgetYear {

    private Integer year;
    private BigDecimal capitalValue;
    private BigDecimal capitalMatchFundValue;
    private BigDecimal revenueValue;
    private BigDecimal revenueMatchFundValue;

    public FundingTotalBudgetYear(Integer year) {
        this.year = year;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getCapitalValue() {
        return capitalValue;
    }

    public void setCapitalValue(BigDecimal capitalValue) {
        this.capitalValue = capitalValue;
    }

    public BigDecimal getCapitalMatchFundValue() {
        return capitalMatchFundValue;
    }

    public void setCapitalMatchFundValue(BigDecimal capitalMatchFundValue) {
        this.capitalMatchFundValue = capitalMatchFundValue;
    }

    public BigDecimal getRevenueValue() {
        return revenueValue;
    }

    public void setRevenueValue(BigDecimal revenueValue) {
        this.revenueValue = revenueValue;
    }

    public BigDecimal getRevenueMatchFundValue() {
        return revenueMatchFundValue;
    }

    public void setRevenueMatchFund(BigDecimal revenueMatchFund) {
        this.revenueMatchFundValue = revenueMatchFund;
    }

    public void addSummary(BudgetSummary bs) {
        boolean isMatchFund = MATCH_FUND_CATEGORY.equalsIgnoreCase(bs.getCategory());
        BigDecimal value = bs.getValue();
        if (SpendType.CAPITAL.equals(bs.getSpendType())) {
            if (isMatchFund) {
                capitalMatchFundValue = value;
            } else {
                capitalValue = value;
            }
        } else {
            if (isMatchFund) {
                revenueMatchFundValue = value;
            } else {
                revenueValue = value;
            }
        }

    }

    public BigDecimal getTotal() {
        return nullSafeAdd(capitalValue, capitalMatchFundValue, revenueValue, revenueMatchFundValue);
    }
}
