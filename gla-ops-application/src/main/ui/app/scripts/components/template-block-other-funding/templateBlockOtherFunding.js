/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class TemplateBlockOtherFunding {

  constructor(ReferenceDataService){
    this.ReferenceDataService = ReferenceDataService;
  }

  $onInit() {
    this.requirementOptions = this.ReferenceDataService.getRequirementOptions();
  }
}

TemplateBlockOtherFunding.$inject = ['ReferenceDataService'];


gla.component('templateBlockOtherFunding', {
  templateUrl: 'scripts/components/template-block-other-funding/templateBlockOtherFunding.html',
  controller: TemplateBlockOtherFunding,
  bindings: {
    block: '<',
    template: '<',
    readOnly: '<',
  },
});

