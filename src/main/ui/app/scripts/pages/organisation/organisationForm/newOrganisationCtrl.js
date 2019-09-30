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
  }

  $onInit() {
    this.initOrganisation();
    this.isNew = true;
    this.title = 'Register an organisation';
    this.isValidRegKey = true;
    super.$onInit();
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
    this.errors = null;
    let data = this.OrganisationService.formModelToApiData(this.org);
    this.OrganisationService.createOrganisation(data).then(resp => {
      if (this.orgWithUser) {
        this.$state.go('confirm-org-and-user-created');
      } else if (this.isProfile) {
        this.$state.go('organisations');
        this.ToastrUtil.success(`Organisation profile requested, awaiting GLA approval`);
      } else {
        this.$state.go('organisations');
        this.ToastrUtil.success(`Your organisation ${this.org.name} has been registered`);
      }
    }).catch(err => {
      this.errors = {};
      err.data.errors.map(e => {
        this.errors[e.name] = e.description
      });
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
