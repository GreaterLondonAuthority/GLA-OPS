/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * Created by cmatias on 24/01/2019.
 */
@Entity(name = "risk_adjusted_figures")
public class RiskAdjustedFigures {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "risk_adjusted_figures_seq_gen")
    @SequenceGenerator(name = "risk_adjusted_figures_seq_gen", sequenceName = "risk_adjusted_figures_seq",
            initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name="financial_year")
    private Integer financialYear;

    @Column(name="starts")
    private BigDecimal starts;

    @Column(name="completions")
    private BigDecimal completions;

    @Column(name="grant_spend")
    private BigDecimal grantSpend;

    public RiskAdjustedFigures() {
    }

    public RiskAdjustedFigures(Integer financialYear) {
        this.financialYear = financialYear;
    }

    public RiskAdjustedFigures(BigDecimal starts, BigDecimal completions, BigDecimal grantSpend, Integer financialYear) {
        this.starts = starts;
        this.completions = completions;
        this.grantSpend = grantSpend;
        this.financialYear = financialYear;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }

    public BigDecimal getStarts() {
        return starts;
    }

    public void setStarts(BigDecimal starts) {
        this.starts = starts;
    }

    public BigDecimal getCompletions() {
        return completions;
    }

    public void setCompletions(BigDecimal completions) {
        this.completions = completions;
    }

    public BigDecimal getGrantSpend() {
        return grantSpend;
    }

    public void setGrantSpend(BigDecimal grantSpend) {
        this.grantSpend = grantSpend;
    }

    @Override
    public RiskAdjustedFigures clone() {
        RiskAdjustedFigures clone = new RiskAdjustedFigures();

        clone.setFinancialYear(this.getFinancialYear());
        clone.setStarts(this.getStarts());
        clone.setCompletions(this.getCompletions());
        clone.setGrantSpend(this.getGrantSpend());

        return clone;
    }


}


