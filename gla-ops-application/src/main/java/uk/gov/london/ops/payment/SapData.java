/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import uk.gov.london.ops.payment.implementation.sap.model.SapDataModel;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
public class SapData {

    public enum Action {
        IGNORED
    }

    public static final String TYPE_PAYMENT  = "Payments";
    public static final String TYPE_RECEIPT  = "Receipts";
    public static final String TYPE_ORDER    = "Order";
    public static final String TYPE_INV_RESP = "InvoiceResponse";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sap_data_seq_gen")
    @SequenceGenerator(name = "sap_data_seq_gen", sequenceName = "sap_data_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "interface_type")
    private String interfaceType;

    @Column(name = "segment_number")
    private Integer segmentNumber;

    @Column(name = "content")
    private String content;

    @Column(name = "processed")
    private boolean processed;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "processed_on")
    private OffsetDateTime processedOn;

    @Column(name = "error_description")
    private String errorDescription;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private Action action;

    @Column(name = "actioned_by")
    private String actionedByUsername;

    @Transient
    private SapDataModel model;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public OffsetDateTime getProcessedOn() {
        return processedOn;
    }

    public void setProcessedOn(OffsetDateTime processedOn) {
        this.processedOn = processedOn;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public Integer getSegmentNumber() {
        return segmentNumber;
    }

    public void setSegmentNumber(Integer segmentNumber) {
        this.segmentNumber = segmentNumber;
    }

    public boolean isPayment() {
        return TYPE_PAYMENT.equals(interfaceType);
    }

    public boolean isReceipt() {
        return TYPE_RECEIPT.equals(interfaceType);
    }

    public SapDataModel getModel() {
        return model;
    }

    public void setModel(SapDataModel model) {
        this.model = model;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getActionedByUsername() {
        return actionedByUsername;
    }

    public void setActionedByUsername(String actionedByUsername) {
        this.actionedByUsername = actionedByUsername;
    }
}
