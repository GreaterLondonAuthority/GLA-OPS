/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ProgrammeCtrl.$inject = ['$state', '$log', 'UserService', 'ProgrammeService', 'FeatureToggleService'];

function ProgrammeCtrl($state, $log, UserService, ProgrammeService, FeatureToggleService) {
  this.user = UserService.currentUser();
  // this.csvDownloadEnabled = false;

  UserService.hasPermission('org.edit.contract');

  // this.editable = true;
  this.editMode = false;


  this.programme = {
    name: undefined,
    templates: [],
    enabled: false,
    restricted: false
  };
  this.templatesList = [];

  this.submitenabled = false;
  this.loading = true;

  // this.templates = [];
  this.selectedTemplate = null;
  // this.restricted = false;

  this.projectsCount = {};

  this.isSaving = false;
  this.managingOrganisations = UserService.currentUserOrganisations();

  this.onManagingOrganisation = (selectedOrg) => {
    this.programme.managingOrganisation = selectedOrg;
  }

  if($state.params.programmeId){
    this.newProgrammeMode = false;
    this.readOnly = true;
  } else {
    this.newProgrammeMode = true;
    this.readOnly = false;

    if(this.managingOrganisations.length === 1){
      let managingOrg = this.managingOrganisations[0];
      this.onManagingOrganisation(managingOrg);
      this.programme.managingOrganisationName= managingOrg.name;
      this.programme.managingOrganisationId= managingOrg.id;
    }
  }



  this.canEditProgram = () => {
    this.editable = UserService.hasPermission('prog.manage') && this.readOnly;
    return this.editable;
  };

  this.getProgrammeNumberOfProjects = () => {
    ProgrammeService.getProgrammeNumberOfProjects($state.params.programmeId)
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
  this.loadProgramme = () => {
    ProgrammeService.getProgramme($state.params.programmeId)
    .then(resp => {
      this.programme = resp.data;
      this.managingOrganisationModel = {
        id: this.programme.managingOrganisationId,
        name: this.programme.managingOrganisationName
      }
      if(this.canEditProgram()){
        this.getProgrammeNumberOfProjects();
      }
    });
  };

  // on view and edit only
  if(!this.newProgrammeMode){
    this.loadProgramme();
  }

  // FeatureToggleService.isFeatureEnabled('outputCSV')
  //   .then(resp => {
  //     this.csvDownloadEnabled = resp.data;
  //   });

  this.onBack = () => {
    $state.go('programmes');
  };

  this.updateEnabled = (enabled) => {
    ProgrammeService.updateEnabled($state.params.programmeId, enabled)
      .then(resp => {
        this.loadProgramme();
      });
  };

  /******************
  CREATE PROGRAMME CTRL
  ******************/

  ProgrammeService.getAllProjectTemplates()
    .then(resp => {
      this.loading = false;
      if (!resp) return;
      this.templatesList = _.orderBy(resp.data, 'name');
    })
    .catch(resp => {
      $log.error(resp);
      this.loading = false;
    });

  /**
   * Add template to selected list handler
   */
  this.onTemplateAdded = (template) => {

    $log.log(template);
    if (template) {
      if (!_.find(this.programme.templates, {id: template.id})) {
        this.programme.templates.push(template);
      }
    }
  };

  /**
   * Remove template from selected list handler
   */
  this.onTemplateRemoved = (template) => {
    $log.debug(template);
    this.programme.templates = this.programme.templates.filter(item => {
      return (item.id !== template.id);
    });
  };

  /**
   * Submit new project
   */
  this.submit = () => {
    if(this.isSaving){return;}
    this.isSaving = true;
    if(this.editMode){
      ProgrammeService.updateProgramme(this.programme, this.programme.id)
      .then(resp => {
        this.isSaving = false;
        if (!resp) return;
        $log.log(resp);
        $state.go('programmes');
      })
      .catch(() => {
        this.isSaving = false;
        $log.error(error);
        this.loading = false;
      });
    } else {

      var data = {
        name: this.programme.name,
        templates: this.programme.templates,
        enabled: this.programme.enabled,
        restricted: this.programme.restricted,
        wbsCode: this.programme.wbsCode,
        managingOrganisation: this.programme.managingOrganisation
      }

      ProgrammeService.createProgramme(data)
      .then(resp => {
        this.isSaving = false;
        if (!resp) return;
        $log.log(resp);
        $state.go('programmes');
      })
      .catch((error) => {
        this.isSaving = false;
        $log.error(error);
        this.loading = false;
      });
    }
  };

  this.edit = () => {
    this.readOnly = false;
    this.editMode = true;
    this.canEditProgram();
  }



}

angular.module('GLA')
  .controller('ProgrammeCtrl', ProgrammeCtrl);
