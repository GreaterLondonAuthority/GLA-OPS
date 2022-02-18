import {Component, Input, OnInit} from '@angular/core';
import {ProjectTransferModalComponent} from "../project-transfer-modal/project-transfer-modal.component";
import {ProjectAssignModalComponent} from "../project-assign-modal/project-assign-modal.component";
import { UserService } from '../../user/user.service';
import {LoadingMaskService} from "../../shared/loading-mask/loading-mask.service";
import {ProjectService} from "../project.service";
import {
  cloneDeep,
  filter,
  find,
  forEach,
  groupBy,
  includes,
  isBoolean,
  map,
  pick,
  remove, size, some,
  sortBy,
  uniqBy
} from 'lodash-es';
import {SessionService} from "../../session/session.service";
import {NavigationService} from "../../navigation/navigation.service";
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import { ErrorModalComponent } from '../../shared/error/error-modal/error-modal.component';

@Component({
  selector: 'gla-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss']
})
export class ProjectsPageComponent implements OnInit {

  @Input() allProgrammes: any[]
  @Input() projectStates: any[]

  user: any;
  totalItems = 0;
  indexStart = 0;
  indexEnd = 0;
  itemsPerPage = 50;
  currentPage = 1;
  projects: any[];

  _operationsConst = {
    assess: 'ASSESS',
    revert: 'REVERT',
    delete: 'DELETE',
    transfer: 'TRANSFER'
  };

  recommendationConst = {
    'RecommendApproval': 'Recommend Approve',
    'RecommendRejection': 'Recommend Reject'
  };

  subStatusConst = {
    'ApprovalRequested': 'Approval Requested',
    'UnapprovedChanges': 'Unapproved Changes',
    'PaymentAuthorisationPending': 'Payment Authorisation Pending',
    'AbandonPending': 'Abandon Pending'
  };
  canViewRecommendations = false;
  canViewAssignee = false;
  canAssess = false;
  groupedFilterDropdownItems: any[];
  defaultFilter: any;
  filterDropdownItems: any[];
  enabledProgrammes: any[];
  canCreate = false;
  canTransfer = false;
  canAssign = false;
  searchOptions: any[];
  byProjectOption: any;
  byProgrammeOption: any;
  byOrganisationOption: any;
  byAssigneeOption: any;
  selectedSearchOption: any;
  searchText: string;
  searchTextModel: string;
  watchingProject: boolean;
  programmesDropdown: any[];
  templatesDropdown: any[];
  isDefaultFilterState: boolean;
  isDefaultProgrammeState: boolean;
  isDefaultTemplateState: boolean;
  allSelected: boolean;

  constructor(private userService: UserService,
              private loadingMaskService: LoadingMaskService,
              private projectService: ProjectService,
              private sessionService: SessionService,
              private navigationService: NavigationService,
              private confirmationDialogService: ConfirmationDialogService,
              private toastrUtilService: ToastrUtilService,
              private ngbModal: NgbModal) { }

