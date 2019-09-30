/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.skills;

import uk.gov.london.common.skills.SkillsGrantType;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name = "skills_funding_summary")
public class SkillsFundingSummaryEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "skills_funding_summary_seq_gen")
    @SequenceGenerator(name = "skills_funding_summary_seq_gen", sequenceName = "skills_funding_summary_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "ukprn")
    private Integer ukprn;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(name = "period")
    private Integer period;

    @Column(name = "actual_year")
    private Integer actualYear;

    @Column(name = "actualMonth")
    private Integer actualMonth;

    @Column(name = "funding_line")
    private String fundingLine;

    @Column(name = "source")
    private String source;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type")
    private SkillsGrantType grantType;

    @Column(name = "total_payment")
    private BigDecimal totalPayment;

    public SkillsFundingSummaryEntity() {}

    public SkillsFundingSummaryEntity(Integer ukprn, Integer academicYear, Integer period, Integer actualYear, Integer actualMonth, String fundingLine, String source, String category, SkillsGrantType grantType, BigDecimal totalPayment) {
        this.ukprn = ukprn;
        this.academicYear = academicYear;
        this.period = period;
        this.actualYear = actualYear;
        this.actualMonth = actualMonth;
        this.fundingLine = fundingLine;
        this.source = source;
        this.category = category;
        this.grantType = grantType;
        this.totalPayment = totalPayment;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getFundingLine() {
        return fundingLine;
    }

    public void setFundingLine(String fundingLine) {
        this.fundingLine = fundingLine;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getActualYear() {
        return actualYear;
    }

    public void setActualYear(Integer actualYear) {
        this.actualYear = actualYear;
    }

    public Integer getActualMonth() {
        return actualMonth;
    }

    public void setActualMonth(Integer actualMonth) {
        this.actualMonth = actualMonth;
    }
}
