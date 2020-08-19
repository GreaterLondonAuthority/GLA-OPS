/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

let gla = angular.module('GLA');

class FinancialYearMonthlyOutputsTable {
  constructor(OutputsService, AssumptionModal, ConfirmationDialog) {
    this.OutputsService = OutputsService;
    this.AssumptionModal = AssumptionModal;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {

  }

  $onChanges(){
    this.initRows();
  }

  initRows(){
    let conditionalColumnsVisibility = [
      this.displayOutputType,
      this.blockConfig.showValueColumn
    ];
    this.columnCount = 7 - _.filter(conditionalColumnsVisibility, visible => !visible).length;

    this.expandedCategories = this.expandedCategories || {};
    (this.tableData || []).forEach(categoryRow => {
      let expanded = this.expandedCategories[this.getCategory(categoryRow)];
      categoryRow.collapsed = !expanded;
    })
  }

  getCategory(categoryRow) {
    return categoryRow[0].config.category;
  }

  getAssumption(categoryRow){
    return (this.categoriesToAssumptions || {})[categoryRow[0].config.category];
  }

  showAssumptionModal(assumption, categoryRow) {
    let modal = this.AssumptionModal.show(assumption);
    modal.result.then((assumption) => {
      if(!assumption.id){
        assumption.category = this.getCategory(categoryRow);
      }
      this.onAssumptionChange({
        event: assumption
      });
    });
  }

  getOutputTypeDesc(outputType) {
    let outputTypeDescription = this.OutputsService.getOutputTypes()[outputType.key];
    if (outputTypeDescription) {
      return outputTypeDescription;
    }
    return outputType.description;
  }

  /**
   * Returns a name for the month, from the month value or the quarter
   * @returns {String} 'MMM' or 'QN'
   */
  getPeriodName(number) {
    if (this.periodType === 'Quarterly') {
      return 'Q'+(number < 4 ? 4 : Math.round((number-1)/3));
    }
    else {
      return moment(number, 'MM').format('MMM');
    }
  }

  deleteAssumption(assumption) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the assumption?');
    modal.result.then(() => {
      this.onDeleteAssumption({
        event: assumption
      });
    });
  }

  onCollapseChange(categoryRow) {
    this.expandedCategories[this.getCategory(categoryRow)] = !categoryRow.collapsed;
  }
}

FinancialYearMonthlyOutputsTable.$inject = ['OutputsService', 'AssumptionModal', 'ConfirmationDialog'];

//TODO review which bindings could go directly into this component instead of being passed
gla.component('financialYearMonthlyOutputsTable', {
  bindings: {
    financialYear: '<',
    projectId: '<',
    blockId: '<',
    blockConfig: '<',
    tableData: '<',
    tableColumnOffsetsHeader: '<',
    tableId: '<',
    categoryName: '<',
    displayOutputType: '<',
    outputTypeName: '<',
    periodType: '<',
    expandedCategories: '<',
    currentFinancialYear: '<',
    columnOffsets: '<',
    readOnly: '<',
    categoriesToAssumptions: '<',
    showAssumptions: '<',
    onRowChanged: '&',
    onDelete: '&',
    onAssumptionChange: '&',
    onDeleteAssumption: '&'
  },
  templateUrl: 'scripts/pages/project/outputs/financial-year-monthly-outputs-table/financialYearMonthlyOutputsTable.html',
  controller: FinancialYearMonthlyOutputsTable
});
