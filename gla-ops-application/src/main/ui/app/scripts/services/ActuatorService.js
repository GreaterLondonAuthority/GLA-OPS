/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ActuatorService.$inject = ['$http'];

function ActuatorService($http) {
  return {
    getInfo() {
      return $http.get(`/sysops/info`);
    },
    getMetrics() {
      return $http.get(`/sysops/metrics`);
    }
  };
}

angular.module('GLA')
  .service('ActuatorService', ActuatorService);
