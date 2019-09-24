/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.project.SpendType;
import uk.gov.london.ops.payment.LedgerStatus;
import uk.gov.london.ops.payment.LedgerType;

import java.math.BigDecimal;

import static uk.gov.london.ops.web.model.ProjectLedgerItemRequest.LedgerEntryType.CAPITAL_EXPENDITURE;
import static uk.gov.london.ops.web.model.ProjectLedgerItemRequest.LedgerEntryType.REVENUE_EXPENDITURE;

/**
 * Created by chris on 24/01/2017.
 */
public class ProjectLedgerItemRequest {

    public enum LedgerEntryType {CAPITAL_EXPENDITURE, CAPITAL_CREDIT, REVENUE_EXPENDITURE, REVENUE_CREDIT}

    private Integer id;

    @JsonIgnore
    private int projectId;

    @JsonIgnore
    private int blockId;

    private Integer year;

    private Integer month;

    private Integer day;

    private Integer quarter;

    private Integer categoryId;

    private String category;

    private String subCategory;

    private Integer externalId;

    private LedgerType ledgerType;

    private SpendType spendType;

    private LedgerEntryType entryType;

    private BigDecimal forecastValue;

    private BigDecimal actualValue;

    private BigDecimal value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
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

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public LedgerType getLedgerType() {
        if (ledgerType != null) {
            return ledgerType;
        }

        if (entryType == null) {
            return null;
        }

        if (LedgerStatus.ACTUAL.equals(getLedgerStatus())) {
            return LedgerType.PAYMENT;
        }

        switch (entryType) {

            case CAPITAL_CREDIT:
            case REVENUE_CREDIT:
            case CAPITAL_EXPENDITURE:
            case REVENUE_EXPENDITURE:
                return LedgerType.PAYMENT;

            default:
                return null;
        }
    }

    public void setLedgerType(LedgerType ledgerType) {
        this.ledgerType = ledgerType;
    }

    public SpendType getSpendType() {
        if (spendType != null) {
            return spendType;
        }

        switch (entryType) {

            case CAPITAL_CREDIT:
            case CAPITAL_EXPENDITURE:
                return SpendType.CAPITAL;

            case REVENUE_CREDIT:
            case REVENUE_EXPENDITURE:
                return SpendType.REVENUE;

            default:
                return null;
        }
    }

    public void setSpendType(SpendType spendType) {
        this.spendType = spendType;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(LedgerEntryType entryType) {
        this.entryType = entryType;
    }

    public BigDecimal getForecastValue() {
        return forecastValue;
    }

    public void setForecastValue(BigDecimal forecastValue) {
        this.forecastValue = forecastValue;
    }

    public BigDecimal getActualValue() {
        return actualValue;
    }

    public void setActualValue(BigDecimal actualValue) {
        this.actualValue = actualValue;
    }

    public BigDecimal getValue() {
        BigDecimal v = value != null ? value : actualValue != null ? actualValue : forecastValue;

        // credits are only positive values
        if (v != null && (entryType == CAPITAL_EXPENDITURE || entryType == REVENUE_EXPENDITURE)) {
            v = v.negate();
        }

        return v;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getFullDate() {
        return String.format("%02d" , day ) + "/" + String.format("%02d" , month) + "/" +year;
    }

    @JsonIgnore
    public LedgerStatus getLedgerStatus() {
        return actualValue != null ? LedgerStatus.ACTUAL : LedgerStatus.FORECAST;
    }

}
