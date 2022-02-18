/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

OrganisationGroupService.$inject = ['$http', 'config'];

function OrganisationGroupService($http, config) {

  return {

    /**
     * Retrieve all available organisation groups
     * @returns {Object} promise
     */
    findAll: function () {
      return $http({
        url: `${config.basePath}/organisationGroups`,
        method: 'GET'
      })
    },

    /**
     * Retrieve organisation group by id
     * @param {Number} id
     * @returns {Object} promise
     */
    findById: function (id) {
      return $http({
        url: `${config.basePath}/organisationGroups/${id}`,
        method: 'GET'
      })
    },

    /**
     * Retrieve organisation group by id
     * @param {Number} id
     * @returns {Object} promise
     */
    findById: function (id) {
      return $http({
        url: `${config.basePath}/organisationGroups/${id}`,
        method: 'GET'
      });
    },

    /**
     * Get organisation groups (consortiums) by organisation id and programme id
     * @param {number} orgId
     * @param {number} programmeId
     * @returns {Promise} A promise which resolves to the list of consortiums
     */
    getOrganisationGroupsForOrg: function (orgId, programmeId) {
      if (!orgId || !programmeId) {
        throw Error('missing required parameters');
      }
      return $http({
        url: `${config.basePath}/organisationGroupsForOrg/${orgId}/programme/${programmeId}`,
        method: 'GET'
      });
    },

    /**
     * Create a new organisation group (consortiums & parnerships)
     * @param  {Object} data
     * @return {Object} promise
     */
    createOrganisationGroup: function(data) {
      return $http({
        url: `${config.basePath}/organisationGroups`,
        method: 'POST',
        data: data
      });
    },


    /**
     * Update organisation group (consortiums & parnerships)
     * @param  {Object} data
     * @return {Object} promise
     */

    updateOrganisationGroup: function(data) {
      return $http({
        url: `${config.basePath}/organisationGroups/${data.id}`,
        method: 'PUT',
        data: data
      });
    },

    /**
     * Looks up an organisation name by ID or provider number, return a 404 if not found.
     * Also Validates if org can be associated with programme
     * @param  {String} orgCode organisation ID or provider number
     * @return {Object} promise
     */
    lookupOrgNameByCodeForConsortium: function(orgCode) {
      let code = (orgCode || '').toString().toUpperCase();
      return $http({
        url: `${config.basePath}/organisationGroups/organisation?orgCode=${code}`,
        method: 'GET'
      })
    },

    /**
     * Gets organisations inside consortium/partnership which already have a project.
     * @param  {Number} id consortium/partnership id
     * @return {Promise} promise of organisations
     */
    organisationsInProjects: function(id) {
      return $http({
        url: `${config.basePath}/organisationGroups/${id}/organisationsInProjects`,
        method: 'GET'
      })
    }
  }
}

angular.module('GLA')
  .service('OrganisationGroupService', OrganisationGroupService);
