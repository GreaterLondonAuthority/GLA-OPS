/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.domain.importdata.ImportJobType;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.organisation.implementation.repository.OrganisationGroupRepository;
import uk.gov.london.ops.organisation.implementation.repository.OrganisationRepository;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.organisation.model.OrganisationGroup;
import uk.gov.london.ops.programme.ProgrammeService;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.service.ImportLogService;
import uk.gov.london.ops.user.UserService;
import uk.gov.london.ops.user.domain.User;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrganisationGroupService {

    public static final String ORG_NAME = "Name for consortium or partnership";
    public static final String PROGRAMME = "Programme";
    public static final String ORG_GROUP_TYPE = "Agreement type";
    public static final String LEAD_ORG = "Lead organisation OPS code";
    public static final String DEV_ORGS = "Developing organisation OPS code";

    @Autowired
    OrganisationGroupRepository organisationGroupRepository;

    @Autowired
    ProjectService projectService;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    ProgrammeService programmeService;

    @Autowired
    UserService userService;

    @Autowired
    ImportLogService importLogService;

    Logger log = LoggerFactory.getLogger(getClass());

    public List<OrganisationGroup> getAllOrganisationGroups() {
       return organisationGroupRepository.findAll();
    }

    public List<OrganisationGroup> findAll() {
        User currentUser = userService.currentUser();
        if (currentUser.isGla()) {
            return filterGroupsThatGLAUserCanAccess(currentUser);
        } else {
            List<OrganisationGroup> allGroups = organisationGroupRepository.findAll();
            return filterGroupsThatUserCanAccess(currentUser, allGroups);
        }
    }

    public OrganisationGroup findFirstByName(String name) {
       return organisationGroupRepository.findFirstByName(name);
    }

    public OrganisationGroup findByGroupId(Integer id) {
        return organisationGroupRepository.findById(id).orElse(null);
    }

    public List<OrganisationGroup> findAllByName(String name) {
        User currentUser = userService.currentUser();
        List<OrganisationGroup> allGroups = organisationGroupRepository.findAllByName(name).stream().collect(Collectors.toList());
        return filterGroupsThatUserCanAccess(currentUser, allGroups);
    }

    private List<OrganisationGroup> filterGroupsThatGLAUserCanAccess(User user) {
        List<Organisation> manOrganisations = user.getOrganisations()
                .stream()
                .filter(organisation -> organisation.isManagingOrganisation())
                .collect(Collectors.toList());

        List<OrganisationGroup> orgGroups = new ArrayList<>();
        for (Organisation org : manOrganisations) {
            orgGroups.addAll(organisationGroupRepository.findAllByManagingOrganisation(org));
        }

        return orgGroups;
    }

    private List<OrganisationGroup> filterGroupsThatUserCanAccess(User user, List<OrganisationGroup> groups) {
        return groups.stream().filter(organisationGroup -> userHasAccess(user, organisationGroup)).collect(Collectors.toList());
    }

    public OrganisationGroup find(Integer id) {
        OrganisationGroup organisationGroup = organisationGroupRepository.findById(id).orElse(null);

        if (organisationGroup == null) {
            throw new NotFoundException();
        }

        User currentUser = userService.currentUser();
        if (!userHasAccess(currentUser, organisationGroup)) {
            throw new ForbiddenAccessException();
        }

        return organisationGroup;
    }

    private boolean userHasAccess(User user, OrganisationGroup group) {
        return user.isGla() || CollectionUtils.containsAny(user.getOrganisations(), group.getOrganisations());
    }

    public Set<OrganisationGroup> getOrganisationGroupsByProgrammeAndOrganisation(Integer programmeId, Integer organisationId) {
        Set<OrganisationGroup> groups = new HashSet<>();
        groups.addAll(organisationGroupRepository.findAllByTypeAndProgrammeIdAndLeadOrganisationId(
                                                  OrganisationGroup.Type.Consortium, programmeId, organisationId));
        groups.addAll(organisationGroupRepository.findAllByTypeAndProgrammeIdAndOrganisations(OrganisationGroup.Type.Partnership,
                                                  programmeId, organisationService.findOne(organisationId)));
        return groups;
    }

    public OrganisationGroup save(OrganisationGroup group) {
        if (group.getOrganisations() == null || group.getOrganisations().isEmpty()) {
            throw new ValidationException("Cannot create a group with no organisations");
        }

        if (group.getLeadOrganisationId() != null
                && !group.getOrganisations()
                        .stream()
                        .filter(o -> o.getId().equals(group.getLeadOrganisationId()))
                        .findAny()
                        .isPresent()) {
            throw new ValidationException("Lead organisation should be part of the organisation list!");
        }

        // set managing org to programme managing org
        if (group.getProgramme() != null) {
            // get real programme as Programme from project may be skeleton from UI
            Programme programme = programmeService.find(group.getProgramme().getId());
            Organisation managingOrganisation = programme.getManagingOrganisation();
            group.setManagingOrganisation(managingOrganisation);
        }
        group.getAllOrganisationIds().forEach(this::validateForConsortiumCreation);

        return organisationGroupRepository.save(group);
    }

    public OrganisationGroup update(Integer id, OrganisationGroup updated) {
        OrganisationGroup existing = find(id);

        User currentUser = userService.currentUser();
        if (!currentUser.isOrgAdmin(existing.getLeadOrganisation())) {
            throw new ValidationException("current user is not org admin of the lead organisation!");
        }

        if (!Objects.equals(existing.getProgramme(), updated.getProgramme())) {
            throw new ValidationException("cannot change organisation group programme!");
        }

        if (!Objects.equals(existing.getLeadOrganisationId(), updated.getLeadOrganisationId())) {
            throw new ValidationException("cannot change organisation group lead!");
        }

        Set<Organisation> deletedMembers = new HashSet<>(existing.getOrganisations());
        deletedMembers.removeAll(updated.getOrganisations());
        for (Organisation org: getGroupOrganisationsInProjects(id)) {
            if (deletedMembers.contains(org)) {
                throw new ValidationException("cannot delete member used in project!");
            }
        }

        return save(updated);
    }

    public void validateForConsortiumCreation(Integer organisationId) {
        validateForConsortiumCreation(organisationService.findOne(organisationId));
    }

    public void validateForConsortiumCreation(Organisation organisation) {
        if (organisation.isManagingOrganisation()) {
            throw new ValidationException("GLA cannot be part of a consortium or partnership");
        }

        if (!organisation.isApproved()) {
            throw new ValidationException("Unapproved organisations cannot be part of a consortium or partnership");
        }
    }

    public void delete(Integer id) {
        organisationGroupRepository.deleteById(id);
    }

    /**
     * @return a list of organisations which have created or are developers of projects within the given organisation group.
     */
    public List<Organisation> getGroupOrganisationsInProjects(Integer groupId) {
        OrganisationGroup group = find(groupId);
        return group.getOrganisations().stream()
                .filter(org -> !CollectionUtils.isEmpty(projectService.findAllByGroupAndOrganisation(groupId, org.getId())))
                .collect(Collectors.toList());
    }

    public int importOrganisationGroups(CSVFile csvFile, int maxRows) {
        int importCount = 0;

        while (csvFile.nextRow()) {
            if (++importCount > maxRows) {
                log.warn("Aborting import after {} rows", (importCount - 1));
                break;
            }

            try {
                String programmeName = csvFile.getString(PROGRAMME);
                Programme programme = programmeService.findByName(programmeName);
                if (programme == null) {
                    importLogService.recordError(ImportJobType.ORGANISATION_GROUP_IMPORT,
                            "Unable to find programme with name: " + programmeName, csvFile.getRowIndex(),
                            csvFile.getCurrentRowSource());
                    continue;
                }

                String orgName = csvFile.getString(ORG_NAME);

                Set<OrganisationGroup> allOrgs = organisationGroupRepository.findAllByName(orgName);



                boolean failed = false;
                for (OrganisationGroup allOrg : allOrgs) {
                    if (allOrg.getProgramme().equals(programme)) {
                        importLogService.recordError(ImportJobType.ORGANISATION_GROUP_IMPORT,
                                "Consortium " + orgName + " already exists for this programme " + programmeName,
                                csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                        failed  = true;
                    }
                }
                if (failed) {
                    continue;
                }

                OrganisationGroup group = new OrganisationGroup();
                group.setName(csvFile.getString(ORG_NAME));
                group.setProgramme(programme);
                group.setManagingOrganisation(programme.getManagingOrganisation());
                String leadOrdId = csvFile.getString(LEAD_ORG);
                Organisation leadOrg = findSingleByImsNumber(leadOrdId, csvFile);

                if (leadOrg == null) {
                    importLogService.recordError(ImportJobType.ORGANISATION_GROUP_IMPORT,
                            "Consortium Lead Org not found" + leadOrdId, csvFile.getRowIndex(),
                            csvFile.getCurrentRowSource());
                    continue;
                }

                group.setLeadOrganisationId(leadOrg.getId());

                group.setType(OrganisationGroup.Type.valueOf(csvFile.getString(ORG_GROUP_TYPE)));

                String devOrgs = csvFile.getString(DEV_ORGS);
                String[] split = devOrgs.split("\\,");
                group.setOrganisations(new HashSet<>());
                group.getOrganisations().add(leadOrg);
                for (String devOrg : split) {
                    Organisation devOrganisation = this.findSingleByImsNumber(devOrg, csvFile);
                    if (devOrganisation != null) {
                        group.getOrganisations().add(devOrganisation);
                    }
                }

                if ((split.length + 1) == group.getOrganisations().size()) {
                    organisationGroupRepository.save(group);
                } else {
                    importLogService.recordError(ImportJobType.ORGANISATION_GROUP_IMPORT,
                            "Not all dev orgs were found for consortium: " + group.getName(), csvFile.getRowIndex(),
                            csvFile.getCurrentRowSource());
                }
            } catch (Exception e) {
                log.error("Error in import Consortium import:  " + e.getMessage(), e);
                try {
                    importLogService.recordError(ImportJobType.ORGANISATION_GROUP_IMPORT, "Error: " + e.getMessage(),
                            csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } catch (IOException e1) {
                    log.error("Error with writing CSV details during error logging:  " + e.getMessage());
                }
            }
        }
        return importCount;
    }

    private Organisation findSingleByImsNumber(String imsNumber, CSVFile csvFile) throws IOException {
        Set<Organisation> allByImsNumber = organisationRepository.findAllByImsNumber(imsNumber.trim());

        if (allByImsNumber == null || allByImsNumber.isEmpty()) {
            importLogService.recordError(ImportJobType.ORGANISATION_GROUP_IMPORT,
                    "Organisation with code: "  +  imsNumber + " not found ", csvFile.getRowIndex(),
                    csvFile.getCurrentRowSource());
        } else if (allByImsNumber.size() > 1) {
            importLogService.recordError(ImportJobType.ORGANISATION_GROUP_IMPORT,
                    "More than one organisation with code: "  +  imsNumber + " found ", csvFile.getRowIndex(),
                    csvFile.getCurrentRowSource());
        } else {
            return allByImsNumber.iterator().next();
        }
        return null;
    }
}

