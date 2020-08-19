/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class ProgressUpdatesCtrl extends ProjectBlockCtrl {
  constructor($state, $log, project, $injector, ProjectBlockService) {
    super($injector);

    this.$log = $log;
    this.$state = $state;
    this.ProjectBlockService = ProjectBlockService;
  }

  back() {
    this.returnToOverview();
  }

  submit() {
    return this.ProjectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, true);
  }
}

ProgressUpdatesCtrl.$inject = ['$state', '$log', 'project', '$injector', 'ProjectBlockService'];

angular.module('GLA')
  .controller('ProgressUpdatesCtrl', ProgressUpdatesCtrl);
