/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

DashboardService.$inject = ['$http', 'config'];

function DashboardService($http, config) {
  return {
    getMetrics(){
      return $http.get(`${config.basePath}/dashboard/metrics/`, {})
    }
  };
}

angular.module('GLA')
  .service('DashboardService', DashboardService);
