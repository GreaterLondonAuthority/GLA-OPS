/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.budget;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by chris on 02/02/2017.
 */
public class ProjectBudgetsYearlySummary {

    private List<ProjectBudgetsSummaryEntry> summaryEntries;

    public ProjectBudgetsYearlySummary() {
    }

    public ProjectBudgetsYearlySummary(List<ProjectBudgetsSummaryEntry> summaryEntries) {
        this.summaryEntries = summaryEntries;
    }

    private ProjectBudgetsAllYearSummary calculateAllYearSummary() {
        BigDecimal forecasts = BigDecimal.ZERO;
        BigDecimal actuals = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (ProjectBudgetsSummaryEntry summaryEntry : summaryEntries) {
            if (summaryEntry.getForecastValue() != null) {
                forecasts = forecasts.add(summaryEntry.getForecastValue());
            }
            BigDecimal remainingForecastAndActuals = summaryEntry.getRemainingForecastAndActuals();
            if (remainingForecastAndActuals != null) {
                total = total.add(remainingForecastAndActuals);
            }
            if (summaryEntry.getActualValue() != null) {
                actuals = actuals.add(summaryEntry.getActualValue());
            }
        }
        return new ProjectBudgetsAllYearSummary(forecasts, actuals, total);

    }

    public List<ProjectBudgetsSummaryEntry> getSummaryEntries() {
        return summaryEntries;
    }

    public void setSummaryEntries(List<ProjectBudgetsSummaryEntry> summaryEntries) {
        this.summaryEntries = summaryEntries;
    }

    public ProjectBudgetsAllYearSummary getProjectBudgetsAllYearSummary() {
        return calculateAllYearSummary();
    }
}
