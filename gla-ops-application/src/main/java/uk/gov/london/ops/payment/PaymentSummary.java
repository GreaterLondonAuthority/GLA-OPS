/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryEntity;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.jpa.NonJoin;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.refdata.PaymentSource;
import uk.gov.london.ops.service.ManagedEntityInterface;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static uk.gov.london.ops.payment.LedgerType.PAYMENT;
import static uk.gov.london.ops.permission.PermissionType.ORG_VIEW_VENDOR_SAP_ID;

/**
 * The amount returned by this entity will be the negated value stored in the DB as the requirement is to display payments
 * are stored as negatives in the DB but needs to be displayed as postive and the other way around for reclaims.
 */
@Entity(name = "v_payment_summaries")
@QueryEntity
@JsonFilter("roleBasedFilter")
public class PaymentSummary implements ManagedEntityInterface {

    @Id
    private Integer id;

    @Column(name = "project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project for this payment")
    protected Integer projectId;

    @Column(name = "block_id")
    protected Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "quarter")
    private Integer quarter;

    @Column(name = "reclaim_of_payment_id")
    @JoinData(targetTable = "project_ledger_entry", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "If this is a reclaim then this references the payment this is a reclaim of. "
                + "Join is to project_ledger_entry but could also self join to this view.")
    protected Integer reclaimOfPaymentId;

    @Column(name = "interest_for_payment_id")
    @JoinData(targetTable = "project_ledger_entry", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "If this is an interest payment then this references the payment this is the interest is for. ")
    protected Integer interestForPaymentId;

    @Column(name = "organisation_id")
    @JoinData(targetTable = "organisation", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The organisation paid by this payment")
    private Integer organisationId;

    @Column(name = "project_title")
    private String projectName;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "prg_name")
    private String programmeName;

    @Column(name = "programme_id")
    private Integer programmeId;

    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "category")
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_source")
    private LedgerSource ledgerSource;

    @Column(name = "amount")
    // column called amount in DB due to reserved word value
    private BigDecimal value;

    @Column(name = "xml_file")
    private String xmlFile;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_status")
    private LedgerStatus ledgerStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type")
    private LedgerType ledgerType;

    @Column(name = "payment_source")
    private String paymentSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "spend_type")
    private SpendType spendType;

    @Column(name = "interest")
    private BigDecimal interest;

    @Column(name = "interestPayment")
    private Boolean interestPayment = Boolean.FALSE;

    @Column(name = "wbs_code")
    @NonJoin("Duplicates the WBS code from the organisation being paid")
    private String wbsCode;

    @Column(name = "ce_code")
    private String ceCode;

    @Column(name = "sap_vendor_id")
    @PermissionRequired({ORG_VIEW_VENDOR_SAP_ID})
    @NonJoin("Not a join")
    private String sapVendorId;

    @Column(name = "external_id")
    @NonJoin("If a milestone payment this will be the external ID of that milestone if present.")
    private Integer externalId;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private OrganisationEntity managingOrganisation;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @JsonIgnore
    @Column(name = "created_by")
    private String creator;

    @Transient
    private String creatorName;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @JsonIgnore
    @Column(name = "modified_by")
    private String modifiedBy;

    @Transient
    private String lastModifierName;

    @Column(name = "authorised_on")
    private OffsetDateTime authorisedOn;

    @Column(name = "authorisor_firstname")
    private String authorisorFirstName;

    @Column(name = "authorisor_lastname")
    private String authorisorLastName;

    @Column(name = "display_date")
    private OffsetDateTime displayDate;

    @Column(name = "company_name")
    private String companyName;

    @JsonIgnore
    @Column(name = "resent_by")
    private String resender;

    @Transient
    private String resenderName;

    @Column(name = "resent_on")
    private OffsetDateTime resentOn;

    @Column(name = "comments")
    private String comments;

    @Transient
    private PaymentSource paymentSourceDetails;

    public Integer getId() {
        return id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public LedgerStatus getLedgerStatus() {
        return ledgerStatus;
    }

    public LedgerType getLedgerType() {
        return ledgerType;
    }

    public String getPaymentSource() {
        return paymentSource;
    }

    public SpendType getSpendType() {
        return spendType;
    }

    public String getCategory() {
        return category;
    }

    /**
     * @return if not null will retrun the negated value stored in the DB as the requirement is to display payments are stored
     * as negatives in the DB but needs to be displayed as postive and the other way around for reclaims.
     */
    public BigDecimal getValue() {
        if (value != null) {
            return value.setScale(2, BigDecimal.ROUND_HALF_UP).negate();
        }
        return null;
    }

    public String getVendorName() {
        return vendorName;
    }

    public LedgerSource getLedgerSource() {
        return ledgerSource;
    }

    public BigDecimal getTotalIncludingInterest() {
        if (interest == null) {
            return getValue();
        } else if (value != null) {
            return getInterest().add(getValue());
        }
        return null;
    }

    public BigDecimal getInterest() {
        return interest == null ? null : interest.negate();
    }

    public OrganisationEntity getManagingOrganisation() {
        return managingOrganisation;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public String getCeCode() {
        return ceCode;
    }

    public void setCeCode(String ceCode) {
        this.ceCode = ceCode;
    }

    public OffsetDateTime getAuthorisedOn() {
        return authorisedOn;
    }

    public OffsetDateTime getDisplayDate() {
        return this.displayDate;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getSapVendorId() {
        return sapVendorId;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProgrammeName() {
        return programmeName;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public String getAuthorisorFirstName() {
        return authorisorFirstName;
    }

    public String getAuthorisorLastName() {
        return authorisorLastName;
    }

    public String getAuthorisor() {
        return authorisorFirstName + " "  + authorisorLastName;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatedBy() {
        return creator;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreator() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getLastModifierName() {
        return lastModifierName;
    }

    public void setLastModifierName(String lastModifierName) {
        this.lastModifierName = lastModifierName;
    }

    public Integer getReclaimOfPaymentId() {
        return reclaimOfPaymentId;
    }

    public boolean isReclaim() {
        return this.reclaimOfPaymentId != null;
    }

    public String getOpsInvoiceNumber() {
        return "P" + getProjectId() + "-" + getId();
    }

    public Integer getBlockId() {
        return blockId;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public boolean isResendable() {
        // authorised payments that aren't already cleared
        return PAYMENT.equals(ledgerType)
            && LedgerStatus.getApprovedPaymentStatuses().contains(ledgerStatus)
            && !LedgerStatus.Cleared.equals(ledgerStatus)
            && authorisedOn != null;
    }

    public OffsetDateTime getResentOn() {
        return resentOn;
    }

    public void setResentOn(OffsetDateTime resentOn) {
        this.resentOn = resentOn;
    }

    public String getResender() {
        return resender;
    }

    public void setResender(String resender) {
        this.resender = resender;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getResenderName() {
        return resenderName;
    }

    public void setResenderName(String resenderName) {
        this.resenderName = resenderName;
    }

    public Integer getInterestForPaymentId() {
        return interestForPaymentId;
    }

    public Boolean getInterestPayment() {
        return interestPayment;
    }

    public boolean isXmlFileAvailable() {
        return xmlFile != null;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public PaymentSource getPaymentSourceDetails() {
        return paymentSourceDetails;
    }

    public void setPaymentSourceDetails(PaymentSource paymentSourceDetails) {
        this.paymentSourceDetails = paymentSourceDetails;
    }

    public static PaymentSummary createFrom(ProjectLedgerEntry ple) {
        PaymentSummary ps = new PaymentSummary();
        ps.id = ple.getId();
        ps.projectId = ple.getProjectId();
        ps.category = ple.getCategory();
        ps.subCategory = ple.getSubCategory();
        ps.interest = ple.getInterest();
        ps.value = ple.getValue();
        ps.ledgerSource = ple.getLedgerSource();
        ps.ledgerStatus = ple.getLedgerStatus();
        ps.ledgerType = ple.getLedgerType();
        ps.projectName = ple.getProjectName();
        ps.programmeName = ple.getProgrammeName();
        ps.vendorName = ple.getVendorName();
        ps.paymentSource = ple.getPaymentSource();
        ps.reclaimOfPaymentId = ple.getReclaimOfPaymentId();
        ps.managingOrganisation = ple.getManagingOrganisation();
        ps.organisationId = ple.getOrganisationId();
        ps.sapVendorId = ple.getSapVendorId();
        ps.interestPayment = ple.isInterestPayment();
        ps.modifiedBy = ple.getModifiedBy();
        ps.creator = ple.getCreatedBy();
        return ps;
    }
}
