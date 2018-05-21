/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class UnitDetailsChangeReport {
  constructor($rootScope, $scope, ReferenceDataService, UnitsService) {

  this.UnitsService = UnitsService;
  }

  $onInit() {
    this.UnitsService.getUnitsMetadata(this.data.context.project.left ? this.data.context.project.left.id  : this.data.context.project.right.id).then((unitsMetadata) => {
      this.tenureIdToName = unitsMetadata.data.tenureDetails.reduce((idToName, item)=>{
        idToName[item.id] = item.name;
        return idToName;
      }, {});
      if(this.data.left){
        _.forEach(this.data.left.tableEntries, (entry) => {
          entry.tenureName = this.tenureIdToName[entry.tenureId];
        });
      }
      if(this.data.right){
        _.forEach(this.data.right.tableEntries, (entry) => {
          entry.tenureName = this.tenureIdToName[entry.tenureId];
        });
      }
      this.reportData = this.UnitsService.prepareReportData(this.data.left, this.data.right);
      if(this.data.left && this.data.right){
        this.data.changes.addDeletions(this.reportData.salesUnits);
        this.data.changes.addDeletions(this.reportData.rentalsUnits);
      }

      this.tenureSummariesFields = [
        {
          label: 'Profiled Units',
          field: 'profiledUnits',
          format: 'number'
        },
        {
          label: 'Total Units',
          field: 'totalUnits',
          format: 'number'
        }
      ];
      this.rentalsUnitsFields = [{
        label: 'TENURE',
        field: 'tenureName',
        format: ''
      },{
        label: 'MARKET TYPE',
        field: 'marketType.name',
        format: ''
      },{
        label: 'BED(S)',
        field: 'nbBeds.displayValue',
        format: ''
      },{
        label: 'UNIT TYPE',
        field: 'unitType.displayValue',
        format: ''
      },{
        label: 'UNITS',
        field: 'nbUnits',
        format: 'number'
      },{
        label: 'NET WEEKLY RENT £',
        field: 'netWeeklyRent',
        format: 'currency'
      },{
        label: 'WEEKLY SC £',
        field: 'weeklyServiceCharge',
        format: 'currency'
      },{
        label: 'RENT TOTAL £',
        field: 'rentTotal',
        format: 'currency'
      },{
        label: 'WEEKLY MARKET RENT £',
        field: 'weeklyMarketRent',
        format: 'currency'
      },{
        label: 'RENT AS A % OF MARKET RENT',
        field: 'rentPercentageOfMarket',
        format: 'number'
      }];

      let hiddenSalesColumns = this.UnitsService.hiddenSalesColumns(unitsMetadata.data);

      this.salesUnitsFields = [{
        label: 'TENURE',
        field: 'tenureName',
        format: ''
      },{
        label: 'MARKET TYPE',
        field: 'marketType.name',
        format: ''
      },{
        label: 'BED(S)',
        field: 'nbBeds.displayValue',
        format: ''
      },{
        label: 'UNIT TYPE',
        field: 'unitType.displayValue',
        format: ''
      },{
        label: 'UNITS',
        field: 'nbUnits',
        format: 'number'
      },{
        label: 'MARKET VALUE £',
        field: 'marketValue',
        format: 'number'
      },{
        label: 'FIRST TRANCHE SALES %',
        field: 'firstTrancheSales',
        format: 'number',
        hidden: hiddenSalesColumns.firstTrancheSales
      },{
        label: '% DISCOUNT OFF MARKET VALUE',
        field: 'discountOffMarketValue',
        format: 'number',
        hidden: hiddenSalesColumns.discountOffMarketValue
      },{
        label: 'NET WEEKLY RENT £',
        field: 'netWeeklyRent',
        format: 'currency',
        hidden: hiddenSalesColumns.netWeeklyRent
      },{
        label: 'WEEKLY SC £',
        field: 'weeklyServiceCharge',
        format: 'number'
      }];

      this.salesUnitsFields = _.filter(this.salesUnitsFields, f => {return !f.hidden });


      this.buildTypeFields = [{
        label: 'BUILD TYPE',
        field: 'buildType',
        format: ''
      }, {
        label: 'NEW BUILD',
        field: 'newBuildUnits',
        format: ''
      }, {
        label: 'REFURBISHED',
        field: 'refurbishedUnits',
        format: ''
      }, {
        label: 'TOTAL',
        field: 'total',
        format: ''
      }];
      this.byNumberOfPeopleFields = [{
        label: 'PEOPLE',
        field: 'people',
        format: ''
      }, {
        label: '1',
        field: 'type1Units',
        format: ''
      }, {
        label: '2',
        field: 'type2Units',
        format: ''
      }, {
        label: '3',
        field: 'type3Units',
        format: ''
      }, {
        label: '4',
        field: 'type4Units',
        format: ''
      }, {
        label: '5',
        field: 'type5Units',
        format: ''
      }, {
        label: '6',
        field: 'type6Units',
        format: ''
      }, {
        label: '7',
        field: 'type7Units',
        format: ''
      }, {
        label: '8+',
        field: 'type8Units',
        format: ''
      }, {
        label: 'TOTAL',
        field: 'total',
        format: ''
      }];
    });
  }
}

UnitDetailsChangeReport.$inject = ['$rootScope', '$scope', 'ReferenceDataService', 'UnitsService'];

angular.module('GLA')
  .component('unitDetailsChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/unitDetailsChangeReport.html',
    controller: UnitDetailsChangeReport  });
