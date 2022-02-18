/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class PermissionsPage {

  $onInit() {
    this.permissions = this.sortPermissions(this.permissions)
  }

  sortPermissions(permissions) {
    return Object.keys(permissions).sort().reduce(function (result, key) {
      result[key] = permissions[key];
      return result;
    }, {});
  }

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
