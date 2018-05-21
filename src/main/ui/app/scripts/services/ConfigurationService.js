/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

ConfigurationService.$inject = ['config', '$http'];

function ConfigurationService(config, $http) {
  return {
    /**
     * Loads app configuration
     * @returns {*}
     */
    getConfig() {
      return $http({
        url: `${config.basePath}/config`,
        method: 'GET',
        cache: true
      });
    },

    /**
     * Returns 'Coming Soon' message configured by admin
     * @returns {*}
     */
    comingSoonMessage() {
      return $http({
        url: `${config.basePath}/messages/coming-soon`,
        method: 'GET'
      });
    },

    /**
     * Updates 'Coming Soon' message configured by admin
     * @param message <{code, text}>
     * @returns {*}
     */
    updateComingSoonMessage(message) {
      return $http({
        url: `${config.basePath}/messages/coming-soon`,
        method: 'PUT',
        data: message
      });
    }
  }
}

angular.module('GLA')
  .service('ConfigurationService', ConfigurationService);
