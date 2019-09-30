/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

//TODO change path
import '../programme/programme-template-modal/programmeTemplateModal';
import DataUtil from '../../util/DateUtil';


class ProgrammeCtrl {
  constructor($state, $log, UserService, ProgrammeService, AssessmentService, ProgrammeTemplateModal, $rootScope, ConfirmationDialog, $animate, $timeout) {
    this.user = UserService.currentUser();
    this.UserService = UserService;
    this.ProgrammeService = ProgrammeService;
    this.AssessmentService = AssessmentService;
    this.ProgrammeTemplateModal = ProgrammeTemplateModal;
    this.ConfirmationDialog = ConfirmationDialog;
    this.$animate = $animate;
    this.$timeout = $timeout;
    this.$rootScope = $rootScope;
    this.$state = $state;
    this.$log = $log;
  };

  $onInit() {
    this.newTemplateWbsDefault = null;


    // static
    this.wbsCodeTypes = this.ProgrammeService.getWbsCodeTypes();
    this.UserService.hasPermission('org.edit.contract');

    // this.editable = true;
    this.editMode = false;
    this.statuses = ['Active', 'Archived', 'Abandoned'];
    this.newTemplateWbsCode = null;
    this.programme = this.programme || {
      name: undefined,
      templates: [],
      enabled: false,
      restricted: false
    };
    this.templatesList = this.templatesList || [];

    this.submitenabled = false;
    this.loading = true;

    // this.templates = [];
    this.selectedTemplate = null;
    // this.restricted = false;

    this.projectsCount = {};

    this.isSaving = false;
    this.managingOrganisations = this.UserService.currentUserOrganisations();
    this.templateIdToProgrammeTemplate = (this.programme.templatesByProgramme || []).reduce((templateIdToProgrammeTemplate, templateProgramme) => {
      templateIdToProgrammeTemplate[templateProgramme.id.templateId] = templateProgramme;
      return templateIdToProgrammeTemplate;
    }, {});

    this.programme.templates.forEach(t => t.usedInAssessment = this.templateIdToProgrammeTemplate[t.id].usedInAssessment);
    this.availableTemplates = this.getAvailableTemplates(this.templatesList, this.programme);


    this.templateIdToTemplate = (this.templatesList || []).reduce((templateIdToTemplate, template) => {
      templateIdToTemplate[template.id] = template;
      return templateIdToTemplate;
    }, {});

    this.setStateModelForProgrammeTemplates(this.programme);
    this.programme.inAssessment = !!this.programme.inAssessment;

    this.programme.financialYearLabel = DataUtil.toFinancialYearString(this.programme.financialYear || DataUtil.getFinancialYear2(moment()));

    this.title = this.newProgrammeMode? 'Create a new programme' : this.programme.name;
    this.showExpandAll = false;
    this.labels = this.ProgrammeService.labels().programmeInfo;

    if(this.newProgrammeMode){
      this.readOnly = false;
      this.programme.status = 'Active';

      if (this.managingOrganisations.length === 1) {
        let managingOrg = this.managingOrganisations[0];
        this.onManagingOrganisation(managingOrg);
        this.programme.managingOrganisationName = managingOrg.name;
        this.programme.managingOrganisationId = managingOrg.id;
      }
    } else {
      this.readOnly = true;
    }
    if(!this.newProgrammeMode){
      this.processProgramme(this.programme);
    }


    this.$animate.enabled(true);
  };

  getTemplatesToggleName(){
    return this.showExpandAll? 'Expand all project types' : 'Collapse all project types';
  }

  collapseTemplates(){
    this.showExpandAll = !this.showExpandAll;
    (this.programme.templatesByProgramme || {}).forEach(template => {
       template.collapsed = this.showExpandAll;
    })
  }

  onCollapseChange(){
    let hasAnySectionOpen = (this.programme.templatesByProgramme || {}).some(template => !template.collapsed);
    this.showExpandAll = !hasAnySectionOpen;
  }

  onManagingOrganisation(selectedOrg) {
    this.programme.managingOrganisation = selectedOrg;
    this.filterReadyForUseAssesmentTemplates();
  }

  canEditProgram() {
    this.editable = this.UserService.hasPermission('prog.manage');
    return this.editable;
  };

  getProgrammeNumberOfProjects() {
    this.ProgrammeService.getProgrammeNumberOfProjects(this.$state.params.programmeId)
      .then(resp => {
        let temp = {};
        let totalProjectCount = 0;
        _.forEach(resp.data, projectCount => {
          temp[projectCount.entityID] = projectCount.entityCount;
          totalProjectCount += projectCount.entityCount;
        });
        this.totalProjectCount = totalProjectCount;
        this.projectsCount = temp;
      });
  };



  /******************
  VIEW PROGRAMME CTRL
  ******************/

  processProgramme(programme){
    // this.programme.templatesByProgramme = _.map(programme.templatesByProgramme, (template)=>{
    programme.templatesByProgramme = _.sortBy(programme.templatesByProgramme, (template)=>{
      template.attrId = template.templateName.split(' ').join('-')+'-wbs-code';
      // return template;
      return template.templateName;
    });

    this.managingOrganisationModel = {
      id: programme.managingOrganisationId,
      name: programme.managingOrganisationName
    }
    if(this.canEditProgram()){
      this.getProgrammeNumberOfProjects();
    }
    return programme;
  };


