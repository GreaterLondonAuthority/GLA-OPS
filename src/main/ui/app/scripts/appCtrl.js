// jscs:disable requireCamelCaseOrUpperCaseIdentifiers
// jscs:disable requireParenthesesAroundIIFE

/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const AppCtrl = ($rootScope, $window, ConfigurationService, UserService, $log, PermPermissionStore) => {
  $rootScope.envVars = {};
  $rootScope.gaCache = [];

  //TODO: maybe move to its own service
  const initGoogleAnalytics = (data) => {
    var IGNORE_DEBUG = true;

    var ga_script = 'https://www.google-analytics.com/analytics.js';
    var env = data['env-name'];

    if (!IGNORE_DEBUG && (env === 'local' || env === 'Dev')) {
      ga_script = 'https://www.google-analytics.com/analytics_debug.js';
    }! function(A, n, g, u, l, a, r) {
      A.GoogleAnalyticsObject = l, A[l] = A[l] || function() {
          (A[l].q = A[l].q || []).push(arguments)
        }, A[l].l = +new Date, a = n.createElement(g),
        r = n.getElementsByTagName(g)[0], a.src = u, r.parentNode.insertBefore(a, r)
    }(window, document, 'script', ga_script, 'ga');
    $window.ga('create', data['ga-account']);

    // loop through missed trackings, this temporarily fixes
    // the loading of our first pageview tracking.
    if ($rootScope.gaCache && $rootScope.gaCache.length > 0) {
      $rootScope.gaCache.forEach(function(item) {
        $window.ga(item.action, item.id, item.data);
      });
      $rootScope.gaCache = [];
    }
  }

  // load env vars
  ConfigurationService.getConfig()
    .then(function(resp) {
      var data = resp.data;
      $log.debug('Environment variables:', JSON.stringify(data, null, 4));

      $rootScope.envVars = data;
      initGoogleAnalytics(data);
    });

  //TODO move to UserService
  const initPermissions = () => {
    PermPermissionStore.clearStore();

    var permissions = UserService.currentUser().permissions;
    PermPermissionStore.defineManyPermissions(permissions, function(permission) {
      var resolved = _.includes(permissions, permission);
      $log.debug('permission ' + (resolved ? 'found' : 'unknown') + ':', permission);
      return resolved;
    });
    $log.debug('Permissions set:', PermPermissionStore.getStore());
  }
  //TODO move to UserService
  // user permissions
  if (UserService.currentUser().loggedOn) {
    initPermissions();
  }
  //TODO move to UserService
  $rootScope.$on('user.login', function() {
    initPermissions();
  });

  //TODO move to UserService
  $rootScope.$on('user.logout', function() {
    PermPermissionStore.clearStore();
    $log.debug('Permissions cleared:', PermPermissionStore.getStore());
  });
}

AppCtrl.$inject = ['$rootScope', '$window', 'ConfigurationService', 'UserService', '$log', 'PermPermissionStore'];

angular.module('GLA')
  .controller('app.appCtrl', AppCtrl);
