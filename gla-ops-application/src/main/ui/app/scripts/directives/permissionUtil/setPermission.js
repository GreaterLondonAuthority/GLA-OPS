/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
 * Permission directive helper for GLA specific use cases
 *
 * @uses `angular-permission`
 * @see https://github.com/Narzerus/angular-permission
 */
function SetPermission($compile) {
  return {
    require: 'permission',
    restrict: 'A',
    scope: {
      setPermission: '&',
      permissionOnly: '&',
      permissionExcept: '&'
    },
    controller: ['UserService', '$scope', '$attrs', function (UserService, $scope, $attrs) {
      //has OPS admin super powers
      $scope.isSuperUser = (UserService.currentUser().isAdmin === true);
    }],
    link: function (scope, element, attrs, permission) {
      var set = scope.setPermission();
      var currentPerm = scope.permissionOnly();

      var org = (scope.isSuperUser && set.org) ? '*' : set.org;
      var only = set.only;
      var except = set.except;

      //extract .[org] ending
      var extractEnding = function (item) {
        item = item.replace(/([a-z.]+\b)/ig, '$1');
        item = (item[item.length - 1] === '.') ? item.slice(0, item.length - 1) : item;
        return item;
      }

      // parse condition
      var parseCondition = function (condition) {
        if (angular.isArray(condition)) {
          condition = condition.map(function (item) {
            item = extractEnding(item);
            item = item + (org ? '.' + org : '');
            return item;
          });
        } else if (angular.isString(condition)) {
          condition = extractEnding(condition);
          condition = condition + (org ? '.' + org : '')
        } else {
          throw ('Permission should be of type \'array\' or \'string\':', condition);
        }
        return condition;
      }

      // parse
      var condition = parseCondition(only ? only : except);

      var parsedPerm = angular.isArray(condition) ?
        JSON.stringify(condition).replace(/"/g, '\'') :
        ('\'' + condition + '\'');

      if (!currentPerm) {
        attrs.$set(only ? 'permissionOnly' : 'permissionExcept', parsedPerm);
        $compile(element)(scope);
      }
    }
  }
}

SetPermission.$inject = ['$compile'];

angular.module('GLA')
  .directive('setPermission', SetPermission);
