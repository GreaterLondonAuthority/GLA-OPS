/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.assessment.AssessmentService;
import uk.gov.london.ops.assessment.AssessmentTemplate;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.domain.EntityCount;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.InternalBlockType;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.repository.ProgrammeRepository;
import uk.gov.london.ops.repository.ProgrammeSummaryRepository;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.repository.TemplateRepository;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;

import java.util.*;

/**
 * Service interface for managing programmes.
 *
 * @author Steve Leach
 */
@Service
public class ProgrammeService {

    @Autowired
    UserService userService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    ProgrammeRepository programmeRepository;

    @Autowired
    TemplateRepository templateRepository;

    @Autowired
    ProgrammeSummaryRepository programmeSummaryRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    ProjectService projectService;

    @Autowired
    Environment environment;

    @Autowired
    TemplateService templateService;

    @Autowired
    AuditService auditService;

    public Page<ProgrammeSummary> getSummaries(boolean enabled, List<Programme.Status> statuses, String programmeText, List<Integer> managingOrganisationsIds, Pageable pageable) {
        if (statuses == null || statuses.isEmpty()) {
            statuses = Arrays.asList(Programme.Status.values());
        }
        User currentUser = userService.currentUser();
        boolean includeRestricted = currentUser.isGla();

        Set<Organisation> usersOrganisations = currentUser.getOrganisations();

        Set<Organisation> managingOrganisations = new HashSet<>();
        if (managingOrganisationsIds != null) {
            for (Integer managingOrganisationId: managingOrganisationsIds) {
                managingOrganisations.add(organisationService.findOne(managingOrganisationId));
            }
        }
        else {
            managingOrganisations = new HashSet<>(usersOrganisations);
            for (Organisation organisation : usersOrganisations) {
                if (organisation.getManagingOrganisationId() != null) {
                    managingOrganisations.add(organisation.getManagingOrganisation());
                }
            }
        }

        if (enabled) {
           // return programmeSummaryRepository.findAllByEnabledAndStatusIn(true, statuses, pageable);
        	 return programmeSummaryRepository.findAllByEnabledAndStatusInAndRestricted(true, statuses,includeRestricted,pageable);
        } else {
            return programmeSummaryRepository.findAll(managingOrganisations,statuses,includeRestricted,programmeText,pageable);
        }
    }

    public Programme getById(Integer id, boolean enrich) {
        Programme programme = find(id);
        if (enrich) {
            Set<ProgrammeTemplateAssessmentTemplate> templatesByProgramme = programme.getProgrammeTemplateAssessmentTemplates();
            templatesByProgramme.forEach(pt -> {
                Integer assessmentsCount = assessmentService.countAssessments(pt.getProgramme(), pt.getTemplate(), pt.getAssessmentTemplate().getId());
                pt.setUsedInAssessment(assessmentsCount != null && assessmentsCount > 0);
            });
        }
        programme.setNbSubmittedProjects(projectRepository.countByProgrammeAndStatusName(programme, ProjectStatus.Submitted.name()));
        return programme;
    }

    public List<Programme> getProgrammesByTemplate(Integer templateId) {
        return programmeRepository.findAllProgrammesForTemplate(templateId);
    }

    public Programme find(Integer id) {
        Programme programme = programmeRepository.findById(id).orElse(null);
        if (programme == null) {
            throw new NotFoundException();
        }
        return programme;
    }

    public Programme findByName(String name) {
        return programmeRepository.findByName(name);
    }

    public List<Programme> findAllByNameContaining(String name) {
        return programmeRepository.findAllByNameContainingIgnoreCase(name);
    }

