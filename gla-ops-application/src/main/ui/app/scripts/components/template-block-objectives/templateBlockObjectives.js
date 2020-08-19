/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class TemplateBlockObjectives {

  constructor(){
  }

  $onInit() {

  }
}

TemplateBlockObjectives.$inject = [];


gla.component('templateBlockObjectives', {
  templateUrl: 'scripts/components/template-block-objectives/templateBlockObjectives.html',
  controller: TemplateBlockObjectives,
  bindings: {
    block: '<',
    template: '<',
    readOnly: '<',
  },
});

