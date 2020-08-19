/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

/**
 * Created by chris on 02/02/2017.
 */
@Entity(name = "v_project_funding_summary")
public class FundingYearSummary {

    @EmbeddedId
    private FundingYearSummaryID id;

    @Column(name = "project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project for this record")
    private Integer projectId;
    @Column(name = "REV_GRANT")
    private BigDecimal revenueGrant;
    @Column(name = "REV_MATCH")
    private BigDecimal revenueMatch;
    @Column(name = "CAP_GRANT")
    private BigDecimal capitalGrant;
    @Column(name = "CAP_MATCH")
    private BigDecimal capitalMatch;

    public FundingYearSummary() {
    }

    public FundingYearSummary(FundingYearSummaryID id, Integer projectId, BigDecimal revenueGrant, BigDecimal revenueMatch,
            BigDecimal capitalGrant, BigDecimal capitalMatch) {
        this.id = id;
        this.projectId = projectId;
        this.revenueGrant = revenueGrant;
        this.revenueMatch = revenueMatch;
        this.capitalGrant = capitalGrant;
        this.capitalMatch = capitalMatch;
    }

    public FundingYearSummaryID getId() {
        return id;
    }

    public void setId(FundingYearSummaryID id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public BigDecimal getRevenueGrant() {
        return revenueGrant;
    }

    public void setRevenueGrant(BigDecimal revenueGrant) {
        this.revenueGrant = revenueGrant;
    }

    public BigDecimal getRevenueMatch() {
        return revenueMatch;
    }

    public void setRevenueMatch(BigDecimal revenueMatch) {
        this.revenueMatch = revenueMatch;
    }

    public BigDecimal getCapitalGrant() {
        return capitalGrant;
    }

    public void setCapitalGrant(BigDecimal capitalGrant) {
        this.capitalGrant = capitalGrant;
    }

    public BigDecimal getCapitalMatch() {
        return capitalMatch;
    }

    public void setCapitalMatch(BigDecimal capitalMatch) {
        this.capitalMatch = capitalMatch;
    }

    public String getFinancialYearForDisplay() {
        return this.getId().getFinancialYear() + "/" + String.valueOf(this.getId().getFinancialYear() + 1).substring(2);
    }

    public boolean isYearValid() {
        return (revenueGrant == null || revenueGrant.compareTo(BigDecimal.ZERO) == 0)
                && (revenueMatch == null || revenueMatch.compareTo(BigDecimal.ZERO) == 0)
                && (capitalMatch == null || capitalMatch.compareTo(BigDecimal.ZERO) == 0)
                && (capitalGrant == null || capitalGrant.compareTo(BigDecimal.ZERO) == 0);
    }
}