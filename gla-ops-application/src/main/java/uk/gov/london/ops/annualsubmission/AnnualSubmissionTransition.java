/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;

import java.util.Objects;

public class AnnualSubmissionTransition {

    private AnnualSubmissionStatus status;

    private boolean commentsRequired;

    public AnnualSubmissionTransition() {}

    public AnnualSubmissionTransition(AnnualSubmissionStatus status, boolean commentsRequired) {
        this.status = status;
        this.commentsRequired = commentsRequired;
    }

    public AnnualSubmissionStatus getStatus() {
        return status;
    }

    public boolean isCommentsRequired() {
        return commentsRequired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnualSubmissionTransition that = (AnnualSubmissionTransition) o;
        return commentsRequired == that.commentsRequired &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, commentsRequired);
    }

}
