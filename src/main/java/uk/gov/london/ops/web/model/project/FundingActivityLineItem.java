/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model.project;

import org.hibernate.validator.constraints.NotBlank;
import uk.gov.london.ops.domain.attachment.StandardAttachment;
import uk.gov.london.ops.domain.project.funding.FundingActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.london.common.GlaUtils.addBigDecimals;

public class FundingActivityLineItem {

    private Integer id;

    private Integer year;

    private Integer quarter;

    private Integer externalId;

    private String categoryDescription;

    private BigDecimal capitalValue;

    private BigDecimal capitalMatchFundValue;

    private BigDecimal revenueValue;

    private BigDecimal revenueMatchFundValue;

    @NotBlank
    private String name;

    private List<StandardAttachment> attachments = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }

    public BigDecimal getCapitalValue() {
        return capitalValue;
    }

    public void setCapitalValue(BigDecimal capitalValue) {
        this.capitalValue = capitalValue;
    }

    public BigDecimal getCapitalMatchFundValue() {
        return capitalMatchFundValue;
    }

    public void setCapitalMatchFundValue(BigDecimal capitalMatchFundValue) {
        this.capitalMatchFundValue = capitalMatchFundValue;
    }

    public BigDecimal getRevenueValue() {
        return revenueValue;
    }

    public void setRevenueValue(BigDecimal revenueValue) {
        this.revenueValue = revenueValue;
    }

    public BigDecimal getRevenueMatchFundValue() {
        return revenueMatchFundValue;
    }

    public void setRevenueMatchFundValue(BigDecimal revenueMatchFundValue) {
        this.revenueMatchFundValue = revenueMatchFundValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StandardAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<StandardAttachment> attachments) {
        this.attachments = attachments;
    }

    public static FundingActivityLineItem createFrom(FundingActivity fundingActivity) {
        FundingActivityLineItem lineItem = new FundingActivityLineItem();
        lineItem.setId(fundingActivity.getId());
        lineItem.setYear(fundingActivity.getYear());
        lineItem.setQuarter(fundingActivity.getQuarter());
        lineItem.setExternalId(fundingActivity.getExternalId());
        lineItem.setCategoryDescription(fundingActivity.getCategoryDescription());
        lineItem.setCapitalValue(fundingActivity.getCapitalMainValue());
        lineItem.setCapitalMatchFundValue(fundingActivity.getCapitalMatchFundValue());
        lineItem.setRevenueValue(fundingActivity.getRevenueMainValue());
        lineItem.setRevenueMatchFundValue(fundingActivity.getRevenueMatchFundValue());
        lineItem.setName(fundingActivity.getName());
        lineItem.setAttachments(fundingActivity.getAttachments());
        return lineItem;
    }

    public BigDecimal getTotal() {
        return addBigDecimals(capitalValue, capitalMatchFundValue, revenueValue, revenueMatchFundValue);
    }

}
