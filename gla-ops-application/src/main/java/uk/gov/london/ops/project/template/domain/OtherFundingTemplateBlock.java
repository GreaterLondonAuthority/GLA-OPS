/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.repeatingentity.OtherFundingSource;

/**
 * Created by cmatias on 01/11/2019.
 */

@Entity
@DiscriminatorValue("OTHER_FUNDING")
public class OtherFundingTemplateBlock extends RepeatingEntityTemplateBlock {

    @Column(name = "entity_name")
    private String entityName = "Funding";

    @Transient
    private List<OtherFundingSource> fundingSources = new ArrayList<>();

    @Transient
    private String funderNameText = "Funder name";

    @Transient
    private String descriptionText = "Provide details";

    @Transient
    private String amountText = "Funding amount (£)";

    @Transient
    private boolean showAmount = false;

    @Transient
    private String securedQuestion = "Is this funding secured?";

    @Transient
    private boolean showSecuredQuestion = false;

    @Transient
    private Requirement evidenceRequirement = Requirement.hidden;

    @Transient
    private Integer maxEvidenceAttachments;

    @Transient
    private Integer maxUploadSizeInMb;

    @Transient
    private String partnersFundingQuestion = "Are you receiving funding from any partner organisations for this project?";

    @Transient
    private boolean showPartnersFundingQuestion;

    @Transient
    private boolean showOtherFundingDetailsSection;

    @Transient
    private String otherFundingDetailQuestion;

    @Transient
    private String otherFundingSecureDateQuestion;

    @Transient
    private String otherFundingFinalDateQuestion;

    @Transient
    private String otherFundingStrategyQuestion;

    public OtherFundingTemplateBlock() {
        super(ProjectBlockType.OtherFunding);
    }

