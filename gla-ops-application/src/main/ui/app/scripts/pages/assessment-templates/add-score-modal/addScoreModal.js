/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function AddScoreModal($uibModal, $rootScope, AssessmentService) {
  return {
    show: function (assessmentTemplate) {
      var modal = $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/assessment-templates/add-score-modal/addScoreModal.html',
        size: 'md',
        resolve: {
          assessmentTemplate: () => {
            return assessmentTemplate;
          }
        },

        controller: ['$uibModalInstance', 'assessmentTemplate', function ($uibModalInstance, assessmentTemplate) {
          this.assessmentTemplate = assessmentTemplate;

          this.addScore = () => {
            $uibModalInstance.close(this.score);
          };

        }]
      });

      return modal;
    }
  };
}

AddScoreModal.$inject = ['$uibModal', '$rootScope', 'AssessmentService'];

angular.module('GLA')
  .service('AddScoreModal', AddScoreModal);
