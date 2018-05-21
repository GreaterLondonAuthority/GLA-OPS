/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

YesNoInputCtrl.$inject = ['$scope', '$element', '$attrs'];

function YesNoInputCtrl($scope, $element, $attrs) {
  var ctrl = this;
}

angular.module('GLA')
  .component('yesNoInput', {
    bindings: {
      name: '@',
      ngModel: '=',
      isDisabled: '='
    },
    templateUrl: 'scripts/components/common/input/yes-no-input/yesNoInput.html',
    controller: YesNoInputCtrl
  });
