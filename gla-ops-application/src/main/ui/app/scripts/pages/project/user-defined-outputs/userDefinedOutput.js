/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import RepeatingEntityCtrl from '../RepeatingEntityCtrl';

class UserDefinedOutput extends RepeatingEntityCtrl{
  constructor(TemplateService) {
    super();
    this.TemplateService = TemplateService;
  }

  $onInit() {
    super.$onInit();
    this.outputNameId = this.generateId();
    this.deliveryAmountId = this.generateId();
    this.baselineId = this.generateId();
    this.monitorOfOutputId = this.generateId();
    this.labels = {
      outputName: this.blockTemplate.outputNameText,
      deliveryAmount: this.blockTemplate.amountToDeliverText,
      baseline: this.blockTemplate.baselineText,
      monitorOfOutput: this.blockTemplate.monitorQuestion
    }
  }

  generateId(){
    return Math.floor(Math.random() * Date.now());
  }
}

UserDefinedOutput.$inject = ['TemplateService'];

angular.module('GLA')
  .component('userDefinedOutput', {
    controller: UserDefinedOutput,
    require: {
      parentCtrl: '?^^entitiesList'
    },
    bindings: {
      definedOutput: '<',
      blockTemplate: '<',
      readOnly: '<'
    },
    templateUrl: 'scripts/pages/project/user-defined-outputs/userDefinedOutput.html'
  });

