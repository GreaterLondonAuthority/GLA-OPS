/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../../../../util/DateUtil';
import OutputsUtil from '../OutputsUtil';

/**
 * Annual Spend Forecast component
 */
class OutputsWizardCtrl {
  constructor($scope) {
    this.$scope = $scope;

    this.initDropdownConfigs();
    this.resetDropdownSelections();

    // generate financial month list
    let yW = this.$scope.$watch('$ctrl.year', value => {
      if(value) {
        this.resetDropdownSelections();
        this.getForecastOrActualChoices();
      }
    });
  }

  initDropdownConfigs() {
    this.categories = OutputsUtil.getCategories();
    this.unitConfig = OutputsUtil.getUnitConfig();
    this.directOrIndirectChoices = OutputsUtil.getDirectOrIndirect();
    this.forecastOrActualChoices = this.getForecastOrActualChoices();

  }

  resetDropdownSelections() {
    this.subCategories = [];

    this.outputsSubCategory = undefined;
    this.selectedUnits = undefined;
    this.addOutputsMonth = undefined;
    this.outputsCategory = undefined;
    this.directOrIndirect = undefined;
    this.forcastOrActual = undefined;
    this.outputsValue = undefined;
  }

  categorySelected(items) {
    if(items === this.subCategories){
      return;
    }

    this.outputsSubCategory = undefined;
    this.outputsValue = undefined;
    this.selectedUnits = undefined;
    this.subCategories = items;
    if(this.subCategories.length === 1){
      this.subCategorySelected(this.subCategories[0]);
    }
  }

  subCategorySelected(item) {
    if(this.selectedUnits && this.selectedUnits.id === this.unitConfig[item.valueType].id){
      return;
    }
    this.selectedUnits = this.unitConfig[item.valueType];
    this.outputsValue = this.selectedUnits.default;
  }

  getForecastOrActualChoices() {
    // TODO on future selected
    if(this.addOutputsMonth){
      let  selectedData = moment(this.addOutputsMonth.label, 'MMMM YYYYY');
      // if this is a future month
      if(moment(moment().month() + 2,'MM').subtract(1,'day').isBefore(selectedData)){
        return [{
          id: 1,
          label: 'Forecast',
          isForecast: true
        }];
      }
    }
    return [{
      id: 1,
      label: 'Forecast',
      isForecast: true
    }, {
      id: 2,
      label: 'Actual',
      isForecast: false
    }];
  }

  onMonthSelected() {
   this.forecastOrActualChoices = this.getForecastOrActualChoices();

  }

  canAddOutput() {
    if(this.readOnly ||
      !this.addOutputsMonth ||
      !this.outputsCategory ||
      (this.subCategories.length > 1 && !this.outputsSubCategory) ||
      !this.directOrIndirect ||
      !this.forcastOrActual ||
      !this.outputsValue ){
        return false;
    }

    return true;
  }

  addForecastOutput() {
      let config;
      if(this.outputsCategory.length > 1) {
        config = this.outputsSubCategory;
      } else {
        config = this.outputsCategory[0];
      }

//    actual, forecast, config, month, outputType, year
    this.onAddOutput({
      event:
      {
        actual: this.forcastOrActual.isForecast ? undefined : this.outputsValue,
        forecast: this.forcastOrActual.isForecast ? this.outputsValue : undefined,
        config,
        month: this.addOutputsMonth.value,
        year: this.addOutputsMonth.calendarYear,
        outputType: this.directOrIndirect.key,
      }
    }).then(()=>{
      this.resetDropdownSelections();
    });
  }
}

OutputsWizardCtrl.$inject = ['$scope'];

angular.module('GLA')
  .component('outputsWizard', {
    bindings: {
      year: '<',
      yearData: '=',
      onAddOutput: '&',
      readOnly: '<',
      periodType: '<',
      outputTypeName: '<',
      categoryName: '<',
      subcategoryName: '<'
    },
    templateUrl: 'scripts/pages/project/outputs/wizard/outputsWizard.html',
    controller: OutputsWizardCtrl
  });
