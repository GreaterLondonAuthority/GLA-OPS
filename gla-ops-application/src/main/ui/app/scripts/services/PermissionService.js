/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

PermissionService.$inject = ['$http', 'config'];

function PermissionService($http, config) {
  return {

    getPermissions(){
      return $http.get(`${config.basePath}/permissions`);
    },

  }
}

angular.module('GLA')
  .service('PermissionService', PermissionService);
