/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by chris on 02/02/2017.
 */
@Entity(name = "v_annual_spend_by_type")
public class AnnualSpendSummaryRecord {

    @Id
    private Integer id; // not really an ID (is rownumber) but is needed by springdata
    @Column(name="financial_year")
    private Integer financialYear;
    @Column(name="project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project for this record")
    private Integer projectId;
    @JoinData(targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The block id for this record. These records will be duplicated per block.")
    @Column(name="block_id")
    private Integer blockId;
    @Column(name="month")
    private Integer month;
    @Column(name="year")
    private Integer year;
    @Column(name="spend")
    private BigDecimal spend;
    @Column(name="ledger_status")
    @Enumerated(EnumType.STRING)
    private LedgerStatus ledgerStatus;
    @Column(name="spend_type")
    @Enumerated(EnumType.STRING)
    private SpendType spendType;

    public AnnualSpendSummaryRecord() {
    }

    public AnnualSpendSummaryRecord(Integer financialYear, Integer projectId, Integer blockId, BigDecimal spend, LedgerStatus ledgerStatus, SpendType spendType, Integer year, Integer month) {
        this.financialYear = financialYear;
        this.projectId = projectId;
        this.blockId = blockId;
        this.spend = spend;
        this.ledgerStatus = ledgerStatus;
        this.spendType = spendType;
        this.year = year;
        this.month = month;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public BigDecimal getSpend() {
        return spend;
    }

    public LedgerStatus getLedgerStatus() {
        return ledgerStatus;
    }

    public SpendType getSpendType() {
        return spendType;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getYear() {
        return year;
    }
}


