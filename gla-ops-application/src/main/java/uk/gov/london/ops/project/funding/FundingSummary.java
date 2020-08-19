/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FundingSummary extends FundingTotalsWrapper {

    private final Map<Integer, FundingSummaryYear> fundingSummaryYears = new HashMap<>();

    public List<FundingSummaryYear> getYearBreakdown() {
        return fundingSummaryYears.values().stream()
                .sorted(Comparator.comparing(FundingSummaryYear::getYear)).collect(Collectors.toList());
    }

    public FundingSummaryYear getYearBreakdown(Integer year) {
        return fundingSummaryYears.get(year);
    }

    public void addActivities(List<FundingActivity> activities) {
        for (FundingActivity activity : activities) {
            addActivity(activity);
        }
    }

    public void addActivity(FundingActivity activity) {
        super.addActivity(activity);

        if (!fundingSummaryYears.containsKey(activity.getYear())) {
            fundingSummaryYears.put(activity.getYear(), new FundingSummaryYear(activity.getYear()));
        }
        fundingSummaryYears.get(activity.getYear()).addActivity(activity);
    }

}
