/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



class OrganisationCtrl {
  constructor($rootScope, $state, $log, OrganisationService, UserService, ConfirmationDialog, ToastrUtil) {
    this.$rootScope = $rootScope;
    this.$state = $state;
    this.OrganisationService = OrganisationService;
    this.UserService = UserService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.ToastrUtil = ToastrUtil;

    this.users = [];
    this.$log = $log;
    this.editable = UserService.hasPermission('org.edit.details', $state.params.orgId);
    this.canViewSapId = UserService.hasPermission('org.view.vendor.sap.id', $state.params.orgId);
    this.userRoleEditOverride = UserService.hasPermission('org.edit.any.role');
    this.readOnly = true;
    this.userRoles = this.availableUserRoles;
    this.showUsers = !!(this.availableUserRoles || []).length;

    this.userRolesMap = (this.userRoles || []).reduce((result, role) => {
      result[role.name] = role;
      return result;
    }, {});

    this.defaultRole = _.find(this.userRoles, {default: true});

    this.setData(this.organisation);

    this.imsNumberLabel = OrganisationService.getImsLabel(this.org);
    this.userRegStatusDropdown = this.OrganisationService.userRegStatuses();
  }

  refreshDetails() {
    this.$rootScope.showGlobalLoadingMask = true;

    // retrieve details
    // console.warn('e2e debug2:' + this.$state.params.orgId);
    this.OrganisationService.getDetails(this.$state.params.orgId)
      .then(response => {
        // console.warn('e2e debug3:' + JSON.stringify(response, null, 2));

        this.setData(response);
        this.$rootScope.showGlobalLoadingMask = false;
      });
  }

  setUserRegFilter() {
    const selections = this.userRegStatusDropdown.reduce((selectedValues, item) => {
      if (item.model) {
        selectedValues.push(item.id)
      }
      return selectedValues;
    }, []);

    if (selections.length === 1) {
      this.userRegFilter = {approved: selections[0] === 'Approved'};
    } else {
      this.userRegFilter = null;
    }
  }


  setData(apiData) {
    // console.warn('e2e setData:' + new Date());
    // console.warn('e2e setData:' + JSON.stringify(apiData, null, 2));

    this.org = apiData.data;
    this.users = apiData.data.users;

    //filter role matching this organisation
    this.users = _.each(this.users, user => {
      user.currentOrgRole = _.find(user.roles, {
        organisationId: this.org.id
      });
    });
  }


  edit() {
    this.$state.go('organisation-edit', {
      orgId: this.org.id,
      orgDetails: this.org
    });
  }


  approveUser(user) {
    this.$rootScope.showGlobalLoadingMask = true;

    this.OrganisationService.approveUser(this.org.id, user.username)
      .then(resp => {
        this.refreshDetails();
      })
      .catch(error => {
        this.$log.error('could not approve user ' + user.username);
      });
  }

  approveOrg(){
    this.OrganisationService.approveOrganisation(this.org.id).then((resp)=>{
      // console.warn('e2e debug:' + JSON.stringify(resp, null, 2));
      this.ToastrUtil.success('Organisation approved');
      this.refreshDetails();
    });
  }

  remove(user) {
    var message = '<p>Are  you sure you want to remove <strong>' + user.firstName + ' ' + user.lastName +
      '</strong> from ' + this.org.name + '?</p> ' + user.firstName + ' ' + user.lastName +
      ' will remain registered on GLA OPS but will no longer be assigned to ' + this.org.name + '.';

    var modal = this.ConfirmationDialog.delete(message);

    modal.result
      .then(() => {
        this.$rootScope.showGlobalLoadingMask = true;
        this.OrganisationService.removeUserFromOrganisation(this.org.id, user.username)
          .then(() => {
            _.remove(this.org.users, user);
            this.$rootScope.showGlobalLoadingMask = false;
            this.ToastrUtil.success('User removed from ' + this.org.name);
          });
      });
  }


  getRoleDescription(roleId, user) {
    //let role = this.assignableRole(roleId);
    let role = this.userRolesMap[roleId];
    if (!role) {
      role = _.find(user.roles, {organisationId: this.org.id, name: roleId});
    }
    return (role || {}).description || 'Role Not Found';
  }

  assignableRole(roleId) {
    // check if new permission || existing logic
    if(this.userRoleEditOverride) {
      return true;
    }
    return this.userRolesMap[roleId];
  };


  userRoleUpdated(user, roleId) {
    this.$rootScope.showGlobalLoadingMask = true;
    this.UserService.updateUserRole(user.username, this.org.id, roleId)
      .then(resp => {
        this.$log.log(resp);
        this.refreshDetails();
      });
  }

  back() {
    this.$state.go('organisations');
  }
}

OrganisationCtrl.$inject = ['$rootScope', '$state', '$log', 'OrganisationService', 'UserService', 'ConfirmationDialog', 'ToastrUtil'];


angular.module('GLA')
  .component('organisationPage', {
    templateUrl: 'scripts/pages/organisation/organisation.html',
    bindings: {
      organisationTypes: '<',
      organisation: '<',
      availableUserRoles: '<'
    },
    controller: OrganisationCtrl,
  });
