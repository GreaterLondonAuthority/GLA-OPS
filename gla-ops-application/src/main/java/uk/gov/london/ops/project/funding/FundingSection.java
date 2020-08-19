/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.gov.london.ops.project.claim.Claim;

public class FundingSection extends FundingTotalsWrapper implements ClaimableFundingEntity {

    private Integer sectionNumber;

    private Map<String, FundingMilestoneGroup> milestones = new HashMap<>();

    public FundingClaimStatus status;

    public String notClaimableReason;

    public Claim claim;

    public FundingSection(Integer sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public Integer getSectionNumber() {
        return sectionNumber;
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

    public void setSectionNumber(Integer sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public void setMilestones(Map<String, FundingMilestoneGroup> milestones) {
        this.milestones = milestones;
    }


    public String getNotClaimableReason() {
        return notClaimableReason;
    }

    public void setNotClaimableReason(String notClaimableReason) {
        this.notClaimableReason = notClaimableReason;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public FundingClaimStatus getStatus() {
        return status;
    }

    public void setStatus(FundingClaimStatus status) {
        this.status = status;
    }

    @JsonIgnore
    public boolean isMonetaryClaimRequired() {
        return this.getTotalCapitalValue().compareTo(BigDecimal.ZERO) != 0
                || this.getTotalRevenueValue().compareTo(BigDecimal.ZERO) != 0;
    }

    @JsonIgnore
    public boolean isEvidenceAttached() {
        return this.getMilestones().stream().flatMap(g -> g.getActivities().stream())
                .noneMatch(m -> m.getAttachments().size() == 0);
    }

    public List<FundingActivityLineItem> getActivities() {
        return this.getMilestones().stream().flatMap(g -> g.getActivities().stream()).collect(Collectors.toList());
    }

    public FundingSectionClaimsSummary getSectionClaimsSummary() {
        int nbClaimedActivities = 0;
        BigDecimal totalCapitalClaimed = BigDecimal.ZERO;
        BigDecimal totalRevenueClaimed = BigDecimal.ZERO;
        for (FundingActivityLineItem activity : getActivities()) {
            if (activity.isClaimed()) {
                nbClaimedActivities++;
                totalCapitalClaimed = nullSafeAdd(totalCapitalClaimed, activity.getCapitalValue());
                totalRevenueClaimed = nullSafeAdd(totalRevenueClaimed, activity.getRevenueValue());
            }
        }
        return new FundingSectionClaimsSummary(nbClaimedActivities, totalCapitalClaimed, totalRevenueClaimed);
    }

}
