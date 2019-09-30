/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model.project;

import uk.gov.london.common.GlaUtils;

import java.math.BigDecimal;

public class AnnualReceiptsLineItem {

    private Integer forecastId;
    private Integer categoryId;
    private String category;
    private BigDecimal forecast;
    private BigDecimal actual;

    public AnnualReceiptsLineItem() {}

    public AnnualReceiptsLineItem(Integer categoryId, String category) {
        this.categoryId = categoryId;
        this.category = category;
    }

    public AnnualReceiptsLineItem(String category, BigDecimal forecast, BigDecimal actual) {
        this.category = category;
        this.forecast = forecast;
        this.actual = actual;
    }

    public Integer getForecastId() {
        return forecastId;
    }

    public void setForecastId(Integer forecastId) {
        this.forecastId = forecastId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getForecast() {
        return forecast;
    }

    public void setForecast(BigDecimal forecast) {
        this.forecast = forecast;
    }

    public BigDecimal getActual() {
        return actual;
    }

    public void setActual(BigDecimal actual) {
        this.actual = actual;
    }

    public void addActual(BigDecimal actual) {
        this.actual = GlaUtils.addBigDecimals(this.actual, actual);
    }

    public void addForecast(BigDecimal forecast) {
        this.forecast = GlaUtils.addBigDecimals(this.forecast, forecast);
    }
}
