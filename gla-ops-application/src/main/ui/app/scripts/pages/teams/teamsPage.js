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

    this.initCurrentState();
    this.initSearchDropdown();
    this.initManagedByDropdown();
    this.initStatusDropdown();

    this.itemsPerPage = 50;
    this.currentPage = 1;
    this.getTeams();

    this.canAddTeams = this.UserService.hasPermission('team.add');
  }

  initCurrentState() {
    this.currentState = {
      titleForBackBtn: 'TEAMS',
      name: this.$state.current.name,
      params: angular.copy(this.$state.params)
    };
  }

  initSearchDropdown() {
    this.searchOptions = [
      {
        name: 'title',
        description: 'Team',
        hint: 'Enter team name',
        maxLength: '50'
      }
    ];

    this.selectedSearchOption = this.searchOptions[0];
  }

  initManagedByDropdown() {
    this.managingOrganisationsDropdown = _.filter(this.managingOrganisations, {isManagingOrganisation: true})
      .map(o => {
        return {
          id: o.id,
          label: o.name
        }
      });
  }

  initStatusDropdown() {
    this.orgStatusDropdown = [
      {
        id: 'Approved',
        label: 'Active'
      },
      {
        id: 'Inactive',
        label: 'Inactive'
      }
    ];
  };

  getTeams() {
    let managingOrgIds = this.getSelectedCheckboxes(this.managingOrganisationsDropdown);
    let orgStatuses = this.getSelectedCheckboxes(this.orgStatusDropdown);
    let params = {
      searchText: this.searchText,
      managingOrgIds: managingOrgIds.join(','),
      orgStatuses: orgStatuses.join(','),
      page: this.currentPage - 1,
      size: this.itemsPerPage,
      sort: ['organisationName,asc', 'name,asc']
    };
    this.TeamService.getTeams(params).then(rsp => {
      this.teams = rsp.data;
    });

    this.showReset = managingOrgIds.length || orgStatuses.length || this.searchText;
  }

  getSelectedCheckboxes(checkboxesDropdown) {
    return checkboxesDropdown.reduce((selectedValues, item) => {
      if (item.model) {
        selectedValues.push(item.id)
      }
      return selectedValues;
    }, []);
  }

  clearAll() {
    this.searchText = null;
    this.initSearchDropdown();
    this.initManagedByDropdown();
    this.initStatusDropdown();
    this.getTeams();
  }

  getTeamStatus(team) {
    if (team.status === 'Approved') {
      return 'Active';
    }
    else {
      return team.status;
    }
  }

  showTeamModal(orgTeam) {
    let modal = this.TeamModal.show(orgTeam, this.managingOrganisations, this.teams.content);
    modal.result.then((team) => {
      let apiRequest;
      if (team.id) {
        apiRequest = this.TeamService.updateTeam(team)
      } else {
        apiRequest = this.TeamService.saveTeam(team);
      }

      apiRequest.then(()=>{
        this.$state.reload();
        this.ToastrUtil.success(team.id ? 'Team updated' : 'New team created');
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

