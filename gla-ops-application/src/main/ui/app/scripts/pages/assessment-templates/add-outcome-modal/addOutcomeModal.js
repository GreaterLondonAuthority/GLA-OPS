/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function AddOutcomeModal($uibModal, $rootScope, AssessmentService) {
  return {
    show: function (assessmentTemplate) {
      var modal = $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/assessment-templates/add-outcome-modal/addOutcomeModal.html',
        size: 'md',
        resolve: {
          assessmentTemplate: () => {
            return assessmentTemplate;
          }
        },

        controller: ['$uibModalInstance', 'assessmentTemplate', function ($uibModalInstance, assessmentTemplate) {
          this.assessmentTemplate = assessmentTemplate;

          this.allowToProceedTypes = AssessmentService.getOutcomeTypes();

          // init outcome if its for creation
          if (!this.outcome) {
            this.outcome = {}
          }

          if (!this.outcome.allowToProceed) {
            this.outcome.allowToProceed = AssessmentService.getDefaultOutcomeType();
          }

          this.addOutcome = () => {
            $uibModalInstance.close(this.outcome);
          };

        }]
      });

      return modal;
    }
  };
}

AddOutcomeModal.$inject = ['$uibModal', '$rootScope', 'AssessmentService'];

angular.module('GLA')
  .service('AddOutcomeModal', AddOutcomeModal);
