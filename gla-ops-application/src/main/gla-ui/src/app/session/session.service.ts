import {Injectable} from '@angular/core';
import {SessionStorageService} from "ngx-webstorage";

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  constructor(private sessionStorage:SessionStorageService) {
  }

  setProjectsFilterState(filterState) {
    this.sessionStorage.store('filterState', filterState);
  }

  getProjectsFilterState() {
    return this.sessionStorage.retrieve('filterState') || {};
  }

  resetProjectsFilterState() {
    this.setProjectsFilterState({});
  }

  setProjectsSearchState(state) {
    this.sessionStorage.store('projectsSearchState', state);
  }

  getProjectsSearchState() {
    return this.sessionStorage.retrieve('projectsSearchState') || {};
  }

  clearProjectsSearchState() {
    this.setProjectsSearchState({});
  }
  //
  // setProgrammesSearchState(state) {
  //   this.sessionStorage.programmeSearchState = state;
  // }
  //
  // getProgrammeSearchState() {
  //   return this.sessionStorage.programmeSearchState || {};
  // }
  //
  // clearProgrammeSearchState() {
  //   this.sessionStorage.programmeSearchState = {};
  // }

  clearProjectsState() {
    this.resetProjectsFilterState();
    this.clearProjectsSearchState();
  }
  //
  setDoNotShowAgainDeleteNotificationModal(doNotShow) {
    this.sessionStorage.store('doNotShowAgainDeleteNotificationModal', doNotShow);
  }

  getDoNotShowAgainDeleteNotificationModal() {
    return this.sessionStorage.retrieve('doNotShowAgainDeleteNotificationModal');
  }
  //
  // setOrganisationsFilter(orgFilter) {
  //   this.sessionStorage.orgFilter = orgFilter;
  // }
  //
  // getOrganisationsFilter() {
  //   return this.sessionStorage.orgFilter;
  // }
  //
  // setAuditHistoryFilter(auditHistoryFilter) {
  //   this.sessionStorage.auditHistoryFilter = auditHistoryFilter;
  // }
  //
  // getAuditHistoryFilter() {
  //   return this.sessionStorage.auditHistoryFilter;
  // }
  //
  // setQuestionsFilter(questionsFilter) {
  //   this.sessionStorage.questionsFilter = questionsFilter;
  // }
  //
  // getQuestionsFilter() {
  //   return this.sessionStorage.questionsFilter;
  // }
  //
  // setTemplatesFilter(templateFilter) {
  //   this.sessionStorage.templateFilter = templateFilter;
  // }
  //
  // getTemplatesFilter() {
  //   return this.sessionStorage.templateFilter;
  // }
  //
  // setCollapsedOrgSections(collapsedOrgSections) {
  //   this.sessionStorage.collapsedOrgSections = collapsedOrgSections;
  // }
  //
  // getCollapsedOrgSections() {
  //   return this.sessionStorage.collapsedOrgSections;
  // }
  //
  //
  // // Payments
  // resetPaymentsFilterState() {
  //   this.sessionStorage.paymentsFilterState = {};
  // }
  //
  // setPaymentsFilterState(filterState) {
  //   this.sessionStorage.paymentsFilterState = filterState;
  // }
  //
  // getPaymentsFilterState() {
  //   return this.sessionStorage.paymentsFilterState || {};
  // }
  //
  // clearPaymentsState() {
  //   this.sessionStorage.paymentsSearchState = {};
  // }
  //
  // setPaymentsSearchState(state) {
  //   this.sessionStorage.paymentsSearchState = state;
  // }
  //
  // getPaymentsSearchState() {
  //   return this.sessionStorage.paymentsSearchState || {};
  // }
  //
  // setUsersPage(state) {
  //   this.sessionStorage.usersPage = state;
  // }
  //
  // getUsersPage() {
  //   return this.sessionStorage.usersPage;
  // }
  //
  // // TODO move to be a property of the users page session object
  // // Users search
  // resetUsersFilterState() {
  //   this.sessionStorage.usersFilterState = {};
  // }
  //
  // setUsersFilterState(filterState) {
  //   this.sessionStorage.usersFilterState = filterState;
  // }
  //
  // // TODO move to be a property of the users page session object
  // getUsersFilterState() {
  //   return this.sessionStorage.usersFilterState || {};
  // }
  //
  // clearUsersState() {
  //   this.sessionStorage.usersSearchState = {};
  // }
  //
  // setUsersSearchState(state) {
  //   this.sessionStorage.usersSearchState = state;
  // }
  //
  // getUsersSearchState() {
  //   return this.sessionStorage.usersSearchState || {};
  // }

  setBannerMessageState(state: any) {
    this.sessionStorage.store('bannerMessageState', state);
  }

  getBannerMessageState() {
    return this.sessionStorage.retrieve('bannerMessageState');
  }

  setTemplateInternalBlock(state:any) {
    this.sessionStorage.store('templateInternalBlock', state);
  }

  getTemplateInternalBlock() {
    return this.sessionStorage.retrieve('templateInternalBlock');
  }

  setTemplateBlock(state) {
    this.sessionStorage.store('templateBlock', state);
  }

  getTemplateBlock() {
    return this.sessionStorage.retrieve('templateBlock');
  }

  setOrgRegistration(state) {
    this.sessionStorage.store('orgRegistration', state);
  }

  getOrgRegistration() {
    return this.sessionStorage.retrieve('orgRegistration');
  }

  setProjectOverview(state) {
    this.sessionStorage.store('projectOverview', state);
  }

  getProjectOverview() {
    return this.sessionStorage.retrieve('projectOverview');
  }
  //
  //
  // setAssessmentPage(state) {
  //   this.sessionStorage.assessmentPage = state;
  // }
  //
  // getAssessmentPage() {
  //   return this.sessionStorage.assessmentPage;
  // }
  //
  // setOrganisationPage(state) {
  //   this.sessionStorage.organisationPage = state;
  // }
  //
  // getOrganisationPage() {
  //   return this.sessionStorage.organisationPage
  // }

  getBlockSessionStorage(blockId: number) {
    return this.sessionStorage.retrieve('' + blockId);
  }

  setBlockSessionStorage(blockId: number, value: any) {
    this.sessionStorage.store('' + blockId, value)
  }

  setConfigListItemsPage(state) {
    this.sessionStorage.store('configListItemsPage', state);
  }

  getConfigListItemsPage() {
    return this.sessionStorage.retrieve('configListItemsPage');
  }

  clear() {
    this.sessionStorage.clear();
  }
}
