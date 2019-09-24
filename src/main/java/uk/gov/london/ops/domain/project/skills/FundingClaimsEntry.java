/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.skills;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;

@Entity(name = "funding_claims_entry")
public class FundingClaimsEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_claims_entry_seq_gen")
    @SequenceGenerator(name = "funding_claims_entry_seq_gen", sequenceName = "funding_claims_entry_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "original_id")
    private Integer originalId;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(name = "period")
    private Integer period;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "actual_delivery")
    private BigDecimal actualDelivery;

    @Column(name = "forecast_delivery")
    private BigDecimal forecastDelivery;
    public FundingClaimsEntry() {}

    @Transient
    private Boolean actualsEditable;

    public FundingClaimsEntry(Integer academicYear, Integer period, Integer categoryId, String categoryName, Integer displayOrder) {
        this.academicYear = academicYear;
        this.period = period;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.displayOrder = displayOrder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOriginalId() {
        if (originalId == null) {
            return id;
        }
        return originalId;
    }

    public void setOriginalId(Integer originalId) {
        this.originalId = originalId;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public BigDecimal getActualDelivery() {
        return actualDelivery;
    }

    public void setActualDelivery(BigDecimal actualDelivery) {
        this.actualDelivery = actualDelivery;
    }

    public BigDecimal getForecastDelivery() {
        return forecastDelivery;
    }

    public void setForecastDelivery(BigDecimal forecastDelivery) {
        this.forecastDelivery = forecastDelivery;
    }

    public Boolean getActualsEditable() {
        return actualsEditable;
    }

    public void setActualsEditable(Boolean actualsEditable) {
        this.actualsEditable = actualsEditable;
    }

    @JsonProperty(value = "totalDelivery", access = JsonProperty.Access.READ_ONLY)
    public BigDecimal getTotalDelivery() {
        return nullSafeAdd(BigDecimal.ZERO, actualDelivery, forecastDelivery);
    }

    public FundingClaimsEntry clone() {
        FundingClaimsEntry clone = new FundingClaimsEntry();
        clone.setOriginalId(this.getOriginalId());
        clone.setAcademicYear(this.getAcademicYear());
        clone.setPeriod(this.getPeriod());
        clone.setCategoryId(this.getCategoryId());
        clone.setCategoryName(this.getCategoryName());
        clone.setDisplayOrder(this.getDisplayOrder());
        clone.setActualDelivery(this.getActualDelivery());
        clone.setForecastDelivery(this.getForecastDelivery());
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FundingClaimsEntry that = (FundingClaimsEntry) o;
        return Objects.equals(originalId, that.originalId) &&
                Objects.equals(academicYear, that.academicYear) &&
                Objects.equals(period, that.period) &&
                Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(categoryName, that.categoryName) &&
                Objects.equals(displayOrder, that.displayOrder) &&
                Objects.equals(actualDelivery, that.actualDelivery) &&
                Objects.equals(forecastDelivery, that.forecastDelivery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalId, academicYear, period, categoryId, categoryName, displayOrder, actualDelivery, forecastDelivery);
    }

}
