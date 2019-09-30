/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.domain.project.ProjectBlockType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@DiscriminatorValue("LEARNING_GRANT")
public class LearningGrantTemplateBlock extends TemplateBlock {

    @Column(name = "start_year")
    private Integer startYear = 2018;

    @Column(name = "number_of_years")
    private Integer numberOfYears = 1;

    @Column(name = "profile_title")
    private String profileTitle;

    @Column(name = "allocation_title")
    private String allocationTitle;

    @Column(name = "cumulative_allocation_title")
    private String cumulativeAllocationTitle;

    @Column(name = "cumulative_earnings_title")
    private String cumulativeEarningsTitle;

    @Column(name = "cumulative_payment_title")
    private String cumulativePaymentTitle;

    @Column(name = "payment_due_title")
    private String paymentDueTitle;

    @NotNull
    @Column(name="grant_type")
    @Enumerated(EnumType.STRING)
    private SkillsGrantType grantType;

    public LearningGrantTemplateBlock() {
        super(ProjectBlockType.LearningGrant);
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
}
