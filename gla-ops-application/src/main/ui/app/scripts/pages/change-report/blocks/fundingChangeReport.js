/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class fundingChangeReport {
  constructor(ProjectFundingService) {
    this.ProjectFundingService = ProjectFundingService;
  }

  $onInit() {
    this.block = this.data.right || this.data.left;
    this.capClaimedFunding = this.block.capClaimedFunding ? this.block.capClaimedFunding : 'GLA CAPITAL CONTRIBUTION £';
    this.capOtherFunding = this.block.capOtherFunding ? this.block.capOtherFunding : 'APPLICANT CAPITAL CONTRIBUTION £';
    this.revClaimedFunding = this.block.revClaimedFunding ? this.block.revClaimedFunding : 'GLA REVENUE CONTRIBUTION £';
    this.revOtherFunding = this.block.revOtherFunding ? this.block.revOtherFunding : 'APPLICANT REVENUE CONTRIBUTION £';

    let fundingSpendType = (this.data.left || this.data.right).fundingSpendType;



    let listOfYears = [];
    if(this.data.left) {
      this.data.left.yearlyData = this.ProjectFundingService.processBudgetSummaries(this.data.left.budgetSummaries);
      listOfYears = _.map(this.data.left.yearlyData || [], (year) => year.year);
    }

    if(this.data.right){
      this.data.right.yearlyData = this.ProjectFundingService.processBudgetSummaries(this.data.right.budgetSummaries);
      listOfYears = _.concat(listOfYears, _.map(this.data.right.yearlyData, (year)=>year.year));
    }

    this.data.changes.changeId = (field, comparisonId) => {
      return comparisonId;
    }

    listOfYears = _.sortBy(_.uniq(listOfYears));

    this.rows = [];
    _.forEach(listOfYears, year=> {
      let left = {};
      let right = {};
      if(this.data.left){
        left = _.find(this.data.left.yearlyData, {year: year}) || {};
      }
      if(this.data.right){
        right = _.find(this.data.right.yearlyData, {year: year}) || {};
      }
      this.rows.push(
        {
          yearLabel: left.yearLabel || right.yearLabel,
          year: year,
          left: left,
          right: right,
          capitalValue: {
            left: left.capitalValue || {},
            right: right.capitalValue || {comparisonId: left.capitalValue ? left.capitalValue.comparisonId : ''}
          },
          capitalMatchFund: {
            left: left.capitalMatchFund || {},
            right: right.capitalMatchFund || {comparisonId: left.capitalMatchFund ? left.capitalMatchFund.comparisonId : ''}
          },
          revenueValue: {
            left: left.revenueValue || {},
            right: right.revenueValue || {comparisonId: left.revenueValue ? left.revenueValue.comparisonId : ''}
          },
          revenueMatchFund: {
            left: left.revenueMatchFund || {},
            right: right.revenueMatchFund || {comparisonId: left.revenueMatchFund ? left.revenueMatchFund.comparisonId : ''}
          }
        });

    })
  }
}

fundingChangeReport.$inject = ['ProjectFundingService'];

angular.module('GLA')
  .component('fundingChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/fundingChangeReport.html',
    controller: fundingChangeReport
  });
