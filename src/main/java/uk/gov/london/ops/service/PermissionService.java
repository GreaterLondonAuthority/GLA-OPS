/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;
import static uk.gov.london.ops.service.PermissionType.ADMIN_CONTENT;
import static uk.gov.london.ops.service.PermissionType.ADMIN_SKILL_PROFILES;
import static uk.gov.london.ops.service.PermissionType.ANNUAL_SUBMISSION_CREATE;
import static uk.gov.london.ops.service.PermissionType.ANNUAL_SUBMISSION_REVERT;
import static uk.gov.london.ops.service.PermissionType.ASSESSMENT_MANAGE;
import static uk.gov.london.ops.service.PermissionType.ASSESSMENT_TEMPLATE_MANAGE;
import static uk.gov.london.ops.service.PermissionType.ASSESSMENT_VIEW;
import static uk.gov.london.ops.service.PermissionType.AUTHORISE_PAYMENT;
import static uk.gov.london.ops.service.PermissionType.CONS;
import static uk.gov.london.ops.service.PermissionType.CONS_CREATE;
import static uk.gov.london.ops.service.PermissionType.CONS_EDIT;
import static uk.gov.london.ops.service.PermissionType.CONTACTS;
import static uk.gov.london.ops.service.PermissionType.CORP_DASH_MARKED_PROJ_MESSAGE;
import static uk.gov.london.ops.service.PermissionType.CORP_DASH_MARK_PROJ;
import static uk.gov.london.ops.service.PermissionType.FAQS;
import static uk.gov.london.ops.service.PermissionType.GLA_CONTACT;
import static uk.gov.london.ops.service.PermissionType.LABELS_MANAGE;
import static uk.gov.london.ops.service.PermissionType.NOTIFICATION_LIST_VIEW;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_ANY_ROLE;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_BUDGET;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_CONTRACT;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_DETAILS;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_GOVERNANCE;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_MANAGING_ORG;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_NAME;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_PARENT_ORG;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_REGISTRATION_KEY;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_TEAM;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_TYPE;
import static uk.gov.london.ops.service.PermissionType.ORG_EDIT_VENDOR_SAP_ID;
import static uk.gov.london.ops.service.PermissionType.ORG_FILTER_TEAM;
import static uk.gov.london.ops.service.PermissionType.ORG_MANAGE;
import static uk.gov.london.ops.service.PermissionType.ORG_MANAGE_APPROVE;
import static uk.gov.london.ops.service.PermissionType.ORG_MANAGE_COPY;
import static uk.gov.london.ops.service.PermissionType.ORG_REQUEST_ACCESS;
import static uk.gov.london.ops.service.PermissionType.ORG_VIEW;
import static uk.gov.london.ops.service.PermissionType.ORG_VIEW_DETAILS;
import static uk.gov.london.ops.service.PermissionType.ORG_VIEW_USERS;
import static uk.gov.london.ops.service.PermissionType.ORG_VIEW_VENDOR_SAP_ID;
import static uk.gov.london.ops.service.PermissionType.OUTPUTS_CONFIGURATION_MANAGE;
import static uk.gov.london.ops.service.PermissionType.OVERRIDES_MANAGE;
import static uk.gov.london.ops.service.PermissionType.PAYMENTS;
import static uk.gov.london.ops.service.PermissionType.PAYMENTS_RECLAIM_CREATE;
import static uk.gov.london.ops.service.PermissionType.PAYMENTS_RESEND;
import static uk.gov.london.ops.service.PermissionType.PERMISSION_LIST_VIEW;
import static uk.gov.london.ops.service.PermissionType.PROG;
import static uk.gov.london.ops.service.PermissionType.PROG_MANAGE;
import static uk.gov.london.ops.service.PermissionType.PROG_MANAGE_CE_CODE;
import static uk.gov.london.ops.service.PermissionType.PROJ;
import static uk.gov.london.ops.service.PermissionType.PROJ_ABANDON;
import static uk.gov.london.ops.service.PermissionType.PROJ_ADD_LABEL;
import static uk.gov.london.ops.service.PermissionType.PROJ_APPROVE;
import static uk.gov.london.ops.service.PermissionType.PROJ_ASSESS;
import static uk.gov.london.ops.service.PermissionType.PROJ_CHANGE_REPORT;
import static uk.gov.london.ops.service.PermissionType.PROJ_COMPLETE;
import static uk.gov.london.ops.service.PermissionType.PROJ_CREATE;
import static uk.gov.london.ops.service.PermissionType.PROJ_DOC_DELETE;
import static uk.gov.london.ops.service.PermissionType.PROJ_DOC_UPLOAD;
import static uk.gov.london.ops.service.PermissionType.PROJ_EDIT;
import static uk.gov.london.ops.service.PermissionType.PROJ_EDIT_HISTORY;
import static uk.gov.london.ops.service.PermissionType.PROJ_EDIT_INTERNAL_BLOCKS;
import static uk.gov.london.ops.service.PermissionType.PROJ_LEDGER_ACTUAL_CREATE;
import static uk.gov.london.ops.service.PermissionType.PROJ_MILESTONE_CONDITIONAL_CREATE;
import static uk.gov.london.ops.service.PermissionType.PROJ_MILESTONE_CONDITIONAL_DELETE;
import static uk.gov.london.ops.service.PermissionType.PROJ_MILESTONE_CONDITIONAL_EDIT;
import static uk.gov.london.ops.service.PermissionType.PROJ_OUTPUTS_EDIT_FUTURE;
import static uk.gov.london.ops.service.PermissionType.PROJ_OUTPUTS_EDIT_PAST;
import static uk.gov.london.ops.service.PermissionType.PROJ_READ;
import static uk.gov.london.ops.service.PermissionType.PROJ_RECOMMEND;
import static uk.gov.london.ops.service.PermissionType.PROJ_REFER;
import static uk.gov.london.ops.service.PermissionType.PROJ_REINSTATE;
import static uk.gov.london.ops.service.PermissionType.PROJ_REJECT;
import static uk.gov.london.ops.service.PermissionType.PROJ_REVERT_OR_DELETE_BLOCK;
import static uk.gov.london.ops.service.PermissionType.PROJ_SUBMIT;
import static uk.gov.london.ops.service.PermissionType.PROJ_SUMMARY_REPORT;
import static uk.gov.london.ops.service.PermissionType.PROJ_TRANSFER;
import static uk.gov.london.ops.service.PermissionType.PROJ_VIEW_DETAILS;
import static uk.gov.london.ops.service.PermissionType.PROJ_VIEW_INTERNAL_BLOCKS;
import static uk.gov.london.ops.service.PermissionType.PROJ_VIEW_RECOMMENDATION;
import static uk.gov.london.ops.service.PermissionType.PROJ_WBS_DELETE;
import static uk.gov.london.ops.service.PermissionType.RECLAIM_INTEREST_PAYMENT;
import static uk.gov.london.ops.service.PermissionType.REPORTS_INTERNAL;
import static uk.gov.london.ops.service.PermissionType.REPORTS_JASPER;
import static uk.gov.london.ops.service.PermissionType.REPORTS_TAB;
import static uk.gov.london.ops.service.PermissionType.REPORTS_VIEW_STATIC;
import static uk.gov.london.ops.service.PermissionType.SYS_DASHBOARD;
import static uk.gov.london.ops.service.PermissionType.SYS_FEATURES_EDIT;
import static uk.gov.london.ops.service.PermissionType.TEAM_ADD;
import static uk.gov.london.ops.service.PermissionType.TEAM_EDIT;
import static uk.gov.london.ops.service.PermissionType.TEAM_VIEW;
import static uk.gov.london.ops.service.PermissionType.TEMP;
import static uk.gov.london.ops.service.PermissionType.TEMP_MANAGE;
import static uk.gov.london.ops.service.PermissionType.USERS_ASSIGN_PRIMARY;
import static uk.gov.london.ops.service.PermissionType.USERS_SEARCH_GLA;
import static uk.gov.london.ops.service.PermissionType.USER_ADD;
import static uk.gov.london.ops.service.PermissionType.USER_APPROVE;
import static uk.gov.london.ops.service.PermissionType.USER_INVITE;
import static uk.gov.london.ops.service.PermissionType.USER_LIST_VIEW;
import static uk.gov.london.ops.service.PermissionType.USER_PERMISSIONS;
import static uk.gov.london.ops.service.PermissionType.USER_REMOVE;
import static uk.gov.london.ops.service.PermissionType.USER_SET_PENDING_THRESHOLD;
import static uk.gov.london.ops.service.PermissionType.USER_VIEW_THRESHOLD;
import static uk.gov.london.ops.service.PermissionType.values;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.PermissionDetails.PermisisonApplicability;
import uk.gov.london.ops.web.model.AssignableRole;

