/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function NumbersOnly() {
  return {
    link: function (scope, element, attr) {
      element.on('keypress', function (e) {
        var key = e.keyCode || e.which;
        var char = String.fromCharCode(e.which);
        //Allow tab navigation and digits
        if (key !== 9 && !/^\d+$/.test(char)) {
          e.preventDefault();
        }
      });

      element.on('input', function (e) {
        if (!/^\d+$/.test(element.val())) {
          element.val('');
        }
      });
    }
  };
}

angular.module('GLA')
  .directive('numbersOnly', NumbersOnly);
