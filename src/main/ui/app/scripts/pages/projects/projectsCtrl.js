/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ProjectsCtrl.$inject = ['$log', 'UserService', 'SessionService', '$state', 'ProjectService', 'MessageModal', '$stateParams', 'ConfirmationDialog', 'ToastrUtil'];

function ProjectsCtrl($log, UserService, SessionService, $state, ProjectService, MessageModal, $stateParams, ConfirmationDialog, ToastrUtil) {
  this.user = UserService.currentUser();
  this.currentPage = 1;
  this.itemsPerPage = 50;
  this.hasFilterSelections = true;

  this.ToastrUtil = ToastrUtil;


  this._operationsConst = {
    assess: 'ASSESS',
    revert: 'REVERT',
    delete: 'DELETE'
  };

  this.recommendationConst = {
    'RecommendApproval': 'Recommend Approve',
    'RecommendRejection': 'Recommend Reject'
  };

  this.subStatusConst = {
    'ApprovalRequested': 'Approval Requested',
    'UnapprovedChanges': 'Unapproved Changes',
    'PaymentAuthorisationPending': 'Payment Authorisation Pending',
    'AbandonPending': 'Abandon Pending'
  };

  const defaultFilterState = {
    active: true,
    activeUnapprovedChanges: true,
    activeApprovalRequested: true,
    activePaymentAuthorisationPending: true,
    activeAbandonPending: true,
    assess: true,
    assessRecommendApprove: true,
    assessRecommendReject: true,
    closed: false,
    draft: true,
    returned: true,
    submitted: true,
  };

  this.saveFilterState = (filterDropdownItems) => {
    let filterState = {};
    _.forEach(filterDropdownItems, (filter) => {
      filterState[filter.name] = filter.model;
    });
    SessionService.setProjectsFilterState(filterState);
  };

  this.applyFilterState = (filterDropdownItems) => {
    let filterState = SessionService.getProjectsFilterState();
    _.forEach(filterDropdownItems, (filter) => {
      filter.model = _.isBoolean(filterState[filter.name]) ? filterState[filter.name] : defaultFilterState[filter.name];
    });
    return filterDropdownItems;
  };

  this.clearSearch = function () {
    this.setSearchParams({});
    $state.go($state.current, $stateParams, {reload: true});
  };

  this.hasUrlParameter = function (){
    return Object.keys($stateParams).some(key => $stateParams[key]);
  };

  this.getSearchParams = function (){
    if(this.hasUrlParameter()){
      return $stateParams;
    }
    return SessionService.getProjectsSearchState();
  };

  this.setSearchParams = function (searchParams){
    searchParams = searchParams || {};
    Object.keys($stateParams).forEach(key => $stateParams[key] = searchParams[key]);
    SessionService.setProjectsSearchState(searchParams);
  };


  const canViewRecommendations = UserService.hasPermission('proj.view.recommendation');
  let filterDropdownItems = ProjectService.filterDropdownItems(canViewRecommendations);


  this.filterDropdownItems = this.applyFilterState(filterDropdownItems);


  this.canAssess = UserService.hasPermission('proj.assess');
  this.canRevert = false; // _.includes(permissions, 'proj.revert');
  this.canDelete = false; // _.includes(permissions, 'proj.delete');

  this.searchOptions = ProjectService.searchOptions();

  let projectsSearchState = this.getSearchParams();
  console.log('search model', projectsSearchState)
  if (projectsSearchState.programmeName) {
    this.selectedSearchOption = this.searchOptions[1];
  }
  else if (projectsSearchState.organisationId) {
    this.selectedSearchOption = this.searchOptions[2];
  }
  else {
    this.selectedSearchOption = this.searchOptions[0];
  }
  // this is used as a boolean  to determine whether we are in a search context ...
  this.searchText = projectsSearchState.title || +projectsSearchState.organisationId || projectsSearchState.programmeName;

  // ... whereas this is used as the search text model
  this.searchTextModel = this.searchText;

  this.select = function (searchOption) {
    this.searchTextModel = null;
    this.selectedSearchOption = searchOption;
    $log.log('this.selectedSearchOption', searchOption)
  };

  this.search = function () {
    this.setSearchParams({
      [this.selectedSearchOption.name]: this.searchTextModel
    });
    $state.go($state.current, $stateParams, {reload: true});
  };




  this.clearFiltersAndSearch = () => {
    SessionService.clearProjectsState();
    this.clearSearch();
  };

  this.getProjects = () => {
    this.projects = [];
    this.loading = true;
    let projectsSearchState = this.getSearchParams();
    // Object.assign(projectsSearchState, $stateParams);
    ProjectService.getAllProjects(projectsSearchState.title, projectsSearchState.organisationId, projectsSearchState.programmeId, projectsSearchState.programmeName)
      .then(data => {
        this.loading = false;
        this.projects = data;
        this.updateFilters();
      })
      .catch(err => {
        $log.error(err);
      })
      .finally(() => {
        this.loading = false;
      });
  };

  this.getProjects();

  /**
   * Create a new project handler
   */
  this.createNewProject = () => {
    if (!this.programmes.length) {
      const modal = MessageModal.show({
        message: 'There are currently no programmes available.'
      });
    } else {
      $state.go('projects-new', {
        programmes: this.programmes
      });
    }
  };

  /**
   * Open project
   */
  this.goToProjectOverview = (id) => {
    $state.go('project.overview', {
      'projectId': id
    });
  };

  this.onAllCheckboxChange = () => {
    _.forEach(this.filteredList, (project) => {
      project.isSelected = this.allSelected;
    });
  };
  this.onProjectCheckboxClick = (project) => {
    this.updateAllSelectedCheckBoxState();
  };
  this.updateAllSelectedCheckBoxState = () => {
    const trueCount = _.groupBy(this.filteredList, 'isSelected').true;
    this.allSelected = trueCount && trueCount.length === this.filteredList.length;
  };



  this.setToAssess = () => {
    const ids = [];
    _.map(this.filteredList, (project) => {
      if (project.isSelected) {
        ids.push(project.id);
      }
    });

    if (ids.length) {
      ConfirmationDialog.show(
        {
          message: 'Are you sure you want to set the selected projects with a status of Submitted to Assess?<br>Only projects with a current status of Submitted can be set to Assess status.',
          approveText: 'SET TO ASSESS',
          dismissText: 'CANCEL'
        }
      ).result.then(() => {
        return ProjectService.projectBulkOperation(ids, this._operationsConst.assess).then((resp) => {
          const data = resp.data;
          if (data.successCount === ids.length) {
            this.ToastrUtil.success(`${data.successCount} project${data.successCount > 1 ? '(s)' : ''} successfully set to Assess status`);
          } else if (data.failureCount === ids.length) {
            this.ToastrUtil.warning('No projects have been updated');
          } else {
            this.ToastrUtil.warning(`${data.successCount} project${data.successCount > 1 ? '(s)' : ''} successfully set to Assess status and ${data.failureCount} not updated`);
          }
          this.getProjects();
        });
      });
    }
  };

  this.showActions = () => {
    // permission check
    if (this.canAssess || this.canRevert || this.canDelete) {
      return _.some(this.filteredList, 'isSelected');
    }
    return false;
  };

  this.updateFilters = () => {
    this.activeFilter = [];
    let filteredList = [];
    let isDefaultFilterState = true;
    _.forEach(this.filterDropdownItems, (item) => {
      if (item.model) {
        if (item.projectRecommentationKey) {
          this.activeFilter.push(item.projectStatusKey + item.projectRecommentationKey);
        } else if (item.projectSubStatusKeys && item.projectSubStatusKeys.length) {
          item.projectSubStatusKeys.forEach(subStatus => {
            this.activeFilter.push(item.projectStatusKey + (subStatus || ''));
          })
        } else {
          this.activeFilter.push(item.projectStatusKey);
        }
      }
      if (item.model !== defaultFilterState[item.name]) {
        isDefaultFilterState = false;
      }
    });
    this.isDefaultFilterState = isDefaultFilterState;
    this.filterCounts = _.countBy(this.filterDropdownItems, 'model');
    this.hasFilterSelections = this.filterCounts.true > 0;

    _.forEach(this.projects, (project) => {
      if (!project.fullStatus) {
        this.setFullStatus(project);
      }

      if (this.projectFilter(project)) {
        filteredList.push(project);
      } else {
        project.isSelected = false;
      }
    });

    this.filteredList = filteredList;
    this.showPage(1);

    this.updateAllSelectedCheckBoxState();

    this.saveFilterState(this.filterDropdownItems);
  };


  this.setFullStatus = (project) => {
    project.fullStatus = project.status;
    if (this.subStatusConst[project.subStatus]) {
      project.fullStatus += `: ${this.subStatusConst[project.subStatus]}`
    } else if (project.status === 'Closed') {
      project.fullStatus += `: ${project.subStatus || 'Rejected'}`
    } else if (canViewRecommendations && project.recommendation && project.status === 'Assess') {
      project.fullStatus += `: ${this.recommendationConst[project.recommendation]}`
    }
  };

  this.showPage = (pageNumber) => {
    this.currentPage = pageNumber;
    this.indexStart = (pageNumber - 1) * this.itemsPerPage;
    let end = (pageNumber) * this.itemsPerPage;
    this.indexEnd = (end > this.filteredList.length) ? this.filteredList.length : end;
    this.projectsPage = this.filteredList.slice(this.indexStart, this.indexEnd);
  };

  this.projectFilter = (project) => {
    const count = this.filterCounts;
    if (count.false === this.filterDropdownItems.length) {
      return false;
    } else if (count.true === this.filterDropdownItems.length) {
      return true;
    } else {
      let projectKey = project.status;
      if (project.status === 'Assess') {
        projectKey += (project.recommendation || '');
      }
      if (['Active', 'Closed'].some(status => status === project.status)) {
        projectKey += (project.subStatus || '');
      }
      return this.activeFilter.indexOf(projectKey) !== -1;
    }
  };
  this.updateFilters();
}

// angular.module('GLA').controller('ProjectsCtrl', ProjectsCtrl);
angular.module('GLA')
  .component('projectsPage', {
    templateUrl: 'scripts/pages/projects/projects.html',
    bindings: {
      programmes: '<'
    },
    controller: ProjectsCtrl
  });
