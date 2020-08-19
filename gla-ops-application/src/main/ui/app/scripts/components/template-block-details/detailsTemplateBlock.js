/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class DetailsTemplateBlock {

  constructor(ProjectDetailsService, ReferenceDataService){
    this.ProjectDetailsService = ProjectDetailsService;
    this.ReferenceDataService = ReferenceDataService;
  }

  $onInit() {
    const simpleProjectDetailsFields = ['maxBoroughs']
    this.configurableFields = this.ProjectDetailsService.getDetailsConfigurableFields().map(fieldId => {
      return {
        id: fieldId,
        label: _.startCase(fieldId),
        displayAsSelect: simpleProjectDetailsFields.includes(fieldId.toString())?false:true
      }
    });
    this.requirementOptions = this.ReferenceDataService.getRequirementOptions();
  }

  canDisplayMaxBorough(field) {
    return field === 'maxBoroughs' && this.template.detailsConfig &&
      (this.template.detailsConfig['boroughRequirement'] === 'mandatory'
        || this.template.detailsConfig['boroughRequirement'] === 'optional'
      )
  }
}

DetailsTemplateBlock.$inject = ['ProjectDetailsService', 'ReferenceDataService'];


gla.component('detailsTemplateBlock', {
  templateUrl: 'scripts/components/template-block-details/detailsTemplateBlock.html',
  controller: DetailsTemplateBlock,
  bindings: {
    block: '<',
    template: '<',
    readOnly: '<',
  },
});

