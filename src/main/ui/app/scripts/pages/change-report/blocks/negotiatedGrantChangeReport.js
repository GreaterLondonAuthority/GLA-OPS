/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class NegotiatedGrantChangeReport {
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
        field: 'grantRequested',format: 'currency',
        label: '£ GRANT REQUESTED',
      },
      {
        field: 'totalUnits',
        label: 'TOTAL AFFORDABLE UNITS',
      },
      {
        field: 'supportedUnits',
        label: 'OF WHICH SUPPORTED & SPECIALISED UNITS',
      },
      {
        field: 'totalCost',format: 'currency',
        label: 'TOTAL DEVELOPMENT COSTS £',
      }
    ];

    this.summariesFields = [
      {
        label: 'Unit development cost',
        field: 'unitDevelopmentCost',
        format: 'currency'
      },
      {
        label: 'Grant per unit',
        field: 'grantPerUnit',
        format: 'currency'
      }
    ];


    this.totalsFields = [
      {
        field: 'totalGrantRequested', format: 'currency',
        label: '£ GRANT REQUESTED',
      },
      {
        field: 'totalUnits',
        label: 'TOTAL AFFORDABLE UNITS',
      },
      {
        field: 'totalSupportedUnits',
        label: 'OF WHICH SUPPORTED & SPECIALISED UNITS',
      },
      {
        field: 'totalCost',  format: 'currency',
        label: 'TOTAL DEVELOPMENT COSTS £',
      }
    ];
    this.totalsFields.forEach(item =>{
      item.changeAttribute = `totals.${item.field}`;
    });
  }
}


NegotiatedGrantChangeReport.$inject = ['GrantService'];

angular.module('GLA')
  .component('negotiatedGrantChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/negotiatedGrantChangeReport.html',
    controller: NegotiatedGrantChangeReport  });
