/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

@Entity(name = "tenure_type")
public class TenureType {

    public static final String LONDON_AFFORDABLE_RENT = "London Affordable Rent";
    public static final String LONDON_LIVING_RENT = "London Living Rent";
    public static final String LONDON_SHARED_OWNERSHIP = "London Shared Ownership";
    public static final String OTHER_AFFORDABLE = "Other Affordable";
    public static final String AFFORDABLE_RENT = "Affordable Rent";
    public static final String AFFORDABLE_HOME_OWNERSHIP = "Affordable Home Ownership";
    public static final String LEGACY_SHARED_OWNERSHIP = "Legacy Shared Ownership";

    @Id
    @NotNull
    private Integer id;

    @Column(name = "name")
    @NotNull
    private String name;

    @ManyToMany
    @JoinTable(name = "tenure_market_type",
            joinColumns = @JoinColumn(name = "tenure_type_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "market_type_id", referencedColumnName = "id"))
    private List<MarketType> marketTypes;

    public TenureType() {}

    public TenureType(Integer id) {
        this.id = id;
    }

    public TenureType(Integer id, String name) {
        this(id);
        this.name = name;
    }

    public TenureType(Integer id, String name, MarketType ... marketTypes) {
        this(id, name);
        this.marketTypes = Arrays.asList(marketTypes);
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

    public List<MarketType> getMarketTypes() {
        return marketTypes;
    }

    public void setMarketTypes(List<MarketType> marketTypes) {
        this.marketTypes = marketTypes;
    }

}
