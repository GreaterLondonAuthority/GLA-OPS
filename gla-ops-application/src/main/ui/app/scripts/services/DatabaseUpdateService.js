/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

DatabaseUpdateService.$inject = ['$http', 'config'];

function DatabaseUpdateService($http, config) {
  return {

    getSqlQueries() {
      return $http.get(`${config.basePath}/support/sql`).then(rsp => {
        return rsp.data;
      });
    },

    query(update) {
      return $http.post(
        `${config.basePath}/support/sql/query`,
        update
      );
    },

    createDatabaseUpdate(update) {
      return $http.post(
        `${config.basePath}/support/sql`,
        update
      );
    },

    approveUpdate(sqlUpdate, id) {
      return $http.put(`${config.basePath}/support/sql/${id}`, sqlUpdate)
    },

    approvePpd(ppdTested, id) {
      return $http.put(`${config.basePath}/support/sql/update/ppd/${id}?ppdTested=${ppdTested}`)
    },

    getUpdateDetails(id) {
      return $http.get(`${config.basePath}/support/sql/${id}`)
    }
  };
}

angular.module('GLA')
  .service('DatabaseUpdateService', DatabaseUpdateService);
