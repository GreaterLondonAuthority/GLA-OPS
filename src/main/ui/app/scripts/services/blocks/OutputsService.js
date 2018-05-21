/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

OutputsService.$inject = ['$http', 'config'];

import DateUtil from '../../util/DateUtil'

function OutputsService($http, config) {
  let apiConfig = config;
  return {

    getOutputConfigGroup: (id) => {
      return $http({
        url: `${config.basePath}/outputGroup/${id}`,
        method: 'GET'
      });
    },

    /**
     * Retrieve the project outputs for the financial year
     * @returns {Object} promise
     */
    getProjectOutputs: (projectId, blockId, financialYear) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/block/${blockId}/outputs/${financialYear}`,
        method: 'GET'
      });
    },

    /**
     * Delete the output
     * @param projectId Project id
     * @param id Output id
     * @returns {*}
     */
    delete: (projectId, id) => {
      return $http.delete(`${config.basePath}/projects/${projectId}/outputs/${id}`);
    },

    postProjectOutputs: ({
                           projectId,
                           actual,
                           forecast,
                           config,
                           month,
                           outputType,
                           year
                         }) => {
      return $http({
        url: `${apiConfig.basePath}/projects/${projectId}/outputs`,
        method: 'POST',
        data: {
          // actual/forecast is dtermined by dropdown and the input value is placed in the correct attribute
          actual,
          forecast,
          config,
          month,
          outputType,
          year,
        }
      });
    },

    updateProjectOutputs: (projectId, data) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/outputs`,
        method: 'PUT',
        data: data
      });
    },


    outputSummaries(outputsBlock, collapsedRows){
      collapsedRows = collapsedRows || {};
      let summaries = (outputsBlock || {}).outputSummaries || [];
      summaries.forEach(o => {
        o.collapsed = collapsedRows[o.comparisonId] == null ? true : !!collapsedRows[o.comparisonId];
      });
      return summaries;
    },


    getAllFinancialYears(outputsBlock){
      let summaries = (outputsBlock || {}).outputSummaries || [];
      return summaries.reduce((res, o) => res.concat(o.subcategories), []).map(item => item.financialYear);
    },


    getOutputBlockSummariesTitle(outputsBlock){
      let allYears = this.getAllFinancialYears(outputsBlock);
      return this.getOutputsSummariesTitle(_.min(allYears), _.max(allYears))
    },


    getOutputsSummariesTitle(startYear, endYear){
      let title = 'Total project outputs';

      if (startYear) {
        title += ' ' + DateUtil.toFinancialYearString(startYear);
      }

      if (endYear && startYear != endYear) {
        title += ' to ' + DateUtil.toFinancialYearString(endYear);
      }
      return title;
    }
  }
}

angular.module('GLA')
  .service('OutputsService', OutputsService);
