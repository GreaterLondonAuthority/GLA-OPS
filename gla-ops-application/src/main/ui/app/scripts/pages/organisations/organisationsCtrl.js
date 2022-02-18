/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import {OrganisationRequestAccessModalComponent} from '../../../../../gla-ui/src/app/organisation-request-access-modal/organisation-request-access-modal.component'

class OrganisationsCtrl {
  constructor($rootScope, $state, $stateParams, OrganisationService, ToastrUtil, UserService, organisationTypes, SessionService, NotificationsService, watchedOrganisations, ConfirmationDialog, managingOrganisationsTeams, canFilterByTeams, NgbModal) {
    $rootScope.showGlobalLoadingMask = true;
    this.$rootScope = $rootScope;
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.OrganisationService = OrganisationService;
    this.ToastrUtil = ToastrUtil;
    this.UserService = UserService;
    this.SessionService = SessionService;
    this.organisationTypes = organisationTypes;
    this.watchedOrganisations = watchedOrganisations;
    this.NotificationsService = NotificationsService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.canFilterByTeams = canFilterByTeams;
    this.managingOrganisationsTeams= managingOrganisationsTeams;
    this.NgbModal = NgbModal

    this.showFilters = true;
    this.loading = true;
    this.cachedOrgsFilter = SessionService.getOrganisationsFilter();
    //Clear sections cache for specific org selection
    SessionService.setCollapsedOrgSections(null);

    this.initSearchDropdown();
    this.initOrgTypeDropdown();
    this.initOrgStatusDropdown();
    this.initRegistrationsDropdown();
    this.initTeamDropDown();
    this.initCurrentState();

    this.user = UserService.currentUser();
    this.orgCollection = [];
    this.totalItems = 0;
    this.indexStart = 0;
    this.indexEnd = 0;
    this.itemsPerPage = 50;

    //Current page starts by 1 in UI but by 0 in backend
    this.currentPage = 1;
    this.sortByName = 'name';
    this.sortReverse = false;


    this.getOrganisations(false, true);
  }

  select(searchOption) {
    this.searchText = null;
    this.selectedSearchOption = searchOption;
  };

  initSearchDropdown() {
    this.searchOptions = [
      {
        name: 'organisation',
        description: 'By Organisation',
        hint: 'Enter organisation ID or name',
        maxLength: '50'
      },
      {
        name: 'sapVendorId',
        description: 'By SAP ID',
        hint: 'Enter SAP ID',
        maxLength: '50'
      }
    ];
    this.selectedSearchOption = this.searchOptions[0];
    this.searchText = this.$stateParams.searchText || (this.cachedOrgsFilter || {}).searchText;
  }

  initOrgTypeDropdown() {
    const selections = (this.cachedOrgsFilter || {}).orgTypes || [];
    this.orgTypeDropdown = Object.keys(this.organisationTypes).reduce((items, key) => {
      items.push({
        id: key,
        label: this.organisationTypes[key].summary,
        displayOrder: this.organisationTypes[key].displayOrder,
        model: selections.indexOf(key) === -1 ? false : true
      });
      return items;
    }, []);
    this.orgTypeDropdown = _.sortBy(this.orgTypeDropdown, 'displayOrder');
  };

  initOrgStatusDropdown() {
    this.orgStatusDropdownDefaults = [
      {
        status: 'Approved',
        model: false
      },
      {
        status: 'Pending',
        model: false
      },
      {
        status: 'Inactive',
        model: false
      },
      {
        status: 'Rejected',
        model: false
      }
    ];
    const selections = (this.cachedOrgsFilter || {}).orgStatuses || [];
    this.orgStatusDropdown = this.orgStatusDropdownDefaults.map(s => {
      return {
        id: s.status,
        label: s.status,
        model: selections.length ? selections.indexOf(s.status) !== -1 : s.model
      }
    });
  };

  initRegistrationsDropdown() {
    const selections = (this.cachedOrgsFilter || {}).userRegStatuses || [];
    this.userRegStatusDropdown = this.OrganisationService.userRegStatuses(selections);
  }

  initTeamDropDown() {
    this.teamsDropdown = angular.copy(this.managingOrganisationsTeams);
    (this.teamsDropdown || []).forEach(group => {
      (group.items || []).forEach(t => t.collapsed = true);
    });

    const selections = (this.cachedOrgsFilter || {}).teamStatuses || [];
    if(selections.length){
      _.forEach(this.teamsDropdown, (group)=>{
        _.forEach(group.items, team =>{

          let teamStatus = _.find(selections, {
            organisationId: team.organisationId,
            teamId: team.teamId
          });
          if(teamStatus){
            team.model = true;
          }
        })
      });
    }
  }

  initCurrentState() {
    this.currentState = {
      titleForBackBtn: 'ORGANISATIONS',
      name: this.$state.current.name,
      params: angular.copy(this.$state.params)
    };
  }


