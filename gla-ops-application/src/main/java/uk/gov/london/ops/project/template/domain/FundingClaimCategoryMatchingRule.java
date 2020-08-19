/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

public class FundingClaimCategoryMatchingRule {

    private String source;
    private String patternToMatch;
    private boolean positiveMatch = true;

    public FundingClaimCategoryMatchingRule() {
    }

    public FundingClaimCategoryMatchingRule(String source, String patternToMatch) {
        this.source = source;
        this.patternToMatch = patternToMatch;
    }

    public FundingClaimCategoryMatchingRule(String source, String patternToMatch, boolean positiveMatch) {
        this.source = source;
        this.patternToMatch = patternToMatch;
        this.positiveMatch = positiveMatch;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPatternToMatch() {
        return patternToMatch;
    }

    public void setPatternToMatch(String patternToMatch) {
        this.patternToMatch = patternToMatch;
    }

    public boolean isPositiveMatch() {
        return positiveMatch;
    }

    public void setPositiveMatch(boolean positiveMatch) {
        this.positiveMatch = positiveMatch;
    }
}
