/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');

class OutputsSummaryTable {
  constructor(OutputsService, Util) {
    this.OutputsService = OutputsService;
    this.NumberUtil = Util.Number;
  }

  $onInit(){
    this.unitConfig = this.OutputsService.getUnitConfig();
    this.outputsConfig = _.find(this.template.blocksEnabled, {block: 'Outputs'});
    this.showBaseline  = this.outputsConfig.showBaselines;
  }

  formatNumber(value, valueType) {
    const precision = this.unitConfig[valueType].precision || 0;
    return value ? this.NumberUtil.formatWithCommas(value, precision) : '';
  }

  showSubCategory(category) {
    let notApplicableSubcategory = _.find(category, {subcategory: 'N/A'});
    return notApplicableSubcategory == undefined;
  }
}

OutputsSummaryTable.$inject = ['OutputsService', 'Util'];

gla.component('outputSummariesTable', {
  templateUrl: 'scripts/components/output-summaries-table/outputSummariesTable.html',
  controller: OutputsSummaryTable,
  bindings: {
    outputSummaries: '<',
    onToggleRow: '&',
    template: '<',
    displayOutputType: '<',
    displayValue: '<'
  }
});

