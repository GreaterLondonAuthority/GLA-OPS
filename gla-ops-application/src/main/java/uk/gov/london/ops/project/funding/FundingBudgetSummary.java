/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import uk.gov.london.ops.payment.SpendType;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by chris on 02/02/2017.
 */
@Entity(name = "v_funding_budget_summary")
public class FundingBudgetSummary {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "block_id")
    private Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "spend_type")
    @Enumerated(EnumType.STRING)
    private SpendType spendType;

    @Column(name = "budget")
    private BigDecimal budget;

    @Column(name = "actual")
    private BigDecimal actualSpend;

    @Column(name = "forecast")
    private BigDecimal forecastSpend;

    @Column(name = "balance")
    private BigDecimal balance;

    public FundingBudgetSummary() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public SpendType getSpendType() {
        return spendType;
    }

    public void setSpendType(SpendType spendType) {
        this.spendType = spendType;
    }

    public BigDecimal getActualSpend() {
        return actualSpend;
    }

    public void setActualSpend(BigDecimal actualSpend) {
        this.actualSpend = actualSpend;
    }

    public BigDecimal getForecastSpend() {
        return forecastSpend;
    }

    public void setForecastSpend(BigDecimal forecastSpend) {
        this.forecastSpend = forecastSpend;
    }
}


