/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.finance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.project.ProjectLedgerEntry;
import uk.gov.london.ops.domain.refdata.CategoryValue;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 08/06/2017.
 */
@Entity(name = "PAYMENT_GROUP")
public class PaymentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_group_sequence_gen")
    @SequenceGenerator(name = "payment_group_sequence_gen", sequenceName = "payment_group_sequence", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "decline_comments")
    private String declineComments;

    @Column(name = "approval_requested_by")
    private String approvalRequestedBy;

    @JoinColumn(name="decline_reason")
    @OneToOne()
    private CategoryValue declineReason;

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
}
