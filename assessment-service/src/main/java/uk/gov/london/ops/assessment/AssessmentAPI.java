/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.framework.annotations.PermissionRequired;

import java.util.List;

import static uk.gov.london.common.user.BaseRole.INTERNAL_BLOCK_EDITOR;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.ops.permission.PermissionType.*;

@RestController
@RequestMapping("/api/v1")
@Api
public class AssessmentAPI {

    @Autowired
    private AssessmentServiceImpl assessmentService;

    @PermissionRequired(ASSESSMENT_VIEW)
    @RequestMapping(value = "/assessmentTemplatesSummary", method = RequestMethod.GET)
    public List<AssessmentTemplate> getAssessmentTemplateSummaries() {
        return assessmentService.getAssessmentTemplateSummaries();
    }

    @PermissionRequired(ASSESSMENT_VIEW)
    @RequestMapping(value = "/assessmentTemplates", method = RequestMethod.GET)
    public List<AssessmentTemplate> getAssessmentTemplates(@RequestParam(required = false) Integer programmeId,
                                                           @RequestParam(required = false) Integer templateId,
                                                           @RequestParam(required = false) Integer managingOrgId) {
        return assessmentService.getAssessmentTemplates(programmeId, templateId, managingOrgId);
    }

    @PermissionRequired({ASSESSMENT_VIEW})
    @RequestMapping(value = "/assessmentTemplatesForUser", method = RequestMethod.GET)
    public List<AssessmentTemplate> getAssessmentTemplatesForCreate(@RequestParam(required = false) Integer programmeId,
                                                                    @RequestParam(required = false) Integer templateId) {
        return assessmentService.getAssessmentTemplatesForCurrentUser(programmeId, templateId);
    }

    @PermissionRequired({ASSESSMENT_VIEW})
    @RequestMapping(value = "/assessmentTemplates/{id}", method = RequestMethod.GET)
    public AssessmentTemplate getAssessmentTemplate(@PathVariable  Integer id) {
        return assessmentService.getAssessmentTemplate(id, true);
    }

    @PermissionRequired(ASSESSMENT_VIEW)
    @RequestMapping(value = "/assessments/{id}", method = RequestMethod.GET)
    public Assessment getAssessment(@PathVariable  Integer id) {
        return assessmentService.getAssessment(id);
    }

    @PermissionRequired(ASSESSMENT_VIEW)
    @RequestMapping(value = "/assessments", method = RequestMethod.GET)
    public Page<AssessmentSummary> getAssessments(
        @RequestParam(name = "createdBy", required = false) String createdByName,
        @RequestParam(name = "project", required = false) String projectNameOrId,
        @RequestParam(name = "assessmentTemplates", required = false) List<String> assessmentTemplates,
        @RequestParam(name = "assessmentStatuses", required = false) List<String> assessmentStatuses,
        @RequestParam(name = "programmes", required = false) List<Integer> programmes,
        @RequestParam(name = "projectStatuses", required = false) List<String> projectStatuses,
        Pageable pageable) {
        return assessmentService.getAssessments(createdByName, projectNameOrId, assessmentTemplates, assessmentStatuses,
                programmes, projectStatuses, pageable);
    }

    @Secured({OPS_ADMIN, INTERNAL_BLOCK_EDITOR})
    @PermissionRequired(ASSESSMENT_TEMPLATE_MANAGE)
    @RequestMapping(value = "/assessmentTemplates", method = RequestMethod.POST)
    public AssessmentTemplate createAssessmentTemplate(@RequestBody AssessmentTemplate assessmentTemplate) {
        return assessmentService.saveAssessmentTemplate(assessmentTemplate);
    }

    @PermissionRequired(ASSESSMENT_MANAGE)
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}/assessments", method = RequestMethod.POST)
    public Assessment createAssessment(@PathVariable Integer projectId, @PathVariable Integer blockId,
                                       @RequestBody Assessment assessment) {
        return assessmentService.createAssessment(projectId, blockId, assessment);
    }

    @Secured({OPS_ADMIN, INTERNAL_BLOCK_EDITOR})
    @RequestMapping(value = "/assessmentTemplates/{id}", method = RequestMethod.PUT)
    public AssessmentTemplate updateAssessmentTemplate(@PathVariable Integer id,
                                                       @RequestBody AssessmentTemplate assessmentTemplate) {
        return assessmentService.saveAssessmentTemplate(assessmentTemplate);
    }

    @PermissionRequired(ASSESSMENT_MANAGE)
    @RequestMapping(value = "/assessments/{id}", method = RequestMethod.PUT)
    public Assessment updateAssessment(@PathVariable Integer id, @RequestBody Assessment assessment) {
        return assessmentService.updateAssessment(assessment);
    }

    @Secured({OPS_ADMIN, INTERNAL_BLOCK_EDITOR})
    @RequestMapping(value = "/assessmentTemplates/{id}", method = RequestMethod.DELETE)
    public void deleteAssessmentTemplate(@PathVariable Integer id) {
        assessmentService.deleteAssessmentTemplate(id);
    }

    @Secured({OPS_ADMIN, INTERNAL_BLOCK_EDITOR})
    @RequestMapping(value = "/assessments/{id}", method = RequestMethod.DELETE)
    public void deleteAssessment(@PathVariable Integer id) {
        assessmentService.deleteAssessment(id);
    }

    @PermissionRequired(ASSESSMENT_MANAGE)
    @RequestMapping(value = "/assessments/{id}/status", method = RequestMethod.PUT)
    public void updateStatus(@PathVariable Integer id,
                             @RequestBody String status) {
        assessmentService.changeStatus(id, AssessmentStatus.valueOf(status));
    }
}
