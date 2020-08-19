/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

angular.module('GLA').directive('glaHeader', function () {
  return {
    restrict: 'E',
    templateUrl: 'scripts/directives/header/gla-header.tpl.html',
    controller: ['$scope', 'UserService', 'MetadataService', function ($scope, UserService, MetadataService) {
      $scope.userData = UserService.currentUser();

      $scope.logout = function() {
        UserService.logout();
      };

      // Listen for login event
      $scope.$on('user.login', function () {
        $scope.userData = UserService.currentUser();
      });

      // Listen for logout event
      $scope.$on('user.logout', function () {
        $scope.userData = UserService.currentUser();
      });

      MetadataService.subscribe((data)=>{

        $scope.$evalAsync(()=>{
          $scope.numberOfUnreadNotifications = data.numberOfUnreadNotifications;
        });
      });
    }]
  };
});
