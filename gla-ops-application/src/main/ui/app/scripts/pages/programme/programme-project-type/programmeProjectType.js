/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import '../programme-assessment-template-modal/programmeAssessmentTemplateModal.js';
import '../give-organisation-access-modal/giveOrganisationAccessModal.js';
import '../remove-organisation-access-modal/removeOrganisationAccessModal.js';

class ProgrammeProjectTypeCtrl {
  constructor(UserService, ProgrammeService, ConfirmationDialog, ProgrammeAssessmentTemplateModal, GiveOrganisationAccessModal, RemoveOrganisationAccessModal) {
    this.UserService = UserService;
    this.ProgrammeService = ProgrammeService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.ProgrammeAssessmentTemplateModal = ProgrammeAssessmentTemplateModal;
    this.GiveOrganisationAccessModal = GiveOrganisationAccessModal;
    this.RemoveOrganisationAccessModal = RemoveOrganisationAccessModal;
  }

  $onInit(){
    this.canEditCeCode = this.UserService.hasPermission('prog.manage.ce.code');
    this.wbsCodeTypes = this.ProgrammeService.getWbsCodeTypes();
    this.statuses = ['Active', 'Inactive'];
    this.labels = this.ProgrammeService.labels().projectType;
    this.templateAccessList = this.getTemplateDefaultAccess(this.projectType.id.templateId);
    this.managingOrgAccess = _.filter(this.templateAccessList, (access)=>{ return this.programme.managingOrganisationId == access.organisationId }).length >= 1;
    this.getTeams(this.projectType.id.templateId);
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

  delete(orgId, name){
    let templateName = this.projectType.templateName;
    let modal = this.RemoveOrganisationAccessModal.show(orgId, name, templateName);
    modal.result.then(() => {
      this.ProgrammeService.removeOrganisationAccess(this.projectType.id.programmeId, this.projectType.id.templateId, orgId).then(resp => this.refreshOrgAccess());
      this.organisationsWithAccess = this.organisationsWithAccess.remove(access => access.organisationId !== orgId)
    });
  }

  onManagingOrgAccessChange(granted) {
    if (granted) {
      this.ProgrammeService.grantOrganisationAccess(this.projectType.id.programmeId, this.projectType.id.templateId, this.programme.managingOrganisationId).then(this.refreshOrgAccess());
    } else {
      this.ProgrammeService.removeOrganisationAccess(this.projectType.id.programmeId, this.projectType.id.templateId, this.programme.managingOrganisationId).then(this.refreshOrgAccess());
    }
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
    }, () => {
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
    let isApproved = false;
    const modal = this.ConfirmationDialog.show({
      message: `<div class="text-left">${template.status === 'Active' ? msgActive : msgInactive}</div>`,
      approveText: 'CONFIRM',
      dismissText: 'CANCEL'
    });

    modal.result.then(() => {
      isApproved = true;
      template.previousStatus = template.status;
    }, () => {
      if (!isApproved) {
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
    let modal = this.ProgrammeAssessmentTemplateModal.show(at, this.availableAssessmentTemplates, this.glaRoles, !!readOnly, !!this.readOnly);
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

  getTeams(templateId) {
    let teamsIds = (this.teams || []).map(q => + q.id);
    let teamsAccess = _.filter(this.templateAccessList, (access)=>{ return teamsIds.indexOf(access.organisationId) !== -1});
    let teamsAccessIds = teamsAccess.map(a => + a.organisationId).filter(id => teamsIds.indexOf(id) !== -1);

    (this.teams || []).forEach(team => {
      if(teamsAccessIds.indexOf(team.id) !== -1) {
        team.hasDefaultAccess = true
      } else {
        team.hasDefaultAccess = false
      }
    });

    return this.teams;
  }

  onTeamDefaultAccessChange(team){
    if(team.hasDefaultAccess) {
      this.ProgrammeService.grantOrganisationAccess(this.projectType.id.programmeId, this.projectType.id.templateId, team.id).then(resp => this.refreshOrgAccess());
    } else {
      this.ProgrammeService.removeOrganisationAccess(this.projectType.id.programmeId, this.projectType.id.templateId, team.id).then(resp => this.refreshOrgAccess());
    }
  }

  refreshOrgAccess() {
    this.ProgrammeService.getDefaultAccess(this.programme.id).then(rsp => {
        this.organisationsWithAccess = rsp.data;
        this.templateAccessList = this.getTemplateDefaultAccess(this.projectType.id.templateId);
      }
    );
  }

  showGiveOrganisationAccessModal(organisation) {
    let modal = this.GiveOrganisationAccessModal.show(organisation, this.templateAccessList, this.projectType.templateName, this.programme.managingOrganisationId);
    modal.result.then((organisation) => {
      this.ProgrammeService.grantOrganisationAccess(this.projectType.id.programmeId, this.projectType.id.templateId, organisation.id);
      this.addOrganisationDefaultAccess(organisation)
      this.templateAccessList = this.getTemplateDefaultAccess(this.projectType.id.templateId);
      this.getOtherOrganisationsDefaultAccess(this.projectType.id.templateId);
    });
  }

  getTemplateId(name) {
    return $ctrl.projectType.templateName.toLowerCase().replace(/ /g, '-');
  }

  addOrganisationDefaultAccess(organisation) {
    this.organisationsWithAccess.push({
      id: {
        organisationId: organisation.id,
        templateId: this.projectType.id.templateId,
        programmeId: this.projectType.id.programmeId
      },
      programmeId: this.projectType.id.programmeId,
      templateId: this.projectType.id.templateId,
      organisationId: organisation.id,
      organisationName: organisation.name,
      managingOrganisationName: organisation.managingOrganisationName,
      relationshipType: 'ASSOCIATED'
    });
  }

  getTemplateDefaultAccess(templateId) {
    return _.filter(this.organisationsWithAccess, (access)=>{return access.templateId === templateId;});
  }

  getOtherOrganisationsDefaultAccess(templateId) {
    let teamsIds = (this.teams || []).map(q => + q.id);
    return _.filter(this.templateAccessList, (access)=>{ return teamsIds.indexOf(access.organisationId) === -1});
  }

}

ProgrammeProjectTypeCtrl.$inject = ['UserService', 'ProgrammeService', 'ConfirmationDialog', 'ProgrammeAssessmentTemplateModal', 'GiveOrganisationAccessModal', 'RemoveOrganisationAccessModal'];

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
      allowChangeInUseAssessmentTemplate: '<',
      organisationsWithAccess: '<',
      teams: '<'
    },
    templateUrl: 'scripts/pages/programme/programme-project-type/programmeProjectType.html'
  });
