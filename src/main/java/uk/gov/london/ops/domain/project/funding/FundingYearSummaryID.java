/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.funding;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class FundingYearSummaryID implements Serializable {

    @Column(name = "block_id")
    private Integer blockId;

    @Column(name = "fin_year")
    private Integer financialYear;

    public FundingYearSummaryID() {
    }

    public FundingYearSummaryID(Integer blockId, Integer financialYear) {
        this.blockId = blockId;
        this.financialYear = financialYear;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }
}
