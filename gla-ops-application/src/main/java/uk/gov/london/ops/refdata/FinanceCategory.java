/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.user.domain.User;

@Entity(name = "finance_category")
public class FinanceCategory implements OpsEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "finance_category_seq_gen")
    @SequenceGenerator(name = "finance_category_seq_gen", sequenceName = "finance_category_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "text")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "spend_status")
    private FinanceCategoryStatus spendStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_status")
    private FinanceCategoryStatus receiptStatus;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, targetEntity = CECode.class)
    @JoinColumn(name = "finance_category_id")
    private List<CECode> ceCodes = new ArrayList<>();

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @ManyToOne
    @JoinColumn(name = "modified_by")
    @JsonIgnore
    private User modifiedByUser;

    public FinanceCategory() {}

    public FinanceCategory(Integer id, String text, FinanceCategoryStatus spendStatus, FinanceCategoryStatus receiptStatus) {
        this.id = id;
        this.text = text;
        this.spendStatus = spendStatus;
        this.receiptStatus = receiptStatus;
    }

    public FinanceCategory(String text, FinanceCategoryStatus spendStatus, FinanceCategoryStatus receiptStatus) {
        this.text = text;
        this.spendStatus = spendStatus;
        this.receiptStatus = receiptStatus;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return null;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {

    }

    @Override
    public String getCreatedBy() {
        return null;
    }

    @Override
    public void setCreatedBy(String createdBy) {

    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public FinanceCategoryStatus getSpendStatus() {
        return spendStatus;
    }

    public void setSpendStatus(FinanceCategoryStatus spendStatus) {
        this.spendStatus = spendStatus;
    }

    public FinanceCategoryStatus getReceiptStatus() {
        return receiptStatus;
    }

    public void setReceiptStatus(FinanceCategoryStatus receiptStatus) {
        this.receiptStatus = receiptStatus;
    }

    public List<CECode> getCeCodes() {
        return ceCodes;
    }

    public void setCeCodes(List<CECode> ceCodes) {
        this.ceCodes = ceCodes;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public User getModifiedByUser() {
        return modifiedByUser;
    }

    public void setModifiedByUser(User modifiedByUser) {
        this.modifiedByUser = modifiedByUser;
    }

    @Override
    public String getModifiedBy() {
        return modifiedByUser == null ? null : modifiedByUser.getFullName();
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        // no-op
    }
}
