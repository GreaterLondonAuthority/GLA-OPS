/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ReceiptsService.$inject = ['$http', 'config'];

function ReceiptsService($http, config) {

  return {

    /**
     * Update receipts
     * @param {Number} projectId - project id
     * @param {Object} data
     * @return {Object} promise
     */
    updateReceipts(projectId, data) {
      return $http.put(`${config.basePath}/projects/${projectId}/receipts`, data);
    },

    getReceipts(projectId, blockId, year){
      return $http.get(`${config.basePath}/projects/${projectId}/receipts/${blockId}?year=${year}`);
    },

    /**
     * Create or update a receipt entry
     * @param  {[type]} projectId [description]
     * @param  {[type]} data      [description]
     * @return {[type]}           [description]
     */
    postReceipt(projectId, fYear, data){
      return $http.post(`${config.basePath}/projects/${projectId}/receipts?year=${fYear}`, data);
    },

    editReceipt(projectId, forecastId, value){
      return $http.put(`${config.basePath}/projects/${projectId}/receipts/${forecastId}`, value);
    },

    /**
     * Delete the receipt
     * @param projectId Project id
     * @param id Receipt id
     * @returns {*}
     */
    delete: (projectId, blockId, ledgerEntryId) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/blocks/${blockId}/ledgerEntry/${ledgerEntryId}`,
        method: 'DELETE'
      })
    },

    getReceiptsMetadata(projectId, blockId, categoryCode, yearMonth){
      return $http.get(`${config.basePath}/projects/${projectId}/receiptsMetaData/${blockId}/categoryCode/${categoryCode}/yearMonth/${yearMonth}`);
    }
  };
}

angular.module('GLA')
  .service('ReceiptsService', ReceiptsService);
