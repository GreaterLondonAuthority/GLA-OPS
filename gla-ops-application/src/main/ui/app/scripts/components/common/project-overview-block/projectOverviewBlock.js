/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


var gla = angular.module('GLA');

class ProjectOverviewBlockCtrl {
  constructor($scope) {
    this.$scope = $scope;
  }

  $onInit($scope){
    this.$scope.$watch('$ctrl.block', block => {
      this.init();
    }, true);
  }

  init(){
    this.complete = this.block.complete;
    this.projectIsActive = this.projectStatus === 'Active';
    this.projectIsClosed = this.projectStatus === 'Closed';
    this.isBlockUnapproved = !this.isLandProject && this.block.blockStatus === 'UNAPPROVED';
    this.isBlockApproved = !this.isBlockUnapproved;
    this.icon = this.getIcon();
    this.blockState = this.getBlockState();
    this.status = this.getStatus();
    this.banner = this.getBanner();
  }

  isActiveOrClosed() {
    return this.projectIsActive || this.projectIsClosed;
  }

  isGreenTheme() {
    return this.isActiveOrClosed() && this.isBlockApproved && !(this.isLandProject && !this.complete)
  }

  getBlockState() {
    return this.isGreenTheme() ? 'valid' : 'invalid'
  }

  getStatus() {
    if (!this.isActiveOrClosed()) {
      return this.complete ? 'COMPLETE' : 'INCOMPLETE';
    } else if (this.projectIsActive && this.isLandProject) {
      return null;
    }
    return this.isBlockUnapproved ? 'UNAPPROVED' : 'APPROVED'
  }

  getIcon() {
    if (!this.isActiveOrClosed() && this.complete || this.isGreenTheme()) {
      return 'glyphicon-ok';
    } else if (!this.isLandProject) {
      return 'glyphicon-exclamation-sign';
    }
  }

  getBanner() {
    return (this.isActiveOrClosed() && !this.complete) ? 'INCOMPLETE' : null
  }
}

ProjectOverviewBlockCtrl.$inject = ['$scope'];

gla.component('projectOverviewBlock', {
  controller: ProjectOverviewBlockCtrl,
  bindings: {
    blockNumber: '<?',
    block: '<',
    projectStatus: '<',
    isLandProject: '<',
  },
  templateUrl: 'scripts/components/common/project-overview-block/projectOverviewBlock.html',
  transclude: true
});
