/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.london.ops.project.funding.FundingEntitiesKt.createFundingActivityLineItemFrom;

public class FundingMilestoneGroup extends FundingTotalsWrapper {

    private final Integer id;
    private final String name;
    private final List<FundingActivityLineItem> activities = new ArrayList<>();

    public FundingMilestoneGroup(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public List<FundingActivityLineItem> getActivities() {
        return activities.stream().sorted(Comparator.comparingInt(FundingActivityLineItem::getId)).collect(Collectors.toList());
    }

    public void addActivity(FundingActivity activity) {
        super.addActivity(activity);

        activities.add(createFundingActivityLineItemFrom(activity));
    }

}
