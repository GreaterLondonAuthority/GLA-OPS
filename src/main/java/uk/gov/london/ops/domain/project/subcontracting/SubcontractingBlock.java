/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.subcontracting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.template.SubcontractingTemplateBlock;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "subcontracting_block")
@DiscriminatorValue("SUBCONTRACTING")
@JoinData(sourceTable = "subcontracting_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the subcontracting lock is a subclass of the project block and shares a common key")
public class SubcontractingBlock extends NamedProjectBlock {

    @Column(name = "has_subcontractors")
    private Boolean hasSubcontractors;

    @Column(name = "question2")
    private Boolean question2;

    @Column(name = "question3")
    private Boolean question3;

    @Column(name = "question4")
    private Boolean question4;

    @Column(name = "question5")
    private Boolean question5;

    @Enumerated(EnumType.STRING)
    @Column(name= "subcontractor_type")
    private SubcontractorType subcontractorType = SubcontractorType.LearningProvider;

    @Column(name = "retention_fee_threshold")
    private BigDecimal retentionFeeThreshold;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, targetEntity = Subcontractor.class, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    @Column(name = "subcontractors")
    private List<Subcontractor> subcontractors = new ArrayList<>();

    @Column(name = "deliverable_name")
    private String deliverableName;

    @Column(name = "quantity_name")
    private String quantityName;

    @Column(name = "value_name")
    private String valueName;

    @Column(name = "fee_name")
    private String feeName;

    public Boolean getHasSubcontractors() {
        return hasSubcontractors;
    }

    public void setHasSubcontractors(Boolean hasSubcontractors) {
        this.hasSubcontractors = hasSubcontractors;
    }

    public Boolean getQuestion2() {
        return question2;
    }

    public void setQuestion2(Boolean question2) {
        this.question2 = question2;
    }

    public Boolean getQuestion3() {
        return question3;
    }

    public void setQuestion3(Boolean question3) {
        this.question3 = question3;
    }

    public Boolean getQuestion4() {
        return question4;
    }

    public void setQuestion4(Boolean question4) {
        this.question4 = question4;
    }

    public Boolean getQuestion5() {
        return question5;
    }

