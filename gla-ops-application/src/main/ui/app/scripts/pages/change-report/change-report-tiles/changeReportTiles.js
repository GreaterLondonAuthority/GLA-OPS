/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ChangeReportTile {
  constructor() {

  }

  $onInit(){
    this.hasRightValues = false;
    _.forEach(this.rows, (row)=>{if(row.right){this.hasRightValues = true}});
  }

}

ChangeReportTile.$inject = [];

angular.module('GLA')
  .component('changeReportTiles', {
    bindings: {
      headingField: '<',
      descriptionField: '<?',
      rows: '<',
      fields: '<',
      changes: '<?'
    },
    templateUrl: 'scripts/pages/change-report/change-report-tiles/changeReportTiles.html',
    controller: ChangeReportTile
  });
