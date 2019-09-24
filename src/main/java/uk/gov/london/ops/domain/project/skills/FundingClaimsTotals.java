/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.skills;

import java.math.BigDecimal;

public class FundingClaimsTotals {

    private BigDecimal actualTotal;
    private BigDecimal forecastTotal;
    private BigDecimal deliveryTotal;

    public FundingClaimsTotals() {}

    FundingClaimsTotals(BigDecimal actualTotal, BigDecimal forecastTotal, BigDecimal deliveryTotal) {
        this.actualTotal = actualTotal;
        this.forecastTotal = forecastTotal;
        this.deliveryTotal = deliveryTotal;
    }

    public BigDecimal getActualTotal() {
        return actualTotal;
    }

    public void setActualTotal(BigDecimal actualTotal) {
        this.actualTotal = actualTotal;
    }

    public BigDecimal getForecastTotal() {
        return forecastTotal;
    }

    public void setForecastTotal(BigDecimal forecastTotal) {
        this.forecastTotal = forecastTotal;
    }

    public BigDecimal getDeliveryTotal() { return deliveryTotal; }

    public void setDeliveryTotal(BigDecimal deliveryTotal) { this.deliveryTotal = deliveryTotal; }
}
