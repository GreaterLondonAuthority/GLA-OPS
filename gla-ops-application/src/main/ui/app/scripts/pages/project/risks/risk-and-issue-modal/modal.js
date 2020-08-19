/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function RiskAndIssueModal($uibModal, RisksService) {
  return {
    show: function (type, riskCategories, riskOrIssue) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/risks/risk-and-issue-modal/modal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.type = type;
          this.riskCategories = riskCategories;
          this.dataBlock = riskOrIssue || {};
          this.btnName = this.dataBlock.id? 'SAVE' : 'CREATE';
          if(type === 'Risk') {
            this.probabilities = RisksService.getProbabilityRating();
            this.impacts = RisksService.getImpactRating();
            this.probabilitiesAfterMitigation = RisksService.getProbabilityRating(true);
            this.impactsAfterMitigation = RisksService.getImpactRating(true);
          } else if(type === 'Issue'){
            this.issueImpactLevels = RisksService.getIssueImpactLevels();
          }

          // this.create = () => {
          //   $uibModal.close({
          //     description: '',
          //     initialImpactRating: '',
          //     initialProbabilityRating: '',
          //     residualImpactRating: '',
          //     residualProbabilityRating: '',
          //     riskCategory: '',
          //     status: '',
          //     title: '',
          //     type: 'Risk'
          //   })
          // };
        }]
      });
    }
  };
}

RiskAndIssueModal.$inject = ['$uibModal', 'RisksService'];

angular.module('GLA')
  .service('RiskAndIssueModal', RiskAndIssueModal);
