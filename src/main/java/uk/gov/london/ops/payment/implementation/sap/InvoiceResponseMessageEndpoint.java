/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.sap;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.payment.*;
import uk.gov.london.ops.payment.implementation.sap.model.InvoiceDetails;
import uk.gov.london.ops.payment.implementation.sap.model.InvoiceResponse;

import java.io.File;
import java.io.IOException;

import static uk.gov.london.common.GlaUtils.getFileContent;

@MessageEndpoint
public class InvoiceResponseMessageEndpoint {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    PaymentService paymentService;

    @Autowired
    private SapDataService sapDataService;

    @Autowired
    Environment environment;

    @Value("${sap.ftp.delete.local.files}")
    private boolean deleteLocalFiles;

    @ServiceActivator(inputChannel = "invoiceResponsesSftpChannel")
    public String process(File file) throws Exception {

        if (!file.getName().toLowerCase().endsWith(".xml")) {
            log.warn("Ignoring non-XML invoice response file: " + file.getName());
            return null;
        }

        if (sapDataService.countByFileName(file.getName()) > 0) {
            log.debug("Ignoring previously processed file {} ", file.getName());
            return null;
        }

        log.debug("processing file {}", file.getName());

        String content = getFileContent(file);

        boolean processed = processFileContent(file.getName(), content);

        if (processed) {
            if (deleteLocalFiles && file.delete()) {
                log.debug("Successfully deleted file {}", file.getName());
            }
            else {
                log.warn("Failed to delete file {} after being processed!", file.getName());
            }
        }

        return null;
    }

    /**
     * @return true if the file content has been processed successfully
     */
    boolean processFileContent(String fileName, String content) throws IOException {
        if (content == null) {
            recordFileProcessingError(fileName, "Unable to read file contents", "{null}");
            return false;
        }

        InvoiceResponse invoiceResponse = toInvoiceResponse(content, fileName);

        if (invoiceResponse == null) {
            recordFileProcessingError(fileName, "Unable to parse XML file contents", content);
            return false;
        }

        int segment = 0;

        for (InvoiceDetails details: invoiceResponse.getDetails()) {
            try {
                processSegment(fileName, ++segment, details, content);
            } catch (Exception e) {
                recordSegmentProcessingError(fileName, segment, "Error processing invoice response entry: " + e.getMessage(), content);
            }
        }

        return true;
    }

    private void processSegment(String fileName, int segment, InvoiceDetails details, String content) {
        if (details.getSupplierInvoiceNumber().endsWith("P")) {
            recordIgnoredSegment(fileName, segment, "Ignoring IMS invoice response: " + details.getSupplierInvoiceNumber());
            return;
        }

        ProjectLedgerEntry payment = paymentService.getBySupplierInvoiceNumber(details.getSupplierInvoiceNumber());

        if (payment == null) {
            recordSegmentProcessingError(fileName, segment,
                    "No payment matching supplier invoice number " + details.getSupplierInvoiceNumber(), content);
            return;
        }

        LedgerStatus status = LedgerStatus.parseSapStatus(details.getInvoiceStatus());

        if (status == null) {
            recordSegmentProcessingError(fileName, segment,
                    "Unknown status " + details.getInvoiceStatus() + " for invoice " + details.getSupplierInvoiceNumber(), content);
            return;
        }

        paymentService.setStatus(payment, status, null);

        recordSegmentProcessed(fileName, segment, payment);
    }

    void recordFileProcessingError(String fileName, String error, String content) {
        recordSapData(fileName, null, true, error, content);
    }

    void recordSegmentProcessingError(String fileName, int segment, String error, String content) {
        recordSapData(fileName, segment, true, error, content);
    }

    void recordIgnoredSegment(String fileName, int segment, String error) {
        recordSapData(fileName, segment, true, error, "");
    }

    void recordSegmentProcessed(String fileName, int segment, ProjectLedgerEntry payment) {
        recordSapData(fileName, segment, true, null, "");
        log.debug("marked payment {} as acknowledged", payment.getId());
    }

    void recordSapData(String fileName, Integer segment, boolean processed, String error, String content) {
        SapData sap_data = new SapData();
        sap_data.setInterfaceType(SapData.TYPE_INV_RESP);
        sap_data.setFileName(fileName);
        sap_data.setProcessedOn(environment.now());
        sap_data.setCreatedOn(environment.now());
        sap_data.setContent(content);
        sap_data.setProcessed(processed);
        sap_data.setErrorDescription(error);

        if (segment != null) {
            sap_data.setSegmentNumber(segment);
        }

        try {
            sapDataService.save(sap_data);
        } catch (Exception e) {
            log.error("Error saving sap_data", e);
        }
    }

    InvoiceResponse toInvoiceResponse(String content, String fileName) throws IOException {
        try {
            XmlMapper mapper = new XmlMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(content, InvoiceResponse.class);
        } catch (Exception e) {
            log.error("Unable to parse invoice response file " + fileName, e);
            return null;
        }
    }


}
