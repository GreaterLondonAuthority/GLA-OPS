/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class OrganisationFormCtrl {
  constructor($injector) {
    this.OrganisationService = $injector.get('OrganisationService');
    this.ConfirmationDialog = $injector.get('ConfirmationDialog');
    this.UserService = $injector.get('UserService');
    this.ToastrUtil = $injector.get('ToastrUtil');
    this.$state = $injector.get('$state');
    this.$rootScope = $injector.get('$rootScope');
  }

  $onInit() {
    this.user = this.UserService.currentUser();
    this.orgTypes = [];
    this.organisationTypes = this.organisationTypes || this.$state.$current.locals.globals.organisationTypes;
    this.icons = this.$state.$current.locals.globals.icons;
    this.canEditSapId = this.UserService.hasPermission('org.edit.vendor.sap.id');
    this.canEditParentOrg = this.UserService.hasPermission('org.edit.parent.org');
    this.canEditManagingOrg = this.UserService.hasPermission('org.edit.managing.org');
    this.canEditTeam = this.UserService.hasPermission('org.edit.team');
    this.canEditGlaContact = this.UserService.hasPermission('org.view.glacontact');
    this.canEditOrgType = this.UserService.hasPermission('org.edit.type') && (this.organisation ? this.organisationTypes[this.organisation.entityType] : true);
    this.canEditRegistrationKey = this.UserService.hasPermission('org.edit.registration.key', this.organisation ? this.organisation.id : null);

    this.isManagingOrganisation = this.organisation ? this.organisation.entityType === 1 : false;
    this.managingOrganisations = this.$state.$current.locals.globals.managingOrganisations;
    this.legalStatuses = this.$state.$current.locals.globals.legalStatuses;
    this.isLegalStatusEnabled = this.$state.$current.locals.globals.isLegalStatusEnabled;
    this.originalOrgName = null;
    this.isUniqueOrgName = true;
    this.isValidRegKey = true;
    this.labels = {
      orgType: 'Organisation type',
      orgName: 'Organisation name',
      managingOrg: 'Managing organisation'
    };

    //Used for editing specific sections from readonly org page
    this.visibleSections = {
      details: true,
      governance: true
    };

    this.sectionEdited = this.$state.params.section;
    if (this.sectionEdited) {
      Object.keys(this.visibleSections).forEach(sectionName => {
        this.visibleSections[sectionName] = (sectionName === this.sectionEdited);
      });
    }
    this.onEntityTypeChange(true);
    this.countOccuranceOfUkprn(this.org.ukprn);
    this.originalUkprn = this.org.id? this.org.ukprn : null;
  }

  countOccuranceOfUkprn(ukprn) {
    if (ukprn != null && ukprn.length) {
      this.OrganisationService.countOccuranceOfUkprn(ukprn).then(rsp => {
        this.ukprnOccurances = rsp.data;
      });
    }
  }

  isUkprnNotValid(){
     return this.org.id == null ? this.ukprnOccurances > 0 : (this.ukprnOccurances > 1 || (this.ukprnOccurances == 1 && this.org.ukprn != this.originalUkprn));
  }

  onEntityTypeChange(init) {
    this.imsNumberLabel = this.OrganisationService.getImsLabel(this.org);

    if (!init) {
      if (this.org.entityType == 1) {
        this.isManagingOrganisation = true;
        this.org.registrationAllowed = true;
      } else {
        this.isManagingOrganisation = false;
        this.org.registrationAllowed = false;
      }
    }
  }

  isTechSupportOrganisation() {
    return this.org.techSupportOrganisation;
  }

  techSupportSelected() {
    return this.org.entityType === 5;
  }

  isSmallBusinessSelected() {
    return this.org.entityType === 7;
  }

  isGlaHNL() {
    this.managingOrgTapped = _.find(this.managingOrganisations, {id: this.org.managingOrganisationId}) || {};
    return this.managingOrgTapped.isGlaHNL;
  }

  validateName() {
    let orgName = this.org.name;
    if (!orgName || !orgName.length) {
      this.isUniqueOrgName = true;
    } else if (this.org.id && this.originalOrgName === orgName) {
      this.isUniqueOrgName = true;
    } else {
      this.OrganisationService.isOrganisationNameUnique(orgName, this.org.managingOrganisationId).then(isUnique => {
        this.isUniqueOrgName = isUnique;
      });
    }
  }

  updateManagingOrg() {
    let managingOrgId = this.org.managingOrganisationId;
    if (managingOrgId) {
      this.org.team = null;
      this.loadingTeams = true;
      this.OrganisationService.getOrganisationTeams(managingOrgId).then(rsp => {
        this.teams = rsp.data || [];
      }).finally(() => {
        this.loadingTeams = false;
      })
    } else {
      this.teams = [];
    }
  }

  updateGlaContact() {
    let managingOrgId = this.org.managingOrganisationId;
    if (managingOrgId) {
      this.org.contact = null;
      this.loadingContacts = true;
      this.OrganisationService.getOrganisationUsers(managingOrgId).then(rsp => {
        this.contacts = rsp.data || [];
      }).finally(() => {
        this.loadingContacts = false;
      })
    } else {
      this.contacts = [];
    }

  }


  onManagingOrgSelect() {
     this.org.regulated = null;

    if (this.org.name) {
      this.validateName();
    }
    if (this.canEditTeam) {
      this.updateManagingOrg();
    }

    if (this.canEditGlaContact) {
      this.updateGlaContact();
    }
  }

  isLearningProvider() {
    return this.OrganisationService.isLearningProvider(this.org);
  }

}

export default OrganisationFormCtrl;
