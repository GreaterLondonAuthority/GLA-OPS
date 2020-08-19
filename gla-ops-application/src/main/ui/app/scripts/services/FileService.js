/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

FileService.$inject = ['$http', 'config', 'numberFilter', 'currencyFilter', 'dateFilter', '$q', '$timeout'];

function FileService($http, config) {
  return {

    getFile(attachmentId) {
      return $http.get(`${config.basePath}/file/${attachmentId}`);
    },

    extractFileNameFromResponse(resp) {
      let fileName = this.fileName;
      let disposition = resp.headers('Content-Disposition');
      if (disposition) {
        let filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        let matches = filenameRegex.exec(disposition);
        if (matches != null && matches[1]) {
          fileName = matches[1].replace(/['"]/g, '');
        }
      }
      return fileName;
    }

  };
}

angular.module('GLA')
  .service('FileService', FileService);
