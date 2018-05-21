/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import NumberUtil from '../../../../util/NumberUtil';
import DateUtil from '../../../../util/DateUtil';
/**
 * Annual Spend Forecast component
 */
class ProjectBudgtForecastCtrl {
  constructor($scope, ConfirmationDialog, UserService) {
    this.$scope = $scope;
    this.ConfirmationDialog = ConfirmationDialog;
    this.UserService = UserService;

    this.canCreateActuals = this.UserService.hasPermission(`proj.ledger.actual.create`);




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

  onActualKeyPress(){
    this.forecastSpendValue = null;
    this.date = null;
    this.dateOptions.minMode = 'day';
    this.dateOptions.maxMode = 'month';
    this.dateOptions.format = 'dd/MM/yyyy';
  }

  onForecastKeyPress(){
    this.actualSpendValue = null;
    this.date = null;
    this.dateOptions.minMode = 'month';
    this.dateOptions.maxMode = 'month';
    this.dateOptions.format = 'MM/yyyy';
  }

  validateSpendValue() {
    return (this.forecastSpendValue !== null && this.forecastSpendValue > 0) || (this.actualSpendValue !== null && this.actualSpendValue > 0);
  }

  /**
   * Format number to string with comma's and append CR
   * @see `StringUtil.numberToStrWithCR`
   */
  formatTotal(value, precision) {
    let res = NumberUtil.formatWithPoundAndCR(value, precision);
    return value && res? `${res}` : '-';
  }

  /**
   * Builds and sends a request for a new spend forecast
   */
  addForecastSpend() {
    // TODO: recurrence still hasnt been added to backend
    const spendData = {
      entryType: this.addSpendType.value,
      forecastValue: this.forecastSpendValue,
      actualValue: this.actualSpendValue,
      month: moment(this.date).month() + 1,
      categoryId: this.addSpendCategory.id,
      year: moment(this.date).year(),
      day: moment(this.date).date()
    };

    this.onAddSpend({event: {data: spendData}})
      .then(() => {
        // clear values
        this.addSpendType = null;
        this.forecastSpendValue = null;
        this.actualSpendValue = null;
        this.date = null;
        this.addSpendCategory = null;
      })
  }

  /**
   * wrapper on the cell edit of forecastMonth
   * add the year value and call the addSpend.
   * we update the cell in the same way we would add a new spend
   * @param  {[type]} event [description]
   * @return {[type]}       [description]
   */
  onCellEdit(event) {
    var data = event.data;

    const year = this.year.financialYear.financialYear;
    // to financial year
    data.year = (data.month > 3) ? year : year + 1;
    this.onAddSpend({event: {data: data}});
  }

  /**
   * Delete spend row
   */
  onDeleteSpend(event) {
    console.log('event', event);
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete this forecast?');
    modal.result.then(() => {
      const year = this.year.financialYear.financialYear;
      event.data.year = (event.data.month > 3) ? year : year + 1;
      this.onRemoveSpend({event:{data: event.data}});
    });
  }
}

ProjectBudgtForecastCtrl.$inject = ['$scope', 'ConfirmationDialog', 'UserService'];

angular.module('GLA')
  .component('projectBudgetForecast', {
    bindings: {
      sessionStorage: '=',
      year: '<',
      yearData: '=',
      spendCategories: '<',
      spendTypes: '<',
      spendRecurrence: '<',
      onAddSpend: '&',
      onRemoveSpend: '&',
      onShowMetadata: '&',
      readOnly: '<',
      readOnlyParent: '<'
    },
    templateUrl: 'scripts/pages/project/project-budget/forecast/projectBudgetForecast.html',
    controller: ProjectBudgtForecastCtrl
  });
