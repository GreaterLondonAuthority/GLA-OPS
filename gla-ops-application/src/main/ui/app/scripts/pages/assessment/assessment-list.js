/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class AssessmentListCtrl {
  constructor($state, AssessmentService, $rootScope, $stateParams, SessionService, ProjectService) {
    this.$state = $state;
    this.AssessmentService = AssessmentService;
    this.$rootScope = $rootScope;
    this.$stateParams = $stateParams;
    this.SessionService = SessionService;
    this.ProjectService = ProjectService;
  }

  $onInit(){
    this.searchOptions = this.AssessmentService.searchOptions();
    this.selectedSearchOption = this.searchOptions[1];

    this.assessmentTypeOptions = _.map(this.assessmentTemplates, p => {return {id: p.id, label: p.name}});
    this.assessmentStatusOptions = this.AssessmentService.getAssessmentStatusOptions();
    this.programmeOptions = _.map(this.allProgrammes, p => {return {id: p.id, label: p.name}});
    let projectStatuses = this.ProjectService.filterDropdownItems(false, this.projectStates);
    this.projectStatusOptions = _.map(projectStatuses, p => {return {id: p.id, label: p.name, items: p.items, model: false}});

    this.currentState = {
      titleForBackBtn: 'ALL ASSESSMENTS',
      name: this.$state.current.name,
      params: angular.copy(this.$state.params)
    };

    this.$rootScope.showGlobalLoadingMask = true;

    this.totalItems = 0;
    this.indexStart = 0;
    this.indexEnd = 0;
    this.itemsPerPage = 50;
    //Current page starts by 1 in UI but by 0 in backend
    this.currentPage = 1;
    this.getAssessments();
  }

  select(searchOption) {
    this.searchTextModel = null;
    this.selectedSearchOption = searchOption;
  }

  search() {
    this.setSearchParams({
      [this.selectedSearchOption.name]: this.searchTextModel
    });
    this.getAssessments()
  }

  clearSearch() {
    this.setSearchParams({});
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
    this.getAssessments()
  }

  clearFiltersAndSearch() {
    this.SessionService.resetUsersFilterState();
    this.SessionService.clearUsersState();
    this.clearSearch();
  }

  setSearchParams(searchParams){
    searchParams = searchParams || {};
    Object.keys(this.$stateParams).forEach(key => this.$stateParams[key] = searchParams[key]);
    this.SessionService.setUsersSearchState(searchParams);
  }

  isAnyFilterApplied() {
    return this.searchTextModel  || (this.isDefaultFilterState != undefined);
  }

  updateFilters() {
    this.isDefaultFilterState = !(
      _.some(this.assessmentTypeOptions, {model: true}) ||
      _.some(this.assessmentStatusOptions, {model: true}) ||
      _.some(this.programmeOptions, {model: true}) ||
      _.some(this.projectStatusOptions, {model: true})
    );

    this.getAssessments();
  }

  getAssessments(resetPage) {
    if (resetPage) {
      this.currentPage = 1;
    }

    let data = {};
    data.page = resetPage ? 0 : this.currentPage - 1;

    if(this.selectedSearchOption.name === 'created_by'){
      data.createdBy = this.searchTextModel;
    } else {
      data.project = this.searchTextModel;
    }

    data.assessmentTypeOptions = _.map(_.filter(this.assessmentTypeOptions, {model:true}), 'label') || [];
    data.assessmentStatusOptions = _.map(_.filter(this.assessmentStatusOptions, {model:true}), 'key') || [];
    data.programmeOptions = _.map(_.filter(this.programmeOptions, {model:true}), 'id') || [];
    data.projectStatusOptions = _.map(_.filter(this.projectStatusOptions, {model:true}), 'label') || [];

    this.AssessmentService.getAssessmentsPerPage(data.createdBy, data.project, data.assessmentTypeOptions, data.assessmentStatusOptions, data.programmeOptions, data.projectStatusOptions, data.page).then(response => {
      this.$rootScope.showGlobalLoadingMask = false;
      this.assessments = response.data.content;
      this.totalItems = response.data.totalElements;
    });
  }

}

AssessmentListCtrl.$inject = ['$state', 'AssessmentService', '$rootScope', '$stateParams', 'SessionService', 'ProjectService'];

angular.module('GLA')
  .component('assessmentList', {
    templateUrl: 'scripts/pages/assessment/assessment-list.html',
    bindings: {
      allProgrammes: '<',
      projectStates: '<',
      assessmentTemplates: '<'
    },
    controller: AssessmentListCtrl
  });
