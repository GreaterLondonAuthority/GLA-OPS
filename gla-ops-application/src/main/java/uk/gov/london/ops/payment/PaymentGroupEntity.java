/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.refdata.CategoryValue;
import uk.gov.london.ops.user.domain.UserOrgFinanceThreshold;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Created by chris on 08/06/2017.
 */
@Entity(name = "PAYMENT_GROUP")
@NamedNativeQuery(
        name="PaymentGroupEntity.findAllWithPayments",
        query = "SELECT ps.organisation_id,ps.managing_organisation_id,ps.ledger_status,ps.ledger_type, pg.ID AS payment_group_id, pg.DECLINE_COMMENTS , pg.DECLINE_REASON , pg.INTEREST_ASSESSED, " +
                "        pg.APPROVAL_REQUESTED_BY , pg.COMMENTS , pg.PAYMENTS_ONLY_APPROVAL,  ps.id as payment_id, ps.project_id, " +
                "        ps.PROJECT_NAME, ps.COMPANY_NAME, ps.PROGRAMME_NAME, ps.VENDOR_NAME , ps.category, ps.sub_category, " +
                "        sap.SAP_ID as org_sap_id, ps.sap_vendor_id, "   +
                "        ps.ledger_source, ps.interest_payment, ps.interest, ps.amount, ps.RECLAIM_OF_PAYMENT_ID, psource.grant_type, " +
                "        CASE WHEN ut.PENDING_THRESHOLD IS NOT NULL THEN NULL ELSE ut.APPROVED_THRESHOLD END AS approved_threshold," +
                "        ps.modified_by, ps.created_by  " +
                "FROM PAYMENT_GROUP pg" +
                "        INNER JOIN PAYMENT_GROUP_PAYMENT  pgp ON pgp.group_id = pg.ID " +
                "        INNER JOIN V_PAYMENT_SUMMARIES ps ON ps.ID = pgp.PAYMENT_ID " +
                "        LEFT JOIN sap_id sap ON sap.organisation_id = ps.organisation_id AND sap.is_default_sap_id = true" +
                "        LEFT JOIN payment_source psource ON psource.name = ps.PAYMENT_SOURCE " +
                "        LEFT JOIN USER_ORG_FINANCE_THRESHOLD ut ON ut.username=?1 AND ut.organisation_id = ps.organisation_id" +
                "        WHERE (true=?2 OR (ps.organisation_id IN ?3 OR ps.managing_organisation_id IN ?3)) " +
                "        AND ps.ledger_status IN (?4) " +
                "        ORDER BY CASE WHEN ps.LEDGER_STATUS NOT IN ('Authorised', 'Declined') THEN ps.created_on ELSE " +
                "        CASE WHEN ps.LEDGER_STATUS = 'Authorised' THEN ps.authorised_on ELSE ps.modified_on END" +
                "        END",
        resultSetMapping = "PaymentGroupPayment"
)

@SqlResultSetMapping(name="PaymentGroupPayment", classes = {
        @ConstructorResult(targetClass = PaymentGroupPayment.class,
        columns = {
                @ColumnResult(name = "ORGANISATION_ID", type = Integer.class),
                @ColumnResult(name = "MANAGING_ORGANISATION_ID", type = Integer.class),
                @ColumnResult(name = "LEDGER_STATUS", type = String.class),
                @ColumnResult(name = "LEDGER_TYPE", type = String.class),
                @ColumnResult(name = "PAYMENT_GROUP_ID", type = Integer.class),
                @ColumnResult(name = "DECLINE_COMMENTS", type = String.class),
                @ColumnResult(name = "DECLINE_REASON", type = String.class),
                @ColumnResult(name = "APPROVAL_REQUESTED_BY", type = String.class),
                @ColumnResult(name = "INTEREST_ASSESSED", type = Boolean.class),
                @ColumnResult(name = "COMMENTS", type = String.class),
                @ColumnResult(name = "PAYMENTS_ONLY_APPROVAL", type = Boolean.class),
                @ColumnResult(name = "PAYMENT_ID", type = Integer.class),
                @ColumnResult(name = "PROJECT_ID", type = Integer.class),
                @ColumnResult(name = "PROJECT_NAME", type = String.class),
                @ColumnResult(name = "COMPANY_NAME", type = String.class),
                @ColumnResult(name = "PROGRAMME_NAME", type = String.class),
                @ColumnResult(name = "VENDOR_NAME", type = String.class),
                @ColumnResult(name = "CATEGORY", type = String.class),
                @ColumnResult(name = "SUB_CATEGORY", type = String.class),
                @ColumnResult(name = "ORG_SAP_ID", type = String.class),
                @ColumnResult(name = "SAP_VENDOR_ID", type = String.class),
                @ColumnResult(name = "LEDGER_SOURCE", type = String.class),
                @ColumnResult(name = "INTEREST_PAYMENT", type = Boolean.class),
                @ColumnResult(name = "INTEREST", type = BigDecimal.class),
                @ColumnResult(name = "AMOUNT", type = BigDecimal.class),
                @ColumnResult(name = "RECLAIM_OF_PAYMENT_ID", type = Integer.class),
                @ColumnResult(name = "GRANT_TYPE", type = String.class),
                @ColumnResult(name = "APPROVED_THRESHOLD", type = Long.class),
                @ColumnResult(name = "MODIFIED_BY", type = String.class),
                @ColumnResult(name = "CREATED_BY", type = String.class)
        })}
)


