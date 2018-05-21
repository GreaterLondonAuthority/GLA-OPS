/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function NumbersWithNegative() {
  return {
    require: 'ngModel',
    link: function(scope, element, attr, ngModelCtrl) {
      function fromUser(text) {
        // only numbers and minus
        text = text.replace(/[^-0-9]/g, '');

        // only one minus
        if(text.length > 1) {
          if(text.charAt(text.length - 1) === '-') {
            text = text.slice(0, text.length - 1);
          }
        }

        // only 9 numbers
        var numberMatch = text.match(/[0-9]/g) || [];
        if(numberMatch.length > 9) {
          text = text.slice(0, text.length - 1);
        }

        ngModelCtrl.$setViewValue(text);
        ngModelCtrl.$render();

        return text;
      }

      ngModelCtrl.$parsers.push(fromUser);
    }
  };
}

angular.module('GLA')
  .directive('numbersWithNegative', NumbersWithNegative);