  onBack() {
    this.$state.go('programmes');
  };


  /**
   * Add template to selected list handler
   */
  onTemplateAdded(template) {
    this.programme.templatesByProgramme = this.programme.templatesByProgramme || [];
    this.$log.log(template);
    if (template) {
      if (!_.find(this.programme.templatesByProgramme, {id: template.id})) {

        this.programme.templatesByProgramme.unshift({
          //TODO do we need this?
          attrId: template.name.split(' ').join('-')+'-wbs-code',
          id: {
            templateId: template.id,
            programmeId: this.programme.id
          },
          programmeName: this.programme.name,
          templateName: template.name,
          capitalWbsCode: this.newTemplateWbsCapitalCode,
          revenueWbsCode: this.newTemplateWbsRevenueCode,
          ceCode: this.newTemplateCeCode,
          defaultWbsCodeType: this.newTemplateWbsDefault,
          status: 'Active',
          paymentsEnabled: true,
          isNew: true
        });
        this.selectedTemplate = null;
        this.newTemplateWbsCapitalCode = null;
        this.newTemplateWbsRevenueCode = null;
        this.newTemplateWbsDefault = null;
        this.newTemplateCeCode = null;
      }
    }
  };

  /**
   * Remove template from selected list handler
   */
  onTemplateRemoved(template) {
    this.$log.debug(template);
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this Project type (template)?');
    modal.result.then((selectedTemplate) => {
      this.programme.templatesByProgramme = this.programme.templatesByProgramme.filter(item => {
        return (item.id !== template.id);
      });

      this.availableTemplates = this.getAvailableTemplates(this.templatesList, this.programme);
    });

  };

  /**
   * Submit new project
   */
  submit() {
    if(this.isSaving){return;}
    this.isSaving = true;
    if(this.editMode){
      this.programme.templates = null;
      this.programme.grantTypes = null;
      return this.ProgrammeService.updateProgramme(this.programme, this.programme.id).then(()=>{
        this.$state.reload();
      })
    } else {
      return this.ProgrammeService.createProgramme(this.programme)
      .then(resp => {
        this.isSaving = false;
        if (!resp) return;
        this.$log.log(resp);
        this.$state.go('programme', {programmeId: resp.data.id});
      })
      .catch((error) => {
        this.isSaving = false;
        this.$log.error(error);
        this.loading = false;
      });
    }
  };

  isUsedInAssessment(templateId){
    return this.templateIdToProgrammeTemplate[templateId].isUsedInAssessment;
  }

  edit() {
    this.$rootScope.showGlobalLoadingMask = true;
    this.$animate.enabled(false);
    this.AssessmentService.getAssessmentTemplateSummaries().then(rsp => {
      this.assessmentTemplates = _.sortBy(rsp.data, 'name');
      this.filterReadyForUseAssesmentTemplates();
      this.readOnly = false;
      this.editMode = true;
      this.canEditProgram();
      this.$rootScope.showGlobalLoadingMask = false;
      this.$timeout(()=>{
        this.$animate.enabled(true);
      })
    });
  }

  filterReadyForUseAssesmentTemplates(){
    this.filteredAssessmentTemplates = _.filter(this.assessmentTemplates, {
      managingOrganisationId: this.programme.managingOrganisation ? this.programme.managingOrganisation.id : this.programme.managingOrganisationId,
      status: 'ReadyForUse'
    });
  }

  stopEditing(){
    this.submit();
  }


  addNewTemplate(){
    let modal = this.ProgrammeTemplateModal.show(this.availableTemplates);
    modal.result.then((selectedTemplate) => {
      this.onTemplateAdded(selectedTemplate);
      this.availableTemplates = this.getAvailableTemplates(this.templatesList, this.programme);
      this.setStateModelForProgrammeTemplates(this.programme);
    });
  }

  getAvailableTemplates(templatesList, programme){
    return _.filter(templatesList, t => {
      return !_.some(programme.templatesByProgramme, {id: {templateId: t.id}});
    });
  }


  setStateModelForProgrammeTemplates(programme){
    (programme.templatesByProgramme || []).forEach(t=>{
      let stateModel = this.templateIdToTemplate[t.id.templateId].stateModel;
      t.stateModelName = _.startCase(stateModel.name);
    })
  }


}

ProgrammeCtrl.$inject = ['$state', '$log', 'UserService', 'ProgrammeService', 'AssessmentService', 'ProgrammeTemplateModal', '$rootScope', 'ConfirmationDialog', '$animate', '$timeout'];

angular.module('GLA')
  .component('programmePage', {
    controller: ProgrammeCtrl,
    bindings: {
      newProgrammeMode: '<',
      programme: '<',
      assessmentTemplates: '<',
      templatesList: '<',
      glaRoles: '<',
      allowChangeInUseAssessmentTemplate: '<',
    },
    templateUrl: 'scripts/pages/programme/programme.html'
  });
