/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.permission;

/**
 * Created by cmatias on 13/12/2018.
 */
public enum PermissionType {

  ORG_VIEW("org.view", "View organisations"),
  ORG_VIEW_DETAILS("org.view.details.", "View organisations details"),
  ORG_VIEW_USERS("org.view.users.", "View organisation users"),
  ORG_VIEW_VENDOR_SAP_ID("org.view.vendor.sap.id", "View organisation vendor SAP id"),
  GLA_CONTACT("org.view.glacontact", "Select a GLA contact for organisation"),

  ORG_MANAGE("org.manage", "Manage organisation and organisation's users"),
  ORG_MANAGE_APPROVE("org.manage.approve", "Manage and approve organisations"),
  ORG_MANAGE_COPY("org.manage.copy", "Manage and copy organisations"),

  ORG_EDIT_DETAILS("org.edit.details.", "Edit organisations details"),
  ORG_EDIT_GOVERNANCE("org.edit.governance", "Edit organisation governance"),
  ORG_EDIT_NAME("org.edit.name.", "Edit organisation name and organisation provider number"),
  ORG_EDIT_VENDOR_SAP_ID("org.edit.vendor.sap.id", "Edit organisations vendor SAP id"),
  ORG_EDIT_TYPE("org.edit.type", "Edit organisation type"),
  ORG_EDIT_MANAGING_ORG("org.edit.managing.org", "Edit organisations management"),
  ORG_EDIT_CONTRACT("org.edit.contract", "Edit organisation contract"),
  ORG_EDIT_BUDGET("org.edit.budget.", "Edit organisation budget"),
  ORG_EDIT_ANY_ROLE("org.edit.any.role", "Edit any organisations role"),
  ORG_EDIT_TEAM("org.edit.team", "Edit organisation team"),
  ORG_EDIT_PARENT_ORG("org.edit.parent.org", "Edit organisation parent"),
  ORG_EDIT_REGISTRATION_KEY("org.edit.registration.key.", "Edit organisation registration key"),

  ORG_FILTER_TEAM("org.filter.team", "Filter organisation team"),

  ORG_REQUEST_ACCESS("org.request.access", "Request access to organisations"),

  TEAM_VIEW("team.view", "View teams"),
  TEAM_ADD("team.add", "Add members to the team"),
  TEAM_EDIT("team.edit.", "Edit teams"),

  USER_PERMISSIONS("user.permissions.", "Give permissions to users"),
  USER_REMOVE("user.remove.", "Remove users"),
  USER_APPROVE("user.approve.", "Approve users"),
  USER_LIST_VIEW("user.list.view.", "View list of users"),
  USER_VIEW_THRESHOLD("user.org.view.threshold", "View users threshold"),
  USER_SET_PENDING_THRESHOLD("user.org.pending.threshold.set", "Set users pending threshold"),
  USER_CHANGE_STATUS("user.change.status", "Ability to enable/disable a user"),
  USER_EDIT("user.edit", "Edit user details"),
  USER_EDIT_PROVIDER_ROLE("user.edit.provider.role", "Edit roles in provider organisation"),
  USERS_SEARCH_GLA("users.search.gla", "Search GLA users"),
  USERS_ASSIGN_PRIMARY("users.assign.primary", "Assign primary organisation role for user "),
  USERS_ASSIGN_SIGNATORY("users.assign.signatory", "Assign user as contract authorised signatory"),
  USER_REQUEST_ORG_ADMIN("users.request.org.admin", "Request org admin role"),
  CLOSE_REQUEST_ORG_ADMIN("users.close.org.admin", "Close request org admin role"),

  PROG("prog", "View programmes"),
  PROG_MANAGE("prog.manage", "Manage programmes"),
  PROG_MANAGE_CE_CODE("prog.manage.ce.code", "Manage CE codes on programmes"),

  TEMP("temp", "View templates"),
  TEMP_MANAGE("temp.manage", "Manage templates"),
  TEMP_EDIT_INTERNAL_BLOCK("temp.edit.internal.block", "Edit template internal blocks"),
  TEMP_EDIT_EXTERNAL_BLOCK("temp.edit.external.block", "Edit template external blocks"),

