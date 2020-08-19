/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class EditAssessmentCtrl {
  constructor($state, AssessmentService, ConfirmationDialog, $q, ErrorService, UserService) {
    this.requestsQueue = [];
    this.$state = $state;
    this.AssessmentService = AssessmentService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.$q = $q;
    this.ErrorService = ErrorService;
    this.UserService = UserService;

  }

  $onInit() {
    this.editable = true;
    this.readOnly = false;
    this.assessmentStatus = this.AssessmentService.getAssessmentStatus(this.assessment);

    this.failedCriteriaDropdown = this.AssessmentService.getFailedCriteriaDropdown();
    this.hasOutcomes = this.assessmentTemplate.outcomes && this.assessmentTemplate.outcomes.length > 0;
    this.lastRequestId = 0;
    this.assessmentCompleted =  this.assessment.status === 'Completed';
    this.currentState = {
      name: this.$state.current.name,
      params: angular.copy(this.$state.params)
    }
  }

  onBack() {
    this.stopEditing();
  }

  stopEditing() {
    return this.save().then(rsp => {
      return this.$state.go('assessment', {
        id: this.assessment.id,
        assessment: this.assessment
      });
    });
  }

  save() {
    let requestId = ++this.lastRequestId;
    let p = this.$q.all(this.requestsQueue).then(() => {
      this.assessment.status = this.assessmentCompleted ? 'Completed' : 'InProgress';
      return this.AssessmentService.saveAssessment(this.assessment.projectId, this.assessment).then((resp) => {
        if (requestId === this.lastRequestId) {
          this.assessment = resp.data;
        }
      }).catch(err => {
        let errMessage = (err.data || {}).description || 'Failed to save';
        this.ConfirmationDialog.warn(errMessage);
      });
    });
    this.requestsQueue.push(p);
    return p;
  }

  abandonAssessment(assessment) {
    // let modal = this.ConfirmationDialog.delete('Are you sure you want to abandon the assessment?');
    let modal = this.ConfirmationDialog.show({
      message: 'Are you sure you want to abandon the assessment?',
      approveText: 'ABANDON',
      dismissText: 'KEEP'
    });
    modal.result.then(() => {
      let p = this.AssessmentService.abandonAssessment(assessment.id)
        .then(rsp => {
          return this.$state.go('project.internal-assessment', {
            blockId: assessment.blockId,
            projectId: assessment.projectId,
          });
        })
        .catch(this.ErrorService.apiValidationHandler());
      this.requestsQueue.push(p);
    });
  }


  isAbandonButtonVisible(){
    let currentUser = this.UserService.currentUser().username;
    return !this.readOnly && this.assessment.id && !this.assessmentCompleted &&
      this.assessment.createdBy === currentUser && this.assessment.status === 'InProgress';
  }
}

EditAssessmentCtrl.$inject = ['$state', 'AssessmentService', 'ConfirmationDialog', '$q', 'ErrorService', 'UserService'];

angular.module('GLA')
  .component('editAssessment', {
    templateUrl: 'scripts/pages/assessment/assessmentForm.html',
    bindings: {
      assessment: '<',
      assessmentTemplate: '<',
      project: '<'
    },
    controller: EditAssessmentCtrl
  });
