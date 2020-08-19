/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

/**
 * Detects chrome autofill by background colour in the autofilled field.
 */

function ChromeAutofill($window, $interval) {
  return {
    link(scope, element, attr) {
      if ($window.navigator.userAgent.indexOf('Chrome') !== -1) {
        let autoFillCheck;
        let totalTime = 2000;
        let stepTime = 100;
        autoFillCheck = $interval(()=> {
          totalTime = totalTime - stepTime;
          if (totalTime <= 0) {
            $interval.cancel(autoFillCheck);
          }
          let isAutoFill = $window.getComputedStyle(element[0]).backgroundColor === 'rgb(250, 255, 189)';
          if (isAutoFill) {
            scope.chromeAutofill();
            $interval.cancel(autoFillCheck);
          }
        }, stepTime);
      }
    },

    scope: {
      'chromeAutofill': '&'
    }
  };
}

ChromeAutofill.$inject = ['$window', '$interval'];

angular.module('GLA')
  .directive('chromeAutofill', ChromeAutofill);
