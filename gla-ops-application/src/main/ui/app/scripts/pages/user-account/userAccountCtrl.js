/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './UserAccountService.js'

class UserAccountCtrl {
  constructor(userProfile, userThresholds, UserService, $state, ConfirmationDialog, UserPasswordReset, OrganisationService, ToastrUtil, $rootScope, ModalDisplayService, FeatureToggleService, RequestAdditionalRoleModal, UserAccountService, ErrorService) {
    this.userProfile = userProfile;
    this.userThresholds = userThresholds;
    this.UserService = UserService;
    this.$state = $state;
    this.ConfirmationDialog = ConfirmationDialog;
    this.UserPasswordReset = UserPasswordReset;
    this.OrganisationService = OrganisationService;
    this.ToastrUtil = ToastrUtil;
    this.$rootScope = $rootScope;
    this.ModalDisplayService = ModalDisplayService;
    this.FeatureToggleService = FeatureToggleService;
    this.RequestAdditionalRoleModal = RequestAdditionalRoleModal;
    this.UserAccountService = UserAccountService;
    this.ErrorService = ErrorService;

    this.FeatureToggleService.isFeatureEnabled('AllowMultipleRolesProcess').subscribe((resp)=>{
      this.followNewProcess = resp;
    });

    this.showThresholds = !!userThresholds.length;
    this.canRequestOrgAdmin = UserService.hasPermission('users.request.org.admin');
    this.canCloseeOrgAdminRequest = UserService.hasPermission('users.close.org.admin');
    this.canSetThresholds = UserService.hasPermission('user.org.pending.threshold.set');
    this.canEditPrimaryOrg = UserService.hasPermission('users.assign.primary');
    this.showAuthorisedSignatory = UserService.hasPermission('users.assign.signatory');
    this.canChangeUserStatus = UserService.hasPermission('user.change.status');
    this.canEditUserName = (UserService.hasPermission('user.edit') || this.isUserOwnRecord());
    this.userProfile.organisations = _.sortBy(this.userProfile.organisations, item => item.orgName.toLowerCase());

    this.orgIdToThresholdItem = {};
    (userThresholds || []).forEach(item => this.refreshThreshold(item));
    this.userProfile.organisations.forEach(org => {
      org.threshold = this.orgIdToThresholdItem[org.orgId];
      org.isAssignableRole = this.isAssignableRole(org);
      if(!org.approved){
        org.roleName = null;
      }
      org.canApprove = UserService.hasPermission('user.approve', org.orgId);
      org.canRemove = UserService.hasPermission('user.remove', org.orgId);
      // org.isEditable = UserService.hasPermission('org.edit.details', org.orgId);
      org.isEditable = org.canApprove || org.canRemove || this.canEditProviderRole(org);
      org.defaultRole = (_.find(org.assignableRoles, {default: true}) || {}).description;

    });
    let canEditAnyOrg = (this.userProfile.organisations || []).some(org => org.isEditable);
    let canSetThresholds = UserService.hasPermission('user.org.pending.threshold.set');

    this.editable = this.canEditUserName || (canSetThresholds && this.showThresholds) || canEditAnyOrg
                       || this.showAuthorisedSignatory;
    this.readOnly = (this.editable && this.$state.params.editMode)? false : true;

    this.showManagedBy = _.some(this.userProfile.organisations, (org) => {return !!org.managingOrgName});
    this.authorisedSignatoryTooltip = UserService.getAuthorisedSignatoryTooltip();
    this.actionsColumnToolTop = UserService.getActionsColumnToolTop();
    console.log('userProfile', this.userProfile)
  }

  canEditProviderRole(org){
    return this.UserService.hasPermission('user.edit.provider.role') &&
      this.UserService.currentUser().organisations.some(o => o.isManagingOrganisation && o.id == org.managingOrgId);
  }

  isUserOwnRecord(){
    let loggedInUserName = this.UserService.currentUser().username
    let currentUserRecord = this.userProfile.username
    return loggedInUserName === currentUserRecord
  }

  edit() {
    this.readOnly = false;
  }

