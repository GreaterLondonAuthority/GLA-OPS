/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class UserDefinedOutputsPage extends ProjectBlockCtrl {
  constructor($injector, ProjectBlockService, TemplateService) {
    super($injector);
    this.ProjectBlockService = ProjectBlockService;
    this.TemplateService = TemplateService;
  }

  $onInit() {
    super.$onInit();
    this.entities = this.projectBlock.userDefinedOutputs;
    this.templateConfig = this.TemplateService.getBlockConfig(this.template, this.projectBlock);
  }

  submit(){
    return this.ProjectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, true);
  }
}

UserDefinedOutputsPage.$inject = ['$injector', 'ProjectBlockService', 'TemplateService'];

angular.module('GLA')
  .component('userDefinedOutputsPage', {
    controller: UserDefinedOutputsPage,
    bindings: {
      project: '<',
      template: '<'
    },
    templateUrl: 'scripts/pages/project/user-defined-outputs/userDefinedOutputsPage.html'
  });

