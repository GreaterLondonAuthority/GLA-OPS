/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


//TODO make generic cell component
class GrantTableController {

  $onInit(){
    this.calculateGrantTableId = this.generateId();
    this.negotiatedGrantTableId = this.generateId();
    this.developerLedGrantTableId = this.generateId();
    this.indicativeGrantTableId = this.generateId();
  }

  tenureChange(t) {
    this.onTenureChange();
  }

  showOtherAffordableTenureType(){
    return this.showOtherAffordableQuestion &&
           this.otherAffordableTenureTypes &&
           this.isOtherAffordableTenureTypePresent() ;
  }

  isOtherAffordableTenureTypePresent(){
    let returnValue =false;
    this.data.tenureTypeAndUnitsEntries.forEach(item => {
        if (item.tenureType.name == 'Other Affordable') {
          returnValue = this.hasUnitsEntered(item)
          this.resetOtherAffordableOption(!returnValue)
        }
      }
    )
    return returnValue
  }

  resetOtherAffordableOption(reset){
    if (reset){
      this.data.otherAffordableTenureType =undefined;
    }
  }
  hasUnitsEntered(otherAffordableTenure){
    let returnValue =false;
    switch (this.data.blockType) {
      case 'IndicativeGrant': returnValue = this.hasIndicativeUnitsEntered(otherAffordableTenure); break
      case 'NegotiatedGrant': returnValue = this.hasNegotiatedUnitsEntered(otherAffordableTenure); break
      case 'CalculateGrant': returnValue = this.hasCalculateUnitsEntered(otherAffordableTenure); break
      case 'DeveloperLedGrant': returnValue = this.hasDeveloperLedUnitsEntered(otherAffordableTenure); break
    }
    return returnValue
  }

  hasNegotiatedUnitsEntered(otherAffordableTenure){
    return otherAffordableTenure.grantRequested || otherAffordableTenure.totalUnits || otherAffordableTenure.supportedUnits
      || otherAffordableTenure.totalCost
  }

  hasCalculateUnitsEntered(otherAffordableTenure){
    return otherAffordableTenure.totalUnits || otherAffordableTenure.s106Units || otherAffordableTenure.totalCost
  }

  hasDeveloperLedUnitsEntered(otherAffordableTenure){
    return otherAffordableTenure.s106Units || otherAffordableTenure.additionalAffordableUnits || otherAffordableTenure.totalCost
  }

  hasIndicativeUnitsEntered (otherAffordableTenure) {
    let returnValue =false
    otherAffordableTenure.indicativeTenureValuesSorted.forEach(item => {
       if (item.units){
         returnValue = true;
       }
    })
    return returnValue
  }

  hasErrors(m, fieldName) {
    let errors = this.data.validationFailures || {};
    let rowErrors = errors[m.id];
    if (!rowErrors) {
      return false;
    }
    let el = _.find(rowErrors, {'name': fieldName});
    return !!el;
  }

  generateId(){
    return Math.floor(Math.random() * Date.now());
  }
}

GrantTableController.$inject = [];


angular.module('GLA')
  .component('grantTable', {
    bindings: {
      data: '<data',
      otherAffordableTenureTypes: '<otherAffordableTenureTypes',
      showOtherAffordableQuestion: '<showOtherAffordableQuestion',
      isReadonly: '<isReadonly',
      onTenureChange: '&'
    },
    controller: GrantTableController,
    templateUrl: 'scripts/pages/project/grant/grant-table/grantTable.html',
  });
