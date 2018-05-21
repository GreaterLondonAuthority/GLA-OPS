/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function Dropdown($timeout) {
  return {
    restrict: 'E',
    require: '^ngModel',
    templateUrl: 'scripts/directives/dropdown/dropdown.tpl.html',
    scope: {
      ngModel: '=', // selection
      items: '=',   // items to select from
      callback: '&' // callback
    },
    link: function (scope, element, attrs) {
      element.on('click', function (event) {
        event.preventDefault();
      });

      scope.default = 'Please select item';
      scope.isButton = 'isButton' in attrs;

      // selection changed handler
      scope.select = function (item) {
        scope.ngModel = item;
        if (scope.callback) {
          $timeout(() => {
            scope.callback({item: item});
          });
        }
      };
    },
  };
}


Dropdown.$inject = ['$timeout'];


angular.module('GLA').directive('dropdown', Dropdown);
