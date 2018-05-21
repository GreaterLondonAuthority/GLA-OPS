/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class CalculateGrantChangeReport {
  constructor(GrantService) {
    this.GrantService = GrantService;
  }

  $onInit() {
    this.reportData = this.GrantService.prepareReportData(this.data.left, this.data.right);

    this.tenuresFields = [
      {
        field: 'tenureType.name',
        label: 'TENURE TYPE'
      },
      {
        field: 'totalUnits',
        label: 'TOTAL UNITS'
      },
      {
        field: 's106Units',
        label: 'OF WHICH NIL GRANT UNITS'
      },
      {
        field: 'totalCost',format: 'currency',
        label: 'TOTAL DEVELOPMENT COSTS'
      }
    ];


    this.summariesFields = [
      {
        label: 'Total grant eligible units',
        field: 'grantEligibleUnits',
        format: 'number'
      },
      {
        label: 'Grant per unit',
        field: 'grantRate',
        format: 'currency'
      },
      {
        label: 'Total grant',
        field: 'totalGrant',
        format: 'currency'
      }
    ];

    this.totalsFields = [
      {
        field: 'totalUnits',
        label: 'TOTAL UNITS',
      },
      {
        field: 'totalS106Units',
        label: 'OF WHICH NIL GRANT UNITS',
      },
      {
        field: 'totalCost',
        format: 'currency',
        label: 'TOTAL DEVELOPMENT COSTS',
      }
    ];
    this.totalsFields.forEach(item =>{
      item.changeAttribute = `totals.${item.field}`;
    });

  }
}

CalculateGrantChangeReport.$inject = ['GrantService'];

angular.module('GLA')
  .component('calculateGrantChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/calculateGrantChangeReport.html',
    controller: CalculateGrantChangeReport  });
