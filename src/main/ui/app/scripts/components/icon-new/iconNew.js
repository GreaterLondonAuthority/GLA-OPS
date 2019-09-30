/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class IconNewCtrl {
  constructor() {
  }
}

IconNewCtrl.$inject = ['$scope'];

angular.module('GLA')
  .component('iconNew', {
    bindings: {
      text: '@',
    },
    templateUrl: 'scripts/components/icon-new/iconNew.html',
    controller: IconNewCtrl
  });
