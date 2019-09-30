/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class OutputsSummaryReport {
  constructor(OutputsService, ReportService) {
    this.OutputsService = OutputsService;
    this.ReportService = ReportService;
  }


  $onInit() {
    this.outputSummaries = this.OutputsService.outputSummaries(this.block);
    this.outputsExpanded = true;
    this.outputSummaries.forEach(row => row.collapsed = false);
    this.outputsSummaryTitle = this.OutputsService.getOutputBlockSummariesTitle(this.block);
    this.OutputsService.getOutputConfigGroup(this.block.configGroupId).then((resp) => {
      this.displayOutputType = resp.data.outputTypes.length > 1 || true;
    });
    this.displayValue = true;
  }

  toggleOutputs() {
    this.outputsExpanded = !this.outputsExpanded;
    this.outputSummaries.forEach(os=>{
      os.collapsed = !this.outputsExpanded;
    });
  }
}

OutputsSummaryReport.$inject = ['OutputsService', 'ReportService'];

angular.module('GLA')
  .component('outputsSummaryReport', {
    bindings: {
      block: '<',
      project: '<',
      template: '<'
    },
    templateUrl: 'scripts/pages/summary-report/outputsSummaryReport.html',
    controller: OutputsSummaryReport
  });