  getOrganisations(resetPage, initialLoad) {
    var page = resetPage ? 0 : this.currentPage - 1;
    var size = this.itemsPerPage;
    var sort = [this.sortByName + ',' + (this.sortReverse ? 'desc' : 'asc')];
    if (this.sortByName != 'name') {
      sort.push('name,asc');
    }

    if (this.user.approved) {
      const orgTypes = this.getSelectedCheckboxes(this.orgTypeDropdown);
      const orgStatuses = this.getSelectedCheckboxes(this.orgStatusDropdown);
      const teamStatuses = [];
      _.forEach(this.teamsDropdown, (group)=>{
        _.forEach(group.items, team =>{
          if(team.model){
            teamStatuses.push({
              organisationId: team.organisationId,
              teamId: team.teamId
            });
          }
        })
      });

      const isOrgStatusesChanged = this.orgStatusDropdownDefaults.some(s => {
        let isChecked = orgStatuses.indexOf(s.status) !== -1;
        return s.model != isChecked;
      });

      const userRegStatuses = this.getSelectedCheckboxes(this.userRegStatusDropdown);
      this.showReset = orgTypes.length || isOrgStatusesChanged || userRegStatuses.length || teamStatuses.length || this.searchText;


      this.updateBrowserUrl();
      this.OrganisationService.retrieveAll(page, size, sort, userRegStatuses, this.selectedSearchOption.name, this.searchText, orgTypes, orgStatuses, teamStatuses).then(response => {
        this.lastSearchText = this.searchText;
        this.$rootScope.showGlobalLoadingMask = false;
        this.loading = false;
        if (resetPage) {
          this.currentPage = 1;
        }
        this.orgCollection = response.data.content;
        if (initialLoad && !this.searchText) {
          this.showFilters = this.orgCollection.length > 0 || (this.cachedOrgsFilter || {}).showFilters;
        }

        this.SessionService.setOrganisationsFilter({
          showFilters: this.showFilters,
          searchText: this.searchText,
          orgTypes,
          orgStatuses,
          userRegStatuses,
          teamStatuses
        });

        this.totalItems = response.data.totalElements;
      });
    } else {
      this.$rootScope.showGlobalLoadingMask = false;
      this.showFilters = false;
    }
  }


  getSelectedOrgTypes() {
    const orgTypes = this.orgTypeDropdown.reduce((selectedTypes, item) => {
      if (item.model) {
        selectedTypes.push(item.id)
      }
      return selectedTypes;
    }, []);
    return orgTypes;
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

  search() {
    this.getOrganisations(true);
  }

  clearSearchText() {
    this.searchText = null;
    this.getOrganisations(true);
  }

  clearAll() {
    this.resetSearch = true;
    this.cachedOrgsFilter = null;
    this.SessionService.setOrganisationsFilter(null);
    this.searchText = null;
    this.initOrgTypeDropdown();
    this.initOrgStatusDropdown();
    this.initRegistrationsDropdown();
    this.initTeamDropDown();
    this.getOrganisations(true);
  }

  sortBy(columnName) {
    console.log('sorting');
    this.sortReverse = (this.sortByName === columnName) ? !this.sortReverse : false;
    this.sortByName = columnName;
    this.currentPage = 1;
    this.getOrganisations();
  }

  requestOrganisationAccess() {
    let modal = this.NgbModal.open(OrganisationRequestAccessModalComponent)
    modal.result.then(orgCode => {
      if (orgCode) {
        this.OrganisationService.linkUserToOrganisation(orgCode, this.user.username).then(() => {
          this.ToastrUtil.success('Your request was sent successfully. You will be notified on approval.');
        });
      }
    })
  }

  updateBrowserUrl() {
    this.$state.go(this.$state.current, {searchText: this.searchText}, {notify: false});
  }

  toggleWatch(organisation) {

    if(this.watchedOrganisations[organisation.id]){
      const modal = this.ConfirmationDialog.show({
        title: 'Stop watching this organisation',
        message: 'By selecting to stop watching this organisation you will cease to receive all relevant notifications for this organisation.',
        approveText: 'STOP WATCHING ORGANISATION',
        dismissText: 'CANCEL'
      });

      modal.result
        .then(() => {
          this.NotificationsService.unwatchOrganisation(this.UserService.currentUser().username, organisation.id).then(() => {
            this.$state.reload();
          });
        });
    } else {
      const modal = this.ConfirmationDialog.show({
        title: 'Watch Organisation',
        message: 'By selecting to watch a organisation you will receive all relevant notifications for this organisation.',
        approveText: 'WATCH ORGANISATION',
        dismissText: 'CANCEL'
      });

      modal.result
        .then(() => {
          this.NotificationsService.watchOrganisation(this.UserService.currentUser().username, organisation.id).then(() => {
            this.$state.reload();
          });
        });
    }


  }
}

OrganisationsCtrl.$inject = ['$rootScope', '$state', '$stateParams', 'OrganisationService', 'ToastrUtil', 'UserService', 'organisationTypes', 'SessionService', 'NotificationsService', 'watchedOrganisations', 'ConfirmationDialog', 'managingOrganisationsTeams', 'canFilterByTeams', 'NgbModal'];


angular.module('GLA')
  .controller('OrganisationsCtrl', OrganisationsCtrl);
