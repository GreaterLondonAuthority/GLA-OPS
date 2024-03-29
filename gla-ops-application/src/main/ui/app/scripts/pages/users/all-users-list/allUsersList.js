/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class AllUsersCtrl {
    constructor($state, $stateParams, UserService, UserAccountService,  SessionService, OrganisationService, ToastrUtil, AddUserToTeamModal,  $q) {
      this.$state = $state;
      this.$stateParams = $stateParams;
      this.SessionService = SessionService;
      this.UserService = UserService;
      this.ToastrUtil = ToastrUtil;
      this.UserAccountService = UserAccountService;

      this.OrganisationService = OrganisationService;
      this.userProfile = this.UserService.currentUser();
      this.AddUserToTeamModal = AddUserToTeamModal;
      this.canAddTeams = this.UserService.hasPermission('team.add');

      this.$q = $q;
    }

    $onInit() {
      this.userStatusOptions = this.applyFilterState(this.UserService.getUserStatusOptions());
      this.isGLASearch = this.UserService.hasPermission('users.search.gla');
      this.userRoleOptions = this.applyFilterState(this.UserService.getUserRoleOptions(this.isGLASearch));
      this.organisationTypeOptions = this.applyFilterState(this.UserService.getOrganisationTypesOptions(this.organisationTypes));
      this.spendAuthorityOptions = this.applyFilterState(this.UserService.getSpendAuthorityOptions(this));
      this.showAuthorisedSignatory = this.showSignatory && this.UserService.hasPermission('users.assign.signatory');
      this.authorisedSignatoryTooltip = this.UserService.getAuthorisedSignatoryTooltip();
      this.users = [];
      this.requestsQueue = [];
      this.filterOptionsAllowed = {
        orgType: this.isGLASearch,
        role: true,
        userStatus: true,
        spendAuthority: this.isGLASearch
      }

      // Pagination variables
      this.totalItems = 0;
      this.itemsPerPage = 50;

      //Current page starts by 1 in UI but by 0 in backend
      this.currentPage = 1;

      this.updateFilters(true);
      this.initialised = true;
      this.getTeams();
      this.setFilterOptions();
    }

    setFilterOptions() {
      if (!this.userTypes.includes('Approved')) {
        this.filterOptionsAllowed.orgType = false
        this.filterOptionsAllowed.role = false
        this.filterOptionsAllowed.spendAuthority = false
      }
    }

    getTeams() {
      if (this.isGLASearch) {
        return this.OrganisationService.getManagingOrganisationsTeams().then((resp) => {
          this.teams = _.remove(resp.data, function(r) { return r.teamId != undefined;});
        });
      } else {
        this.teams = [];
      }
    }

    $onChanges(changes) {
      if (this.initialised) {
        this.updateFilters(true)
      }
    }

    clearFiltersAndSearch() {
      this.onClear();
    }

    goToUserProfile(user) {
      this.$state.go('user-account', {userId: user.userId});
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
      filterDropdownItems = _.sortBy(filterDropdownItems, 'displayOrder');
      return filterDropdownItems;
    };


    updateFilters(resetPage) {

      this.isDefaultFilterState = !(
        _.some(this.userStatusOptions, {model: true}) ||
        _.some(this.userRoleOptions, {model: true}) ||
        _.some(this.organisationTypeOptions, {model: true}) ||
        _.some(this.spendAuthorityOptions, {model: true})
      );

      this.saveFilterState(_.concat(
        this.userStatusOptions,
        this.userRoleOptions,
        this.organisationTypeOptions,
        this.spendAuthorityOptions
      ));


      let data = {};

      if (this.searchType === 'username' || !this.searchType) {
        data.username = this.searchText;
      } else {
        data.organisation = this.searchText;
      }

      data.userStatus = _.map(_.filter(this.userStatusOptions, {model: true}), 'key') || [];
      data.userRoles = _.map(_.filter(this.userRoleOptions, {model: true}), 'key') || [];
      data.organisationTypes = _.map(_.filter(this.organisationTypeOptions, {model: true}), 'key') || [];
      data.spendAuthority = _.map(_.filter(this.spendAuthorityOptions, {model: true}), 'key') || [];
      data.registrationStatus = this.userTypes

      data.page = resetPage ? 0 : this.currentPage - 1;

      this.getUsers(data, resetPage);
    }

    getUsers(data, resetPage) {
      this.$q.all(this.requestsQueue).then(() => {
        let p = this.UserService.getUsers(data).then(rsp => {
          this.dataLoaded = true;

          this.userOrgs = [];

          this.users = rsp.data.content;

          this.users.forEach((user) => {
            let orgIds = _.sortBy(_.uniqBy(user.roles, 'organisationId'), 'orgName').map(role => role.organisationId);
            user.userOrgs = [];
            this.updateAllSelectedCheckBoxState();

            if (orgIds.length === 0) {
              const dummyRole = {
                approved: false,
                canHaveThreshold: false,
                entityType: 'N/A',
                userId: user.userId,
                roleDescription: 'No Roles'
              }
              const dummyOrg = {
                id: 0,
                name: 'No Organisations',
                entityType: 'N/A',
                roles: [dummyRole],
                user: user,
                userRow: true
              }
              user.userOrgs.push(dummyOrg)
              user.roles.push(dummyRole)
            }

            orgIds.forEach((orgId, orgIndex) => {
              let roles = this.getOrgRoles(user, orgId);
              user.userOrgs.push({
                id: orgId,
                name: (roles[0] || {}).orgName,
                entityType: (roles[0] || {}).entityType,
                roles: roles,
                user: user,
                userRow: orgIndex === 0
              });
            });
          });

          if (resetPage) {
            this.currentPage = 1;
          }
          this.totalItems = rsp.data.totalElements;

        });
        this.requestsQueue.push(p);
        p.finally(() => _.remove(this.requestsQueue, p));
      })
    }

    getOrgRoles(user, orgId) {
      if (!user || !orgId) {
        return [];
      }

      let roles = _.filter(user.roles, {organisationId: orgId});

      return _.sortBy(roles, 'roleDescription');
    }

    canHaveThreshold(roles) {
      return _.some(roles, {canHaveThreshold: true});
    }

    onUserCheckboxClick() {
      this.updateAllSelectedCheckBoxState();
    };

    updateAllSelectedCheckBoxState() {
      const trueCount = _.groupBy(this.users, 'isSelected').true;
      this.allSelected = trueCount && trueCount.length === this.users.length;
    };

    refresh(editMode){
      this.$state.params.editMode = !!editMode;
      return this.$state.go(this.$state.current, this.$state.params, {reload: true});
    }

    addToTeam() {
      var selectedUsers = _.filter(this.users, {isSelected: true});
      var modal = this.AddUserToTeamModal.show(this.userProfile,  this.teams, selectedUsers );
      modal.result.then(selected => {

        var teamName = selected.selectedTeam.teamName;

        var users = [];
        _.forEach(selectedUsers, (user) => {
          users.push({
            username: user.username,
            requestedRole: user.requestedRole,

          })
        });

        this.UserService.addUsersToTeam(users,selected.selectedTeam.teamId).then(resp =>
          {
            if (resp.data === 1) {
              this.ToastrUtil.success( resp.data + ' user added to ' + teamName);
            } else {
              this.ToastrUtil.success(resp.data + ' users added to ' + teamName);
            }
            this.refresh(true);
          }
        )
      });
    }

    deleteFromTeam($event, role, user) {
      role.orgName = role.orgName;
      role.orgId = role.organisationId;
      role.roleName = role.role;
      this.UserAccountService.deleteFromTeam(role, user).then(()=>{
          this.refresh(true);
      });
      $event.stopPropagation();

    }

    onAllCheckboxChange () {
      _.forEach(this.users, (user) => {
        if (user.hasRoleInManagingOrg) {
          user.isSelected = this.allSelected;
        }
      });
    };

    showActions() {
      return _.some(this.users, 'isSelected');
    }

  }

  AllUsersCtrl.$inject = ['$state', '$stateParams', 'UserService', 'UserAccountService', 'SessionService','OrganisationService', 'ToastrUtil', 'AddUserToTeamModal' , '$q'];

  angular.module('GLA').controller('AllUsersCtrl', AllUsersCtrl);

  angular.module('GLA')
    .component('allUsersList', {
      templateUrl: 'scripts/pages/users/all-users-list/allUsersList.html',
      bindings: {
        organisationTypes: '<',
        showSignatory: '<',
        searchText: '<',
        searchType: '<',
        onClear: '&',
        userTypes: '<'
      },
      controller: AllUsersCtrl
    });
