/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class HeaderStatusCtrl {

}

HeaderStatusCtrl.$inject = ['$scope'];


angular.module('GLA')
  .component('headerStatus', {
    templateUrl: 'scripts/components/header-status/headerStatus.html',
    transclude: {
      'hsLeft': '?hsLeft',
      'hsRight': '?hsRight',
      'hsCenter': '?hsCenter'
    },
    controller: HeaderStatusCtrl
  });
