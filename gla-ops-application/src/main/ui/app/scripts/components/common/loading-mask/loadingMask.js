/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

var gla = angular.module('GLA');

gla.component('loadingMask', {
  templateUrl: 'scripts/components/common/loading-mask/loadingMask.html',
  bindings:{
    text: '@text'
  }
});
