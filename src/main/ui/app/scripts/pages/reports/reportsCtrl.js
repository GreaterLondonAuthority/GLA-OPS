/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ReportsCtrl($timeout, ProgrammeService, FeatureToggleService, $q, config, programmes, reports) {
  this.DOWNLOAD = 'DOWNLOAD';
  this.GENERATING = 'GENERATING';
  this.linkLabel = this.DOWNLOAD;
  this.allProgrammes = programmes || [];

  this.boroughReportLinkForcedToDisabled = false;
  this.enabled = {};
  this.enabled.programmeSelector = false;
  this.enabled.boroughReportLink = false;

  this.reportTypes = [];

  this.milestones = [
    {
      id: -1,
      description: 'Current / Latest'
    },
    {
      id: 3005,
      description: 'Acquisition'
    },
    {
      id: 3003,
      description: 'Start on Site'
    },
    {
      id: 3004,
      description: 'Completion'
    }
  ];


  $q.all({
    programmeReportEnabled: FeatureToggleService.isFeatureEnabled('outputCSV').then(rsp => rsp.data),
    affordableHousingReportEnabled: FeatureToggleService.isFeatureEnabled('AffordableHousingReport').then(rsp => rsp.data)

  }).then(data => {
    this.reportTypes = [{
      name: 'BOROUGH_REPORT',
      description: 'Borough report',
      url: (programmeId) => `${config.basePath}/report/borough/csv?programme=${programmeId}`,
      extraParams: true
    }];

    if (data.programmeReportEnabled) {
      this.reportTypes.push({
        name: 'PROGRAMME_REPORT',
        description: 'Programme report',
        url: (programmeId) => `${config.basePath}/programmes/${programmeId}/csvexport`
      });
    }


    const REPORT_TYPE = 'AffordableHousing';
    if(data.affordableHousingReportEnabled && this.isReportSupported(this.allProgrammes, REPORT_TYPE)){
      this.reportTypes.push({
        name: 'AFFORDABLE_HOUSING',
        description: '(In development) Affordable Housing',
        url: (programmeId) => `${config.basePath}/report/affordableHousing/csv?programme=${programmeId}&milestoneId=${this.selectedMilestone.id || -1}`,
        filterProgramme: REPORT_TYPE,
        showMilestonesDropdown: true,
        extraParams: true
      });
    }

    (reports || []).forEach(report => {
      this.reportTypes.push({
        name: report.name,
        description: report.name,
        url: (programmeId) => `${config.basePath}/report/csv/${report.name}?programme=${programmeId}`,
        extraParams: true
      });
    });

    this.selectedReportType = {
      name: null,
      description: 'SELECT REPORT TYPE'
    };
    this.resetExtraParameters();
  });

  this.resetExtraParameters = function(){
    this.projectTypeParameter = '';
    this.boroughParameter = '';
    this.projectStatusParameter = '';
  };


  this.selectReportType = function (selectedReportType) {
    this.selectedReportType = selectedReportType;
    this.resetExtraParameters();
    this.updateProgrammes(selectedReportType);
    this.loadBoroughReportFilters();
    this.updateEnableElements();
    this.updateReportUrl();
  };


  //Programmes
  this.updateProgrammes = function (selectedReportType) {
    this.programmes = [];
    //TODO why do we do this? should be a placeholder instead
    this.selectedProgramme = {name: null, description: 'SELECT A PROGRAMME'};
    this.selectedMilestone = {name: null, description: 'SELECT A MILESTONE'};

    if (this.selectedReportType.name) {
        this.enabled.programmeSelector = true;
        this.programmeId = null;
        this.allProgrammes.forEach((programme)=>{
          this.programmes.push({
            name: programme.id,
            description: programme.name,
            supportedReports: programme.supportedReports
          });
        });
        this.filterProgrammes(selectedReportType);
    } else {
      this.filteredProgrammes = [];
      this.enabled.programmeSelector = false;
    }
  };

  this.filterProgrammes = (selectedReportType) => {
    this.filteredProgrammes = [];
    _.forEach(this.programmes, (programme)=>{
      if(selectedReportType.filterProgramme){
        if(programme.supportedReports && programme.supportedReports.indexOf(selectedReportType.filterProgramme) !== -1){
          this.filteredProgrammes.push(programme);
        }
      } else {
        this.filteredProgrammes.push(programme);
      }
    });
  };

  this.isReportSupported = (prgrammes, reportType) => {
    return programmes.some(programme => {
      return (programme.supportedReports || []).indexOf(reportType) !== -1
    });
  };

  this.selectProgramme = function (programme) {
    this.resetExtraParameters();
    this.programmeId = programme.name;
    this.updateEnableElements();
    this.loadBoroughReportFilters();
    this.updateReportUrl();
  };

  this.loadBoroughReportFilters = function () {
    if (this.programmeId && this.selectedReportType.extraParams) {
      //Project type
      ProgrammeService.getTemplateByProgramme(this.programmeId).then(resp => {
        this.projectTypes = [];
        this.projectTypes.push({name: null, description: 'ALL'})
        this.selectedProjectType = this.projectTypes[0]
        if (resp) {
          for (var i = 0; i < resp.length; i++) {
            this.projectTypes.push({name: resp[i].id, description: resp[i].name})
          }
        }
      });

      //Project Status
      ProgrammeService.getStatusesByProgramme(this.programmeId).then(resp => {
        this.projectStatus = []
        this.projectStatus.push({name: null, description: 'ALL'})
        this.selectedProjectStatus = this.projectStatus[0];
        if (resp.data) {
          for (var i = 0; i < resp.data.length; i++) {
            this.projectStatus.push({name: resp.data[i], description: resp.data[i]})
          }
        }
      });


      //Borough
      ProgrammeService.getBoroughsByProgramme(this.programmeId).then(resp => {
        this.boroughs = []
        this.boroughs.push({name: null, description: 'ALL'});
        this.selectedBorough = this.boroughs[0]
        if (resp.data) {
          for (var i = 0; i < resp.data.length; i++) {
            this.boroughs.push({name: resp.data[i], description: resp.data[i]})
          }
        }
      });


    } else {
      this.disableFilters();
    }
  }

  this.disableFilters = function () {
    this.projectTypes = [];
    this.projectTypes.push({name: null, description: 'ALL'});
    this.selectedProjectType = this.projectTypes[0];

    this.projectStatus = [];
    this.projectStatus.push({name: null, description: 'ALL'});
    this.selectedProjectStatus = this.projectStatus[0];
    this.projectStatusParameter = '';

    this.boroughs = [];
    this.boroughs.push({name: null, description: 'ALL'});
    this.selectedBorough = this.boroughs[0];

    this.enabled.projectType = false;
    this.enabled.borough = false;
    this.enabled.projectStatus = false;
    this.resetExtraParameters();
  };

  this.selectProjectType = function (projectType) {
    this.projectTypeParameter = projectType.name != null
      ? '&projectType=' + projectType.name
      : '';
    this.updateReportUrl();
  };

  this.selectBorough = function (borough) {
    this.boroughParameter = borough.name != null ? '&borough=' + borough.name : '';
    this.updateReportUrl();
  };

  this.selectProjectStatus = function (projectStatus) {
    this.projectStatusParameter = projectStatus.name != null
      ? '&status=' + projectStatus.name
      : '';
    this.updateReportUrl();
  };


  this.updateEnableElements = function () {
    if (!this.selectedReportType.name || !this.programmeId) {
      this.disableFilters();
      this.enabled.boroughReportLink = false
    } else if (this.programmeId) {
      this.enableReportLink();
      if (this.selectedReportType.extraParams) {
        this.enabled.projectType = true;
        this.enabled.borough = true;
        this.enabled.projectStatus = true;
      } else {
        this.disableFilters();
      }
    } else {
      this.disableFilters();
    }

    if(this.selectedReportType.name === 'AFFORDABLE_HOUSING' && !this.selectedMilestone.id){
      this.enabled.boroughReportLink = false
    }
  };

  this.onFormChange = function(){
    this.updateEnableElements();
    this.updateReportUrl();
  };

  //TODO shouldn't we use service instead of URL
  this.updateReportUrl = function () {
    if (this.selectedReportType.url && this.programmeId) {
      this.reportUrl = this.selectedReportType.url(this.programmeId);
      this.reportUrl += this.projectTypeParameter;
      this.reportUrl += this.boroughParameter;
      this.reportUrl += this.projectStatusParameter;
    } else {
      this.reportUrl = '';
    }

    // console.log('url', this.reportUrl);
  };

  //todo temporally fix to simulate waiting loading
  this.generatingLink = function () {
    this.linkLabel = this.GENERATING;
    this.boroughReportLinkForcedToDisabled = true;
    this.enabled.boroughReportLink = false;
    const _this = this;
    $timeout(function () {
      _this.boroughReportLinkForcedToDisabled = false;
      _this.enableReportLink();
    }, 5000)
  };

  this.enableReportLink = function () {
    if (!this.boroughReportLinkForcedToDisabled && this.programmeId) {
      this.linkLabel = this.DOWNLOAD;
      this.enabled.boroughReportLink = true
    }
  };


  this.enable = function () {
    this.selectedReportType = {name: null, description: 'SELECT REPORT TYPE'};
    //programme
    this.selectedProgramme = {name: null, description: 'SELECT A PROGRAMME'};
    this.enabled.programmeSelector = false;
    this.programmeId = null;
    this.updateEnableElements();
  }
}

ReportsCtrl.$inject = ['$timeout', 'ProgrammeService', 'FeatureToggleService', '$q', 'config', 'programmes', 'reports'];

angular.module('GLA')
  .controller('ReportsCtrl', ReportsCtrl);
