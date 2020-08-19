/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "annual_submission_category")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AnnualSubmissionCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "annual_submission_category_seq_gen")
    @SequenceGenerator(name = "annual_submission_category_seq_gen", sequenceName = "annual_submission_category_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type")
    private AnnualSubmissionType type;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status_type")
    private AnnualSubmissionStatusType status;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "grant_type")
    private AnnualSubmissionGrantType grant;

    @Column(name = "comments_required")
    private boolean commentsRequired;

    @Column(name = "hidden")
    private boolean hidden;

    public AnnualSubmissionCategory() {}

    public AnnualSubmissionCategory(String name, AnnualSubmissionType type, AnnualSubmissionStatusType status, AnnualSubmissionGrantType grant, boolean commentsRequired) {
        this(name, type, status, grant, commentsRequired, false);
    }

    public AnnualSubmissionCategory(String name, AnnualSubmissionType type, AnnualSubmissionStatusType status, AnnualSubmissionGrantType grant, boolean commentsRequired, boolean hidden) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.grant = grant;
        this.commentsRequired = commentsRequired;
        this.hidden = hidden;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AnnualSubmissionType getType() {
        return type;
    }

    public void setType(AnnualSubmissionType type) {
        this.type = type;
    }

    public AnnualSubmissionStatusType getStatus() {
        return status;
    }

    public void setStatus(AnnualSubmissionStatusType status) {
        this.status = status;
    }

    public AnnualSubmissionGrantType getGrant() {
        return grant;
    }

    public void setGrant(AnnualSubmissionGrantType grant) {
        this.grant = grant;
    }

    public boolean isCommentsRequired() {
        return commentsRequired;
    }

    public void setCommentsRequired(boolean commentsRequired) {
        this.commentsRequired = commentsRequired;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
