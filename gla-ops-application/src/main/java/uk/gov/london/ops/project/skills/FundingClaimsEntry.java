/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;

@Entity(name = "funding_claims_entry")
public class FundingClaimsEntry {

    private static final int NO_FORECAST_PERIOD = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_claims_entry_seq_gen")
    @SequenceGenerator(name = "funding_claims_entry_seq_gen", sequenceName = "funding_claims_entry_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "original_id")
    private Integer originalId;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(name = "period")
    private Integer period;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "parent_category_id")
    private Integer parentCategoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "actual_delivery")
    private BigDecimal actualDelivery;

    @Column(name = "forecast_delivery")
    private BigDecimal forecastDelivery;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "entry_id")
    @JoinData(joinType = Join.JoinType.OneToMany, comment = "join to contract type")
    private Set<ContractTypeFundingEntry> contractTypeFundingEntries = new HashSet<>();


    public FundingClaimsEntry() {
    }

    @Transient
    private Boolean actualsEditable;

    public FundingClaimsEntry(Integer academicYear, Integer period, Integer categoryId, String categoryName,
            Integer displayOrder) {
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

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
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

    public Set<ContractTypeFundingEntry> getContractTypeFundingEntries() {
        return contractTypeFundingEntries;
    }

    public void setContractTypeFundingEntries(Set<ContractTypeFundingEntry> contractTypeFundingEntries) {
        this.contractTypeFundingEntries = contractTypeFundingEntries;
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
        clone.setParentCategoryId(this.getParentCategoryId());
        clone.setCategoryName(this.getCategoryName());
        clone.setDisplayOrder(this.getDisplayOrder());
        clone.setActualDelivery(this.getActualDelivery());
        clone.setForecastDelivery(this.getForecastDelivery());

        contractTypeFundingEntries.forEach(c -> clone.getContractTypeFundingEntries().add(c.clone()));

        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FundingClaimsEntry that = (FundingClaimsEntry) o;
        return Objects.equals(originalId, that.originalId)
                && Objects.equals(academicYear, that.academicYear)
                && Objects.equals(period, that.period)
                && Objects.equals(categoryId, that.categoryId)
                && Objects.equals(parentCategoryId, that.parentCategoryId)
                && Objects.equals(categoryName, that.categoryName)
                && Objects.equals(displayOrder, that.displayOrder)
                && Objects.equals(actualDelivery, that.actualDelivery)
                && Objects.equals(forecastDelivery, that.forecastDelivery);

    }

    @Override
    public int hashCode() {
        return Objects
                .hash(originalId, academicYear, period, categoryId, parentCategoryId, categoryName, displayOrder, actualDelivery,
                        forecastDelivery);
    }

    public BigDecimal getContractTypeTotal() {
        return this.getContractTypeFundingEntries().stream()
                .map(ContractTypeFundingEntry::getContractValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getFlexibleAllocationTotal() {
        return this.getContractTypeFundingEntries().stream()
                .map(ContractTypeFundingEntry::getFlexibleAllocation)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getFlexibleAllocationTotal(String contractType) {
        return this.getContractTypeFundingEntries().stream()
                .filter(t -> t.getContractType().equals(contractType))
                .map(ContractTypeFundingEntry::getFlexibleAllocation)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getContracTypeTotal(String contractType) {
        return this.getContractTypeFundingEntries().stream()
                .filter(t -> t.getContractType().equals(contractType))
                .map(ContractTypeFundingEntry::getContractValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void removeContractTypeFundingEntriesFor(String contractType) {
        this.contractTypeFundingEntries.removeIf(e -> e.getContractType().equals(contractType));
    }

    public boolean isEmpty() {
        return this.getActualDelivery() == null
                && this.getForecastDelivery() == null
                && (this.getContractTypeFundingEntries().isEmpty() ||
                this.getContractTypeFundingEntries().stream().allMatch(ContractTypeFundingEntry::isEmpty));
    }

    public boolean isFull() {
        return ((this.getActualsEditable() != null && this.getActualsEditable() && this.getActualDelivery() !=null)
                || (this.getActualsEditable() == null || !this.getActualsEditable()))
                && (this.getForecastDelivery() != null || this.getPeriod() == NO_FORECAST_PERIOD)
                && (this.getContractTypeFundingEntries().isEmpty() ||
                this.getContractTypeFundingEntries().stream().allMatch(ContractTypeFundingEntry::isComplete));
    }

}
