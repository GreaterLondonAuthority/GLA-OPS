/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FundingClaimCategory {

    private Integer id;
    private String name;
    private boolean actualsEditable;
    private Integer displayOrder;

    private Set<FundingClaimCategory> subCategories = new HashSet<>();
    private Set<FundingClaimCategoryMatchingRule> matchingRules;

    public FundingClaimCategory() {
    }

    public FundingClaimCategory(Integer id, String name, Integer displayOrder, boolean actualsEditable) {
        this.id = id;
        this.name = name;
        this.displayOrder = displayOrder;
        this.actualsEditable = actualsEditable;
    }

    public FundingClaimCategory(Integer id, String name, Integer displayOrder) {
        this(id, name, displayOrder, false);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActualsEditable() {
        return actualsEditable;
    }

    public void setActualsEditable(boolean actualsEditable) {
        this.actualsEditable = actualsEditable;
    }

    public Set<FundingClaimCategoryMatchingRule> getMatchingRules() {
        return matchingRules;
    }

    public void setMatchingRules(Set<FundingClaimCategoryMatchingRule> matchingRules) {
        this.matchingRules = matchingRules;
    }

    public Set<FundingClaimCategory> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(Set<FundingClaimCategory> subCategories) {
        this.subCategories = subCategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FundingClaimCategory that = (FundingClaimCategory) o;
        return actualsEditable == that.actualsEditable
                && Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(displayOrder, that.displayOrder)
                && Objects.equals(subCategories, that.subCategories)
                && Objects.equals(matchingRules, that.matchingRules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, actualsEditable, displayOrder, matchingRules, subCategories);
    }
}
