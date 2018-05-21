/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


angular.module('GLA', [
  'ui.bootstrap',
  'ngAnimate',
  'ngCookies',
  'ngMessages',
  'ngResource',
  'ngRoute',
  'ngSanitize',
  'ngTouch',
  'ui.router',
  'ui.bootstrap',
  'validation.match',
  'ngStorage',
  // Note: this is now loaded in index.html as we hav applid a patch to UI-SELECT
  'ui.select',
  'angularMoment',
  'toastr',
  'permission',
  'angular.vertilize',
  'ng.deviceDetector'
]);
