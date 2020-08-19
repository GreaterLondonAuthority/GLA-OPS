/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.project.claim.ClaimStatus;

@Entity(name = "funding_claim")
public class FundingClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_claim_seq_gen")
    @SequenceGenerator(name = "funding_claim_seq_gen", sequenceName = "funding_claim_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "original_id")
    private Integer originalId;

    @Column(name = "block_id")
    private Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "quarter")
    private Integer quarter;

    @Column(name = "REV_GRANT")
    private Integer revenueGrant;

    @Column(name = "CAP_GRANT")
    private Integer capitalGrant;

    @Column(name = "CLAIM_STATUS")
    @Enumerated(EnumType.STRING)
    private ClaimStatus claimStatus;

    public FundingClaim() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOriginalId() {
        return originalId;
    }

    public void setOriginalId(Integer originalId) {
        this.originalId = originalId;
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

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getRevenueGrant() {
        return revenueGrant;
    }

    public void setRevenueGrant(Integer revenueGrant) {
        this.revenueGrant = revenueGrant;
    }

    public Integer getCapitalGrant() {
        return capitalGrant;
    }

    public void setCapitalGrant(Integer capitalGrant) {
        this.capitalGrant = capitalGrant;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
    }
}

