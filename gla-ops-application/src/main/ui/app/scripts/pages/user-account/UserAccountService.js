/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


UserAccountService.$inject = ['ConfirmationDialog', '$rootScope', 'OrganisationService', 'ToastrUtil'];


function UserAccountService(ConfirmationDialog, $rootScope, OrganisationService, ToastrUtil) {

  return {
    /**
     * @param orgRole [orgName, orgId, hasSingleRoleInThisOrg, rolName]
     * @param user [firstName, lastName, username]
     * @returns Promise
     */
    deleteUser(orgRole, user) {
      if (orgRole.hasSingleRoleInThisOrg) {
        return this.removeUserFromOrganisation(orgRole, user);
      } else {
        return this.deleteUserRole(orgRole, user);
      }
    },

    deleteUserRole(orgRole, user) {
      let fullName = user.firstName + ' ' + user.lastName;
      var message = '<p>Are you sure you want to remove this role from <strong>' + fullName +
        '</strong> from ' + orgRole.orgName + '?';

      let modal = ConfirmationDialog.delete(message);

      return modal.result.then(() => {
        $rootScope.showGlobalLoadingMask = true;
        return OrganisationService.removeUserFromRole(orgRole.orgId, user.username, orgRole.roleName)
          .then(() => {
            $rootScope.showGlobalLoadingMask = false;
            ToastrUtil.success(`User's role was removed from ${orgRole.orgName}`);
          });
      });
    },

    deleteFromTeam(orgRole, user) {
      let fullName = user.firstName + ' ' + user.lastName;
      var message = '<p>Are you sure you want to remove <strong>' + fullName +
        '</strong> from ' + orgRole.orgName + '?';

      let modal = ConfirmationDialog.show({
        message: message,
        approveText: 'REMOVE USER',
        dismissText: 'CANCEL'
      });
      return modal.result.then(() => {
        $rootScope.showGlobalLoadingMask = true;
        return OrganisationService.removeUserFromRole(orgRole.orgId, user.username, orgRole.roleName)
          .then(() => {
            $rootScope.showGlobalLoadingMask = false;
            ToastrUtil.success(`User was removed from team ${orgRole.orgName}`);
          });
      });
    },

    removeUserFromOrganisation(orgRole, user) {
      let fullName = user.firstName + ' ' + user.lastName;
      let modal, message, toastMessage;

      if(orgRole.approved){

        message = '<p>Are  you sure you want to remove <strong>' + fullName +
          '</strong> from ' + orgRole.orgName + '?</p> ' + fullName +
          ' will remain registered on GLA OPS but will no longer be assigned to ' + orgRole.orgName + '.';

        toastMessage = `User removed from ${orgRole.orgName}`;
        modal = ConfirmationDialog.delete(message);

      } else {
        toastMessage = `Pending user role rejected`;
        let modalConfig = {
          title: `Reject request`,
          approveText: 'REJECT',
          dismissText: 'CANCEL',
          message: `<p><strong>${fullName}</strong> won't have access to <strong>${orgRole.orgName}</strong> info.</p> The user will still retain their password and can request access to another organisation.`,
          showIcon: false
        };
        modal = ConfirmationDialog.show(modalConfig);
      }

      return modal.result.then(() => {
        $rootScope.showGlobalLoadingMask = true;
        return OrganisationService.removeUserFromOrganisation(orgRole.orgId, user.username)
          .then(() => {
            $rootScope.showGlobalLoadingMask = false;
            ToastrUtil.success(toastMessage);
          });
      });
    }
  }
}

angular.module('GLA').service('UserAccountService', UserAccountService);
