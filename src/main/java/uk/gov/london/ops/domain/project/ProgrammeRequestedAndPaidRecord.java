/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

/**
 * Project summary for use by API.
 *
 * @author Chris
 */
public class ProgrammeRequestedAndPaidRecord {

    private RequestedAndPaidRecord strategicRecord;
    private RequestedAndPaidRecord nonStrategicRecord;
    private AssociatedProjectsRecord associatedProjectsRecord;
    private StrategicPartnershipUnitSummary strategicPartnershipUnitSummary;

    public ProgrammeRequestedAndPaidRecord() {
    }

    public ProgrammeRequestedAndPaidRecord(RequestedAndPaidRecord strategicRecord, RequestedAndPaidRecord nonStrategicRecord, AssociatedProjectsRecord associatedProjectsRecord, StrategicPartnershipUnitSummary strategicPartnershipUnitSummary) {
        this.strategicRecord = strategicRecord;
        this.nonStrategicRecord = nonStrategicRecord;
        this.associatedProjectsRecord = associatedProjectsRecord;
        this.strategicPartnershipUnitSummary = strategicPartnershipUnitSummary;
    }

    public RequestedAndPaidRecord getStrategicRecord() {
        return strategicRecord;
    }

    public void setStrategicRecord(RequestedAndPaidRecord strategicRecord) {
        this.strategicRecord = strategicRecord;
    }

    public RequestedAndPaidRecord getNonStrategicRecord() {
        return nonStrategicRecord;
    }

    public void setNonStrategicRecord(RequestedAndPaidRecord nonStrategicRecord) {
        this.nonStrategicRecord = nonStrategicRecord;
    }

    public AssociatedProjectsRecord getAssociatedProjectsRecord() {
        return associatedProjectsRecord;
    }

    public void setAssociatedProjectsRecord(AssociatedProjectsRecord associatedProjectsRecord) {
        this.associatedProjectsRecord = associatedProjectsRecord;
    }

    public StrategicPartnershipUnitSummary getStrategicPartnershipUnitSummary() {
        return strategicPartnershipUnitSummary;
    }

    public void setStrategicPartnershipUnitSummary(StrategicPartnershipUnitSummary strategicPartnershipUnitSummary) {
        this.strategicPartnershipUnitSummary = strategicPartnershipUnitSummary;
    }
}
