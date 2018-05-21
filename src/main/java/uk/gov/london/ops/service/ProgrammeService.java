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
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.EntityCount;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.ProgrammeSummary;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.repository.ProgrammeRepository;
import uk.gov.london.ops.repository.ProgrammeSummaryRepository;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.repository.TemplateRepository;
import uk.gov.london.ops.service.project.ProjectService;

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
    AuditService auditService;

    /**
     * @deprecated use getSummaries() instead and remove once we switch to managing org filter
     */
    public List<ProgrammeSummary> findAllEnabled(boolean enabled) {
        if (userService.currentUser().isGla()) {
            if (enabled) {
                return programmeSummaryRepository.findAllByEnabled(true);
            }
            else {
                return (List<ProgrammeSummary>) programmeSummaryRepository.findAll();
            }
        }
        else {
            if (enabled) {
                return programmeSummaryRepository.findAllByRestrictedAndEnabled(false, true);
            }
            else {
                return programmeSummaryRepository.findAllByRestricted(false);
            }
        }
    }

    public List<ProgrammeSummary> getSummaries(boolean enabled) {
        Set<Organisation> usersOrganisations = userService.currentUser().getOrganisations();

        Set<Organisation> managingOrganisations = new HashSet<>(usersOrganisations);

        for (Organisation organisation: usersOrganisations) {
            if (organisation.getManagingOrganisationId() != null) {
                managingOrganisations.add(organisation.getManagingOrganisation());
            }
        }

        if (enabled) {
            return programmeSummaryRepository.findAllByEnabled(true);
        }
        else {
            return programmeSummaryRepository.findAllByManagingOrganisationIn(managingOrganisations);
        }
    }

    public Programme getById(Integer id) {
        Programme programme = find(id);
        programme.setNbSubmittedProjects(projectRepository.countByProgrammeAndStatus(programme, Project.Status.Submitted));
        return programme;
    }

    public Programme find(Integer id) {
        Programme programme = programmeRepository.findOne(id);
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
        validateProgramme(programme);
        programme.setCreatedOn(environment.now());
        programme.setCreator(userService.loadCurrentUser());
        return programmeRepository.save(programme);
    }

    void validateProgramme(Programme programme) {
        if (StringUtils.isEmpty(programme.getName())) {
            throw new ValidationException("name","Programmes must have a name");
        }

        if (programme.getTemplates() == null || programme.getTemplates().isEmpty()) {
            throw new ValidationException("template","Programmes must have a template");
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
            programmeRepository.delete(id);
        }
        catch (DataIntegrityViolationException e){
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

    public Programme update(Integer id, Programme programme) {
        Programme existing = programmeRepository.findOne(id);
        if (existing == null) {
            throw new ValidationException("Unable to find programme with specified ID");
        }

        validateProgramme(programme);

        if (existing.isRestricted() != programme.isRestricted()) {
            Set<EntityCount> projectCountPerTemplateForProgramme = getProjectCountPerTemplateForProgramme(programme.getId());
            for (EntityCount entityCount : projectCountPerTemplateForProgramme) {
                if (entityCount.getEntityCount() > 0) {
                    throw new ValidationException("Unable to change programme restriction setting as projects have already been created. ");
                }
            }
        }

        existing.setName(programme.getName());
        if(!Objects.equals(existing.getWbsCode(), programme.getWbsCode())){
          auditService.auditCurrentUserActivity(String.format("Programme with ID %d WBS code was edited from %s to %s", existing.getId(), existing.getWbsCode(), programme.getWbsCode()));
        }
        existing.setWbsCode(programme.getWbsCode());
        existing.setEnabled(programme.isEnabled());
        existing.setRestricted(programme.isRestricted());

        for (Template template : programme.getTemplates()) {
            if (!isTemplatePresent(template.getId(), existing.getTemplates())) {
                // add missing template
                auditService.auditCurrentUserActivity(String.format("Template with ID %d was added to programme %d", template.getId(), existing.getId()));
                existing.getTemplates().add(templateRepository.findOne(template.getId()));
            }
        }

        for (Iterator<Template> iterator = existing.getTemplates().iterator(); iterator.hasNext(); ) {
            Template template = iterator.next();
            if (!isTemplatePresent(template.getId(), programme.getTemplates())) {
                // remove redundant template
                List<Project> existingProjects = projectRepository.findAllByProgrammeAndTemplate(existing, template);
                if (existingProjects != null && existingProjects.size() > 0) {
                    throw new ValidationException("Unable to remove template '%s' as it has already been used to create projects" , template.getName());
                }
                auditService.auditCurrentUserActivity(String.format("Template with ID %d was removed from programme %d", template.getId(), existing.getId()));
                iterator.remove();
            }
        }
        auditService.auditCurrentUserActivity(String.format("Programme with ID %d was edited", existing.getId()));

        return programmeRepository.save(existing);
    }

    private boolean isTemplatePresent(Integer templateId, Collection<Template> templates) {
        for (Template template : templates) {
            if (template.getId().equals(templateId)) {
                return true;
            }
        }
        return false;
    }

    public Programme updateManagingOrg(Integer id, Integer orgId) {
        Programme programme = programmeRepository.findOne(id);

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

}
