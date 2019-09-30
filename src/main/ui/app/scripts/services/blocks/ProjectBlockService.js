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
     * @param displayOrder of the block
     */
    getHistory(projectId, displayOrder){
      return $http.get(`${config.basePath}/projects/${projectId}/displayOrder/${displayOrder}/history`);
    },

    approveBlock(projectId, blockId){
      return $http.put(`${config.basePath}/projects/${projectId}/${blockId}/approve`);
    },

    deleteBlock(projectId, blockId){
      return $http.delete(`${config.basePath}/projects/${projectId}/blocks/${blockId}`);
    },

    revertBlock(projectId, blockId){
      return $http.put(`${config.basePath}/projects/${projectId}/block/${blockId}/revert`);
    },

    updateBlock(projectId, blockId, block, releaseLock) {
      return $http.put(`${config.basePath}/projects/${projectId}/blocks/${blockId}?releaseLock=${!!releaseLock}`, block, releaseLock)
    },

    updateInternalBlock(projectId, block){
      return $http.put(`${config.basePath}/projects/${projectId}/internalBlocks/${block.id}`, block);
    }
  };
}

angular.module('GLA')
  .service('ProjectBlockService', ProjectBlockService);
