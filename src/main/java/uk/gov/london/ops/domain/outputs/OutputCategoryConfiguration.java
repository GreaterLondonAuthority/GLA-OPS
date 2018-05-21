/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.outputs;

import javax.persistence.*;

/**
 * Created by chris on 16/02/2017.
 */
@Entity(name="output_cat_config")
public class OutputCategoryConfiguration {

    public enum InputValueType {
        UNITS,
        HECTARES,
        POSITIONS,
        SQUARE_METRES,
        SQUARE_METRES_NET,
        SQUARE_METRES_GROSS,
        BEDROOMS,
        MONETARY_VALUE,
        NUMBER_OF,
        NUMBER_OF_DECIMAL,
        ENTER_VALUE,
        ENTER_VALUE_DECIMALS,
        NET_AREA,
        DISTANCE,
        LENGTH
    }

    @Id
    private Integer id;

    @Column(name="category")
    private String category;

    @Column(name="subcategory")
    private String subcategory;

    @Enumerated(EnumType.STRING)
    @Column(name="value_type")
    private InputValueType valueType;


    public OutputCategoryConfiguration() {
    }

    public OutputCategoryConfiguration(Integer id, String category, String subcategory, InputValueType valueType) {
        this.id = id;
        this.category = category;
        this.subcategory = subcategory;
        this.valueType = valueType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public InputValueType getValueType() {
        return valueType;
    }

    public void setValueType(InputValueType valueType) {
        this.valueType = valueType;
    }

}
