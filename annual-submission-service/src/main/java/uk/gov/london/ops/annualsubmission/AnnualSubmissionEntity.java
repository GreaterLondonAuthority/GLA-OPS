/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.london.common.error.ApiErrorItem;
import uk.gov.london.ops.notification.NotificationTargetEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.london.ops.annualsubmission.AnnualSubmissionGrantType.DPF;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionGrantType.RCGF;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionStatusType.Actual;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionStatusType.Forecast;

@Entity(name = "annual_submission")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AnnualSubmissionEntity implements Serializable, NotificationTargetEntity {

    @Transient
    Logger log = LoggerFactory.getLogger(getClass());

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "annual_submission_seq_gen")
    @SequenceGenerator(name = "annual_submission_seq_gen", sequenceName = "annual_submission_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @NotNull
    @Column(name = "organisation_id")
    private Integer organisationId;

    @NotNull
    @Column(name = "financial_year")
    private Integer financialYear;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private AnnualSubmissionStatus status = AnnualSubmissionStatus.Draft;

    @Column(name = "submission_comments")
    private String submissionComments;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_on")
    private LocalDate approvedOn;

    @Column(name = "approval_comments")
    private String approvalComments;

    @Column(name = "rcgf_rollover")
    private Long rcgfRollover;

    @Column(name = "dpf_rollover")
    private Long dpfRollover;

    @Column(name = "rcgf_rollover_interest")
    private Long rcgfRolloverInterest;

    @Column(name = "dpf_rollover_interest")
    private Long dpfRolloverInterest;

    @Column(name = "rcgf_withdrawal")
    private Long rcgfWithdrawal;

    @Column(name = "dpf_withdrawal")
    private Long dpfWithdrawal;

    @Column(name = "rcgf_withdrawal_interest")
    private Long rcgfWithdrawalInterest;

    @Column(name = "dpf_withdrawal_interest")
    private Long dpfWithdrawalInterest;

    // *************************************************
    // GLA Certification
    // *************************************************
    @Column(name = "authorised_by")
    private String authorisedBy;

    @Column(name = "authorised_by_job_title")
    private String authorisedByJobTitle;

    @Column(name = "agreement_text")
    private String agreementText;

    @Column(name = "authorised_on")
    private LocalDate authorisedOn;

    @Column(name = "submitted_on")
    private OffsetDateTime submittedOn;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "agreement_signed")
    private boolean agreementSigned;
    // *************************************************

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true,
            targetEntity = AnnualSubmissionBlockEntity.class)
    @JoinColumn(name = "annual_submission_id")
    private final List<AnnualSubmissionBlockEntity> blocks = new ArrayList<>();

    @Transient
    private Set<AnnualSubmissionTransition> allowedTransitions = new HashSet<>();

    @Transient
    private List<ApiErrorItem> messages = new ArrayList<>();

    @Transient
    private String submittedByFullName;


    public AnnualSubmissionEntity() {}

    public AnnualSubmissionEntity(Integer organisationId, Integer financialYear) {
        this.organisationId = organisationId;
        this.financialYear = financialYear;
    }

    public AnnualSubmissionEntity(Integer organisationId, Integer financialYear, AnnualSubmissionStatus status) {
        this(organisationId, financialYear);
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }

    public AnnualSubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(AnnualSubmissionStatus status) {
        this.status = status;
    }

    public String getSubmissionComments() {
        return submissionComments;
    }

    public void setSubmissionComments(String submissionComments) {
        this.submissionComments = submissionComments;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDate getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(LocalDate approvedOn) {
        this.approvedOn = approvedOn;
    }

    public String getApprovalComments() {
        return approvalComments;
    }

    public void setApprovalComments(String approvalComments) {
        this.approvalComments = approvalComments;
    }

    public Long getRcgfRollover() {
        return rcgfRollover;
    }

    public void setRcgfRollover(Long rcgfRollover) {
        this.rcgfRollover = rcgfRollover;
    }

    public Long getDpfRollover() {
        return dpfRollover;
    }

    public void setDpfRollover(Long dpfRollover) {
        this.dpfRollover = dpfRollover;
    }

    public Long getRcgfRolloverInterest() {
        return rcgfRolloverInterest;
    }

    public void setRcgfRolloverInterest(Long rcgfRolloverInterest) {
        this.rcgfRolloverInterest = rcgfRolloverInterest;
    }

    public Long getDpfRolloverInterest() {
        return dpfRolloverInterest;
    }

    public void setDpfRolloverInterest(Long dpfRolloverInterest) {
        this.dpfRolloverInterest = dpfRolloverInterest;
    }

    public Long getRcgfWithdrawal() {
        return rcgfWithdrawal;
    }

    public void setRcgfWithdrawal(Long rcgfWithdrawal) {
        this.rcgfWithdrawal = rcgfWithdrawal;
    }

    public Long getDpfWithdrawal() {
        return dpfWithdrawal;
    }

    public void setDpfWithdrawal(Long dpfWithdrawal) {
        this.dpfWithdrawal = dpfWithdrawal;
    }

    public Long getRcgfWithdrawalInterest() {
        return rcgfWithdrawalInterest;
    }

    public void setRcgfWithdrawalInterest(Long rcgfWithdrawalInterest) {
        this.rcgfWithdrawalInterest = rcgfWithdrawalInterest;
    }

    public Long getDpfWithdrawalInterest() {
        return dpfWithdrawalInterest;
    }

    public void setDpfWithdrawalInterest(Long dpfWithdrawalInterest) {
        this.dpfWithdrawalInterest = dpfWithdrawalInterest;
    }

    public List<AnnualSubmissionBlockEntity> getBlocks() {
        return blocks;
    }

    public Set<AnnualSubmissionTransition> getAllowedTransitions() {
        return allowedTransitions;
    }

    public void setAllowedTransitions(Set<AnnualSubmissionTransition> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public List<ApiErrorItem> getMessages() {
        return messages;
    }

    public void setMessages(List<ApiErrorItem> messages) {
        this.messages = messages;
    }

    public AnnualSubmissionBlockEntity getAnnualRcgf() {
        return getBlockByType(Actual, RCGF);
    }

    public AnnualSubmissionBlockEntity getForecastRcgf() {
        return getBlockByType(Forecast, RCGF);
    }

    public AnnualSubmissionBlockEntity getAnnualDpf() {
        return getBlockByType(Actual, DPF);
    }

    public AnnualSubmissionBlockEntity getForecastDpf() {
        return getBlockByType(Forecast, DPF);
    }

    public AnnualSubmissionBlockEntity getBlockByType(AnnualSubmissionStatusType statusType, AnnualSubmissionGrantType grantType) {
        return blocks.stream()
                .filter(b -> statusType.equals(b.getStatusType()) && grantType.equals(b.getGrantType()))
                .findFirst()
                .orElse(null);
    }

    public AnnualSubmissionBlockEntity getBlockById(Integer blockId) {
        return blocks.stream().filter(b -> blockId.equals(b.getId())).findFirst().orElse(null);
    }

    public String getAuthorisedByJobTitle() {
        return authorisedByJobTitle;
    }

    public void setAuthorisedByJobTitle(String authorisedByJobTitle) {
        this.authorisedByJobTitle = authorisedByJobTitle;
    }

    public OffsetDateTime getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(OffsetDateTime submittedOn) {
        this.submittedOn = submittedOn;
    }

    public LocalDate getAuthorisedOn() {
        return authorisedOn;
    }

    public void setAuthorisedOn(LocalDate authorisedOn) {
        this.authorisedOn = authorisedOn;
    }

    public String getAuthorisedBy() {
        return authorisedBy;
    }

    public void setAuthorisedBy(String authorisedBy) {
        this.authorisedBy = authorisedBy;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getAgreementText() {
        return agreementText;
    }

    public void setAgreementText(String agreementText) {
        this.agreementText = agreementText;
    }

    public boolean isAgreementSigned() {
        return agreementSigned;
    }

    public void setAgreementSigned(boolean agreementSigned) {
        this.agreementSigned = agreementSigned;
    }

    public void setSubmittedByFullName(String submittedByFullName) {
        this.submittedByFullName = submittedByFullName;
    }

    public String getSubmittedByFullName() {
        return submittedByFullName;
    }

    public boolean anyBlocksLocked() {
        for (AnnualSubmissionBlockEntity block: blocks) {
            if (block.isLocked()) {
                return true;
            }
        }
        return false;
    }

    public boolean isCertificationAgreementComplete() {
        return authorisedBy != null
                && authorisedByJobTitle != null
                && authorisedOn != null
                && agreementSigned;
    }

    public boolean isComplete() {
        for (AnnualSubmissionBlockEntity block: blocks) {
            if (!block.isComplete()) {
                return false;
            }
        }
        return true;
    }

    public void merge(AnnualSubmissionEntity updated) {
        this.setSubmissionComments(updated.getSubmissionComments());
        this.setApprovalComments(updated.getApprovalComments());
        this.setApprovedBy(updated.getApprovedBy());
        this.setApprovedOn(updated.getApprovedOn());
        this.setRcgfRollover(updated.getRcgfRollover());
        this.setDpfRollover(updated.getDpfRollover());
        this.setRcgfRolloverInterest(updated.getRcgfRolloverInterest());
        this.setDpfRolloverInterest(updated.getDpfRolloverInterest());
        this.setRcgfWithdrawal(updated.getRcgfWithdrawal());
        this.setDpfWithdrawal(updated.getDpfWithdrawal());
        this.setRcgfWithdrawalInterest(updated.getRcgfWithdrawalInterest());
        this.setDpfWithdrawalInterest(updated.getDpfWithdrawalInterest());
        this.setAuthorisedBy(updated.getAuthorisedBy());
        this.setAuthorisedByJobTitle(updated.getAuthorisedByJobTitle());
        if (this.getAuthorisedOn() == null) {
            this.setAuthorisedOn(updated.getAuthorisedOn());
        } else if (updated.getAuthorisedOn() == null) {
            log.warn("Attempted to overwrite authorised on date with null value for block: " + this.getId());
        }
        this.setSubmittedOn(updated.getSubmittedOn());
        this.setSubmittedBy(updated.getSubmittedBy());
        this.setAgreementSigned(updated.isAgreementSigned());
    }

    @Override
    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }
}
