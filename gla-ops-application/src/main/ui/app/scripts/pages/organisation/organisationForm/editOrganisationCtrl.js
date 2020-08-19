/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import OrganisationFormCtrl from './organisationFormCtrl';

class EditOrganisationCtrl extends OrganisationFormCtrl {

  constructor($injector) {
    super($injector);
  }

  $onInit() {
    this.org = this.OrganisationService.apiToFormModelData(this.organisation);
    this.isNew = false;
    super.$onInit();

    if(!this.canEditManagingOrg){
      this.managingOrganisations = [{
        id: this.org.managingOrganisationId,
        name: this.org.managingOrganisationName
      }];
    }

    this.originalOrgName = this.org.name;
    this.originalRegistrationKey = this.org.registrationKey;
    this.title = 'Edit an organisation';
    this.isValidRegKey = true;
  }

  validateRegistrationKey(key){
    this.isValidRegKey = true;
    if(key){
      key = _.trim(key);
      if(!(_.toLower(this.org.imsNumber) === _.toLower(key) ||
        _.toLower(this.originalRegistrationKey) === _.toLower(key)
        )){

        this.OrganisationService.lookupOrgNameByCode(key)
          .then((response) => {
            this.isValidRegKey = false;
          })
          .catch((err) => {
            this.isValidRegKey = true;
          });
      }
    }
  }

  back() {
    this.submit();
  };

  submit() {
    let data = this.OrganisationService.formModelToApiData(this.org);
    return this.OrganisationService.updateDetails(this.org.id, data).then(resp => {
      this.$state.go('organisation.view', {orgId: this.org.id}, {reload: true});
      this.ToastrUtil.success(`Your organisation ${this.org.name} has been updated`);
    },(resp)=>{
      this.ConfirmationDialog.warn(resp.data ? resp.data.description : null);
    });
  }
}

EditOrganisationCtrl.$inject = ['$injector'];


angular.module('GLA')
  .component('editOrganisationPage', {
    templateUrl: 'scripts/pages/organisation/organisationForm/organisationForm.html',
    bindings: {
      organisationTypes: '<',
      organisation: '<',
      teams: '<',
      contacts: '<',
      legalStatuses: '<',
      isLegalStatusEnabled: '<'
    },
    controller: EditOrganisationCtrl,
  });

