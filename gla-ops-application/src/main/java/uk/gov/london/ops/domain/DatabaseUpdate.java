/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain;


import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Represents a database update made by support.
 *
 * @author Rob Bettison
 */
@Entity(name = "database_updates")
public class DatabaseUpdate {

    public enum Status {
        Rejected, Approved, AwaitingApproval, Complete, Failed
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "database_update_seq_gen")
    @SequenceGenerator(name = "database_update_seq_gen", sequenceName = "database_update_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "sql", nullable = false)
    private String sql;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "approved_on")
    private OffsetDateTime approvedOn;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "ppd_tested")
    private boolean ppd;

    @Column(name = "rows_affected")
    private Integer rowsAffected;
    
    @Column(name = "summary")
    private String summary;   
    @Column(name = "tracking_id")
    private String trackingId;

    public DatabaseUpdate() {}

    public DatabaseUpdate(String sql, String createdBy, OffsetDateTime createdOn, boolean ppd, String summary, String trackingId) {
        this.sql = sql;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.status = Status.AwaitingApproval;
        this.ppd = ppd;
        this.summary = summary;
        this.trackingId = trackingId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public OffsetDateTime getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(OffsetDateTime approvedOn) {
        this.approvedOn = approvedOn;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isPpd() {
        return ppd;
    }

    public void setPpd(boolean ppd) {
        this.ppd = ppd;
    }

    public void setRowsAffected(Integer rowsAffected) {
        this.rowsAffected = rowsAffected;
    }

    public Integer getRowsAffected() {
        return rowsAffected;
    }

    public boolean hasStatus(Status status) {
        return (getStatus() != null) && getStatus().equals(status);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }
    
}
