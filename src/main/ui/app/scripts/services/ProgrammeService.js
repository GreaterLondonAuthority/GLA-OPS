/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ProgrammeService.$inject = ['$resource', '$http', 'config'];

function ProgrammeService($resource, $http, config) {

  return {

    /**
     * Retrieve list of all programmes relevant to that user
     * @returns {Object} promise
     */
    getProgrammes: function () {
      return $http({
        url: config.basePath + '/programmes',
        method: 'GET'
      });
    },

    /**
     * Retrieve list of all programmes
     * @returns {Object} promise
     */
    getEnabledProgrammes: function () {
      return $http({
        url: config.basePath + '/programmes',
        method: 'GET',
        params: {
          enabled: true
        }
      });
    },

    /**
     * Retrieves a single programme
     * @param {Number} programmeId
     * @returns {Object} promise
     */
    getProgramme: function(programmeId) {
      return $http({
        url: config.basePath + '/programmes/' + programmeId,
        method: 'GET'
      })
    },

    /**
     * Retrieve all available templates
     */
    getAllProjectTemplates: function () {
      return $http({
        url: config.basePath + '/templates',
        method: 'GET'
      });
    },


    /**
     * Retrieve list of all templates by programme
     * @returns {Object} promise
     */
    getTemplateByProgramme: function (programmeId) {
      return $resource(`${config.basePath}/templates`)
        .query({
          programmeId:  programmeId
        })
        .$promise;
    },


    /**
     * Retrieve all available templates
     */
    getBoroughsByProgramme: function (programmeId) {
      return $http({
        url: config.basePath + '/programmes/'+programmeId+'/borough',
        method: 'GET'
      });
    },


    /**
     * Retrieve all available templates
     */
    getStatusesByProgramme: function (programmeId) {
      return $http({
        url: config.basePath + '/programmes/'+programmeId+'/status',
        method: 'GET'
      });
    },



    /**
     * Create a new programme
     */
    createProgramme: function (data) {
      return $http({
        url: config.basePath + '/programmes',
        method: 'POST',
        data: data,
        serialize: false
      })
    },
    /**
     * Create a new programme
     */
    updateProgramme: function (data, programmeId) {
      return $http({
        url: config.basePath + '/programmes/'+programmeId,
        method: 'PUT',
        data: data,
        serialize: false
      })
    },

    getProgrammeNumberOfProjects: function(programmeId) {
      return $http({
        url: config.basePath + '/programmes/'+programmeId+'/projectCountPerTemplate'
      });
    },

    /**
     * Enable / disable a programme.
     */
    updateEnabled: function(programmeId, enabled) {
      return $http({
        url: config.basePath + '/programmes/'+programmeId+'/enabled',
        method: 'PUT',
        data: enabled,
        serialize: false
      })
    }
  }
}

angular.module('GLA')
  .service('ProgrammeService', ProgrammeService);
