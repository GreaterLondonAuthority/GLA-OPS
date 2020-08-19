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

public class FundingSummaryYear extends FundingTotalsWrapper {

    private final Integer year;
    private final Map<String, FundingMilestoneGroup> milestones = new HashMap<>();

    public FundingSummaryYear(Integer year) {
        this.year = year;
    }

    public Integer getYear() {
        return year;
    }

    public List<FundingMilestoneGroup> getMilestones() {
        return milestones.values().stream().sorted(Comparator.comparing(FundingMilestoneGroup::getId))
                .collect(Collectors.toList());
    }

    public FundingMilestoneGroup getMilestone(String name) {
        return milestones.get(name);
    }

    public void addActivity(FundingActivity activity) {
        super.addActivity(activity);

        if (!milestones.containsKey(activity.getCategoryDescription())) {
            milestones.put(activity.getCategoryDescription(),
                    new FundingMilestoneGroup(activity.getId(), activity.getCategoryDescription()));
        }
        milestones.get(activity.getCategoryDescription()).addActivity(activity);
    }

}
