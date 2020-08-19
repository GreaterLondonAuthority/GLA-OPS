/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.project.block.ProjectBlockType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("FUNDING_CLAIMS")
public class FundingClaimsTemplateBlock extends TemplateBlock {

    @Transient
    private List<FundingClaimPeriod> periods = new ArrayList<>();

    @Transient
    private List<FundingClaimCategory> categories;

    @Transient
    private List<String> contractTypes = new ArrayList<>();

    @Transient
    private String newAllocationText;

    @Transient
    private String newAllocationRationaleText;

    @Transient
    private String newAllocationInfoMessage;

    //TODO should this be per contract type or for all of them
    @Transient
    private BigDecimal flexibleAllocationThreshold;

    @Transient
    private boolean fundingVariationsEnabled = false;

    @PostLoad
    @PostPersist
    @PostUpdate
    public void loadBlockData() {
        FundingClaimsTemplateBlock data = JSONUtils.fromJSON(this.blockData, FundingClaimsTemplateBlock.class);
        if (data != null) {
            this.periods = data.periods;
            this.categories = data.categories;
            this.newAllocationText = data.newAllocationText;
            this.newAllocationRationaleText = data.newAllocationRationaleText;
            this.newAllocationInfoMessage = data.newAllocationInfoMessage;
            this.flexibleAllocationThreshold = data.flexibleAllocationThreshold;
            this.fundingVariationsEnabled = data.fundingVariationsEnabled;
            this.contractTypes = data.contractTypes;
        }
    }

    public FundingClaimCategory getFundingClaimCategoryById(Integer id) {
        return categories.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    public FundingClaimsTemplateBlock() {
        super(ProjectBlockType.FundingClaims);
    }

    public List<FundingClaimCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<FundingClaimCategory> categories) {
        this.categories = categories;
    }

    public List<FundingClaimPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(List<FundingClaimPeriod> periods) {
        this.periods = periods;
    }

    public String getNewAllocationText() {
        return newAllocationText;
    }

    public void setNewAllocationText(String newAllocationText) {
        this.newAllocationText = newAllocationText;
    }

    public String getNewAllocationRationaleText() {
        return newAllocationRationaleText;
    }

    public void setNewAllocationRationaleText(String newAllocationRationaleText) {
        this.newAllocationRationaleText = newAllocationRationaleText;
    }

    public String getNewAllocationInfoMessage() {
        return newAllocationInfoMessage;
    }

    public void setNewAllocationInfoMessage(String newAllocationInfoMessage) {
        this.newAllocationInfoMessage = newAllocationInfoMessage;
    }

    public BigDecimal getFlexibleAllocationThreshold() {
        return flexibleAllocationThreshold;
    }

    public void setFlexibleAllocationThreshold(BigDecimal flexibleAllocationThreshold) {
        this.flexibleAllocationThreshold = flexibleAllocationThreshold;
    }

    public boolean isFundingVariationsEnabled() {
        return fundingVariationsEnabled;
    }

    public void setFundingVariationsEnabled(boolean fundingVariationsEnabled) {
        this.fundingVariationsEnabled = fundingVariationsEnabled;
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        FundingClaimsTemplateBlock cloned = (FundingClaimsTemplateBlock) clone;
        cloned.setPeriods(this.getPeriods());
        cloned.setCategories(this.getCategories());
        cloned.setNewAllocationRationaleText(this.getNewAllocationRationaleText());
        cloned.setNewAllocationText(this.getNewAllocationText());
        cloned.setNewAllocationInfoMessage(this.getNewAllocationInfoMessage());
        cloned.setFlexibleAllocationThreshold(this.getFlexibleAllocationThreshold());
        cloned.setFundingVariationsEnabled(this.isFundingVariationsEnabled());
        cloned.setContractTypes(this.getContractTypes());
    }

    public List<String> getContractTypes() {
        return contractTypes;
    }

    public void setContractTypes(List<String> contractTypes) {
        this.contractTypes = contractTypes;
    }
}
