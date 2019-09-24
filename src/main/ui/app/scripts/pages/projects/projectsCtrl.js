/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class ProjectsCtrl {
  constructor($log, UserService, SessionService, $state, ProjectService, MessageModal, $stateParams, ConfirmationDialog, ToastrUtil, TransferModal, $rootScope) {
    this.$log = $log;
    this.UserService = UserService;
    this.SessionService = SessionService;
    this.$state = $state;
    this.ProjectService = ProjectService;
    this.MessageModal = MessageModal;
    this.$stateParams = $stateParams;
    this.ConfirmationDialog = ConfirmationDialog;
    this.ToastrUtil = ToastrUtil;
    this.TransferModal = TransferModal;
    this.$rootScope = $rootScope;
  }

  $onInit(){
    this.user = this.UserService.currentUser();
    this.totalItems = 0;
    this.indexStart = 0;
    this.indexEnd = 0;
    this.itemsPerPage = 50;
    this.currentPage = 1;
    this.$rootScope.showGlobalLoadingMask = true;
    this.projects = [];

    this._operationsConst = {
      assess: 'ASSESS',
      revert: 'REVERT',
      delete: 'DELETE',
      transfer: 'TRANSFER'
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


    this.canViewRecommendations = this.UserService.hasPermission('proj.view.recommendation');
    this.groupedFilterDropdownItems = this.ProjectService.filterDropdownItems(this.canViewRecommendations, this.projectStates);
    let filterDropdownItems = this.flatCheckboxes(this.groupedFilterDropdownItems);
    filterDropdownItems.forEach(f => f.collapsed = true);

    this.defaultFilter = angular.copy(filterDropdownItems);
    this.filterDropdownItems = this.applyCachedStatusFiltersState(filterDropdownItems);
    this.initProgrammesDropdown();
    this.initTemplatesDropdown();

    this.canAssess = this.UserService.hasPermission('proj.assess');
    this.canCreate = this.UserService.hasPermissionStartingWith('proj.create');
    this.canTransfer = this.UserService.hasPermissionStartingWith('proj.transfer');


    this.searchOptions = this.ProjectService.searchOptions();

    this.byProjectOption = _.find(this.searchOptions, {name: 'title'});
    this.byProgrammeOption = _.find(this.searchOptions, {name: 'programmeName'});
    this.byOrganisationOption = _.find(this.searchOptions, {name: 'organisationName'});

    let projectsSearchState = this.getSearchParams();

    if (projectsSearchState.organisationName) {
      this.selectedSearchOption = this.byOrganisationOption;
    } else if (projectsSearchState.programmeName) {
      this.selectedSearchOption = this.byProgrammeOption;
    } else {
      this.selectedSearchOption = this.byProjectOption;
    }
    // this is used as a boolean  to determine whether we are in a search context ...
    this.searchText = projectsSearchState.title || projectsSearchState.organisationName || projectsSearchState.programmeName;

    // ... whereas this is used as the search text model
    this.searchTextModel = this.searchText;
    this.watchingProject = this.watchingProject == null ? false : this.watchingProject;
    this.getProjects(true);
  }

  saveProjectStatusesToCache() {
    let filterState = this.SessionService.getProjectsFilterState();
    _.forEach(this.filterDropdownItems, (filter) => {
      filterState[filter.name] = _.pick(filter, ['model', 'collapsed']);
    });
    this.SessionService.setProjectsFilterState(filterState);
  };

  saveProgrammesToCache() {
    let filterState = this.SessionService.getProjectsFilterState();
    filterState.programmes = this.getSelectedCheckboxes(this.programmesDropdown);
    filterState.templates = this.getSelectedCheckboxes(this.templatesDropdown);
    this.SessionService.setProjectsFilterState(filterState);
  };


  applyCachedStatusFiltersState(filterDropdownItems) {
    let filterState = this.SessionService.getProjectsFilterState();
    _.forEach(filterDropdownItems, (filter) => {
      if (_.isBoolean((filterState[filter.name] || {}).model) || _.isBoolean((filterState[filter.name] || {}).collapsed)) {
        filter.model = filterState[filter.name].model;
        filter.collapsed = filterState[filter.name].collapsed;
      }
    });
    return filterDropdownItems;
  };

  clearSearch() {
    this.setSearchParams({});
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
  };

  hasUrlParameter () {
    return Object.keys(this.$stateParams).some(key => this.$stateParams[key]);
  };

  getSearchParams() {
    if (this.hasUrlParameter()) {
      return this.$stateParams;
    }
    return this.SessionService.getProjectsSearchState();
  };

  setSearchParams(searchParams) {
    searchParams = searchParams || {};
    Object.keys(this.$stateParams).forEach(key => this.$stateParams[key] = searchParams[key]);
    this.SessionService.setProjectsSearchState(searchParams);
  };

  flatCheckboxes(groupedCheckbox) {
    let checkboxes = [];
    this.traverseCheckboxes(groupedCheckbox, (c) => checkboxes.push(c));
    return checkboxes;
  };

  traverseCheckboxes(groupedCheckboxes, callback) {
    (groupedCheckboxes || []).forEach(checkbox => {
      if (checkbox.items && checkbox.items.length) {
        this.traverseCheckboxes(checkbox.items, callback);
      } else {
        callback(checkbox);
      }
    })
  };

  getSelectedCheckboxes(checkboxesDropdown) {
    const selections = checkboxesDropdown.reduce((selectedValues, item) => {
      if (item.model) {
        selectedValues.push(item.id)
      }
      return selectedValues;
    }, []);
    return selections;
  };

  initProgrammesDropdown() {
    let selections = [];
    if (this.$stateParams.programmeId) {
      selections.push(this.$stateParams.programmeId);
    } else {
      selections = (this.SessionService.getProjectsFilterState() || {}).programmes || [];
    }

    let programmeDropdown = _.filter(this.allProgrammes, (p) => {
      return p.status != 'Abandoned';
    });

    this.programmesDropdown = _.map(programmeDropdown, p => {
      return {
        id: p.id,
        label: p.name,
        model: selections.length ? selections.indexOf(p.id) !== -1 : p.model
      }
    });
  };

  initTemplatesDropdown() {
    let selections = [];
    if (this.$stateParams.templateId) {
      selections.push(this.$stateParams.templateId);
    } else {
      selections = (this.SessionService.getProjectsFilterState() || {}).templates || [];
    }

    let templates = [];
    this.allProgrammes.forEach(programme => {
      if (programme.status !== 'Abandoned') {
        programme.templates.forEach(template => {
          templates.push(template);
        });
      }
    });

    templates = _.sortBy(_.uniqBy(templates, 'id'), 'name');

    this.templatesDropdown = _.map(templates, t => {
      return {
        id: t.id,
        label: t.name,
        model: selections.length ? selections.indexOf(t.id) !== -1 : t.model
      }
    });
  };

  select(searchOption) {
    this.searchTextModel = null;
    this.selectedSearchOption = searchOption;
  };

  search () {
    this.setSearchParams({
      [this.selectedSearchOption.name]: this.searchTextModel
    });
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
  };

  clearFiltersAndSearch() {
    this.SessionService.clearProjectsState();
    this.clearSearch();
  };

  getStateParam(status, substatus) {
    return substatus ? `${status}:${substatus}` : status;
  };

  getStateParams(statusCheckbox) {
    let states = [];
    if (statusCheckbox.projectRecommentationKeys) {
      statusCheckbox.projectRecommentationKeys.forEach(recommendationKey => {
        states.push(this.getStateParam(statusCheckbox.projectStatusKey, recommendationKey));
      })
    } else if (statusCheckbox.projectSubStatusKeys && statusCheckbox.projectSubStatusKeys.length) {
      statusCheckbox.projectSubStatusKeys.forEach(subStatus => {
        states.push(this.getStateParam(statusCheckbox.projectStatusKey, subStatus));
      })
    } else {
      states.push(this.getStateParam(statusCheckbox.projectStatusKey));
    }
    return states;
  };

  getSelectedStatuses() {
    let states = [];
    let filteredList = [];
    _.forEach(this.filterDropdownItems, (statusCheckbox) => {
      if (statusCheckbox.model) {
        states = states.concat(this.getStateParams(statusCheckbox))
      }
    });
    return states;
  };

  hasStateCheckboxFilterChanged() {
    return this.filterDropdownItems.some(checkbox => {
      let defaultModel = _.find(this.defaultFilter, {name: checkbox.name}).model;
      return defaultModel != checkbox.model;
    });
  };

  onProgrammeSelected() {
    // deselect all templates
    _.forEach(this.templatesDropdown, templateDropdown => { templateDropdown.model = false });

    // selecting programmes should result in associated templates to be selected
    const selectedProgrammes = this.getSelectedCheckboxes(this.programmesDropdown);

    let templatesToBeSelected = [];
    _.forEach(this.allProgrammes, (programme) => {
      if (_.includes(selectedProgrammes, programme.id)) {
        _.forEach(programme.templates, (template) => {
          templatesToBeSelected.push(template.id);
        });
      }
    });

    _.forEach(this.templatesDropdown, (templateDropdown) => {
      if (_.includes(templatesToBeSelected, templateDropdown.id)) {
        templateDropdown.model = true;
      }
    });

    // this will trigger the reevaluation of the component, and will result in "Filter applied" / "Deselect all" to be displayed
    this.templatesDropdown = angular.copy(this.templatesDropdown);

    this.getProjects(true);
  };

  onWatchedCheckboxClick() {
    this.getProjects(true);
  }

  getProjects(showFirstPage) {
    let projectsSearchState = this.getSearchParams();
    let page = showFirstPage ? 0 : this.currentPage - 1;

    let states = this.getSelectedStatuses();
    const selectedProgrammes = this.getSelectedCheckboxes(this.programmesDropdown);
    const selectedTemplates = this.getSelectedCheckboxes(this.templatesDropdown);
    this.isDefaultFilterState = !this.hasStateCheckboxFilterChanged();
    this.isDefaultProgrammeState = !selectedProgrammes.length;
    this.isDefaultTemplateState = !selectedTemplates.length;

    this.ProjectService.getProjects(
      projectsSearchState.title,
      projectsSearchState.organisationName,
      projectsSearchState.programmeName,
      states,
      this.selectedSearchOption.name === this.byProgrammeOption.name ? null : selectedProgrammes,
      selectedTemplates,
      this.watchingProject,
      page)
      .then(rsp => {
        if (showFirstPage) {
          this.currentPage = 1;
        }
        this.projects = rsp.data.content;
        this.totalItems = rsp.data.totalElements;

        _.forEach(this.projects, (project) => {
          if (!project.fullStatus) {
            this.setFullStatus(project);
          }
        });
        this.updateAllSelectedCheckBoxState();
        this.saveProjectStatusesToCache();
        this.saveProgrammesToCache();

      })
      .catch(err => {
        this.$log.error(err);
      })
      .finally(() => {
        this.$rootScope.showGlobalLoadingMask = false;
      });
  };

  /**
   * Create a new project handler
   */
  createNewProject() {
    if (!this.programmes.length) {
      const modal = MessageModal.show({
        message: 'There are currently no programmes available.'
      });
    } else {
      this.$state.go('projects-new', {
        programmes: this.programmes
      });
    }
  };

  /**
   * Open project
   */
  goToProjectOverview(id) {
    this.$state.go('project-overview', {
      'projectId': id
    });
  };

  onAllCheckboxChange () {
    _.forEach(this.projects, (project) => {
      project.isSelected = this.allSelected;
    });
  };

  onProjectCheckboxClick() {
    this.updateAllSelectedCheckBoxState();
  };

  updateAllSelectedCheckBoxState() {
    const trueCount = _.groupBy(this.projects, 'isSelected').true;
    this.allSelected = trueCount && trueCount.length === this.projects.length;
  };


  setToAssess() {
    const ids = [];
    _.map(this.projects, (project) => {
      if (project.isSelected) {
        ids.push(project.id);
      }
    });

    if (ids.length) {
      this.ConfirmationDialog.show(
        {
          message: 'Are you sure you want to set the selected projects with a status of Submitted to Assess?<br>Only projects with a current status of Submitted can be set to Assess status.',
          approveText: 'SET TO ASSESS',
          dismissText: 'CANCEL'
        }
      ).result.then(() => {
        return this.ProjectService.projectBulkOperation(ids, this._operationsConst.assess).then((resp) => {
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

  transfer() {
    const projects = [];
    _.map(this.projects, (project) => {
      if (project.isSelected) {
        projects.push(project);
      }
    });

    if (projects.length) {
      this.TransferModal.show(projects).result.then(() => {
        this.getProjects();
      });
    }
  };

  showActions() {
    // permission check
    if (this.canAssess) {// || this.canRevert || this.canDelete) {
      return _.some(this.projects, 'isSelected');
    }
    return false;
  };


  setFullStatus(project) {
    project.fullStatus = project.statusName;
    if (this.subStatusConst[project.subStatusName]) {
      project.fullStatus += `: ${this.subStatusConst[project.subStatusName]}`
    } else if (project.statusName === 'Closed') {
      project.fullStatus += `: ${project.subStatusName || 'Rejected'}`
    } else if (this.canViewRecommendations && project.recommendation && project.statusName === 'Assess') {
      project.fullStatus += `: ${this.recommendationConst[project.recommendation]}`
    } else if (project.subStatusName) {
      project.fullStatus += `: ${project.subStatusName}`
    }
  };

  isAnyFilterApplied() {
    return this.searchText || !this.isDefaultFilterState || !this.isDefaultProgrammeState || !this.isDefaultTemplateState
  }
}

ProjectsCtrl.$inject = ['$log', 'UserService', 'SessionService', '$state', 'ProjectService', 'MessageModal', '$stateParams', 'ConfirmationDialog', 'ToastrUtil', 'TransferModal', '$rootScope'];

angular.module('GLA')
  .component('projectsPage', {
    templateUrl: 'scripts/pages/projects/projects.html',
    bindings: {
      programmes: '<',
      allProgrammes: '<',
      projectStates: '<'
    },
    controller: ProjectsCtrl
  });
