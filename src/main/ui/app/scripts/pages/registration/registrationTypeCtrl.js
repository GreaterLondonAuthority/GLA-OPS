/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

RegistrationTypeCtrl.$inject = ['$scope', '$log', 'OrganisationService', 'UserService'];

function RegistrationTypeCtrl($scope, $log, OrganisationService, UserService) {
  var ctrl = this;

  ctrl.done = false;

  $scope.regdata = {};

  ctrl.checkOrgCode = function () {
    let orgCode = $scope.regdata.orgCode;
    if ($scope.regdata.orgCode) {
      OrganisationService.lookupOrgNameByCode($scope.regdata.orgCode)
        .then(function (response) {
          if (orgCode === $scope.regdata.orgCode) {
            if (response == undefined || response.status != 200) {
              $log.log('org with id or ims not found: ' + $scope.regdata.orgCode);
              ctrl.orgCodeValidationError = true;
              ctrl.orgName = '';
            } else {
              ctrl.orgCodeValidationError = false;
              ctrl.orgName = response.data;
              $log.log('org with id or ims number', $scope.regdata.orgCode + ' found.');
            }
          }
        })
        .catch(function (error) {
          if (orgCode === $scope.regdata.orgCode) {
            $log.log('org with id or ims not found:', error);
            ctrl.orgCodeValidationError = true;
          }
        });
    } else {
      ctrl.orgCodeValidationError = false;
      ctrl.orgName = null;
    }
  };

  ctrl.passwordChanged = function() {
    ctrl.errors = null;
  };

  ctrl.submit = function() {
    UserService.registerUser($scope.regdata).then(
        function() {
          ctrl.done = true;
        },
        function(error) {
          ctrl.errors = {};
          error.data.errors.map(function(i) {
            ctrl.errors[i.name] = i.description
          });
          //register.regForm.$setValidity('emailexists', false, register.regForm);
          $log.log(ctrl.errors);
        }
      );

  }
}

angular.module('GLA')
  .controller('RegistrationTypeCtrl', RegistrationTypeCtrl);
