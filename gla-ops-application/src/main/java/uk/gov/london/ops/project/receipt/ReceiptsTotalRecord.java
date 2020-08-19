/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.receipt;

import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.payment.LedgerStatus;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by chris on 02/02/2017.
 */
@Entity(name = "v_receipt_totals")
public class ReceiptsTotalRecord {

    @Id
    private Integer id; // not really an ID (is rownumber) but is needed by springdata
    @Column(name = "project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project for this receipt")
    private Integer projectId;
    @JoinData(targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The block id for this record. These records will duplicated per block")
    @Column(name = "block_id")
    private Integer blockId;
    @Column(name = "total")
    private BigDecimal total;
    @Column(name = "financial_year")
    private Integer financialYear;
    @Column(name = "ledger_status")
    @Enumerated(EnumType.STRING)
    private LedgerStatus ledgerStatus;

    public ReceiptsTotalRecord() {
    }

    public Integer getId() {
        return id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public LedgerStatus getLedgerStatus() {
        return ledgerStatus;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }
}


