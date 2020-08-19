/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class UserRegistrationForm {
  constructor() {
  }

  $onInit(){
    this.regForm = {};
    this.regData = this.regData || {};
    this.lastIsFormValid = false;
  }

  $doCheck() {
    let currentIsFormValid = this.isFormValid();
    if(this.lastIsFormValid !== currentIsFormValid){
      this.onFormValidityChange({$event: {
        isFormValid: currentIsFormValid,
        data: this.regData
      }});
      this.lastIsFormValid = currentIsFormValid;
    }
  }


  passwordChanged() {
    this.errors = null;
  };

  get isInvalidCodeErrorVisible() {
    return this.regData.orgCode && this.regData.orgCode.length > 2 && !this.orgName;
  }

  isFormValid() {
    let isValid = this.regForm.$valid && this.regData.isPasswordStrongEnough;
    if(this.showOrgLookup){
      isValid = isValid && this.regData.orgCode && this.orgName;
    }
    return !!isValid;
  }
}


UserRegistrationForm.$inject = [];


angular.module('GLA')
  .component('userRegistrationForm', {
    templateUrl: 'scripts/pages/registration/registrationForm/userRegistrationForm.html',
    bindings: {
      regData: '<?',
      showOrgLookup: '<?',
      errors: '<?',
      onFormValidityChange: '&'
    },
    controller: UserRegistrationForm
  });
