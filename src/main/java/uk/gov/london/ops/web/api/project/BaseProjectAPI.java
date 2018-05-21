/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.project.ProjectService;

/**
 * Created by chris on 09/02/2017.
 */
public class BaseProjectAPI {

    @Autowired
    ProjectService service;

    protected void verifyBinding(String summary, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(summary, bindingResult.getFieldErrors());
        }
    }

}
