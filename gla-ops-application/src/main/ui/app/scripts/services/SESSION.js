/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

angular.module('GLA')
  .config(['$httpProvider', $httpProvider => {
    $httpProvider.interceptors.push(
      ['$rootScope', '$q', '$injector', '$sessionStorage', '$log', '$location', 'ModalDisplayService', 'AuthService', ($rootScope, $q, $injector, $sessionStorage, $log, $location, ModalDisplayService, AuthService) => {
        $rootScope.disabledHttpInterceptors = [];

        let responseError = resp => {
          const UserService = $injector.get('UserService');
          const status = resp.status;
          console.log('responseError', resp);
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
          } else if (status === 403 && !(resp.config && resp.config.ignoreErrors && resp.config.ignoreErrors['403'])) {
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
              errorId: (resp.data || resp.error || {}).id
            });
            $log.error(`500 >= Status(${status}) < 600`);
            $log.error(JSON.stringify(resp));
          }
          return $q.reject(resp);
        }

        //Subscribe to ng9 request interceptor for 401, 403, 500 codes but handle it with the same code as for ng1
        //$http and HttpClient requests interceptors are not intercepting each others requests
        //So ng9 interceptor emits response errors through AuthService service which we handle here
        AuthService.addResponseErrorListener((rsp) => {
          console.log('ng1 interceptor', rsp)
          responseError(rsp);
        });

        const renderModal = msg => {
          ModalDisplayService.standardError(msg);
        }

        return {
          'responseError': responseError
        };
      }]);
  }]);
