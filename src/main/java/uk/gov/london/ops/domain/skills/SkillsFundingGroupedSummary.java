/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.skills;

import uk.gov.london.common.skills.SkillsGrantType;

import java.math.BigDecimal;

public class SkillsFundingGroupedSummary {

    private Integer ukprn;

    private Integer academicYear;

    private Integer period;

    private SkillsGrantType grantType;

    private BigDecimal totalPayment;

    public SkillsFundingGroupedSummary() {}

    public SkillsFundingGroupedSummary(Integer ukprn, Integer academicYear, Integer period, SkillsGrantType grantType, BigDecimal totalPayment) {
        this.ukprn = ukprn;
        this.academicYear = academicYear;
        this.period = period;
        this.grantType = grantType;
        this.totalPayment = totalPayment;
    }

    public Integer getUkprn() {
        return ukprn;
    }

    public void setUkprn(Integer ukprn) {
        this.ukprn = ukprn;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public SkillsGrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(SkillsGrantType grantType) {
        this.grantType = grantType;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }
}

