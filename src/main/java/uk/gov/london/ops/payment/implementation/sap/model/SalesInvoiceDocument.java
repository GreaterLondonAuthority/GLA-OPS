/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.sap.model;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * Java representation of a SAP sales invoice XML document.
 */
@XmlRootElement(name="",namespace = "")
public class SalesInvoiceDocument {

    private static final long GRANT_TAX_RATE = 0;

    public static class InvoiceType {
        @XmlAttribute(name="Code")
        public String invoiceType = "";  // or "CRN" for credit note
    }

    public static class Currency {
        @XmlAttribute(name="Code")
        public String code = "";

        @XmlValue()
        public String value = "";
    }

    public static class InvoiceCurrency {
        @XmlElement(name="Currency")
        public Currency currency = new Currency();
    }

    public static class InvoiceHead {
        @XmlElement(name="InvoiceType")
        public InvoiceType invoiceType = new InvoiceType();

        @XmlElement(name="InvoiceCurrency")
        public InvoiceCurrency invoiceCurrency = new InvoiceCurrency();
    }

    public static class InvoiceLineReferences {
        @XmlElement(name="CostCentre")
        public String costCentre = "";

        @XmlElement(name="GeneralLedgerCode")
        public String generalLedgerCode = ";
    }

    public static class Price {
        @XmlElement(name="UnitPrice")
        public BigDecimal unitPrice;
    }

    public static class SupplierReferences {
        public String BuyersCodeForSupplier = "";
    }

    public static class Supplier {
        @XmlElement(name="SupplierReferences")
        public SupplierReferences references = new SupplierReferences();
    }

    public static class BuyerReferences {
        @XmlElement(name="SuppliersCodeForBuyer")
        public String suppliersCodeForBuyer;
    }

    public static class Buyer {
        @XmlElement(name="BuyerReferences")
        public BuyerReferences buyerReferences = new BuyerReferences();
    }

    public static class Country {
        @XmlAttribute(name="Code")
        public String code = "";
    }

    public static class Address {
        @XmlElement(name="AddressLine")
        public String addressLine = "Financial Services Centre";

        @XmlElement(name="Street")
        public String street = "";

        @XmlElement(name="City")
        public String city = "";

        @XmlElement(name="PostCode")
        public String postCode = "";

        @XmlElement(name="Country")
        public Country country = new Country();
    }

    public static class Contact {
        @XmlElement(name="Name")
        public String name = "";
    }

    public static class InvoiceTo {
        @XmlElement(name="Address")
        public Address address = new Address();

        @XmlElement(name="Contact")
        public Contact contact = new Contact();
    }

    public static class LineTax {
        @XmlElement(name="TaxRate")
        public BigDecimal taxRate = BigDecimal.valueOf(GRANT_TAX_RATE);

        @XmlElement(name="TaxValue")
        public BigDecimal taxValue;
    }

    public static class InvoiceLine {
        @XmlAttribute(name="Action")
        public String action = "Add";

        @XmlElement(name="LineNumber")
        public int lineNumber = 1;

        @XmlElement(name="InvoiceLineReferences")
        public InvoiceLineReferences references = new InvoiceLineReferences();

        @XmlElement(name="Price")
        public Price price = new Price();

        @XmlElement(name="LineTax")
        public LineTax lineTax = new LineTax();

        @XmlElement(name="NetLineTotal")
        public BigDecimal netLineTotal;

        @XmlElement(name="LineTotal")
        public BigDecimal lineTotal;
    }

    public static class InvoiceTotal {
        public int NumberOfLines = 1;
        public int NumberOfTaxRates = 1;
        public BigDecimal LineValueTotal = BigDecimal.ZERO;
        public BigDecimal TaxableTotal;
        public BigDecimal TaxTotal;
        public BigDecimal NetPaymentTotal;
        public BigDecimal GrossPaymentTotal;
    }

    public static class InvoiceReferences {
        public String SuppliersInvoiceNumber = "";
    }

    @XmlElement(name="InvoiceHead")
    public InvoiceHead head = new InvoiceHead();

    @XmlElement(name="InvoiceReferences")
    public InvoiceReferences invoiceReferences = new InvoiceReferences();

    @XmlElement(name="InvoiceDate")
    public String invoiceDate = "";

    @XmlElement(name="TaxPointDate")
    public String taxPointDate = "";

    @XmlElement(name="Supplier")
    public Supplier supplier = new Supplier();

    @XmlElement(name="Buyer")
    public Buyer buyer = new Buyer();

    @XmlElement(name="InvoiceTo")
    public InvoiceTo invoiceTo = new InvoiceTo();

    @XmlElement(name="Narrative")
    public String narrative;

    @XmlTransient
    public List<InvoiceLine> lines = new LinkedList<>();

    public void addLine(BigDecimal amount, String wbsCode, String generalLedgerCode) {
        InvoiceLine line = new InvoiceLine();
        line.lineNumber = lines.size()+1;
        line.price.unitPrice = amount;
        line.lineTax.taxValue = amount.multiply(line.lineTax.taxRate).divide(new BigDecimal(100));
        line.netLineTotal = amount;
        line.lineTotal = amount.add(line.lineTax.taxValue);
        line.references.costCentre = wbsCode;
        line.references.generalLedgerCode = generalLedgerCode;
        lines.add(line);
    }

    @XmlElement(name="InvoiceLine")
    public List<InvoiceLine> getLines() {
        return lines;
    }

    @XmlElement(name="InvoiceTotal")
    public InvoiceTotal getTotals() {
        InvoiceTotal total = new InvoiceTotal();
        total.NumberOfLines = 1;
        total.NumberOfTaxRates = 1;
        total.LineValueTotal = lines.get(0).lineTotal;
        total.TaxableTotal = lines.get(0).netLineTotal;
        total.TaxTotal = lines.get(0).lineTax.taxValue;
        total.GrossPaymentTotal = total.LineValueTotal;
        total.NetPaymentTotal = total.TaxableTotal;
        return total;
    }

}
