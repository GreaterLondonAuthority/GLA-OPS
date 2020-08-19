/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

let gla = angular.module('GLA');

class OutputsBaselinesTable {
  constructor(OutputsService) {
    this.OutputsService = OutputsService;
  }

  $onInit() {
  }

  $onChanges(){
    this.initRows();
  }

  initRows(){
    this.expandedCategories = this.expandedCategories || {};
    let conditionalColumnsVisibility = [
      this.displayValue
    ];
    this.columnCount = 4 - _.filter(conditionalColumnsVisibility, visible => !visible).length;

    (this.tableData || []).forEach(categoryRow => {
      let expanded = this.expandedCategories[this.getCategory(categoryRow)];
      categoryRow.collapsed = !expanded;
    })
  }

  getCategory(categoryRow) {
    return categoryRow[0].config.category;
  }

  onCollapseChange(categoryRow) {
    this.expandedCategories[this.getCategory(categoryRow)] = !categoryRow.collapsed;
  }
}

OutputsBaselinesTable.$inject = ['OutputsService'];

//TODO review which bindings could go directly into this component instead of being passed
gla.component('outputsBaselinesTable', {
  bindings: {
    columnOffsets: '<',
    categoryName: '<',
    tableData: '<',
    expandedCategories: '<',
    categories: '<',
    displayOutputType: '<',
    displayValue: '<',
    readOnly: '<',
    tableId: '<',
    onRowChanged: '&',
    onDelete: '&'
  },
  templateUrl: 'scripts/pages/project/outputs/outputs-baselines-table/outputsBaselinesTable.html',
  controller: OutputsBaselinesTable
});
