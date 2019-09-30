/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class AssessmentListCtrl {
  constructor($state, AssessmentService) {
    this.$state = $state;
    this.AssessmentService = AssessmentService;
  }

}

AssessmentListCtrl.$inject = ['$state', 'AssessmentService'];

angular.module('GLA')
  .component('assessmentList', {
    templateUrl: 'scripts/pages/assessment/assessment-list.html',
    bindings: {
      assessments: '<'
    },
    controller: AssessmentListCtrl
  });
