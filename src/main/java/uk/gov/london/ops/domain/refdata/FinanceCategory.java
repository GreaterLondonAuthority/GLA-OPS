/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.refdata;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "finance_category")
public class FinanceCategory {

    @Id
    @Column(name = "id")
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

    public FinanceCategory() {}

    public FinanceCategory(Integer id, String text, FinanceCategoryStatus spendStatus, FinanceCategoryStatus receiptStatus) {
        this.id = id;
        this.text = text;
        this.spendStatus = spendStatus;
        this.receiptStatus = receiptStatus;
    }

    public Integer getId() {
        return id;
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

}
