/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class YesNoInputCtrl {
  constructor() {
  }

  $onInit(){
    this.yesValue = this.mode == 'bool' ? true : 'yes';
    this.noValue = this.mode == 'bool' ? false : 'no';
  }
}

YesNoInputCtrl.$inject = [];


angular.module('GLA')
  .component('yesNoInput', {
    bindings: {
      name: '@',
      mode: '@',
      ngModel: '=',
      isDisabled: '=',
      onChange: '&'
    },
    templateUrl: 'scripts/components/common/input/yes-no-input/yesNoInput.html',
    controller: YesNoInputCtrl
  });
