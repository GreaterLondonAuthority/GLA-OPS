/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


//TODO use 'Select report type' placeholder instead of adding/removing option to the dropdown
class ReportsCtrl{
  constructor( ProgrammeService, FeatureToggleService, $q, config, ReportService, ConfirmationDialog, UserService, TemplateService, $state) {
    this.config = config;
    this.$q = $q;
    this.$state = $state;
    this.FeatureToggleService = FeatureToggleService;
    this.ProgrammeService = ProgrammeService;
    this.TemplateService = TemplateService;
    this.ReportService = ReportService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.UserService = UserService;
  }

  $onInit() {
    this.linkLabel = 'GENERATE';
    this.allProgrammes = this.programmes || [];
    // console.log('this.programmes', this.programmes)

    this.boroughReportLinkForcedToDisabled = false;
    this.enabled = {};
    this.enabled.programmeSelector = false;
    this.enabled.boroughReportLink = false;

    this.canRunJasper = this.UserService.hasPermission('reports.jasper');
    this.UserService.isCurrentUserAllowedToAccessSkillsGateway().then(rsp => {
      this.isCurrentUserAllowedToAccessSkillsGateway = rsp.data;
    });

    this.showStaticReports = this.UserService.hasPermission('reports.view.static');
    this.showStaticReports = false;
    this.viewInteralReports = this.UserService.hasPermission('reports.internal.view');
    this.runAdhocReports = this.UserService.hasPermission('reports.adhoc');

    this.reportTypes = [];
    this.reportsAvailable = !!this.reports.length;
    this.filterDropDowns = {};
    this.filterSingleSelectValues = {};
    this.programmeIds = [];

    this.reportStatusMap = {
      'inProgress': 'In progress',
      'noResults': 'No results',
      'Complete': 'Complete'
    };
    this.reportTypes = [];
    (this.reports || []).forEach(report => {
      if(this.viewInteralReports || report.external){
        let reportFiltersList = report.reportFiltersList;
        if(!this.viewInteralReports){
          reportFiltersList = _.filter(reportFiltersList, {external: true});
        }
        this.reportTypes.push({
          name: report.name,
          description: report.description,
          sqlFilter: report.sqlFilter,
          singleSelect: report.singleSelect,
          url: (programmeId) => `${this.config.basePath}/generate/csv/${report.name}?programme=${programmeId}`,
          extraParams: true,
          filters: reportFiltersList
        });
      }
    });


    this.startPolling();
  }

  getLabel(filter) {
    return _.startCase(filter.name);
  }

  selectReportType(selectedReportType) {
    this.selectedReportType = selectedReportType;
    this.updateProgrammes(selectedReportType);
    this.loadDependentFilters();
    this.updateEnableElements();
  }

  hasTooManyPendingReports(){
    return this.pendingReportsCount > 3
  }


  //Programmes
  updateProgrammes (selectedReportType) {
    if(selectedReportType!=null) {

      this.showProgrammeFilter = !!_.find(selectedReportType.filters, {name: 'Programme'});
      this.programmes = [];
      //TODO why do we do this? should be a placeholder instead
      this.enabled.programmeSelector = true;
      this.programmeId = null;
      this.allProgrammes.forEach((programme)=>{
        if(programme.status != 'Abandoned'){
          this.programmes.push({
            id: programme.id,
            label: programme.name,
            supportedReports: programme.supportedReports
          });
        }
      });
      this.filterProgrammes(selectedReportType);
    }
    else {
      this.filteredProgrammes = [];
      this.enabled.programmeSelector = false;
    }
  };

  filterProgrammes(selectedReportType) {
    let filteredProgrammes = [];

    _.forEach(this.programmes, (programme)=>{
      // TODO this can never be true !
      if(selectedReportType.filterProgramme){
        if(programme.supportedReports && programme.supportedReports.indexOf(selectedReportType.filterProgramme) !== -1){
          filteredProgrammes.push(programme);
        }
      } else {
        filteredProgrammes.push(programme);
      }
    });

    this.filteredProgrammes = filteredProgrammes;
    console.log('this.filteredProgrammes', this.filteredProgrammes);
  };

  isReportSupported (programmes, reportType) {
    return programmes.some(programme => {
      return (programme.supportedReports || []).indexOf(reportType) !== -1
    });
  };

  selectProgramme (programme) {
    this.programmeIds = _.filter(this.filteredProgrammes, {model: true}).map(p => p.id);
    this.updateEnableElements();
    this.loadDependentFilters();
  };

  getFilterDropDowns(filter) {
    return this.filterDropDowns[filter.name];
  }

  loadDependentFilters () {
    this.filterSingleSelectValues = {};
    if(this.selectedReportType !=null) {
      if (this.programmeIds.length && this.selectedReportType.extraParams) {
        this.ReportService.getFilterDropDowns(this.programmeIds, this.selectedReportType.name).then(data => {
          let info = [];
          _.forEach(data, (key, value) => {
            _.forEach(key, (key, value) => {
              info.push({
                'id':value,
                'label':key
              });
            });
            this.filterDropDowns[value] = info;
            info = [];
          });
        });
      }
    }
    else {
      this.disableFilters();
    }
  }

  disableFilters () {
    this.enabled.projectType = false;
    this.enabled.borough = false;
    this.enabled.projectStatus = false;
  };

