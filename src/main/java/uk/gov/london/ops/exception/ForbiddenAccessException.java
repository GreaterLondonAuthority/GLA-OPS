/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.exception;

/**
 * This will be captured by {@link DefaultExceptionHandler} and will result in an HTTP 403.
 */
public class ForbiddenAccessException extends RuntimeException {

    public ForbiddenAccessException() {}

    public ForbiddenAccessException(String message) {
        super(message);
    }

}
