/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class EditAssessmentTemplateCtrl {
  constructor($state, AssessmentService, UserService, ConfirmationDialog, AddScoreModal, AddSectionModal, AddCriteriaModal, ErrorService) {
    this.$state = $state;
    this.AssessmentService = AssessmentService;
    this.UserService = UserService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.AddScoreModal = AddScoreModal;
    this.AddSectionModal = AddSectionModal;
    this.AddCriteriaModal = AddCriteriaModal;
    this.ErrorService = ErrorService;
  }

  $onInit() {
    this.managingOrganisations = this.UserService.currentUserOrganisations();

    this.blockSessionStorage = this.blockSessionStorage || {};
    this.blockSessionStorage.assessmentTemplateSectionsTableState = this.blockSessionStorage.assessmentTemplateSectionsTableState || [];

    this.editable = true;
    this.readOnly = false;


    this.initManagingOrg();
    // this is always set back to Draft/false then editing so no need to check
    // this.isReadyForUse = assessmentTemplate.status === 'ReadyForUse';
    this.isReadyForUse = false;
    this.save();
  }

  initManagingOrg() {
    this.assessmentTemplate.managingOrganisation = {
      id: this.assessmentTemplate.managingOrganisationId,
      name: this.assessmentTemplate.managingOrganisationName
    };
  }

  onManagingOrganisation(selectedOrg) {
    this.assessmentTemplate.managingOrganisation = selectedOrg;
    this.save();
  }

  showAddScoreModal() {
    const modal = this.AddScoreModal.show(this.assessmentTemplate);
    modal.result.then((scoreEntry) => {
      if (scoreEntry) {
        this.assessmentTemplate.scores.push(scoreEntry);
        this.save();
      }
    });
  }

  showAddSectionModal(section) {
    const modal = this.AddSectionModal.show(section, this.assessmentTemplate.includeWeight);
    modal.result.then((section) => {
      if (section && !section.id) {
        this.assessmentTemplate.sections.push(section);
      }
      this.save();
    });
  }

  showAddCriteriaModal(section, criteria) {
    const modal = this.AddCriteriaModal.show(this.assessmentTemplate, criteria);
    modal.result.then((result) => {
      if (result && !result.id) {
        section.criteriaList.push(result);
      }
      this.save();
    });
  }

  deleteScoreEntry(id) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the row?');
    modal.result.then(() => {
      this.assessmentTemplate.scores = _.reject(this.assessmentTemplate.scores, function(d){ return d.id === id; });
      this.save();
    });
  }

  deleteSection(id) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the section?');
    modal.result.then(() => {
      this.assessmentTemplate.sections = _.reject(this.assessmentTemplate.sections, function(d){ return d.id === id; });
      this.save();
    });
  }

  deleteCriteria(section, criteriaId) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this criteria and related options?');
    modal.result.then(() => {
      section.criteriaList = _.reject(section.criteriaList, function(d){ return d.id === criteriaId; });
      this.save();
    });
  }

  onBack() {
    this.stopEditing();
  }

  stopEditing() {
    this.save();
    this.$state.go('assessment-template', {
      id: this.assessmentTemplate.id,
      assessmentTemplate: this.assessmentTemplate
    });
  }

  save() {

    this.assessmentTemplate.status = this.isReadyForUse ? 'ReadyForUse' : 'Draft';

    this.AssessmentService.saveAssessmentTemplate(this.assessmentTemplate).then((resp) => {
      this.assessmentTemplate = resp.data;
      this.initManagingOrg();
    }).catch(this.ErrorService.apiValidationHandler());
  }

}

EditAssessmentTemplateCtrl.$inject = ['$state', 'AssessmentService', 'UserService', 'ConfirmationDialog', 'AddScoreModal', 'AddSectionModal', 'AddCriteriaModal', 'ErrorService'];

angular.module('GLA')
  .component('editAssessmentTemplate', {
    templateUrl: 'scripts/pages/assessment-templates/assessmentTemplate.html',
    bindings: {
      assessmentTemplate: '<'
    },
    controller: EditAssessmentTemplateCtrl
  });
