/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

MilestonesService.$inject = ['$http', 'config'];

function MilestonesService($http, config) {

  return {
    claimActions: {
      claim: 1,
      return: 2,
      cancel: 3
    },

    getMilestoneBlock(projectId, blockId){
      return $http.get(`${config.basePath}/projects/${projectId}/milestones/${blockId}`);
    },

    claimMilestone(projectId, milestoneId, data) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/milestones/${milestoneId}/claim`,
        method: 'PUT',
        data: data
      });
    },

    cancelClaim(projectId, milestoneId) {
      return $http.put(`${config.basePath}/projects/${projectId}/milestones/${milestoneId}/cancelClaim`);
    },

    hasForecastInThePast(milestones) {
      return (milestones || []).some(m => {
        return new Date(m.milestoneDate) <= new Date() && m.milestoneStatus === 'FORECAST';
      });
    },

    attachEvidence(projectId, blockId, milestoneId, fileId) {
      return $http.put(`${config.basePath}/projects/${projectId}/milestones/${blockId}/milestone/${milestoneId}/file/${fileId}`);
    },

    deleteEvidence(projectId, blockId, milestoneId, attachmentId) {
      return $http.delete(`${config.basePath}/projects/${projectId}/milestones/${blockId}/milestone/${milestoneId}/attachment/${attachmentId}`);
    },

    setMilestoneAsNotApplicable(milestone){
      milestone.notApplicable = true;
      milestone.milestoneDate = null;
      milestone.monetarySplit = 0;
      milestone.claimStatus = null;
      milestone.milestoneStatus = null;
      return milestone;
    }
  }
}

angular.module('GLA')
  .service('MilestonesService', MilestonesService);
