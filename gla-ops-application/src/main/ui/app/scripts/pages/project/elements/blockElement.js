/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import RepeatingEntityCtrl from '../RepeatingEntityCtrl';

class BlockElement extends RepeatingEntityCtrl{
  constructor(TemplateService) {
    super();
    this.TemplateService = TemplateService;
  }

  $onInit() {
    super.$onInit();
    this.nameId = this.generateId();
    this.postcodeId = this.generateId();
    this.projectTypeId = this.generateId();
    this.guidanceId = this.generateId();
    this.descriptionId = this.generateId();
    this.projectStageId = this.generateId();
    this.projectClassificationId = this.generateId();
    this.operationalPeriodId = this.generateId();

    this.labels = {
      name: this.label('Name'),
      postcode: this.label('Postcode'),
      projectType: this.blockTemplate.projectTypesText,
      guidance: this.blockTemplate.guidanceText,
      description: this.blockTemplate.descriptionText,
      projectStage : this.blockTemplate.projectStagesText,
      projectClassification : this.blockTemplate.projectClassificationText,
      operationalPeriod: this.blockTemplate.operationalPeriodText
    }
  }

  generateId(){
    return Math.floor(Math.random() * Date.now());
  }

  label(labelText){
    let text = `${this.blockTemplate.elementTextSingular || ''} ${labelText || ''}`;
    return _.upperFirst(text.toLowerCase().trim());
  }
}

BlockElement.$inject = ['TemplateService'];

angular.module('GLA')
  .component('blockElement', {
    controller: BlockElement,
    require: {
      parentCtrl: '?^^entitiesList'
    },
    bindings: {
      element: '<',
      blockTemplate: '<',
      readOnly: '<'
    },
    templateUrl: 'scripts/pages/project/elements/blockElement.html'
  });

