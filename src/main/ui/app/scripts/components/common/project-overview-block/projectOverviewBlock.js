/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

var gla = angular.module('GLA');

class ProjectOverviewBlockCtrl {
  constructor(){
    this.complete = this.block.complete;
    this.projectIsActive = this.projectStatus === 'Active';
    this.projectIsClosed = this.projectStatus === 'Closed';
    this.isBlockUnapproved = !this.isLandProject && this.block.blockStatus === 'UNAPPROVED';
  }
};

ProjectOverviewBlockCtrl.$inject = [];

gla.component('projectOverviewBlock', {
  controller: ProjectOverviewBlockCtrl,
  bindings: {
    blockNumber: '<?',
    complete: '<?',
    block: '<',
    projectStatus: '<',
    isLandProject: '<',
  },
  templateUrl: 'scripts/components/common/project-overview-block/projectOverviewBlock.html',
  transclude: true
});
