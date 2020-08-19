/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

let id = 0;

class GrantCategoriesTable {
  constructor($timeout) {
    this.$timeout = $timeout;
  }

  $onInit() {
    id++;
    this.commentsId = `commentsId${id}`;
    this.noDataId = `noDataId${id}`;
    this.total = _.sumBy(this.rows, 'value');
    this.hasComments = (this.rows || []).some(r => !!r.comments);
  }

  onNoDataClick(){
    //Need $timeout to have parent noDataModel updated
    this.$timeout(()=>{
      this.onNoDataChange();
    });
  }
}


GrantCategoriesTable.$inject = ['$timeout'];

angular.module('GLA')
  .component('grantCategoriesTable', {
    templateUrl: 'scripts/pages/organisation/recoverable-grant-submission/grant-categories-table/grantCategoryTables.html',
    bindings: {
      rows: '<',
      noDataModel: '=',
      noDataLabel: '@',
      grantType: '<',
      categories: '<',
      showComments: '=',
      readOnly: '<',
      onEdit: '&',
      onAdd: '&',
      onNoDataChange: '&',
      deleteRow: '&'
    },
    controller: GrantCategoriesTable
  });

