/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.unit;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.jpa.NonJoin;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.refdata.CategoryValue;
import uk.gov.london.ops.refdata.MarketType;

@Entity(name = "units_table_entry")
public class UnitDetailsTableEntry implements OpsEntity<Integer>, ComparableItem {

    public enum Type {
        Rent, Sales
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "units_table_entry_seq_gen")
    @SequenceGenerator(name = "units_table_entry_seq_gen", sequenceName = "units_table_entry_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "original_id")
    @JoinData(targetTable = "units_table_entry", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "Self join, if this is a clone of a previous issue caused by editing an approved block")
    private Integer originalId;

    @Column(name = "project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project for this receipt")
    private Integer projectId;

    @Column(name = "block_id")
    @JoinData(targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The block id for this record. These records will duplicated per block")
    private Integer blockId;

    @Column(name = "type")
    private Type type;

    @Column(name = "tenure_id")
    @NonJoin("Is the external ID of the tenure")
    private Integer tenureId;

    @JoinColumn(name = "market_type_id")
    @OneToOne()
    private MarketType marketType;

    @JoinColumn(name = "nb_beds_id")
    @OneToOne()
    private CategoryValue nbBeds;

    @JoinColumn(name = "unit_type_id")
    @OneToOne
    private CategoryValue unitType;

    @Column(name = "nb_units")
    private Integer nbUnits;

    @Column(name = "net_weekly_rent")
    private BigDecimal netWeeklyRent;

    @Column(name = "weekly_service_charge")
    private BigDecimal weeklyServiceCharge;

    @Column(name = "weekly_market_rent")
    private BigDecimal weeklyMarketRent;

    @Column(name = "market_value")
    private BigDecimal marketValue;

    @Column(name = "first_tranche_sales")
    private Integer firstTrancheSales;

    @Column(name = "discount_off_market_value")
    private Integer discountOffMarketValue;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;


    @JoinData(joinType = Join.JoinType.ManyToOne, targetColumn = "username", targetTable = "users",
            comment = "")
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @JoinData(joinType = Join.JoinType.ManyToOne, targetColumn = "username", targetTable = "users",
            comment = "")
    @Column(name = "modified_by")
    private String modifiedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getTenureId() {
        return tenureId;
    }

    public void setTenureId(Integer tenureId) {
        this.tenureId = tenureId;
    }

    public MarketType getMarketType() {
        return marketType;
    }

    public void setMarketType(MarketType marketType) {
        this.marketType = marketType;
    }

    public CategoryValue getNbBeds() {
        return nbBeds;
    }

    public void setNbBeds(CategoryValue nbBeds) {
        this.nbBeds = nbBeds;
    }

    public CategoryValue getUnitType() {
        return unitType;
    }

    public void setUnitType(CategoryValue unitType) {
        this.unitType = unitType;
    }

    public Integer getNbUnits() {
        return nbUnits;
    }

    public void setNbUnits(Integer nbUnits) {
        this.nbUnits = nbUnits;
    }

    public BigDecimal getNetWeeklyRent() {
        return netWeeklyRent;
    }

    public void setNetWeeklyRent(BigDecimal netWeeklyRent) {
        this.netWeeklyRent = netWeeklyRent;
    }

    public BigDecimal getWeeklyServiceCharge() {
        return weeklyServiceCharge;
    }

    public void setWeeklyServiceCharge(BigDecimal weeklyServiceCharge) {
        this.weeklyServiceCharge = weeklyServiceCharge;
    }

    public BigDecimal getWeeklyMarketRent() {
        return weeklyMarketRent;
    }

    public void setWeeklyMarketRent(BigDecimal weeklyMarketRent) {
        this.weeklyMarketRent = weeklyMarketRent;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public Integer getFirstTrancheSales() {
        return firstTrancheSales;
    }

    public void setFirstTrancheSales(Integer firstTrancheSales) {
        this.firstTrancheSales = firstTrancheSales;
    }

    public Integer getDiscountOffMarketValue() {
        return discountOffMarketValue;
    }

    public void setDiscountOffMarketValue(Integer discountOffMarketValue) {
        this.discountOffMarketValue = discountOffMarketValue;
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

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
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

    @Transient
    public BigDecimal getRentTotal() {
        if (getNetWeeklyRent() != null && getWeeklyServiceCharge() != null) {
            return getNetWeeklyRent().add(getWeeklyServiceCharge());
        }
        return null;
    }

    @Transient
    public BigDecimal getRentPercentageOfMarket() {
        final BigDecimal totalRent = getRentTotal();
        final BigDecimal weeklyMarketRentTemp = getWeeklyMarketRent();
        if (totalRent != null
                && weeklyMarketRentTemp != null
                && BigDecimal.ZERO.compareTo(weeklyMarketRentTemp) != 0) {
            return totalRent.divide(weeklyMarketRentTemp, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        return null;
    }

    public UnitDetailsTableEntry copy() {
        UnitDetailsTableEntry copy = new UnitDetailsTableEntry();
        copy.projectId = this.projectId;
        copy.type = this.type;
        copy.tenureId = this.tenureId;
        copy.marketType = this.marketType;
        copy.nbBeds = this.nbBeds;
        copy.unitType = this.unitType;
        copy.nbUnits = this.nbUnits;
        copy.netWeeklyRent = this.netWeeklyRent;
        copy.weeklyServiceCharge = this.weeklyServiceCharge;
        copy.weeklyMarketRent = this.weeklyMarketRent;
        copy.marketValue = this.marketValue;
        copy.firstTrancheSales = this.firstTrancheSales;
        copy.discountOffMarketValue = this.discountOffMarketValue;
        copy.createdOn = this.createdOn;
        copy.createdBy = this.createdBy;
        copy.modifiedOn = this.modifiedOn;
        copy.modifiedBy = this.modifiedBy;
        copy.originalId = this.getOriginalId(); // don't change this for property, we need get method
        return copy;
    }

    public void merge(UnitDetailsTableEntry unitTableEntry) {
        this.setNbUnits(unitTableEntry.getNbUnits());
        this.setNetWeeklyRent(unitTableEntry.getNetWeeklyRent());
        this.setWeeklyServiceCharge(unitTableEntry.getWeeklyServiceCharge());
        this.setWeeklyMarketRent(unitTableEntry.getWeeklyMarketRent());
        this.setMarketValue(unitTableEntry.getMarketValue());
        this.setFirstTrancheSales(unitTableEntry.getFirstTrancheSales());
        this.setDiscountOffMarketValue(unitTableEntry.getDiscountOffMarketValue());
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(getOriginalId());
    }

}
