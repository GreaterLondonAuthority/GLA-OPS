/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import java.time.Year;
import java.util.stream.Collectors;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.skills.AllocationType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.london.common.skills.SkillsGrantType.AEB_GRANT;
import static uk.gov.london.ops.project.skills.AllocationType.Community;
import static uk.gov.london.ops.project.skills.AllocationType.Delivery;
import static uk.gov.london.ops.project.skills.AllocationType.LearnerSupport;

@Entity
@DiscriminatorValue("LEARNING_GRANT")
public class LearningGrantTemplateBlock extends TemplateBlock {

    @Column(name = "start_year")
    private Integer startYear = Year.now().getValue();;

    @Column(name = "number_of_years")
    private Integer numberOfYears = 1;

    @Column(name = "profile_title")
    private String profileTitle = "PROFILE %";

    @Column(name = "allocation_title")
    private String allocationTitle ="ALLOCATION £";

    @Column(name = "cumulative_allocation_title")
    private String cumulativeAllocationTitle = "CUMULATIVE ALLOCATION £";

    @Column(name = "cumulative_earnings_title")
    private String cumulativeEarningsTitle = "CUMULATIVE EARNINGS £";

    @Column(name = "cumulative_payment_title")
    private String cumulativePaymentTitle = "CUMULATIVE PAYMENT £";

    @Column(name = "payment_due_title")
    private String paymentDueTitle = "PAYMENT DUE £";

    @Column(name = "can_manually_claim_p14")
    private boolean canManuallyClaimP14;

    @NotNull
    @Column(name = "grant_type")
    @Enumerated(EnumType.STRING)
    private SkillsGrantType grantType;

    @Transient
    private List<AllocationType> allocationTypes;

    @Transient
    private SkillsGrantType profileAllocationType;

    public LearningGrantTemplateBlock() {
        super(ProjectBlockType.LearningGrant);
    }

    @PostLoad
    void loadBlockData() {
        LearningGrantTemplateBlock data = JSONUtils.fromJSON(this.blockData, LearningGrantTemplateBlock.class);
        if (data != null) {
            this.startYear = data.getStartYear();
            this.numberOfYears = data.getNumberOfYears();
            this.grantType = data.getGrantType();
            this.canManuallyClaimP14 = data.getCanManuallyClaimP14();
            this.allocationTypes = data.getAllocationTypes();
            this.paymentDueTitle = data.paymentDueTitle;
            this.cumulativePaymentTitle = data.cumulativePaymentTitle;
            this.cumulativeEarningsTitle = data.cumulativeEarningsTitle;
            this.cumulativeAllocationTitle = data.cumulativeAllocationTitle;
            this.allocationTitle = data.allocationTitle;
            this.profileTitle = data.profileTitle;
            this.profileAllocationType = data.profileAllocationType;
        }
    }

    @Override
    public boolean shouldSaveBlockData() {
        return true;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        LearningGrantTemplateBlock cloned = (LearningGrantTemplateBlock) clone;
        cloned.setStartYear(getStartYear());
        cloned.setNumberOfYears(getNumberOfYears());
        cloned.setProfileTitle(getProfileTitle());
        cloned.setAllocationTitle(getAllocationTitle());
        cloned.setCumulativeAllocationTitle(getCumulativeAllocationTitle());
        cloned.setCumulativeEarningsTitle(getCumulativeEarningsTitle());
        cloned.setCumulativePaymentTitle(getCumulativePaymentTitle());
        cloned.setPaymentDueTitle(getPaymentDueTitle());
        cloned.setGrantType(getGrantType());
        cloned.setCanManuallyClaimP14(getCanManuallyClaimP14());
        cloned.setAllocationTypes(getAllocationTypes());
        cloned.setProfileAllocationType(getProfileAllocationType());
    }

    @Override
    public List<TemplateBlockCommand> getTemplateBlockCommands() {
        List<TemplateBlockCommand> globalCommands = super.getTemplateBlockCommands().stream().collect(Collectors.toList());
        globalCommands.add(TemplateBlockCommand.EDIT_LEARNING_GRANT_LABELS);
        return globalCommands;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getNumberOfYears() {
        return numberOfYears;
    }

    public void setNumberOfYears(Integer numberOfYears) {
        this.numberOfYears = numberOfYears;
    }

    public String getProfileTitle() {
        return profileTitle;
    }

    public void setProfileTitle(String profileTitle) {
        this.profileTitle = profileTitle;
    }

    public String getAllocationTitle() {
        return allocationTitle;
    }

    public void setAllocationTitle(String allocationTitle) {
        this.allocationTitle = allocationTitle;
    }

    public String getCumulativeAllocationTitle() {
        return cumulativeAllocationTitle;
    }

    public void setCumulativeAllocationTitle(String cumulativeAllocationTitle) {
        this.cumulativeAllocationTitle = cumulativeAllocationTitle;
    }

    public String getCumulativeEarningsTitle() {
        return cumulativeEarningsTitle;
    }

    public void setCumulativeEarningsTitle(String cumulativeEarningsTitle) {
        this.cumulativeEarningsTitle = cumulativeEarningsTitle;
    }

    public String getCumulativePaymentTitle() {
        return cumulativePaymentTitle;
    }

    public void setCumulativePaymentTitle(String cumulativePaymentTitle) {
        this.cumulativePaymentTitle = cumulativePaymentTitle;
    }

    public String getPaymentDueTitle() {
        return paymentDueTitle;
    }

    public void setPaymentDueTitle(String paymentDueTitle) {
        this.paymentDueTitle = paymentDueTitle;
    }

    public SkillsGrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(SkillsGrantType grantType) {
        this.grantType = grantType;
    }

    public List<AllocationType> getAllocationTypes() {
        if (allocationTypes == null) {
            if (getProfileAllocationType() == AEB_GRANT) {
                return new ArrayList<>(Arrays.asList(Delivery, Community));
            } else {
                return new ArrayList<>(Arrays.asList(Delivery, LearnerSupport));
            }
        } else {
            return allocationTypes;
        }
    }

    public void setAllocationTypes(List<AllocationType> allocationTypes) {
        this.allocationTypes = allocationTypes;
    }

    public boolean getCanManuallyClaimP14() {
        return canManuallyClaimP14;
    }

    public void setCanManuallyClaimP14(boolean canManuallyClaimP14) {
        this.canManuallyClaimP14 = canManuallyClaimP14;
    }

    public SkillsGrantType getProfileAllocationType() {
        return this.profileAllocationType != null ? this.profileAllocationType : this.grantType;
    }

    public void setProfileAllocationType(SkillsGrantType profileAllocationType) {
        this.profileAllocationType = profileAllocationType;
    }
}
