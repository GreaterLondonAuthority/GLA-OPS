/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.outputs;

import java.util.Comparator;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.refdata.OutputCategoryConfiguration;

/**
 * Created by chris on 02/02/2017.
 */
@Entity(name = "v_outputs_summary")
public class OutputTableSummaryRecord implements Comparable, ComparableItem {

    @Id
    private Integer id; // not really an ID (is rownumber) but is needed by springdata
    @Column(name = "financial_year")
    private Integer financialYear;
    @Column(name = "project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project for this entry")
    private Integer projectId;
    @Column(name = "block_id")
    @JoinData(targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The block id for this record. These records will be duplicated per block.")
    private Integer blockId;
    @Column(name = "calc_output_type")
    private String outputType;
    @Column(name = "category")
    private String category;
    @Column(name = "subcategory")
    private String subcategory;
    @Column(name = "value_type")
    @Enumerated(EnumType.STRING)
    private OutputCategoryConfiguration.InputValueType valueType;
    @Column(name = "baseline")
    private Double baseline;
    @Column(name = "forecast")
    private Double forecast;
    @Column(name = "actual")
    private Double actual;
    @Column(name = "total")
    private Double total;

    public OutputTableSummaryRecord() {
    }

    public OutputTableSummaryRecord(Integer id, Integer financialYear, Integer projectId, Integer blockId, String outputType,
            String category, String subcategory, OutputCategoryConfiguration.InputValueType valueType, Double forecast,
            Double actual, Double total) {
        this.id = id;
        this.financialYear = financialYear;
        this.projectId = projectId;
        this.blockId = blockId;
        this.outputType = outputType;
        this.category = category;
        this.subcategory = subcategory;
        this.valueType = valueType;
        this.forecast = forecast;
        this.actual = actual;
        this.total = total;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
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

    public OutputCategoryConfiguration.InputValueType getValueType() {
        return valueType;
    }

    public void setValueType(OutputCategoryConfiguration.InputValueType valueType) {
        this.valueType = valueType;
    }

    public Double getForecast() {
        return forecast;
    }

    public void setForecast(Double forecast) {
        this.forecast = forecast;
    }

    public Double getActual() {
        return actual;
    }

    public void setActual(Double actual) {
        this.actual = actual;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getBaseline() {
        return baseline;
    }

    public void setBaseline(Double baseline) {
        this.baseline = baseline;
    }

    @Override
    public int compareTo(Object o) {
        OutputTableSummaryRecord other = (OutputTableSummaryRecord) o;
        return Comparator.comparingInt(OutputTableSummaryRecord::getFinancialYear)
                .thenComparing(OutputTableSummaryRecord::getCategory)
                .thenComparing(OutputTableSummaryRecord::getSubcategory)
                .compare(this, other);
    }

    @Override
    public String getComparisonId() {
        return (this.getFinancialYear() + "-" + this.getOutputType() + "-" + this.getCategory()) + (subcategory == null ? ""
                : "-" + subcategory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OutputTableSummaryRecord that = (OutputTableSummaryRecord) o;
        return Objects.equals(financialYear, that.financialYear)
                && Objects.equals(outputType, that.outputType)
                && Objects.equals(category, that.category)
                && Objects.equals(subcategory, that.subcategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(financialYear, outputType, category, subcategory);
    }

    @Override
    public String toString() {
        return "OutputTableSummaryRecord{"
                + "id=" + id
                + ", financialYear=" + financialYear
                + ", projectId=" + projectId
                + ", blockId=" + blockId
                + ", outputType='" + outputType + '\''
                + ", category='" + category + '\''
                + ", subcategory='" + subcategory + '\''
                + ", valueType=" + valueType
                + ", baseline=" + baseline
                + ", forecast=" + forecast
                + ", actual=" + actual
                + ", total=" + total
                + '}';
    }

}
