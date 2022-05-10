/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

SessionService.$inject = ['$sessionStorage', 'GlaSessionService'];

function SessionService($sessionStorage, GlaSessionService) {
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
      GlaSessionService.setDoNotShowAgainDeleteNotificationModal(doNotShow);
    },

    getDoNotShowAgainDeleteNotificationModal() {
      return GlaSessionService.getDoNotShowAgainDeleteNotificationModal();
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

    setUsersPage(state) {
      $sessionStorage.usersPage = state;
    },

    getUsersPage() {
      return $sessionStorage.usersPage;
    },

    // TODO move to be a property of the users page session object
    // Users search
    resetUsersFilterState() {
      $sessionStorage.usersFilterState = {};
    },

    setUsersFilterState(filterState){
      $sessionStorage.usersFilterState = filterState;
    },

    // TODO move to be a property of the users page session object
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

    setTemplateBlock(state) {
      $sessionStorage.templateBlock = state;
    },

    getTemplateBlock() {
      return $sessionStorage.templateBlock;
    },

    setOrgRegistration(state) {
      GlaSessionService.setOrgRegistration(state);
    },

    getOrgRegistration() {
      return GlaSessionService.getOrgRegistration();
    },

    //TODO remove
    setProjectOverview(state) {
      $sessionStorage.projectOverview = state;
    },

    //TODO remove
    getProjectOverview() {
      return $sessionStorage.projectOverview;
    },


    setAssessmentPage(state) {
      $sessionStorage.assessmentPage = state;
    },

    getAssessmentPage() {
      return $sessionStorage.assessmentPage;
    },

    setOrganisationPage(state) {
      $sessionStorage.organisationPage = state;
    },

    getOrganisationPage() {
      return $sessionStorage.organisationPage
    },

    clear(){
      $sessionStorage.$reset();
      GlaSessionService.clear();
    }
  };
}

angular.module('GLA')
  .service('SessionService', SessionService);
