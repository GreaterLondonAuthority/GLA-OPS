/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');

class ReportHeader {
  constructor(ProjectService) {
    this.ProjectService = ProjectService;
  }

  $onInit() {
    this.subStatusText = this.ProjectService.subStatusText(this.project);
  }
}

ReportHeader.$inject = ['ProjectService'];

gla.component('reportHeader', {
  templateUrl: 'scripts/components/report-header/reportHeader.html',
  controller: ReportHeader,
  bindings: {
    project: '<',
    template: '<?',
    hideSubheader: '<?',
    onBack: '&'
  },
  transclude: true
});

