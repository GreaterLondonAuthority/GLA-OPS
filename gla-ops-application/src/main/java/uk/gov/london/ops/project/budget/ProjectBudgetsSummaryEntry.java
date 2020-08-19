/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.budget;

import java.math.BigDecimal;
import uk.gov.london.ops.payment.SpendType;
import uk.gov.london.ops.framework.ComparableItem;

/**
 * Created by chris on 02/02/2017.
 */
public class ProjectBudgetsSummaryEntry implements ComparableItem {

    private Integer financialYear;

    private SpendType spendType;

    private BigDecimal forecastValue;

    private BigDecimal actualValue;


    public ProjectBudgetsSummaryEntry() {
    }

    public ProjectBudgetsSummaryEntry(Integer financialYear, SpendType spendType, BigDecimal forecastValue,
            BigDecimal actualValue) {
        this.financialYear = financialYear;
        this.spendType = spendType;
        this.forecastValue = forecastValue;
        this.actualValue = actualValue;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }

    public SpendType getSpendType() {
        return spendType;
    }

    public void setSpendType(SpendType spendType) {
        this.spendType = spendType;
    }

    public BigDecimal getForecastValue() {
        return forecastValue;
    }

    public void setForecastValue(BigDecimal forecastValue) {
        this.forecastValue = forecastValue;
    }

    public BigDecimal getActualValue() {
        return actualValue;
    }

    public void setActualValue(BigDecimal actualValue) {
        this.actualValue = actualValue;
    }

    public BigDecimal getRemainingForecastAndActuals() {
        if (forecastValue == null || actualValue == null) {
            return forecastValue == null ? actualValue : forecastValue;
        }
        return forecastValue.add(actualValue);
    }

    @Override
    public String getComparisonId() {
        return financialYear + ":" + spendType;
    }
}
