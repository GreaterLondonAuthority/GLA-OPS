/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

public class MarketTypeDTO {

    private Integer id;

    private Integer displayOrder;

    private String name;

    private Boolean availableForRental = false;

    private Boolean availableForSales = false;

    public MarketTypeDTO() {
    }

    public MarketTypeDTO(MarketType marketType, String name) {
        this.id = marketType.getId();
        this.displayOrder = marketType.getDisplayOrder();
        this.name = name;
        this.availableForRental = marketType.getAvailableForRental();
        this.availableForSales = marketType.getAvailableForSales();
    }

    public MarketTypeDTO(MarketType marketType) {
        this(marketType, marketType.getName());
    }

    public Integer getId() {
        return id;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public String getName() {
        return name;
    }

    public Boolean getAvailableForRental() {
        return availableForRental;
    }

    public Boolean getAvailableForSales() {
        return availableForSales;
    }
}
