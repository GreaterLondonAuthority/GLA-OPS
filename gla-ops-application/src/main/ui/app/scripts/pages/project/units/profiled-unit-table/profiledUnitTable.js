/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ProfiledUnitTableCtrl {
  constructor(UnitsService){
    this.UnitsService = UnitsService;
  }

  $onInit(){
    this.wizardType= this.wizardType || 'Rent';
  }
}

ProfiledUnitTableCtrl.$inject = ['UnitsService'];

angular.module('GLA')
  .component('profiledUnitTable', {
    bindings: {
      units: '<',
      readOnly: '<',
      wizardType: '@',
      showMarketTypes: '<',
      hasLegacyRent: '<?',
      hasLegacySales: '<?',
      hasDiscountedRate: '<?',
      onEdit: '&',
      onDelete: '&'
    },
    templateUrl: 'scripts/pages/project/units/profiled-unit-table/profiledUnitTable.html',
    controller: ProfiledUnitTableCtrl
  });
