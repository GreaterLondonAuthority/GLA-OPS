/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import uk.gov.london.ops.payment.LedgerSource;

import java.math.BigDecimal;

public class SAPMetaData {

    private String payerName;

    private String date;

    private String categoryCode;

    private Integer categoryCodeId ;

    private String sapCategoryCode;

    private String invoiceNumber;

    private BigDecimal amount;

    private LedgerSource ledgerSource;

    private SpendType spendType;

    public String getPayerName() {
        return payerName;
    }

    public SAPMetaData(String payerName, String date, String categoryCode, Integer categoryCodeId, String sapCategoryCode, String invoiceNumber, BigDecimal amount,LedgerSource ledgerSource, SpendType spendType) {
        this.payerName = payerName;
        this.date = date;
        this.categoryCode = categoryCode;
        this.categoryCodeId = categoryCodeId;
        this.sapCategoryCode = sapCategoryCode;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.ledgerSource = ledgerSource;
        this.spendType = spendType;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getSapCategoryCode() {
        return sapCategoryCode;
    }

    public void setSapCategoryCode(String sapCategoryCode) {
        this.sapCategoryCode = sapCategoryCode;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getCategoryCodeId() {
        return categoryCodeId;
    }

    public void setCategoryCodeId(Integer categoryCodeId) {
        this.categoryCodeId = categoryCodeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LedgerSource getLedgerSource() {
        return ledgerSource;
    }

    public void setLedgerSource(LedgerSource ledgerSource) {
        this.ledgerSource = ledgerSource;
    }

    public SpendType getSpendType() { return spendType; }

    public void setSpendType(SpendType spendType) { this.spendType = spendType; }

}
