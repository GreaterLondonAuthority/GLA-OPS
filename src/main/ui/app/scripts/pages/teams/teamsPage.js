/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './teamModal.js'

class TeamsCtrl {
  constructor($state, ToastrUtil, TeamModal, TeamService, UserService, ErrorService, ConfirmationDialog) {
    this.$state = $state;
    this.ToastrUtil = ToastrUtil;
    this.TeamModal = TeamModal;
    this.TeamService = TeamService;
    this.UserService = UserService;
    this.ErrorService = ErrorService;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
    this.managingOrganisations = this.UserService.currentUserOrganisations('team.edit');
    this.canAddTeams = this.UserService.hasPermission('team.add');
    (this.teams || []).forEach(team => {
      team.canBeEdited = this.UserService.hasPermission('team.edit', team.organisationId);
    });
  }

  showTeamModal(orgTeam) {
    let modal = this.TeamModal.show(orgTeam, this.managingOrganisations, this.teams);
    modal.result.then((team) => {
      let apiRequest;
      if (team.id) {
        apiRequest = this.TeamService.updateTeam(team)
      } else {
        apiRequest = this.TeamService.saveTeam(team);
      }

      apiRequest.then(()=>{
        this.$state.reload();
        this.ToastrUtil.success(team.id ? 'Team Updated' : 'Team Added');
      }).catch(this.ErrorService.apiValidationHandler());
    });
  }


  delete(team){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the team?');
    modal.result.then(() => {
      this.TeamService.deleteTeam(team).then(rsp => {
        this.$state.reload();
        this.ToastrUtil.success('Team Deleted');
      }).catch(this.ErrorService.apiValidationHandler())
    });
  }

}

TeamsCtrl.$inject = ['$state', 'ToastrUtil', 'TeamModal', 'TeamService', 'UserService', 'ErrorService', 'ConfirmationDialog'];


angular.module('GLA')
  .component('teamsPage', {
    templateUrl: 'scripts/pages/teams/teamsPage.html',
    bindings: {
      teams: '<'
    },
    controller: TeamsCtrl
  });

