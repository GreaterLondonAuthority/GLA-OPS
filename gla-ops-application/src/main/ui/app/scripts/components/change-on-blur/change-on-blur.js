/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

/**
 * Triggers on blur only if the value has changed
 */

function ChangeOnBlur($log) {
  return {
    restrict: 'A',
    require: 'ngModel',
    link(scope, elm, attrs, ngModelCtrl) {
      if (attrs.type === 'radio' || attrs.type === 'checkbox') {
        return;
      }

      let expressionToCall = attrs.changeOnBlur;
      let oldValue = null;

      elm.on('focus', () => {
        scope.$apply(() => {
          oldValue = elm.val();
        });
      });
      elm.on('blur', ()=> {
        scope.$apply(()=> {
          var newValue = elm.val();
          if (newValue !== oldValue) {
            scope.$eval(expressionToCall);
          }
        });
      });
    }
  };
}

ChangeOnBlur.$inject = ['$log'];

angular.module('GLA')
  .directive('changeOnBlur', ChangeOnBlur);
