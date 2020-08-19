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

public class FundingYearBreakdown extends FundingTotalsWrapper {

    private final Integer year;
    private final Map<Integer, FundingSection> sections = new HashMap<>();

    public FundingYearBreakdown(Integer year, List<FundingActivity> activities) {
        this.year = year;
        for (FundingActivity activity: activities) {
            addActivity(activity);
        }
    }

    public Integer getYear() {
        return year;
    }

    public List<FundingSection> getSections() {
        return sections.values().stream()
                .sorted(Comparator.comparing(FundingSection::getSectionNumber))
                .collect(Collectors.toList());
    }

    public FundingSection getSection(Integer sectionNumber) {
        return sections.get(sectionNumber);
    }

    public void addActivity(FundingActivity activity) {
        super.addActivity(activity);

        if (!sections.containsKey(activity.getQuarter())) {
            sections.put(activity.getQuarter(), new FundingSection(activity.getQuarter()));
        }
        sections.get(activity.getQuarter()).addActivity(activity);
    }

}
