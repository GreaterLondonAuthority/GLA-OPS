/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

FinanceService.$inject = ['$http', 'config'];

function FinanceService($http, config) {

  return {

    getFinanceCategories(reload){
      return $http.get(`${config.basePath}/finance/categories`, {cache: !reload});
    },

    getReceiptCategories(reload){
      return this.getFinanceCategories(reload).then(rsp => {
        return _.filter(rsp.data, {receiptStatus: 'ReadWrite'});
      })
    },

    getSpendCategories(reload){
      return this.getFinanceCategories(reload).then(rsp => {
        return _.filter(rsp.data, {spendStatus: 'ReadWrite'});
      })
    },

    updateCategory(data){

      let id = data.id;
      return $http.put(`${config.basePath}/finance/categories/${id}`, data);
    },
    createCategory(data){
      return $http.post(`${config.basePath}/finance/categories/`, data);
    }

  };

}

angular.module('GLA').service('FinanceService', FinanceService);
