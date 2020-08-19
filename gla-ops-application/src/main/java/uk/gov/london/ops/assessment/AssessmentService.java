/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.assessment.implementation.AssessmentRepository;
import uk.gov.london.ops.assessment.implementation.AssessmentSummaryRepository;
import uk.gov.london.ops.assessment.implementation.AssessmentTemplateRepository;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.framework.portableentity.PortableEntityProvider;
import uk.gov.london.ops.framework.portableentity.SanitiseComponent;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.programme.ProgrammeService;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.programme.domain.ProgrammeTemplateAssessmentTemplate;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.user.UserService;
import uk.gov.london.ops.user.domain.User;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentService implements PortableEntityProvider {

    @Autowired
    private AssessmentTemplateRepository assessmentTemplateRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentSummaryRepository assessmentSummaryRepository;

    @Autowired
    private FeatureStatus featureStatus;

    @Autowired
    private UserService userService;

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    SanitiseComponent sanitiseComponent;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    Environment environment;

    @Autowired
    OrganisationService organisationService;

    public List<AssessmentTemplate> getAssessmentTemplateSummaries() {
        return assessmentTemplateRepository.findAllSummaries();
    }

    public List<AssessmentTemplate> getAssessmentTemplatesForCreate(Integer programmeId, Integer templateId) {
        User user = userService.currentUser();
        Programme programme = programmeService.find(programmeId);
        Role role = user.getRole(programme.getManagingOrganisationId());

        ProgrammeTemplate template = programme.getProgrammeTemplateByTemplateID(templateId);
        return template.getAssessmentTemplates().stream()
                .filter(at -> at.getAllowedRoles().contains(role.getName()))
                .filter(at -> !at.getNoNewAssessments())
                .map(ProgrammeTemplateAssessmentTemplate::getAssessmentTemplate)
                .sorted(Comparator.comparing(AssessmentTemplate::getName, String::compareTo))
                .collect(Collectors.toList());
    }

    public List<AssessmentTemplate> getAssessmentTemplates(Integer managingOrgId) {
        return assessmentTemplateRepository.findAllByManagingOrganisation(managingOrgId);
    }

    public List<AssessmentTemplate> getAssessmentTemplates(Integer programmeId, Integer templateId, Integer managingOrgId) {

        List<AssessmentTemplate> res;

        if (programmeId != null && templateId != null) {
            res = assessmentTemplateRepository.findAll(programmeId, templateId);
        } else if (managingOrgId != null) {
            res = assessmentTemplateRepository.findAllByManagingOrganisation(managingOrgId);
        } else {
            res = assessmentTemplateRepository.findAll();
        }

        for (AssessmentTemplate assessmentTemplate : res) {
            assessmentTemplate.setUsed(this.isTemplateUsed(assessmentTemplate.getId()));
        }

        return res;
    }

    private boolean isTemplateUsed(Integer id) {
        Integer count = programmeService.countByAssessmentTemplateId(id);
        return (count != null && count > 0);
    }

    public AssessmentTemplate getAssessmentTemplate(Integer id, boolean enrich) {
        AssessmentTemplate assessmentTemplate = assessmentTemplateRepository.findById(id).orElse(null);
        if (enrich && assessmentTemplate != null) {
            Integer count = programmeService.countByAssessmentTemplateId(id);
            assessmentTemplate.setUsed(count != null && count > 0);
            assessmentTemplate.setUsed(this.isTemplateUsed(id));
        }
        return assessmentTemplate;
    }

    public AssessmentTemplate getAssessmentTemplate(String name) {
        return assessmentTemplateRepository.findByName(name);
    }

    public Assessment getAssessment(Integer id) {
        Assessment assessment =  assessmentRepository.findById(id).orElse(null);
        if (assessment == null) {
            return null;
        }
        AssessmentTemplate assessmentTemplate = assessment.getAssessmentTemplate();
        if (assessmentTemplate != null && assessmentTemplate.getSummary() && assessmentTemplate.getOutcomeOfAssessmentTemplateId() != null) {
            List<Assessment> outcomeAssessments = assessmentRepository.findByProjectIdAndTemplateId(assessment.getProjectId(), assessment.getProjectStatus(), assessmentTemplate.getOutcomeOfAssessmentTemplateId());
            outcomeAssessments = outcomeAssessments.stream().filter(oa -> StringUtils.equals(assessment.getProjectSubStatus(), oa.getProjectSubStatus()) && oa.getStatus() != AssessmentStatus.Abandoned).collect(Collectors.toList());
            if (outcomeAssessments != null) {
                AssessmentTemplate outcomeAssessmentTemplate = assessmentTemplateRepository.findById(assessmentTemplate.getOutcomeOfAssessmentTemplateId()).orElse(null);
                assessment.setOutcomeSummary(new AssessmentOutcomeSummary(outcomeAssessmentTemplate, outcomeAssessments));
            }
        }
        return assessment;
    }

    public Integer countAssessments(Integer programmeId, Integer templateId, Integer assessmentId) {
        return assessmentRepository.countAssessments(programmeId, templateId, assessmentId);
    }

    public AssessmentTemplate saveAssessmentTemplate(AssessmentTemplate assessmentTemplate) {
        if (assessmentTemplate != null && assessmentTemplate.getId() != null) {
            validateAssessmentBeforeChange(assessmentTemplate.getId());
        }
        // temporary fix , need proper investigation about best way to implement this.
        if (assessmentTemplate.getName() == null) {
            AssessmentTemplate fromDB = assessmentTemplateRepository.getOne(assessmentTemplate.getId());
            assessmentTemplate.setName(fromDB.getName());
        }
        return assessmentTemplateRepository.save(assessmentTemplate);
    }

    public Assessment createAssessment(Integer projectId, Assessment assessment) {
        User currentUser = userService.currentUser();
        Project project = projectService.get(projectId);
        assessment.setBlock(project.getInternalAssessmentBlock());
        assessment.setProjectStatus(project.getStatusName());
        assessment.setProjectSubStatus(project.getSubStatusName());
        assessment.setUsersPrimaryOrganisation(currentUser.getPrimaryOrganisation());
        assessment.setProjectId(projectId);
        assessment.setManagingOrgId(project.getManagingOrganisationId());
        assessment.setModifiedOn(OffsetDateTime.now());

        if (assessment.getAssessmentTemplate() == null || assessment.getAssessmentTemplate().getId() == null) {
            throw new ValidationException("Assessment template is required");
        }

        ProgrammeTemplateAssessmentTemplate programmeTemplateAssessmentTemplate = project.getProgrammeTemplate().getAssessmentTemplates().stream()
                .filter(ptat -> ptat.getAssessmentTemplate().getId().equals(assessment.getAssessmentTemplateId())).findFirst().orElse(null);

        if (assessment.getAssessmentTemplate() != null && programmeTemplateAssessmentTemplate == null) {
            throw new ValidationException("Assessment template is no available for this project");
        }

        if (assessment.getSections().isEmpty()) {
            assessment.initFrom(programmeTemplateAssessmentTemplate.getAssessmentTemplate());
        }
        return saveAssessment(assessment);
    }

    public Assessment updateAssessment(Assessment updated) {
        Assessment existing = getAssessment(updated.getId());
        existing.merge(updated);
        return saveAssessment(existing);
    }

    private Assessment saveAssessment(Assessment assessment) {
        return assessmentRepository.save(assessment);
    }

    public void deleteAssessmentTemplate(Integer id) {
        validateAssessmentBeforeChange(id);
        assessmentTemplateRepository.deleteById(id);
    }

    public void deleteAssessment(Integer id) {
        assessmentRepository.deleteById(id);
    }

    void validateAssessmentBeforeChange(Integer assessmentTemplateId) {
        AssessmentTemplate assessmentTemplate = getAssessmentTemplate(assessmentTemplateId, true);

        if (assessmentTemplate == null) {
            throw new ValidationException("Assessment template doesn't exists");
        }

        if (assessmentTemplate.isUsed() && !featureStatus.isEnabled(Feature.AllowChangeInUseAssessmentTemplate)) {
            throw new ValidationException("Assessment template is in use");
        }
    }

    public Page<AssessmentSummary> getAssessments(String createdByName, String projectNameOrId, List<String> assessmentTemplates, List<String> assessmentStatuses, List<Integer> programmes, List<String> projectStatuses, Pageable pageable) {
        return assessmentSummaryRepository.findAll(userService.currentUser(), createdByName, projectNameOrId, assessmentTemplates, assessmentStatuses, programmes, projectStatuses, pageable);
    }

    @Override
    public boolean canHandleEntity(String className) {
        return AssessmentTemplate.class.getSimpleName().equals(className);
    }

    @Override
    public String sanitize(String className, Integer id) throws JsonProcessingException {
        if (AssessmentTemplate.class.getSimpleName().equals(className)) {
            return sanitizeAssessmentTemplate(id);
        }
        return null;
    }

    private String sanitizeAssessmentTemplate(Integer id) throws JsonProcessingException {
        AssessmentTemplate template = assessmentTemplateRepository.findById(id).orElse(null);

        if (template == null) {
            throw new NotFoundException();
        }
        objectMapper.writeValueAsString(template);
        entityManager.detach(template);
        AssessmentTemplate sanitised = (AssessmentTemplate) sanitiseComponent.sanitise(template, Collections.singletonList(Organisation.class));
        sanitised.setName(String.format("Copy of %s", sanitised.getName()));
        sanitised.setStatus(AssessmentTemplateStatus.Draft);
        sanitised.setCreatedBy(null);
        sanitised.setCreatedOn(null);
        return objectMapper.writeValueAsString(sanitised);
    }

    @Override
    public void persist(String className, String json) {
        try {
            AssessmentTemplate assessmentTemplate = objectMapper.readValue(json, AssessmentTemplate.class);
            Organisation org = organisationService.findOne(assessmentTemplate.getTemporaryManagingOrganisationId());
            assessmentTemplate.setManagingOrganisation(org);
            if (assessmentTemplate.getId() != null) {
                throw new ValidationException("cannot edit existing assessment template!");
            }
            assessmentTemplateRepository.save(assessmentTemplate);
        } catch (IOException e) {
            throw new ValidationException("Unable to parse JSON Request: " + e.getMessage());
        }
    }

    public Assessment changeStatus(Integer id, AssessmentStatus status) {
        Assessment assessment = getAssessment(id);
        validateAssessmentTransition(status, assessment);
        assessment.setStatus(status);
        return saveAssessment(assessment);
    }

    private void validateAssessmentTransition(AssessmentStatus status, Assessment assessment) {
        String username = userService.currentUsername();

        if (!AssessmentStatus.Abandoned.equals(status)) {
            throw new ValidationException("Only abandoned transitions are valid");
        }

        if (!AssessmentStatus.InProgress.equals(assessment.getStatus())) {
            throw new ValidationException("Only In Progress assessments can be abandoned");
        }

        if (!username.equals(assessment.getCreatedBy())) {
            throw new ValidationException("Assessments can only be abandoned by the user that created them");
        }
    }
}
