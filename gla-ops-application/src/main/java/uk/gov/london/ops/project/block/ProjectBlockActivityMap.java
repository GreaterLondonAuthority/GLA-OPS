/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.permission.PermissionService;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectOverview;
import uk.gov.london.ops.project.accesscontrol.ProjectAccessControlInterface;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.ProjectSubStatus;
import uk.gov.london.ops.user.User;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.ops.project.accesscontrol.AccessControlRelationshipType.OWNER;
import static uk.gov.london.ops.project.block.ProjectBlockAction.DELETE;
import static uk.gov.london.ops.project.block.ProjectBlockAction.EDIT;
import static uk.gov.london.ops.project.state.ProjectStateEntity.getStatusType;
import static uk.gov.london.ops.project.state.ProjectStateEntity.getSubStatusType;
import static uk.gov.london.ops.project.state.ProjectStatus.Active;
import static uk.gov.london.ops.project.state.ProjectSubStatus.PaymentAuthorisationPending;

@Component
public class ProjectBlockActivityMap {

    List<BlockActionRule> rules;

    static final String ROLE_DELIMITER = " \\| ";
    static final String ANY_MATCH = "ANY";

    /**
     * Maps rows from the CSV file into StateTransition objects.
     */
    final CSVFile.CSVMapper<BlockActionRule> csvMapper = csvRow -> new BlockActionRule(
            ProjectBlockAction.valueOf(csvRow.getString("BLOCK_ACTION")),
            ProjectStatus.valueOf(csvRow.getString("PROJECT_STATUS")),
            csvRow.getString("PROJECT_SUB_STATUS"),
            csvRow.getString("BLOCK_TYPE"),
            csvRow.getString("BLOCK_STATUS"),
            csvRow.getString("BLOCK_COMPLETION"),
            csvRow.getString("PAYMENTS_ONLY_CYCLE"),
            Arrays.asList(csvRow.getString("USER_ROLES").split(ROLE_DELIMITER)),
            csvRow.getString("PERMISSIONS")
    );

    /**
     * Load block action rules from CSV file on startup.
     */
    @PostConstruct
    void loadRules() throws IOException {
        rules = CSVFile.fromResource(this, "allowed-block-actions.csv").loadData(csvMapper);
    }

    @Autowired
    PermissionService permissionService;

    public List<ProjectBlockAction> getAllowedActionsFor(Project project, NamedProjectBlock block, User user) {
        Collection<Integer> orgIds = getUsersOrgsForProject(user, project.getAccessControlList());
        return getAllowedActionsFor(block, orgIds, user, project.getStatusType(), project.getSubStatusType(),
                project.getSubStatusName());
    }

    public List<ProjectBlockAction> getAllowedActionsFor(ProjectOverview project,
            NamedProjectBlock block,
            User user,
            Collection<? extends ProjectAccessControlInterface> projectAccessControlList) {
        Collection<Integer> orgIds = getUsersOrgsForProject(user, projectAccessControlList);
        ProjectStatus statusType = getStatusType(project.getStatusName(), project.getSubStatusName());
        ProjectSubStatus subStatusType = getSubStatusType(project.getStatusName(), project.getSubStatusName());
        return getAllowedActionsFor(block, orgIds, user, statusType, subStatusType, project.getSubStatusName());
    }

    private List<ProjectBlockAction> getAllowedActionsFor(NamedProjectBlock block,
            Collection<Integer> orgIds,
            User user,
            ProjectStatus statusType,
            ProjectSubStatus subStatusType,
            String subStatusName) {
        List<ProjectBlockAction> allowedActions = new ArrayList<>();

        for (BlockActionRule rule : rules) {
            if (matches(rule, block, orgIds, user, statusType, subStatusType, subStatusName)) {
                allowedActions.add(rule.getAction());
            }
        }
        return allowedActions;
    }

    private Collection<Integer> getUsersOrgsForProject(User user, Collection<? extends ProjectAccessControlInterface>
            projectAccessControlList) {
        Set<Integer> aclOrgIds = projectAccessControlList.stream()
                .filter(pac -> OWNER.equals(pac.getRelationshipType()))
                .map(ProjectAccessControlInterface::getOrganisationId)
                .collect(Collectors.toSet());
        return CollectionUtils.intersection(user.getOrganisationIds(), aclOrgIds);
    }

