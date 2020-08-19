/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import NewOrganisationCtrl from './newOrganisationCtrl';

class NewOrganisationProfileCtrl extends NewOrganisationCtrl {
  constructor($injector, managingOrganisations) {
    super($injector);
    this.managingOrganisations = managingOrganisations;
  }

  $onInit() {
    super.$onInit();
    this.title = 'Create a new organisation profile';
    this.btnText = 'REQUEST NEW PROFILE';
    this.isProfile = true;
    this.orgToCopyFrom = null;
    this.userOrganisations = this.UserService.currentUserOrganisations();
    this.canEditOrgType = true;
    this.canEditManagingOrg = true;

    const defaultOrgToCopyFrom = {
      id: null,
      name: 'None'
    };

    this.orgsToCopyFrom = [defaultOrgToCopyFrom].concat(this.userOrganisations);

    _.assign(this.labels, {
      orgType: 'Organisation type for this profile',
      orgName: 'New organisation profile name',
      managingOrg: 'The GLA department you will be dealing with (managing organisation)'
    });

    this.isCopyOrgEnabled = this.UserService.hasPermission('org.manage.copy');

    if(this.isCopyOrgEnabled && this.userOrganisations.length === 1){
      this.$rootScope.showGlobalLoadingMask = true;
      this.orgToCopyFrom = this.userOrganisations[0].id;
      this.copyFrom(this.orgToCopyFrom);
    } else {
      this.org.managingOrganisationId = this.$state.params.managingOrgId? this.$state.params.managingOrgId * 1 : null;
    }
  }

  copyFrom(orgId) {
    if (orgId) {
      this.OrganisationService.getDetails(orgId).then((org) => {
        this.org = this.OrganisationService.apiToFormModelData(org.data);
        this.org.managingOrganisationId = this.$state.params.managingOrgId * 1 || this.org.managingOrganisationId;
        this.org.id = null;
        this.org.sapVendorId = null;
        this.org.imsNumber = null;
        this.org.name = null;
        this.org.userRegStatus = null;
        this.org.status = null;
        this.$rootScope.showGlobalLoadingMask = false;
      });
    } else {
      this.initOrganisation();
      this.org.managingOrganisationId = this.$state.params.managingOrgId * 1 || this.org.managingOrganisationId;
    }
  }
}

NewOrganisationProfileCtrl.$inject = ['$injector', 'managingOrganisations'];

export default NewOrganisationProfileCtrl;


angular.module('GLA')
  .controller('NewOrganisationProfileCtrl', NewOrganisationProfileCtrl);
