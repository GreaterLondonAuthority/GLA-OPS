/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class OrgAndUserCreatedConfirmation {
  constructor(OrganisationService) {
    this.OrganisationService = OrganisationService;
  }

  $onInit() {
    this.navigationCircles = this.OrganisationService.getNavigationCircles();
    this.navigationCircles.forEach(nc => {
      nc.active = false;
      nc.completed = true;
    });
  }


}

OrgAndUserCreatedConfirmation.$inject = ['OrganisationService'];

angular.module('GLA')
  .component('orgAndUserCreatedConfirmation', {
    templateUrl: 'scripts/pages/confirmation/orgAndUserCreatedConfirmation.html',
    bindings: {
      showNavigationCircles: '<'
    },
    controller: OrgAndUserCreatedConfirmation
  });