    public Programme create(Programme programme) {
        if (programme.getId() != null) {
            throw new ValidationException("id", "New programmes must not have an ID");
        }

        if (programme.getManagingOrganisationId() == null) {
            throw new ValidationException("managingOrganisation", "Programmes must have a managing organisation");
        }

        validateProgramme(programme);

        programme.setCreatedOn(environment.now());
        programme.setCreator(userService.loadCurrentUser());
        for (ProgrammeTemplate programmeTemplate : programme.getTemplatesByProgramme()) {
            Integer templateId = programmeTemplate.getId().getTemplateId() == null ? programmeTemplate.getTemplate().getId() : programmeTemplate.getId().getTemplateId();
            Template templateToAdd = getInflatedTemplate(templateId);

            programmeTemplate.setTemplate(templateToAdd);
            programmeTemplate.setProgramme(programme);
        }
        return programmeRepository.save(programme);
    }

    private void validateProgramme(Programme programme) {
        if (StringUtils.isEmpty(programme.getName())) {
            throw new ValidationException("name", "Programmes must have a name");
        }

        if (programme.getTemplates() == null || programme.getTemplates().isEmpty()) {
            throw new ValidationException("template", "Programmes must have a template");
        }

        if (programme.getManagingOrganisationId() != null && !userService.currentUser().inOrganisation(programme.getManagingOrganisationId())) {
            throw new ValidationException("User not part of the managing organisation!");
        }
    }

    public void updateEnabled(Integer id, boolean enabled) {
        Programme programme = find(id);
        programme.setEnabled(enabled);
        programmeRepository.save(programme);
    }

