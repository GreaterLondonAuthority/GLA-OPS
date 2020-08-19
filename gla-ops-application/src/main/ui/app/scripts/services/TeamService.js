/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

TeamService.$inject = ['$http', 'config'];

function TeamService($http, config) {

  return {
    getTeams(params) {
      return $http({
        url: `${config.basePath}/teams`,
        method: 'GET',
        params: params
      })
    },

    saveTeam(team){
      return $http.post(`${config.basePath}/teams`, team)
    },

    updateTeam(team){
      return $http.put(`${config.basePath}/teams/${team.id}`, team)
    },

    deleteTeam(team) {
      return $http.delete(`${config.basePath}/teams/${team.id}`)
    },

    isTeamNameUnique(team, orgTeams){
      let existingTeam = _.find(orgTeams, (t) => {
        t = t || {name: ''};
        return t.organisationId === team.organisationId && t.name.toLowerCase() === (team.name || '').toLowerCase();
      });

      if(existingTeam && existingTeam.id != team.id){
        return false;
      }
      return true;
    }
  };
}

angular.module('GLA')
  .service('TeamService', TeamService);
