/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ProjectBlockService.$inject = ['$http', 'config'];

function ProjectBlockService($http, config) {

  return {
    /**
     * Get block history
     * @param projectId project id
     * @param blockId id of the block
     */
    getHistory(projectId, blockId){
      return $http.get(`${config.basePath}/projects/${projectId}/${blockId}/history`);
    },

    approveBlock(projectId, blockId){
      return $http.put(`${config.basePath}/projects/${projectId}/${blockId}/approve`);
    },

    deleteBlock(projectId, blockId){
      return $http.delete(`${config.basePath}/projects/${projectId}/blocks/${blockId}`);
    }
  };
}

angular.module('GLA')
  .service('ProjectBlockService', ProjectBlockService);