  ngOnInit(): void {
    this.user = this.userService.currentUser();
    this.loadingMaskService.showLoadingMask(false)
    this.projects = [];

    this.canViewRecommendations = this.userService.hasPermission('proj.view.recommendation');
    this.canViewAssignee = this.userService.hasPermission('proj.view.assignee');
    this.groupedFilterDropdownItems = this.projectService.filterDropdownItems(this.canViewRecommendations, this.projectStates);
    let filterDropdownItems = this.flatCheckboxes(this.groupedFilterDropdownItems);
    filterDropdownItems.forEach(f => f.collapsed = true);

    this.defaultFilter = cloneDeep(filterDropdownItems);
    this.filterDropdownItems = this.applyCachedStatusFiltersState(filterDropdownItems);
    this.allProgrammes = sortBy(this.allProgrammes, 'name');
    this.enabledProgrammes = filter(this.allProgrammes, {enabled: true});
    this.initProgrammesDropdown();
    this.initTemplatesDropdown();

    this.canAssess = this.userService.hasPermission('proj.assess');
    this.canCreate = this.userService.hasPermissionStartingWith('proj.create');
    this.canTransfer = this.userService.hasPermissionStartingWith('proj.transfer');
    this.canAssign = this.userService.hasPermissionStartingWith('proj.assign');


    this.searchOptions = this.projectService.searchOptions();
    if (!this.canViewAssignee) {
      remove(this.searchOptions, {name: 'assignee'});
    }

    this.byProjectOption = find(this.searchOptions, {name: 'title'});
    this.byProgrammeOption = find(this.searchOptions, {name: 'programmeName'});
    this.byOrganisationOption = find(this.searchOptions, {name: 'organisationName'});
    this.byAssigneeOption = find(this.searchOptions, {name: 'assignee'});

    let projectsSearchState = this.getSearchParams();

    if (projectsSearchState.organisationName) {
      this.selectedSearchOption = this.byOrganisationOption;
    } else if (projectsSearchState.programmeName) {
      this.selectedSearchOption = this.byProgrammeOption;
    } else if (projectsSearchState.assignee) {
      this.selectedSearchOption = this.byAssigneeOption;
    } else {
      this.selectedSearchOption = this.byProjectOption;
    }
    // this is used as a boolean  to determine whether we are in a search context ...
    this.searchText = projectsSearchState.title || projectsSearchState.organisationName || projectsSearchState.programmeName || projectsSearchState.assignee;

    // ... whereas this is used as the search text model
    this.searchTextModel = this.searchText;
    this.watchingProject = this.watchingProject == null ? false : this.watchingProject;
    this.getProjects(true);
  }

  saveProjectStatusesToCache() {
    let filterState = this.sessionService.getProjectsFilterState();
    forEach(this.filterDropdownItems, (filter) => {
      filterState[filter.name] = pick(filter, ['model', 'collapsed']);
    });
    this.sessionService.setProjectsFilterState(filterState);
  }

  saveProgrammesToCache() {
    let filterState = this.sessionService.getProjectsFilterState();
    filterState.programmes = this.getSelectedCheckboxes(this.programmesDropdown);
    filterState.templates = this.getSelectedCheckboxes(this.templatesDropdown);
    this.sessionService.setProjectsFilterState(filterState);
  }


  applyCachedStatusFiltersState(filterDropdownItems) {
    let filterState = this.sessionService.getProjectsFilterState();
    forEach(filterDropdownItems, (filter) => {
      if (isBoolean((filterState[filter.name] || {}).model) || isBoolean((filterState[filter.name] || {}).collapsed)) {
        filter.model = filterState[filter.name].model;
        filter.collapsed = filterState[filter.name].collapsed;
      }
    });
    return filterDropdownItems;
  }

  clearSearch() {
    this.setSearchParams({});
    let stateParams = this.navigationService.getCurrentStateParams();
    this.navigationService.goToCurrentUiRouterState(stateParams, {reload: true})
    // this.$state.go(this.$state.current, this.$stateParams, {reload: true});
  }

  hasUrlParameter () {
    let stateParams = this.navigationService.getCurrentStateParams();
    return Object.keys(stateParams).some(key => stateParams[key]);
  }

  getSearchParams() {
    if (this.hasUrlParameter()) {
      return this.navigationService.getCurrentStateParams();
    }
    return this.sessionService.getProjectsSearchState();
  }

  setSearchParams(searchParams) {
    searchParams = searchParams || {};
    let stateParams = this.navigationService.getCurrentStateParams();
    Object.keys(stateParams).forEach(key => stateParams[key] = searchParams[key]);
    this.sessionService.setProjectsSearchState(searchParams);
  }

  flatCheckboxes(groupedCheckbox) {
    let checkboxes = [];
    this.traverseCheckboxes(groupedCheckbox, (c) => checkboxes.push(c));
    return checkboxes;
  }

  traverseCheckboxes(groupedCheckboxes, callback) {
    (groupedCheckboxes || []).forEach(checkbox => {
      if (checkbox.items && checkbox.items.length) {
        this.traverseCheckboxes(checkbox.items, callback);
      } else {
        callback(checkbox);
      }
    })
  }

  getSelectedCheckboxes(checkboxesDropdown) {
    const selections = checkboxesDropdown.reduce((selectedValues, item) => {
      if (item.model) {
        selectedValues.push(item.id)
      }
      return selectedValues;
    }, []);
    return selections;
  }

