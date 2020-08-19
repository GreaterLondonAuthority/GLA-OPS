/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function TeamModal($uibModal, TeamService) {
  return {
    show: function (team, managingOrganisations, teams) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/teams/teamModal.html',
        size: 'md',
        controller: [function () {
          this.isUniqueName = true;
          this.team = angular.copy(team || {});
          if(!this.team.id && managingOrganisations.length === 1){
            this.team.organisationId = managingOrganisations[0].id;
          }
          this.managingOrganisations = managingOrganisations || [];

          this.onManagingOrgSelect = () => {
            this.validateTeamName(this.team);
          };

          this.sgwAccessSelectable = () => {
            if (this.team.organisationId) {
              let selectedManagingOrg = _.find(this.managingOrganisations, {id: this.team.organisationId});
              return selectedManagingOrg.skillsGatewayAccessAllowed;
            }
            return false;
          };

          //This works because we don't use pagination
          this.validateTeamName = (team) => {
            this.isUniqueName = TeamService.isTeamNameUnique(team, teams)
          };
        }]
      });
    }
  }
}

TeamModal.$inject = ['$uibModal', 'TeamService'];

angular.module('GLA')
  .service('TeamModal', TeamModal);
