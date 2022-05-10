/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.deliverypartner;

import static uk.gov.london.ops.project.block.ProjectBlockType.DeliveryPartners;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.project.block.ProjectDifferences;
import uk.gov.london.ops.project.repeatingentity.EntityCollection;
import uk.gov.london.ops.project.template.domain.DeliveryPartnersTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;

@Entity(name = "delivery_partners_block")
@DiscriminatorValue("DELIVERY_PARTNERS")
@JoinData(sourceTable = "delivery_partners_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id",
        joinType = Join.JoinType.OneToOne,
        comment = "the delivery partners block is a subclass of the project block and shares a common key")
public class DeliveryPartnersBlock extends NamedProjectBlock implements EntityCollection<DeliveryPartner> {

    @Column(name = "entity_name")
    private String entityName = "";

    @Column(name = "has_delivery_partners")
    private Boolean hasDeliveryPartners;

    @Column(name = "question2")
    private Boolean question2;

    @Column(name = "question3")
    private Boolean question3;

    @Column(name = "question4")
    private Boolean question4;

    @Column(name = "question5")
    private Boolean question5;

    @Column(name = "delivery_partner_type")
    private String deliveryPartnerType = "LearningProvider";

    @Column(name = "retention_fee_threshold")
    private BigDecimal retentionFeeThreshold;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, targetEntity = DeliveryPartner.class, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    private List<DeliveryPartner> deliveryPartners = new ArrayList<>();

    @Column(name = "deliverable_name")
    private String deliverableName;

    @Column(name = "quantity_name")
    private String quantityName;

    @Column(name = "value_name")
    private String valueName;

    @Column(name = "fee_name")
    private String feeName;

    @Column(name = "show_deliverables")
    private boolean showDeliverables;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Boolean getHasDeliveryPartners() {
        return hasDeliveryPartners;
    }

