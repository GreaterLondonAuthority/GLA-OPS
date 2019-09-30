/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.outputs;

import uk.gov.london.ops.domain.project.ComparableItem;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity(name = "outputs_category_cost")
public class OutputCategoryCost implements ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outputs_category_cost_seq_gen")
    @SequenceGenerator(name = "outputs_category_cost_seq_gen", sequenceName = "outputs_category_cost_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "output_cat_config_id")
    private Integer outputCategoryConfigurationId;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    public OutputCategoryCost() {}

    public OutputCategoryCost(Integer outputCategoryConfigurationId) {
        this.outputCategoryConfigurationId = outputCategoryConfigurationId;
    }

    public OutputCategoryCost(Integer outputCategoryConfigurationId, BigDecimal unitCost) {
        this(outputCategoryConfigurationId);
        this.unitCost = unitCost;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOutputCategoryConfigurationId() {
        return outputCategoryConfigurationId;
    }

    public void setOutputCategoryConfigurationId(Integer outputCategoryConfigurationId) {
        this.outputCategoryConfigurationId = outputCategoryConfigurationId;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public OutputCategoryCost clone() {
        return new OutputCategoryCost(outputCategoryConfigurationId, unitCost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputCategoryCost that = (OutputCategoryCost) o;
        return Objects.equals(outputCategoryConfigurationId, that.outputCategoryConfigurationId) &&
                Objects.equals(unitCost, that.unitCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputCategoryConfigurationId, unitCost);
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(outputCategoryConfigurationId);
    }

}