  ASSESSMENT_VIEW("assessment.view", "View assessments"),
  ASSESSMENT_MANAGE("assessment.manage", "Manage assessments"),
  ASSESSMENT_TEMPLATE_MANAGE("assessment.template.manage", "Manage assessment templates"),
  
  CONS("cons", "View consortiums"),
  CONS_CREATE("cons.create", "Create consortiums"),
  CONS_EDIT("cons.edit.", "Edit consortiums"),

  PROJ("proj", "View projects"),
  PROJ_CREATE("proj.create.", "Create projects"),
  PROJ_VIEW_DETAILS("proj.view.details", "View projects details"),
  PROJ_EDIT("proj.edit.", "Edit projects"),
  PROJ_EDIT_HISTORY("proj.edit.history.", "Edit projects history"),
  PROJ_READ("proj.read.", "Read projects"),
  PROJ_SUBMIT("proj.submit.", "Submit projects"),
  PROJ_ASSESS("proj.assess", "Evaluate projects"),
  PROJ_REFER("proj.refer", "Refer projects"),
  PROJ_REINSTATE("proj.reinstate", "Reinstate projects"),
  PROJ_APPROVE("proj.approve", "Approve projects"),
  PROJ_RECOMMEND("proj.recommend", "Recommend projects"),
  PROJ_ABANDON("proj.abandon", "Abandon projects"),
  PROJ_DELETE("proj.delete", "Delete project"),
  PROJ_PAYMENT_SUSPEND("proj.payments.suspend", "Suspend Payments on the project"),
  PROJ_REJECT("proj.reject", "Reject projects"),
  PROJ_COMPLETE("proj.complete", "Complete projects"),
  PROJ_ASSIGN("proj.assign", "Assign projects"),
  PROJ_VIEW_RECOMMENDATION("proj.view.recommendation", "View recommendations on projects"),
  PROJ_VIEW_INTERNAL_BLOCKS("proj.view.internal.blocks", "View internal blocks on projects"),
  PROJ_VIEW_ASSIGNEE("proj.view.assignee", "View project assignee"),
  PROJ_EDIT_INTERNAL_BLOCKS("proj.edit.internal.blocks", "Edit internal blocks on projects"),
  PROJ_DOC_UPLOAD("proj.doc.upload", "Upload documents on projects"),
  PROJ_DOC_DELETE("proj.doc.delete", "Delete documents on projects"),
  PROJ_MILESTONE_CONDITIONAL_CREATE("proj.milestone.conditional.create",
      "Create conditional milestone on projects"),
  PROJ_MILESTONE_CONDITIONAL_EDIT("proj.milestone.conditional.edit",
      "Edit conditional milestone on projects"),
  PROJ_MILESTONE_CONDITIONAL_DELETE("proj.milestone.conditional.delete",
      "Delete conditional milestone on projects"),
  PROJ_MILESTONE_WITHDRAW("proj.milestone.withdraw",
      "Withdraw approved milestone on projects"),
  PROJ_WBS_DELETE("proj.wbs.delete", "Delete WBS on projects"),
  PROJ_OUTPUTS_EDIT_PAST("proj.outputs.editPast.", "Edit past outputs on projects"),
  PROJ_OUTPUTS_EDIT_FUTURE("proj.outputs.editFuture.", "Edit future outputs on projects"),
  PROJ_CHANGE_REPORT("proj.change.report.", "Change reports on projects"),
  PROJ_SUMMARY_REPORT("proj.summary.report.", "View summary report on projects"),
  PROJ_LEDGER_ACTUAL_CREATE("proj.ledger.actual.create", "Create actual ledger on projects"),
  PROJ_TRANSFER("proj.transfer", "Transfer projects"),
  PROJ_ADD_LABEL("proj.add.label", "Add label on projects"),
  PROJ_DOWNLOAD_ZIP("proj.download.zip", "Allow download of all file answers to project questions"),
  PROJ_REVERT_OR_DELETE_BLOCK("proj.revert.block.", "Revert or delete block on projects"),
  PROJ_SHARE("proj.share", "Share project with another organisation"),

