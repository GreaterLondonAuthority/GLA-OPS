/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class EditAssessmentCtrl {
  constructor($state, AssessmentService, ConfirmationDialog, $q) {
    this.requestsQueue = [];
    this.$state = $state;
    this.AssessmentService = AssessmentService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.$q = $q;

  }

  $onInit() {
    this.editable = true;
    this.readOnly = false;
    this.failedCriteriaDropdown = this.AssessmentService.getFailedCriteriaDropdown();
  }

  onBack() {
    this.stopEditing();
  }

  stopEditing() {
    this.assessment.status = this.assessmentCompleted ? 'Completed' : 'InProgress';
    return this.save().then(rsp => {
      return this.$state.go('assessment', {
        id: this.assessment.id,
        assessment: this.assessment
      });
    });
  }

  save() {
      return this.$q.all(this.requestsQueue).then(() => {
        let p = this.AssessmentService.saveAssessment(this.assessment.projectId, this.assessment).then((resp) => {
          this.assessment = resp.data;
          this.assessmentCompleted = false;
        }).catch(err => {
          let errMessage = (err.data || {}).description || 'Failed to save';
          this.ConfirmationDialog.warn(errMessage);
        });
        this.requestsQueue.push(p);
        return p;
      });
  }

}

EditAssessmentCtrl.$inject = ['$state', 'AssessmentService', 'ConfirmationDialog', '$q'];

angular.module('GLA')
  .component('editAssessment', {
    templateUrl: 'scripts/pages/assessment/assessmentForm.html',
    bindings: {
      assessment: '<',
      assessmentTemplate: '<'
    },
    controller: EditAssessmentCtrl
  });
