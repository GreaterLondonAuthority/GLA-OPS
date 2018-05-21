/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ProfiledUnitWizardCtrl {
  constructor($log, UnitsService) {
    this.UnitsService = UnitsService;
    this.$log = $log;
  }

  $onInit() {
    this.unit = {
      type: this.wizardType || 'Rent'
    };
    let tenureTypesFilter = {'availableForRental': true};
    if(this.wizardType === 'Sales'){
      tenureTypesFilter = {'availableForSales': true};
    }

    let tenureDetails = [];
    _.forEach(this.config.tenureDetails, (tenure) => {
      let tenureClone = _.clone(tenure);
      tenureClone.marketTypes = _.filter(tenureClone.marketTypes, tenureTypesFilter);
      if(tenureClone.marketTypes.length > 0){
        tenureDetails.push(tenureClone);
      }
    });

    this.tenureDetails = tenureDetails;
    if(tenureDetails.length === 1){
      this.unit.tenure = tenureDetails[0];
      this.onTenureSelect(tenureDetails[0]);
    }

    this.$log.log('component config:', this.tenureDetails);
  }

  onTenureSelect() {
    if(this.unit.tenure.marketTypes.length === 1) {
      this.unit.marketType = this.unit.tenure.marketTypes[0];
    } else {
      this.unit.marketType = null;
    }
    this.onMarketTypeSelect();
  }
  onMarketTypeSelect() {
    if(this.unit.marketType) {
      if(this.unit.type === 'Rent' && this.unit.marketType.id !== this.UnitsService.LEGACY_RENT_MARKET_TYPE_ID){
        this.unit.weeklyMarketRent = undefined;
      }
      if(this.unit.type === 'Sales' && this.unit.marketType.id === this.UnitsService.DISCOUNTED_RATE_MARKET_TYPE_ID){
        this.unit.firstTrancheSales = undefined;
      }
      if(this.unit.type === 'Sales' && this.unit.marketType.id !== this.UnitsService.DISCOUNTED_RATE_MARKET_TYPE_ID){
        this.unit.discountOffMarketValue = undefined;
      }
    } else {
      this.unit.weeklyMarketRent = undefined;
      this.unit.firstTrancheSales = undefined;
      this.unit.discountOffMarketValue = undefined;
    }
  }


  isValidForm(){
    let requiredFields;
    if(this.unit.type === 'Sales') {
      requiredFields = ['tenure', 'marketType', 'nbBeds', 'unitType', 'nbUnits', 'marketValue', 'firstTrancheSales', 'weeklyServiceCharge'];
      if(this.unit.marketType && this.unit.marketType.id === this.UnitsService.DISCOUNTED_RATE_MARKET_TYPE_ID){
        //'firstTrancheSales' is replaced with 'discountOffMarketValue' for this market type
        requiredFields[6] = 'discountOffMarketValue';
      }else if(this.unit.marketType && this.unit.marketType.id === this.UnitsService.LEGACY_SALES_MARKET_TYPE_ID){
        requiredFields[6] = 'netWeeklyRent';
      }
    } else {
      requiredFields = ['tenure', 'marketType', 'nbBeds', 'unitType', 'nbUnits', 'netWeeklyRent', 'weeklyServiceCharge'];
      if(this.unit.marketType && this.unit.marketType.id === this.UnitsService.LEGACY_RENT_MARKET_TYPE_ID){
        requiredFields.push('weeklyMarketRent');
      }
    }
    let hasMissingFields = requiredFields.some(requiredField => _.isNil(this.unit[requiredField]));
    return !hasMissingFields;
  }



  add() {
    let data = angular.copy(this.unit);
    data.tenureId = this.unit.tenure.id;
    delete data.tenure;
    this.onAdd({$event: data});
  }
}

ProfiledUnitWizardCtrl.$inject = ['$log', 'UnitsService'];

/**
 * @wizardType 'rent' or 'sales'
 */
angular.module('GLA')
  .component('profiledUnitWizard', {
    bindings: {
      showMarketTypes: '<',
      onAdd: '&',
      config: '<',
      wizardType: '@',
      readOnly: '<'
    },
    templateUrl: 'scripts/components/profiled-unit-wizard/profiledUnitWizard.html',
    controller: ProfiledUnitWizardCtrl
  });
