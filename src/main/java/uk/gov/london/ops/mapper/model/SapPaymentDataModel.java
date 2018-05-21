/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.mapper.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.math.BigDecimal;

public class SapPaymentDataModel implements SapDataModel {

    @JacksonXmlProperty(isAttribute = true)
    private String paymentReference;

    @JacksonXmlProperty(isAttribute = true)
    private String PCSProjectNumber;

    @JacksonXmlProperty(isAttribute = true)
    private String PCSPhaseNumber;

    @JacksonXmlProperty(isAttribute = true)
    private String payeeName;

    @JacksonXmlProperty(isAttribute = true)
    private String paymentDate;

    @JacksonXmlProperty(isAttribute = true)
    private BigDecimal paidAmount;

    @JacksonXmlProperty(isAttribute = true)
    private String accountCode;

    @JacksonXmlProperty(isAttribute = true)
    private String accountDescription;

    @JacksonXmlProperty(isAttribute = true)
    private String activityCode;

    @JacksonXmlProperty(isAttribute = true)
    private String activityDescription;

    @JacksonXmlProperty(isAttribute = true)
    private String paymentDescription;

    @JacksonXmlProperty(isAttribute = true)
    private String costCenterCode;

    @JacksonXmlProperty(isAttribute = true)
    private String orderNumber;

    @JacksonXmlProperty(isAttribute = true)
    private String WBSElement;

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPCSProjectNumber() {
        return PCSProjectNumber;
    }

    public void setPCSProjectNumber(String PCSProjectNumber) {
        this.PCSProjectNumber = PCSProjectNumber;
    }

    public String getPCSPhaseNumber() {
        return PCSPhaseNumber;
    }

    public void setPCSPhaseNumber(String PCSPhaseNumber) {
        this.PCSPhaseNumber = PCSPhaseNumber;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
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

    public String getPaymentDescription() {
        return paymentDescription;
    }

    public void setPaymentDescription(String paymentDescription) {
        this.paymentDescription = paymentDescription;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getWBSElement() {
        return WBSElement;
    }

    public void setWBSElement(String WBSElement) {
        this.WBSElement = WBSElement;
    }
}
