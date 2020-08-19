/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class OutcomeAssessmentSummary {

  constructor($state){
    this.$state = $state;
  }

  $onInit() {
    this.currentState = {
      titleForBackBtn: 'BACK',
      name: this.$state.current.name,
      params: angular.copy(this.$state.params)
    }
  }


  isComplete(assessment){
    return assessment && assessment.status === 'Completed';
  }

}

OutcomeAssessmentSummary.$inject = ['$state'];

gla.component('outcomeAssessmentSummary', {
  templateUrl: 'scripts/pages/assessment/outcome-assessment-summary/outcomeAssessmentSummary.html',
  controller: OutcomeAssessmentSummary,
  bindings: {
    assessment: '<'
  },
});