  initProgrammesDropdown() {
    let selections = [];
    let stateParams = this.navigationService.getCurrentStateParams();
    if (stateParams.programmeId) {
      selections.push(stateParams.programmeId);
    } else {
      selections = (this.sessionService.getProjectsFilterState() || {}).programmes || [];
    }

    let programmeDropdown = filter(this.allProgrammes, (p) => {
      return p.status != 'Abandoned';
    });

    this.programmesDropdown = map(programmeDropdown, p => {
      return {
        id: p.id,
        label: p.name,
        model: selections.length ? selections.indexOf(p.id) !== -1 : p.model
      }
    });
  }

  initTemplatesDropdown() {
    let selections = [];
    let stateParams = this.navigationService.getCurrentStateParams();
    if (stateParams.templateId) {
      selections.push(stateParams.templateId);
    } else {
      selections = (this.sessionService.getProjectsFilterState() || {}).templates || [];
    }

    let templates = [];
    this.allProgrammes.forEach(programme => {
      if (programme.status !== 'Abandoned') {
        programme.templates.forEach(template => {
          templates.push(template);
        });
      }
    });

    templates = sortBy(uniqBy(templates, 'id'), 'name');

    this.templatesDropdown = map(templates, t => {
      return {
        id: t.id,
        label: t.name,
        model: selections.length ? selections.indexOf(t.id) !== -1 : t.model
      }
    });
  }

  select(searchOption) {
    this.searchTextModel = null;
    this.selectedSearchOption = searchOption;
  }

  search () {
    this.setSearchParams({
      [this.selectedSearchOption.name]: this.searchTextModel
    });
    let stateParams = this.navigationService.getCurrentStateParams();
    this.navigationService.goToCurrentUiRouterState(stateParams, {reload: true});
  }

  clearFiltersAndSearch() {
    this.sessionService.clearProjectsState();
    this.clearSearch();
  }