  updateEnableElements () {
    if (!this.selectedReportType) {
      this.disableFilters();
      this.enabled.boroughReportLink = false
    }
    else{
      this.enabled.programmeSelector = !!_.find(this.selectedReportType.filters, {name: 'Programme'}); //_.includes(this.selectedReportType.filters, 'Programme');
      this.enabled.projectType = !!_.find(this.selectedReportType.filters, {name: 'TemplateType'}) && this.enabled.programmeSelector;//_.includes(this.selectedReportType.filters, 'TemplateType') && this.enabled.programmeSelector;
      this.enabled.borough = !!_.find(this.selectedReportType.filters, {name: 'Borough'}) && this.enabled.programmeSelector;//_.includes(this.selectedReportType.filters, 'Borough') && this.enabled.programmeSelector;
      this.enabled.projectStatus = !!_.find(this.selectedReportType.filters, {name: 'ProjectType'}) && this.enabled.programmeSelector;//_.includes(this.selectedReportType.filters, 'ProjectType') && this.enabled.programmeSelector;
    }
    if(!this.enabled.projectStatus) {
      this.projectStatus = [];
    }
    if(!this.enabled.projectType) {
      this.projectTypes = [];
    }
    if(!this.enabled.borough) {
      this.boroughs = [];
    }
  };

  onFormChange (){
    this.updateEnableElements();
  };

  enableReportLink () {
    if (!this.boroughReportLinkForcedToDisabled && this.programmeIds.length) {
      this.enabled.boroughReportLink = true
    }
  };

  getFiltersObject() {
    let filtersObject = [];
    _.forEach(this.filterDropDowns, (filterDropDown, key) => {
        this.processSelected(filterDropDown, key, filtersObject);
    });
    let programmeFilter = {
      'filter':'Programme',
      'parameters': this.programmeIds
    };
    filtersObject.push(programmeFilter);
    return filtersObject;
  }

  processSelected(filterDropDown, key, filtersObject) {
    let filter = {
      'filter': key,
      'parameters': []
    };
    _.forEach(filterDropDown, (key, value) => {
      if (key.model == true) {
        filter.parameters.push(key.id);
      }
    });

    if(this.filterSingleSelectValues[key]){
      filter.parameters.push(this.filterSingleSelectValues[key]);
    }

    if(filter.parameters.length) {
      filtersObject.push(filter);
    }
  }

  downloadCSV () {
    if(!this.isFormValid()){
      return;
    }
    this.boroughReportLinkForcedToDisabled = true;
    this.enabled.boroughReportLink = false;

    //TODO we can use service methods instead of url params. It was a direct html link before but now we don't need it
    //TODO form this.filters object from items selected in checkboxes!
    this.ReportService.generateReportWithParameters(this.selectedReportType.name, this.getFiltersObject())
      .then((content) => {
        if(this.poll && !this.poll.isStopped){
          //Stop to check results immediately, not after a long timeout if polling was running for a long time already;
          this.poll.stop();
        }
        this.startPolling();
        this.resetFilters();
        this.boroughReportLinkForcedToDisabled = false;
      })
      .catch(err => {
        let errInfo = err.data || {};
        this.ConfirmationDialog.warn(errInfo.description || 'Report cannot be produced!');
      })
  }

  resetFilters() {
    this.showProgrammeFilter = false;
    this.selectedReportType = null;
    this.programmeIds = [];
    this.filterSingleSelectValues = {};
    this.selectReportType(this.selectedReportType);
  }

  isAnyFilterApplied() {
    if(this.selectedReportType!=null) {
      return true;
    }
    else {
      return false;
    }
  }

  startPolling(){
    this.poll = this.ReportService.pollReports((rsp)=>{
      console.log('rsp', rsp.data);
      this.generatedReports = rsp.data;
      this.pendingReportsCount = _.filter(this.generatedReports, {status: 'inProgress'}).length;
      console.log('this.pendingReportsCount', this.pendingReportsCount);
      if(this.pendingReportsCount === 0){
        this.poll.stop();
      }
    });
  }

  $onDestroy () {
    console.log('on destroy', this.poll);
    if(this.poll){
      this.poll.stop();
    }
  };

  isFormValid(){
    return this.selectedReportType && this.programmeIds.length && !this.hasTooManyPendingReports() && !this.hasSqlQueryParamsMissing();
  }

  hasSqlQueryParamsMissing(){
    return _.some((this.selectedReportType || {}).filters, f => {
      return f.sqlFilter && f.singleSelect && this.filterSingleSelectValues[f.name] == null;
    });
  }
  delete(report) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the Report?');
    modal.result.then(() => {
      this.onDelete(report.id)
    });
  }

  onDelete(reportId) {
    this.ReportService.deleteUserReport(reportId).then(rsp => {
      this.$state.reload();
    })
  }
}

ReportsCtrl.$inject = ['ProgrammeService', 'FeatureToggleService', '$q', 'config', 'ReportService', 'ConfirmationDialog', 'UserService', 'TemplateService', '$state'];


angular.module('GLA')
  .component('reportsPage', {
    templateUrl: 'scripts/pages/reports/reports.html',
    bindings: {
      envVars: '<',
      programmes: '<',
      reports: '<',
      programmeReportEnabled: '<',
      affordableHousingReportEnabled: '<',
      boroughReportEnabled: '<'
    },
    controller: ReportsCtrl
  });
