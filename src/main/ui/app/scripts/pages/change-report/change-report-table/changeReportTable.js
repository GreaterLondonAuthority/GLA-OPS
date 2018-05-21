/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
function index(obj, i) {
  if(!obj){
    return;
  }
  return obj[i];
}
class ChangeReportTable {
  constructor($rootScope, $scope) {
    this.hasRightValues = false;
    this.hasLeftValues = false;
    _.forEach(this.rows, (row)=>{
      if(!!row.right){
        this.hasRightValues = true;
      }
      if(!!row.left){
        this.hasLeftValues = true;
      }
    });

    this.sideControler = {
      left: this.hasLeftValues,
      right: this.hasRightValues
    };
    // console.log('sideControler', this.sideControler);
  }

  getValue(data, field) {
    if(!data){
      return;
    }

    //function index(obj,i){return obj[i]};'a.b.etc'.split('.').reduce(index, {a:{b:{etc:123}}})
    return field.split('.').reduce(index, data);
  }
}

ChangeReportTable.$inject = ['$rootScope', '$scope'];

angular.module('GLA')
  .component('changeReportTable', {
    bindings: {
      label: '<',
      heading: '<',
      rows: '<',
      fields: '<',
      changes: '<?',
      showTableSeparators: '<?', //Shows gaps in risks block when now risks and issues. Adding this property to workaround it
      showNoElementMessage: '<?'
    },
    templateUrl: 'scripts/pages/change-report/change-report-table/changeReportTable.html',
    controller: ChangeReportTable
  });
