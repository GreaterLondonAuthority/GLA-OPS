/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class NewAssessmentTemplateCtrl {
  constructor($state, AssessmentService, UserService, ConfirmationDialog) {
    this.$state = $state;
    this.AssessmentService = AssessmentService;
    this.UserService = UserService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.managingOrganisations = this.UserService.currentUserOrganisations();
    this.assessmentTemplate = {
    };
  }

  $onInit() {
  }

  goBack() {
    this.$state.go('assessment-templates');
  }

  onManagingOrganisation(selectedOrg) {
    this.assessmentTemplate.managingOrganisation = selectedOrg;
  }

  create() {
    this.AssessmentService.saveAssessmentTemplate(this.assessmentTemplate)
      .then(resp => {
        this.$state.go('assessment-template-edit', {
          id: resp.data.id,
          assessmentTemplate: resp.data
        });
      })
      .catch((err) => {
        let errMessage = (err.data || {}).description || 'Failed to save';
        this.ConfirmationDialog.warn(errMessage);
      });
  }

}

NewAssessmentTemplateCtrl.$inject = ['$state', 'AssessmentService', 'UserService', 'ConfirmationDialog'];

angular.module('GLA')
  .component('newAssessmentTemplate', {
    templateUrl: 'scripts/pages/assessment-templates/newAssessmentTemplate.html',
    controller: NewAssessmentTemplateCtrl
  });
