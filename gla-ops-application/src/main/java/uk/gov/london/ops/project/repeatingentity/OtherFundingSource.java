/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

public class OtherFundingSource {

    private String fundingSource;

    private boolean showFunderName = false;

    private boolean showDescription = false;

    public OtherFundingSource() {
    }

    public OtherFundingSource(String fundingSource, boolean showFunderName, boolean showDescription) {
        this.fundingSource = fundingSource;
        this.showFunderName = showFunderName;
        this.showDescription = showDescription;
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public boolean isShowFunderName() {
        return showFunderName;
    }

    public void setShowFunderName(boolean showFunderName) {
        this.showFunderName = showFunderName;
    }

    public boolean isShowDescription() {
        return showDescription;
    }

    public void setShowDescription(boolean showDescription) {
        this.showDescription = showDescription;
    }
}
