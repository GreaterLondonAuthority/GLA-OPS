/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class OutputsConfigurationPage {
  constructor($state, OutputConfigurationService) {
    this.$state = $state;
    this.OutputConfigurationService = OutputConfigurationService;
  }

  $onInit() {
    _.sortBy(outputConfigurations, 'subcategory');
  }

  refresh() {
    this.$state.reload();
  }

}
OutputsConfigurationPage.$inject = ['$state', 'OutputConfigurationService'];

angular.module('GLA')
.component('outputsConfigurationPage', {
  templateUrl: 'scripts/pages/outputs-configuration/outputsConfigurationPage.html',
  bindings: {
    outputConfigurations: '<',
  },
  controller: OutputsConfigurationPage
});
