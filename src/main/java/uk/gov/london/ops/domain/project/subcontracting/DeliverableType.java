/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.subcontracting;

public enum DeliverableType {

    ADULT_EDUCATION_BUDGET("Adult Education Budget (AEB)"),
    COMMUNITY_LEARNERS("Community Learners (CL)"),
    OTHER("Other");


    String description;

    DeliverableType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
