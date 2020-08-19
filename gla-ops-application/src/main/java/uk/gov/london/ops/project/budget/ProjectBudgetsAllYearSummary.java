/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.budget;

import java.math.BigDecimal;
import uk.gov.london.ops.framework.ComparableItem;

/**
 * Created by chris on 02/02/2017.
 */
public class ProjectBudgetsAllYearSummary implements ComparableItem {

    private BigDecimal forecastValueTotal;

    private BigDecimal actualValueTotal;

    private BigDecimal remainingForecastAndActualsTotal;

    public ProjectBudgetsAllYearSummary() {
    }

    public ProjectBudgetsAllYearSummary(BigDecimal forecastValueTotal, BigDecimal actualValueTotal,
            BigDecimal remainingForecastAndActualsTotal) {
        this.forecastValueTotal = forecastValueTotal;
        this.actualValueTotal = actualValueTotal;
        this.remainingForecastAndActualsTotal = remainingForecastAndActualsTotal;
    }

    public BigDecimal getForecastValueTotal() {
        return forecastValueTotal;
    }

    public void setForecastValueTotal(BigDecimal forecastValueTotal) {
        this.forecastValueTotal = forecastValueTotal;
    }

    public BigDecimal getActualValueTotal() {
        return actualValueTotal;
    }

    public void setActualValueTotal(BigDecimal actualValueTotal) {
        this.actualValueTotal = actualValueTotal;
    }

    public BigDecimal getRemainingForecastAndActualsTotal() {
        return remainingForecastAndActualsTotal;
    }

    @Override
    public String getComparisonId() {
        return "allYearSummary";
    }
}
