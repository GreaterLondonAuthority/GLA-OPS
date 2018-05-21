/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class UserAccountCtrl {
  constructor(userProfile, userThresholds, UserService, $state, ConfirmationDialog, OrganisationService, ToastrUtil, $rootScope) {
    this.userProfile = userProfile;
    this.userThresholds = userThresholds;
    this.UserService = UserService;
    this.$state = $state;
    this.ConfirmationDialog = ConfirmationDialog;
    this.OrganisationService = OrganisationService;
    this.ToastrUtil = ToastrUtil;
    this.$rootScope = $rootScope;
    this.showThresholds = !!userThresholds.length;

    this.orgIdToThresholdItem = {};
    (userThresholds || []).forEach(item => this.refreshThreshold(item));
    this.userProfile.organisations.forEach(org => {
      org.threshold = this.orgIdToThresholdItem[org.orgId];
      org.isAssignableRole = this.isAssignableRole(org);
      org.isEditable = UserService.hasPermission('org.edit.details', org.orgId);
      org.defaultRole = (_.find(org.assignableRoles, {default: true}) || {}).description;

    });
    let canEditAnyOrg = (this.userProfile.organisations || []).some(org => org.isEditable);
    let canSetThresholds = UserService.hasPermission('user.org.pending.threshold.set');

    this.editable = (canSetThresholds && this.showThresholds) || canEditAnyOrg;
    this.readOnly = (this.editable && this.$state.params.editMode)? false : true;
    console.log('userProfile', this.userProfile)
  }

  edit() {
    this.readOnly = false;
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


  approve(thresholdObj) {
    this.UserService.approveUserThreshold(thresholdObj.id.username, thresholdObj.id.organisationId).then(rsp => {
      this.refreshThreshold(rsp.data, true);
    });
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

  deleteUser(org) {
    let fullName = this.userProfile.firstName + ' ' + this.userProfile.lastName;
    var message = '<p>Are  you sure you want to remove <strong>' + fullName +
      '</strong> from ' + org.orgName + '?</p> ' + fullName +
      ' will remain registered on GLA OPS but will no longer be assigned to ' + org.orgName + '.';

    var modal = this.ConfirmationDialog.delete(message);

    modal.result.then(() => {
      this.$rootScope.showGlobalLoadingMask = true;
      this.OrganisationService.removeUserFromOrganisation(org.orgId, this.userProfile.username)
        .then(() => {
          if(this.userProfile.organisations.length === 1){
            this.$state.go('users');
          }else{
            this.refresh(true);
          }
          this.ToastrUtil.success(`User removed from ${org.orgName}`);
        });
    });
  }

  approveUser(org) {
    this.$rootScope.showGlobalLoadingMask = true;
    this.OrganisationService.approveUser(org.orgId, this.userProfile.username)
      .then(resp => this.refresh(true));
  }

  isAssignableRole(org) {
    return !!_.find(org.assignableRoles, {name: org.roleName});
  };

  updateUserRole(org, roleId) {
    this.$rootScope.showGlobalLoadingMask = true;
    this.UserService.updateUserRole(this.userProfile.username, org.orgId, roleId)
      .then(resp => this.refresh(true));
  }

  refresh(editMode){
    this.$state.params.editMode = !!editMode;
    return this.$state.go(this.$state.current, this.$state.params, {reload: true});
  }

  save() {
    this.back();
  }

  back() {
    this.$state.go('users');
  }

}

UserAccountCtrl.$inject = ['userProfile', 'userThresholds', 'UserService', '$state', 'ConfirmationDialog', 'OrganisationService', 'ToastrUtil', '$rootScope'];


angular.module('GLA')
  .controller('UserAccountCtrl', UserAccountCtrl);
