/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.outputs;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.framework.ComparableItem;

@Entity(name = "outputs_category_cost")
public class OutputCategoryCost implements ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outputs_category_cost_seq_gen")
    @SequenceGenerator(name = "outputs_category_cost_seq_gen", sequenceName = "outputs_category_cost_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "output_cat_config_id")
    private Integer outputCategoryConfigurationId;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "recovery_output")
    private Boolean recoveryOutput;

    public OutputCategoryCost() {
    }

    public OutputCategoryCost(Integer outputCategoryConfigurationId) {
        this.outputCategoryConfigurationId = outputCategoryConfigurationId;
    }

    public OutputCategoryCost(Integer outputCategoryConfigurationId, BigDecimal unitCost, Boolean recoveryOutput) {
        this(outputCategoryConfigurationId);
        this.unitCost = unitCost;
        this.recoveryOutput = recoveryOutput;
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

    public Boolean isRecoveryOutput() {
        return recoveryOutput;
    }

    public void setRecoveryOutput(Boolean recoveryOutput) {
        this.recoveryOutput = recoveryOutput;
    }

    public OutputCategoryCost clone() {
        return new OutputCategoryCost(outputCategoryConfigurationId, unitCost, recoveryOutput);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OutputCategoryCost that = (OutputCategoryCost) o;
        return Objects.equals(outputCategoryConfigurationId, that.outputCategoryConfigurationId)
                && Objects.equals(unitCost, that.unitCost)
                && Objects.equals(recoveryOutput, that.recoveryOutput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputCategoryConfigurationId, unitCost, recoveryOutput);
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(outputCategoryConfigurationId);
    }

}
