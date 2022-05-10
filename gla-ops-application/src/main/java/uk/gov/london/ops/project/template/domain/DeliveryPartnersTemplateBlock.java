/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

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
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.deliverypartner.DeliverableType;

@Entity
@DiscriminatorValue("DELIVERY_PARTNERS")
public class DeliveryPartnersTemplateBlock extends TemplateBlock {

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "has_delivery_partners_title")
    private String hasDeliveryPartnersTitle = "Does this project have delivery partners?";

    @Column(name = "question2")
    private String question2;

    @Column(name = "question3")
    private String question3;

    @Column(name = "question4")
    private String question4;

    @Column(name = "question5")
    private String question5;

    @Column(name = "delivery_partner_type")
    private String deliveryPartnerType;

    @Column(name = "retention_fee_threshold")
    private BigDecimal retentionFeeThreshold;

    @ElementCollection(targetClass = DeliverableType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "template_deliverable_types", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "deliverable_type")
    @Enumerated(EnumType.STRING)
    private Set<DeliverableType> availableDeliverableTypes = new HashSet<>();

    @Column(name = "deliverable_name")
    private String deliverableName = "PROVISION";

    @Column(name = "quantity_name")
    private String quantityName = "NUMBER OF LEARNERS";

    @Column(name = "value_name")
    private String valueName = "AMOUNT ALLOCATED";

    @Column(name = "feeName")
    private String feeName = "RETENTION FEE";

    @Column(name = "show_deliverables")
    private boolean showDeliverables;

    @Transient
    private String[] availableDeliveryPartnerTypes = new String[]{"LearningProvider"};

    @Transient
    private boolean showOrganisationType = false;

    @Transient
    private String organisationTypeColumnName = "ORG TYPE";

    @Transient
    private String organisationNameColumnText = "ORG NAME";

    @Transient
    private String roleColumnText = "ROLE";

    @Transient
    private boolean showRoleColumn = false;

    @Transient
    private String ukprnColumnText = "UKRPRN";

    @Transient
    private boolean showUkprnColumn = false;

    @Transient
    private String contractValueColumnText = "CONTRACT VALUE";

    @Transient
    private boolean showContractValueColumn = false;

    public DeliveryPartnersTemplateBlock() {
        super(ProjectBlockType.DeliveryPartners);
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getHasDeliveryPartnersTitle() {
        return hasDeliveryPartnersTitle;
    }

    public void setHasDeliveryPartnersTitle(String hasDeliveryPartnersTitle) {
        this.hasDeliveryPartnersTitle = hasDeliveryPartnersTitle;
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

    public boolean isShowDeliverables() {
        return showDeliverables;
    }

    public void setShowDeliverables(boolean showDeliverables) {
        this.showDeliverables = showDeliverables;
    }

    public String[] getAvailableDeliveryPartnerTypes() {
        return availableDeliveryPartnerTypes;
    }

    public void setAvailableDeliveryPartnerTypes(String[] availableDeliveryPartnerTypes) {
        this.availableDeliveryPartnerTypes = availableDeliveryPartnerTypes;
    }

    public boolean isShowOrganisationType() {
        return showOrganisationType;
    }

    public void setShowOrganisationType(boolean showOrganisationType) {
        this.showOrganisationType = showOrganisationType;
    }

    public String getOrganisationTypeColumnName() {
        return organisationTypeColumnName;
    }

    public void setOrganisationTypeColumnName(String organisationTypeColumnName) {
        this.organisationTypeColumnName = organisationTypeColumnName;
    }

    public String getOrganisationNameColumnText() {
        return organisationNameColumnText;
    }

    public void setOrganisationNameColumnText(String organisationNameColumnText) {
        this.organisationNameColumnText = organisationNameColumnText;
    }

    public String getRoleColumnText() {
        return roleColumnText;
    }

    public void setRoleColumnText(String roleColumnText) {
        this.roleColumnText = roleColumnText;
    }

    public boolean isShowRoleColumn() {
        return showRoleColumn;
    }

    public void setShowRoleColumn(boolean showRoleColumn) {
        this.showRoleColumn = showRoleColumn;
    }

    public String getUkprnColumnText() {
        return ukprnColumnText;
    }

    public void setUkprnColumnText(String ukprnColumnText) {
        this.ukprnColumnText = ukprnColumnText;
    }

    public boolean isShowUkprnColumn() {
        return showUkprnColumn;
    }

    public void setShowUkprnColumn(boolean showUkprnColumn) {
        this.showUkprnColumn = showUkprnColumn;
    }

    public String getContractValueColumnText() {
        return contractValueColumnText;
    }

    public void setContractValueColumnText(String contractValueColumnText) {
        this.contractValueColumnText = contractValueColumnText;
    }

    public boolean isShowContractValueColumn() {
        return showContractValueColumn;
    }

    public void setShowContractValueColumn(boolean showContractValueColumn) {
        this.showContractValueColumn = showContractValueColumn;
    }

    @PostLoad
    void loadBlockData() {
        DeliveryPartnersTemplateBlock data = JSONUtils.fromJSON(this.blockData, DeliveryPartnersTemplateBlock.class);
        if (data != null) {
            this.setAvailableDeliveryPartnerTypes(data.getAvailableDeliveryPartnerTypes());
            this.setShowOrganisationType(data.isShowOrganisationType());
            this.setOrganisationTypeColumnName(data.getOrganisationTypeColumnName());

            this.organisationNameColumnText = data.organisationNameColumnText;
            this.roleColumnText = data.roleColumnText;
            this.showRoleColumn = data.showRoleColumn;
            this.ukprnColumnText = data.ukprnColumnText;
            this.showUkprnColumn = data.showUkprnColumn;
            this.contractValueColumnText = data.contractValueColumnText;
            this.showContractValueColumn = data.showContractValueColumn;
        }
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }


    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        DeliveryPartnersTemplateBlock cloned = (DeliveryPartnersTemplateBlock) clone;
        cloned.setEntityName(getEntityName());
        cloned.setHasDeliveryPartnersTitle(getHasDeliveryPartnersTitle());
        cloned.setQuestion2(getQuestion2());
        cloned.setQuestion3(getQuestion3());
        cloned.setQuestion4(getQuestion4());
        cloned.setQuestion5(getQuestion5());
        cloned.setDeliveryPartnerType(getDeliveryPartnerType());
        cloned.setRetentionFeeThreshold(getRetentionFeeThreshold());
        for (DeliverableType availableDeliverableType : getAvailableDeliverableTypes()) {
            cloned.getAvailableDeliverableTypes().add(availableDeliverableType);
        }
        cloned.setDeliverableName(getDeliverableName());
        cloned.setQuantityName(getQuantityName());
        cloned.setValueName(getValueName());
        cloned.setFeeName(getFeeName());
        cloned.setShowDeliverables(isShowDeliverables());
        cloned.setAvailableDeliverableTypes(getAvailableDeliverableTypes());
        cloned.setShowOrganisationType(isShowOrganisationType());
        cloned.setOrganisationTypeColumnName(getOrganisationTypeColumnName());
        cloned.setOrganisationNameColumnText(getOrganisationNameColumnText());
        cloned.setRoleColumnText(getRoleColumnText());
        cloned.setShowRoleColumn(isShowRoleColumn());
        cloned.setUkprnColumnText(getUkprnColumnText());
        cloned.setShowUkprnColumn(isShowUkprnColumn());
        cloned.setContractValueColumnText(getContractValueColumnText());
        cloned.setShowContractValueColumn(isShowContractValueColumn());
        cloned.setAvailableDeliveryPartnerTypes(getAvailableDeliveryPartnerTypes());
    }
}
