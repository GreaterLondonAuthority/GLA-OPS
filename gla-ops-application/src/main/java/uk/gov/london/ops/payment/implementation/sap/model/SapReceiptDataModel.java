/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.sap.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class SapReceiptDataModel implements SapDataModel {

    @JacksonXmlProperty(isAttribute = true)
    private String PCSProjectNumber;

    @JacksonXmlProperty(isAttribute = true)
    private String payerName;

    @JacksonXmlProperty(isAttribute = true)
    private String receiptDate;

    @JacksonXmlProperty(isAttribute = true)
    private String ReceiptPostingDate;

    @JacksonXmlProperty(isAttribute = true)
    private String receiptReference;

    @JacksonXmlProperty(isAttribute = true)
    private BigDecimal receiptAmount;

    @JacksonXmlProperty(isAttribute = true)
    private String costCenterCode;

    @JacksonXmlProperty(isAttribute = true)
    private String accountCode;

    @JacksonXmlProperty(isAttribute = true)
    private String accountDescription;

    @JacksonXmlProperty(isAttribute = true)
    private String activityCode;

    @JacksonXmlProperty(isAttribute = true)
    private String activityDescription;

    @JacksonXmlProperty(isAttribute = true)
    private String invoiceDate;

    @JacksonXmlProperty(isAttribute = true)
    private String invoiceNumber;

    @JacksonXmlProperty(isAttribute = true)
    private String WBSElement;

    public String getPCSProjectNumber() {
        return PCSProjectNumber;
    }

    public void setPCSProjectNumber(String PCSProjectNumber) {
        this.PCSProjectNumber = PCSProjectNumber;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getReceiptPostingDate() {
        return ReceiptPostingDate;
    }

    public void setReceiptPostingDate(String receiptPostingDate) {
        ReceiptPostingDate = receiptPostingDate;
    }

    public String getReceiptReference() {
        return receiptReference;
    }

    public void setReceiptReference(String receiptReference) {
        this.receiptReference = receiptReference;
    }

    public BigDecimal getReceiptAmount() {
        return receiptAmount;
    }

    public void setReceiptAmount(BigDecimal receiptAmount) {
        this.receiptAmount = receiptAmount;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountDescription() {
        return accountDescription;
    }

    public void setAccountDescription(String accountDescription) {
        this.accountDescription = accountDescription;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getWBSElement() {
        return WBSElement;
    }

    public void setWBSElement(String WBSElement) {
        this.WBSElement = WBSElement;
    }

    @Override
    public String getDate() {
        return StringUtils.defaultIfBlank(ReceiptPostingDate, receiptDate);
    }

}
