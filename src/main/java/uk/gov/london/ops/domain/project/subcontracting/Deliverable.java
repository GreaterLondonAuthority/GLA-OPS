/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.subcontracting;

import java.math.BigDecimal;
import javax.persistence.*;

@Entity
public class Deliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deliverable_seq_gen")
    @SequenceGenerator(name = "deliverable_seq_gen", sequenceName = "deliverable_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "deliverable_type")
    @Enumerated(EnumType.STRING)
    private DeliverableType deliverableType;

    @Column(name = "deliverable_type_description")
    private String deliverableTypeDescription;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "fee")
    private BigDecimal fee;

    @Column(name = "comments")
    private String comments;

    @Transient
    private DeliverableFeeCalculation feeCalculation;

    public Deliverable() {}

    public Deliverable(BigDecimal value) {
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DeliverableType getDeliverableType() {
        return deliverableType;
    }

    public void setDeliverableType(DeliverableType deliverableType) {
        this.deliverableType = deliverableType;
    }

    public String getDeliverableTypeDescription() {
        return deliverableTypeDescription;
    }

    public void setDeliverableTypeDescription(String deliverableTypeDescription) {
        this.deliverableTypeDescription = deliverableTypeDescription;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public DeliverableFeeCalculation getFeeCalculation() {
        return feeCalculation;
    }

    public void setFeeCalculation(DeliverableFeeCalculation feeCalculation) {
        this.feeCalculation = feeCalculation;
    }

    public void merge(Deliverable updated) {
        this.setDeliverableType(updated.getDeliverableType());
        this.setDeliverableTypeDescription(updated.getDeliverableTypeDescription());
        this.setComments(updated.getComments());
        this.setQuantity(updated.getQuantity());
        this.setValue(updated.getValue());
        this.setFee(updated.getFee());
    }
}