    public void deleteProgramme(Integer id) {
        try {
            programmeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException cause = (ConstraintViolationException) e.getCause();
                throw new RuntimeException(String.format("Referential integrity error deleting programme %d : %s",
                        id, cause.getConstraintName()));
            } else {
                throw e;
            }
        }
    }

    public Set<EntityCount> getProjectCountPerTemplateForProgramme(Integer progId) {
        return programmeRepository.getProjectsPerTemplateCountByProgramme(progId);
    }

    public Programme update(Integer id, Programme programmeWithUpdates) {
        Programme existing = programmeRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new ValidationException("Unable to find programme with specified ID");
        }

        validateProgramme(programmeWithUpdates);

        if (existing.isRestricted() != programmeWithUpdates.isRestricted()) {
            Set<EntityCount> projectCountPerTemplateForProgramme = getProjectCountPerTemplateForProgramme(programmeWithUpdates.getId());
            for (EntityCount entityCount : projectCountPerTemplateForProgramme) {
                if (entityCount.getEntityCount() > 0) {
                    throw new ValidationException("Unable to change programme restriction setting as projects have already been created. ");
                }
            }
        }

        existing.setModifiedOn(environment.now());
        existing.setModifier(userService.loadCurrentUser());

        existing.setName(programmeWithUpdates.getName());
        existing.setFinancialYear(programmeWithUpdates.getFinancialYear());

        existing.setEnabled(programmeWithUpdates.isEnabled());
        existing.setRestricted(programmeWithUpdates.isRestricted());
        existing.setInAssessment(programmeWithUpdates.isInAssessment());
        existing.setStatus(programmeWithUpdates.getStatus());

        // check for add/edit
        for (ProgrammeTemplate programmeTemplate : programmeWithUpdates.getTemplatesByProgramme()) {
            ProgrammeTemplate programmeTemplateToUpdate;
            if (!existing.isTemplatePresent(programmeTemplate.getId().getTemplateId())) {
                Template template = getInflatedTemplate(programmeTemplate.getId().getTemplateId());

                // add missing template
                auditService.auditCurrentUserActivity(String.format("Template with ID %d was added to programme %d", programmeTemplate.getId().getTemplateId(), existing.getId()));
                programmeTemplateToUpdate = new ProgrammeTemplate(existing, template);
                existing.getTemplatesByProgramme().add(programmeTemplateToUpdate);
                auditService.auditCurrentUserActivity(String.format("Programme with ID %d and Template ID %d capital WBS code was set to %s",
                        existing.getId(), programmeTemplateToUpdate.getTemplate().getId(), programmeTemplateToUpdate.getCapitalWbsCode()));
                auditService.auditCurrentUserActivity(String.format("Programme with ID %d and Template ID %d revenue WBS code was set to %s",
                        existing.getId(), programmeTemplateToUpdate.getTemplate().getId(), programmeTemplateToUpdate.getRevenueWbsCode()));
                auditService.auditCurrentUserActivity(String.format("Programme with ID %d and Template ID %d default WBS code was set to %s",
                        existing.getId(), programmeTemplateToUpdate.getTemplate().getId(), programmeTemplateToUpdate.getDefaultWbsCodeType()));

            } else {
                programmeTemplateToUpdate = existing.getTemplatesByProgramme().stream().filter(t -> t.getId().getTemplateId().equals(programmeTemplate.getId().getTemplateId())).findFirst().orElse(null);
                if (programmeTemplateToUpdate != null) {
                    if (!Objects.equals(programmeTemplateToUpdate.getCapitalWbsCode(), programmeTemplate.getCapitalWbsCode())) {
                        auditService.auditCurrentUserActivity(String.format("Programme with ID %d and Template ID %d capital WBS code was edited from %s to %s",
                                existing.getId(), programmeTemplateToUpdate.getId().getTemplateId(), programmeTemplateToUpdate.getCapitalWbsCode(), programmeTemplate.getCapitalWbsCode()));
                    }
                    if (!Objects.equals(programmeTemplateToUpdate.getRevenueWbsCode(), programmeTemplate.getRevenueWbsCode())) {
                        auditService.auditCurrentUserActivity(String.format("Programme with ID %d and Template ID %d revenue WBS code was edited from %s to %s",
                                existing.getId(), programmeTemplateToUpdate.getId().getTemplateId(), programmeTemplateToUpdate.getRevenueWbsCode(), programmeTemplate.getRevenueWbsCode()));
                    }
                    if (!Objects.equals(programmeTemplateToUpdate.getDefaultWbsCodeType(), programmeTemplate.getDefaultWbsCodeType())) {
                        auditService.auditCurrentUserActivity(String.format("Programme with ID %d and Template ID %d revenue WBS code was edited from %s to %s",
                                existing.getId(), programmeTemplateToUpdate.getId().getTemplateId(), programmeTemplateToUpdate.getDefaultWbsCodeType(), programmeTemplate.getDefaultWbsCodeType()));
                    }
                }
            }
            if (!programmeTemplateToUpdate.getStatus().equals(programmeTemplate.getStatus())) {
                programmeTemplateToUpdate.setStatus(programmeTemplate.getStatus());
                auditService.auditCurrentUserActivity(String.format("Template with ID %d was set to status: %s", programmeTemplate.getId().getTemplateId(), programmeTemplate.getStatus()));
            }

            if (programmeTemplateToUpdate.isPaymentsEnabled() != programmeTemplate.isPaymentsEnabled()) {
                programmeTemplateToUpdate.setPaymentsEnabled(programmeTemplate.isPaymentsEnabled());
                auditService.auditCurrentUserActivity(String.format("Template with ID %d was set to payment claims enabled: %s", programmeTemplate.getId().getTemplateId(), programmeTemplate.isPaymentsEnabled()));
            }

            programmeTemplateToUpdate.setCapitalWbsCode(programmeTemplate.getCapitalWbsCode());
            programmeTemplateToUpdate.setRevenueWbsCode(programmeTemplate.getRevenueWbsCode());
            programmeTemplateToUpdate.setDefaultWbsCodeType(programmeTemplate.getDefaultWbsCodeType());
            programmeTemplateToUpdate.setCeCode(programmeTemplate.getCeCode());

            // delete any not required
            for (Iterator<ProgrammeTemplateAssessmentTemplate> iterator = programmeTemplateToUpdate.getAssessmentTemplates().iterator(); iterator.hasNext(); ) {
                ProgrammeTemplateAssessmentTemplate ptat = iterator.next();

                if (ptat.getAssessmentTemplate() == null || ptat.getAssessmentTemplate().getId() == null)  {
                    throw new ValidationException("Unable to find assessment ID to remove");
                }
                int toUse = ptat.getAssessmentTemplate().getId();
                if (programmeTemplate.getProgrammeTemplateAssessmentTemplateByAssessmentID(toUse) == null) {
                    iterator.remove();
                }
            }

            // add new/update existing
            for (ProgrammeTemplateAssessmentTemplate ptat : programmeTemplate.getAssessmentTemplates()) {
                if (ptat.getAssessmentTemplate() == null || ptat.getAssessmentTemplate().getId() == null)  {
                    throw new ValidationException("Unable to find assessment ID to update");
                }
                int toUse = ptat.getAssessmentTemplate().getId();
                ProgrammeTemplateAssessmentTemplate updated = programmeTemplateToUpdate.getProgrammeTemplateAssessmentTemplateByAssessmentID(toUse);
                if (updated == null) {
                    AssessmentTemplate assessmentTemplate = new AssessmentTemplate();
                    assessmentTemplate.setId(toUse);
                    updated = new ProgrammeTemplateAssessmentTemplate(programmeTemplateToUpdate.getProgramme().getId(),
                            programmeTemplateToUpdate.getTemplate().getId(), assessmentTemplate);
                    programmeTemplateToUpdate.getAssessmentTemplates().add(
                            updated);

                }
                updated.setAllowedRoles(ptat.getAllowedRoles());
            }

            if (programmeTemplate.getAssessmentTemplates().size() > 0  && programmeTemplateToUpdate.getTemplate().getInternalBlockByType(InternalBlockType.Assessment) == null) {
                templateService.addInternalBlock(programmeTemplate.getId().getTemplateId(), new InternalAssessmentTemplateBlock("ASSESSMENT"));
            }
        }

        // check for delete
        for (Iterator<ProgrammeTemplate> iterator = existing.getTemplatesByProgramme().iterator(); iterator.hasNext(); ) {
            ProgrammeTemplate programmeTemplate = iterator.next();
            if (!programmeWithUpdates.isTemplatePresent(programmeTemplate.getTemplate().getId())) {
                // remove redundant template
                List<Project> existingProjects = projectRepository.findAllByProgrammeAndTemplate(existing, programmeTemplate.getTemplate());
                if (existingProjects != null && existingProjects.size() > 0) {
                    throw new ValidationException("Unable to remove template '%s' as it has already been used to create projects", programmeTemplate.getTemplate().getName());
                }
                iterator.remove();
                auditService.auditCurrentUserActivity(String.format("Template with ID %d was removed from programme %d", programmeTemplate.getTemplate().getId(), existing.getId()));
            }
        }

        existing.getTemplates().forEach(t -> t.setStatus(Template.TemplateStatus.Active));

        auditService.auditCurrentUserActivity(String.format("Programme with ID %d was edited", existing.getId()));

        return programmeRepository.save(existing);
    }

    private Template getInflatedTemplate(Integer templateId) {
        Template template = templateRepository.getOne(templateId);

        if (!Template.TemplateStatus.Active.equals(template.getStatus())) {
            template = templateService.inflateTemplateFromJson(template);
        }
        return template;
    }

    public Programme updateManagingOrg(Integer id, Integer orgId) {
        Programme programme = programmeRepository.getOne(id);

        // if there's already an managing org, and it has projects then can't update
        if (programme.getManagingOrganisationId() != null) {
            Long count = projectRepository.countByProgramme(programme);

            if (count > 0) {
                throw new ValidationException(String.format("Unable to update programme %d as it already has projects associated with it", id));
            }
        }

        Organisation managingOrg = organisationService.find(orgId);
        if (!managingOrg.isManagingOrganisation()) {
            throw new ValidationException(String.format("Organisation %d is not a managing org", id));
        }
        programme.setManagingOrganisation(managingOrg);
        programmeRepository.save(programme);

        projectService.updateManagingOrgForProjectsOnProgramme(programme, managingOrg);

        return programme;
    }

    public void updateSupportedReports(Integer id, List<String> supportedReports) {
        Programme programme = find(id);
        programme.setSupportedReports(supportedReports);
        programmeRepository.save(programme);
    }

    public List<Programme> getProgrammesManagedBy(Organisation organisation){
        return programmeRepository.findAllByManagingOrganisation(organisation);
    }

}
