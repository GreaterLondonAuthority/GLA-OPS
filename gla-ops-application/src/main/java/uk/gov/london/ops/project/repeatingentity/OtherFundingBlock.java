/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import static javax.persistence.CascadeType.ALL;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.template.domain.OtherFundingTemplateBlock;

/**
 * Created by cmatias on 01/11/2019.
 */
@Entity(name = "other_funding_block")
@DiscriminatorValue("OTHER_FUNDING")
@JoinData(sourceTable = "other_funding_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the other funding block is a subclass of the project block and shares a common key")
public class OtherFundingBlock extends RepeatingEntityBlock<OtherFunding> {

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "block_id",
            targetTable = "other_funding", comment = "")
    @OneToMany(fetch = FetchType.LAZY, cascade = ALL, orphanRemoval = true, targetEntity = OtherFunding.class)
    @JoinColumn(name = "block_id")
    @OrderBy("id")
    List<OtherFunding> otherFundings = new ArrayList<>();

    @Column(name = "has_funding_partners")
    private Boolean hasFundingPartners;

    @Column(name = "other_funding_summary")
    private String otherFundingSummary;

    @Column(name = "should_secure_by_date")
    private Boolean shouldSecureByDate;

    @Column(name = "final_secured_by")
    private String finalSecuredBy;

    @Column(name = "funding_strategy")
    private String fundingStrategy;


    public OtherFundingBlock() {
    }

    @Override
    public ProjectBlockType getProjectBlockType() {
        return ProjectBlockType.OtherFunding;
    }

    @Override
    public List<OtherFunding> getRepeatingEntities() {
        return otherFundings;
    }

    @Override
    public String getRootPath() {
        return "otherFunding";
    }

    public List<OtherFunding> getOtherFundings() {
        return otherFundings;
    }

    public void setOtherFundings(List<OtherFunding> otherFundings) {
        this.otherFundings = otherFundings;
    }

    public OtherFunding getOtherFundingById(Integer otherFundingId) {
        return this.getOtherFundings().stream().filter(of -> of.getId().equals(otherFundingId)).findFirst().orElse(null);
    }

    public Boolean getHasFundingPartners() {
        return hasFundingPartners;
    }

    public void setHasFundingPartners(Boolean hasFundingPartners) {
        this.hasFundingPartners = hasFundingPartners;
    }

    public String getOtherFundingSummary() {
        return otherFundingSummary;
    }

    public void setOtherFundingSummary(String otherFundingSummary) {
        this.otherFundingSummary = otherFundingSummary;
    }

    public Boolean getShouldSecureByDate() {
        return shouldSecureByDate;
    }

    public void setShouldSecureByDate(Boolean shouldSecureByDate) {
        this.shouldSecureByDate = shouldSecureByDate;
    }

    public String getFinalSecuredBy() {
        return finalSecuredBy;
    }

    public void setFinalSecuredBy(String finalSecuredBy) {
        this.finalSecuredBy = finalSecuredBy;
    }

    public String getFundingStrategy() {
        return fundingStrategy;
    }

    public void setFundingStrategy(String fundingStrategy) {
        this.fundingStrategy = fundingStrategy;
    }

    @Override
    protected void generateValidationFailures() {

        if (project == null) {
            return;
        }

        OtherFundingTemplateBlock blockTemplate = (OtherFundingTemplateBlock) project.getTemplate()
                .getSingleBlockByType(ProjectBlockType.OtherFunding);

        if (getBlockRequired() && otherFundings != null && otherFundings.isEmpty()) {
            this.addErrorMessage("table", "", "You must add at least one type of funding");
        }
    }

    @Override
    public OtherFunding getNewEntityInstance() {
        return new OtherFunding();
    }

    @Override
    public boolean isComplete() {
        boolean hasMissingAttachments = false;
        boolean hasMissingOtherFundings = false;

        OtherFundingTemplateBlock blockTemplate = (OtherFundingTemplateBlock) project.getTemplate()
                .getSingleBlockByType(ProjectBlockType.OtherFunding);

        if (getBlockRequired() || !blockTemplate.getHasBlockRequiredOption()) {
            hasMissingOtherFundings = (otherFundings == null || otherFundings.isEmpty());
        }

        if (getBlockRequired()
                && blockTemplate.getEvidenceRequirement() == Requirement.mandatory) {
            hasMissingAttachments = getOtherFundings().stream()
                    .anyMatch(of -> Boolean.TRUE.equals(of.isFundingSecured()) && of.attachments.size() == 0);
        }

        return isNotRequired() || (super.isComplete() && !hasMissingOtherFundings && !hasMissingAttachments);
    }

    @Override
    public void merge(NamedProjectBlock block) {
        super.merge(block);
        OtherFundingBlock updated = (OtherFundingBlock) block;
        this.getOtherFundings().clear();
        this.getOtherFundings().addAll(updated.getOtherFundings());
        this.setHasFundingPartners(updated.getHasFundingPartners());
        this.setOtherFundingSummary(updated.getOtherFundingSummary());
        this.setShouldSecureByDate(updated.getShouldSecureByDate());
        this.setFinalSecuredBy(updated.getFinalSecuredBy());
        this.setFundingStrategy(updated.getFundingStrategy());

    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        OtherFundingBlock clone = (OtherFundingBlock) target;
        for (OtherFunding otherFunding : this.getOtherFundings()) {
            clone.getOtherFundings().add(otherFunding);
        }
        clone.setHasFundingPartners(this.getHasFundingPartners());
        clone.setOtherFundingSummary(this.getOtherFundingSummary());
        clone.setShouldSecureByDate(this.getShouldSecureByDate());
        clone.setFinalSecuredBy(this.getFinalSecuredBy());
        clone.setFundingStrategy(this.getFundingStrategy());
    }

    @Override
    public boolean isSelfContained() {
        return false;
    }

}
