/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class ObjectivesPage extends ProjectBlockCtrl {
  constructor($injector, ProjectBlockService, TemplateService) {
    super($injector);
    this.ProjectBlockService = ProjectBlockService;
    this.TemplateService = TemplateService;
  }

  $onInit() {
    super.$onInit();
    this.entities = this.projectBlock.projectObjectives;
    this.templateConfig = this.TemplateService.getBlockConfig(this.template, this.projectBlock);
    console.log(this.templateConfig)
  }

  getAnswerAsText(boolValue){
    if(boolValue == null){
      return 'Not provided';
    }
    return boolValue? 'Yes' : 'No';
  }

  submit(){
    console.log('submission')
    return this.ProjectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, true);
  }
}

ObjectivesPage.$inject = ['$injector', 'ProjectBlockService', 'TemplateService'];

angular.module('GLA')
  .component('objectivesPage', {
    controller: ObjectivesPage,
    bindings: {
      project: '<',
      template: '<'
    },
    templateUrl: 'scripts/pages/project/objectives/objectivesPage.html'
  });

