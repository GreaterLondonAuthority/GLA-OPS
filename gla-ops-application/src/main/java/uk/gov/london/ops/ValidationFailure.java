/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops;

/**
 * Data validation failure object.
 *
 * @author Rob Bettison
 */
public class ValidationFailure {

    private ValidationType validationType;
    private String detail;

    public enum ValidationType {
        DuplicateBlocks,
        TestValidation
    }

    public ValidationFailure(ValidationType validationType, String detail) {
        this.validationType = validationType;
        this.detail = detail;
    }

    public ValidationFailure() {}

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public ValidationType getValidationType() {
        return validationType;
    }

    public void setValidationType(ValidationType validationType) {
        this.validationType = validationType;
    }
}
