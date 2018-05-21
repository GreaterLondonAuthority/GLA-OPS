/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

SessionService.$inject = ['$sessionStorage'];

function SessionService($sessionStorage) {
  return {
    setProjectsFilterState(filterState) {
      $sessionStorage.filterState = filterState;
    },
    getProjectsFilterState() {
      return $sessionStorage.filterState || {};
    },
    resetProjectsFilterState() {
      $sessionStorage.filterState = {};
    },

    setProjectsSearchState(state) {
      $sessionStorage.projectsSearchState = state;
    },

    getProjectsSearchState() {
      return $sessionStorage.projectsSearchState || {};
    },

    clearProjectsSearchState() {
      $sessionStorage.projectsSearchState = {};
    },

    clearProjectsState() {
      this.resetProjectsFilterState();
      this.clearProjectsSearchState();
    },

    setDoNotShowAgainDeleteNotificationModal(doNotShow) {
      $sessionStorage.doNotShowAgainDeleteNotificationModal = doNotShow;
    },

    getDoNotShowAgainDeleteNotificationModal() {
      return $sessionStorage.doNotShowAgainDeleteNotificationModal;
    },

    setOrganisationsFilter(orgFilter) {
      $sessionStorage.orgFilter = orgFilter;
    },

    getOrganisationsFilter(){
      return $sessionStorage.orgFilter;
    },



    // Payments
    resetPaymentsFilterState() {
      $sessionStorage.paymentsFilterState = {};
    },

    setPaymentsFilterState(filterState){
      $sessionStorage.paymentsFilterState = filterState;
    },

    getPaymentsFilterState(){
      return $sessionStorage.paymentsFilterState  || {};
    },

    clearPaymentsState(){
      $sessionStorage.paymentsSearchState = {};
    },

    setPaymentsSearchState(state) {
      $sessionStorage.paymentsSearchState = state;
    },

    getPaymentsSearchState() {
      return $sessionStorage.paymentsSearchState || {};
    },

    // Users search
    resetUsersFilterState() {
      $sessionStorage.usersFilterState = {};
    },

    setUsersFilterState(filterState){
      $sessionStorage.usersFilterState = filterState;
    },

    getUsersFilterState(){
      return $sessionStorage.usersFilterState  || {};
    },

    clearUsersState(){
      $sessionStorage.usersSearchState = {};
    },

    setUsersSearchState(state) {
      $sessionStorage.usersSearchState = state;
    },

    getUsersSearchState() {
      return $sessionStorage.usersSearchState || {};
    },



    clear(){
      $sessionStorage.$reset();
    }
  };
}

angular.module('GLA')
  .service('SessionService', SessionService);
