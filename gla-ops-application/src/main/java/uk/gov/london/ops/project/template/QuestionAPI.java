/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template;

import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.project.template.domain.AnswerOption;
import uk.gov.london.ops.project.template.domain.Question;

/**
 * REST API for accessing the questions library.
 *
 * This is an administrative API. For end-users working with questions on projects, the questions should be accessed via the
 * template API.
 *
 * @author Steve Leach
 */
@RestController
@RequestMapping("/api/v1/questions")
@Api("managing the questions library")
public class QuestionAPI {

    @Autowired
    QuestionService service;

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(method = RequestMethod.GET)
    public Page<Question> getAll(@RequestParam(name = "question", required = false, defaultValue = "") String question,
            @RequestParam(name = "template", required = false, defaultValue = "") String template,
            @RequestParam(required = false) boolean enrich,
            Pageable pageable) {
        return service.getAll(question, template, enrich, pageable);
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Question getById(@PathVariable Integer id, @RequestParam(required = false) boolean enrich) {
        return service.getById(id, enrich);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(method = RequestMethod.POST)
    public Question create(@Valid @RequestBody Question question) {
        return service.create(question);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/{questionId}", method = RequestMethod.PUT)
    public void update(@PathVariable Integer questionId, @Valid @RequestBody Question question) {
        service.update(questionId, question);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/{questionId}", method = RequestMethod.DELETE)
    public void deleteQuestion(@PathVariable Integer questionId) {
        service.deleteQuestion(questionId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/{id}/requirement", method = RequestMethod.PUT)
    public void updateRequirement(@PathVariable Integer id, @RequestBody String requirement) {
        service.updateRequirement(id, Requirement.valueOf(requirement));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/{id}/option", method = RequestMethod.POST)
    public void addOption(@PathVariable Integer id, @RequestBody AnswerOption option,
            @RequestParam(required = false) boolean defaultUnanswered) {
        service.addOption(id, option, defaultUnanswered);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/{id}/option", method = RequestMethod.DELETE)
    public void deleteOption(@PathVariable Integer id, @RequestParam String option) {
        service.deleteOption(id, option);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @PostMapping("/{questionId}/file")
    @ApiOperation(value = "file upload", notes = "Endpoint for uploading a file")
    public @ResponseBody
    AttachmentFile upload(MultipartFile file,
            @PathVariable Integer questionId,
            @RequestParam Integer orgId,
            @RequestParam Integer programmeId,
            @RequestParam Integer projectId,
            @RequestParam Integer blockId) throws IOException {
        return service.uploadFile(questionId, orgId, programmeId, projectId, blockId, file);
    }

}
