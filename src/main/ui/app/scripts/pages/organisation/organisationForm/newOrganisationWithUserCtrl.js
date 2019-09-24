/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import NewOrganisationProfileCtrl from './newOrganisationProfileCtrl';

class NewOrganisationWithUserCtrl extends NewOrganisationProfileCtrl {
  constructor($injector, managingOrganisations) {
    super($injector, managingOrganisations);
  }

  $onInit() {
    super.$onInit();
    this.org.managingOrganisationId = null;
    this.orgWithUser = true;
  }

  onUserFormValidityChange($event){
    console.log('$event', $event);
    this.isUserFormValid = $event.isFormValid;
    this.org.userRegistration = $event.data;
  }
}

NewOrganisationWithUserCtrl.$inject = ['$injector', 'managingOrganisations'];


angular.module('GLA')
  .controller('NewOrganisationWithUserCtrl', NewOrganisationWithUserCtrl);
