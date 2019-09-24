/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import uk.gov.london.common.GlaUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity(name = "claim")
public class Claim implements ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_claim_seq_gen")
    @SequenceGenerator(name = "funding_claim_seq_gen", sequenceName = "funding_claim_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "original_id")
    private Integer originalId;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "block_id")
    private Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "claim_type_period")
    private Integer claimTypePeriod;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "claim_status")
    @Enumerated(EnumType.STRING)
    private ClaimStatus claimStatus;

    @Column(name = "claim_type")
    @Enumerated(EnumType.STRING)
    private ClaimType claimType;

    @Column(name = "claimed_on")
    private OffsetDateTime claimedOn;

    public Claim() {
    }

    public Claim(Integer year, Integer claimTypePeriod) {
        this.year = year;
        this.claimTypePeriod = claimTypePeriod;
    }

    public Claim(ClaimType type, Integer year, Integer claimTypePeriod) {
        this.claimType = type;
        this.year = year;
        this.claimTypePeriod = claimTypePeriod;
    }

    public Claim(Integer year, Integer claimTypePeriod, ClaimStatus claimStatus) {
        this.year = year;
        this.claimTypePeriod = claimTypePeriod;
        this.claimStatus = claimStatus;
    }

    public Integer getClaimTypePeriod() {
        return claimTypePeriod;
    }

    public Claim(Integer blockId, ClaimStatus claimStatus, ClaimType claimType) {
        this.blockId = blockId;
        this.claimStatus = claimStatus;
        this.claimType = claimType;
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

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setClaimTypePeriod(Integer claimTypePeriod) {
        this.claimTypePeriod = claimTypePeriod;
    }

    public enum ClaimType {ADVANCE, QUARTER, MONTH}

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
    }

    public ClaimType getClaimType() {
        return claimType;
    }

    public void setClaimType(ClaimType claimType) {
        this.claimType = claimType;
    }

    public OffsetDateTime getClaimedOn() {
        return claimedOn;
    }

    public void setClaimedOn(OffsetDateTime claimedOn) {
        this.claimedOn = claimedOn;
    }

    public Integer getYearPeriod(){
        if (getYear() != null && getClaimTypePeriod() != null) {
            return (getYear() * 100) + getClaimTypePeriod();
        }

        return null;
    }

    public Integer getClaimYearMonth() {
        if (getYear() != null && getClaimTypePeriod() != null) {
            int claimActualYear = GlaUtils.getActualYearFrom(getYear(), getClaimTypePeriod());
            int claimActualMonth = GlaUtils.getFirstMonthInQuarter(getClaimTypePeriod());
            return (claimActualYear * 100) + claimActualMonth;
        }
        return null;
    }

    public Claim clone(Integer newBlockId) {
        Claim clone = new Claim();
        clone.setOriginalId(this.getOriginalId());
        clone.setEntityId(this.getEntityId());
        clone.setBlockId(newBlockId);
        clone.setYear(this.getYear());
        clone.setAmount(this.getAmount());
        clone.setClaimStatus(this.getClaimStatus());
        clone.setClaimType(this.getClaimType());
        clone.setClaimTypePeriod(this.getClaimTypePeriod());
        clone.setClaimedOn(this.getClaimedOn());
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return Objects.equals(getOriginalId(), claim.getOriginalId()) &&
                Objects.equals(entityId, claim.entityId) &&
                Objects.equals(year, claim.year) &&
                Objects.equals(amount, claim.amount) &&
                claimStatus == claim.claimStatus &&
                claimType == claim.claimType &&
                Objects.equals(claimTypePeriod, claim.claimTypePeriod) &&
                Objects.equals(claimedOn, claim.claimedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOriginalId(), entityId, year, amount, claimStatus, claimType, claimTypePeriod, claimedOn);
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(getOriginalId());
    }

}
