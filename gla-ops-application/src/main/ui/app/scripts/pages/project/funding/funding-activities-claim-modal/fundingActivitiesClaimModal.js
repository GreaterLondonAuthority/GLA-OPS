/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function FundingActivitiesClaimModal($uibModal) {
  return {
    show: function (config) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/funding/funding-activities-claim-modal/fundingActivitiesClaimModal.html',
        size: 'md',
        controller: [function () {
          this.section = config.section;
          this.activity = config.activity;
          this.budget = config.budget;
          this.unclaimedGrant = config.unclaimedGrant;
          this.showCapitalGla = config.showCapitalGla;
          this.showRevenueGla = config.showRevenueGla;
          this.showCapitalOther = config.showCapitalOther;
          this.showRevenueOther = config.showRevenueOther;
          this.readOnly = config.readOnly;
        }]
      });
    }
  };
}

FundingActivitiesClaimModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('FundingActivitiesClaimModal', FundingActivitiesClaimModal);
