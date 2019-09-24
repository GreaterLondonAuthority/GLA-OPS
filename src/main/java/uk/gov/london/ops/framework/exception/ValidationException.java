/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.exception;

import uk.gov.london.common.error.ApiError;
import uk.gov.london.common.error.ApiErrorItem;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This will be captured by {@link DefaultExceptionHandler} and will result in an HTTP 400.
 */
public class ValidationException extends RuntimeException {

    private final ApiError error;

    public ValidationException(String description) {
        super(description);
        error = new ApiError(description);
    }

    public ValidationException(String name, String description) {
        error = new ApiError(null, Arrays.asList(new ApiErrorItem(name, description)));
    }

    public ValidationException(String description, List<FieldError> fieldErrors) {
        super(description);
        List<ApiErrorItem> errors = fieldErrors.stream().map(fe -> new ApiErrorItem(fe.getField(), fe.getDefaultMessage())).collect(Collectors.toList());
        error = new ApiError(description, errors);
    }

    public ApiError getError() {
        return error;
    }
}
