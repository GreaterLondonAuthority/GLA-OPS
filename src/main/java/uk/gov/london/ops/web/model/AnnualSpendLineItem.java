/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.common.GlaUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chris on 23/01/2017.
 */
public class AnnualSpendLineItem {

    private Integer categoryId;
    private String spendCategory;
    private BigDecimal capitalForecast;
    private BigDecimal capitalActual;
    private BigDecimal revenueForecast;
    private BigDecimal revenueActual;

    public AnnualSpendLineItem() {
    }

    public AnnualSpendLineItem(Integer categoryId, String spendCategory) {
        this.categoryId = categoryId;
        this.spendCategory = spendCategory;
    }

    public AnnualSpendLineItem(BigDecimal capitalForecast, BigDecimal capitalActual, BigDecimal revenueForecast, BigDecimal revenueActual) {
        this.capitalForecast = capitalForecast;
        this.capitalActual = capitalActual;
        this.revenueForecast = revenueForecast;
        this.revenueActual = revenueActual;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getSpendCategory() {
        return spendCategory;
    }

    public void setSpendCategory(String spendCategory) {
        this.spendCategory = spendCategory;
    }

    public BigDecimal getCapitalForecast() {
        return capitalForecast;
    }

    public void setCapitalForecast(BigDecimal capitalForecast) {
        this.capitalForecast = capitalForecast;
    }

    public BigDecimal getCapitalActual() {
        return capitalActual;
    }

    public void setCapitalActual(BigDecimal capitalActual) {
        this.capitalActual = capitalActual;
    }

    public BigDecimal getRevenueForecast() {
        return revenueForecast;
    }

    public void setRevenueForecast(BigDecimal revenueForecast) {
        this.revenueForecast = revenueForecast;
    }

    public BigDecimal getRevenueActual() {
        return revenueActual;
    }

    public void setRevenueActual(BigDecimal revenueActual) {
        this.revenueActual = revenueActual;
    }

    public void addCapitalActual(BigDecimal valueToAdd) {
        capitalActual = GlaUtils.addBigDecimals(capitalActual, valueToAdd);
    }

     public void addCapitalForecast(BigDecimal valueToAdd) {
        capitalForecast = GlaUtils.addBigDecimals(capitalForecast, valueToAdd);
    }

    public void addRevenueActual(BigDecimal valueToAdd) {
        revenueActual = GlaUtils.addBigDecimals(revenueActual, valueToAdd);
    }

    public void addRevenueForecast(BigDecimal valueToAdd) {
        revenueForecast = GlaUtils.addBigDecimals(revenueForecast, valueToAdd);
    }

    @JsonIgnore
    public boolean isExpenditure() {

        List<BigDecimal> bds = Arrays.asList(capitalForecast, capitalActual, revenueActual, revenueForecast);

        for (BigDecimal bd : bds) {
            if (bd != null) {
                return bd.compareTo(BigDecimal.ZERO) <=0;
            }
        }
        // assume it's expnditure
        return true;
    }
}
