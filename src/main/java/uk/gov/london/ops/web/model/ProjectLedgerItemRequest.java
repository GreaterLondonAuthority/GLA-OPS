/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.project.SpendType;

import java.math.BigDecimal;

import static uk.gov.london.ops.web.model.ProjectLedgerItemRequest.LedgerEntryType.CAPITAL_EXPENDITURE;
import static uk.gov.london.ops.web.model.ProjectLedgerItemRequest.LedgerEntryType.REVENUE_EXPENDITURE;

/**
 * Created by chris on 24/01/2017.
 */
public class ProjectLedgerItemRequest {

    public enum LedgerEntryType {CAPITAL_EXPENDITURE, CAPITAL_CREDIT, REVENUE_EXPENDITURE, REVENUE_CREDIT}

    @JsonIgnore
    private int projectId;

    @JsonIgnore
    private int blockId;

    private int year;

    private int month;

    private int day;

    private Integer categoryId;

    private LedgerEntryType entryType;

    private BigDecimal forecastValue;

    private BigDecimal actualValue;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
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

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getFullDate() {
        return String.format("%02d" , day ) + "/" + String.format("%02d" , month) + "/" +year;
    }

    @JsonIgnore
    public SpendType getSpendType() {
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

    @JsonIgnore
    public LedgerType getLedgerType() {
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

    @JsonIgnore
    public LedgerStatus getLedgerStatus() {
        return actualValue != null ? LedgerStatus.ACTUAL : LedgerStatus.FORECAST;
    }

    @JsonIgnore
    public BigDecimal getValue() {
        BigDecimal value = actualValue != null ? actualValue : forecastValue;

        // credits are only positive values
        if (value != null && (entryType == CAPITAL_EXPENDITURE || entryType == REVENUE_EXPENDITURE)) {
            value = value.negate();
        }

        return value;
    }

}