    public OtherFundingTemplateBlock(int displayOrder) {
        super(displayOrder, ProjectBlockType.OtherFunding);
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<OtherFundingSource> getFundingSources() {
        return fundingSources;
    }

    public void setFundingSources(
            List<OtherFundingSource> fundingSources) {
        this.fundingSources = fundingSources;
    }

    public String getFunderNameText() {
        return funderNameText;
    }

    public void setFunderNameText(String funderNameText) {
        this.funderNameText = funderNameText;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public String getAmountText() {
        return amountText;
    }

    public void setAmountText(String amountText) {
        this.amountText = amountText;
    }

    public boolean isShowAmount() {
        return showAmount;
    }

    public void setShowAmount(boolean showAmount) {
        this.showAmount = showAmount;
    }

    public String getSecuredQuestion() {
        return securedQuestion;
    }

    public void setSecuredQuestion(String securedQuestion) {
        this.securedQuestion = securedQuestion;
    }

    public boolean isShowSecuredQuestion() {
        return showSecuredQuestion;
    }

    public void setShowSecuredQuestion(boolean showSecuredQuestion) {
        this.showSecuredQuestion = showSecuredQuestion;
    }

    public Requirement getEvidenceRequirement() {
        return evidenceRequirement;
    }

    public void setEvidenceRequirement(Requirement evidenceRequirement) {
        this.evidenceRequirement = evidenceRequirement;
    }

    public Integer getMaxEvidenceAttachments() {
        return maxEvidenceAttachments;
    }

    public void setMaxEvidenceAttachments(Integer maxEvidenceAttachments) {
        this.maxEvidenceAttachments = maxEvidenceAttachments;
    }

    public Integer getMaxUploadSizeInMb() {
        return maxUploadSizeInMb;
    }

    public void setMaxUploadSizeInMb(Integer maxUploadSizeInMb) {
        this.maxUploadSizeInMb = maxUploadSizeInMb;
    }

    public String getPartnersFundingQuestion() {
        return partnersFundingQuestion;
    }

    public void setPartnersFundingQuestion(String partnersFundingQuestion) {
        this.partnersFundingQuestion = partnersFundingQuestion;
    }

    public boolean isShowPartnersFundingQuestion() {
        return showPartnersFundingQuestion;
    }

    public void setShowPartnersFundingQuestion(boolean showPartnersFundingQuestion) {
        this.showPartnersFundingQuestion = showPartnersFundingQuestion;
    }

    public boolean isShowOtherFundingDetailsSection() {
        return showOtherFundingDetailsSection;
    }

    public void setShowOtherFundingDetailsSection(boolean showOtherFundingDetailsSection) {
        this.showOtherFundingDetailsSection = showOtherFundingDetailsSection;
    }

    public String getOtherFundingDetailQuestion() {
        return otherFundingDetailQuestion;
    }

    public void setOtherFundingDetailQuestion(String otherFundingDetailQuestion) {
        this.otherFundingDetailQuestion = otherFundingDetailQuestion;
    }

    public String getOtherFundingSecureDateQuestion() {
        return otherFundingSecureDateQuestion;
    }

    public void setOtherFundingSecureDateQuestion(String otherFundingSecureDateQuestion) {
        this.otherFundingSecureDateQuestion = otherFundingSecureDateQuestion;
    }

    public String getOtherFundingFinalDateQuestion() {
        return otherFundingFinalDateQuestion;
    }

    public void setOtherFundingFinalDateQuestion(String otherFundingFinalDateQuestion) {
        this.otherFundingFinalDateQuestion = otherFundingFinalDateQuestion;
    }

    public String getOtherFundingStrategyQuestion() {
        return otherFundingStrategyQuestion;
    }

    public void setOtherFundingStrategyQuestion(String otherFundingStrategyQuestion) {
        this.otherFundingStrategyQuestion = otherFundingStrategyQuestion;
    }

    @Override
    @PostLoad
    public void loadBlockData() {
        super.loadBlockData();
        OtherFundingTemplateBlock data = JSONUtils.fromJSON(this.blockData, OtherFundingTemplateBlock.class);
        if (data != null) {
            this.setEntityName(data.getEntityName());
            this.setFundingSources(data.getFundingSources());
            this.setFunderNameText(data.getFunderNameText());
            this.setDescriptionText(data.getDescriptionText());
            this.setAmountText(data.getAmountText());
            this.setShowAmount(data.isShowAmount());
            this.setSecuredQuestion(data.getSecuredQuestion());
            this.setShowSecuredQuestion(data.isShowSecuredQuestion());
            this.setEvidenceRequirement(data.getEvidenceRequirement());
            this.setMaxEvidenceAttachments(data.getMaxEvidenceAttachments());
            this.setMaxUploadSizeInMb(data.getMaxUploadSizeInMb());
            this.setPartnersFundingQuestion(data.getPartnersFundingQuestion());
            this.setShowPartnersFundingQuestion(data.isShowPartnersFundingQuestion());
            this.setShowOtherFundingDetailsSection(data.isShowOtherFundingDetailsSection());
            this.setOtherFundingDetailQuestion(data.getOtherFundingDetailQuestion());
            this.setOtherFundingSecureDateQuestion(data.getOtherFundingSecureDateQuestion());
            this.setOtherFundingFinalDateQuestion(data.getOtherFundingFinalDateQuestion());
            this.setOtherFundingStrategyQuestion(data.getOtherFundingStrategyQuestion());
        }
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        OtherFundingTemplateBlock ofb = (OtherFundingTemplateBlock) clone;
        ofb.setEntityName(this.getEntityName());
        ofb.setFundingSources(this.getFundingSources());
        ofb.setFunderNameText(this.getFunderNameText());
        ofb.setDescriptionText(this.getDescriptionText());
        ofb.setAmountText(this.getAmountText());
        ofb.setShowAmount(this.isShowAmount());
        ofb.setSecuredQuestion(this.getSecuredQuestion());
        ofb.setShowSecuredQuestion(this.isShowSecuredQuestion());
        ofb.setEvidenceRequirement(this.getEvidenceRequirement());
        ofb.setMaxEvidenceAttachments(this.getMaxEvidenceAttachments());
        ofb.setMaxUploadSizeInMb(this.getMaxUploadSizeInMb());
        ofb.setPartnersFundingQuestion(this.getPartnersFundingQuestion());
        ofb.setShowPartnersFundingQuestion(this.isShowPartnersFundingQuestion());
        ofb.setShowOtherFundingDetailsSection(this.isShowOtherFundingDetailsSection());
        ofb.setOtherFundingDetailQuestion(this.getOtherFundingDetailQuestion());
        ofb.setOtherFundingSecureDateQuestion(this.getOtherFundingSecureDateQuestion());
        ofb.setOtherFundingFinalDateQuestion(this.getOtherFundingFinalDateQuestion());
        ofb.setOtherFundingStrategyQuestion(this.getOtherFundingStrategyQuestion());
    }
}
