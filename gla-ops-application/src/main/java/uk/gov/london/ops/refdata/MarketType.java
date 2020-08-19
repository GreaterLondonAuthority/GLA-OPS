/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by chris on 30/05/2017.
 */
@Entity(name = "market_type")
public class MarketType {

    @Id
    private Integer id;

    @Column
    private Integer displayOrder;

    @JsonIgnore
    @Column
    private String name;

    @Column
    private Boolean availableForRental = false;

    @Column
    private Boolean availableForSales = false;

    public MarketType() {
    }

    public MarketType(Integer id, Integer displayOrder, String name, Boolean availableForRental, Boolean availableForSales) {
        this.id = id;
        this.displayOrder = displayOrder;
        this.name = name;
        this.availableForRental = availableForRental;
        this.availableForSales = availableForSales;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAvailableForRental() {
        return availableForRental;
    }

    public void setAvailableForRental(Boolean availableForRental) {
        this.availableForRental = availableForRental;
    }

    public Boolean getAvailableForSales() {
        return availableForSales;
    }

    public void setAvailableForSales(Boolean availableForSales) {
        this.availableForSales = availableForSales;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MarketType that = (MarketType) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
