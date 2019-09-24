/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class AssessmentCtrl {
  constructor($state, $stateParams, AssessmentService) {
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.AssessmentService = AssessmentService;
  }

  $onInit() {
    this.readOnly = true;
    this.maxScore = _.maxBy(this.assessmentTemplate.scores, 'score');
  }

  onBack() {
    if (this.$stateParams.backNavigatesTo === 'assessments') {
      this.$state.go('assessments');
    }
    else {
      this.$state.go('project.internal-assessment', {
        blockId: this.assessment.blockId,
        projectId: this.assessment.projectId,
      });
    }
  }

  getFailedCriteriaText(failedCriteriaValue){
    return this.AssessmentService.getFailedCriteriaText(failedCriteriaValue);
  }

  edit() {
    this.$state.go('assessment-edit', {
      id: this.assessment.id,
      assessment: this.assessment
    });
  }
}

AssessmentCtrl.$inject = ['$state', '$stateParams', 'AssessmentService'];

angular.module('GLA')
  .component('assessment', {
    templateUrl: 'scripts/pages/assessment/assessment.html',
    bindings: {
      editable: '<',
      assessment: '<',
      assessmentTemplate: '<'
    },
    controller: AssessmentCtrl
  });
