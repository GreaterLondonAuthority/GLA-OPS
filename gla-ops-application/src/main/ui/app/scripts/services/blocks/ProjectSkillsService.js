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

    getLearningGrantBlock(projectId, blockId){
      return $http.get(`${config.basePath}/projects/${projectId}/learningGrant/${blockId}`);
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

    periodName(month) {
      return month <= 12 ?  moment().month(month - 1).format('MMMM') : 'Return';
    },

    getLabels(templateConfig){
      let labels = {
        totalAllocation: 'Total Project Allocation (£)',
        contractType: 'Contract Type',
        academicYear: 'Academic Year',
        deliverAllocation: 'Delivery Allocation (£)',
        communityAllocation: 'Of which, Community Learning (£)',
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
    },

    /**
     *   Array of all selected and deselected contract types
     */
    getAllContractTypes(block, templateConfig){
      let allContractTypes = [];

      templateConfig.contractTypes.forEach(contractType => {
        let existingContractType = _.find(block.contractTypes, t => t.name === contractType);
        if(existingContractType){
          existingContractType.original = existingContractType.selected;
        }
        allContractTypes.push(existingContractType || {name: contractType} );
      });

      return allContractTypes;
    },

    /**
     * Return contract types selected inside the block in the order defined in template config
     * @param block
     * @param templateConfig
     * @returns {Array|*}
     */
    getActiveContractTypes(block, templateConfig){
      if(!block){
        return [];
      }
      let allContractTypes = this.getAllContractTypes(block, templateConfig);
      let activeContractTypes = [];
      allContractTypes.forEach( ct => {
        if(ct.selected) {
          activeContractTypes.push(ct)
        }
      });
      return activeContractTypes;
    },


    getPeriodTotals(totals, contractTypes){
      let allPeriodTotals = [];
      let years = Object.keys(totals || {});
      years.forEach(year => {
        let periods = Object.keys(totals[year] || {});
        periods.forEach(period => {
          let periodTotals = {
            year: year,
            period: period,
            totals: totals[year][period],
            comparisonId: `totals:${year}:${period}`
          };
          periodTotals.totals.contractTypeTotals = contractTypes.map(ct => _.find(totals[year][period].contractTypeTotals || [], {contractType: ct.name})).filter(ctt=> !!ctt);
          allPeriodTotals.push(periodTotals);
        })
      });
      return allPeriodTotals;
    },

    getPeriodIdToTextMap(periods){
      let periodsMap = {};
      (periods || {}).forEach(p => periodsMap[p.period] = p.text);
      return periodsMap;
    }
  };
}

angular.module('GLA')
  .service('ProjectSkillsService', ProjectSkillsService);
