/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.template;

import java.util.List;

/**
 * An organisation block template.
 */
public class OrganisationBlockTemplate {

    private String blockName;
    private Double displayOrder;
    private boolean showBusinessDetails = false;
    private boolean showAddressDetails = false;
    private boolean showRegulatoryInformation = false;
    private List<OrganisationBlockQuestionTemplate> questions;

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isShowBusinessDetails() {
        return showBusinessDetails;
    }

    public void setShowBusinessDetails(boolean showBusinessDetails) {
        this.showBusinessDetails = showBusinessDetails;
    }

    public boolean isShowAddressDetails() {
        return showAddressDetails;
    }

    public void setShowAddressDetails(boolean showAddressDetails) {
        this.showAddressDetails = showAddressDetails;
    }

    public boolean isShowRegulatoryInformation() {
        return showRegulatoryInformation;
    }

    public void setShowRegulatoryInformation(boolean showRegulatoryInformation) {
        this.showRegulatoryInformation = showRegulatoryInformation;
    }

    public List<OrganisationBlockQuestionTemplate> getQuestions() {
        return questions;
    }

    public void setQuestions(List<OrganisationBlockQuestionTemplate> questions) {
        this.questions = questions;
    }
}
