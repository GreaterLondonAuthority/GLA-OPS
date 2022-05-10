/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import RepeatingEntityCtrl from '../RepeatingEntityCtrl';

class Objective extends RepeatingEntityCtrl{
  constructor(TemplateService) {
    super();
    this.TemplateService = TemplateService;
  }

  $onInit() {
    super.$onInit();
    this.titleId = this.generateId();
    this.summaryId = this.generateId();
    this.labels = {
      title: this.label('Title'),
      description: this.label('Description')
    }
  }

  generateId(){
    return Math.floor(Math.random() * Date.now());
  }

  label(labelText){
    let text = `${this.blockTemplate.objectiveTextSingular || ''} ${labelText || ''}`;
    return _.upperFirst(text.toLowerCase().trim());
  }
}

Objective.$inject = ['TemplateService'];

angular.module('GLA')
  .component('glaObjective', {
    controller: Objective,
    require: {
      parentCtrl: '?^^entitiesList'
    },
    bindings: {
      objective: '<',
      blockTemplate: '<',
      readOnly: '<'
    },
    templateUrl: 'scripts/pages/project/objectives/objective.html'
  });

