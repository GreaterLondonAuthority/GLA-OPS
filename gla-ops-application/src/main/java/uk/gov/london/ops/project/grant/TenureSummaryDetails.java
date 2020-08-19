/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.grant;

import com.fasterxml.jackson.annotation.JsonInclude;
import uk.gov.london.ops.framework.ComparableItem;

/**
 * Created by chris on 17/11/2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenureSummaryDetails implements ComparableItem {

    private String name;

    private Integer grantEligibleUnits;
    private Integer unitDevelopmentCost;
    private Integer grantRate;
    private Integer grantPerUnit;
    private Long totalGrant;
    private Integer year;


    public TenureSummaryDetails() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGrantEligibleUnits() {
        return grantEligibleUnits;
    }

    public void setGrantEligibleUnits(Integer grantEligibleUnits) {
        this.grantEligibleUnits = grantEligibleUnits;
    }

    public Integer getGrantRate() {
        return grantRate;
    }

    public void setGrantRate(Integer grantRate) {
        this.grantRate = grantRate;
    }

    public Long getTotalGrant() {
        return totalGrant;
    }

    public void setTotalGrant(Long totalGrant) {
        this.totalGrant = totalGrant;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getUnitDevelopmentCost() {
        return unitDevelopmentCost;
    }

    public void setUnitDevelopmentCost(Integer unitDevelopmentCost) {
        this.unitDevelopmentCost = unitDevelopmentCost;
    }

    public Integer getGrantPerUnit() {
        return grantPerUnit;
    }

    public void setGrantPerUnit(Integer grantPerUnit) {
        this.grantPerUnit = grantPerUnit;
    }

    @Override
    public String getComparisonId() {
        return name;
    }
}
