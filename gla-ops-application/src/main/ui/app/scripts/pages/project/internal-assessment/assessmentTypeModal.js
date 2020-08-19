/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function AssessmentTypeModal($uibModal) {
  return {
    show: function (assessmentTemplates) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/internal-assessment/assessmentTypeModal.html',
        size: 'md',
        controller: [function () {
          this.assessmentTemplates = assessmentTemplates;
        }]
      });
    }
  }
}

AssessmentTypeModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('AssessmentTypeModal', AssessmentTypeModal);
