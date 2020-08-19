/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

BudgetService.$inject = ['$resource', '$http', 'config', 'UserService'];

function BudgetService($resource, $http, config, UserService) {

  return {

    /**
     * Retrieve the project spend data
     * @param {Number} projectId
     * @return {Object} promise
     */

    retrieveProjectBudgets: (projectId, blockId) => {
      return $http({
        url: config.basePath + '/projects/' + projectId + '/projectBudgets/' + blockId,
        method: 'GET'
      });
    },

    /**
     * Save project project budgets
     * @param {Number} projectId
     * @param {Object} data - {
     *   "fromDate": "123",
     *   "toDate": "xyz",
     *   "revenue": null,
     *   "wbsRevenueCode": null,
     *   "capital": null,
     *   "wbsCapitalCode": null
     * }
     * @return {Object} promise
     */
    saveProjectBudgets: (projectId, data, releaseLock) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/projectBudgets?releaseLock=${!!releaseLock}`,
        method: 'PUT',
        data: data
      });
    },


    getProjectBudget: (projectId, blockId, year) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/${blockId}/annualSpendFor/${year}`,
        method: 'GET'
      });
    },


    /**
     * Update Annual Spend
     * @param {Number} projectId
     * @param {Number} blockId
     * @param {Number} year
     * @param {Object} data - blockData
     * @param {Boolean} releaseLock - release edit lock
     *
     * @returns {Object} promise
     */
    updateAnnualSpend: (projectId, blockId, year, data, releaseLock) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/${blockId}/annualSpend/${year}?autosave=${!releaseLock}`,
        method: 'PUT',
        data: data
      })
    },


    /**
     * Create/update the annual spend forecast
     * @param {Number} projectId
     * @param {Number} blockId
     * @param {Number} year
     * @param {Object} data - blockData
     * @param {Boolean} releaseLock - release edit lock
     *
     * @returns {Object} promise
     */
    updateAnnualSpendForecast: (projectId, year, data, releaseLock) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/payments?releaseLock=${releaseLock}&year=${year}`,
        method: 'POST',
        data: data
      })
    },

    /**
     * Delete the annual spend forecast
     * @param {Number} projectId
     * @param {Number} blockId
     * @param {Number} year
     * @param {Object} data - blockData
     * @param {Boolean} releaseLock - release edit lock
     *
     * @returns {Object} promise
     */
    deleteAnnualSpendForecast: (projectId, blockId, financialYear, month, entityType, categoryId, calendarYear, releaseLock) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/${blockId}/annualSpendForecast/${financialYear}`,
        method: 'DELETE',
        params: {
          month: month,
          entityType: entityType,
          categoryId: categoryId,
          autosave: !releaseLock,
          actualYear: calendarYear
        }
      });
    },

    getBudgetsMetadata(projectId, blockId, categoryId, yearMonth) {
      return $http.get(`${config.basePath}/projects/${projectId}/projectBudgetsMetaData/${blockId}/categoryCode/${categoryId}/yearMonth/${yearMonth}`);
    }
  };
}

angular.module('GLA')
  .service('BudgetService', BudgetService);
