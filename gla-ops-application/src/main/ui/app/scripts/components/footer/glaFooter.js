/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

GLAFooterCtrl.$inject = ['$rootScope', '$scope', '$element', '$window', '$timeout'];

function GLAFooterCtrl($rootScope, $scope, $element, $window, $timeout) {
  var ctrl = this;

  this.unWatch = $rootScope.$watch('envVars', function(data) {
    if(data && data['system-environment']) {
      this.environment = data['system-environment'];
      this.aboutUrl = data['about-url'];
      this.unWatch();
    }
  }.bind(this));
}

angular.module('GLA')
  .component('glaFooter', {
    templateUrl: 'scripts/components/footer/glaFooter.html',
    controller: GLAFooterCtrl
  });
