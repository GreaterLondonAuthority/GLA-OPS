/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class DeveloperLedGrantChangeReport {
  constructor(GrantService) {
    this.GrantService = GrantService;
  }

  $onInit() {
    this.reportData = this.GrantService.prepareReportData(this.data.left, this.data.right);

    this.blockFields =
      {
        left: this.data.left,
        right: this.data.right,
        changes: this.data.changes
      };
    this.tenuresFields = [
      {
        field: 'tenureType.name',
        label: 'TENURE TYPE',
      },
      {
        field: 's106Units',
        label: 'S106 AGREEMENT UNITS',
      },
      {
        field: 'additionalAffordableUnits',
        label: 'ADDITIONAL AFFORDABLE UNITS',
      },
      {
        field: 'totalCost',
        label: 'TOTAL DEVELOPMENT COSTS £',
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
        field: 'totalS106Units',
        label: 'S106 AGREEMENT UNITS',
      },
      {
        field: 'totalAdditionalUnits',
        label: 'ADDITIONAL AFFORDABLE UNITS',
      },
      {
        field: 'totalCost',
        label: 'TOTAL DEVELOPMENT COSTS £',
      }
    ];

    this.totalsFields.forEach(item =>{
      item.changeAttribute = `totals.${item.field}`;
    });

  }

  canShowOtherAffordableTenureType(){
    return (this.blockFields.left && this.blockFields.left.otherAffordableTenureType)
      || (this.blockFields.right && this.blockFields.right.otherAffordableTenureType);
  }
}

DeveloperLedGrantChangeReport.$inject = ['GrantService'];

angular.module('GLA')
  .component('developerLedGrantChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/developerLedGrantChangeReport.html',
    controller: DeveloperLedGrantChangeReport  });
