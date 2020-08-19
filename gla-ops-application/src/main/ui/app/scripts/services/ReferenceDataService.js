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

    getAvailablePaymentSources(){
      return $http.get(`${config.basePath}/paymentSources`, {cache: true}).then(rsp => rsp.data);
    },

    getBlockTypes(){
      return $http.get(`${config.basePath}/templates/projectBlockTypes`).then(rsp => rsp.data);
    },

    getIcons() {
      return $http.get(`${config.basePath}/files`, {
        cache: true,
        params: {category: 'Icon'}
      }).then(rsp => rsp.data);
    },

    getRequirementOptions() {
      return [{
        label: 'Hidden',
        id: 'hidden'
      }, {
        label: 'Mandatory',
        id: 'mandatory'
      }, {
        label: 'Optional',
        id: 'optional'
      }]
    }
  };
}

angular.module('GLA').service('ReferenceDataService2', ReferenceDataService);
