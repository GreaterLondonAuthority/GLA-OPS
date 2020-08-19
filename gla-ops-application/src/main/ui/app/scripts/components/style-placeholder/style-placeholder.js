/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');
const placeholderCls = 'placeholder';

function StylePlaceholder($timeout, boolFilter) {
  return {
    restrict: 'A',
    require: 'ngModel',
    link(scope, element, attrs, ngModelCtrl) {
      let tagName = element.prop('tagName').toLowerCase();
      let isSelect = tagName === 'select';

      if(!isSelect){
        console.error('Unsupported tag');
      }

      scope.$watch(() => {
        return ngModelCtrl.$modelValue;
      }, (modelValue) => {
        if(modelValue == null || modelValue == ''){
          element.addClass(placeholderCls)
        }else {
          element.removeClass(placeholderCls)
        }
      });
    }
  };
}

StylePlaceholder.$inject = ['$timeout', 'boolFilter'];

angular.module('GLA').directive('stylePlaceholder', StylePlaceholder);
