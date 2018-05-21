/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import uk.gov.london.ops.domain.outputs.OutputCategoryConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chris on 12/10/2017.
 */
public class OutputSummaryGroup implements Comparable, ComparableItem {

    public static final String DIRECT_OUTPUT_TYPE="DIRECT";
    public static final String INDIRECT_OUTPUT_TYPE="INDIRECT";

    private String outputType;

    private String category;

    private OutputCategoryConfiguration.InputValueType valueType;

    private BigDecimal actual = null;

    private BigDecimal forecast = null;

    private BigDecimal total = null;

    private List<OutputTableSummaryRecord> subcategories = new ArrayList<>();

    public OutputSummaryGroup(String outputType, OutputCategoryConfiguration.InputValueType valueType, String category) {
        this.outputType = outputType;
        this.category = category;
        this.valueType = valueType;
    }

    public void addOutputTableSummaryRecord(OutputTableSummaryRecord record) {
        subcategories.add(record);
        addActual(record.getActual());
        addForecast(record.getForecast());
        addTotal(record.getTotal());
    }

    public List<OutputTableSummaryRecord> getSubcategories() {
        return subcategories;
    }

    private void addForecast(Double newForecast) {
        if (newForecast != null) {
            forecast = forecast == null ? new BigDecimal(newForecast) : forecast.add(new BigDecimal(newForecast));
        }
    }

    private void addTotal(Double newTotal) {
        if (newTotal != null) {
            total = total == null ? new BigDecimal(newTotal) : total.add(new BigDecimal(newTotal));
        }
    }

    private void addActual(Double newActual) {
        if (newActual != null) {
            actual = actual == null ? new BigDecimal(newActual) : actual.add(new BigDecimal(newActual));
        }
    }

    public String getOutputType() {
        return outputType;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getActual() {
        return actual;
    }

    public BigDecimal getForecast() {
        return forecast;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OutputCategoryConfiguration.InputValueType getValueType() {
        return valueType;
    }


    @Override
    public String getComparisonId() {
        return this.getOutputType() + "-" + this.getValueType() + "-" + this.getCategory();
    }

    @Override
    public int compareTo(Object o) {
        OutputSummaryGroup other = (OutputSummaryGroup) o;
        return Comparator.comparing(OutputSummaryGroup::getOutputType)
                .thenComparing(OutputSummaryGroup::getCategory)
                .compare(this, other);
    }
}
