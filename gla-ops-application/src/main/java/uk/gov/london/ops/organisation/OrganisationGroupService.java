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
import org.springframework.util.CollectionUtils;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.organisation.implementation.repository.OrganisationGroupRepository;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.organisation.model.OrganisationGroup;
import uk.gov.london.ops.programme.ProgrammeDetailsSummary;
import uk.gov.london.ops.programme.ProgrammeService;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrganisationGroupService {

    @Autowired
    OrganisationGroupRepository organisationGroupRepository;

    @Autowired
    ProjectService projectService;

    @Autowired
    OrganisationServiceImpl organisationService;

    @Autowired
    ProgrammeService programmeService;

    @Autowired
    UserServiceImpl userService;

    public List<OrganisationGroup> findAll() {
        List<OrganisationGroup> groups;
        UserEntity currentUser = userService.currentUser();
        if (currentUser.isGla()) {
            groups = filterGroupsThatGLAUserCanAccess(currentUser);
        } else {
            List<OrganisationGroup> allGroups = organisationGroupRepository.findAll();
            groups = filterGroupsThatUserCanAccess(currentUser, allGroups);
        }
        enrich(groups);
        return groups;
    }

    private void enrich(List<OrganisationGroup> groups) {
        for (OrganisationGroup group: groups) {
            enrich(group);
        }
    }

    private void enrich(OrganisationGroup group) {
        if (group.getProgrammeId() != null) {
            ProgrammeDetailsSummary programme = programmeService.getProgrammeDetailsSummary(group.getProgrammeId());
            group.setProgramme(programme);
        }
    }

    public OrganisationGroup findFirstByName(String name) {
       return organisationGroupRepository.findFirstByName(name);
    }

    public OrganisationGroup findByGroupId(Integer id) {
        return organisationGroupRepository.findById(id).orElse(null);
    }

    public List<OrganisationGroup> findAllByName(String name) {
        UserEntity currentUser = userService.currentUser();
        List<OrganisationGroup> allGroups = organisationGroupRepository.findAllByName(name).stream().collect(Collectors.toList());
        return filterGroupsThatUserCanAccess(currentUser, allGroups);
    }

    private List<OrganisationGroup> filterGroupsThatGLAUserCanAccess(UserEntity user) {
        List<OrganisationEntity> manOrganisations = user.getOrganisations()
                .stream()
                .filter(organisation -> organisation.isManaging())
                .collect(Collectors.toList());

        List<OrganisationGroup> orgGroups = new ArrayList<>();
        for (OrganisationEntity org : manOrganisations) {
            orgGroups.addAll(organisationGroupRepository.findAllByManagingOrganisation(org));
        }

        return orgGroups;
    }

    private List<OrganisationGroup> filterGroupsThatUserCanAccess(UserEntity user, List<OrganisationGroup> groups) {
        return groups.stream().filter(organisationGroup -> userHasAccess(user, organisationGroup)).collect(Collectors.toList());
    }

    public OrganisationGroup find(Integer id) {
        OrganisationGroup organisationGroup = organisationGroupRepository.findById(id).orElse(null);

        if (organisationGroup == null) {
            throw new NotFoundException();
        }

        UserEntity currentUser = userService.currentUser();
        if (!userHasAccess(currentUser, organisationGroup)) {
            throw new ForbiddenAccessException();
        }

        enrich(organisationGroup);

        return organisationGroup;
    }

    private boolean userHasAccess(UserEntity user, OrganisationGroup group) {
        return user.isGla() || CollectionUtils.containsAny(user.getOrganisations(), group.getOrganisations());
    }

    public Set<OrganisationGroup> getOrganisationGroupsByProgrammeAndOrganisation(Integer programmeId, Integer organisationId) {
        Set<OrganisationGroup> groups = new HashSet<>();
        groups.addAll(organisationGroupRepository.findAllByTypeAndProgrammeIdAndLeadOrganisationId(
                                                  OrganisationGroupType.Consortium, programmeId, organisationId));
        groups.addAll(organisationGroupRepository.findAllByTypeAndProgrammeIdAndOrganisations(OrganisationGroupType.Partnership,
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
        if (group.getProgrammeId() != null) {
            // get real programme as Programme from project may be skeleton from UI
            ProgrammeDetailsSummary programme = programmeService.getProgrammeDetailsSummary(group.getProgrammeId());
            OrganisationEntity managingOrganisation = organisationService.findOne(programme.getManagingOrganisationId());
            group.setManagingOrganisation(managingOrganisation);
        }
        group.getAllOrganisationIds().forEach(this::validateForConsortiumCreation);

        return organisationGroupRepository.save(group);
    }

    public OrganisationGroup update(Integer id, OrganisationGroup updated) {
        OrganisationGroup existing = find(id);

        UserEntity currentUser = userService.currentUser();
        if (!currentUser.isOrgAdmin(existing.getLeadOrganisation())) {
            throw new ValidationException("current user is not org admin of the lead organisation!");
        }

        if (!Objects.equals(existing.getProgrammeId(), updated.getProgrammeId())) {
            throw new ValidationException("cannot change organisation group programme!");
        }

        if (!Objects.equals(existing.getLeadOrganisationId(), updated.getLeadOrganisationId())) {
            throw new ValidationException("cannot change organisation group lead!");
        }

        Set<OrganisationEntity> deletedMembers = new HashSet<>(existing.getOrganisations());
        deletedMembers.removeAll(updated.getOrganisations());
        for (OrganisationEntity org: getGroupOrganisationsInProjects(id)) {
            if (deletedMembers.contains(org)) {
                throw new ValidationException("cannot delete member used in project!");
            }
        }

        return save(updated);
    }

    public void validateForConsortiumCreation(Integer organisationId) {
        validateForConsortiumCreation(organisationService.findOne(organisationId));
    }

    public void validateForConsortiumCreation(OrganisationEntity organisation) {
        if (organisation.isManaging()) {
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
    public List<OrganisationEntity> getGroupOrganisationsInProjects(Integer groupId) {
        OrganisationGroup group = find(groupId);
        return group.getOrganisations().stream()
                .filter(org -> !CollectionUtils.isEmpty(projectService.findAllByGroupAndOrganisation(groupId, org.getId())))
                .collect(Collectors.toList());
    }

}
