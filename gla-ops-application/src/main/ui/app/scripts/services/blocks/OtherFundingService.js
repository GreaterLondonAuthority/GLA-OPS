/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

OtherFundingService.$inject = ['$http', 'config'];

function OtherFundingService($http, config) {

  return {
    attachEvidence(projectId, blockId, otherFundingId, fileId) {
      return $http.put(`${config.basePath}/otherFunding/project/${projectId}/block/${blockId}/item/${otherFundingId}/file/${fileId}`);
    },

    deleteEvidence(projectId, blockId, otherFundingId, fileId) {
      return $http.delete(`${config.basePath}/otherFunding/project/${projectId}/block/${blockId}/item/${otherFundingId}/file/${fileId}`);
    },

  };
}

angular.module('GLA')
  .service('OtherFundingService', OtherFundingService);
