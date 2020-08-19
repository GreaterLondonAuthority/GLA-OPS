/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


/**
 * Annual Spend Forecast component
 */
class OutputsWizardCtrl {
  constructor($scope, OutputsService) {
    this.$scope = $scope;
    this.OutputsService = OutputsService;
  }

  $onInit(){
    this.baseline = this.baseline || false;
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
    this.unitConfig = this.OutputsService.getUnitConfig();
    this.forecastOrActualChoices = this.getForecastOrActualChoices();
    this.directOrIndirectChoices = this.directOrIndirectChoices || [];
    if (this.directOrIndirectChoices.length == 1) {
      this.directOrIndirect = this.directOrIndirectChoices[0];
    }
  }

  resetDropdownSelections() {
    this.subCategories = [];

    this.outputsSubCategory = undefined;
    this.selectedUnits = undefined;
    this.addOutputsMonth = undefined;
    this.outputsCategory = undefined;
    if (this.directOrIndirectChoices.length > 1) {
      this.directOrIndirect = undefined;
    }
    this.forcastOrActual = undefined;
    this.outputsValue = undefined;
    this.outputsUnitCost = undefined;
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
    } else {
      this.outputsCategoryCost = null;
      this.outputsUnitCost = null;
    }
  }

  updateUnitCost(subcategory){
    // TODO should it be > 0?
    if(this.categoriesCosts && this.categoriesCosts.length > 1){
      this.outputsCategoryCost = (_.find(this.categoriesCosts, {outputCategoryConfigurationId: subcategory.id}) || {});
      this.outputsUnitCost = this.outputsCategoryCost ? this.outputsCategoryCost.unitCost : null;
    }
  }

  subCategorySelected(item) {
    this.updateUnitCost(item);
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
      (this.subCategories.length < 1 && !this.outputsSubCategory) ||
      // (!this.directOrIndirect || true) ||
      !this.forcastOrActual ||
      !this.outputsValue ){
        return false;
    }

    return true;
  }

  canAddBaseline() {
    if(this.readOnly ||
      !this.outputsCategory ||
      (this.subCategories.length > 1 && !this.outputsSubCategory) ||
      this.outputsValue == null){
        return false;
    }

    return true;
  }

  addBaselinedOutput() {
    let config;
    if(this.outputsCategory.length > 1) {
      config = this.outputsSubCategory;
    } else {
      config = this.outputsCategory[0];
    }
    let output = {
      event:
        {
          baseline: this.outputsValue,
          config,
          month: 0,
          year: 0,
          // outputType: this.directOrIndirect.key,
        }
    }
    let p = this.onAddOutput(output);
    if(p) {
      p.then(() => {
        this.resetDropdownSelections();
      });
    }else{
      console.warn('onAddOutput should return a promise on success');
    }
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
        outputType: this.displayOutputType ? this.directOrIndirect.key : 'DIRECT',
      }
    }).then(()=>{
      this.resetDropdownSelections();
    });
  }
}

OutputsWizardCtrl.$inject = ['$scope', 'OutputsService'];

angular.module('GLA')
  .component('outputsWizard', {
    bindings: {
      baseline: '<',
      year: '<',
      yearData: '=',
      onAddOutput: '&',
      readOnly: '<',
      periodType: '<',
      outputTypeName: '<',
      categoryName: '<',
      subcategoryName: '<',
      categories: '<',
      directOrIndirectChoices: '<',
      displayOutputType: '<',
      categoriesCosts: '<',
      displayUnitCost:'<',
      disabledMonths:'<?'
    },
    templateUrl: 'scripts/pages/project/outputs/wizard/outputsWizard.html',
    controller: OutputsWizardCtrl
  });
