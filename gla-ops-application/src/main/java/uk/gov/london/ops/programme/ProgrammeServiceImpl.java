/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.assessment.AssessmentService;
import uk.gov.london.ops.assessment.AssessmentTemplate;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.contracts.ContractModel;
import uk.gov.london.ops.contracts.ContractService;
import uk.gov.london.ops.domain.EntityCount;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.programme.domain.*;
import uk.gov.london.ops.programme.implementation.ProgrammePublicProfileSummaryRepository;
import uk.gov.london.ops.programme.implementation.ProgrammeRepository;
import uk.gov.london.ops.programme.implementation.ProgrammeSummaryRepository;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.accesscontrol.AccessControlRelationshipType;
import uk.gov.london.ops.project.accesscontrol.DefaultAccessControlSummary;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.project.template.domain.InternalAssessmentTemplateBlock;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.service.DataAccessControlService;
import uk.gov.london.ops.user.User;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.listToCsString;
import static uk.gov.london.ops.framework.OPSUtils.currentUsername;
import static uk.gov.london.ops.user.UserUtils.currentUser;

/**
 * Service interface for managing programmes.
 *
 * @author Steve Leach
 */
@Service
public class ProgrammeServiceImpl implements ProgrammeService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    UserServiceImpl userService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    ProgrammeRepository programmeRepository;

    @Autowired
    ProgrammeSummaryRepository programmeSummaryRepository;

    @Autowired
    ProgrammePublicProfileSummaryRepository programmePublicProfileSummaryRepository;

    @Autowired
    OrganisationServiceImpl organisationService;

    @Autowired
    ProjectService projectService;

    @Autowired
    DataAccessControlService dataAccessControlService;

    @Autowired
    Environment environment;

    @Autowired
    TemplateServiceImpl templateService;

    @Autowired
    AuditService auditService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ContractService contractService;

    public Page<ProgrammeSummary> getAllPaged(boolean enabled, List<Programme.Status> statuses, String programmeText,
            List<Integer> managingOrganisationsIds, Pageable pageable) {
        if (statuses == null || statuses.isEmpty()) {
            statuses = Arrays.asList(Programme.Status.values());
        }
        UserEntity currentUser = userService.currentUser();
        boolean includeRestricted = currentUser.isGla();

        Set<OrganisationEntity> usersOrganisations = currentUser.getOrganisations();

        Set<OrganisationEntity> managingOrganisations = new HashSet<>();
        if (managingOrganisationsIds != null) {
            for (Integer managingOrganisationId : managingOrganisationsIds) {
                managingOrganisations.add(organisationService.findOne(managingOrganisationId));
            }
        } else {
            managingOrganisations = new HashSet<>(usersOrganisations);
            for (OrganisationEntity organisation : usersOrganisations) {
                if (organisation.getManagingOrganisationId() != null) {
                    managingOrganisations.add(organisation.getManagingOrganisation());
                }
            }
        }

        Page<ProgrammeSummary> programmes;
        if (enabled) {
            programmes = programmeSummaryRepository.findAllEnabled(statuses, includeRestricted, pageable);
        } else {
            programmes = programmeSummaryRepository
                    .findAll(managingOrganisations, statuses, includeRestricted, programmeText, pageable);
        }
        enrich(programmes.getContent());
        return programmes;
    }

    private void enrich(List<ProgrammeSummary> programmes) {
        for (ProgrammeSummary programme: programmes) {
            enrich(programme);
        }
    }

    private void enrich(ProgrammeSummary programme) {
        if (programme.getModifiedBy() != null) {
            programme.setModifierName(userService.getUserFullName(programme.getModifiedBy()));
        }
    }

    public Collection<ProgrammeFilterItem> getProgrammesFilters() {
        Map<Integer, ProgrammeFilterItem> programmes = new HashMap<>();

        List<Map<String, Object>> results = getProgrammesFilterListAsMap();
        for (Map<String, Object> result : results) {
            Integer programmeId = (Integer) result.get("programme_id");
            Integer programmeManagingOrgId = (Integer) result.get("managing_organisation_id");

            if (!programmes.containsKey(programmeId)) {
                programmes.put(programmeId, new ProgrammeFilterItem(
                        programmeId,
                        (String) result.get("programme_name"),
                        (String) result.get("programme_status"),
                        (Boolean) result.get("programme_enabled"),
                        programmeManagingOrgId,
                        (String) result.get("managing_organisation_name")
                ));
            }

            ProgrammeFilterItem programme = programmes.get(programmeId);
            programme.getTemplates().add(new TemplateFilterItem(
                    (Integer) result.get("template_id"),
                    (String) result.get("template_name")
            ));
        }

        return programmes.values();
    }

    private List<Map<String, Object>> getProgrammesFilterListAsMap() {
        String sql = buildSqlQueryForLoadingProgrammesFilterList();
        return jdbcTemplate.queryForList(sql);
    }

    private String buildSqlQueryForLoadingProgrammesFilterList() {
        UserEntity currentUser = userService.currentUser();

        String managingOrgIdsAsCsString = listToCsString(currentUser.getManagingOrganisationsIds()
                .stream().map(Object::toString).collect(Collectors.toList()));

        String sql = "select "
                + "p.id as programme_id, "
                + "p.name as programme_name, "
                + "p.status as programme_status, "
                + "p.enabled as programme_enabled, "
                + "p.managing_organisation_id as managing_organisation_id, "
                + "o.name as managing_organisation_name, "
                + "t.id as template_id, "
                + "t.name as template_name "
                + "from programme p "
                + "inner join programme_template pt on pt.programme_id = p.id "
                + "inner join template t on t.id = pt.template_id "
                + "inner join organisation o on o.id = p.managing_organisation_id "
                + "where p.managing_organisation_id in (" + managingOrgIdsAsCsString + ") ";
        if (!currentUser.isGla()) {
            sql += "and p.restricted = false";
        }

        return sql;
    }

    public Programme getById(Integer id, boolean enrich) {
        Programme programme = find(id);
        if (enrich) {
            programme.setCreatorName(userService.getUserFullName(programme.getCreatedBy()));
            programme.setModifierName(userService.getUserFullName(programme.getModifiedBy()));
            Set<ProgrammeTemplateAssessmentTemplate> templatesByProgramme = programme.getProgrammeTemplateAssessmentTemplates();
            templatesByProgramme.forEach(pt -> {
                Integer assessmentsCount = assessmentService
                        .countAssessments(pt.getProgramme(), pt.getTemplate(), pt.getAssessmentTemplate().getId());
                pt.setUsedInAssessment(assessmentsCount != null && assessmentsCount > 0);
            });
        }
        programme.setNbSubmittedProjects(projectService.countByProgrammeAndStatusName(programme, ProjectStatus.Submitted.name()));
        return programme;
    }

    public Programme getOne(Integer id) {
        return programmeRepository.getOne(id);
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

    @Override
    public List<ProgrammeDetailsSummary> getProgrammeDetailsSummaries() {
        return toDetailSummaries(findAll());
    }

    @Override
    public List<ProgrammeDetailsSummary> getProgrammeDetailsSummariesByTemplate(Integer templateId) {
        return toDetailSummaries(programmeRepository.findAllProgrammesForTemplate(templateId));
    }

    private List<ProgrammeDetailsSummary> toDetailSummaries(List<Programme> programmes) {
        return programmes.stream().map(this::toDetailsSummary).collect(Collectors.toList());
    }

    @Override
    public ProgrammeDetailsSummary getProgrammeDetailsSummary(Integer programmeId) {
        Programme programmeEntity = getOne(programmeId);
        return toDetailsSummary(programmeEntity);
    }

    @Override
    public ProgrammeDetailsSummary getProgrammeDetailsSummary(String programmeName) {
        Programme programmeEntity = findByName(programmeName);
        return toDetailsSummary(programmeEntity);
    }

    private ProgrammeDetailsSummary toDetailsSummary(Programme programmeEntity) {
        return new ProgrammeDetailsSummary(programmeEntity.getId(),
                programmeEntity.getName(),
                programmeEntity.getManagingOrganisationId(),
                programmeEntity.getCompanyEmail(),
                programmeEntity.getGrantTypes(),
                programmeEntity.hasIndicativeTemplate());
    }

    public List<Programme> findAll() {
        return programmeRepository.findAll();
    }

    public List<Programme> findAllByNameContaining(String name) {
        return programmeRepository.findAllByNameContainingIgnoreCase(name);
    }

    public Programme save(Programme programme) {
        return programmeRepository.save(programme);
    }

    public Programme create(Programme programme) {
        if (programme.getId() != null) {
            throw new ValidationException("id", "New programmes must not have an ID");
        }

        if (programme.getManagingOrganisationId() == null) {
            throw new ValidationException("managingOrganisation", "Programmes must have a managing organisation");
        }

        validateProgramme(programme, null);

        programme.setCreatedOn(environment.now());
        programme.setCreatedBy(currentUsername());
        for (ProgrammeTemplate programmeTemplate : programme.getTemplatesByProgramme()) {
            Integer templateId = programmeTemplate.getId().getTemplateId() == null ? programmeTemplate.getTemplate().getId()
                    : programmeTemplate.getId().getTemplateId();
            Template templateToAdd = getInflatedTemplate(templateId);

            programmeTemplate.setTemplate(templateToAdd);
            programmeTemplate.setProgramme(programme);
        }
        programme = save(programme);
        for (Template template : programme.getTemplates()) {
            dataAccessControlService.insertDefaultAccessControl(programme.getId(), template.getId(),
                    programme.getManagingOrganisationId(), AccessControlRelationshipType.MANAGING);
        }
        return programme;
    }

    void validateProgramme(Programme programme, Programme existingProgramme) {
        if (StringUtils.isEmpty(programme.getName())) {
            throw new ValidationException("name", "Programmes must have a name");
        }

        if (programme.getTemplates() == null || programme.getTemplates().isEmpty()) {
            throw new ValidationException("template", "Programmes must have a template");
        }

        if (programme.getManagingOrganisationId() != null && !userService.currentUser()
                .inOrganisation(programme.getManagingOrganisationId())) {
            throw new ValidationException("User not part of the managing organisation!");
        }

        if (programme.getOpeningDatetime() != null && programme.getClosingDatetime() != null
                && (programme.getOpeningDatetime().isAfter(programme.getClosingDatetime())
                || programme.getOpeningDatetime().equals(programme.getClosingDatetime()))) {
            throw new ValidationException("When changing the scheduled programme opening dates the closing date "
                    + "must be further in the future than the opening date");
        }

        if (existingProgramme != null) {
            if (existingProgramme.getOpeningDatetime() != null) {
                //check that date have actually been updated so no errors for 'expired' dates
                if (!programme.getOpeningDatetime().isEqual(existingProgramme.getOpeningDatetime())
                        && programme.getOpeningDatetime().isBefore(environment.now())) {
                    throw new ValidationException(
                            "When changing the scheduled programme opening date the new value must be in the future");
                }
            } else if (programme.getOpeningDatetime() != null && programme.getOpeningDatetime().isBefore(environment.now())) {
                throw new ValidationException("When changing the scheduled programme opening date the new value must be in the future");
            }

            if (existingProgramme.getClosingDatetime() != null) {
                //check that date have actually been updated so no errors for 'expired' dates
                if (!programme.getClosingDatetime().isEqual(existingProgramme.getClosingDatetime())
                        && programme.getClosingDatetime().isBefore(environment.now())) {
                    throw new ValidationException("When changing the scheduled programme closing date the new value must be in the future");
                }
            } else if (programme.getClosingDatetime() != null && programme.getClosingDatetime().isBefore(environment.now())) {
                throw new ValidationException("When changing the scheduled programme closing date the new value must be in the future");
            }
        }
    }

    public void updateEnabled(Integer id, boolean enabled) {
        Programme programme = find(id);
        programme.setEnabled(enabled);
        save(programme);
    }

    public void deleteProgramme(Integer id) {
        try {
            Programme programme = getOne(id);
            for (Template template : programme.getTemplates()) {
                dataAccessControlService.deleteDefaultAccessControl(programme.getId(), template.getId(),
                        programme.getManagingOrganisationId());
            }
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

    @Scheduled(cron = "${programme.scheduler.cron.expression}")
    public void triggerScheduledProgrammes() {
        scheduleProgammeEnabled(environment.now());
    }

    public void scheduleProgammeEnabled(OffsetDateTime dateTime) {
        List<Programme> openingProgrammes = programmeRepository.findAllByOpeningDatetime(dateTime.truncatedTo(ChronoUnit.HOURS));
        List<Programme> closingProgrammes = programmeRepository.findAllByClosingDatetime(dateTime.truncatedTo(ChronoUnit.HOURS));
        List<Programme> updatedProgrammes = new ArrayList<>();
        for (Programme programme : openingProgrammes) {
            if (closingProgrammes.contains(programme)) {
                continue;
            } else {
                programme.setEnabled(true);
                updatedProgrammes.add(programme);
            }
        }
        for (Programme programme : closingProgrammes) {
            if (openingProgrammes.contains(programme)) {
                continue;
            } else {
                programme.setEnabled(false);
                updatedProgrammes.add(programme);
            }
        }
        programmeRepository.saveAll(updatedProgrammes);
    }

    public void setProgrammeOpenCloseDates(Integer programmeId, OffsetDateTime open,
                                           OffsetDateTime close) {
        Programme programme = programmeRepository.findById(programmeId).get();
        programme.setOpeningDatetime(open);
        programme.setClosingDatetime(close);
        programmeRepository.save(programme);
    }

    @Transactional
    public Programme update(Integer id, Programme programmeWithUpdates) {
        Programme existing = programmeRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new ValidationException("Unable to find programme with specified ID");
        }

        validateProgramme(programmeWithUpdates, existing);

        if (existing.isRestricted() != programmeWithUpdates.isRestricted()) {
            Set<EntityCount> projectCountPerTemplateForProgramme = getProjectCountPerTemplateForProgramme(
                    programmeWithUpdates.getId());
            for (EntityCount entityCount : projectCountPerTemplateForProgramme) {
                if (entityCount.getEntityCount() > 0) {
                    throw new ValidationException(
                            "Unable to change programme restriction setting as projects have already been created. ");
                }
            }
        }

        existing.setModifiedOn(environment.now());
        existing.setModifiedBy(currentUsername());

        // Programme public profile fields
        existing.setName(programmeWithUpdates.getName());
        existing.setDescription(programmeWithUpdates.getDescription());
        existing.setTotalFunding(programmeWithUpdates.getTotalFunding());
        existing.setWebsiteLink(programmeWithUpdates.getWebsiteLink());

        existing.setFinancialYear(programmeWithUpdates.getFinancialYear());
        existing.setEnabled(programmeWithUpdates.isEnabled());
        existing.setRestricted(programmeWithUpdates.isRestricted());
        existing.setInAssessment(programmeWithUpdates.isInAssessment());
        existing.setStatus(programmeWithUpdates.getStatus());
        existing.setCompanyName(programmeWithUpdates.getCompanyName());
        existing.setCompanyEmail(programmeWithUpdates.getCompanyEmail());
        existing.setOpeningDatetime(programmeWithUpdates.getOpeningDatetime());
        existing.setClosingDatetime(programmeWithUpdates.getClosingDatetime());
        updateProgrammeYear(programmeWithUpdates, existing);
        // check for add/edit
        for (ProgrammeTemplate programmeTemplate : programmeWithUpdates.getTemplatesByProgramme()) {
            ProgrammeTemplate programmeTemplateToUpdate;
            if (!existing.isTemplatePresent(programmeTemplate.getId().getTemplateId())) {
                Template template = getInflatedTemplate(programmeTemplate.getId().getTemplateId());
                if (template.getContract() != null && template.getContractId() == null) {
                    ContractModel contractModel = contractService.create(template.getContract());
                    template.setContractId(contractModel.getId());
                }

                // add missing template
                auditService.auditCurrentUserActivity(
                        String.format("Template with ID %d was added to programme %d", programmeTemplate.getId().getTemplateId(),
                                existing.getId()));
                programmeTemplateToUpdate = new ProgrammeTemplate(existing, template);
                existing.getTemplatesByProgramme().add(programmeTemplateToUpdate);
                auditService.auditCurrentUserActivity(
                        String.format("Programme with ID %d and Template ID %d capital WBS code was set to %s",
                                existing.getId(), programmeTemplateToUpdate.getTemplate().getId(),
                                programmeTemplateToUpdate.getCapitalWbsCode()));
                auditService.auditCurrentUserActivity(
                        String.format("Programme with ID %d and Template ID %d revenue WBS code was set to %s",
                                existing.getId(), programmeTemplateToUpdate.getTemplate().getId(),
                                programmeTemplateToUpdate.getRevenueWbsCode()));
                auditService.auditCurrentUserActivity(
                        String.format("Programme with ID %d and Template ID %d default WBS code was set to %s",
                                existing.getId(), programmeTemplateToUpdate.getTemplate().getId(),
                                programmeTemplateToUpdate.getDefaultWbsCodeType()));

                dataAccessControlService.insertDefaultAccessControl(programmeTemplate.getId().getProgrammeId(),
                        programmeTemplate.getId().getTemplateId(), existing.getManagingOrganisationId(),
                        AccessControlRelationshipType.MANAGING);

            } else {
                programmeTemplateToUpdate = existing.getTemplatesByProgramme().stream()
                        .filter(t -> t.getId().getTemplateId().equals(programmeTemplate.getId().getTemplateId())).findFirst()
                        .orElse(null);
                if (programmeTemplateToUpdate != null) {
                    if (!Objects.equals(programmeTemplateToUpdate.getCapitalWbsCode(), programmeTemplate.getCapitalWbsCode())) {
                        auditService.auditCurrentUserActivity(
                                String.format("Programme with ID %d and Template ID %d capital WBS code was edited from %s to %s",
                                        existing.getId(), programmeTemplateToUpdate.getId().getTemplateId(),
                                        programmeTemplateToUpdate.getCapitalWbsCode(), programmeTemplate.getCapitalWbsCode()));
                    }
                    if (!Objects.equals(programmeTemplateToUpdate.getRevenueWbsCode(), programmeTemplate.getRevenueWbsCode())) {
                        auditService.auditCurrentUserActivity(
                                String.format("Programme with ID %d and Template ID %d revenue WBS code was edited from %s to %s",
                                        existing.getId(), programmeTemplateToUpdate.getId().getTemplateId(),
                                        programmeTemplateToUpdate.getRevenueWbsCode(), programmeTemplate.getRevenueWbsCode()));
                    }
                    if (!Objects.equals(programmeTemplateToUpdate.getDefaultWbsCodeType(),
                            programmeTemplate.getDefaultWbsCodeType())) {
                        auditService.auditCurrentUserActivity(
                                String.format("Programme with ID %d and Template ID %d revenue WBS code was edited from %s to %s",
                                        existing.getId(), programmeTemplateToUpdate.getId().getTemplateId(),
                                        programmeTemplateToUpdate.getDefaultWbsCodeType(),
                                        programmeTemplate.getDefaultWbsCodeType()));
                    }
                }
            }
            if (!programmeTemplateToUpdate.getStatus().equals(programmeTemplate.getStatus())) {
                programmeTemplateToUpdate.setStatus(programmeTemplate.getStatus());
                auditService.auditCurrentUserActivity(
                        String.format("Template with ID %d was set to status: %s", programmeTemplate.getId().getTemplateId(),
                                programmeTemplate.getStatus()));
            }

            if (programmeTemplateToUpdate.isPaymentsEnabled() != programmeTemplate.isPaymentsEnabled()) {
                programmeTemplateToUpdate.setPaymentsEnabled(programmeTemplate.isPaymentsEnabled());
                auditService.auditCurrentUserActivity(String.format("Template with ID %d was set to payment claims enabled: %s",
                        programmeTemplate.getId().getTemplateId(), programmeTemplate.isPaymentsEnabled()));
            }

            programmeTemplateToUpdate.setCapitalWbsCode(programmeTemplate.getCapitalWbsCode());
            programmeTemplateToUpdate.setRevenueWbsCode(programmeTemplate.getRevenueWbsCode());
            programmeTemplateToUpdate.setDefaultWbsCodeType(programmeTemplate.getDefaultWbsCodeType());
            programmeTemplateToUpdate.setCeCode(programmeTemplate.getCeCode());

            // delete any not required
            for (Iterator<ProgrammeTemplateAssessmentTemplate> iterator = programmeTemplateToUpdate.getAssessmentTemplates()
                    .iterator(); iterator.hasNext(); ) {
                ProgrammeTemplateAssessmentTemplate ptat = iterator.next();

                if (ptat.getAssessmentTemplate() == null || ptat.getAssessmentTemplate().getId() == null) {
                    throw new ValidationException("Unable to find assessment ID to remove");
                }
                int toUse = ptat.getAssessmentTemplate().getId();
                if (programmeTemplate.getProgrammeTemplateAssessmentTemplateByAssessmentID(toUse) == null) {
                    iterator.remove();
                }
            }

            // add new/update existing
            for (ProgrammeTemplateAssessmentTemplate ptat : programmeTemplate.getAssessmentTemplates()) {
                if (ptat.getAssessmentTemplate() == null || ptat.getAssessmentTemplate().getId() == null) {
                    throw new ValidationException("Unable to find assessment ID to update");
                }
                int toUse = ptat.getAssessmentTemplate().getId();
                ProgrammeTemplateAssessmentTemplate updated = programmeTemplateToUpdate
                        .getProgrammeTemplateAssessmentTemplateByAssessmentID(toUse);
                if (updated == null) {
                    AssessmentTemplate assessmentTemplate = new AssessmentTemplate();
                    assessmentTemplate.setId(toUse);
                    updated = new ProgrammeTemplateAssessmentTemplate(programmeTemplateToUpdate.getProgramme().getId(),
                            programmeTemplateToUpdate.getTemplate().getId(), assessmentTemplate);
                    programmeTemplateToUpdate.getAssessmentTemplates().add(
                            updated);

                }
                updated.setNoNewAssessments(ptat.getNoNewAssessments());
                updated.setAllowedRoles(ptat.getAllowedRoles());
            }

            if (programmeTemplate.getAssessmentTemplates().size() > 0
                    && programmeTemplateToUpdate.getTemplate().getInternalBlockByType(InternalBlockType.Assessment) == null) {
                templateService.addInternalBlock(programmeTemplate.getId().getTemplateId(),
                        new InternalAssessmentTemplateBlock("ASSESSMENT"));
            }
        }

        // check for delete
        for (Iterator<ProgrammeTemplate> iterator = existing.getTemplatesByProgramme().iterator(); iterator.hasNext(); ) {
            ProgrammeTemplate programmeTemplate = iterator.next();
            if (!programmeWithUpdates.isTemplatePresent(programmeTemplate.getTemplate().getId())) {
                // remove redundant template
                List<Project> existingProjects = projectService
                        .findAllByProgrammeAndTemplate(existing, programmeTemplate.getTemplate());
                if (existingProjects != null && existingProjects.size() > 0) {
                    throw new ValidationException("Unable to remove template '%s' as it has already been used to create projects",
                            programmeTemplate.getTemplate().getName());
                }
                iterator.remove();
                dataAccessControlService.deleteAllDefaultAccessControl(programmeTemplate.getId().getProgrammeId(),
                        programmeTemplate.getId().getTemplateId());
                auditService.auditCurrentUserActivity(String.format("Template with ID %d was removed from programme %d",
                        programmeTemplate.getTemplate().getId(), existing.getId()));
            }
        }

        existing.getTemplates().forEach(t -> t.setStatus(Template.TemplateStatus.Active));

        auditService.auditCurrentUserActivity(String.format("Programme with ID %d was edited", existing.getId()));

        return programmeRepository.save(existing);
    }

    private void updateProgrammeYear(Programme programmeWithUpdates, Programme existing) {

        existing.setYearType(programmeWithUpdates.getYearType());
        boolean yearChanged = false;
        if (ObjectUtils.compare(existing.getStartYear(), programmeWithUpdates.getStartYear()) != 0) {
            existing.setStartYear(programmeWithUpdates.getStartYear());
            yearChanged = true;
        }
        if (ObjectUtils.compare(existing.getEndYear(), programmeWithUpdates.getEndYear()) != 0) {
            existing.setEndYear(programmeWithUpdates.getEndYear());
            yearChanged = true;
        }

        if (yearChanged) {
            if (existing.isYearlyDataValid()) {
                projectService.respondToProgrammeYearChange(existing.getId(), existing.getStartYear(), existing.getEndYear());
            } else if (!existing.isYearlyDataValid()) {
                projectService.resetToTemplateYears(existing.getId());
            }
            auditService.auditCurrentUserActivity(String.format("Programme's '%s' years changed: from %d to %d",
                    existing.getName() + '(' + existing.getId() +  ')',
                    existing.getStartYear(),
                    existing.getEndYear()));
        }
    }

    private Template getInflatedTemplate(Integer templateId) {
        Template template = templateService.get(templateId);
        if (!Template.TemplateStatus.Active.equals(template.getStatus())) {
            template = templateService.inflateTemplateFromJson(template);
        }
        return template;
    }

    public void updateSupportedReports(Integer id, List<String> supportedReports) {
        Programme programme = find(id);
        programme.setSupportedReports(supportedReports);
        save(programme);
    }

    public List<ProgrammePublicProfileSummary> getPublicProgrammes() {
        return programmePublicProfileSummaryRepository
                .findAllByStatusAndEnabledAndRestricted(Programme.Status.Active, true, false);
    }

    /***
     * This method is used to grant access to all specific programme & template projects.
     * All the users of the organisation which is granted access should be able to have access
     * to the projects and see/perform actions based on their roles.
     *
     * @param programmeId the id of programme which contains the template
     * @param templateId the id of the template which will be shared with an organisation
     * @param organisationId  the id of the organisation which should receive the access
     */
    @Transactional
    public void grantOrganisationAccess(Integer programmeId, Integer templateId, Integer organisationId) {
        OrganisationEntity org = organisationService.findOne(organisationId);
        AccessControlRelationshipType type =
                org.isInternalOrganisation() ? AccessControlRelationshipType.MANAGING : AccessControlRelationshipType.ASSOCIATED;
        dataAccessControlService.insertDefaultAccessControl(programmeId, templateId, organisationId, type);
        projectService.insertProjectAccessControl(programmeId, templateId, organisationId, type);

        auditService
                .auditCurrentUserActivity(String.format("Grant access to org: %d to template: %d", organisationId, templateId));
    }

    /***
     * This method is used to remove the access granted to a template.
     * The default access control and the project access control for the template projects should be
     * removed. The users should not be able to see the projects after the access was removed.
     *
     * @param programmeId the id of programme which contains the template
     * @param templateId the id of the template which needs access to be removed
     * @param organisationId  the id of the organisation which should not have access
     */
    @Transactional
    public void removeOrganisationAccess(Integer programmeId, Integer templateId, Integer organisationId) {
        Programme programme = find(programmeId);
        Template template = templateService.find(templateId);

        // Delete the default access to don't be used for new projects
        dataAccessControlService.deleteDefaultAccessControl(programmeId, templateId, organisationId);

        // Remove org access for all projects belonging to this programme and template
        projectService.deleteProjectAccessControl(programmeId, templateId, organisationId);

        auditService
                .auditCurrentUserActivity(String.format("Removed access to org: %d to template: %d", organisationId, templateId));
    }

    public List<DefaultAccessControlSummary> getDefaultAccess(Integer programmeId) {
        return projectService.getDefaultAccessByProgrammeId(programmeId);
    }

    public Integer countByAssessmentTemplateId(Integer assessmentTemplateId) {
        return programmeRepository.countByAssessmentTemplateId(assessmentTemplateId);
    }

    public void delete(Programme programme) {
        if (environment.isTestEnvironment()) {
            int count = projectService.countAllByProgramme(programme);
            if (count > 0) {
                log.warn(String.format("%d projects still found on programme: %s", count, programme.getName()));
            } else {
                log.debug(String.format("0 projects found on programme: %s, deleting", programme.getName()));
                programmeRepository.delete(programme);
            }
        } else {
            log.warn("attempt to delete programme {} {} in a non test env", programme.getId(), programme.getName());
        }
    }

    public Set<Integer> getAssessmentTemplatesForCurrentUser(Integer programmeId, Integer templateId) {
        User user = currentUser();
        Programme programme = find(programmeId);
        Set<String> roles = user.getApprovedRolesForOrg(programme.getManagingOrganisationId());

        ProgrammeTemplate template = programme.getProgrammeTemplateByTemplateID(templateId);
        return template.getAssessmentTemplates().stream()
                .filter(at -> CollectionUtils.containsAny(at.getAllowedRoles(), roles))
                .filter(at -> !at.getNoNewAssessments())
                .map(ProgrammeTemplateAssessmentTemplate::getAssessmentTemplateId)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, String> getTemplateIdsAndNamesForProgrammes(Integer[] programmeIds) {
        Set<Template> templates = new HashSet<>();

        for (Integer programmeId : programmeIds) {
            Programme p = find(programmeId);
            templates.addAll(p.getTemplates());
        }

        return templates.stream().sorted(Comparator.comparing(Template::getName))
                .collect(Collectors.toMap(
                        t -> String.valueOf(t.getId()),
                        Template::getName,
                        (a, b) -> {
                            throw new ValidationException("Values Should be unique");
                        },
                        TreeMap::new));
    }

}
