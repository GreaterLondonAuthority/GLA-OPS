/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

angular.module('GLA').component('glaCookieWarning', {
  templateUrl: 'scripts/directives/cookie-warning/gla-cookie-warning.tpl.html',
  controller: ['$localStorage', '$scope', '$window', '$element', function ($localStorage, $scope, $window, $element) {
    this.$localStorage = $localStorage;
    this.close = function () {
      $localStorage.isCookieAccepted = true;
    };
  }]
});


