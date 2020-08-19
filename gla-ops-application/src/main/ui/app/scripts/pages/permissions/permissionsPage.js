/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class PermissionsPage {
}

PermissionsPage.$inject = [];

angular.module('GLA')
  .component('permissionsPage', {
    templateUrl: 'scripts/pages/permissions/permissionsPage.html',
    bindings: {
      permissions: '<'
    },
    controller: PermissionsPage
  });
