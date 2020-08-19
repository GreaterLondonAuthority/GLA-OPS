/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

AuditService.$inject = ['$http', 'config'];

function AuditService($http, config) {
  return {

    getPagedAuditEvents(page, sort, size, username, fromDate, toDate) {
      const params = {
        page: page,
        size: size,
        sort: sort,
        username: username,
        fromDate: fromDate ? (moment(fromDate).format('YYYY-MM-DD')) : null,
        toDate: toDate ? (moment(toDate).format('YYYY-MM-DD')) : null,
      };

      return $http({
        url: config.basePath + '/allAudit',
        method: 'GET',
        params: params
      })
    },

    searchOptions() {
      return [{
        name: 'username',
        description: 'Username or email',
        hint: 'Enter username or email',
        maxLength: '50'
      }];
    }

  };
}

angular.module('GLA')
  .service('AuditService', AuditService);
