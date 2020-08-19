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

public class FundingByYearAndQuarter extends FundingSectionTotalsWrapper {

    private final Map<Integer, FundingYQYear> years = new HashMap<>();

    public List<FundingYQYear> getYears() {
        return years.values().stream().sorted(Comparator.comparing(FundingYQYear::getYear)).collect(Collectors.toList());
    }

    public FundingYQYear getYear(Integer year) {
        return years.get(year);
    }

    public void addActivities(List<FundingActivity> activities) {
        for (FundingActivity activity : activities) {
            addActivity(activity);
        }
    }

    @Override
    public void addActivity(FundingActivity activity) {
        super.addActivity(activity);

        if (!years.containsKey(activity.getYear())) {
            years.put(activity.getYear(), new FundingYQYear(activity.getYear()));
        }

        years.get(activity.getYear()).addActivity(activity);
    }

    /* YEAR WRAPPER */
    public class FundingYQYear extends FundingSectionTotalsWrapper {

        private final Integer year;
        private final Map<String, FundingYQMilestone> milestones = new HashMap<>();

        FundingYQYear(Integer year) {
            this.year = year;
        }

        public Integer getYear() {
            return year;
        }

        public List<FundingYQMilestone> getMilestones() {
            return milestones.values().stream().sorted(Comparator.comparing(FundingYQMilestone::getId))
                    .collect(Collectors.toList());
        }

        public FundingYQMilestone getMilestone(String name) {
            return milestones.get(name);
        }

        @Override
        protected void addActivity(FundingActivity activity) {
            super.addActivity(activity);

            if (!milestones.containsKey(activity.getCategoryDescription())) {
                milestones.put(activity.getCategoryDescription(),
                        new FundingYQMilestone(activity.getId(), activity.getCategoryDescription()));
            }

            milestones.get(activity.getCategoryDescription()).addActivity(activity);
        }
    }

    /* MILESTONE WRAPPER */
    public class FundingYQMilestone extends FundingSectionTotalsWrapper {

        private final Integer id;
        private final String name;
        private final Map<String, FundingYQActivity> activities = new HashMap<>();

        public FundingYQMilestone(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Integer getId() {
            return id;
        }

        public List<FundingYQActivity> getActivities() {
            return activities.values().stream()
                    .sorted(Comparator.comparingInt(FundingYQActivity::getId))
                    .collect(Collectors.toList());
        }

        public FundingYQActivity getActivity(String name) {
            return activities.get(name);
        }

        @Override
        protected void addActivity(FundingActivity activity) {
            super.addActivity(activity);

            if (!activities.containsKey(activity.getName())) {
                activities.put(activity.getName(), new FundingYQActivity(activity.getId(), activity.getName()));
            }

            activities.get(activity.getName()).addActivity(activity);
        }
    }

    /* ACTIVITY WRAPPER */
    public class FundingYQActivity extends FundingSectionTotalsWrapper {

        private final String name;
        private final Integer id;

        public FundingYQActivity(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Integer getId() {
            return id;
        }
    }

}
