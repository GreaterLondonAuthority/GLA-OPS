/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class TemplateBlockOutputs {

  constructor(OutputsService, OutputConfigurationService) {
    this.OutputsService = OutputsService;
    this.OutputConfigurationService = OutputConfigurationService;

  }

  $onInit() {
    this.configurableFields = this.OutputsService.getOutputsConfigurableFields().map(fieldId => {
      return {
        id: fieldId,
        label: _.startCase(fieldId)
      }
    });

    this.OutputConfigurationService.getAllOutputConfigurationGroup().then(resp => {
      this.outputConfigurationsOptions = resp.data;
    });

  }

  getAnswerAsText(boolValue){
    if(boolValue == null){
      return 'Not provided';
    }
    return boolValue? 'Yes' : 'No';
  }
}

TemplateBlockOutputs.$inject = ['OutputsService', 'OutputConfigurationService'];

gla.component('templateBlockOutputs', {
  templateUrl: 'scripts/components/template-block-outputs/templateBlockOutputs.html',
  controller: TemplateBlockOutputs,
  bindings: {
    block: '<',
    template: '<',
    readOnly: '<'
  },
});

