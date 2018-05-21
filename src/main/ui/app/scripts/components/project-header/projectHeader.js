/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class ProjectHeaderCtrl {
  get hasVersion(){
    return this.editableBlock && this.editableBlock.approvalTime
  }
}

ProjectHeaderCtrl.$inject = ['$scope'];


angular.module('GLA')
  .component('projectHeader', {
  templateUrl: 'scripts/components/project-header/projectHeader.html',
  transclude: true,
  bindings: {
    onBack: '&?',
    editableBlock: '<?',
    mouseDownState: '=',
    subtitle: '@',
    project: '<?',
    // showProjectMenu: '<?'
    linkMenuItems: '<?',
    actionMenuItems: '<?',
    onActionClicked: '&?'
  },
  controller: ProjectHeaderCtrl
});
