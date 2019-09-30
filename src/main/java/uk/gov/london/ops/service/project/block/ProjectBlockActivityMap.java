/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project.block;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.project.state.ProjectSubStatus;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.PermissionService;
import uk.gov.london.ops.framework.annotations.LogMetrics;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static uk.gov.london.ops.domain.project.NamedProjectBlock.Action.DELETE;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.Action.EDIT;
import static uk.gov.london.ops.domain.project.state.ProjectStatus.Active;

@Component
public class ProjectBlockActivityMap {

    List<BlockActionRule> rules;

    static final String ROLE_DELIMITER = " \\| ";
    static final String ANY_MATCH = "ANY";
    static final String OPEN_PROGRAMME = "OPEN";

    /**
     * Maps rows from the CSV file into StateTransition objects.
     */
    final CSVFile.CSVMapper<BlockActionRule> csvMapper = csvRow -> new BlockActionRule(
            NamedProjectBlock.Action.valueOf(csvRow.getString("BLOCK_ACTION")),
            ProjectStatus.valueOf(csvRow.getString("PROJECT_STATUS")),
            csvRow.getString("PROJECT_SUB_STATUS"),
            csvRow.getString("BLOCK_TYPE"),
            csvRow.getString("BLOCK_STATUS"),
            csvRow.getString("BLOCK_COMPLETION"),
            Arrays.asList(csvRow.getString("USER_ROLES").split(ROLE_DELIMITER)),
            csvRow.getString("PROGRAMME_STATE"),
            csvRow.getString("PERMISSIONS")
    );

    /**
     * Load block action rules from CSV file on startup.
     */
    @PostConstruct
    void loadRules() throws IOException {
        rules = CSVFile.fromResource(this,"allowed-block-actions.csv").loadData(csvMapper);
    }

    @Autowired
    PermissionService permissionService;

    @LogMetrics
    public List<NamedProjectBlock.Action> getAllowedActionsFor(final Project project, final NamedProjectBlock block, final User user) {
        List<NamedProjectBlock.Action> allowedActions = new ArrayList<>();
        for (BlockActionRule rule: rules) {
            if (matches(rule, project, block, user)) {
                allowedActions.add(rule.getAction());
            }
        }
        return allowedActions;
    }

    private boolean matches(BlockActionRule rule, Project project, NamedProjectBlock block, User user) {
        return !blockLockedByOtherUser(block, user)
                && verifyBlockVersionedForDelete(rule, block)
                && verifyProjectHasNoPendingPaymentsForEdit(rule, project)
                && matchesStatus(rule, project)
                && matchesSubStatus(rule, project)
                && matchesBlockType(rule, block)
                && matchesBlockStatus(rule, project, block)
                && matchesBlockCompletion(rule, block)
                && matchesUserRoles(rule, project, user)
                && matchesProgrammeState(rule, project)
                && matchesPermissions(rule, project, user)
                && activeBlockCanBeEdited(project, block);
    }

    /**
     * We only want to allow DELETE on blocks which have previous versions: there are cases where there is only a single
     * version of a block and its status is UNAPPROVED, deleting it would then remove it completely from the project.
     */
    private boolean verifyBlockVersionedForDelete(BlockActionRule rule, NamedProjectBlock block) {
        return (!DELETE.equals(rule.getAction()) || block.getVersionNumber() > 1);
    }

    /**
     * When a project has pending payments we want to remove the ability to edit it.
     */
    private boolean verifyProjectHasNoPendingPaymentsForEdit(BlockActionRule rule, Project project) {
        return !((EDIT.equals(rule.getAction()) || DELETE.equals(rule.getAction())) && project.isPendingPayments());
    }

    private boolean matchesStatus(BlockActionRule rule, Project project) {
        return rule.getStatus().equals(project.getStatusType());
    }

    private boolean matchesSubStatus(BlockActionRule rule, Project project) {
        return ANY_MATCH.equals(rule.getSubStatus())
                || (StringUtils.isEmpty(rule.getSubStatus()) && project.getSubStatusName() == null)
                || (StringUtils.isNotEmpty(rule.getSubStatus()) && ProjectSubStatus.valueOf(rule.getSubStatus()).equals(project.getSubStatusType()));
    }

    private boolean matchesBlockType(BlockActionRule rule, NamedProjectBlock block) {
        return ANY_MATCH.equals(rule.getBlockType()) || ProjectBlockType.valueOf(rule.getBlockType()).equals(block.getBlockType());
    }

    private boolean matchesBlockStatus(BlockActionRule rule, Project project, NamedProjectBlock block) {
        // TODO : double check if this exception is needed
        // Last approved blocks can be edited if there is no unapproved version,
        // as this automatically creates a new unapproved version.
        NamedProjectBlock latestBlock = project.getLatestBlockOfType(block.getBlockType(), block.getDisplayOrder());
        if (latestBlock != null && !Objects.equals(latestBlock.getVersionNumber(), block.getVersionNumber())) {
            return false;
        }
        return ANY_MATCH.equals(rule.getBlockStatus()) || NamedProjectBlock.BlockStatus.valueOf(rule.getBlockStatus()).equals(block.getBlockStatus());
    }

    private boolean matchesBlockCompletion(BlockActionRule rule, NamedProjectBlock block) {
        return ANY_MATCH.equals(rule.getBlockCompletion()) || block.isComplete();
    }

    private boolean matchesUserRoles(BlockActionRule rule, Project project, User user) {
        if (rule.getUserRoles().contains(ANY_MATCH)) {
            return true;
        }
        else if (user.isGla()) {
            return CollectionUtils.containsAny(rule.getUserRoles(), user.getApprovedRolesNames());
        }
        else {
            return CollectionUtils.containsAny(rule.getUserRoles(), user.getApprovedRolesForOrgs(project.getOrganisation()));
        }
    }

    private boolean matchesProgrammeState(BlockActionRule rule, Project project) {
        return ANY_MATCH.equals(rule.getProgrammeState()) || (OPEN_PROGRAMME.equals(rule.getProgrammeState()) && project.getProgramme().isEnabled());
    }

    private boolean matchesPermissions(BlockActionRule rule, Project project, User user) {
        if (user.isOpsAdmin() || ANY_MATCH.equals(rule.getPermissions())) {
            return true;
        }
        else {
            if (rule.getPermissions().endsWith(".")) { // meaning its org specific
                return permissionService.currentUserHasPermissionForOrganisation(rule.getPermissions(), project.getOrganisation().getId());
            }
            else {
                return permissionService.currentUserHasPermission(rule.getPermissions());
            }
        }
    }

    private boolean activeBlockCanBeEdited(Project project, NamedProjectBlock block) {
        // Blocks on active projects can only be edited if they support version control
        return !Active.equals(project.getStatusType()) || block.allowMultipleVersions();
    }

    public boolean isActionAllowed(Project project, NamedProjectBlock block, User currentUser, NamedProjectBlock.Action action) {
        return getAllowedActionsFor(project, block, currentUser).contains(action);
    }

    private boolean blockLockedByOtherUser(NamedProjectBlock block, User currentUser) {
        return (block.getLockDetails() != null) && ! block.getLockDetails().getUsername().equals(currentUser.getUsername());
    }

}
