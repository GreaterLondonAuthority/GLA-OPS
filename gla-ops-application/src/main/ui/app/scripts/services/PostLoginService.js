/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

PostLoginService.$inject = ['$http', 'config'];

function PostLoginService($http, config) {
  return {
    checkOrganisationsLegalStatus(){
      return $http({
        url: `${config.basePath}/postLogin/legalStatus`,
        method: 'GET',
        transformResponse: (data, headers, status) => {
          return data;
        }
      });
    }
  }
}

angular.module('GLA').service('PostLoginService', PostLoginService);
