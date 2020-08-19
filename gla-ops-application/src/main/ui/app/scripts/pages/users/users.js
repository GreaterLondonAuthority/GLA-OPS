/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './approved-users-list/approvedUsersList.js'
import './pending-users-list/pendingUsersList.js'

class UsersCtrl {
  constructor($state, $stateParams, UserService, SessionService, $q) {
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.SessionService = SessionService;
    this.UserService = UserService;
    this.$q = $q;
  }

  $onInit() {
    this.tabs = {
      approvedUsers: 0,
      pendingUsers: 1
    };

    let pageSession = this.SessionService.getUsersPage() || {};

    this.activeTabIndex = pageSession.activeTabIndex || this.tabs.approvedUsers;

    this.searchOptions = this.UserService.searchOptions();
    let usersSearchState = this.SessionService.getUsersSearchState();
    this.requestsQueue = [];

    // Pagination variables
    this.totalItems = 0;
    this.itemsPerPage = 50;

    //Current page starts by 1 in UI but by 0 in backend
    this.currentPage = 1;
    this.sortByName = 'name';
    this.sortReverse = false;


    if (usersSearchState.organisation) {
      this.selectedSearchOption = this.searchOptions[1];
      this.searchTextModel = usersSearchState.organisation;
    } else {
      this.selectedSearchOption = this.searchOptions[0];
      this.searchTextModel = usersSearchState.username;
    }
    this.searchText = this.searchTextModel;
    this.searchType = this.selectedSearchOption.name;
  }

  select(searchOption) {
    this.searchTextModel = null;
    this.selectedSearchOption = searchOption;
  }

  search() {
    this.SessionService.setUsersSearchState({
      [this.selectedSearchOption.name]: this.searchTextModel
    });

    this.searchText = this.searchTextModel;
    this.searchType = this.selectedSearchOption.name;
  }

  clearSearch() {
    this.SessionService.setUsersSearchState({});
    //TODO this.$onInit?
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
  }

  clearFiltersAndSearch() {
    this.SessionService.resetUsersFilterState();
    this.SessionService.clearUsersState();
    this.clearSearch();
  }

  onApprovedUsersTabSelected() {
    this.onActiveTabChange(this.tabs.approvedUsers)
  }

  onPendingUsersTabSelected() {
    this.onActiveTabChange(this.tabs.pendingUsers)
  }

  onActiveTabChange(tabIndex) {
    this.activeTabIndex = tabIndex;
    let pageSession = this.SessionService.getUsersPage() || {};
    pageSession.activeTabIndex = tabIndex;
    this.SessionService.setUsersPage(pageSession);
  }
}

UsersCtrl.$inject = ['$state', '$stateParams', 'UserService', 'SessionService', '$q'];

angular.module('GLA').controller('UsersCtrl', UsersCtrl);

angular.module('GLA')
  .component('usersPage', {
    templateUrl: 'scripts/pages/users/users.html',
    bindings: {
      organisationTypes: '<'
    },
    controller: UsersCtrl
  });
