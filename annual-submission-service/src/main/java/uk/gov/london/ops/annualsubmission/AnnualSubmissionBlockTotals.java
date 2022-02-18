/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;

import javax.persistence.Transient;
import java.io.Serializable;

public class AnnualSubmissionBlockTotals implements Serializable {

    @Transient
    private final int financialYear;

    @Transient
    private Integer openingBalance;

    @Transient
    private Integer totalGenerated;

    @Transient
    private Integer totalSpent;

    @Transient
    private Integer closingBalance;

    public AnnualSubmissionBlockTotals(int financialYear, AnnualSubmissionBlockEntity block) {
        this.financialYear = financialYear;
        if (block != null) {
            this.openingBalance = block.getOpeningBalance();
            this.totalGenerated = block.getTotalGenerated();
            this.totalSpent = block.getTotalSpent();
            this.closingBalance = block.getClosingBalance();
        }
    }

    public int getFinancialYear() {
        return financialYear;
    }

    public Integer getOpeningBalance() {
        return openingBalance;
    }

    public Integer getTotalGenerated() {
        return totalGenerated;
    }

    public Integer getTotalSpent() {
        return totalSpent;
    }

    public Integer getClosingBalance() {
        return closingBalance;
    }

}
