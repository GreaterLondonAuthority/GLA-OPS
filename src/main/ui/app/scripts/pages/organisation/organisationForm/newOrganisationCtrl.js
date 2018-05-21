/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import OrganisationFormCtrl from './organisationFormCtrl';

class NewOrganisationCtrl extends OrganisationFormCtrl {
  constructor($injector) {
    super($injector);
    this.initOrganisation();
    this.isNew = true;
    this.title = 'Register an organisation';
    // this.org.managingOrganisationId = this.$state.params.managingOrgId;
  }

  initOrganisation() {
    this.org = {
      address: {}
    };
  }

  back() {
    this.$state.go('organisations');
  }

  submit() {
    let data = this.OrganisationService.formModelToApiData(this.org);
    this.OrganisationService.createOrganisation(data).then(resp => {
      this.$state.go('organisations');
      if (this.isProfile) {
        this.ToastrUtil.success(`Organisation profile requested, awaiting GLA approval`);
      } else {
        this.ToastrUtil.success(`Your organisation ${this.org.name} has been registered`);
      }
    })
  }
}

NewOrganisationCtrl.$inject = ['$injector'];

export default NewOrganisationCtrl;


angular.module('GLA')
  .component('newOrganisationPage', {
    templateUrl: 'scripts/pages/organisation/organisationForm/organisationForm.html',
    bindings: {
      organisationTypes: '<'
    },
    controller: NewOrganisationCtrl
  });
