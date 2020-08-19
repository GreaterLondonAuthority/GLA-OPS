/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.permission;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.permission.PermissionType.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import uk.gov.london.common.organisation.OrganisationType;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.permission.implementation.PermissionDetails;
import uk.gov.london.ops.permission.implementation.PermissionDetails.PermissionApplicability;
import uk.gov.london.ops.permission.implementation.ProjectAccessControlSummaryRepository;
import uk.gov.london.ops.project.accesscontrol.ProjectAccessControlSummary;
import uk.gov.london.ops.role.RoleService;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.role.model.RoleNameAndDescription;
import uk.gov.london.ops.user.UserService;
import uk.gov.london.ops.user.domain.User;

@Service
public class PermissionService {

    public static final String MY_ORG_ID_PLACEHOLDER = "[MY_ORG_ID]";

    public static final String PERMISSION_ALL = "*";

    final FeatureStatus featureStatus;
    final RoleService roleService;
    final UserService userService;
    final ProjectAccessControlSummaryRepository projectAccessControlSummaryRepository;

    public PermissionService(FeatureStatus featureStatus, RoleService roleService, UserService userService,
                             ProjectAccessControlSummaryRepository projectAccessControlSummaryRepository) {
        this.featureStatus = featureStatus;
        this.roleService = roleService;
        this.userService = userService;
        this.projectAccessControlSummaryRepository = projectAccessControlSummaryRepository;
    }

    public Set<String> getPermissionsForUser(User user) {
        Set<String> permissions = new HashSet<>();

        Set<Role> userApprovedRoles = user.getApprovedRoles();

        if (!user.isApproved() || userApprovedRoles.isEmpty()) {
            permissions.addAll(unapproved_user_permissions);
        }

        for (Role role : userApprovedRoles) {
            permissions.addAll(getRolePermissions(role));
            if (role.getName().equals("ROLE_ORG_ADMIN") && role.getOrganisation().getEntityType()
                    .equals(OrganisationType.TECHNICAL_SUPPORT.id())) {
                permissions.add(SYS_DASHBOARD.getPermissionKey());
            }
        }
        if (!featureStatus.isEnabled(Feature.Payments)) {
            permissions.removeIf(p -> p.startsWith(PAYMENTS.getPermissionKey()));
            permissions.removeIf(p -> p.startsWith(AUTHORISE_PAYMENT.getPermissionKey()));
        }

        if (!featureStatus.isEnabled(Feature.AllowNonGLAReportingAccess) && !user.isGla()) {
            permissions.removeIf(p -> p.startsWith(REPORTS_TAB.getPermissionKey()));
        }

        return permissions;
    }

    private Set<String> getRolePermissions(Role role) {
        String orgId = String.valueOf(role.getOrganisation().getId());
        return permissions_map.get(role.getName()).stream()
                .map(permission -> permission.replace(MY_ORG_ID_PLACEHOLDER, orgId))
                .collect(Collectors.toSet());
    }

