/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

LabelService.$inject = ['$http', 'config'];

function LabelService($http, config) {
  return {
    getPreSetLabels: function (organisationId, markedForCorporateReport) {
      let cfg = {
        params: {
          managingOrganisationId: organisationId || null,
          markedForCorporate: markedForCorporateReport,
        }
      };

      return $http.get(`${config.basePath}/preSetLabels`, cfg);
    },

    createLabel(data) {
      return $http.post(`${config.basePath}/preSetLabels`, data);
    },

    updateLabel(data) {
      return $http.put(`${config.basePath}/preSetLabels/${data.id}`, data);
    },

    deleteLabel(data) {
      return $http.delete(`${config.basePath}/preSetLabels/${data.id}`);
    },

  }
}

angular.module('GLA')
  .service('LabelService', LabelService);
