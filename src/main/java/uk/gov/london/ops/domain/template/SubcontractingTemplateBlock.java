/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.project.subcontracting.DeliverableType;
import uk.gov.london.ops.domain.project.subcontracting.SubcontractorType;

@Entity
@DiscriminatorValue("SUBCONTRACTING")
public class SubcontractingTemplateBlock extends TemplateBlock {

    @Column(name = "has_subcontractors_title")
    private String hasSubcontractorsTitle;

    @Column(name = "question2")
    private String question2;

    @Column(name = "question3")
    private String question3;

    @Column(name = "question4")
    private String question4;

    @Column(name = "question5")
    private String question5;

    @Enumerated(EnumType.STRING)
    @Column(name= "subcontractor_type")
    private SubcontractorType subcontractorType;

    @Column(name = "retention_fee_threshold")
    private BigDecimal retentionFeeThreshold;

    @ElementCollection(targetClass = DeliverableType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "template_deliverable_types", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "deliverable_type")
    @Enumerated(EnumType.STRING)
    private Set<DeliverableType> availableDeliverableTypes = new HashSet<>();

    @Column(name = "deliverable_name")
    private String deliverableName;

    @Column(name = "quantity_name")
    private String quantityName;

    @Column(name = "value_name")
    private String valueName;

    @Column(name = "feeName")
    private String feeName;

    public SubcontractingTemplateBlock() {
        super(ProjectBlockType.Subcontracting);
    }

    public String getHasSubcontractorsTitle() {
        return hasSubcontractorsTitle;
    }

    public void setHasSubcontractorsTitle(String hasSubcontractorsTitle) {
        this.hasSubcontractorsTitle = hasSubcontractorsTitle;
    }

    public String getQuestion2() {
        return question2;
    }

    public void setQuestion2(String question2) {
        this.question2 = question2;
    }

    public String getQuestion3() {
        return question3;
    }

    public void setQuestion3(String question3) {
        this.question3 = question3;
    }

    public String getQuestion4() {
        return question4;
    }

    public void setQuestion4(String question4) {
        this.question4 = question4;
    }

    public String getQuestion5() {
        return question5;
    }

    public void setQuestion5(String question5) {
        this.question5 = question5;
    }

    public SubcontractorType getSubcontractorType() {
        return subcontractorType;
    }

    public void setSubcontractorType(SubcontractorType subcontractorType) {
        this.subcontractorType = subcontractorType;
    }

    public BigDecimal getRetentionFeeThreshold() {
        return retentionFeeThreshold;
    }

    public void setRetentionFeeThreshold(BigDecimal retentionFeeThreshold) {
        this.retentionFeeThreshold = retentionFeeThreshold;
    }

    public Set<DeliverableType> getAvailableDeliverableTypes() {
        return availableDeliverableTypes;
    }

    public void setAvailableDeliverableTypes(Set<DeliverableType> availableDeliverableTypes) {
        this.availableDeliverableTypes = availableDeliverableTypes;
    }

    public String getDeliverableName() {
        return deliverableName;
    }

    public void setDeliverableName(String deliverableName) {
        this.deliverableName = deliverableName;
    }

    public String getQuantityName() {
        return quantityName;
    }

    public void setQuantityName(String quantityName) {
        this.quantityName = quantityName;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        SubcontractingTemplateBlock cloned = (SubcontractingTemplateBlock) clone;
        cloned.setHasSubcontractorsTitle(getHasSubcontractorsTitle());
        cloned.setQuestion2(getQuestion2());
        cloned.setQuestion3(getQuestion3());
        cloned.setQuestion4(getQuestion4());
        cloned.setQuestion5(getQuestion5());
        cloned.setSubcontractorType(getSubcontractorType());
        cloned.setRetentionFeeThreshold(getRetentionFeeThreshold());
        for (DeliverableType availableDeliverableType : getAvailableDeliverableTypes()) {
            cloned.getAvailableDeliverableTypes().add(availableDeliverableType);
        }
        cloned.setDeliverableName(getDeliverableName());
        cloned.setQuantityName(getQuantityName());
        cloned.setValueName(getValueName());
        cloned.setFeeName(getFeeName());
    }
}