  ANNUAL_SUBMISSION_CREATE("annual.submission.create.", "Create annual submission"),
  ANNUAL_SUBMISSION_REVERT("annual.submission.revert.", "Revert annual submission"),

  PAYMENTS("payments", "View payments"),
  VIEW_PAYMENT_HISTORY("payments.history", "View payment history on payment summary page"),
  PAYMENTS_RECLAIM_CREATE("payments.reclaim.create", "Create reclaim on payments"),
  PAYMENTS_RESEND("payments.resend", "Resend payments"),
  AUTHORISE_PAYMENT("payments.authorise.", "Authorise payments"),
  CANCEL_PAYMENT_APPROVAL("payments.cancel.approval", "Cancel Authorised Payment"),
  RECLAIM_INTEREST_PAYMENT("reclaim.payments.interest", "Reclaim interest payments"),
  FAQS("faqs", "Frequently ask questions section"),
  CONTACTS("contacts", "View contacts"),
  REPORTS_TAB("reports.tab", "View tab reports"),
  REPORTS_VIEW_STATIC("reports.view.static", "View static reports"),
  REPORTS_JASPER("reports.jasper", "View jasper reports"),
  REPORTS_INTERNAL("reports.internal.view", "View internal reports"),
  REPORTS_ADHOC("reports.adhoc", "Generate Ad-hoc reports"),

  SYS_DASHBOARD("system.dashboard", "View the system dashboard"),
  SYS_FEATURES_EDIT("system.features.edit", "Switch feature toggles on and off"),
  ADMIN_CONTENT("admin.content", "View admin content"),
  ADMIN_SKILL_PROFILES("admin.skill.profiles", "View admin skill profiles"),
  CONTRACT_TYPES("admin.contract.types", "View Contract types"),

  CORP_DASH_MARK_PROJ("corp.dash.proj.mark", "Mark projects for corporate reporting"),
  CORP_DASH_MARKED_PROJ_MESSAGE("corp.dash.proj.marked.msg", "View corporate reporting data"),

  PERMISSION_LIST_VIEW("permission.list.view", "View permissions"),
  LABELS_MANAGE("labels.manage", "Manage labels"),
  NOTIFICATION_LIST_VIEW("notification.list.view", "View notifications"),
  NOTIFICATION_SCHEDULE("notification.schedule", "Schedule notifications"),
  OVERRIDES_MANAGE("overrides.manage", "Manage overrides"),
  SAP_DATA_ERRORS("sap.errors", "Manage SAP data errors"),
  OUTPUTS_CONFIGURATION_MANAGE("outputsConfiguration.manage", "Manage outputs configurations"),

  GIVE_ACCESS_TO_ORG_VIA_TEMPLATE("organisation.give.access", "Give organisation access to projects on specific templates"),

  BROADCAST("broadcast", "Ability to view broadcast messages"),
  BROADCAST_CREATE("broadcast.create", "Ability to create a broadcast messages"),
  BROADCAST_DELETE("broadcast.delete", "Ability to delete a broadcast message"),
  BROADCAST_APPROVE("broadcast.approve", "Ability to approve a broadcast message"),

  BLOCK_USAGE("block.usage", "Ability to view block usage"),
  EMAIL_REPORTS("email.reports", "Ability to view email reports"),
  SWITCH_STORAGE("switch.storage", "Switch file storage option"),

  VIEW_PROGRAMME_ALLOCATIONS("view.programme.allocations", "Ability to view programme allocations page");


  private final String permissionKey;
  private final String permissionDescription;

  PermissionType(String permissionKey, String permissionDescription) {
    this.permissionKey = permissionKey;
    this.permissionDescription = permissionDescription;
  }

  public String getPermissionKey() {
    return permissionKey;
  }

  public String getDescription() {
    if (null == permissionDescription || permissionDescription.isEmpty()) {
      return permissionKey;
    }
    return permissionDescription;
  }

  public String applyingFor(String applicability) {
    return this.getPermissionKey() + applicability;
  }

  public String getDescriptionByKey(String key) {
    if (this.permissionKey.equalsIgnoreCase(key)) {
      return this.getDescription();
    }
    return null;
  }
}
