/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

angular.module('GLA')
  .directive('focusMe', () => {
    return {
      restrict: 'A',
      scope: {
        trigger: '=focusMe',
        focusMeIf: '&',
        focusReset: '@focusReset'
      },
      link(scope, element) {
        scope.$watch('trigger', value => {
          if(scope.focusMeIf) {
            const condition = typeof(scope.focusMeIf) === 'function' ? scope.focusMeIf() : scope.focusMeIf;
            if(condition === true) {
              element[0].focus();
            }
          }
          if (value === true) {
            element[0].focus();

            if (scope.focusReset !== 'false') {
              scope.trigger = false;
            }
          }
        });
      }
    }
  });
