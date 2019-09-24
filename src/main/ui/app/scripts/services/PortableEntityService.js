/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

PortableEntityService.$inject = ['$http', 'config'];

function PortableEntityService($http, config) {
  return {
    getSanitisedEntity(className, id){
      return $http.get(`${config.basePath}/portable/entity/${className}/${id}`)
    },

    saveSanitisedEntity(className, json){
      return $http.post(`${config.basePath}/portable/entity/${className}`,json)
    },
  };
}

angular.module('GLA')
  .service('PortableEntityService', PortableEntityService);
