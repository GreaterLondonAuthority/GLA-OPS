/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class InternalBlocksChangeReport {
  constructor(RisksService) {
    this.RisksService = RisksService;
  }

  $onInit() {
    this.noRatingLabel = this.RisksService.getInternalRiskNoRatingLabel();
    this.block = this.RisksService.getInternalRiskFromProject(this.project);
    if (this.comments && this.comments.length) {
      this.lastComment = this.comments[this.comments.length - 1];
    }
  }
}

InternalBlocksChangeReport.$inject = ['RisksService'];

angular.module('GLA')
  .component('internalBlocksChangeReport', {
    bindings: {
      project: '<',
      comments: '<?'
    },
    templateUrl: 'scripts/pages/change-report/blocks/internalBlocksChangeReport.html',
    controller: InternalBlocksChangeReport
  });