  getStateParam(status, substatus?) {
    return substatus ? `${status}:${substatus}` : status;
  }

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
  }

  getSelectedStatuses() {
    let states = [];
    let filteredList = [];
    forEach(this.filterDropdownItems, (statusCheckbox) => {
      if (statusCheckbox.model) {
        states = states.concat(this.getStateParams(statusCheckbox))
      }
    });
    return states;
  }

  hasStateCheckboxFilterChanged() {
    return this.filterDropdownItems.some(checkbox => {
      let defaultModel = find(this.defaultFilter, {name: checkbox.name}).model;
      return defaultModel != checkbox.model;
    });
  }

  onProgrammeSelected() {
    // deselect all templates
    forEach(this.templatesDropdown, templateDropdown => { templateDropdown.model = false });

    // selecting programmes should result in associated templates to be selected
    const selectedProgrammes = this.getSelectedCheckboxes(this.programmesDropdown);

    let templatesToBeSelected = [];
    forEach(this.allProgrammes, (programme) => {
      if (includes(selectedProgrammes, programme.id)) {
        forEach(programme.templates, (template) => {
          templatesToBeSelected.push(template.id);
        });
      }
    });

    forEach(this.templatesDropdown, (templateDropdown) => {
      if (includes(templatesToBeSelected, templateDropdown.id)) {
        templateDropdown.model = true;
      }
    });

    // this will trigger the reevaluation of the component, and will result in "Filter applied" / "Deselect all" to be displayed
    this.templatesDropdown = cloneDeep(this.templatesDropdown);

    this.getProjects(true);
  };

  onWatchedCheckboxClick() {
    this.getProjects(true);
  }

  getProjects(showFirstPage?) {
    let projectsSearchState = this.getSearchParams();
    let page = showFirstPage ? 0 : this.currentPage - 1;

    let states = this.getSelectedStatuses();
    const selectedProgrammes = this.getSelectedCheckboxes(this.programmesDropdown);
    const selectedTemplates = this.getSelectedCheckboxes(this.templatesDropdown);
    this.isDefaultFilterState = !this.hasStateCheckboxFilterChanged();
    this.isDefaultProgrammeState = !selectedProgrammes.length;
    this.isDefaultTemplateState = !selectedTemplates.length;

    this.projectService.getProjects(
      projectsSearchState.title,
      projectsSearchState.organisationName,
      projectsSearchState.programmeName,
      projectsSearchState.assignee,
      states,
      this.selectedSearchOption.name === this.byProgrammeOption.name ? null : selectedProgrammes,
      selectedTemplates,
      this.watchingProject,
      page)
      .subscribe((rsp: any) => {
        if (showFirstPage) {
          this.currentPage = 1;
        }
        this.projects = rsp.content;
        this.totalItems = rsp.totalElements;

        forEach(this.projects, (project) => {
          if (!project.fullStatus) {
            this.setFullStatus(project);
          }
        });
        this.updateAllSelectedCheckBoxState();
        this.saveProjectStatusesToCache();
        this.saveProgrammesToCache();
        this.loadingMaskService.showLoadingMask(false);
      }, err => {
        this.loadingMaskService.showLoadingMask(false);
      });
  }

  /**
   * Create a new project handler
   */
  createNewProject() {
    if (!this.enabledProgrammes.length) {
      this.confirmationDialogService.show({message:'There are currently no programmes available.',
        approveText:'Ok', showDismiss:false, showIcon:false})
    } else {
      this.navigationService.goToUiRouterState('projects-new', {
        programmes: this.enabledProgrammes
      });
    }
  }

  /**
   * Open project
   */
  goToProjectOverview(id) {
    this.navigationService.goToUiRouterState('project-overview', {
      projectId: id,
      backNavigation: {
        name: 'projects'
      }
    });
  }

  onAllCheckboxChange () {
    forEach(this.projects, (project) => {
      project.isSelected = this.allSelected;
    });
  }

  onProjectCheckboxClick() {
    this.updateAllSelectedCheckBoxState();
  }

  updateAllSelectedCheckBoxState() {
    const trueCount = groupBy(this.projects, 'isSelected').true;
    this.allSelected = trueCount && trueCount.length === this.projects.length;
  }

  setToAssess() {
    const ids = [];
    map(this.projects, (project) => {
      if (project.isSelected) {
        ids.push(project.id);
      }
    });

    if (ids.length) {
      this.confirmationDialogService.show(
        {
          message: 'Are you sure you want to set the selected projects with a status of Submitted to Assess?<br>Only projects with a current status of Submitted can be set to Assess status.',
          approveText: 'SET TO ASSESS',
          dismissText: 'CANCEL'
        }
      ).result.then(() => {
        return this.projectService.projectBulkOperation(ids, this._operationsConst.assess).subscribe((resp: any) => {
          const data = resp;
          if (data.successCount === ids.length) {
            this.toastrUtilService.success(`${data.successCount} project${data.successCount > 1 ? '(s)' : ''} successfully set to Assess status`);
          } else if (data.failureCount === ids.length) {
            this.toastrUtilService.warning('No projects have been updated');
          } else {
            this.toastrUtilService.warning(`${data.successCount} project${data.successCount > 1 ? '(s)' : ''} successfully set to Assess status and ${data.failureCount} not updated`);
          }
          this.getProjects();
        });
      });
    }
  }

  transfer() {
    const projects = [];
    map(this.projects, (project) => {
      if (project.isSelected) {
        projects.push(project);
      }
    });

    if (projects.length) {
      const modal = this.ngbModal.open(ProjectTransferModalComponent);
      modal.componentInstance.projects = projects;
      // modal.componentInstance.assignableUsers = assignableUsers;
      modal.result.then(() => {
        this.getProjects();
      }, ()=>{});
    }
  }

  assign() {
    let selectedProjects = filter(this.projects, {isSelected: true});
    let projectIds = selectedProjects.map(p => p.id)

    let assignableUsers = []
    this.projectService.getAssignableUsers(projectIds).subscribe(resp => {
      let result = sortBy(resp, (user: any) => {
        return user.lastName.toLowerCase();
      });
      result.forEach((item: any) => {
        assignableUsers.push({
          label: item.firstName + ' ' + item.lastName,
          id: item.username,
          model: this.isUserAssignedToAllSelected(item.username, selectedProjects)
        })
      })
      if (assignableUsers.length > 0) {
        this.showAssignModel(selectedProjects, assignableUsers);
      } else {
        const error = {error:{title: 'No Assignable Users', description: 'There are no users that can be assigned to all of the projects you have selected. Please amend your project selection and try again.'}}
        const modal = this.ngbModal.open(ErrorModalComponent);
        modal.componentInstance.error = error
      }
    });
  }

  unassign() {
    let selectedProjects = filter(this.projects, {isSelected: true});
    let projectIds = selectedProjects.map(p => p.id)
    let assignableUsers = []

    this.projectService.getAssignableUsers(projectIds).subscribe(resp => {
      let result = sortBy(resp, (user: any) => {
        return user.lastName.toLowerCase();
      });
      result.forEach((item: any) => {
        if (this.isUserAssignedToAnySelected(item.username, selectedProjects)) {
          assignableUsers.push({
            label: item.firstName + ' ' + item.lastName,
            id: item.username,
            model: true
          })
        }
      })
      //rare case scenario where users are already assigned but aren't normally assignable
      selectedProjects.forEach(project => {
        let projectAssignableUsers = this.getProjectAssignableUsers(project)
        projectAssignableUsers.forEach(user => {
          if (!assignableUsers.some(u => u.id === user.id)) {
            assignableUsers.push(user)
          }
        })
      })
      this.showUnassignModal(selectedProjects, assignableUsers);
    });
  }

  getProjectAssignableUsers(project: any): any[] {
    let assignableUsers = []
    if (!project.assignee) {
      return assignableUsers
    }
    let assigneeIds = project.assignee.split('|')
    let assigneeNames = project.assigneeName.split('|')
    for (let i in assigneeIds) {
      let id = assigneeIds[i]
      let label = assigneeNames[i]
      assignableUsers.push({id, label, model: true})
    }
    return assignableUsers
  }

  isUserAssignedToAllSelected(username, selectedProjects) {
    //returns false if user is assignee in all selected projects
    for (let project of selectedProjects) {
      let projectAssignee = project.assignee? project.assignee.split('|'): [];
      if (!projectAssignee.includes(username)) {
        return false
      }
    }
    return true
  }

  isUserAssignedToAnySelected(username: string, selectedProjects: any[]) {
    //returns true if user is assigned to any project in the selected
    for (let project of selectedProjects) {
      // console.log(project)
      let projectAssignee = project.assignee? project.assignee.split('|'): [];
      if (projectAssignee.includes(username)) {
        return true
      }
    }
    return false
  }

  showAssignModel(projects, assignableUsers){
    const modal = this.ngbModal.open(ProjectAssignModalComponent);
    modal.componentInstance.projects = projects;
    modal.componentInstance.assignableUsers = assignableUsers;
    modal.result.then(() => {
      this.projectService.assignMultipleProjects(projects).subscribe(()=>{});
    }, ()=>{});
  }

  showUnassignModal(projects, unassignableUsers) {
    const modal = this.ngbModal.open(ProjectAssignModalComponent);
    modal.componentInstance.projects = projects;
    modal.componentInstance.assignableUsers = unassignableUsers;
    modal.componentInstance.unassign = true;
    modal.result.then(() => {
      this.projectService.unassignMultipleProjects(projects.map(p => p.id), projects[0].assigneesToRemove).subscribe(()=>{});
    }, ()=>{});
  }

  showActions() {
    return this.canAssess || this.canTransfer || this.canAssign;
  }

  assessEnabled() {
    if (this.canAssess) {
      return some(this.projects, 'isSelected');
    }
    return false;
  }

  transferEnabled() {
    if (this.canTransfer) {
      return some(this.projects, 'isSelected');
    }
    return false;
  }

  assignEnabled() {
    if (this.canAssign) {
      return size(filter(this.projects, 'isSelected')) > 0;
    }
    return false;
  }

  unassignEnabled() {
    if (this.canAssign) {
      return size(filter(this.projects, p => p.isSelected && !!p.assignee)) > 0
    }
    return false
  }

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
  }

  isAnyFilterApplied() {
    return this.searchText || !this.isDefaultFilterState || !this.isDefaultProgrammeState || !this.isDefaultTemplateState
  }
}
