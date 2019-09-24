/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class OutputsCostsChangeReport {
  constructor(OutputsService) {
    this.OutputsService = OutputsService;
  }

  $onInit(){

    this.outputsCostsFields = [
      {
        field: 'category',
        label: 'CATEGORY'
      },{
        field: 'subcategory',
        label: 'SUBCATEGORY'
      },
      {
        field: 'unitCost',
        label: 'UNIT COST £'
      }
    ];


    let outputsBlock = (_.find(this.project.projectBlocksSorted, {type: 'OutputsBlock'})  || {});

    this.outputCategories = [];
    this.OutputsService.getOutputConfigGroup(outputsBlock.configGroupId).then((resp) => {
      this.outputCategories = resp.data.categories;

      let leftCCs = this.data.left ? (this.data.left.categoriesCosts || []) : [];
      let rightCCs = this.data.right ? (this.data.right.categoriesCosts || []) : [];
      this.outputsCostsToCompare = [];
      for (let i = 0; i < leftCCs.length; i++) {
        let leftCC = leftCCs[i];
        let rightCC = _.find(rightCCs, {outputCategoryConfigurationId: leftCC.outputCategoryConfigurationId}) || null;

        let category = (_.find(this.outputCategories, {id: leftCC.outputCategoryConfigurationId}) || {}).category;
        let subcategory = (_.find(this.outputCategories, {id: leftCC.outputCategoryConfigurationId}) || {}).subcategory;
        leftCC.category = category;
        leftCC.subcategory = subcategory;
        if (rightCC) {
          rightCC.category = category;
          rightCC.subcategory = subcategory;
        }

        this.outputsCostsToCompare.push({
          left: leftCC,
          right: rightCC
        })
      }
    });

  }
}

OutputsCostsChangeReport.$inject = ['OutputsService'];

angular.module('GLA')
  .component('outputsCostsChangeReport', {
    bindings: {
      data: '<',
      project: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/outputsCostsChangeReport.html',
    controller: OutputsCostsChangeReport  });
