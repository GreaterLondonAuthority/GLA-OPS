/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import uk.gov.london.ops.domain.user.User;

@Entity
public class DeliveryOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "override_seq_gen")
    @SequenceGenerator(name = "override_seq_gen", sequenceName = "override_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @NotNull
    @Column(name = "project_id")
    private Integer projectId;

    @NotNull
    @Column(name = "override_reason")
    private String overrideReason;

    @NotNull
    @Column(name = "override_type")
    private String overrideType;

    @Column(name = "tenure")
    private String tenure;

    @Column(name = "reported_value")
    private Integer reportedValue;

    @Column(name = "reported_date")
    private OffsetDateTime reportedDate;

    @Column(name = "comments")
    private String comments;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "overridden_by")
    private User overriddenBy;

    @Column(name = "overridden_on")
    private OffsetDateTime overriddenOn;

    public DeliveryOverride() {
    }

    public DeliveryOverride(Integer projectId, String overrideReason, String overrideType,
        User overriddenBy, OffsetDateTime overriddenOn) {
        this.projectId = projectId;
        this.overrideReason = overrideReason;
        this.overrideType = overrideType;
        this.overriddenBy = overriddenBy;
        this.overriddenOn = overriddenOn;
    }

    public DeliveryOverride(Integer projectId, String overrideReason, String overrideType,
        String tenure, Integer reportedValue, OffsetDateTime reportedDate, String comments,
        User overriddenBy, OffsetDateTime overriddenOn) {
        this.projectId = projectId;
        this.overrideReason = overrideReason;
        this.overrideType = overrideType;
        this.tenure = tenure;
        this.reportedValue = reportedValue;
        this.reportedDate = reportedDate;
        this.comments = comments;
        this.overriddenBy = overriddenBy;
        this.overriddenOn = overriddenOn;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }

    public String getOverrideType() {
        return overrideType;
    }

    public void setOverrideType(String overrideType) {
        this.overrideType = overrideType;
    }

    public String getTenure() {
        return tenure;
    }

    public void setTenure(String tenure) {
        this.tenure = tenure;
    }

    public Integer getReportedValue() {
        return reportedValue;
    }

    public void setReportedValue(Integer reportedValue) {
        this.reportedValue = reportedValue;
    }

    public OffsetDateTime getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(OffsetDateTime reportedDate) {
        this.reportedDate = reportedDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @JsonIgnore
    public User getOverriddenBy() {
        return overriddenBy;
    }

    @JsonProperty(value = "overriddenBy", access = JsonProperty.Access.READ_ONLY)
    public String getOverriddenByUsername() {
        return overriddenBy.getFirstName() + " " + overriddenBy.getLastName();
    }

    public void setOverriddenBy(User overriddenBy) {
        this.overriddenBy = overriddenBy;
    }

    public OffsetDateTime getOverriddenOn() {
        return overriddenOn;
    }

    public void setOverriddenOn(OffsetDateTime overriddenOn) {
        this.overriddenOn = overriddenOn;
    }
}
