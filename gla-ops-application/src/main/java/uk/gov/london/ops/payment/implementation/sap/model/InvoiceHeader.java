/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.sap.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class InvoiceHeader {

    @JacksonXmlProperty(localName = "ExecutionDate")
    private String executionDate;

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(String executionDate) {
        this.executionDate = executionDate;
    }

}
