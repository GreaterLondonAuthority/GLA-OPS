/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import uk.gov.london.ops.framework.ComparableItem;

import javax.persistence.*;
import java.math.BigDecimal;

import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;

@Entity(name = "v_budget_summaries")
public class BudgetSummary implements ComparableItem {

    @Id
    private Integer id;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "block_id")
    private Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type")
    private LedgerType ledgerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "spend_type")
    private SpendType spendType;

    @Column(name = "category")
    private String category;

    @Column(name = "amount")
    // column called amount in DB due to reserved word value
    private BigDecimal value;

    public BudgetSummary() {}

    public BudgetSummary(SpendType spendType, BigDecimal value) {
        this.spendType = spendType;
        this.value = value;
    }

    public BudgetSummary(SpendType spendType, String category, BigDecimal value) {
        this(spendType, value);
        this.category = category;
    }

    public BudgetSummary(Integer year, SpendType spendType, BigDecimal value) {
        this(spendType, value);
        this.year = year;
    }

    public BudgetSummary(Integer year, SpendType spendType, String category, BigDecimal value) {
        this(spendType, category, value);
        this.year = year;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
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

    public LedgerType getLedgerType() {
        return ledgerType;
    }

    public void setLedgerType(LedgerType ledgerType) {
        this.ledgerType = ledgerType;
    }

    public SpendType getSpendType() {
        return spendType;
    }

    public void setSpendType(SpendType spendType) {
        this.spendType = spendType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String getComparisonId() {
        return projectId + ":" + blockId + ":" + year + ":" + spendType + ":" + category;
    }

    public boolean isMatchFund() {
        return MATCH_FUND_CATEGORY.equals(this.category);
    }

}
