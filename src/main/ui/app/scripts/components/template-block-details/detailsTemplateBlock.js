/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class DetailsTemplateBlock {

  constructor(ProjectDetailsService){
    this.ProjectDetailsService = ProjectDetailsService;
  }

  $onInit() {
    this.configurableFields = this.ProjectDetailsService.getDetailsConfigurableFields().map(fieldId => {
      return {
        id: fieldId,
        label: _.startCase(fieldId)
      }
    });

    this.requirementOptions = this.ProjectDetailsService.getRequirementOptions();
  }
}

DetailsTemplateBlock.$inject = ['ProjectDetailsService'];


gla.component('detailsTemplateBlock', {
  templateUrl: 'scripts/components/template-block-details/detailsTemplateBlock.html',
  controller: DetailsTemplateBlock,
  bindings: {
    block: '<',
    template: '<',
    readOnly: '<',
  },
});

