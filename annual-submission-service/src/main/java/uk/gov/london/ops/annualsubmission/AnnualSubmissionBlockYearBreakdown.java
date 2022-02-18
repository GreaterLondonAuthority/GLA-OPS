/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Transient;

public class AnnualSubmissionBlockYearBreakdown implements Serializable {

    @Transient
    private int financialYear;

    @Transient
    private List<AnnualSubmissionEntryEntity> entries;

    @Transient
    private Integer total;

    public AnnualSubmissionBlockYearBreakdown() {
    }

    public AnnualSubmissionBlockYearBreakdown(int financialYear, List<AnnualSubmissionEntryEntity> entries, Integer total) {
        this.financialYear = financialYear;
        this.entries = entries;
        this.total = total;
    }

    public int getFinancialYear() {
        return financialYear;
    }

    public List<AnnualSubmissionEntryEntity> getEntries() {
        return entries;
    }

    public Integer getTotal() {
        return total;
    }

}
