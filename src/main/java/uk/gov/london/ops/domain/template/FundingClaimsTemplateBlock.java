/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.framework.JSONUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import java.util.List;

@Entity
@DiscriminatorValue("FUNDING_CLAIMS")
public class    FundingClaimsTemplateBlock extends TemplateBlock {

    @Transient
    private List<FundingClaimPeriod> periods;

    @Transient
    private List<FundingClaimCategory> categories;

    @Transient
    private String newAllocationText;

    @Transient
    private String newAllocationRationaleText;

    @Transient
    private String newAllocationInfoMessage;

    @PostLoad
    void loadBlockData() {
        FundingClaimsTemplateBlock data = JSONUtils.fromJSON(this.blockData, FundingClaimsTemplateBlock.class);
        if (data != null) {
            this.periods = data.periods;
            this.categories = data.categories;
            this.newAllocationText = data.newAllocationText;
            this.newAllocationRationaleText = data.newAllocationRationaleText;
            this.newAllocationInfoMessage = data.newAllocationInfoMessage;
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

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        FundingClaimsTemplateBlock cloned = (FundingClaimsTemplateBlock) clone;
        cloned.setPeriods(this.getPeriods());
        cloned.setCategories(this.getCategories());
        cloned.setNewAllocationRationaleText(this.getNewAllocationRationaleText());
        cloned.setNewAllocationText(this.getNewAllocationText());
        cloned.setNewAllocationInfoMessage(this.getNewAllocationInfoMessage());
    }

}
