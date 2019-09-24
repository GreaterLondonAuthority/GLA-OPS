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
     * Returns all messages configured by admin
     * @returns {*}
     */
    getMessages() {
      return $http({
        url: `${config.basePath}/messages`,
        method: 'GET'
      });
    },


    /**
     * Returns 'Coming Soon' message configured by admin
     * @returns {*}
     */
    comingSoonMessage() {
      return $http({
        url: `${config.basePath}/messages/coming-soon`,
        method: 'GET',
        transformResponse: (data, headers, status) => {
          return data;
        }
      });
    },

    /**
     * Returns 'system outage' message configured by admin
     * @returns {*}
     */
    systemOutageMessage() {
      return $http({
        url: `${config.basePath}/messages/system-outage`,
        method: 'GET',
        transformResponse: (data, headers, status) => {
          return data;
        }
      });
    },

    /**
     * Returns 'home page' message configured by admin
     * @returns {*}
     */
    homePageMessage() {
      return $http({
        url: `${config.basePath}/messages/home-page`,
        method: 'GET',
        transformResponse: (data, headers, status) => {
          return data;
        }
      });
    },

    /**
     * Returns 'home page' message configured by admin
     * @returns {*}
     */
    getMessage( messageKey) {
      return $http({
        url: `${config.basePath}/messages/${messageKey}`,
        method: 'GET',
        transformResponse: (data, headers, status) => {
          return data;
        }
      });
    },

    /**
     * Updates 'Coming Soon' message configured by admin
     * @param message <{code, text}>
     * @returns {*}
     */
    udpateConfigMessage(message) {
      return $http({
        url: `${config.basePath}/messages/${message.code}`,
        method: 'PUT',
        data: message
      });
    },

    isResizeCssPropertySupported() {
      let textarea = document.createElement('textarea');
      return textarea.style.resize != undefined;
    }
  }
}

angular.module('GLA')
  .service('ConfigurationService', ConfigurationService);