@Service
public class PermissionService {

  public static  final String MY_ORG_ID_PLACEHOLDER = "[MY_ORG_ID]";

  public static final String PERMISSION_ALL = "*";

  @Autowired
  FeatureStatus featureStatus;

  @Autowired
  UserService userService;

  @Autowired
  OrganisationService organisationService;

  public Set<String> getPermissionsForUser(User user) {
    Set<String> permissions = new HashSet<>();

    if (!user.isApproved() || user.getApprovedRoles().isEmpty()) {
      permissions.addAll(unapproved_user_permissions);
    }

    for (Role role : user.getApprovedRoles()) {
      permissions.addAll(getRolePermissions(role));
    }

    if (!featureStatus.isEnabled(Feature.Payments)) {
      permissions.removeIf(p -> p.startsWith(PAYMENTS.getPermissionKey()));
      permissions.removeIf(p -> p.startsWith(AUTHORISE_PAYMENT.getPermissionKey()));
    }

    if(!featureStatus.isEnabled(Feature.AllowNonGLAReportingAccess) && !user.isGla()) {
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

          ANNUAL_SUBMISSION_CREATE.applyingFor(PERMISSION_ALL),

          AUTHORISE_PAYMENT.applyingFor(MY_ORG_ID_PLACEHOLDER),

          ADMIN_CONTENT.getPermissionKey(),
          ADMIN_SKILL_PROFILES.getPermissionKey(),
          SYS_DASHBOARD.getPermissionKey(),
          SYS_FEATURES_EDIT.getPermissionKey(),
          CORP_DASH_MARK_PROJ.getPermissionKey(),
          PERMISSION_LIST_VIEW.getPermissionKey(),
          LABELS_MANAGE.getPermissionKey(),
          GLA_CONTACT.getPermissionKey(),
          NOTIFICATION_LIST_VIEW.getPermissionKey(),
          OVERRIDES_MANAGE.getPermissionKey(),
          OUTPUTS_CONFIGURATION_MANAGE.getPermissionKey()
  ));

