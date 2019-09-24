/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class FundingSummaryReport {
  constructor(ProjectFundingService) {
    this.ProjectFundingService = ProjectFundingService;
  }


  $onInit() {
    let fundingSpendType = this.block.fundingSpendType;

    this.capClaimedFunding = this.block.capClaimedFunding ? this.block.capClaimedFunding : 'GLA CAPITAL CONTRIBUTION £';
    this.capOtherFunding = this.block.capOtherFunding ? this.block.capOtherFunding : 'APPLICANT CAPITAL CONTRIBUTION £';
    this.revClaimedFunding = this.block.revClaimedFunding ? this.block.revClaimedFunding : 'GLA REVENUE CONTRIBUTION £';
    this.revOtherFunding = this.block.revOtherFunding ? this.block.revOtherFunding : 'APPLICANT REVENUE CONTRIBUTION £';

    if(fundingSpendType === 'CAPITAL_ONLY'){
      this.showCapital = true;
      this.showRevenue = false;
    } else if(fundingSpendType === 'REVENUE_ONLY'){
      this.showCapital = false;
      this.showRevenue = true;
    } else {
      this.showCapital = true;
      this.showRevenue = true;
    }
    this.ProjectFundingService.addYearLabels(this.block.fundingTotalBudget.years);
  }
}

FundingSummaryReport.$inject = ['ProjectFundingService'];


angular.module('GLA')
  .component('fundingSummaryReport', {
    bindings: {
      block: '<',
    },
    templateUrl: 'scripts/pages/summary-report/fundingSummaryReport.html',
    controller: FundingSummaryReport
  });
