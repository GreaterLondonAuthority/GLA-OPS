/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.exception;

import java.util.List;
import java.util.Random;

/**
 * Details of errors returned by the OPS REST API.
 */
public class ApiError {

    private String description;

    private List<ApiErrorItem> errors;

    private String errorId;

    private static Random rnd = new Random();

    public ApiError(String description) {
        this.description = description;
        this.errors = null;
        setErrorId();
    }

    public ApiError(String description, List<ApiErrorItem> errors) {
        this.description = description;
        this.errors = errors;
        setErrorId();
    }

    private void setErrorId() {
        int idNum = rnd.nextInt();
        idNum = Math.abs(idNum);        // Need positive values
        idNum /= 2;                     // Remove small values
        idNum += Integer.MAX_VALUE / 2;

        // Convert to a hex string
        this.errorId = String.format("%x",idNum);
    }

    /**
     * Returns the error ID, which will be a random 8 digit hexadecimal number.
     *
     * Every instance of the error should have its own ID, with low (though non-zero)
     * probability of collisions.
     *
     * API server code should write the error details, including the error ID, to
     * log files. Client code should provide the error ID to the user in any error
     * notification message, allowing users to provide the ID to support staff who
     * can then use it to locate the appropriate server log entries.
     */
    public String getId() {
        return errorId;
    }

    public String getDescription() {
        return description;
    }

    public List<ApiErrorItem> getErrors() {
        return errors;
    }

}
