/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ConsortiumFormCtrl from './consortiumFormCtrl'

angular.module('GLA').component('consortiumForm', {
  templateUrl: 'scripts/pages/consortiums/consortium-form/consortiumForm.html',
  controller: ConsortiumFormCtrl,
  bindings: {
    programmes: '<',
    data: '<',
    readOnly: '<?',
    organisationsInProjects: '<?',
    leadOrganisations: '<',
    onSave: '&onSave'
  }
});
