/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ForecastDataUtil from '../../../../util/ForecastDataUtil';

/**
 * Annual Spend Forecast component
 */
class ReceiptWizardCtrl {
  constructor($scope, UserService) {
    this.$scope = $scope;
    this.UserService = UserService;

    this.canCreateActuals = this.UserService.hasPermission(`proj.ledger.actual.create`);

    this.initDropdownConfigs();
    this.resetDropdownSelections();

    this.receiptTypes = [{
      text: 'Incoming credit (£)',
      sign: 1
    }, {
      text: 'Outgoing debit (-£)',
      sign: -1
    }];

    // generate financial month list
    let yW = this.$scope.$watch('$ctrl.year', value => {
      if(value) {
        this.resetDropdownSelections();
      }
    });
  }


  $onChanges(changes){
    if(changes.year && this.year){
      let year = this.year.financialYear.financialYear;
      this.dateOptions = {
        showWeeks: false,
        format: 'yyyy-MM',
        formatYear: 'yyyy',
        formatMonth: 'MMM',
        minMode: 'month',
        maxMode: 'month',
        yearColumns: 3,
        yearColumns: 3,
        initDate: new Date(`${year}-04-01`),
        minDate: new Date(`${year}-04-01`),
        maxDate: new Date(`${year + 1}-03-31`),
        datepickerMode: 'month'
      };
    }
  }

  initDropdownConfigs() {

  }

  onActualKeyPress(){
    this.forecastValue = null;
    this.date = null;
    this.dateOptions.minMode = 'day';
    this.dateOptions.maxMode = 'month';
    this.dateOptions.format = 'dd/MM/yyyy';
  }

  onForecastKeyPress(){
    this.actualValue = null;
    this.date = null;
    this.dateOptions.minMode = 'month';
    this.dateOptions.maxMode = 'month';
    this.dateOptions.format = 'MM/yyyy';
  }

  resetDropdownSelections() {
    this.date = undefined;
    this.forecastValue = undefined;
    this.actualValue = undefined;
    this.receiptCategory = undefined;
    this.receiptType = undefined;
  }

  addReceiptOutput() {
    this.onAddReceipt({
      event:
      {
        // this is passed so that addReceipt in receiptsCtrl knows the financialYear
        // financialYear: this.addReceiptMonth.financialYear,
        forecastValue: this.forecastValue ? this.receiptType.sign * this.forecastValue : null,
        actualValue: this.actualValue ? this.receiptType.sign * this.actualValue : null,
        year: moment(this.date).year(),
        month: moment(this.date).month() + 1,
        day: moment(this.date).date(),
        categoryId: this.receiptCategory
      }
    }).then(()=>{
      this.resetDropdownSelections();
    });
  }
}

ReceiptWizardCtrl.$inject = ['$scope', 'UserService'];

angular.module('GLA')
  .component('receiptWizard', {
    bindings: {
      year: '<',
      onAddReceipt: '&',
      readOnly: '<',
      categories: '<'
    },
    templateUrl: 'scripts/pages/project/receipts/wizard/receiptWizard.html',
    controller: ReceiptWizardCtrl
  });
