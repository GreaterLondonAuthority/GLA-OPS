/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class UsersCtrl {
  constructor($state, $stateParams, UserService, SessionService) {
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.SessionService = SessionService;
    this.UserService = UserService;
    this.searchOptions = UserService.searchOptions();
    this.registrationStatusOptions = this.applyFilterState(UserService.getRegistrationStatusOptions());
    this.isGLASearch = UserService.hasPermission('users.search.gla');
    this.userRoleOptions = this.applyFilterState(UserService.getUserRoleOptions(this.isGLASearch));
    this.organisationTypeOptions = this.applyFilterState(UserService.getOrganisationTypesOptions(this.organisationTypes));
    this.spendAuthorityOptions= this.applyFilterState(UserService.getSpendAuthorityOptions(this));
    let usersSearchState = this.getSearchParams();
    if (usersSearchState.organisation) {
      this.selectedSearchOption = this.searchOptions[1];
      this.searchTextModel = usersSearchState.organisation;
    }
    else {
      this.selectedSearchOption = this.searchOptions[0];
      this.searchTextModel = usersSearchState.username;
    }
    this.searchText = this.searchTextModel;
    //
    this.updateFilters(true);
  }

  $onInit() {
  }

  select(searchOption) {
    this.searchTextModel = null;
    this.selectedSearchOption = searchOption;
  }

  search() {
    this.setSearchParams({
      [this.selectedSearchOption.name]: this.searchTextModel
    });
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
  }

  clearSearch() {
    this.setSearchParams({});
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
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

  getSearchParams(){
    if(this.hasUrlParameter()){
      return this.$stateParams;
    }
    return this.SessionService.getUsersSearchState();
  }

  goToUserProfile(user){
    this.$state.go('user-account', {userId:user.username});
  }

  hasUrlParameter(){
    return Object.keys(this.$stateParams).some(key => this.$stateParams[key]);
  }

  saveFilterState(filterDropdownItems) {
    let filterState = {};
    _.forEach(filterDropdownItems, (filter) => {
      filterState[filter.name] = filter.model;
    });
    this.SessionService.setUsersFilterState(filterState);
  };

  applyFilterState(filterDropdownItems) {
    let filterState = this.SessionService.getUsersFilterState();
    _.forEach(filterDropdownItems, (filter) => {
      filter.model = _.isBoolean(filterState[filter.name]) ? filterState[filter.name] : false;
    });
    return filterDropdownItems;
  };


  updateFilters(resetPage){

    this.isDefaultFilterState = !(
      _.some(this.registrationStatusOptions, {model: true}) ||
      _.some(this.userRoleOptions, {model: true}) ||
      _.some(this.organisationTypeOptions, {model: true}) ||
      _.some(this.spendAuthorityOptions, {model: true})
    );

    this.saveFilterState(_.concat(
      this.registrationStatusOptions,
      this.userRoleOptions,
      this.organisationTypeOptions,
      this.spendAuthorityOptions
    ));


    let data = {};

    if(this.selectedSearchOption.name === 'username'){
      data.username = this.searchTextModel;
    } else {
      data.organisation = this.searchTextModel;
    }

    data.registrationStatus = _.map(_.filter(this.registrationStatusOptions, {model:true}), 'key') || [];
    data.userRoles = _.map(_.filter(this.userRoleOptions, {model:true}), 'key') || [];
    data.organisationTypes = _.map(_.filter(this.organisationTypeOptions, {model:true}), 'key') || [];
    data.spendAuthority = _.map(_.filter(this.spendAuthorityOptions, {model:true}), 'key') || [];

    this.UserService.getUsers(data).then(rsp => {
      this.users = rsp.data.content;
    });
  }

}

UsersCtrl.$inject = ['$state', '$stateParams', 'UserService', 'SessionService'];

angular.module('GLA').controller('UsersCtrl', UsersCtrl);

angular.module('GLA')
  .component('usersPage', {
    templateUrl: 'scripts/pages/users/users.html',
    bindings: {
      organisationTypes: '<'
    },
    controller: UsersCtrl
  });
