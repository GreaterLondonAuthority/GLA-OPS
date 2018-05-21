/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    UserService userService;

    public Set<String> getPermissionsForUser(User user) {
        Set<String> permissions = new HashSet<>();

        if (!user.isApproved()) {
            permissions.addAll(unapproved_user_permissions);
        }

        for (Role role : user.getApprovedRoles()) {
            permissions.addAll(getRolePermissions(role));
        }

        if (!featureStatus.isEnabled(FeatureStatus.Feature.Payments)) {
            permissions.removeIf(p -> p.startsWith(PAYMENTS));
            permissions.removeIf(p -> p.startsWith(AUTHORISE_PAYMENT));
        }

        if (!featureStatus.isEnabled(FeatureStatus.Feature.ChangeReporting)) {
            permissions.removeIf(p -> p.startsWith(PROJ_CHANGE_REPORT));
        }

        return permissions;
    }

    private Set<String> getRolePermissions(Role role) {
        String orgId = String.valueOf(role.getOrganisation().getId());
        return permissions_map.get(role.getName()).stream().map(permission -> permission.replace(MY_ORG_ID_PLACEHOLDER, orgId)).collect(Collectors.toSet());
    }

    private static final String MY_ORG_ID_PLACEHOLDER = "[MY_ORG_ID]";
    private static final String PERMISSION_ALL = "*";

    public static final String ORG_VIEW = "org.view";
    public static final String ORG_VIEW_DETAILS = "org.view.details.";
    public static final String ORG_VIEW_USERS = "org.view.users.";
    public static final String ORG_VIEW_VENDOR_SAP_ID = "org.view.vendor.sap.id";

    public static final String ORG_MANAGE = "org.manage";
    public static final String ORG_MANAGE_REGISTER = "org.manage.register";
    public static final String ORG_MANAGE_APPROVE = "org.manage.approve";
    public static final String ORG_MANAGE_CREATE_PROFILE = "org.manage.create.profile";

    public static final String ORG_EDIT_DETAILS = "org.edit.details.";
    public static final String ORG_EDIT_NAME = "org.edit.name";
    public static final String ORG_EDIT_VENDOR_SAP_ID = "org.edit.vendor.sap.id";
    public static final String ORG_EDIT_TYPE = "org.edit.type";
    public static final String ORG_EDIT_MANAGING_ORG = "org.edit.managing.org";
    public static final String ORG_EDIT_CONTRACT = "org.edit.contract";
    public static final String ORG_EDIT_BUDGET = "org.edit.budget";
    public static final String ORG_EDIT_ANY_ROLE = "org.edit.any.role";

    public static final String ORG_REQUEST_ACCESS = "org.request.access";

    public static final String USER_ADD = "user.add.";
    public static final String USER_PERMISSIONS = "user.permissions.";
    public static final String USER_REMOVE = "user.remove.";
    public static final String USER_APPROVE = "user.approve.";
    public static final String USER_INVITE = "user.invite.";
    public static final String USER_LIST_VIEW = "user.list.view.";
    public static final String USER_SET_PENDING_THRESHOLD = "user.org.pending.threshold.set";


    public static final String USERS_SEARCH_GLA = "users.search.gla";

    public static final String PROG = "prog";
    public static final String PROG_MANAGE = "prog.manage";

    public static final String TEMP = "temp";
    public static final String TEMP_MANAGE = "temp.manage";

    public static final String CONS = "cons";
    public static final String CONS_CREATE = "cons.create";
    public static final String CONS_EDIT = "cons.edit.";

    public static final String PROJ = "proj";
    public static final String PROJ_CREATE = "proj.create";
    public static final String PROJ_VIEW_DETAILS = "proj.view.details";
    public static final String PROJ_EDIT = "proj.edit.";
    public static final String PROJ_EDIT_HISTORY = "proj.edit.history.";
    public static final String PROJ_READ = "proj.read.";
    public static final String PROJ_SUBMIT = "proj.submit.";
    public static final String PROJ_ASSESS = "proj.assess";
    public static final String PROJ_REFER = "proj.refer";
    public static final String PROJ_REINSTATE = "proj.reinstate";
    public static final String PROJ_APPROVE = "proj.approve";
    public static final String PROJ_RECOMMEND = "proj.recommend";
    public static final String PROJ_ABANDON = "proj.abandon";
    public static final String PROJ_COMPLETE = "proj.complete";
    public static final String PROJ_VIEW_RECOMMENDATION = "proj.view.recommendation";
    public static final String PROJ_DOC_UPLOAD = "proj.doc.upload";
    public static final String PROJ_DOC_DELETE = "proj.doc.delete";
    public static final String PROJ_MILESTONE_CONDITIONAL_CREATE = "proj.milestone.conditional.create";
    public static final String PROJ_MILESTONE_CONDITIONAL_EDIT = "proj.milestone.conditional.edit";
    public static final String PROJ_MILESTONE_CONDITIONAL_DELETE = "proj.milestone.conditional.delete";
    public static final String PROJ_WBS_DELETE = "proj.wbs.delete";
    public static final String PROJ_OUTPUTS_EDIT_PAST = "proj.outputs.editPast.";
    public static final String PROJ_OUTPUTS_EDIT_FUTURE = "proj.outputs.editFuture.";
    public static final String PROJ_CHANGE_REPORT = "proj.change.report.";
    public static final String PROJ_LEDGER_ACTUAL_CREATE = "proj.ledger.actual.create";
    public static final String PROJ_TRANSFER = "proj.transfer";



    public static final String PAYMENTS = "payments";
    public static final String PAYMENTS_RECLAIM_CREATE = "payments.reclaim.create";
    public static final String AUTHORISE_PAYMENT = "payments.authorise.";
    public static final String RECLAIM_INTEREST_PAYMENT = "reclaim.payments.interest";
    public static final String FAQS = "faqs";
    public static final String CONTACTS = "contacts";
    public static final String REPORTS_TAB = "reports.tab";

    public static final String SYS_DASHBOARD = "system.dashboard";
    public static final String ADMIN_CONTENT = "admin.content";


    private static final Set<String> ops_admin_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE,
            ORG_MANAGE_REGISTER,
            ORG_MANAGE_APPROVE,

            ORG_VIEW,
            ORG_VIEW_DETAILS + PERMISSION_ALL,
            ORG_VIEW_USERS + PERMISSION_ALL,
            ORG_VIEW_VENDOR_SAP_ID,

            ORG_EDIT_DETAILS + PERMISSION_ALL,
            ORG_EDIT_NAME,
            ORG_EDIT_VENDOR_SAP_ID,
            ORG_EDIT_TYPE,
            ORG_EDIT_MANAGING_ORG,
            ORG_EDIT_CONTRACT,
            ORG_EDIT_BUDGET,
            ORG_EDIT_ANY_ROLE,

            ORG_REQUEST_ACCESS,

            USER_ADD + PERMISSION_ALL,
            USER_PERMISSIONS + PERMISSION_ALL,
            USER_REMOVE + PERMISSION_ALL,
            USER_APPROVE + PERMISSION_ALL,
            USER_INVITE + PERMISSION_ALL,
            USER_LIST_VIEW + PERMISSION_ALL,
            USER_SET_PENDING_THRESHOLD,

            USERS_SEARCH_GLA,

            PROG,
            PROG_MANAGE,
            TEMP,
            TEMP_MANAGE,
            CONS,

            PROJ,
            PROJ_CREATE,
            PROJ_VIEW_DETAILS,
            PROJ_READ + PERMISSION_ALL,
            PROJ_EDIT + PERMISSION_ALL,
            PROJ_EDIT_HISTORY + PERMISSION_ALL,
            PROJ_SUBMIT + PERMISSION_ALL,
            PROJ_ASSESS,
            PROJ_REFER,
            PROJ_REINSTATE,
            PROJ_APPROVE,
            PROJ_ABANDON,
            PROJ_COMPLETE,
            PROJ_DOC_UPLOAD,
            PROJ_DOC_DELETE,
            PROJ_MILESTONE_CONDITIONAL_CREATE,
            PROJ_MILESTONE_CONDITIONAL_EDIT,
            PROJ_MILESTONE_CONDITIONAL_DELETE,
            PROJ_WBS_DELETE,
            PROJ_OUTPUTS_EDIT_PAST + MY_ORG_ID_PLACEHOLDER,
            PROJ_OUTPUTS_EDIT_FUTURE + MY_ORG_ID_PLACEHOLDER,
            PROJ_VIEW_RECOMMENDATION,
            PROJ_CHANGE_REPORT + PERMISSION_ALL,
            PROJ_LEDGER_ACTUAL_CREATE,
            PROJ_TRANSFER,

            PAYMENTS,
            PAYMENTS_RECLAIM_CREATE,
            RECLAIM_INTEREST_PAYMENT,
            AUTHORISE_PAYMENT + MY_ORG_ID_PLACEHOLDER,
            FAQS,
            CONTACTS,
            REPORTS_TAB,
            ADMIN_CONTENT,
            SYS_DASHBOARD
    ));

    private static final Set<String> gla_org_admin_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE,
            ORG_MANAGE_APPROVE,
            ORG_VIEW,
            ORG_VIEW_DETAILS + MY_ORG_ID_PLACEHOLDER,
            ORG_VIEW_USERS + MY_ORG_ID_PLACEHOLDER,
            ORG_EDIT_DETAILS + MY_ORG_ID_PLACEHOLDER,
            ORG_VIEW_VENDOR_SAP_ID,
            ORG_EDIT_CONTRACT,
            ORG_EDIT_BUDGET,

            ORG_REQUEST_ACCESS,

            USER_APPROVE + MY_ORG_ID_PLACEHOLDER,
            USER_LIST_VIEW + PERMISSION_ALL,

            USERS_SEARCH_GLA,

            PROG,
            TEMP,
            TEMP_MANAGE,
            CONS,

            PROJ,
            PROJ_CREATE,
            PROJ_VIEW_DETAILS,
            PROJ_READ + PERMISSION_ALL,
            PROJ_EDIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_EDIT_HISTORY + PERMISSION_ALL,
            PROJ_SUBMIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_ASSESS,
            PROJ_REFER,
            PROJ_APPROVE,
            PROJ_COMPLETE,
            PROJ_DOC_UPLOAD,
            PROJ_DOC_DELETE,
            PROJ_MILESTONE_CONDITIONAL_CREATE,
            PROJ_MILESTONE_CONDITIONAL_EDIT,
            PROJ_MILESTONE_CONDITIONAL_DELETE,
            PROJ_WBS_DELETE,
            PROJ_OUTPUTS_EDIT_PAST + MY_ORG_ID_PLACEHOLDER,
            PROJ_OUTPUTS_EDIT_FUTURE + MY_ORG_ID_PLACEHOLDER,
            PROJ_VIEW_RECOMMENDATION,
            PROJ_CHANGE_REPORT + PERMISSION_ALL,

            FAQS,
            CONTACTS,
            REPORTS_TAB,
            PAYMENTS,
            PAYMENTS_RECLAIM_CREATE,
            RECLAIM_INTEREST_PAYMENT,
            AUTHORISE_PAYMENT + MY_ORG_ID_PLACEHOLDER
    ));

    private static final Set<String> gla_spm_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE,
            ORG_VIEW,
            ORG_VIEW_VENDOR_SAP_ID,
            ORG_EDIT_CONTRACT,
            ORG_EDIT_BUDGET,

            ORG_REQUEST_ACCESS,

            USER_LIST_VIEW + PERMISSION_ALL,

            USERS_SEARCH_GLA,

            PROG,
            TEMP,
            TEMP_MANAGE,
            CONS,

            PROJ,
            PROJ_CREATE,
            PROJ_VIEW_DETAILS,
            PROJ_READ + PERMISSION_ALL,
            PROJ_EDIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_EDIT_HISTORY + PERMISSION_ALL,
            PROJ_SUBMIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_ASSESS,
            PROJ_REFER,
            PROJ_APPROVE,
            PROJ_COMPLETE,
            PROJ_DOC_UPLOAD,
            PROJ_DOC_DELETE,
            PROJ_MILESTONE_CONDITIONAL_CREATE,
            PROJ_MILESTONE_CONDITIONAL_EDIT,
            PROJ_MILESTONE_CONDITIONAL_DELETE,
            PROJ_WBS_DELETE,
            PROJ_OUTPUTS_EDIT_PAST + MY_ORG_ID_PLACEHOLDER,
            PROJ_OUTPUTS_EDIT_FUTURE + MY_ORG_ID_PLACEHOLDER,
            PROJ_VIEW_RECOMMENDATION,
            PROJ_CHANGE_REPORT + PERMISSION_ALL,

            FAQS,
            CONTACTS,
            REPORTS_TAB,
            PAYMENTS,
            PAYMENTS_RECLAIM_CREATE,
            RECLAIM_INTEREST_PAYMENT,
            AUTHORISE_PAYMENT + MY_ORG_ID_PLACEHOLDER
    ));

    private static final Set<String> gla_pm_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE,
            ORG_VIEW,
            ORG_VIEW_VENDOR_SAP_ID,
            ORG_EDIT_CONTRACT,

            ORG_REQUEST_ACCESS,

            USER_LIST_VIEW + PERMISSION_ALL,

            USERS_SEARCH_GLA,

            PROG,
            CONS,

            PROJ,
            PROJ_CREATE,
            PROJ_VIEW_DETAILS,
            PROJ_READ + PERMISSION_ALL,
            PROJ_EDIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_EDIT_HISTORY + PERMISSION_ALL,
            PROJ_SUBMIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_ASSESS,
            PROJ_RECOMMEND,
            PROJ_REFER,
            PROJ_COMPLETE,
            PROJ_DOC_UPLOAD,
            PROJ_MILESTONE_CONDITIONAL_CREATE,
            PROJ_MILESTONE_CONDITIONAL_EDIT,
            PROJ_MILESTONE_CONDITIONAL_DELETE,
            PROJ_OUTPUTS_EDIT_FUTURE + MY_ORG_ID_PLACEHOLDER,
            PROJ_VIEW_RECOMMENDATION,
            PROJ_CHANGE_REPORT + PERMISSION_ALL,

            FAQS,
            CONTACTS,
            REPORTS_TAB,
            PAYMENTS,
            PAYMENTS_RECLAIM_CREATE,
            RECLAIM_INTEREST_PAYMENT
    ));

    private static final Set<String> gla_finance_permissions = new HashSet<>(Arrays.asList(
            ORG_VIEW,
            ORG_VIEW_VENDOR_SAP_ID,

            ORG_REQUEST_ACCESS,

            USER_LIST_VIEW + PERMISSION_ALL,

            USERS_SEARCH_GLA,
            USER_SET_PENDING_THRESHOLD,
            PROG,
            CONS,
            PROJ,
            PROJ_VIEW_DETAILS,
            PROJ_READ + PERMISSION_ALL,
            PROJ_VIEW_RECOMMENDATION,
            PROJ_CHANGE_REPORT + PERMISSION_ALL,

            FAQS,
            CONTACTS,
            REPORTS_TAB,
            PAYMENTS
    ));

    private static final Set<String> gla_read_only_permissions = new HashSet<>(Arrays.asList(
            ORG_VIEW,
            ORG_VIEW_VENDOR_SAP_ID,

            ORG_REQUEST_ACCESS,

            USER_LIST_VIEW + PERMISSION_ALL,

            USERS_SEARCH_GLA,

            PROG,
            CONS,
            PROJ,
            PROJ_VIEW_DETAILS,
            PROJ_READ + PERMISSION_ALL,
            PROJ_VIEW_RECOMMENDATION,
            PROJ_CHANGE_REPORT + PERMISSION_ALL,

            FAQS,
            CONTACTS,
            REPORTS_TAB,
            PAYMENTS
    ));

    private static final Set<String> org_admin_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE,
            ORG_MANAGE_CREATE_PROFILE,
            ORG_REQUEST_ACCESS,
            ORG_VIEW,
            ORG_VIEW_DETAILS + MY_ORG_ID_PLACEHOLDER,
            ORG_VIEW_USERS + MY_ORG_ID_PLACEHOLDER,
            ORG_EDIT_DETAILS + MY_ORG_ID_PLACEHOLDER,
            USER_ADD + MY_ORG_ID_PLACEHOLDER,
            USER_PERMISSIONS + MY_ORG_ID_PLACEHOLDER,
            USER_REMOVE + MY_ORG_ID_PLACEHOLDER,
            USER_APPROVE + MY_ORG_ID_PLACEHOLDER,
            USER_INVITE + MY_ORG_ID_PLACEHOLDER,
            USER_LIST_VIEW + PERMISSION_ALL,
            CONS,
            CONS_CREATE,
            CONS_EDIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_OUTPUTS_EDIT_PAST + MY_ORG_ID_PLACEHOLDER,
            PROJ_OUTPUTS_EDIT_FUTURE + MY_ORG_ID_PLACEHOLDER,

            PROJ,
            PROJ_ABANDON,
            PROJ_COMPLETE,
            PROJ_CREATE,
            PROJ_VIEW_DETAILS,
            PROJ_READ + MY_ORG_ID_PLACEHOLDER,
            PROJ_EDIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_EDIT_HISTORY + MY_ORG_ID_PLACEHOLDER,
            PROJ_SUBMIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_MILESTONE_CONDITIONAL_EDIT,
            PROJ_CHANGE_REPORT + MY_ORG_ID_PLACEHOLDER,

            FAQS,
            CONTACTS,
            PAYMENTS
    ));

    private static final Set<String> project_editor_permissions = new HashSet<>(Arrays.asList(
            ORG_MANAGE,
            ORG_REQUEST_ACCESS,
            ORG_VIEW,
            ORG_VIEW_DETAILS + MY_ORG_ID_PLACEHOLDER,
            USER_LIST_VIEW + PERMISSION_ALL,
            CONS,
            PROJ_ABANDON,
            PROJ_COMPLETE,
            PROJ,
            PROJ_CREATE,
            PROJ_VIEW_DETAILS,
            PROJ_READ + MY_ORG_ID_PLACEHOLDER,
            PROJ_EDIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_EDIT_HISTORY + MY_ORG_ID_PLACEHOLDER,
            PROJ_SUBMIT + MY_ORG_ID_PLACEHOLDER,
            PROJ_MILESTONE_CONDITIONAL_EDIT,
            PROJ_CHANGE_REPORT + MY_ORG_ID_PLACEHOLDER,
            PROJ_OUTPUTS_EDIT_PAST + MY_ORG_ID_PLACEHOLDER,
            PROJ_OUTPUTS_EDIT_FUTURE + MY_ORG_ID_PLACEHOLDER,

            FAQS,
            CONTACTS,
            PAYMENTS
    ));

    private static final Set<String> unapproved_user_permissions = new HashSet<>(Arrays.asList(
            ORG_REQUEST_ACCESS,
            USER_LIST_VIEW + PERMISSION_ALL
    ));

    private static final Map<String, Set<String>> permissions_map = new HashMap() {{
        put(Role.OPS_ADMIN, ops_admin_permissions);
        put(Role.GLA_ORG_ADMIN, gla_org_admin_permissions);
        put(Role.GLA_SPM, gla_spm_permissions);
        put(Role.GLA_PM, gla_pm_permissions);
        put(Role.GLA_FINANCE, gla_finance_permissions);
        put(Role.GLA_READ_ONLY, gla_read_only_permissions);
        put(Role.ORG_ADMIN, org_admin_permissions);
        put(Role.PROJECT_EDITOR, project_editor_permissions);

        Set<String> tech_admin_persmissions = new HashSet<>();
        tech_admin_persmissions.addAll(gla_read_only_permissions);
        tech_admin_persmissions.add(SYS_DASHBOARD);
        put(Role.TECH_ADMIN, tech_admin_persmissions);
    }};

    public boolean currentUserHasPermission(String permission) {
        return getPermissionsForUser(userService.currentUser()).contains(permission);
    }

    public boolean currentUserHasPermissionForOrganisation(String permission, Integer orgID) {
        return userHasPermissionForOrganisation(userService.currentUser(), permission, orgID);
    }

    public boolean userHasAllPermission(User user, String permission) {
        String allOrgsPermission = permission + PERMISSION_ALL;
        for (String userPermission : getPermissionsForUser(user)) {
            if (userPermission.equals(allOrgsPermission)) {
                // User has permission for specified organisation
                return true;
            }
        }
        return false;
    }

    public boolean userHasPermissionForOrganisation(User user, String permission, Integer orgID) {
        String orgSpecificPermission = (orgID == null) ? "ZZZ" : permission + orgID;
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

}
