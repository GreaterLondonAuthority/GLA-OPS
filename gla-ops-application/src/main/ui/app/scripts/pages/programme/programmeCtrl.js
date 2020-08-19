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
  constructor($state, $log, UserService, ProgrammeService, AssessmentService, ProgrammeTemplateModal, $rootScope, ConfirmationDialog, $animate, $timeout, TemplateService, fYearFilter) {
    this.$state = $state;
    this.$log = $log;
    this.UserService = UserService;
    this.ProgrammeService = ProgrammeService;
    this.AssessmentService = AssessmentService;
    this.ProgrammeTemplateModal = ProgrammeTemplateModal;
    this.$rootScope = $rootScope;
    this.ConfirmationDialog = ConfirmationDialog;
    this.$animate = $animate;
    this.$timeout = $timeout;
    this.TemplateService = TemplateService;
    this.fyearFilter = fYearFilter;
  };

  $onInit() {
    this.user = this.UserService.currentUser();
    this.newTemplateWbsDefault = null;


    // static
    this.wbsCodeTypes = this.ProgrammeService.getWbsCodeTypes();
    this.UserService.hasPermission('org.edit.contract');

    // this.editable = true;
    this.editMode = false;
    this.statuses = ['', 'Active', 'Archived', 'Abandoned'];
    this.yearTypes = ['Calendar', 'Academic', 'Financial'];
    this.yearHintText = ''
    this.yearLength =0
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
    this.setYearRelatedData();
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
    this.programme.templatesByProgramme = this.programme.templatesByProgramme || [];
    this.programme.templatesByProgramme.forEach(p => {
      p.teams = angular.copy(this.teams);
    });
  };

  setYearRelatedData() {
    if (this.programme.yearType) {
      if (this.programme.yearType === 'Calendar') {
        this.yearHintText = 'YYYY'
        this.yearLength = 4
       this.formattedStartYear = this.programme.startYear
       this.formattedEndYear = this.programme.endYear
      } else {
        this.yearHintText = 'YYYY/YY'
        this.yearLength = 7
       this.formattedStartYear = this.getFormattedYear(this.programme.startYear)
       this.formattedEndYear = this.getFormattedYear(this.programme.endYear)
      }
    }
  }

  getFormattedYear(year){
    return year? this.fyearFilter(year):year;
  }


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
          temp[projectCount.entityId] = projectCount.entityCount;
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
    };
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
    if(this.isSaving || this.hasFormErrors()){return;}
    this.isSaving = true;
    this.assignYearsValue()
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

  assignYearsValue(){
        this.programme.startYear = this.getBaseYear(this.formattedStartYear)
        this.programme.endYear = this.getBaseYear(this.formattedEndYear)
  }

  getBaseYear(year){
    let returnValue = year;
    if (year) {
      returnValue = year.toString().replace(/\/.*/, '');
    }
    return returnValue
  }

  onYearTypeChange(){
    this.setYearRelatedData()
    this.onStartYearChange()
    this.onEndYearChange()
  }

  onStartYearChange(){
    this.startYearInvalidMsg =null;
     if (!this.formattedStartYear) {
       return
     }
    let startYear = this.getBaseYear(this.formattedStartYear)
    if (this.invalidYearFormat(this.formattedStartYear)){
      this.startYearInvalidMsg = this.programme.yearType === 'Calendar'?
        'Please specify Start year in YYYY format':
        'Please specify Start year in YYYY/YY format'
    }
  }

  onEndYearChange(){
    this.endYearInvalidMsg = null;
     if (!this.formattedEndYear) {
       return
     }
    let startYear = this.getBaseYear(this.formattedStartYear)
    let endYear = this.getBaseYear(this.formattedEndYear)
    if (!startYear){
      this.endYearInvalidMsg ='Specify start year first'
    } else if (this.invalidYearFormat(this.formattedEndYear)){
      this.endYearInvalidMsg = this.programme.yearType === 'Calendar'?
        'Please specify End year in YYYY format':
        'Please specify End year in YYYY/YY format'
    }
    else if (startYear > endYear){
      this.endYearInvalidMsg ='End year can not be less than Start year'
    }
  }

  invalidYearFormat(year){
    return this.programme.yearType === 'Calendar'? year.length < 4 || !year.match('[0-9]{4}'):
                                    (year.length < 7 || !year.match('[0-9]{4}/[0-9]{2}'))
  }

  hasFormErrors(){
    return this.startYearInvalidMsg || this.endYearInvalidMsg;
  }
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
    let cache = this.TemplateService.getCache();
    if(cache){
      cache.removeAll();
    }
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

ProgrammeCtrl.$inject = ['$state', '$log', 'UserService', 'ProgrammeService', 'AssessmentService', 'ProgrammeTemplateModal', '$rootScope', 'ConfirmationDialog', '$animate', '$timeout', 'TemplateService', 'fYearFilter'];

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
      teams: '<',
      organisationsWithAccess: '<'
    },
    templateUrl: 'scripts/pages/programme/programme.html'
  });
