/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './recoverable-grant/recoverableGrant.js'
import './inactive-modal/inactiveModal.js'

class OrganisationCtrl {
  constructor($rootScope, $state, $stateParams, $log, OrganisationService, UserService, ConfirmationDialog, ToastrUtil, SessionService, OrganisationRejectModal, OrganisationInactiveModal) {
    this.$rootScope = $rootScope;
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.OrganisationService = OrganisationService;
    this.UserService = UserService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.OrganisationRejectModal = OrganisationRejectModal;
    this.OrganisationInactiveModal = OrganisationInactiveModal;
    this.ToastrUtil = ToastrUtil;
    this.SessionService = SessionService;
    this.$log = $log;
  }

  $onInit(){
    this.readOnly = true;

    this.setData(this.organisation);
    this.isManagingOrganisation = this.org ? this.org.entityType === 1 : false;
    this.isTeamOrganisation = this.org ? this.org.entityType === 8 : false;
    this.imsNumberLabel = this.OrganisationService.getImsLabel(this.org);
    this.orgEntityName = this.isTeamOrganisation ? 'Team' : 'Organisation';
    this.initDisplayFieldsFlags();

    if (this.organisation && this.organisation.ukprn) {
      this.OrganisationService.countOccuranceOfUkprn(this.organisation.ukprn).then(rsp => this.occuranceOfUkprn = rsp.data);
    }

    let collapsedSectionsByDefault = {
      details: false,
      governance: false,
      programmes: false,
      contracts: false,
      grant: false
    };

    if (this.$stateParams.backNavigation) {
      this.SessionService.setOrganisationPage({backNavigation: this.$stateParams.backNavigation});
    }

    let collapsedSectionsCache = this.SessionService.getCollapsedOrgSections();
    this.collapsedSections = collapsedSectionsCache || collapsedSectionsByDefault;
    this.isLearningProvider = this.OrganisationService.isLearningProvider(this.org);
    this.formatSortCode = this.formatSortCode(this.org.sortCode);
  }

  initDisplayFieldsFlags() {
    this.displayAddress = !this.isTeamOrganisation;
    this.displayRegulatoryInformation = !this.isTeamOrganisation;
    this.displayParentOrganisation = !this.isTeamOrganisation;
    this.displaySapId = !this.isTeamOrganisation && this.UserService.hasPermission('org.view.vendor.sap.id', this.$state.params.orgId);
    this.displayTeam = !this.isManagingOrganisation && !this.isTeamOrganisation;
    this.displayGLAContact = !this.isTeamOrganisation;
    this.displayAllowRegistrations = this.isManagingOrganisation || this.isTeamOrganisation;
    this.displayAccessToSGW = this.isManagingOrganisation || this.isTeamOrganisation;
  }

  onCollapseChange() {
    this.SessionService.setCollapsedOrgSections(this.collapsedSections);
  }

  refreshDetails() {
    this.$rootScope.showGlobalLoadingMask = true;
    return this.$state.go(this.$state.current, this.$stateParams, {reload: true}).then(()=>{
      this.$rootScope.showGlobalLoadingMask = false;
    });
  }


  setData(apiData) {
    this.org = apiData;
    this.users = apiData.users;

    this.editable = (this.org.allowedActions || []).indexOf('EDIT') != -1;
    let canEditGovernance = this.UserService.hasPermission('org.edit.governance', this.org.id);
    this.editableGovernance = canEditGovernance && (this.org.allowedActions || []).indexOf('EDIT') != -1;


    //filter role matching this organisation
    this.users = _.each(this.users, user => {
      user.currentOrgRole = _.find(user.roles, {
        organisationId: this.org.id
      });
    });
  }


  edit(section) {
    this.$state.go('organisation.edit', {
      orgId: this.org.id,
      orgDetails: this.org,
      section: section
    });
  }

  approveOrg() {
    let isInactive = this.org.status === 'Inactive';
    let message;

    if(isInactive){
      message = `Are you sure you want this organisation to be active?`;
    } else{
      let users = this.users.filter(u => u.currentOrgRole.name === 'ROLE_ORG_ADMIN').map(u => `${u.firstName} ${u.lastName} (${u.username})`).join()
      message = `<p>${users} has requested this organisation registration.</p>
                 <p>Approving the registration will approve the user and assign them the Org Admin role.</p>`;
    }

    let modal = this.ConfirmationDialog.show({
      message: message,
      approveText: 'APPROVE',
      showDismiss: false,
      info: true
    });

    modal.result
      .then(() => {
        this.$rootScope.showGlobalLoadingMask = true;
        this.OrganisationService.approveOrganisation(this.org.id).then((resp) => {
          this.refreshDetails().then(()=>{
            this.ToastrUtil.success(isInactive? 'Organisation approved' : 'Organisation & Org Admin approved');
          });
        });
      });
  }

  rejectOrg() {
    let modal = this.OrganisationRejectModal.show();
    modal.result.then((params) => {
      this.$rootScope.showGlobalLoadingMask = true;

      // update here
      this.OrganisationService.rejectOrganisation(this.org.id, params.reason.value, params.reasonText).then((resp) => {
        this.refreshDetails().then(()=>{
            this.ToastrUtil.success('Organisation & Org Admin rejected');
        });
      });
    });
  }

  inactivateOrganisation(){
    let modal = this.OrganisationInactiveModal.show();

    modal.result
      .then((params) => {
        this.$rootScope.showGlobalLoadingMask = true;

        // update here
        this.OrganisationService.inactivateOrganisation(this.org.id, params.reason.value, params.reasonText, params.duplicateOrgId).then((resp) => {
          this.refreshDetails().then(()=>{
            this.ToastrUtil.success('This organisation is now inactive');
          });
        });
      });
  }

  back() {
    let previousState = (this.SessionService.getOrganisationPage() || {}).backNavigation;
    if(previousState && previousState.name){
      this.$state.go(previousState.name, previousState.params);
    } else {
      this.$state.go('organisations');
    }

    this.SessionService.setOrganisationPage(null);
  }

  isApproved(){
    return this.org.status === 'Approved'
  }

  /**
   * Format sort code number to appear with dash
   * example: 11-22-33
   */
  formatSortCode(value) {
    if (value) {
      return (String(value).match(/.{1,2}/g) || []).join('-');
    }
  }

}

OrganisationCtrl.$inject = ['$rootScope', '$state', '$stateParams', '$log', 'OrganisationService', 'UserService', 'ConfirmationDialog', 'ToastrUtil', 'SessionService', 'OrganisationRejectModal', 'OrganisationInactiveModal'];


angular.module('GLA')
  .component('organisationPage', {
    templateUrl: 'scripts/pages/organisation/organisation.html',
    bindings: {
      organisationTypes: '<',
      organisation: '<',
      showUsers: '<?',
      remainingYears: '<',
      showDuplicateOrgAsLink: '<',
      legalStatuses: '<',
    },
    controller: OrganisationCtrl,
  });