    private static final Set<String> ops_admin_permissions = new HashSet<>(Arrays.asList(
            ASSESSMENT_TEMPLATE_MANAGE.getPermissionKey(),
            ORG_MANAGE_APPROVE.getPermissionKey(),
            PAYMENTS_RESEND.getPermissionKey(),

            ORG_VIEW_DETAILS.applyingFor(PERMISSION_ALL),
            ORG_VIEW_USERS.applyingFor(PERMISSION_ALL),

            ORG_EDIT_DETAILS.applyingFor(PERMISSION_ALL),
            ORG_EDIT_GOVERNANCE.getPermissionKey(),
            ORG_EDIT_NAME.getPermissionKey(),
            ORG_EDIT_VENDOR_SAP_ID.getPermissionKey(),
            ORG_EDIT_TYPE.getPermissionKey(),
            ORG_EDIT_MANAGING_ORG.getPermissionKey(),
            ORG_EDIT_BUDGET.applyingFor(PERMISSION_ALL),
            ORG_EDIT_ANY_ROLE.getPermissionKey(),
            ORG_EDIT_TEAM.getPermissionKey(),
            ORG_EDIT_PARENT_ORG.getPermissionKey(),
            ORG_EDIT_REGISTRATION_KEY.applyingFor(PERMISSION_ALL),

            TEAM_ADD.getPermissionKey(),
            TEAM_EDIT.applyingFor(PERMISSION_ALL),

            USER_ADD.applyingFor(PERMISSION_ALL),
            USER_PERMISSIONS.applyingFor(PERMISSION_ALL),
            USER_REMOVE.applyingFor(PERMISSION_ALL),
            USER_APPROVE.applyingFor(PERMISSION_ALL),
            USER_INVITE.applyingFor(PERMISSION_ALL),
            USER_SET_PENDING_THRESHOLD.getPermissionKey(),
            USER_CHANGE_STATUS.getPermissionKey(),
            USERS_ASSIGN_PRIMARY.getPermissionKey(),

            PROG_MANAGE.getPermissionKey(),
            PROG_MANAGE_CE_CODE.getPermissionKey(),
            TEMP.getPermissionKey(),
            TEMP_MANAGE.getPermissionKey(),

            PROJ_EDIT.applyingFor(PERMISSION_ALL),
            PROJ_REVERT_OR_DELETE_BLOCK.applyingFor(PERMISSION_ALL),
            PROJ_SUBMIT.applyingFor(PERMISSION_ALL),
            PROJ_REINSTATE.getPermissionKey(),
            PROJ_APPROVE.getPermissionKey(),
            PROJ_ABANDON.getPermissionKey(),
            PROJ_REJECT.getPermissionKey(),
            PROJ_DOC_DELETE.getPermissionKey(),
            PROJ_WBS_DELETE.getPermissionKey(),
            PROJ_OUTPUTS_EDIT_PAST.applyingFor(PERMISSION_ALL),
            PROJ_OUTPUTS_EDIT_FUTURE.applyingFor(PERMISSION_ALL),
            PROJ_LEDGER_ACTUAL_CREATE.getPermissionKey(),
            PROJ_TRANSFER.getPermissionKey(),
            PROJ_EDIT_INTERNAL_BLOCKS.getPermissionKey(),
            PROJ_SHARE.getPermissionKey(),

            ANNUAL_SUBMISSION_CREATE.applyingFor(PERMISSION_ALL),

            AUTHORISE_PAYMENT.applyingFor(MY_ORG_ID_PLACEHOLDER),

            ADMIN_CONTENT.getPermissionKey(),
            ADMIN_SKILL_PROFILES.getPermissionKey(),

            REPORTS_ADHOC.getPermissionKey(),

            SYS_DASHBOARD.getPermissionKey(),
            SYS_FEATURES_EDIT.getPermissionKey(),
            CORP_DASH_MARK_PROJ.getPermissionKey(),
            PERMISSION_LIST_VIEW.getPermissionKey(),
            LABELS_MANAGE.getPermissionKey(),
            GLA_CONTACT.getPermissionKey(),
            NOTIFICATION_LIST_VIEW.getPermissionKey(),
            NOTIFICATION_SCHEDULE.getPermissionKey(),
            OVERRIDES_MANAGE.getPermissionKey(),
            OUTPUTS_CONFIGURATION_MANAGE.getPermissionKey(),

            GIVE_ACCESS_TO_ORG_VIA_TEMPLATE.getPermissionKey()
    ));

    private static final Set<String> gla_org_admin_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE_APPROVE.getPermissionKey(),
            ORG_VIEW_DETAILS.applyingFor(MY_ORG_ID_PLACEHOLDER),
            ORG_VIEW_USERS.applyingFor(PERMISSION_ALL),
            ORG_EDIT_DETAILS.applyingFor(MY_ORG_ID_PLACEHOLDER),
            ORG_EDIT_GOVERNANCE.getPermissionKey(),
            ORG_EDIT_TYPE.getPermissionKey(),
            ORG_EDIT_BUDGET.applyingFor(MY_ORG_ID_PLACEHOLDER),
            ORG_EDIT_TEAM.getPermissionKey(),
            ORG_EDIT_REGISTRATION_KEY.applyingFor(MY_ORG_ID_PLACEHOLDER),
            GLA_CONTACT.getPermissionKey(),

            TEAM_ADD.getPermissionKey(),
            TEAM_EDIT.applyingFor(MY_ORG_ID_PLACEHOLDER),

            USER_APPROVE.applyingFor(MY_ORG_ID_PLACEHOLDER),
            USER_CHANGE_STATUS.getPermissionKey(),

            TEMP.getPermissionKey(),
            TEMP_MANAGE.getPermissionKey(),
            PROJ_APPROVE.getPermissionKey(),
            PROJ_DOC_DELETE.getPermissionKey(),
            PROJ_WBS_DELETE.getPermissionKey(),
            PROJ_OUTPUTS_EDIT_PAST.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_EDIT_INTERNAL_BLOCKS.getPermissionKey(),

