/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.web;

import org.springframework.validation.BindingResult;
import uk.gov.london.ops.framework.exception.ValidationException;

public class APIUtils {

    public static void verifyBinding(String summary, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(summary, bindingResult.getFieldErrors());
        }
    }

}
