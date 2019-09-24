/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import uk.gov.london.ops.notification.NotificationTargetEntity;
import uk.gov.london.ops.refdata.CategoryValue;

/**
 * Created by chris on 08/06/2017.
 */
@Entity(name = "PAYMENT_GROUP")
public class PaymentGroup implements NotificationTargetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_group_sequence_gen")
    @SequenceGenerator(name = "payment_group_sequence_gen", sequenceName = "payment_group_sequence", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "decline_comments")
    private String declineComments;

    @Column(name = "approval_requested_by")
    private String approvalRequestedBy;

    @Column(name = "interest_assessed")
    private Boolean interestAssessed = false;

    @JoinColumn(name="decline_reason")
    @OneToOne()
    private CategoryValue declineReason;

    @Transient
    private boolean thresholdExceeded;

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
    private List<PaymentSummary> payments =  new ArrayList<>();

    public PaymentGroup() {
    }

    public PaymentGroup(List<ProjectLedgerEntry> ledgerEntries) {
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

    public Boolean getInterestAssessed() {
        return interestAssessed ;
    }

    public void setInterestAssessed(Boolean interestAssessed) {
        this.interestAssessed = interestAssessed;
    }

    @Override
    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }
}