            AUTHORISE_PAYMENT.applyingFor(MY_ORG_ID_PLACEHOLDER),
            CORP_DASH_MARK_PROJ.getPermissionKey(),
            PERMISSION_LIST_VIEW.getPermissionKey()
    ));

    private static final Set<String> gla_spm_permissions = new HashSet<>(Arrays.asList(
            ORG_EDIT_BUDGET.applyingFor(MY_ORG_ID_PLACEHOLDER),

            TEMP.getPermissionKey(),
            TEMP_MANAGE.getPermissionKey(),

            PROJ_APPROVE.getPermissionKey(),
            PROJ_DOC_DELETE.getPermissionKey(),
            PROJ_WBS_DELETE.getPermissionKey(),
            PROJ_OUTPUTS_EDIT_PAST.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_EDIT_INTERNAL_BLOCKS.getPermissionKey(),

            AUTHORISE_PAYMENT.applyingFor(MY_ORG_ID_PLACEHOLDER),
            CORP_DASH_MARK_PROJ.getPermissionKey(),
            PERMISSION_LIST_VIEW.getPermissionKey()
    ));

    private static final Set<String> additional_gla_pm_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE.getPermissionKey(),
            ORG_EDIT_CONTRACT.getPermissionKey(),

            PROJ_CREATE.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_EDIT.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_EDIT_HISTORY.applyingFor(PERMISSION_ALL),
            PROJ_SUBMIT.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_ASSESS.getPermissionKey(),
            PROJ_REFER.getPermissionKey(),
            PROJ_COMPLETE.getPermissionKey(),
            PROJ_DOC_UPLOAD.getPermissionKey(),
            PROJ_MILESTONE_CONDITIONAL_CREATE.getPermissionKey(),
            PROJ_MILESTONE_CONDITIONAL_EDIT.getPermissionKey(),
            PROJ_MILESTONE_CONDITIONAL_DELETE.getPermissionKey(),
            PROJ_OUTPUTS_EDIT_FUTURE.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_EDIT_INTERNAL_BLOCKS.getPermissionKey(),

            ANNUAL_SUBMISSION_REVERT.applyingFor(PERMISSION_ALL),

            PAYMENTS_RECLAIM_CREATE.getPermissionKey(),
            RECLAIM_INTEREST_PAYMENT.getPermissionKey(),
            CORP_DASH_MARK_PROJ.getPermissionKey(),
            PROJ_ADD_LABEL.getPermissionKey(),
            PERMISSION_LIST_VIEW.getPermissionKey()
    ));

    private static final Set<String> gla_pm_only_permissions = Stream.of(
            PROJ_RECOMMEND.getPermissionKey()
    ).collect(Collectors.toSet());

    private static final Set<String> gla_registration_approver_permissions = Stream.of(
            ORG_VIEW.getPermissionKey(),
            ORG_VIEW_DETAILS.applyingFor(PERMISSION_ALL),
            ORG_VIEW_USERS.applyingFor(PERMISSION_ALL),
            ORG_VIEW_VENDOR_SAP_ID.getPermissionKey(),
            GLA_CONTACT.getPermissionKey(),
            ORG_MANAGE.getPermissionKey(),
            ORG_MANAGE_APPROVE.getPermissionKey(),
            ORG_EDIT_ANY_ROLE.getPermissionKey(),
            ORG_FILTER_TEAM.getPermissionKey(),
            ORG_REQUEST_ACCESS.getPermissionKey(),

            TEAM_VIEW.getPermissionKey(),
            TEAM_ADD.getPermissionKey(),

            USER_ADD.applyingFor(PERMISSION_ALL),
            USER_PERMISSIONS.applyingFor(PERMISSION_ALL),
            USER_REMOVE.applyingFor(PERMISSION_ALL),
            USER_APPROVE.applyingFor(PERMISSION_ALL),
            USER_INVITE.applyingFor(PERMISSION_ALL),
            USER_LIST_VIEW.applyingFor(PERMISSION_ALL),
            USER_VIEW_THRESHOLD.getPermissionKey(),
            USER_CHANGE_STATUS.getPermissionKey(),
            USERS_SEARCH_GLA.getPermissionKey(),
            USERS_ASSIGN_PRIMARY.getPermissionKey(),

            PROG.getPermissionKey(),
            PROG_MANAGE.getPermissionKey(),

            TEMP.getPermissionKey(),
            TEMP_MANAGE.getPermissionKey(),

            ASSESSMENT_VIEW.getPermissionKey(),

            CONTACTS.getPermissionKey(),

            GIVE_ACCESS_TO_ORG_VIA_TEMPLATE.getPermissionKey()
    ).collect(Collectors.toSet());

    private static final Set<String> gla_finance_only_permissions = new HashSet<>(Arrays.asList(
            USER_SET_PENDING_THRESHOLD.getPermissionKey(),
            PROJ_EDIT_INTERNAL_BLOCKS.getPermissionKey(),
            ANNUAL_SUBMISSION_REVERT.applyingFor(PERMISSION_ALL),
            PERMISSION_LIST_VIEW.getPermissionKey()
    ));

    private static final Set<String> gla_read_only_permissions = new HashSet<>(Arrays.asList(
            ORG_VIEW.getPermissionKey(),
            ORG_VIEW_VENDOR_SAP_ID.getPermissionKey(),
            ORG_REQUEST_ACCESS.getPermissionKey(),
            ORG_FILTER_TEAM.getPermissionKey(),
            TEAM_VIEW.getPermissionKey(),
            USER_LIST_VIEW.applyingFor(PERMISSION_ALL),
            USER_VIEW_THRESHOLD.getPermissionKey(),
            USERS_SEARCH_GLA.getPermissionKey(),
            PROG.getPermissionKey(),
            CONS.getPermissionKey(),
            ASSESSMENT_VIEW.getPermissionKey(),
            ASSESSMENT_MANAGE.getPermissionKey(),
            PROJ.getPermissionKey(),
            PROJ_VIEW_DETAILS.getPermissionKey(),
            PROJ_READ.applyingFor(PERMISSION_ALL),
            PROJ_VIEW_RECOMMENDATION.getPermissionKey(),
            PROJ_VIEW_INTERNAL_BLOCKS.getPermissionKey(),
            PROJ_CHANGE_REPORT.applyingFor(PERMISSION_ALL),
            PROJ_SUMMARY_REPORT.applyingFor(PERMISSION_ALL),
            FAQS.getPermissionKey(),
            CONTACTS.getPermissionKey(),
            REPORTS_TAB.getPermissionKey(),
            REPORTS_VIEW_STATIC.getPermissionKey(),
            REPORTS_JASPER.getPermissionKey(),
            REPORTS_INTERNAL.getPermissionKey(),
            PAYMENTS.getPermissionKey(),
            VIEW_PAYMENT_HISTORY.getPermissionKey(),
            CORP_DASH_MARKED_PROJ_MESSAGE.getPermissionKey(),
            PERMISSION_LIST_VIEW.getPermissionKey(),
            PROJ_DOWNLOAD_ZIP.getPermissionKey()
    ));

    private static final Set<String> additional_org_admin_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE_COPY.getPermissionKey(),
            ORG_VIEW_USERS.applyingFor(MY_ORG_ID_PLACEHOLDER),
            ORG_EDIT_DETAILS.applyingFor(MY_ORG_ID_PLACEHOLDER),
            ORG_EDIT_REGISTRATION_KEY.applyingFor(MY_ORG_ID_PLACEHOLDER),
            USER_ADD.applyingFor(MY_ORG_ID_PLACEHOLDER),
            USER_PERMISSIONS.applyingFor(MY_ORG_ID_PLACEHOLDER),
            USER_REMOVE.applyingFor(MY_ORG_ID_PLACEHOLDER),
            USER_APPROVE.applyingFor(MY_ORG_ID_PLACEHOLDER),
            USER_INVITE.applyingFor(MY_ORG_ID_PLACEHOLDER),
            USER_CHANGE_STATUS.getPermissionKey(),
            CONS_CREATE.getPermissionKey(),
            CONS_EDIT.applyingFor(MY_ORG_ID_PLACEHOLDER)
    ));

    private static final Set<String> additional_project_editor_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE.getPermissionKey(),
            PROJ_ABANDON.getPermissionKey(),
            PROJ_COMPLETE.getPermissionKey(),
            PROJ_CREATE.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_EDIT.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_EDIT_HISTORY.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_SUBMIT.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_MILESTONE_CONDITIONAL_EDIT.getPermissionKey(),
            PROJ_OUTPUTS_EDIT_PAST.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_OUTPUTS_EDIT_FUTURE.applyingFor(MY_ORG_ID_PLACEHOLDER),
            ANNUAL_SUBMISSION_CREATE.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_REVERT_OR_DELETE_BLOCK.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PERMISSION_LIST_VIEW.getPermissionKey()
    ));

    private static final Set<String> project_reader_permissions = new HashSet<>(Arrays.asList(
            ORG_REQUEST_ACCESS.getPermissionKey(),
            ORG_VIEW.getPermissionKey(),
            ORG_VIEW_DETAILS.applyingFor(MY_ORG_ID_PLACEHOLDER),
            USER_LIST_VIEW.applyingFor(PERMISSION_ALL),
            CONS.getPermissionKey(),
            PROJ.getPermissionKey(),
            PROJ_VIEW_DETAILS.getPermissionKey(),
            PROJ_READ.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_CHANGE_REPORT.applyingFor(MY_ORG_ID_PLACEHOLDER),
            PROJ_SUMMARY_REPORT.applyingFor(MY_ORG_ID_PLACEHOLDER),
            FAQS.getPermissionKey(),
            CONTACTS.getPermissionKey(),
            PAYMENTS.getPermissionKey(),
            REPORTS_TAB.getPermissionKey(),
            PERMISSION_LIST_VIEW.getPermissionKey()
    ));

    private static final Set<String> unapproved_user_permissions = new HashSet<>(Arrays.asList(
            ORG_REQUEST_ACCESS.getPermissionKey(),
            USER_LIST_VIEW.applyingFor(PERMISSION_ALL)
    ));

    private static final Map<String, Set<String>> permissions_map = new HashMap<String, Set<String>>() {{
        Set<String> glaNonReadOnlyPermissions = concatPermissions(gla_read_only_permissions, additional_gla_pm_permissions);
        Set<String> glaOpsAdminPermissions = concatPermissions(glaNonReadOnlyPermissions, ops_admin_permissions);
        glaOpsAdminPermissions.remove(PROJ_EDIT.applyingFor(MY_ORG_ID_PLACEHOLDER));
        glaOpsAdminPermissions.remove(PROJ_SUBMIT.applyingFor(MY_ORG_ID_PLACEHOLDER));
        put(OPS_ADMIN, glaOpsAdminPermissions);

        put(GLA_ORG_ADMIN, concatPermissions(glaNonReadOnlyPermissions, gla_org_admin_permissions));

        put(GLA_SPM, concatPermissions(glaNonReadOnlyPermissions, gla_spm_permissions));

        put(GLA_PM, concatPermissions(glaNonReadOnlyPermissions, gla_pm_only_permissions));

        put(GLA_REGISTRATION_APPROVER, gla_registration_approver_permissions);

        put(GLA_FINANCE, concatPermissions(gla_read_only_permissions, gla_finance_only_permissions));

        put(GLA_READ_ONLY, gla_read_only_permissions);

        Set<String> projectEditor = concatPermissions(additional_project_editor_permissions, project_reader_permissions);
        put(ORG_ADMIN, concatPermissions(projectEditor, additional_org_admin_permissions));

        put(PROJECT_EDITOR, projectEditor);

        put(PROJECT_READER, project_reader_permissions);

        Set<String> techAdminPermissions = new HashSet<>(gla_read_only_permissions);
        techAdminPermissions.add(REPORTS_ADHOC.getPermissionKey());
        techAdminPermissions.add(SYS_DASHBOARD.getPermissionKey());
        techAdminPermissions.add(SYS_FEATURES_EDIT.getPermissionKey());
        techAdminPermissions.add(PERMISSION_LIST_VIEW.getPermissionKey());
        techAdminPermissions.add(NOTIFICATION_LIST_VIEW.getPermissionKey());
        put(TECH_ADMIN, techAdminPermissions);
    }};

    private static Set<String> concatPermissions(Set<String> a, Set<String> b) {
        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet());
    }

    public boolean currentUserHasPermission(PermissionType permission) {
        return currentUserHasPermission(permission.getPermissionKey());
    }

    public boolean currentUserHasPermission(String permission) {
        return getPermissionsForUser(userService.currentUser()).contains(permission);
    }

    public boolean currentUserHasPermissionForOrganisation(PermissionType permission, Integer orgId) {
        return currentUserHasPermissionForOrganisation(permission.getPermissionKey(), orgId);
    }

    public boolean currentUserHasPermissionForOrganisation(String permission, Integer orgId) {
        return userHasPermissionForOrganisation(userService.currentUser(), permission, orgId);
    }

    public boolean userHasPermissionForOrganisation(User user, PermissionType permission, Integer orgId) {
        return userHasPermissionForOrganisation(user, permission.getPermissionKey(), orgId);
    }

    public boolean userHasPermissionForOrganisation(User user, String permission, Integer orgId) {
        String orgSpecificPermission = (orgId == null) ? "ZZZ" : permission + orgId;
        String allOrgsPermission = permission + PERMISSION_ALL;

        for (String userPermission : getPermissionsForUser(user)) {
            if (userPermission.equals(permission)) {
                // Permission is not organisation-specific, and user has it
                return true;
            }
            if (userPermission.equals(orgSpecificPermission)) {
                // User has permission for specified organisation
                return true;
            }
            if (userPermission.equals(allOrgsPermission)) {
                // User has permission for all organisations
                return true;
            }
        }

        return false;
    }

    private String getPermissionDescription(String key) {
        for (PermissionType permissionType : values()) {
            if (null != permissionType.getDescriptionByKey(key)) {
                return permissionType.getDescriptionByKey(key);
            }
        }
        return key;
    }

    private Map<String, List<PermissionDetails>> getPermissionDescriptionMapForCurrentUser() {
        Map<String, List<PermissionDetails>> permissionsDescriptionMap = new HashMap<>();
        Set<Role> roles = userService.currentUser().getRoles();

        for (Role role : roles) {
            groupRoleAndPermissions(permissionsDescriptionMap, role.getName());
        }

        // get roles current user can assign
        for (Role role : userService.currentUser().getRoles()) {
            Integer orgId = role.getOrganisation().getId();
            if (userService.currentUser().isOpsAdmin() || userService
                    .canAssignRoles(userService.currentUser().getRoles(), orgId)) {
                List<RoleNameAndDescription> assignableRoles = roleService.getAssignableRoles(orgId);
                for (RoleNameAndDescription assignableRole : assignableRoles) {
                    groupRoleAndPermissions(permissionsDescriptionMap, assignableRole.getName());
                }
            }
        }

        return permissionsDescriptionMap;
    }

    private Map<String, List<PermissionDetails>> getPermissionDescriptionMap() {
        Map<String, List<PermissionDetails>> permissionsDescriptionMap = new HashMap<>();
        for (String role : permissions_map.keySet()) {
            groupRoleAndPermissions(permissionsDescriptionMap, role);
        }
        return permissionsDescriptionMap;
    }

    private void groupRoleAndPermissions(Map<String, List<PermissionDetails>> permissionsDescriptionMap, String role) {
        List<PermissionDetails> allPermissions = getPermissionDescriptionForRole(role);
        // Change role from ROLE_OPS_ADMIN to OPS ADMIN
        role = role.substring(5).replace("_", " ");
        permissionsDescriptionMap.put(role, allPermissions);
    }

    private List<PermissionDetails> getPermissionDescriptionForRole(String role) {
        List<PermissionDetails> allPermissions = new ArrayList<>();

        for (String permissionKey : PermissionService.permissions_map.get(role)) {
            String key;
            if (permissionKey.endsWith(".*")) {
                key = permissionKey.substring(0, permissionKey.length() - "*".length());
                allPermissions.add(new PermissionDetails(getPermissionDescription(key), PermissionApplicability.ALL));
            }

            if (permissionKey.endsWith(".[MY_ORG_ID]")) {
                key = permissionKey.substring(0, permissionKey.length() - "[MY_ORG_ID]".length());
                allPermissions.add(new PermissionDetails(getPermissionDescription(key), PermissionApplicability.MY_ORG));
            }

            if (!permissionKey.endsWith(".*") && !permissionKey.endsWith(".[MY_ORG_ID]")) {
                allPermissions.add(new PermissionDetails(getPermissionDescription(permissionKey),
                        PermissionApplicability.NON_SPECIFIC));
            }
        }

        return allPermissions;
    }

    /**
     * Returns a map of the role and all associated permissions.
     */
    public Map<String, List<PermissionDetails>> getPermissions() {
        return userService.currentUser().isOpsAdmin()
                ? getPermissionDescriptionMap()
                : getPermissionDescriptionMapForCurrentUser();
    }

    /**
     * Returns a list of all project access controls for a specific project.
     */
    public Collection<ProjectAccessControlSummary> getProjectAccessControlList(Integer projectId) {
        return projectAccessControlSummaryRepository.findAllByIdProjectId(projectId);
    }

}
