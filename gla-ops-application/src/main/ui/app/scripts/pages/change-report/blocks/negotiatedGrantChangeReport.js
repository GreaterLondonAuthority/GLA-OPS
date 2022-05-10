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
    this.summariesFields = [];

    this.blockFields =
      {
        left: this.data.left,
        right: this.data.right,
        changes: this.data.changes
      };
    this.tenuresFields = [
      {
        field: 'tenureType.name',
        label: 'TENURE TYPE'
      },
      {
        field: 'grantRequested',
        format: 'currency',
        label: '£ GRANT REQUESTED',
      },
      {
        field: 'totalUnits',
        label: 'TOTAL AFFORDABLE UNITS',
      }];

    this.totalsFields = [
      {
        field: 'totalGrantRequested',
        format: 'currency',
        label: '£ GRANT REQUESTED',
      },
      {
        field: 'totalUnits',
        label: 'TOTAL AFFORDABLE UNITS',
      }];

    if (this.data.left.showSpecialisedUnits) {
      this.tenuresFields.push({
        field: 'supportedUnits',
        label: 'OF WHICH SUPPORTED & SPECIALISED UNITS',
      });
      this.totalsFields.push(
        {
          field: 'totalSupportedUnits',
          label: 'OF WHICH SUPPORTED & SPECIALISED UNITS',
        });
    }
    if (this.data.left.showDevelopmentCost) {
      this.tenuresFields.push({
        field: 'totalCost',
        format: 'currency',
        label: 'TOTAL DEVELOPMENT COSTS £',
      });
      this.totalsFields.push(
        {
          field: 'totalCost',
          format: 'currency',
          label: 'TOTAL DEVELOPMENT COSTS £',
        });
      this.summariesFields.push({
        label: 'Unit development cost',
        field: 'unitDevelopmentCost',
        format: 'currency'
      });
    }
    if (this.data.left.showDevelopmentCost && this.data.left.showPercentageCosts) {
      this.tenuresFields.push({
        field: 'percentageOfTotalCost',
        format: 'number|1',
        label: 'GRANT AS % COSTS',
      });
      this.totalsFields.push(
        {
          field: 'percentageOfTotalCost',
          format: 'number|1',
          label: 'GRANT AS % COSTS'
        });
    }

    this.summariesFields.push({
      label: 'Grant per unit',
      field: 'grantPerUnit',
      format: 'currency'
    });

    this.totalsFields.forEach(item =>{
      item.changeAttribute = `totals.${item.field}`;
    });
  }

  canShowOtherAffordableTenureType(){
    return (this.blockFields.left && this.blockFields.left.otherAffordableTenureType)
      || (this.blockFields.right && this.blockFields.right.otherAffordableTenureType);
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
