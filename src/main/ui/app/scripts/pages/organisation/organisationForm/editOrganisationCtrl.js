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
    this.isNew = false;
    this.org = this.OrganisationService.apiToFormModelData(this.organisation);
    if(!this.canEditManagingOrg){
      this.managingOrganisations = [{
        id: this.org.managingOrganisationId,
        name: this.org.managingOrganisationName
      }];
    }

    this.originalOrgName = this.org.name;
    this.title = 'Edit an organisation';
  }

  back() {
    this.submit();
  };



  submit() {
    let data = this.OrganisationService.formModelToApiData(this.org);
    return this.OrganisationService.updateDetails(this.org.id, data).then(resp => {
      this.$state.go('organisation', {orgId: this.org.id});
      this.ToastrUtil.success(`Your organisation ${this.org.name} has been updated`);
    });
  }
}

EditOrganisationCtrl.$inject = ['$injector'];


angular.module('GLA')
  .component('editOrganisationPage', {
    templateUrl: 'scripts/pages/organisation/organisationForm/organisationForm.html',
    bindings: {
      organisationTypes: '<',
      organisation: '<'
    },
    controller: EditOrganisationCtrl,
  });

