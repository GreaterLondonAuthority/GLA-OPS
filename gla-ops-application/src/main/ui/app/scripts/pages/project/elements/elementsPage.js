/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class ElementsPage extends ProjectBlockCtrl {
  constructor($injector, ProjectBlockService, TemplateService) {
    super($injector);
    this.ProjectBlockService = ProjectBlockService;
    this.TemplateService = TemplateService;
  }

  $onInit() {
    super.$onInit();
    this.entities = this.projectBlock.projectElements;
    this.templateConfig = this.TemplateService.getBlockConfig(this.template, this.projectBlock);
  }

  submit(){
    this.projectBlock.validationFailures = undefined;
    return this.ProjectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, true);
  }
}

ElementsPage.$inject = ['$injector', 'ProjectBlockService', 'TemplateService'];

angular.module('GLA')
  .component('elementsPage', {
    controller: ElementsPage,
    bindings: {
      project: '<',
      template: '<'
    },
    templateUrl: 'scripts/pages/project/elements/elementsPage.html'
  });

