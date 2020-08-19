/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');


function AllowEnter() {
  return {
    link(scope, element, attrs) {
      element.bind('keydown keypress', function (event) {
        if (event.which === 13 && event.target == element[0]) {
          scope.$apply(function () {
            element[0].click();
          });
          event.preventDefault();
        }
      });
    }
  };
}

AllowEnter.$inject = [];

angular.module('GLA')
  .directive('allowEnter', AllowEnter);
