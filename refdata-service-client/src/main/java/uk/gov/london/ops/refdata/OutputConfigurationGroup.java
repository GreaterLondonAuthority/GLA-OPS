/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 20/07/2017.
 */
@Entity(name = "output_config_group")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"handler", "hibernateLazyInitializer"})
public class OutputConfigurationGroup {

    public enum PeriodType {
        Monthly, Quarterly
    }

    @Id
    private Integer id;

    @Column(name = "period_type")
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @Column(name = "output_type_name")
    private String outputTypeName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "subcategory_name")
    private String subcategoryName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "OUTPUT_GROUP_OUTPUT_TYPE",
            joinColumns = @JoinColumn(name = "output_group_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "output_type_key", referencedColumnName = "key"))
    @OrderColumn(name = "display_order")
    private List<OutputType> outputTypes =  new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "OUTPUT_GROUP_OUTPUT_CONFIG",
            joinColumns = @JoinColumn(name = "output_group_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "output_config_id", referencedColumnName = "id"))
    @OrderColumn(name = "display_order")
    private List<OutputCategoryConfiguration> categories =  new ArrayList<>();

    @Transient
    List<Integer> categoryIDs = new ArrayList<>();

    @Transient
    List<String> outputTypeKeys = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<OutputCategoryConfiguration> getCategories() {
        return categories;
    }

    public void setCategories(List<OutputCategoryConfiguration> categories) {
        this.categories = categories;
    }

    public List<Integer> getCategoryIDs() {
        return categoryIDs;
    }

    public void setCategoryIDs(List<Integer> categoryIDs) {
        this.categoryIDs = categoryIDs;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public String getOutputTypeName() {
        return outputTypeName;
    }

    public void setOutputTypeName(String outputTypeName) {
        this.outputTypeName = outputTypeName;
    }

    public List<OutputType> getOutputTypes() {
        return outputTypes;
    }

    public void setOutputTypes(List<OutputType> outputTypes) {
        this.outputTypes = outputTypes;
    }

    public List<String> getOutputTypeKeys() {
        return outputTypeKeys;
    }

    public void setOutputTypeKeys(List<String> outputTypeKeys) {
        this.outputTypeKeys = outputTypeKeys;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public void setSubcategoryName(String subcategoryName) {
        this.subcategoryName = subcategoryName;
    }
}
