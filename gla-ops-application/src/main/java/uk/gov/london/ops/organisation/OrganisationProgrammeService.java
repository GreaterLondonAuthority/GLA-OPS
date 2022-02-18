/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.organisation.implementation.repository.*;
import uk.gov.london.ops.organisation.model.*;
import uk.gov.london.ops.programme.ProgrammeDetailsSummary;
import uk.gov.london.ops.programme.ProgrammeServiceImpl;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.block.ProjectBlockStatus;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.grant.IndicativeGrantBlock;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.ProjectSubStatus;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.refdata.TenureType;
import uk.gov.london.ops.user.UserService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.addBigDecimals;
import static uk.gov.london.ops.organisation.model.OrganisationBudgetEntry.Type.Initial;

@Transactional
@Service
public class OrganisationProgrammeService {

    @Autowired
    private OrganisationBudgetEntryRepository organisationBudgetEntryRepository;

    @Autowired
    private OrganisationProgrammeRepository organisationProgrammeRepository;

    @Autowired
    private OrganisationProgrammeSummaryRepository organisationProgrammeSummaryRepository;

    @Autowired
    private RequestedAndPaidRecordRepository requestedAndPaidRecordRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private ProgrammeServiceImpl programmeService;

    @Autowired
    private AssociatedProjectsRecordRepository associatedProjectsRecordRepository;

    @Autowired
    private AssociatedProjectRequestedAndSOSRecordRepository associatedProjectRequestedAndSOSRecordRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private Environment environment;

    @Autowired
    private UserService userService;

    public OrganisationProgramme getOrganisationProgramme(Integer organisationId, Integer programmeId) {
        OrganisationProgramme organisationProgramme =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);

        if (organisationProgramme == null) {
            organisationProgramme = new OrganisationProgramme();
            organisationProgramme.setId(new ProgrammeOrganisationID(programmeId, organisationId));
        }