  private static final Set<String> gla_org_admin_permissions = new HashSet<>(Arrays.asList(
      ORG_MANAGE_APPROVE.getPermissionKey(),
      ORG_VIEW_DETAILS.applyingFor(MY_ORG_ID_PLACEHOLDER),
      ORG_VIEW_USERS.applyingFor(MY_ORG_ID_PLACEHOLDER),
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

  private static final Set<String> gla_pm_only_permissions = new HashSet<>(Arrays.asList(
      PROJ_RECOMMEND.getPermissionKey()
  ));

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
      CORP_DASH_MARKED_PROJ_MESSAGE.getPermissionKey(),
          PERMISSION_LIST_VIEW.getPermissionKey()
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

  private static final Map<String, Set<String>> permissions_map = new HashMap() {{


    Set<String> glaNonReadOnlyPermissions =  Stream.concat(gla_read_only_permissions.stream(), additional_gla_pm_permissions.stream()).collect(Collectors.toSet());
    Set<String> gla_pm =  Stream.concat(glaNonReadOnlyPermissions.stream(), gla_pm_only_permissions.stream()).collect(Collectors.toSet());
    Set<String> gla_spm =  Stream.concat(glaNonReadOnlyPermissions.stream(), gla_spm_permissions.stream()).collect(Collectors.toSet());
    Set<String> gla_org_admin =  Stream.concat(glaNonReadOnlyPermissions.stream(), gla_org_admin_permissions.stream()).collect(Collectors.toSet());
    Set<String> gla_ops_admin =  Stream.concat(glaNonReadOnlyPermissions.stream(), ops_admin_permissions.stream()).collect(Collectors.toSet());
    Set<String> gla_finance =  Stream.concat(gla_read_only_permissions.stream(), gla_finance_only_permissions.stream()).collect(Collectors.toSet());


    gla_ops_admin.remove(PROJ_EDIT.applyingFor(MY_ORG_ID_PLACEHOLDER));
    gla_ops_admin.remove(PROJ_SUBMIT.applyingFor(MY_ORG_ID_PLACEHOLDER));


    Set<String> projectEditor =  Stream.concat(additional_project_editor_permissions.stream(), project_reader_permissions.stream()).collect(Collectors.toSet());
    Set<String> orgAdmin =  Stream.concat(projectEditor.stream(), additional_org_admin_permissions.stream()).collect(Collectors.toSet());

    put(OPS_ADMIN, gla_ops_admin);
    put(GLA_ORG_ADMIN, gla_org_admin);
    put(GLA_SPM, gla_spm);
    put(GLA_PM, gla_pm);
    put(GLA_FINANCE, gla_finance);
    put(GLA_READ_ONLY, gla_read_only_permissions);
    put(ORG_ADMIN, orgAdmin);
    put(PROJECT_EDITOR, projectEditor);
    put(PROJECT_READER, project_reader_permissions);

    Set<String> tech_admin_permissions = new HashSet<>();
    tech_admin_permissions.addAll(gla_read_only_permissions);
    tech_admin_permissions.add(SYS_DASHBOARD.getPermissionKey());
    tech_admin_permissions.add(SYS_FEATURES_EDIT.getPermissionKey());
    tech_admin_permissions.add(PERMISSION_LIST_VIEW.getPermissionKey());
    tech_admin_permissions.add(NOTIFICATION_LIST_VIEW.getPermissionKey());
    put(TECH_ADMIN, tech_admin_permissions);
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

  public boolean userHasPermissionForOrganisation(User user, PermissionType permission, Integer orgID) {
    return userHasPermissionForOrganisation(user, permission.getPermissionKey(), orgID);
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


  public String getPermissionDescription(String key) {
    for (PermissionType permissionType : values()) {
      if (null != permissionType.getDescriptionByKey(key)) {
        return permissionType.getDescriptionByKey(key);
      }
    }
    return key;
  }

    private Map<String, List<PermissionDetails>> getPermissionDescriptionMapForCurrentUser(Map<String, Set<String>> permissionsMap) {
        Map<String, List<PermissionDetails>> permissionsDescriptionMap = new HashMap();
        Set<Role> roles = userService.currentUser().getRoles();

        for (Role role : roles) {
            groupRoleAndPermissions(permissionsMap, permissionsDescriptionMap, role.getName());
        }

        // get roles current user can assign
        for (Role role : userService.currentUser().getRoles()) {
            Integer orgId = role.getOrganisation().getId();
            if (userService.currentUser().isOpsAdmin() || userService.canAssignRoles(userService.currentUser().getRoles(), orgId)) {
              List<AssignableRole> assignableRoles = organisationService.getAssignableRoles(orgId);
              for (AssignableRole assignableRole : assignableRoles){
                groupRoleAndPermissions(permissionsMap, permissionsDescriptionMap, assignableRole.getName());
              }
            }
        }

        return permissionsDescriptionMap;
    }

    private Map<String, List<PermissionDetails>> getPermissionDescriptionMap(Map<String, Set<String>> permissionsMap) {
        getPermissionDescriptionMapForCurrentUser(permissionsMap);
        Map<String, List<PermissionDetails>> permissionsDescriptionMap = new HashMap();

        for (String role : permissionsMap.keySet()) {
            groupRoleAndPermissions(permissionsMap, permissionsDescriptionMap, role);
        }


        return permissionsDescriptionMap;
    }

    private void groupRoleAndPermissions(Map<String, Set<String>> permissionsMap, Map<String, List<PermissionDetails>> permissionsDescriptionMap, String role) {
        List<PermissionDetails> allPermissions = getPermissionDescriptionForRole(permissionsMap, role);
        // Change role from ROLE_OPS_ADMIN to OPS ADMIN
        role = role.substring(5).replace("_", " ");

        permissionsDescriptionMap.put(role, allPermissions);
    }

    private List<PermissionDetails> getPermissionDescriptionForRole(Map<String, Set<String>> permissionsMap, String role) {
        List<PermissionDetails> allPermissions = new ArrayList<>();

        for (String permissionKey : permissionsMap.get(role)) {
            String key;
            if (permissionKey.endsWith(".*")) {
                key = permissionKey.substring(0, permissionKey.length() - "*".length());
                allPermissions.add(new PermissionDetails(getPermissionDescription(key), PermisisonApplicability.ALL));
            }

            if (permissionKey.endsWith(".[MY_ORG_ID]")) {
                key = permissionKey.substring(0, permissionKey.length() - "[MY_ORG_ID]".length());
                allPermissions.add(new PermissionDetails(getPermissionDescription(key), PermisisonApplicability.MY_ORG));
            }

            if (!permissionKey.endsWith(".*") && !permissionKey.endsWith(".[MY_ORG_ID]")) {
                allPermissions.add(new PermissionDetails(getPermissionDescription(permissionKey), PermisisonApplicability.NON_SPECIFIC));
            }
        }

        return allPermissions;
    }

    /**
     * Returns a map of the role and all associated permissions.
     */
    public Map<String, List<PermissionDetails>> getPermissions() {
        return userService.currentUser().isOpsAdmin() ?
                getPermissionDescriptionMap(permissions_map) :
                getPermissionDescriptionMapForCurrentUser(permissions_map);
    }
}
