/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.sap.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class InvoiceDetails {

    @JacksonXmlProperty(localName = "SupplierInvoiceNumber")
    private String supplierInvoiceNumber;

    @JacksonXmlProperty(localName = "TfLSAPInvoiceNumber")
    private String sapInvoiceNumber;

    @JacksonXmlProperty(localName = "TfLSAPInvoiceStatus")
    private String invoiceStatus;

    public String getSupplierInvoiceNumber() {
        return supplierInvoiceNumber;
    }

    public void setSupplierInvoiceNumber(String supplierInvoiceNumber) {
        this.supplierInvoiceNumber = supplierInvoiceNumber;
    }

    public String getSapInvoiceNumber() {
        return sapInvoiceNumber;
    }

    public void setSapInvoiceNumber(String sapInvoiceNumber) {
        this.sapInvoiceNumber = sapInvoiceNumber;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }
}
