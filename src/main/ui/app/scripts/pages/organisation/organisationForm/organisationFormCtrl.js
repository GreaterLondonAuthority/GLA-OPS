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
    this.UserService = $injector.get('UserService');
    this.ToastrUtil = $injector.get('ToastrUtil');
    this.$state = $injector.get('$state');
    this.$rootScope = $injector.get('$rootScope');
    this.user = this.UserService.currentUser();
    this.orgTypes = [];
    this.canEditSapId = this.UserService.hasPermission('org.edit.vendor.sap.id');
    this.canEditOrgType = this.UserService.hasPermission('org.edit.type');
    this.canEditManagingOrg = this.UserService.hasPermission('org.edit.managing.org');
    this.organisationTypes = this.organisationTypes || this.$state.$current.locals.globals.organisationTypes;
    this.managingOrganisations = this.UserService.currentUserOrganisations();
    this.originalOrgName = null;
    this.isUniqueOrgName = true;
    this.labels = {
      orgType: 'Organisation type',
      orgName: 'Organisation name'
    }
  }

  $onInit() {
    this.onEntityTypeChange();
  }
  onEntityTypeChange(){
    this.imsNumberLabel = this.OrganisationService.getImsLabel(this.org);
  }

  isTechSupportOrganisation() {
    return this.org.techSupportOrganisation;
  }

  techSupportSelected() {
    if(this.org.entityType === 5) return true;
    else return false;
  }

  validateName(orgName) {
    if(!orgName || !orgName.length){
      this.isUniqueOrgName = false;
    }else if(this.org.id && this.originalOrgName === orgName){
      this.isUniqueOrgName = true;
    }else {
      this.OrganisationService.isOrganisationNameUnique(orgName).then(isUnique => {
        this.isUniqueOrgName = isUnique;
      });
    }
  }
}

export default OrganisationFormCtrl;
