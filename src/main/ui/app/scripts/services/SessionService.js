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

    setProgrammesSearchState(state) {
      $sessionStorage.programmeSearchState = state;
    },

    getProgrammeSearchState() {
      return $sessionStorage.programmeSearchState || {};
    },

    clearProgrammeSearchState() {
      $sessionStorage.programmeSearchState = {};
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

    setAuditHistoryFilter(auditHistoryFilter) {
      $sessionStorage.auditHistoryFilter = auditHistoryFilter;
    },

    getAuditHistoryFilter(){
      return $sessionStorage.auditHistoryFilter;
    },

    setQuestionsFilter(questionsFilter) {
      $sessionStorage.questionsFilter = questionsFilter;
    },

    getQuestionsFilter(){
      return $sessionStorage.questionsFilter;
    },

    setTemplatesFilter(templateFilter) {
      $sessionStorage.templateFilter = templateFilter;
    },

    getTemplatesFilter(){
      return $sessionStorage.templateFilter;
    },

    setCollapsedOrgSections(collapsedOrgSections) {
      $sessionStorage.collapsedOrgSections = collapsedOrgSections;
    },

    getCollapsedOrgSections(){
      return $sessionStorage.collapsedOrgSections;
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

    setBannerMessageState(state) {
      $sessionStorage.bannerMessageState = state;
    },

    getBannerMessageState() {
      return $sessionStorage.bannerMessageState;
    },

    setTemplateBlock(state) {
      $sessionStorage.templateBlock = state;
    },

    getTemplateBlock() {
      return $sessionStorage.templateBlock;
    },

    clear(){
      $sessionStorage.$reset();
    }
  };
}

angular.module('GLA')
  .service('SessionService', SessionService);