    public void setQuestion5(Boolean question5) {
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

    public List<Subcontractor> getSubcontractors() {
        return subcontractors;
    }

    public void setSubcontractors(List<Subcontractor> subcontractors) {
        this.subcontractors = subcontractors;
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

    @JsonProperty(value = "subcontractors", access = JsonProperty.Access.READ_ONLY)
    public List<Subcontractor> getEnrichedSubcontractors() {
        if (subcontractors != null) {
            for (Subcontractor subcontractor: subcontractors) {
                if (subcontractor.getDeliverables() != null) {
                    for (Deliverable deliverable: subcontractor.getDeliverables()) {
                        DeliverableFeeCalculation deliverableFeeCalculation = getDeliverableFeeCalculation(deliverable);
                        deliverable.setFeeCalculation(deliverableFeeCalculation);
                    }
                }
            }
        }
        return subcontractors;
    }

    private DeliverableFeeCalculation getDeliverableFeeCalculation(Deliverable deliverable) {
        BigDecimal value = deliverable.getValue();
        BigDecimal fee = deliverable.getFee();
        return getDeliverableFeeCalculation(value, fee);
    }

    public DeliverableFeeCalculation getDeliverableFeeCalculation(BigDecimal value, BigDecimal fee) {
        DeliverableFeeCalculation feeCalculation = new DeliverableFeeCalculation();
        if (value != null &&  fee != null) {
            BigDecimal feePercentage = fee.multiply(new BigDecimal("100.00")).divide(value, 2, BigDecimal.ROUND_HALF_UP);
            feeCalculation.setFeePercentage(feePercentage);
            feeCalculation.setFeePercentageExceeded(retentionFeeThreshold != null && feePercentage.compareTo(retentionFeeThreshold) > 0);
        }
        return feeCalculation;
    }

    @Override
    public boolean isComplete() {
        return isVisited() && getValidationFailures().size() == 0;

    }

    @Override
    protected void generateValidationFailures() {
        if (hasSubcontractors == null) {
            this.addErrorMessage("question", "", "All questions must be answered");
            return;
        }

        if (hasSubcontractors && !areQuestionsComplete()) {
            this.addErrorMessage("question", "", "All questions must be answered if this project has subcontractors");
        }

        if(hasSubcontractors && (subcontractors.isEmpty())) {
            this.addErrorMessage("table", "", "At least one subcontractor must be entered");
            return;
        }

        if (hasSubcontractors && subcontractors.stream().anyMatch(s -> s.getDeliverables().isEmpty())) {
          if(this.deliverableName == null || this.deliverableName.isEmpty()) {
            this.addErrorMessage("table", "", "All subcontractors must have at least one deliverable");
          } else {
            this.addErrorMessage("table", "", "All subcontractors must have at least one " + getDeliverableName().toLowerCase());
          }
        }
    }

    @JsonIgnore
    private boolean areQuestionsComplete() {
        if(project == null){
            return false;
        }
        SubcontractingTemplateBlock stb = (SubcontractingTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.Subcontracting);
        return isQuestionComplete(stb.getQuestion2(), question2)
                && isQuestionComplete(stb.getQuestion3(), question3)
                && isQuestionComplete(stb.getQuestion4(), question4)
                && isQuestionComplete(stb.getQuestion5(), question5);
    }

    @JsonIgnore
    private boolean isQuestionComplete(String templateQuestion, Boolean projectAnswer) {
        return StringUtils.isEmpty(templateQuestion) || projectAnswer != null;
    }

    @Override
    public void merge(NamedProjectBlock block) {
        SubcontractingBlock updated = (SubcontractingBlock) block;
        this.setHasSubcontractors(updated.getHasSubcontractors());
        this.setQuestion2(updated.getQuestion2());
        this.setQuestion3(updated.getQuestion3());
        this.setQuestion4(updated.getQuestion4());
        this.setQuestion5(updated.getQuestion5());
        this.setSubcontractorType(updated.getSubcontractorType());
        this.setRetentionFeeThreshold(updated.getRetentionFeeThreshold());
        this.getSubcontractors().clear();
        this.getSubcontractors().addAll(updated.getSubcontractors());
        this.setDeliverableName(updated.getDeliverableName());
        this.setQuantityName(updated.getQuantityName());
        this.setValueName(updated.getValueName());
        this.setFeeName(updated.getFeeName());
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        if(templateBlock instanceof SubcontractingTemplateBlock) {
            SubcontractingTemplateBlock stb = (SubcontractingTemplateBlock) templateBlock;
            this.setRetentionFeeThreshold(stb.getRetentionFeeThreshold());
            if(stb.getSubcontractorType() != null) {
                this.setSubcontractorType(stb.getSubcontractorType());
            }
            this.setDeliverableName(stb.getDeliverableName());
            this.setQuantityName(stb.getQuantityName());
            this.setValueName(stb.getValueName());
            this.setFeeName(stb.getFeeName());
        }
    }


    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        SubcontractingBlock clone = (SubcontractingBlock) target;
        clone.setHasSubcontractors(this.hasSubcontractors);
        clone.setQuestion2(this.question2);
        clone.setQuestion3(this.question3);
        clone.setQuestion4(this.question4);
        clone.setQuestion5(this.question5);
        clone.setSubcontractorType(this.subcontractorType);
        clone.setRetentionFeeThreshold(this.retentionFeeThreshold);
        clone.setDeliverableName(this.deliverableName);
        clone.setQuantityName(this.quantityName);
        clone.setValueName(this.valueName);
        clone.setFeeName(this.feeName);

        for (Subcontractor subcontractor : this.getSubcontractors()) {
            Subcontractor subcont = new Subcontractor();
            subcont.setOrganisationName(subcontractor.getOrganisationName());
            subcont.setIdentifierType(subcontractor.getIdentifierType());
            subcont.setIdentifier(subcontractor.getIdentifier());

            clone.getSubcontractors().add(subcont);

            for (Deliverable deliverable : subcontractor.getDeliverables()) {
                Deliverable toAdd = new Deliverable();
                toAdd.setValue(deliverable.getValue());
                toAdd.setFee(deliverable.getFee());
                toAdd.setQuantity(deliverable.getQuantity());
                toAdd.setDeliverableType(deliverable.getDeliverableType());
                toAdd.setComments(deliverable.getComments());
                subcont.getDeliverables().add(toAdd);
            }
        }
    }
}
