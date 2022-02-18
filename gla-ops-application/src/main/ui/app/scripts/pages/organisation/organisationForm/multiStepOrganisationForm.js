/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import NewOrganisationProfileCtrl from './newOrganisationProfileCtrl';

class MultiStepOrganisationForm extends NewOrganisationProfileCtrl {
  constructor($injector, $scope) {
    super($injector);
    this.$scope = $scope;
  }

  $onInit() {
    super.$onInit();
    this.$scope.orgForm = {};
    this.btnText = 'NEXT';
    this.org = this.organisation || this.initOrganisation();
    this.org.managingOrganisationId = this.managingOrganisationId;
    this.orgWithUser = true;
    this.canEditName = this.UserService.hasPermission('org.edit.name', (this.organisation || {}).id);

    this.$scope.$watch('$ctrl.isFormInvalid()', invalid => {
      this.isFormValid = !invalid;
      if(this.onFormValidityChange){
        this.onFormValidityChange({event: {
            organisation: this.org,
            isFormValid: this.isFormValid
          }
        });
      }
    });
  }

  isFormInvalid() {
    return !this.isValidRegKey || !this.isUniqueOrgName || this.$scope.orgForm.$invalid
  }
}

MultiStepOrganisationForm.$inject = ['$injector', '$scope'];

export default MultiStepOrganisationForm;

angular.module('GLA')
  .component('multiStepOrganisationForm', {
    templateUrl: 'scripts/pages/organisation/organisationForm/multiStepOrganisationForm.html',
    bindings: {
      organisation: '<',
      organisationTypes: '<',
      managingOrganisations: '<',
      managingOrganisationId: '<',
      legalStatuses: '<',
      isLegalStatusEnabled : '<',
      organisationTemplates: '<',
      onSave: '&',
      onFormValidityChange: '&?'
    },
    controller: MultiStepOrganisationForm
  });