    private boolean matches(BlockActionRule rule,
            NamedProjectBlock block,
            Collection<Integer> orgIds,
            User user,
            ProjectStatus statusType,
            ProjectSubStatus subStatusType,
            String subStatusName) {
        return !blockLockedByOtherUser(block, user)
                && verifyBlockVersionedForDelete(rule, block)
                && verifyProjectHasNoPendingPaymentsForEdit(rule, subStatusType)
                && matchesStatus(rule, statusType)
                && matchesSubStatus(rule, subStatusType, subStatusName)
                && matchesBlockType(rule, block)
                && matchesBlockStatus(rule, block)
                && matchesBlockCompletion(rule, block)
                && matchesPaymentsOnlyLifecycle(rule, block)
                && matchesUserRoles(rule, orgIds, user)
                && matchesPermissions(rule, user, orgIds)
                && activeBlockCanBeEdited(statusType, block);
    }

    /**
     * We only want to allow DELETE on blocks which have previous versions: there are cases where there is only a single version
     * of a block and its status is UNAPPROVED, deleting it would then remove it completely from the project.
     */
    private boolean verifyBlockVersionedForDelete(BlockActionRule rule, NamedProjectBlock block) {
        return (!DELETE.equals(rule.getAction()) || block.getVersionNumber() > 1);
    }

    /**
     * When a project has pending payments we want to remove the ability to edit it.
     */
    private boolean verifyProjectHasNoPendingPaymentsForEdit(BlockActionRule rule, ProjectSubStatus subStatusType) {
        return !((EDIT.equals(rule.getAction()) || DELETE.equals(rule.getAction())) && PaymentAuthorisationPending
                .equals(subStatusType));
    }

    private boolean matchesStatus(BlockActionRule rule, ProjectStatus statusType) {
        return rule.getStatus().equals(statusType);
    }

    private boolean matchesSubStatus(BlockActionRule rule, ProjectSubStatus subStatusType, String subStatusName) {
        return ANY_MATCH.equals(rule.getSubStatus())
                || (StringUtils.isEmpty(rule.getSubStatus()) && subStatusName == null)
                || (StringUtils.isNotEmpty(rule.getSubStatus()) && ProjectSubStatus.valueOf(rule.getSubStatus())
                .equals(subStatusType));
    }

    private boolean matchesBlockType(BlockActionRule rule, NamedProjectBlock block) {
        return ANY_MATCH.equals(rule.getBlockType()) || ProjectBlockType.valueOf(rule.getBlockType())
                .equals(block.getBlockType());
    }

    private boolean matchesBlockStatus(BlockActionRule rule, NamedProjectBlock block) {
        return block.isLatestVersion() && (ANY_MATCH.equals(rule.getBlockStatus()) || ProjectBlockStatus
                .valueOf(rule.getBlockStatus()).equals(block.getBlockStatus()));
    }

    private boolean matchesBlockCompletion(BlockActionRule rule, NamedProjectBlock block) {
        return ANY_MATCH.equals(rule.getBlockCompletion()) || block.isComplete();
    }

    private boolean matchesPaymentsOnlyLifecycle(BlockActionRule rule, NamedProjectBlock block) {
        return ANY_MATCH.equals(rule.getPaymentsOnlyLifecycle()) ||
                !block.isHasBeenThroughPaymentsOnlyCycle();
    }

    private boolean matchesUserRoles(BlockActionRule rule, Collection<Integer> orgIds, User user) {
        if (rule.getUserRoles().contains(ANY_MATCH)) {
            return true;
        } else if (user.isGla()) {
            return CollectionUtils.containsAny(rule.getUserRoles(), user.getApprovedRolesNames());
        } else {
            for (Integer orgId : orgIds) {
                if (CollectionUtils.containsAny(rule.getUserRoles(), user.getApprovedRolesForOrg(orgId))) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean matchesPermissions(BlockActionRule rule, User user, Collection<Integer> orgIds) {
        if (user.isOpsAdmin() || ANY_MATCH.equals(rule.getPermissions())) {
            return true;
        } else {
            if (rule.getPermissions().endsWith(".")) { // meaning its org specific
                for (Integer orgId : orgIds) {
                    if (permissionService.currentUserHasPermissionForOrganisation(rule.getPermissions(), orgId)) {
                        return true;
                    }
                }
                return false;
            } else {
                return permissionService.currentUserHasPermission(rule.getPermissions());
            }
        }
    }

    private boolean activeBlockCanBeEdited(ProjectStatus statusType, NamedProjectBlock block) {
        // Blocks on active projects can only be edited if they support version control
        return !Active.equals(statusType) || block.allowMultipleVersions();
    }

    public boolean isActionAllowed(Project project, NamedProjectBlock block, User currentUser, ProjectBlockAction action) {
        return getAllowedActionsFor(project, block, currentUser).contains(action);
    }

    private boolean blockLockedByOtherUser(NamedProjectBlock block, User currentUser) {
        return (block.getLockDetails() != null) && !block.getLockDetails().getUsername().equals(currentUser.getUsername());
    }

}
