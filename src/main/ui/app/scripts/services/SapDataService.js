/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

SapDataService.$inject = ['$http', 'config'];

function SapDataService($http, config) {
  return {
    getSapData(processed){
      return $http.get(`${config.basePath}/finance/sapData/`, {
        params: {
          processed: processed
        }
      })
    },

    ignore(id){
      return $http.put(`${config.basePath}/finance/sapData/${id}/ignored`, true);
    }
  };
}

angular.module('GLA')
  .service('SapDataService', SapDataService);
