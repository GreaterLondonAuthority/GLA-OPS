/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function RequestAdditionalRoleModal($uibModal, $timeout, UserService) {
  return {
    show: function (userProfile) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/user-account/modal/requestAdditionalRoleModal.html',
        size: 'md',
        controller: [function () {
          var ctrl = this;
          this.userProfile = userProfile;
          this.selection = {};
          if (this.userProfile.assignableOrganisations.length === 1) {
            this.selection.selectedOrganisation = this.userProfile.assignableOrganisations[0];
          }

          $timeout(function(){
            ctrl.focusSummary = true;
          },100);
        }]
      });
    }
  }
}

RequestAdditionalRoleModal.$inject = ['$uibModal', '$timeout', 'UserService'];

angular.module('GLA')
  .service('RequestAdditionalRoleModal', RequestAdditionalRoleModal);
