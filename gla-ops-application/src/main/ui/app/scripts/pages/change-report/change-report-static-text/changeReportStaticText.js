/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ChangeReportStaticText {
  constructor(ReportService) {
    this.ReportService = ReportService;
    this.displayMode = this.ReportService.getReportDisplayMode();
  }
}

ChangeReportStaticText.$inject = ['ReportService'];

angular.module('GLA')
  .component('changeReportStaticText', {
    bindings: {
      data: '<',
      text: '<',
      subtext: '<'
    },
    templateUrl: 'scripts/pages/change-report/change-report-static-text/changeReportStaticText.html',
    controller: ChangeReportStaticText
  });
