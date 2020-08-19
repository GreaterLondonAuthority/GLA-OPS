/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class TenureTilesCtrl {
  constructor(UserService){

  }
}

TenureTilesCtrl.$inject = ['UserService'];


angular.module('GLA')
  .component('tenureTiles', {
  templateUrl: 'scripts/components/tenure-tiles/tenureTiles.html',
  bindings: {
    tenures: '<?'
  },
  controller: TenureTilesCtrl

});
