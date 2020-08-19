/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

HomeCtrl.$inject = ['$scope', '$state', '$log', 'ConfigurationService',
  'UserService', 'PostLoginService', 'ConfirmationDialog', '$rootScope',
  '$location'];

function HomeCtrl($scope, $state, $log, ConfigurationService, UserService,
  PostLoginService, ConfirmationDialog, $rootScope, $location) {
  if (UserService.currentUser().loggedOn) {
    UserService.logout();
  }
  var ctrl = this;

  ConfigurationService.comingSoonMessage().then((resp) => {
    this.comingSoonMessage = resp.data;
  });

  $rootScope.showGlobalLoadingMask = false;

  ctrl.resetAutoFill = function () {
    this.isAutoFilled = false;
  };

  ctrl.onAutoFill = function () {
    this.isAutoFilled = true;
  };

  this.showReasonSuccess = $state.params.reasonSuccess;
  this.showReasonError = $state.params.reasonError;

  ctrl.error = false;

  ctrl.submit = function () {
    $rootScope.showGlobalLoadingMask = true;
    UserService.login(ctrl.uname, ctrl.pass).then(function (user) {
        ctrl.error = false;

        if ($rootScope.redirectURL) {
          console.log('redirectURL', $rootScope.redirectURL)
          $location.url($rootScope.redirectURL);
        } else if (user.data.primaryRole === 'Admin') {
          $state.go('projects');
        } else {
          $state.go('user');
        }
        $rootScope.redirectURL = null;

        let currentUser = UserService.currentUser();
        if(currentUser.approved) {
          // Any post login api calls, should be performed here
          PostLoginService.checkOrganisationsLegalStatus().then((resp) => {
            if (resp.data) {
              let modal = ConfirmationDialog.warn(resp.data);
              modal.result.then(() => {});
            }
          }).catch((err) => {
            console.log('err', err)
          });
        }

      }, function (err) {
        ctrl.error = true;
        ctrl.errorMessage = err.data || 'Sorry, your email and password combination is not recognised';
        $rootScope.showGlobalLoadingMask = false;
        $log.error(err);
      }
    );
  };

  this.clearErrors = function () {
    ctrl.error = false;
  };
}

angular.module('GLA').controller('HomeCtrl', HomeCtrl);
