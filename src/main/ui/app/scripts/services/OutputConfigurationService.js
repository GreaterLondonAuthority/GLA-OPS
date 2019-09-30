/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

OutputConfigurationService.$inject = ['$http', 'config'];

function OutputConfigurationService($http, config) {
  return {
    getAllOutputConfiguration(){
      return $http.get(`${config.basePath}/outputCategory/`);
    },
    getAllOutputConfigurationGroup(){
      return $http.get(`${config.basePath}/outputConfigurationGroup/`);
    }
  }
}

angular.module('GLA')
  .service('OutputConfigurationService', OutputConfigurationService);
