/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class EditAssessmentTemplateCtrl {
  constructor($state, AssessmentService, UserService, ConfirmationDialog, AddScoreModal, AddOutcomeModal, AddSectionModal, AddCriteriaModal, ErrorService, $timeout) {
    this.$state = $state;
    this.AssessmentService = AssessmentService;
    this.UserService = UserService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.AddScoreModal = AddScoreModal;
    this.AddOutcomeModal = AddOutcomeModal;
    this.AddSectionModal = AddSectionModal;
    this.AddCriteriaModal = AddCriteriaModal;
    this.ErrorService = ErrorService;
    this.$timeout = $timeout;
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
    this.filterAssessmentTemplatesByManagingOrg();
    this.save();
  }

  initManagingOrg() {
    this.assessmentTemplate.managingOrganisation = {
      id: this.assessmentTemplate.managingOrganisationId,
      name: this.assessmentTemplate.managingOrganisationName
    };
  }

  filterAssessmentTemplatesByManagingOrg(){
    this.filteredAssessmentTemplates = _.filter(this.assessmentTemplates, at => at.managingOrganisationId == this.assessmentTemplate.managingOrganisation.id && at.id != this.assessmentTemplate.id);
  }

  onManagingOrganisation(selectedOrg) {
    this.assessmentTemplate.managingOrganisation = selectedOrg;
    this.outcomeAssessment = null;
    this.assessmentTemplate.outcomeOfAssessmentTemplateId = null;
    this.filterAssessmentTemplatesByManagingOrg();
    this.save();
  }

  onOutcomeTemplateSelect(){
    this.save();
  }

  onSummaryChange(){
    //$timeout is needed to have model value changed
    this.$timeout(()=>{
      this.assessmentTemplate.outcomeOfAssessmentTemplateId = null;
      this.save();
    });
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
    const modal = this.AddSectionModal.show(section, this.assessmentTemplate.includeWeight, this.assessmentTemplate.sections);
    modal.result.then((section) => {
      if (section && !section.id) {
        this.assessmentTemplate.sections.push(section);
      }
      this.save();
    });
  }

  showAddOutcomeModal(outcome) {
    const modal = this.AddOutcomeModal.show(outcome);
    modal.result.then((outcome) => {
      if (outcome && !outcome.id) {
        this.assessmentTemplate.outcomes.push(outcome);
      }
      this.save();
    });
  }

  showAddCriteriaModal(section, criteria) {
    const modal = this.AddCriteriaModal.show(this.assessmentTemplate, angular.copy(criteria), section);
    modal.result.then((criteriaFromModal) => {
      if (criteriaFromModal && !criteriaFromModal.id) {
        section.criteriaList.push(criteriaFromModal);
      } else{
        _.merge(criteria, criteriaFromModal)
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

  deleteOutcomeEntry(id) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the row?');
    modal.result.then(() => {
      this.assessmentTemplate.outcomes = _.reject(this.assessmentTemplate.outcomes, function(d){ return d.id === id; });
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
      this.outcomeAssessment = _.find(this.assessmentTemplates, {id: this.assessmentTemplate.outcomeOfAssessmentTemplateId});
    }).catch(this.ErrorService.apiValidationHandler());
  }

}

EditAssessmentTemplateCtrl.$inject = ['$state', 'AssessmentService', 'UserService', 'ConfirmationDialog', 'AddScoreModal', 'AddOutcomeModal', 'AddSectionModal', 'AddCriteriaModal', 'ErrorService', '$timeout'];

angular.module('GLA')
  .component('editAssessmentTemplate', {
    templateUrl: 'scripts/pages/assessment-templates/assessmentTemplate.html',
    bindings: {
      assessmentTemplate: '<',
      assessmentTemplates: '<'
    },
    controller: EditAssessmentTemplateCtrl
  });
