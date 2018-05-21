/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

angular.module('GLA')
  .config($httpProvider => {
    $httpProvider.interceptors.push(
      ($rootScope, $q, $injector, $sessionStorage, $log, $location, ModalDisplayService) => {
        $rootScope.disabledHttpInterceptors = [];

        const renderModal = msg => {
          ModalDisplayService.standardError(msg);
          // const modal = $injector.get('$uibModal');
          // const _ = $injector.get('_');
          //
          // const modalInstance = modal.open({
          //   animation: true,
          //   templateUrl: 'scripts/components/misc/serverError.html',
          //   size: 'md',
          //   resolve: {
          //     message: () => {
          //       return msg;
          //     }
          //   },
          //   controller: ['$scope', 'message', ($scope, message) => {
          //     $scope.header = message.header ? message.header : 'Oops!';
          //     $scope.subHeader = message.subHeader ? message.subHeader : 'Something has gone wrong';
          //     var defaultMessage = 'It’s not your fault. Try again by clicking OK but if the issue persists you may need to speak to the GLA technical team.';
          //     if (message.errorId) defaultMessage = defaultMessage+' They may ask for the code below.';
          //     $scope.body = message.body ? message.body : defaultMessage;
          //     $scope.errorId = message.errorId ? message.errorId : '';
          //   }]
          // });
        }

        return {
          'responseError': resp => {
            const $state = $injector.get('$state');
            const $stateParams = $injector.get('$stateParams');
            const UserService = $injector.get('UserService');
            const status = resp.status;
            console.log('responseError');
            $rootScope.showGlobalLoadingMask = false;

            if (status !== 401) {
              $rootScope.redirectURL = null;
            }

            if (status === 401) {
              console.log('setting redirectURL', $location.url());
              if($location.url() !== '/home') {
                $rootScope.redirectURL = $location.url();
              }
              UserService.logout();
            } else if (status === 403 && !(resp.config.ignoreErrors && resp.config.ignoreErrors['403'])) {
              renderModal({
                header: 'Oops!',
                subHeader: 'Access denied',
                body: 'You are not authorised to view this page.'
              });
            } else if (status === 404) {
              // renderModal({
              //   header: 'Oops!',
              //   subHeader: '404 Not found',
              //   body: 'Sorry, the requested page could not be found.'
              // });

            } else if (status >= 500 && status < 600) {
              renderModal({
                errorId: (resp.data || {}).id
              });
              $log.error(`500 >= Status(${status}) < 600`);
              $log.error(JSON.stringify(resp));
            }
            return $q.reject(resp);
          }
        };
      });
  });
