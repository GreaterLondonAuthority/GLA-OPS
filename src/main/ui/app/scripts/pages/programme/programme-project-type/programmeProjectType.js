/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import '../programme-assessment-template-modal/programmeAssessmentTemplateModal.js';

class ProgrammeProjectTypeCtrl {
  constructor(UserService, ProgrammeService, ConfirmationDialog, ProgrammeAssessmentTemplateModal) {
    this.UserService = UserService;
    this.ProgrammeService = ProgrammeService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.ProgrammeAssessmentTemplateModal = ProgrammeAssessmentTemplateModal;
  }

  $onInit(){
    this.canEditCeCode = this.UserService.hasPermission('prog.manage.ce.code');
    this.wbsCodeTypes = this.ProgrammeService.getWbsCodeTypes();
    this.statuses = ['Active', 'Inactive'];
    this.labels = this.ProgrammeService.labels().projectType;
  }

  $onChanges(changes) {
    if (changes.assessmentTemplates && changes.assessmentTemplates.currentValue &&
      (changes.assessmentTemplates.currentValue != changes.assessmentTemplates.previousValue)) {
      this.updateAvailableAssessmentTemplates();
      if (!this.programme.id) {
        this.projectType.assessmentTemplates = [];
      }
    }
  }

  getWbsCodeType(wbsCodeType){
    return _.find(this.wbsCodeTypes, {key: wbsCodeType})
  }

  onDefaultWbsSelect(template){
    if(template.isNew){
      return;
    }

    let isApproved = false;
    const modal = this.ConfirmationDialog.show({
      title: 'Set Payment Default',
      message: 'Confirm that by changing your payment default all your outgoing payment will now go through the selected WBS code excluding any interest applied. If no payment default is provided you will not able to approve any further payments',
      approveText: 'SET PAYMENT DEFAULT',
      dismissText: 'CANCEL'
    });

    modal.result.then(() => {
      isApproved = true;
      template.previousDefaultWbsCodeType = template.defaultWbsCodeType;
    });

    modal.closed.then(() => {
      if(!isApproved){
        template.defaultWbsCodeType = template.previousDefaultWbsCodeType;
      }
    });
  }


  onStatusSelect(template){
    if(template.isNew){
      return;
    }

    let msgActive = `Changing the status to active means the template will be available for new projects. <div class="mtop20">Change to active?</div>`;
    let msgInactive = `Changing the status to inactive means the template will not be available for new projects.<p class="mtop20">Change to inactive?</p>`;
    console.log('template', template);
    let isApproved = false;
    const modal = this.ConfirmationDialog.show({
      message: `<div class="text-left">${template.status === 'Active' ? msgActive : msgInactive}</div>`,
      approveText: 'CONFIRM',
      dismissText: 'CANCEL'
    });

    modal.result.then(() => {
      isApproved = true;
      template.previousStatus = template.status;
    });
    modal.closed.then(() => {
      if(!isApproved){
        template.status = template.previousStatus;
      }
    });
  }

  onAssessmentTemplate(template, assessment) {
    if (assessment) {
      //TODO Why don't we bind to property directly in html
      template.assessmentTemplate = {
        id: assessment.id,
        name: assessment.name
      }
    } else {
      template.assessmentTemplate = null;
    }
  }

  /**
   * @param at {assessmentTemplate, allowedRoles}
   */
  showAssessmentTemplateModal(at, readOnly) {
    let modal = this.ProgrammeAssessmentTemplateModal.show(at, this.availableAssessmentTemplates, this.glaRoles, !!readOnly);
    modal.result.then((at) => {
      this.projectType.assessmentTemplates = this.projectType.assessmentTemplates || [];
      if (!at.id) {
        this.projectType.assessmentTemplates.push(at);
      }
      this.updateAvailableAssessmentTemplates();
    });
  }

  usedByRolesText(at){
    if(at.allowedRoles.length == this.glaRoles.length) {
      return 'Used by all roles';
    }
    return `Used by ${at.allowedRoles.length} ${at.allowedRoles.length === 1? 'role' : 'roles'}`;
  }


  updateAvailableAssessmentTemplates() {
    this.availableAssessmentTemplates = (this.assessmentTemplates || []).filter(at => {
      return !((this.projectType.assessmentTemplates || []).some(existing => existing.assessmentTemplate.id === at.id));
    });
  }

  removeAssessmentTemplate(assessmentTemplate){
    _.remove(this.projectType.assessmentTemplates, assessmentTemplate);
    this.updateAvailableAssessmentTemplates();
  }

  isAssessmentTemplateEditableOrDeletable(at){
    if (this.readOnly) {
      return false;
    }
    else if (this.allowChangeInUseAssessmentTemplate) { // GLA-27409
      return true;
    }
    else {
      return (!at.usedInAssessment)
        // TODO not sure why checking the IDs is required here
        && ((this.programme.managingOrganisation ? this.programme.managingOrganisation.id : this.programme.managingOrganisationId) || !at.id);
    }
  }

}

ProgrammeProjectTypeCtrl.$inject = ['UserService', 'ProgrammeService', 'ConfirmationDialog', 'ProgrammeAssessmentTemplateModal'];

angular.module('GLA')
  .component('programmeProjectType', {
    controller: ProgrammeProjectTypeCtrl,
    bindings: {
      projectType: '<',
      readOnly: '<',
      projectsCount: '<',
      assessmentTemplates: '<',
      programme: '<',
      glaRoles: '<',
      onDelete: '&',
      allowChangeInUseAssessmentTemplate: '<'
    },
    templateUrl: 'scripts/pages/programme/programme-project-type/programmeProjectType.html'
  });
