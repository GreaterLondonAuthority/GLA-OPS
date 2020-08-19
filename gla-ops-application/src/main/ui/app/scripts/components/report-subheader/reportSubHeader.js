/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');

class ReportSubHeader {
  constructor() {
  }

  $onInit() {
    let approvalTimes = this.project.projectBlocksSorted.map(item => {
      return item.approvalTime;
    });
    this.lastApproved = _.max(approvalTimes);
  }
}

ReportSubHeader.$inject = [];

gla.component('reportSubheader', {
  templateUrl: 'scripts/components/report-subheader/reportSubHeader.html',
  controller: ReportSubHeader,
  bindings: {
    project: '<',
    template: '<',
    showDatePicker: '<?',
    onBack: '&'
  },
  transclude: true
});

