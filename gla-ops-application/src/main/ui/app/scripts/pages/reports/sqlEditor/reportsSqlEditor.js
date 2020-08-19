/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ReportsSqlEditorCtrl {
  constructor(ReportService, FileService, $state, $scope) {
    this.ReportService = ReportService;
    this.FileService = FileService;
    this.$state = $state;
    this.$scope = $scope;
  }

  $onInit() {
  }

  back() {
    this.$state.go('reports');
  }

  isSQLValid() {
    let hasUnquotedSemicolumns = /^([^']|'[^']*")*?(;)/;
    return this.sql.toUpperCase().startsWith('SELECT')
      && this.sql.toUpperCase().indexOf('INSERT') === -1
      && this.sql.toUpperCase().indexOf('UPDATE') === -1
      && this.sql.toUpperCase().indexOf('DELETE') === -1
      && !hasUnquotedSemicolumns.test(this.sql);
  }

  generateCSV() {
    this.serverError = null;

    this.ReportService.generateAdHocReport(this.sql, this.fileName)
      .then((resp) => {
        let fileName = this.FileService.extractFileNameFromResponse(resp);

        if (window.navigator.msSaveOrOpenBlob) {
          var blob = new Blob([decodeURIComponent(encodeURI(resp.data))], {
            type: 'text/csv;charset=utf-8;'
          });
          navigator.msSaveBlob(blob, fileName);
        } else {
          var a = document.createElement('a');
          a.href = 'data:attachment/csv;charset=utf-8,' + encodeURI(resp.data);
          a.target = '_blank';
          a.download = fileName;
          document.body.appendChild(a);
          a.click();
        }
      })
      .catch(err => {
        this.serverError = err.data;
      });
  }

}

ReportsSqlEditorCtrl.$inject = ['ReportService', 'FileService', '$state', '$scope'];


angular.module('GLA')
  .component('reportsSqlEditor', {
    templateUrl: 'scripts/pages/reports/sqlEditor/reportsSqlEditor.html',
    controller: ReportsSqlEditorCtrl
  });