    public void setHasDeliveryPartners(Boolean hasDeliveryPartners) {
        this.hasDeliveryPartners = hasDeliveryPartners;
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

    public String getDeliveryPartnerType() {
        return deliveryPartnerType;
    }

    public void setDeliveryPartnerType(String deliveryPartnerType) {
        this.deliveryPartnerType = deliveryPartnerType;
    }

    public BigDecimal getRetentionFeeThreshold() {
        return retentionFeeThreshold;
    }

    public void setRetentionFeeThreshold(BigDecimal retentionFeeThreshold) {
        this.retentionFeeThreshold = retentionFeeThreshold;
    }

    public List<DeliveryPartner> getDeliveryPartners() {
        return deliveryPartners;
    }

    public void setDeliveryPartners(List<DeliveryPartner> deliveryPartners) {
        this.deliveryPartners = deliveryPartners;
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

    @JsonProperty(value = "deliveryPartners", access = JsonProperty.Access.READ_ONLY)
    public List<DeliveryPartner> getEnrichedDeliveryPartners() {
        if (deliveryPartners != null) {
            for (DeliveryPartner deliveryPartner : deliveryPartners) {
                if (deliveryPartner.getDeliverables() != null) {
                    for (Deliverable deliverable : deliveryPartner.getDeliverables()) {
                        DeliverableFeeCalculation deliverableFeeCalculation = getDeliverableFeeCalculation(deliverable);
                        deliverable.setFeeCalculation(deliverableFeeCalculation);
                    }
                }
            }
        }
        return deliveryPartners;
    }

    private DeliverableFeeCalculation getDeliverableFeeCalculation(Deliverable deliverable) {
        BigDecimal value = deliverable.getValue();
        BigDecimal fee = deliverable.getFee();
        return getDeliverableFeeCalculation(value, fee);
    }

    public DeliverableFeeCalculation getDeliverableFeeCalculation(BigDecimal value, BigDecimal fee) {
        DeliverableFeeCalculation feeCalculation = new DeliverableFeeCalculation();
        if (value != null && fee != null && value.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal feePercentage = fee.multiply(new BigDecimal("100.00")).divide(value, 2, BigDecimal.ROUND_HALF_UP);
            feeCalculation.setFeePercentage(feePercentage);
            feeCalculation.setFeePercentageExceeded(
                    retentionFeeThreshold != null && feePercentage.compareTo(retentionFeeThreshold) > 0);
        }
        return feeCalculation;
    }

    @Override
    public boolean isComplete() {
        return isVisited() && getValidationFailures().size() == 0;
    }

    @Override
    protected void generateValidationFailures() {
        if (hasDeliveryPartners == null) {
            this.addErrorMessage("question", "", "All questions must be answered");
            return;
        }

        if (hasDeliveryPartners && !areQuestionsComplete()) {
            this.addErrorMessage("question", "", "All questions must be answered if this project has " + this.getEntityName());
        }

        if (hasDeliveryPartners && (deliveryPartners.isEmpty())) {
            this.addErrorMessage("table", "", "At least one " + this.getEntityName() + " must be entered");
            return;
        }

        if (hasDeliveryPartners && this.showDeliverables && deliveryPartners.stream()
                .anyMatch(s -> s.getDeliverables().isEmpty())) {
            if (this.deliverableName == null || this.deliverableName.isEmpty()) {
                this.addErrorMessage("table", "", "All " + this.getEntityName() + " must have at least one deliverable");
            } else {
                this.addErrorMessage("table", "",
                        "All " + this.getEntityName() + " must have at least one " + getDeliverableName());
            }
        }
    }

    @JsonIgnore
    private boolean areQuestionsComplete() {
        if (project == null) {
            return false;
        }
        DeliveryPartnersTemplateBlock stb = (DeliveryPartnersTemplateBlock) project.getTemplate()
                .getSingleBlockByType(DeliveryPartners);
        return isQuestionComplete(stb.getQuestion2(), question2)
                && isQuestionComplete(stb.getQuestion3(), question3)
                && isQuestionComplete(stb.getQuestion4(), question4)
                && isQuestionComplete(stb.getQuestion5(), question5);
    }

    @JsonIgnore
    private boolean isQuestionComplete(String templateQuestion, Boolean projectAnswer) {
        return StringUtils.isEmpty(templateQuestion) || projectAnswer != null;
    }

    public boolean isShowDeliverables() {
        return showDeliverables;
    }

    public void setShowDeliverables(boolean showDeliverables) {
        this.showDeliverables = showDeliverables;
    }

    @Override
    public void merge(NamedProjectBlock block) {
        DeliveryPartnersBlock updated = (DeliveryPartnersBlock) block;
        this.setEntityName(updated.getEntityName());
        this.setHasDeliveryPartners(updated.getHasDeliveryPartners());
        this.setQuestion2(updated.getQuestion2());
        this.setQuestion3(updated.getQuestion3());
        this.setQuestion4(updated.getQuestion4());
        this.setQuestion5(updated.getQuestion5());
        this.setDeliveryPartnerType(updated.getDeliveryPartnerType());
        this.setRetentionFeeThreshold(updated.getRetentionFeeThreshold());
        this.getDeliveryPartners().clear();
        this.getDeliveryPartners().addAll(updated.getDeliveryPartners());
        this.setDeliverableName(updated.getDeliverableName());
        this.setQuantityName(updated.getQuantityName());
        this.setValueName(updated.getValueName());
        this.setFeeName(updated.getFeeName());
        this.setShowDeliverables(updated.isShowDeliverables());
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        if (templateBlock instanceof DeliveryPartnersTemplateBlock) {
            DeliveryPartnersTemplateBlock stb = (DeliveryPartnersTemplateBlock) templateBlock;
            this.setEntityName(stb.getEntityName());
            this.setRetentionFeeThreshold(stb.getRetentionFeeThreshold());
            if (stb.getDeliveryPartnerType() != null) {
                this.setDeliveryPartnerType(stb.getDeliveryPartnerType());
            }
            this.setDeliverableName(stb.getDeliverableName());
            this.setQuantityName(stb.getQuantityName());
            this.setValueName(stb.getValueName());
            this.setFeeName(stb.getFeeName());
            this.setShowDeliverables(stb.isShowDeliverables());

        }
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        DeliveryPartnersBlock clone = (DeliveryPartnersBlock) target;
        clone.setEntityName(this.entityName);
        clone.setHasDeliveryPartners(this.hasDeliveryPartners);
        clone.setQuestion2(this.question2);
        clone.setQuestion3(this.question3);
        clone.setQuestion4(this.question4);
        clone.setQuestion5(this.question5);
        clone.setDeliveryPartnerType(this.deliveryPartnerType);
        clone.setRetentionFeeThreshold(this.retentionFeeThreshold);
        clone.setDeliverableName(this.deliverableName);
        clone.setQuantityName(this.quantityName);
        clone.setValueName(this.valueName);
        clone.setFeeName(this.feeName);
        clone.setShowDeliverables(this.showDeliverables);

        for (DeliveryPartner deliveryPartner : this.getDeliveryPartners()) {
            DeliveryPartner partner = new DeliveryPartner();
            partner.setOrganisationName(deliveryPartner.getOrganisationName());
            partner.setIdentifier(deliveryPartner.getIdentifier());
            partner.setOrganisationType(deliveryPartner.getOrganisationType());
            partner.setOriginalId(deliveryPartner.getOriginalId());

            clone.getDeliveryPartners().add(partner);

            for (Deliverable deliverable : deliveryPartner.getDeliverables()) {
                Deliverable toAdd = new Deliverable();
                toAdd.setValue(deliverable.getValue());
                toAdd.setFee(deliverable.getFee());
                toAdd.setQuantity(deliverable.getQuantity());
                toAdd.setDeliverableType(deliverable.getDeliverableType());
                toAdd.setComments(deliverable.getComments());
                partner.getDeliverables().add(toAdd);
            }
        }
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        DeliveryPartnersBlock otherDeliveryPartnersBlock = (DeliveryPartnersBlock) otherBlock;

        for (DeliveryPartner deliveryPartner : this.getDeliveryPartners()) {
            DeliveryPartner otherDeliveryPartner = otherDeliveryPartnersBlock.getDeliveryPartnerByComparison(deliveryPartner);
            if (otherDeliveryPartner == null) {
                ProjectDifference difference = new ProjectDifference(deliveryPartner);
                difference.setDifferenceType(ProjectDifference.DifferenceType.Addition);
                differences.add(difference);
            } else {
                differences.addAll(deliveryPartner.compareWith(otherDeliveryPartner));
            }
        }

        for (DeliveryPartner deliveryPartner : otherDeliveryPartnersBlock.getDeliveryPartners()) {
            if (this.getDeliveryPartnerByComparison(deliveryPartner) == null) {
                // no longer present so must have been deleted
                ProjectDifference difference = new ProjectDifference(deliveryPartner);
                difference.setDifferenceType(ProjectDifference.DifferenceType.Deletion);
                differences.add(difference);
            }
        }
    }

    private DeliveryPartner getDeliveryPartnerByComparison(ComparableItem comparableItem) {
        if (comparableItem == null) {
            return null;
        }
        for (DeliveryPartner deliveryPartner : deliveryPartners) {
            if (ComparableItem.areEqual(comparableItem, deliveryPartner)) {
                return deliveryPartner;
            }
        }
        return null;
    }

    @Override
    public DeliveryPartner getNewEntityInstance() {
        return new DeliveryPartner();
    }

    @Override
    public void createChildEntity(DeliveryPartner child) {
        this.getDeliveryPartners().add(child);
    }

    @Override
    public boolean hasChildEntities() {
        return !this.getDeliveryPartners().isEmpty();
    }

    @Override
    public boolean isSelfContained() {
        return false;
    }

}
