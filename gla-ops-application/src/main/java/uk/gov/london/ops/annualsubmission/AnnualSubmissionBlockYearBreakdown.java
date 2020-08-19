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
import java.util.List;

public class AnnualSubmissionBlockYearBreakdown implements Serializable {

    @Transient
    private int financialYear;

    @Transient
    private List<AnnualSubmissionEntry> entries;

    @Transient
    private Integer total;

    public AnnualSubmissionBlockYearBreakdown() {}

    public AnnualSubmissionBlockYearBreakdown(int financialYear, List<AnnualSubmissionEntry> entries, Integer total) {
        this.financialYear = financialYear;
        this.entries = entries;
        this.total = total;
    }

    public int getFinancialYear() {
        return financialYear;
    }

    public List<AnnualSubmissionEntry> getEntries() {
        return entries;
    }

    public Integer getTotal() {
        return total;
    }

}
