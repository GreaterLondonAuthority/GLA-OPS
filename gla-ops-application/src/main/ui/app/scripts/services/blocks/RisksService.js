/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const INTERNAL_RISK_BLOCK_TYPE = 'Risk';

RisksService.$inject = ['$http', 'config', 'orderByFilter'];

function RisksService($http, config, orderByFilter) {
  return {

    getRiskCategories: (id) => {
      return $http({
        url: `${config.basePath}/categoryValues/RiskCategory`,
        method: 'GET'
      }).then(rsp => rsp.data);
    },


    postNewRiskOrIssue: (projectId, blockId, data) => {
      return $http({
        method: 'POST',
        url: `${config.basePath}/projects/${projectId}/risks/${blockId}/risk`,
        data: data
      });
    },

    updateNewRiskOrIssue: (projectId, blockId, data) => {
      return $http({
        method: 'PUT',
        url: `${config.basePath}/projects/${projectId}/risks/${blockId}/risk/${data.id}`,
        data: data
      });
    },

    closeRiskOrIssue: (projectId, blockId, riskId) => {
      return $http({
        method: 'PUT',
        url: `${config.basePath}/projects/${projectId}/risks/${blockId}/risk/${riskId}/close`
      });
    },

    deleteRiskOrIssue: (projectId, blockId, riskId) => {
      return $http({
        method: 'DELETE',
        url: `${config.basePath}/projects/${projectId}/risks/${blockId}/risk/${riskId}`,
      });
    },

    postNewAction: (projectId, blockId, riskId, data) => {
      return $http({
        method: 'POST',
        url: `${config.basePath}/projects/${projectId}/risks/${blockId}/risk/${riskId}/action`,
        data: data
      });
    },

    deleteAction: (projectId, blockId, riskId, actionId) => {
      return $http({
        method: 'DELETE',
        url: `${config.basePath}/projects/${projectId}/risks/${blockId}/risk/${riskId}/action/${actionId}`
      });
    },


    getOverallRatings: () => {
      return [
        {
          id: 1,
          color: 'Green',
          description: 'Low risk level'
        },
        {
          id: 2,
          color: 'Amber',
          description: 'Moderate risk level'
        },
        {
          id: 3,
          color: 'Red',
          description: 'Significant risk level'
        }
      ];
    },
    getProbabilityRating: (includeNA) => {

      let tab = [{
        displayValue: '1 - Remote: 0-25 % chance of materialising',
        value: 1
      },
      {
        displayValue: '2 - Improbable: 26 - 50% chance of materialising',
        value: 2
      },
      {
        displayValue: '3 - Probable: 51 - 75% chance of materialising',
        value: 3
      },
      {
        displayValue: '4 - Highly likely: 76 - 100% chance of materialising',
        value: 4
      }];
      if(includeNA){
        tab.push({
          displayValue: 'N/A - No mitigation in place',
          value: null
        });
      }
      return tab;
    },
    getImpactRating: (includeNA) => {

      let tab = [{
        displayValue: '1 - Moderate impact',
        value: 1
      },
      {
        displayValue: '2 - Significant impact',
        value: 2
      },
      {
        displayValue: '3 - Substantial impact',
        value: 3
      },
      {
        displayValue: '4 - Catastrophic impact',
        value: 4
      }];

      if(includeNA){
        tab.push({
          displayValue: 'N/A - No mitigation in place',
          value: null
        });
      }
      return tab;
    },
    getIssueImpactLevels: () => {
      return [{
        displayValue: 'Green - Low impact',
        value: 1
      }, {
        displayValue: 'Amber - Moderate impact',
        value: 2
      }, {
        displayValue: 'Red - Significant impact',
        value: 3
      }];
    },
    getIssueImpactLevelsDisplayMap: () => {
      return {
        1: {
            displayValue: 'Green - Low',
            css: 'impact-level-green'
        },
        2: {
            displayValue: 'Amber - Moderate',
            css: 'impact-level-amber'
        },
        3: {
            displayValue: 'Red - Significant',
            css: 'impact-level-red'
        }
      };
    },

    getRisks(projectBlock){
      projectBlock = projectBlock || {};
      let risks =  _.filter(projectBlock.projectRiskAndIssues, {type: 'Risk'}) || [];
      this.addSortingOrderAndComputedRating(risks);
      return orderByFilter(risks, 'sortOrder');
    },

    getIssues(projectBlock){
      projectBlock = projectBlock || {};
      let issues =  _.filter(projectBlock.projectRiskAndIssues, {type: 'Issue'}) || [];
      return _.sortBy(issues, (issue) => {
        let sortString = '';

        if (issue.status === 'Open') {
          sortString = '0:';
        } else if (issue.status === 'Closed') {
          sortString = '1:';
        }

        sortString += (10-issue.initialImpactRating);

        sortString += issue.title;

        return sortString;
      });
    },

    addSortingOrderAndComputedRating(risks){
      // set up sort string and calculated values
      for (let i = 0; i < risks.length; i++) {
        let risk = risks[i];
        let sortString = '';
        if (risk.status === 'Open') {
          sortString = '0:';
        } else if (risk.status === 'Closed') {
          sortString = '1:';
        }

        if (risk.residualProbabilityRating && risk.residualImpactRating) {
          let result = risk.residualProbabilityRating * risk.residualImpactRating;
          risk.computedResidualRating = result;
          sortString += (99-result) + ':'
        } else {
          sortString += '99:';
        }

        if (risk.initialProbabilityRating && risk.initialImpactRating) {
          let result = risk.initialProbabilityRating * risk.initialImpactRating;
          risk.computedInitialRating = result;
          sortString += (99-result) + ':'
        }

        sortString += risk.riskCategory.displayOrder < 10 ? '0' + risk.riskCategory.displayOrder :  risk.riskCategory.displayOrder;
        sortString += ':' + risk.title;
        risk.sortOrder = sortString;
      }
    },

    getInternalRiskFromProject(project){
      return _.find(project.internalBlocksSorted, {type: INTERNAL_RISK_BLOCK_TYPE});
    },

    getInternalRiskTemplateConfig(template){
      return _.find(template.internalBlocks, {type: INTERNAL_RISK_BLOCK_TYPE});
    },

    getInternalRiskNoRatingLabel(){
      return 'No risk assessment carried out';
    },

    updateInternalRisk(){
      return $http.put(`${config.basePath}/projects/${projectId}/milestones/${blockId}/milestone/${milestoneId}/file/${fileId}`);
    }
  };
}

angular.module('GLA')
  .service('RisksService', RisksService);
