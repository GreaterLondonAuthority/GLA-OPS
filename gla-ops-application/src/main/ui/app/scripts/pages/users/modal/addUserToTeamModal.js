/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function AddUserToTeamModal($uibModal, $timeout, OrganisationService) {
  return {
    show: function (userProfile, teams, users) {

      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/users/modal/addUserToTeamModal.html',
        size: 'md',

        controller: class ModalCtrl {

          constructor() {
            this.numberOfUsers = users.length;
            this.selection = {};
            this.organisationService = OrganisationService;
            this.teams = teams;
            this.users = users;
            this.moreThanOneTeamManagingOrg = _.some(teams, function (t) {
              return t.organisationId != teams[0].organisationId;
            });
            this.organisationService.getTeamRoles().then(rsp => {
              this.assignableRoles = rsp.data;
            });
          }

          allowAdd() {
            var allowedToAdd = true;
            if (this.selection.selectedTeam) {
              _.forEach(this.users, function(user) {
                if (_.isUndefined(user.requestedRole)) {
                  allowedToAdd = false;
                }
              });
            } else {
              allowedToAdd = false;
            }
            return allowedToAdd;
          }

          getTeamDisplayName(team){
            if(this.moreThanOneTeamManagingOrg){
              return `${team.organisationName} : ${team.teamName}`
            }
            return team.teamName;
          }
        }
      });
    }
  }
}

  AddUserToTeamModal.$inject = ['$uibModal', '$timeout', 'OrganisationService'];

  angular.module('GLA')
    .service('AddUserToTeamModal', AddUserToTeamModal);
