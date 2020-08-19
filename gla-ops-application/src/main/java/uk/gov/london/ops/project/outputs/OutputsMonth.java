/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.outputs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import uk.gov.london.common.GlaUtils;

public class OutputsMonth {

    List<OutputTableEntry> outputs = new ArrayList<>();
    private final Integer month;

    public OutputsMonth(Integer month) {
        this.month = month;
    }

    public Integer getMonth() {
        return month;
    }

    public List<OutputTableEntry> getOutputs() {
        return outputs;
    }

    void addEntry(OutputTableEntry entry) {
        outputs.add(entry);
    }

    public BigDecimal getForecastTotal() {
        return outputs.stream().map(c -> c.getForecast()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getActualTotal() {
        return outputs.stream().map(c -> c.getActual()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getDifferenceTotal() {
        return outputs.stream().map(c -> c.getDifference()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getForecastTotalTotal() {
        return outputs.stream().map(c -> c.getForecastTotal()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getActualTotalTotal() {
        return outputs.stream().map(c -> c.getActualTotal()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getRemainingAdvancePaymentTotal() {
        return outputs.stream().map(c -> c.getRemainingAdvancePayment()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getClaimableAmountTotal() {
        return outputs.stream().map(c -> c.getClaimableAmount()).reduce(null, GlaUtils::nullSafeAdd);
    }

}