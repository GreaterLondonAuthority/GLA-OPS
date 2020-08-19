/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

/**
 * Stores static key value data Created by chris on 26/05/2017.
 */
@Entity(name = "category_value")
public class CategoryValue {

    public enum Category {
        Bedrooms, UnitTypes, PaymentDeclineReason, ReclaimDeclineReason, RiskCategory
    }

    @Id
    private Integer id;

    @Column
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column
    private Integer displayOrder;

    @Column
    private String displayValue;

    public CategoryValue() {
    }

    public CategoryValue(Integer id, Category category, String displayValue, Integer displayOrder) {
        this.id = id;
        this.category = category;
        this.displayValue = displayValue;
        this.displayOrder = displayOrder;
    }

    public Integer getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}