  canChangeAuthorisedSignatory(role){
     return this.UserService.canChangeAuthorisedSignatory(role)
  }

  resetUserPassword() {
    let modal = this.UserPasswordReset.show({
      title: 'Reset password for user ' + this.userProfile.username,
    })
    modal.result.then(result => {
      return this.UserService.resetUserPassword(this.userProfile.username, result)
      .then(resp => {
        this.ConfirmationDialog.show({
          title: 'Password changed',
          message: `Password changed for user ${this.userProfile.username}`,
          approveText: 'OK',
          showIcon: false,
          showDismiss: false,
        })
      })
      .catch(err => {
        this.ConfirmationDialog.warn(err.data ? err.data.description : null);
      })
    })
  }


  changeUserStatus() {
    let modal = this.ConfirmationDialog.show({
      title: `Are you sure you want to ${this.userProfile.enabled ? 'deactivate' : 'activate'} this user?`,
      message: this.userProfile.enabled ? `This will prevent the user from logging into OPS` : `This will allow the user to login to OPS`,
      approveText: 'YES, CONTINUE',
      dismissText: 'NO, CANCEL'
    });
    modal.result.then(() => {
      return this.UserService.updateUserStatus(this.userProfile.username, !this.userProfile.enabled)
        .then(rsp => {
          this.userProfile.enabled = !this.userProfile.enabled
        })
        .catch(err => {
          this.ConfirmationDialog.warn(err.data ? err.data.description : null);
        });
    });
  }

  primaryChange(organisationId, roleName) {
    this.UserService.updatePrimaryOrganisation(this.userProfile.username, organisationId, roleName);
  }

  refreshThreshold(apiThreshold, disableEdit){
    apiThreshold.isSet = apiThreshold.requesterUsername || apiThreshold.approverUsername;
    apiThreshold.isApproved = apiThreshold.approverUsername && apiThreshold.pendingThreshold == null;
    apiThreshold.isPending = apiThreshold.isSet && !apiThreshold.isApproved;
    let currentUser = this.UserService.currentUser().username;

    apiThreshold.canBeApproved = apiThreshold.isPending && apiThreshold.requesterUsername !== currentUser && apiThreshold.id.username !== currentUser;
    apiThreshold.canBeEdited = disableEdit? false : !apiThreshold.canBeApproved;
    apiThreshold.canBeCanceled = apiThreshold.canBeEdited && apiThreshold.isPending;
    apiThreshold.viewValue = apiThreshold.isApproved? apiThreshold.approvedThreshold : apiThreshold.pendingThreshold;
    apiThreshold.originalVieValue = apiThreshold.viewValue;
    let originalThreshold = this.orgIdToThresholdItem[apiThreshold.id.organisationId];
    this.orgIdToThresholdItem[apiThreshold.id.organisationId] = _.merge(originalThreshold, apiThreshold);
    // console.log('apiThreshold', apiThreshold);
  }

  updatePendingThreshold(thresholdObj) {
    thresholdObj.isPending = true;
    this.UserService.updateUserThreshold(thresholdObj.id.username, thresholdObj.id.organisationId, thresholdObj.viewValue || 0).then(rsp => {
      this.refreshThreshold(rsp.data);
    })
  }

  getThreshold(orgId) {
    return this.orgIdToThresholdItem[orgId];
  }

  showRequestRoleModal() {
    var modal = this.RequestAdditionalRoleModal.show(this.userProfile);
    modal.result.then(selected => {
      this.UserService.addAdditionalUserRole(this.userProfile.username, selected.selectedOrganisation.id, selected.selectedRole.name).then(() =>
        {
          this.ToastrUtil.success('The new role has been added to this user.');
          this.refresh(true);

        }
      )

    });
  }

  signatoryChange(organisationId, roleName, signatory) {
    this.UserService.updateAuthorisedSignatory(this.userProfile.username, organisationId, roleName, signatory);
  }

  approve(thresholdObj) {
    this.UserService.approveUserThreshold(thresholdObj.id.username, thresholdObj.id.organisationId).then(rsp => {
      this.refreshThreshold(rsp.data, true);
    })
  }

