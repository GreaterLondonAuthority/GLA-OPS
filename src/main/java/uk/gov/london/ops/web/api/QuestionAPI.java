/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.template.AnswerOption;
import uk.gov.london.ops.domain.template.Question;
import uk.gov.london.ops.domain.template.Requirement;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.service.QuestionService;

import java.util.List;

/**
 * REST API for accessing the questions library.
 *
 * This is an administrative API. For end-users working with questions
 * on projects, the questions should be accessed via the template API.
 *
 * @author Steve Leach
 */
@RestController
@RequestMapping("/api/v1/questions")
@Api(description = "managing the questions library")
public class QuestionAPI {

    @Autowired
    QuestionService service;

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(method = RequestMethod.GET)
    public List<Question> getAll() {
        return service.getAll();
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public Question getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value="/{id}/requirement", method = RequestMethod.PUT)
    public void updateRequirement(@PathVariable Integer id, @RequestBody String requirement) {
        service.updateRequirement(id, Requirement.valueOf(requirement));
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value="/{id}/option", method = RequestMethod.POST)
    public void addOption(@PathVariable Integer id, @RequestBody AnswerOption option, @RequestParam(required = false) boolean defaultUnanswered) {
        service.addOption(id, option, defaultUnanswered);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value="/{id}/option", method = RequestMethod.DELETE)
    public void deleteOption(@PathVariable Integer id, @RequestParam String option) {
        service.deleteOption(id, option);
    }

}
