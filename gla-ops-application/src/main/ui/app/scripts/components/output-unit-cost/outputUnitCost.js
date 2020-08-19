/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');

class OutputUnitCost {
  constructor(OutputsService) {
    this.OutputsService = OutputsService;
  }

  $onInit(){
    this.outputsBlock = (_.find(this.project.projectBlocksSorted, {type: 'OutputsBlock'})  || {});

    this.outputCategories = [];
    this.OutputsService.getOutputConfigGroup(this.outputsBlock.configGroupId).then((resp) => {
      this.outputCategories = resp.data.categories;
      // init table
      _.each(this.outputCategories, category => {
        let outputCost = this.getOutputCost(category.id);

        if (!outputCost) {
          outputCost = {
            outputCategoryConfigurationId: category.id,
          };
          this.block.categoriesCosts.push(outputCost);
        }

        this.enrichOutputCostFromCategory(outputCost, category);
      });
    });
  }

  enrichOutputCostFromCategory(outputCost, outputCategory) {
    outputCost.category = outputCategory.category;
    outputCost.subcategory = outputCategory.subcategory;
    outputCost.hidden = outputCategory.hidden;
  }

  getOutputCost(categoryId) {
    return _.find(this.block.categoriesCosts, {outputCategoryConfigurationId: categoryId});
  }

}

OutputUnitCost.$inject = ['OutputsService', 'Util'];

gla.component('outputUnitCost', {
  templateUrl: 'scripts/components/output-unit-cost/outputUnitCost.html',
  controller: OutputUnitCost,
  bindings: {
    project: '<',
    block: '<',
    readOnly: '<',
    canEditAdvancePayment: '<',
    showRecoveryColumn: '<'
  }
});