        return populateOrganisationProgrammeData(organisationProgramme);
    }

    private OrganisationProgramme populateOrganisationProgrammeData(OrganisationProgramme organisationProgramme) {
        Integer programmeId = organisationProgramme.getId().getProgrammeId();
        ProgrammeDetailsSummary programme = programmeService.getProgrammeDetailsSummary(programmeId);
        organisationProgramme.setProgramme(programme);

        organisationProgramme.setBudgetEntries(getBudgetEntries(organisationProgramme.getId().getOrgId(),
                organisationProgramme.getId().getProgrammeId()));

        return organisationProgramme;
    }

    public ProgrammeRequestedAndPaidRecord getRequestedAndPaidRecord(Integer programmeId, Integer organisationId) {

        Programme programme = programmeService.find(programmeId);
        Organisation organisation = organisationService.findOne(organisationId);

        if (programme == null || organisation == null) {
            throw new ValidationException("Unrecognised organisation or programme " + programmeId + " " + organisationId);
        }

        RequestedAndPaidRecord strategic = requestedAndPaidRecordRepository.findById(new RequestedAndPaidRecordID(programmeId,
                organisationId, true)).orElse(null);
        RequestedAndPaidRecord nonStrategic = requestedAndPaidRecordRepository.findById(new RequestedAndPaidRecordID(programmeId,
                organisationId, false)).orElse(null);
        AssociatedProjectsRecord associatedProjectsRecord = associatedProjectsRecordRepository.findById(
                new ProgrammeOrganisationID(programmeId, organisationId)).orElse(null);
        Set<AssociatedProjectRequestedAndSOSRecord> associatedRecords =
                associatedProjectRequestedAndSOSRecordRepository.findAllByProgrammeIdAndOrgId(programmeId, organisationId);

        OrganisationProgramme orgProg =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);

        if (orgProg != null && orgProg.isStrategicPartnership()) {

            Set<TenureType> tenureTypes = new HashSet<>();
            for (Template template : programme.getTemplates()) {
                tenureTypes.addAll(template.getTenureTypes().stream().map(t -> t.getTenureType()).collect(Collectors.toSet()));
            }

            for (TenureType tenure : tenureTypes) {
                Optional<AssociatedProjectRequestedAndSOSRecord> first = associatedRecords
                        .stream()
                        .filter(r -> r.getTenureTypeExtId().equals(tenure.getId()))
                        .findFirst();
                if (!first.isPresent()) {
                    AssociatedProjectRequestedAndSOSRecord associatedProjectRequestedAndSOSRecord =
                            new AssociatedProjectRequestedAndSOSRecord();
                    associatedProjectRequestedAndSOSRecord.setOrgId(organisationId);
                    associatedProjectRequestedAndSOSRecord.setProgrammeId(programmeId);
                    associatedProjectRequestedAndSOSRecord.setTenureTypeExtId(tenure.getId());
                    associatedProjectRequestedAndSOSRecord.setTenureTypeName(tenure.getName());
                    associatedRecords.add(associatedProjectRequestedAndSOSRecord);
                }
            }

            if (orgProg.getPlannedUnits() != null && orgProg.getPlannedUnits().size() > 0) {
                for (StrategicPlannedUnitsForTenure tenure : orgProg.getPlannedUnits()) {
                    Optional<AssociatedProjectRequestedAndSOSRecord> first = associatedRecords
                            .stream()
                            .filter(r -> r.getTenureTypeExtId().equals(tenure.getTenureType()))
                            .findFirst();
                    if (first.isPresent()) {
                        first.get().setUnitsPlanned(tenure.getUnitsPlanned());
                    }
                }
            }
        }


        List<Project> indicatives = new ArrayList<>();
        Set<Template> templates = programme.getTemplates();

        for (Template template : templates) {
            if (!template.getBlocksByType(ProjectBlockType.IndicativeGrant).isEmpty()) {
                indicatives.addAll(projectService.findAllByProgrammeAndTemplateAndOrganisation(programme, template, organisation.getId()));
            }
        }

        if (indicatives.size() > 0) {
            if (nonStrategic == null) {
                nonStrategic = new RequestedAndPaidRecord();
            }
            updateIndicativeCostsForOrganisationProgramme(indicatives, nonStrategic);
        }


        // if not strategic response then return skeleton for UI
        if (strategic == null) {
            if (orgProg != null && orgProg.isStrategicPartnership()) {
                strategic = new RequestedAndPaidRecord(new RequestedAndPaidRecordID(programmeId, organisationId,
                        true));

            }
        }

        if (strategic != null && associatedProjectsRecord != null) {
            if (strategic.getTotalRequested() != null) {
                long requestedVariance = strategic.getTotalRequested() - associatedProjectsRecord.getStrategicRequested();
                associatedProjectsRecord.setRequestedVariance(requestedVariance);
            }
            if (strategic.getTotalPaid() != null) {
                long sosVariance = strategic.getTotalPaid() - associatedProjectsRecord.getStartedOnSite();
                associatedProjectsRecord.setVarianceBetweenPaidAndSoSClaimed(sosVariance);
            }
        }
        return new ProgrammeRequestedAndPaidRecord(strategic, nonStrategic, associatedProjectsRecord,
                new StrategicPartnershipUnitSummary(associatedRecords));
    }

    public OrganisationBudgetEntry saveBudgetEntry(Integer organisationId, Integer programmeId, OrganisationBudgetEntry entry) {
        entry.setOrganisationId(organisationId);
        entry.setProgrammeId(programmeId);

        validateBudgetEntry(entry);

        if (Initial.equals(entry.getType())) {
            OrganisationBudgetEntry existingInitialEntry = findExistingInitialBudgetEntry(entry);
            if (existingInitialEntry != null) {
                auditService.auditCurrentUserActivity(String.format("Initial %s%s approval value changed from %s to %s",
                        entry.isStrategic() ? "Strategic " : "", entry.getGrantType(), existingInitialEntry.getAmount(),
                        entry.getAmount()));
                existingInitialEntry.setAmount(entry.getAmount());
                entry = existingInitialEntry;
            }
        } else {
            entry.setType(OrganisationBudgetEntry.Type.Additional);
        }

        return organisationBudgetEntryRepository.save(entry);
    }

    public void deleteBudgetEntry(Integer entryId) {
        OrganisationBudgetEntry entry = organisationBudgetEntryRepository.getOne(entryId);

        if (Initial.equals(entry.getType())) {
            throw new ValidationException("cannot delete an Initial approval entry!");
        }

        auditService.auditCurrentUserActivity(String.format("deleted budget entry for organisation %d programme %d with value %s",
                entry.getOrganisationId(), entry.getProgrammeId(), entry.getAmount()));

        organisationBudgetEntryRepository.deleteById(entryId);
    }

    public void updateOrganisationProgramme(Integer organisationId, Integer programmeId,
                                            OrganisationProgramme organisationProgramme) {
        organisationProgramme.setId(new ProgrammeOrganisationID(programmeId, organisationId));
        organisationProgrammeRepository.save(organisationProgramme);
    }

    public ProgrammeRequestedAndPaidRecord updatePlannedUnits(Integer organisationId, Integer programmeId, Integer tenureExtId,
                                                              Integer plannedUnits) {
        OrganisationProgramme orgProgramme =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);

        if (orgProgramme == null) {
            throw new ValidationException("Unable to find record for requested Programme and Organisation");
        }

        Set<StrategicPlannedUnitsForTenure> plannedUnitsList = orgProgramme.getPlannedUnits();
        if (plannedUnitsList == null) {
            orgProgramme.setPlannedUnits(new HashSet<>());
        }

        Optional<StrategicPlannedUnitsForTenure> first = orgProgramme.getPlannedUnits().stream().filter(
                p -> p.getProgrammeId().equals(programmeId)
                        && p.getOrgId().equals(organisationId)
                        && p.getTenureType().equals(tenureExtId)).findFirst();

        if (first.isPresent()) {
            first.get().setUnitsPlanned(plannedUnits);
        } else {
            orgProgramme.getPlannedUnits().add(new StrategicPlannedUnitsForTenure(programmeId, organisationId, tenureExtId,
                    plannedUnits));
        }

        organisationProgrammeRepository.saveAndFlush(orgProgramme);
        return getRequestedAndPaidRecord(programmeId, organisationId);
    }

    public ProgrammeRequestedAndPaidRecord deletePlannedUnits(Integer organisationId, Integer programmeId, Integer tenureExtId) {
        OrganisationProgramme orgProgramme =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);

        if (orgProgramme == null) {
            throw new ValidationException("Unable to find record for requested Programme and Organisation");
        }

        boolean removed = orgProgramme.getPlannedUnits().removeIf(
                p -> p.getProgrammeId().equals(programmeId)
                        && p.getOrgId().equals(organisationId)
                        && p.getTenureType().equals(tenureExtId));

        if (!removed) {
            throw new ValidationException("Unable to find record for requested Programme and Organisation");
        }

        organisationProgrammeRepository.saveAndFlush(orgProgramme);
        return getRequestedAndPaidRecord(programmeId, organisationId);
    }

    public List<OrganisationProgrammeSummary> getProgrammes(Integer organisationId) {
        return organisationProgrammeSummaryRepository.findAllForOrganisation(organisationId);
    }

    /**
     * Return true if the organisation is marked as strategic for that given programme
     */
    public boolean isStrategic(Integer organisationId, Integer programmeId) {
        OrganisationProgramme organisationProgramme =
                organisationProgrammeRepository.findById(new ProgrammeOrganisationID(programmeId, organisationId))
                        .orElse(null);
        return organisationProgramme != null && organisationProgramme.isStrategicPartnership();
    }

    private List<OrganisationBudgetEntry> getBudgetEntries(Integer organisationId, Integer programmeId) {
        List<OrganisationBudgetEntry> budgetEntries = organisationBudgetEntryRepository.findAllByOrganisationIdAndProgrammeId(
                organisationId, programmeId);
        for (OrganisationBudgetEntry budgetEntry: budgetEntries) {
            userService.enrich(budgetEntry);
        }
        return budgetEntries;
    }

    private void validateBudgetEntry(OrganisationBudgetEntry entry) {
        if (entry.getApprovedOn() != null && entry.getApprovedOn().isAfter(environment.now().toLocalDate())) {
            throw new ValidationException("approval date cannot be in the future!");
        }

        if (entry.isStrategic()) {
            OrganisationProgramme organisationProgramme = organisationProgrammeRepository.findById(new ProgrammeOrganisationID(
                    entry.getProgrammeId(), entry.getOrganisationId())).orElse(null);
            if (organisationProgramme == null || !organisationProgramme.isStrategicPartnership()) {
                throw new ValidationException(
                        "cannot save a strategic budget entry if the organisation programme is not marked as strategic!");
            }
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (OrganisationBudgetEntry existingEntry : organisationBudgetEntryRepository.findAllLike(entry)) {
            sum = addBigDecimals(sum, existingEntry.getAmount());
        }
        sum = addBigDecimals(sum, entry.getAmount());
        if (sum.signum() == -1) {
            throw new ValidationException("the budget cannot be reduced by more than the current allocation");
        }
    }

    private OrganisationBudgetEntry findExistingInitialBudgetEntry(OrganisationBudgetEntry entry) {
        return organisationBudgetEntryRepository.findInitial(entry.getOrganisationId(), entry.getProgrammeId(),
                entry.getGrantType(), entry.isStrategic());
    }

    // this should be in project service but autowiring causes issues.
    private void updateIndicativeCostsForOrganisationProgramme(List<Project> indicatives, RequestedAndPaidRecord nonStrategic) {
        Long totalIndicativeGrantRequested = null;
        Long totalIndicativeGrantApproved = null;

        for (Project indicative : indicatives) {
            Long indicativeGrantApproved = getIndicativeGrantApproved(indicative);
            if (indicativeGrantApproved != null) {
                totalIndicativeGrantApproved = totalIndicativeGrantApproved == null ? indicativeGrantApproved :
                        totalIndicativeGrantApproved + indicativeGrantApproved;
            }

            Long indicativeGrantRequested = getIndicativeGrantRequested(indicative);
            // default requested to approved
            if (indicativeGrantRequested != null) {
                totalIndicativeGrantRequested = totalIndicativeGrantRequested == null ? indicativeGrantRequested :
                        totalIndicativeGrantRequested + indicativeGrantRequested;
            } else if (indicativeGrantApproved != null) {
                totalIndicativeGrantRequested = totalIndicativeGrantRequested == null ? indicativeGrantApproved :
                        totalIndicativeGrantApproved + indicativeGrantApproved;
            }
        }

        if (nonStrategic != null) {
            nonStrategic.setIndicativeGrantApproved(totalIndicativeGrantApproved);
            nonStrategic.setIndicativeGrantRequested(totalIndicativeGrantRequested);
            nonStrategic.setTotalApproved(nonStrategic.getTotalApproved() + (totalIndicativeGrantApproved != null
                    ? totalIndicativeGrantApproved : 0));
            nonStrategic.setTotalRequested(nonStrategic.getTotalRequested() + (totalIndicativeGrantRequested != null
                    ? totalIndicativeGrantRequested : 0));
        }
    }

    private Long getIndicativeGrantRequested(Project indicative) {
        Long indicativeGrantRequested = null;
        IndicativeGrantBlock indicativeBlock = (IndicativeGrantBlock) indicative.getSingleLatestBlockOfType(
                ProjectBlockType.IndicativeGrant);
        List<ProjectStatus> singleStatus = new ArrayList<ProjectStatus>() {{
            add(ProjectStatus.Assess);
        }};
        List<ProjectStatus> requiresSubStatus = new ArrayList<ProjectStatus>() {{
            add(ProjectStatus.Active);
        }};
        List<ProjectSubStatus> requestedSubStatus = new ArrayList<ProjectSubStatus>() {{
            add(ProjectSubStatus.ApprovalRequested);
            add(ProjectSubStatus.PaymentAuthorisationPending);
        }};

        if (ProjectBlockStatus.UNAPPROVED.equals(indicativeBlock.getBlockStatus())) {
            if (singleStatus.contains(indicative.getStatusType()) || (requiresSubStatus.contains(indicative.getStatusType())
                    && requestedSubStatus.contains(indicative.getSubStatusType()))) {
                indicativeGrantRequested = indicativeBlock.getTotalGrantEligibility();
            }
        }
        return indicativeGrantRequested;
    }

    private Long getIndicativeGrantApproved(Project indicative) {
        Long indicativeGrantApproved = null;
        List<ProjectStatus> singleStatus = new ArrayList<ProjectStatus>() {{
            add(ProjectStatus.Active);
        }};
        List<ProjectStatus> requiresSubStatus = new ArrayList<ProjectStatus>() {{
            add(ProjectStatus.Closed);
        }};
        List<ProjectSubStatus> requestedSubStatus = new ArrayList<ProjectSubStatus>() {{
            add(ProjectSubStatus.Completed);
        }};

        if (singleStatus.contains(indicative.getStatusType()) || (requiresSubStatus.contains(indicative.getStatusType())
                && requestedSubStatus.contains(indicative.getSubStatusType()))) {
            IndicativeGrantBlock indicativeBlock =
                    (IndicativeGrantBlock) indicative.getLatestApprovedBlock(ProjectBlockType.IndicativeGrant);
            indicativeGrantApproved = indicativeBlock.getTotalGrantEligibility();
        }
        return indicativeGrantApproved;
    }

}
