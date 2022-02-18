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
    this.OrganisationSapIdModalService = $injector.get('OrganisationSapIdModalService');
    this.UserService = $injector.get('UserService');
    this.ToastrUtil = $injector.get('ToastrUtil');
    this.$state = $injector.get('$state');
    this.$rootScope = $injector.get('$rootScope');
    this.envVars = this.$rootScope.envVars;
  }

  $onInit() {
    this.user = this.UserService.currentUser();
    this.orgTypes = [];
    this.organisationTypes = this.organisationTypes || this.$state.$current.locals.globals.organisationTypes;
    this.organisationTypesList = this.prepareOrgTypeListDropdown(this.organisationTypes);
    this.icons = this.$state.$current.locals.globals.icons;
    //Ops Admin or Gla Admin - people who can see 'CREATE NEW ORGANISATION' button
    this.isGlaOrOpsAdmin = this.UserService.hasPermission('org.manage.approve');
    this.isGlaOrgAdmin = !this.user.isAdmin && this.isGlaOrOpsAdmin;
    this.canEditName = this.UserService.hasPermission('org.edit.name', (this.organisation || {}).id);
    this.canEditSapId = this.UserService.hasPermission('org.edit.vendor.sap.id');
    this.canViewTeams = this.UserService.hasPermission('team.view');
    this.canEditParentOrg = this.UserService.hasPermission('org.edit.parent.org');
    this.canEditManagingOrg = this.UserService.hasPermission('org.edit.managing.org');
    this.canEditTeam = this.UserService.hasPermission('org.edit.team');
    this.canEditGlaContact = this.UserService.hasPermission('org.view.glacontact');
    this.canEditOrgType = this.UserService.hasPermission('org.edit.type') && (this.organisation ? this.organisationTypes[this.org.entityType] : true);
    this.canEditRegistrationKey = this.UserService.hasPermission('org.edit.registration.key', (this.organisation || {}).id);
    this.isManagingOrganisation = this.organisation ? this.organisation.entityType === 1 : false;
    this.isDeprecatedOrgType = this.organisation ? (this.organisation.entityType === 3 || this.organisation.entityType === 6 || this.organisation.entityType === 7) : false;
    this.managingOrganisations = this.$state.$current.locals.globals.managingOrganisations;
    this.isGlaOrgAdminOfEditedOrg = this.isGlaOrgAdminIn(this.org.id);
    if(this.isGlaOrgAdmin && this.canEditManagingOrg && !this.isGlaOrgAdminOfEditedOrg){
      this.managingOrganisations = this.managingOrganisations.filter(mo => {
        return this.isGlaOrgAdminIn(mo.id);
      });
    }
    let legalStatusMap = this.$state.$current.locals.globals.legalStatuses;
    this.legalStatuses = []
    _.forEach(legalStatusMap, (val, key) => {
      this.legalStatuses.push({ name: key, description: val });
    });
    this.organisationTemplates = this.$state.$current.locals.globals.organisationTemplates;
    this.isLegalStatusEnabled = this.$state.$current.locals.globals.isLegalStatusEnabled;
    this.originalOrgName = null;
    this.isUniqueOrgName = true;
    this.isValidRegKey = true;
    this.labels = {
      orgType: 'Organisation type',
      orgName: 'Organisation name',
      knownAs: 'Also known as (Optional)',
      companyCode: 'Companies House registration number',
      societyNumber: 'Society number',
      isCharityCommission: 'Is the organisation registered with Charity Commission?',
      charityNumber: 'Registered charity number',
      isRegulator: 'Is the organisation registered with the social housing regulator?',
      providerNumber: 'Registered Provider of Social Housing number',
      isLearningProvider: 'Is the organisation a Registered Learning Provider?',
      ukprn: 'UKPRN (Optional)',
      managingOrg: 'Managing organisation'
    };
    this.financeContactTooltipText = 'Separate multiple email addresses with a comma. OPS sends payment details to this'
      + ' address. If it contains someone\'s name rather than a generic'
      + ' address such as \'Finance@something.com\', ensure that they have'
      + ' consented to their email address being provided here.';

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
    this.org.sapIds = this.org.sapIds || [];
    this.onSapIdsChange();
    if (this.canViewTeams) {
      this.getTeams();
    }
  }

  prepareOrgTypeListDropdown(organisationTypes) {
    let organisationTypesList = Object.keys(organisationTypes).map((key) => organisationTypes[key]);
    _.forEach(organisationTypesList, (orgType) => {
      if (orgType.deprecated) {
        orgType.summary = orgType.summary + ' (deprecated)';
      }
    });
    return organisationTypesList;
  }

  isGlaOrgAdminIn(orgId){
    if(!orgId || this.user.isAdmin){
      return false;
    }
    let glaOrgAdminRole = _.find(this.user.roles, {organisationId: orgId, name: 'ROLE_GLA_ORG_ADMIN'});
    return !!glaOrgAdminRole;
  }

  countOccuranceOfUkprn(ukprn) {
    if (ukprn != null && ukprn.length) {
      this.OrganisationService.countOccuranceOfUkprn(ukprn).then(rsp => {
        this.ukprnOccurances = rsp.data;
      });
    }
  }

  onSapIdsChange() {
    this.defaultSapIdCount=0
    this.org.sapIds.forEach(sapId => { if (sapId.defaultSapId) {this.defaultSapIdCount++} })
    this.org.sapIds = _.sortBy(this.org.sapIds, (item) => {
      return (item.description ? item.description: item.sapId);
    });
  }

  isUkprnNotValid(){
     return this.org.id == null ? this.ukprnOccurances > 0 : (this.ukprnOccurances > 1 || (this.ukprnOccurances == 1 && this.org.ukprn != this.originalUkprn));
  }

  onEntityTypeChange(init) {
    if (!init) {
      if (this.org.entityType == 1) {
        this.isManagingOrganisation = true;
        this.org.registrationAllowed = true;
      } else {
        this.isManagingOrganisation = false;
        this.org.registrationAllowed = false;
      }
    }

    let template = _.find(this.organisationTemplates, {id: this.org.entityType});
    if (template) {
      let orgDetailsBlock = _.find(template.blocks, {blockName: 'Organisation Details'});
      this.orgDetailsQuestions = orgDetailsBlock.questions;
    }

    if(this.org.entityType === 6) {
      this.org.isLearningProvider = true;
    } else if (!this.isFieldVisible('isLearningProvider')){
      this.org.isLearningProvider = null;
    }

    if(!this.isFieldVisible('regulated')){
      this.org.regulated = null;
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
    } else if (!this.org.managingOrganisationId) {
      this.isUniqueOrgName = true;
    } else {
      this.OrganisationService.isOrganisationNameUnique(orgName, this.org.managingOrganisationId).then(isUnique => {
        this.isUniqueOrgName = isUnique;
      });
    }
  }

  getTeams() {
    let managingOrgId = this.org.managingOrganisationId;
    if (managingOrgId) {
      this.org.team = this.org.team;
      this.loadingTeams = true;
      this.OrganisationService.getOrganisationTeams(managingOrgId).then(rsp => {
        let allTeams = rsp.data || [];
        this.teams = _.filter(allTeams, (team)=>{ return team.id !== this.org.id});
      }).finally(() => {
        this.loadingTeams = false;
      })
    } else {
      this.teams = [];
    }
  }

  updateManagingOrg() {
    let managingOrgId = this.org.managingOrganisationId;
    if (managingOrgId) {
      this.org.team = null;
      this.loadingTeams = true;
      this.OrganisationService.getOrganisationTeams(managingOrgId).then(rsp => {
        let allTeams = rsp.data || [];
        this.teams = _.filter(allTeams, (team)=>{ return team.id !== this.org.id});
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

  showAddSapIdModal() {
    let modal = this.OrganisationSapIdModalService.show('ADD', this.org.sapIds);
    modal.result.then((result) => {
      this.org.sapIds.push(result);
      this.onSapIdsChange();
    }, err => {});
  }

  showEditSapIdModal(sapIdModel) {
    let modal =this.OrganisationSapIdModalService.show('UPDATE', this.org.sapIds, _.cloneDeep(sapIdModel));
    modal.result.then((result) => {
      let index = this.org.sapIds.findIndex(x => x == sapIdModel)
      if (index >=0) {
        this.org.sapIds[index] = result;
      }
      this.onSapIdsChange();
    }, err => {});
  }

  deleteSapId(sapIdModel) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this SAP ID?');
    modal.result.then(() => {
      _.remove(this.org.sapIds, sapIdModel);
      this.onSapIdsChange();
    }, err => {});
  }

  isFieldVisible(fieldName) {
    if (!this.org.entityType) {
      return false;
    }

    let fieldConfig = _.find(this.orgDetailsQuestions, {modelAttribute: fieldName}) || {};
    if (fieldConfig.parentModel != null) {
      return this.isParentAnswered(fieldConfig.parentModel, fieldConfig.parentAnswerToMatch);
    } else if (fieldConfig.requirement === 'mandatory' || fieldConfig.requirement === 'optional') {
      return true;
    }
  }

  isParentAnswered(parentModel, parentAnswer){
    return ('' + this.org[parentModel]) === parentAnswer;
  }

  isFieldRequired(fieldName) {
    let fieldConfig = _.find(this.orgDetailsQuestions, {modelAttribute: fieldName}) || {};
    return fieldConfig.requirement === 'mandatory';
  }

}

export default OrganisationFormCtrl;
