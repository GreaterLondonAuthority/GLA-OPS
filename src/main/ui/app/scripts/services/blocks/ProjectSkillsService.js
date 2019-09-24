/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ProjectSkillsService.$inject = ['$http', 'config'];

function ProjectSkillsService($http, config) {

  return {

    getLearningGrantBlock(projectId, year){
      return $http.get(`${config.basePath}/projects/${projectId}/learningGrant`, {
        params: {
          year: year
        }
      });
    },

    updateLearningGrantBlock(projectId, blockId, year, block, releaseLock) {
      return $http.put(`${config.basePath}/projects/${projectId}/learningGrant/${blockId}/years/${year}?releaseLock=${!!releaseLock}`, block, releaseLock)
    },

    getFundingClaimsBlock(projectId){
      return $http.get(`${config.basePath}/projects/${projectId}/fundingClaims`);
    },

    updateFundingClaimsBlockEntry(projectId, blockId, data) {
      return $http.put(`${config.basePath}/projects/${projectId}/fundingClaims/${blockId}/entry`, data)
    },

    monthName(month) {
      return moment().month(month - 1).format('MMMM');
    },

    getLabels(templateConfig){
      let labels = {
        totalAllocation: 'Total Project Allocation (£)',
        academicYear: 'Academic Year',
        deliverAllocation: 'Delivery Allocation (£)',
        learnerSupportAllocation: 'Learner Support Allocation (£)',
        tableColumns: {
          monthTitle: 'MONTH',
          profileTitle: 'PROFILE %',
          allocationTitle: 'ALLOCATION £',
          cumulativeAllocationTitle: 'CUMULATIVE ALLOCATION £',
          ilrTotalTitle: 'ILR TOTAL £',
          cumulativePaymentTitle: 'CUMULATIVE PAYMENT £',
          paymentDueTitle: 'PAYMENT DUE £',
          statusTitle: 'STATUS'
        }
      };
      labels.tableColumns = _.merge(labels.tableColumns, templateConfig);
      return labels;
    },

    isAebProcured(block){
      return (block || {}).grantType === 'AEB_PROCURED';
    },

    isAebGrant(block){
      return (block || {}).grantType === 'AEB_GRANT';
    }

  };
}

angular.module('GLA')
  .service('ProjectSkillsService', ProjectSkillsService);