public class PaymentGroupEntity implements PaymentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_group_sequence_gen")
    @SequenceGenerator(name = "payment_group_sequence_gen", sequenceName = "payment_group_sequence", initialValue = 10000,
        allocationSize = 1)
    private Integer id;

    @Column(name = "decline_comments")
    private String declineComments;

    @Column(name = "payments_only_approval")
    private Boolean paymentsOnlyApproval;

    @Column(name = "comments")
    private String comments;

    @Column(name = "approval_requested_by")
    private String approvalRequestedBy;

    @Column(name = "interest_assessed")
    private Boolean interestAssessed = false;

    @JoinColumn(name = "decline_reason")
    @OneToOne()
    private CategoryValue declineReason;

    @Transient
    private boolean thresholdExceeded;

    @Transient
    private boolean suspendPayments;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PAYMENT_GROUP_PAYMENT", 
            joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "payment_id", referencedColumnName = "id"))
    @OrderColumn(name = "sort_order")
    private List<ProjectLedgerEntry> ledgerEntries =  new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PAYMENT_GROUP_PAYMENT",
            joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "payment_id", referencedColumnName = "id"))
    @OrderColumn(name = "sort_order")
    private final List<PaymentSummary> payments =  new ArrayList<>();

    public PaymentGroupEntity() {
    }

    public PaymentGroupEntity(List<ProjectLedgerEntry> ledgerEntries) {
        this.ledgerEntries = ledgerEntries;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDeclineComments() {
        return declineComments;
    }

    public void setDeclineComments(String declineComments) {
        this.declineComments = declineComments;
    }

    public Boolean getPaymentsOnlyApproval() {
        return paymentsOnlyApproval;
    }

    public void setPaymentsOnlyApproval(Boolean paymentsOnlyApproval) {
        this.paymentsOnlyApproval = paymentsOnlyApproval;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public CategoryValue getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(CategoryValue declineReason) {
        this.declineReason = declineReason;
    }

    public List<ProjectLedgerEntry> getLedgerEntries() {
        return ledgerEntries;
    }

    public void setLedgerEntries(List<ProjectLedgerEntry> ledgerEntries) {
        this.ledgerEntries = ledgerEntries;
    }

    public List<PaymentSummary> getPayments() {
        return payments;
    }

    public String getApprovalRequestedBy() {
        return approvalRequestedBy;
    }

    public void setApprovalRequestedBy(String approvalRequestedBy) {
        this.approvalRequestedBy = approvalRequestedBy;
    }

    public boolean isThresholdExceeded() {
        return thresholdExceeded;
    }

    public void setThresholdExceeded(boolean thresholdExceeded) {
        this.thresholdExceeded = thresholdExceeded;
    }

    public boolean isSuspendPayments() {
        return suspendPayments;
    }

    public void setSuspendPayments(boolean suspendPayments) {
        this.suspendPayments = suspendPayments;
    }

    public boolean isOnlyReclaimPayments() {
        if (ledgerEntries != null) {
            for (ProjectLedgerEntry ledgerEntry : ledgerEntries) {
                if (!ledgerEntry.isReclaim()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Integer getManagingOrganisation() {
        if (ledgerEntries != null && ledgerEntries.size() > 0)  {
            return ledgerEntries.get(0).getManagingOrganisationId();
        }
        return null;
    }

    @JsonIgnore
    public Integer getProjectId() {
        if (ledgerEntries != null && ledgerEntries.size() > 0)  {
            return ledgerEntries.get(0).getProjectId();
        }
        return null;
    }

    public Boolean getInterestAssessed() {
        return interestAssessed;
    }

    public void setInterestAssessed(Boolean interestAssessed) {
        this.interestAssessed = interestAssessed;
    }

    @Override
    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }

    public void updateLedgerEntriesThresholdDataFrom(UserOrgFinanceThreshold orgThreshold) {
        if (ledgerEntries != null) {
            for (ProjectLedgerEntry ledgerEntry : ledgerEntries) {
                ledgerEntry.setThresholdOrganisation(orgThreshold.getId().getOrganisationId());
                ledgerEntry.setThresholdValue(orgThreshold.getApprovedThreshold());
            }
        }
    }

    public boolean hasReclaim() {
        return ledgerEntries != null && ledgerEntries.stream().anyMatch(ProjectLedgerEntry::isReclaim);
    }

    public Long getMaxRequestedPayment() {
        if (ledgerEntries == null ) return 0L;

        Optional<ProjectLedgerEntry> max = ledgerEntries.stream()
                .filter(ple -> !ple.isReclaim())
                .max(Comparator.comparing(ProjectLedgerEntry::getValue));

        return max.isPresent() ? max.get().getValue().negate().longValue() : 0L;

    }
}
