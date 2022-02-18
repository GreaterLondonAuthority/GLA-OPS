/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import NumberUtil from '../../util/NumberUtil';

class AssessmentCtrl {
  constructor($state, $stateParams, AssessmentService, $window, SessionService) {
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.AssessmentService = AssessmentService;
    this.$window = $window;
    this.SessionService = SessionService;
  }

  $onInit() {
    this.readOnly = true;
    this.assessmentStatus = this.AssessmentService.getAssessmentStatus(this.assessment);
    this.maxScore = _.maxBy(this.assessmentTemplate.scores, 'score');
    this.hasOutcomes = this.assessmentTemplate.outcomes && this.assessmentTemplate.outcomes.length > 0;
    this.markedComplete = this.assessment.status === 'Completed';
    this.currentState = {
      name: this.$state.current.name,
      params: angular.copy(this.$state.params)
    };

    if (this.$stateParams.backNavigation) {
      this.SessionService.setAssessmentPage({backNavigation: this.$stateParams.backNavigation});
    }

    let previousState = (this.SessionService.getAssessmentPage() || {}).backNavigation || {};
    this.backBtnName = previousState.titleForBackBtn || 'ASSESSMENT OVERVIEW';
  }

  onBack() {
    let previousState = (this.SessionService.getAssessmentPage() || {}).backNavigation;
    if(previousState && previousState.name){
      this.$state.go(previousState.name, previousState.params);
    } else {
      this.$state.go('project.internal-assessment', {
        blockId: this.assessment.blockId,
        projectId: this.assessment.projectId,
      });
    }

    this.SessionService.setAssessmentPage(null);
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

  formatNumber(value) {
    return NumberUtil.formatWithCommas(value,2);
  }
}

AssessmentCtrl.$inject = ['$state', '$stateParams', 'AssessmentService', '$window', 'SessionService'];

angular.module('GLA')
  .component('glaAssessment', {
    templateUrl: 'scripts/pages/assessment/assessment.html',
    bindings: {
      editable: '<',
      assessment: '<',
      project: '<',
      assessmentTemplate: '<'
    },
    controller: AssessmentCtrl
  });
