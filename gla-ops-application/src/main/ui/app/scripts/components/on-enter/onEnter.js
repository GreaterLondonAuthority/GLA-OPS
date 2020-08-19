/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');


function OnEnter() {
  return {
    link(scope, element, attrs) {
      element.bind('keydown keypress', function (event) {
        console.log('event', event.target == element[0]);
        if (event.which === 13 && event.target == element[0]) {
          scope.$apply(function () {
            scope.$eval(attrs.onEnter);
          });
          event.preventDefault();
        }
      });
    }
  };
}

OnEnter.$inject = [];

angular.module('GLA')
  .directive('onEnter', OnEnter);
