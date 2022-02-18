/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class PendingUsersCtrl {
  constructor($state, $rootScope, UserService, UserAccountService, SessionService, $q, OrganisationService, ErrorService) {
    this.$state = $state;
    this.$rootScope = $rootScope;
    this.UserService = UserService;
    this.UserAccountService = UserAccountService;
    this.SessionService = SessionService;
    this.$q = $q;
    this.OrganisationService = OrganisationService;
    this.ErrorService = ErrorService;
  }

  $onInit() {
    this.isGLASearch = this.UserService.hasPermission('users.search.gla');
    this.requestsQueue = [];

    // Pagination variables
    this.totalItems = 0;
    this.itemsPerPage = 50;

    //Current page starts by 1 in UI but by 0 in backend
    this.currentPage = 1;
    this.updateFilters(true);
    this.initialised = true;

    this.showAuthorisedSignatory = this.UserService.hasPermission('users.assign.signatory');
    this.authorisedSignatoryTooltip = this.UserService.getAuthorisedSignatoryTooltip();
  }

  $onChanges(changes) {
    if (this.initialised) {
      this.updateFilters(true)
    }
  }


  goToUserProfile(user){
    this.$state.go('user-account', {userId:user.userId});
  }

  updateFilters(resetPage){
    let data = {};
    if (this.searchType === 'username' || !this.searchType) {
      data.username = this.searchText;
    } else {
      data.organisation = this.searchText;
    }

    data.registrationStatus = ['Pending'];

    data.page = resetPage ? 0 : this.currentPage - 1;

    this.getPendingUsers(data, resetPage);
  }

  getPendingUsers(data, resetPage){
    this.$q.all(this.requestsQueue).then(() => {
      let p = this.UserService.getUserRoles(data).then(rsp => {
        this.dataLoaded = true;
        this.users = rsp.data.content;
        this.users.forEach((u)=>{
          u.isEditable = this.UserService.hasPermission('org.edit.details', u.organisationId);
        });
        if (resetPage) {
          this.currentPage = 1;
        }
        this.totalItems = rsp.data.totalElements;
        this.$rootScope.showGlobalLoadingMask = false;
      });
      this.requestsQueue.push(p);
      p.finally(() => _.remove(this.requestsQueue, p));
    })
  }

  canChangeAuthorisedSignatory(role){
    return this.UserService.canChangeAuthorisedSignatory(role)
  }

  signatoryChange(organisationId, roleName, signatory) {
    this.UserService.updateAuthorisedSignatory(this.userProfile.username, organisationId, roleName, signatory);
  }

  approveUser(roleSummary) {
    this.$rootScope.showGlobalLoadingMask = true;
    this.OrganisationService.approveUser(roleSummary.organisationId, roleSummary.username, roleSummary.newRole, roleSummary.authorisedSignatory)
      .then(resp => {
        this.updateFilters(true);
      })
      .catch(this.ErrorService.apiValidationHandler())
  }

  rejectUser(roleSummary) {
    this.UserService.getUserProfile(roleSummary.username).then(rsp => {
      let user = rsp.data;
      let orgRole = _.find(user.organisations, {orgId: roleSummary.organisationId, roleName: roleSummary.role});
      this.UserAccountService.deleteUser(orgRole, user).then(() => {
        this.updateFilters(true);
      });
    });
  }
}

PendingUsersCtrl.$inject = ['$state', '$rootScope', 'UserService', 'UserAccountService', 'SessionService', '$q', 'OrganisationService', 'ErrorService'];

angular.module('GLA').controller('PendingUsersCtrl', PendingUsersCtrl);

angular.module('GLA')
  .component('pendingUsersList', {
    templateUrl: 'scripts/pages/users/pending-users-list/pendingUsersList.html',
    bindings: {
      organisationTypes: '<',
      searchText: '<',
      searchType: '<'
    },
    controller: PendingUsersCtrl
  });