  decline(thresholdObj) {
    let modal = this.ConfirmationDialog.show({
      approveText: 'DECLINE',
      dismissText: 'CANCEL',
      message: 'Are you sure you want to decline the spend authority change?'
    });
    modal.result.then(() => {
      return this.UserService.declineUserThreshold(thresholdObj.id.username, thresholdObj.id.organisationId).then(rsp => {
        this.refreshThreshold(rsp.data);
      });
    });
  }

  cancel(thresholdObj){
    return this.UserService.declineUserThreshold(thresholdObj.id.username, thresholdObj.id.organisationId).then(rsp => {
      this.refreshThreshold(rsp.data);
    });
  }


  requestOrgAdminRoleForOrg(organisationId){
    this.requestOrgAdminRole()
    this.UserService.requestOrgAdminRole(this.userProfile.username,organisationId)
  }

  closeRequestOrgAdminRoleForOrg(organisationId){
    this.UserService.closeOrgAdminRole(this.userProfile.username,organisationId)
  }

  requestOrgAdminRole(organisationId, orgName){
    let modal = this.ConfirmationDialog.show({
      title: `Request Organisation Admin Role`,
      message:
        '<div align="left" class="mleft30"> <li>The existing Organisation Admin for ' + orgName + '  is no longer able to carry out this role and should have their OPS account deactivated.</li>' +
        '<li>I\'m authorised by ' + orgName + ' to be assigned as its new Organisation Admin.</li></div>',
      showConfirmationCheckbox: 'true',
      approveText: 'Request',
      dismissText: 'Cancel'
    });
    modal.result.then(() => {
      return this.UserService.requestOrgAdminRole(this.userProfile.username, organisationId).then(() => {
        this.ToastrUtil.success('Thank you for your request. We aim to respond within two working days.');
        this.refresh(true);
      });
    });

  }

  closeOrgAdminRole(organisationId){
    let modal = this.ConfirmationDialog.show({
      title: `Close Request`,
      message: 'Are you sure you want to close this user\'s request to assign them the Organisation Admin role?',
      approveText: 'Close Request',
      dismissText: 'Cancel'
    });
    modal.result.then(() => {
      return this.UserService.closeOrgAdminRole(this.userProfile.username, organisationId).then(() => {
        this.refresh(true);
      });
    });
  }

  deleteUser(role) {
    this.UserAccountService.deleteUser(role, this.userProfile).then(()=>{
      if (role.hasSingleRoleInThisOrg && this.userProfile.organisations.length === 1) {
        this.$state.go('users');
      } else {
        this.refresh(true);
      }
    });
  }

  approveUser(org) {
    this.$rootScope.showGlobalLoadingMask = true;
    this.OrganisationService.approveUser(org.orgId, this.userProfile.username, org.roleName, org.authorisedSignatory)
      .then(resp => this.refresh(true))
      .catch(this.ErrorService.apiValidationHandler())
  }

  isAssignableRole(org) {
    return !!_.find(org.assignableRoles, {name: org.roleName});
  };

  updateUserRole(org, roleId) {
    if(!org.approved){
      return;
    }
    this.$rootScope.showGlobalLoadingMask = true;
    this.UserService.updateUserRole(this.userProfile.username, org.orgId, roleId)
      .then(resp => this.refresh(true));
  }

  refresh(editMode){
    this.$state.params.editMode = !!editMode;
    return this.$state.go(this.$state.current, this.$state.params, {reload: true});
  }

  save() {
    if (this.canEditUserName && this.userProfile.firstName && this.userProfile.lastName) {
      this.UserService.updateUserDetails(this.userProfile)
    }
    this.back();
  }

  back() {
    this.$state.go('users');
  }

}

UserAccountCtrl.$inject = ['userProfile', 'userThresholds', 'UserService', '$state', 'ConfirmationDialog', 'UserPasswordReset', 'OrganisationService', 'ToastrUtil', '$rootScope', 'ModalDisplayService', 'FeatureToggleService','RequestAdditionalRoleModal', 'UserAccountService', 'ErrorService'];


angular.module('GLA')
  .controller('UserAccountCtrl', UserAccountCtrl);
