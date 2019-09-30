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
    getProgrammes: function (params) {
      return $http({
        url: config.basePath + '/programmes',
        method: 'GET',
        params: _.merge({
          page: 0,
          size: 1000,
          sort: 'name,asc',
        }, params)
      });
    },

    /**
     * Retrieve list of all programmes
     * @returns {Object} promise
     */
    getEnabledProgrammes: function () {
      return this.getProgrammes({enabled: true});
    },

    /**
     * Retrieves a single programme
     * @param {Number} programmeId
     * @returns {Object} promise
     */
    getProgramme: function (programmeId, enrich) {
      return $http({
        url: config.basePath + '/programmes/' + programmeId,
        method: 'GET',
        params: {
          enrich: enrich
        }
      })
    },


    /**
     * Retrieve all available templates
     */
    getBoroughsByProgramme: function (programmeId) {
      return $http({
        url: config.basePath + '/programmes/' + programmeId + '/borough',
        method: 'GET'
      });
    },


    /**
     * Retrieve all available templates
     */
    getStatusesByProgramme: function (programmeId) {
      return $http({
        url: config.basePath + '/programmes/' + programmeId + '/status',
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
        url: config.basePath + '/programmes/' + programmeId,
        method: 'PUT',
        data: data,
        serialize: false
      })
    },

    getProgrammeNumberOfProjects: function (programmeId) {
      return $http({
        url: config.basePath + '/programmes/' + programmeId + '/projectCountPerTemplate'
      });
    },

    /**
     * Enable / disable a programme.
     */
    updateEnabled: function (programmeId, enabled) {
      return $http({
        url: config.basePath + '/programmes/' + programmeId + '/enabled',
        method: 'PUT',
        data: enabled,
        serialize: false
      })
    },

    getWbsCodeTypes() {
      return [{
        key: null,
        label: 'Not provided'
      }, {
        key: 'Capital',
        label: 'Capital'
      }, {
        key: 'Revenue',
        label: 'Revenue'
      }];
    },

    labels() {
      return {
        programmeInfo: {
          managingOrganisation: 'Managing organisation',
          status: 'Programme status',
          glaInternal: 'Programme restricted for GLA internal use only',
          enableForProjects: 'Programme enabled for new projects',
          markForAssessment: 'Programme marked for assessment',
          totalProjects: 'Total submitted projects',
          financialYear: 'Financial year',
        },

        projectType: {
          wbsCapital: 'WBS Capital',
          wbsRevenue: 'WBS Revenue',
          ceCode: 'CE Code',
          paymentDefault: 'Payment Default',
          status: 'Status',
          statusModel: 'Status model',
          paymentsEnabled: 'Payment claims enabled',
          assessmentTemplates: 'Assessment templates'
        }
      }
    }
  }
}

angular.module('GLA')
  .service('ProgrammeService', ProgrammeService);
