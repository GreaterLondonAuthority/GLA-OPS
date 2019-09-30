/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ReferenceDataService.$inject = ['$http', 'config'];

function ReferenceDataService($http, config) {

  return {
    getBoroughs() {
      return $http.get(`${config.basePath}/boroughs`, {cache: true}).then(rsp => rsp.data);
    },

    getConfigItemsByExternalId(externalId){
      return $http.get(`${config.basePath}/configItems/${externalId}`).then(rsp => rsp.data);
    },

    getBlockTypes(){
      return $http.get(`${config.basePath}/templates/projectBlockTypes`).then(rsp => rsp.data);
    }
  };
}

angular.module('GLA').service('ReferenceDataService', ReferenceDataService);
