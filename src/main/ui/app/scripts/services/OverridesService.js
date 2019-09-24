/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

OverridesService.$inject = ['$http', 'config'];

function OverridesService($http, config) {
  return {
    getAllOverrides(){
      return $http.get(`${config.basePath}/allOverrides`);
    },

    getOverridesByProjectId: function (projectId) {
      let cfg = {params: {projectId: projectId || null,}};
      return $http.get(`${config.basePath}/overrides`, cfg);
    },

    createOverride(data) {
      return $http.post(`${config.basePath}/override`, data);
    },

    updateOverride(data) {
      return $http.put(`${config.basePath}/override/${data.id}`, data);
    },

    deleteOverride(data) {
      return $http.delete(`${config.basePath}/override/${data.id}`);
    },

    getMetadata() {
      return $http.get(`${config.basePath}/overrides/metadata`);
    },

  }
}

angular.module('GLA')
  .service('OverridesService', OverridesService);
