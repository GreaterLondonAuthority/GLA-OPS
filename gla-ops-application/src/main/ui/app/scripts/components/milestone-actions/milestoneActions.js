/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class MilestoneActions {
  constructor() {
  }

}

MilestoneActions.$inject = [];



gla.component('milestoneActions', {
  bindings: {
    milestone: '<',
    readOnly: '<',
    onClaimCancel: '&',
    onClaim: '&',
    onWithdraw: '&',
    onCancelWithdraw: '&',
    onRepay: '&'
  },
  controller: MilestoneActions,
  templateUrl: 'scripts/components/milestone-actions/milestoneActions.html'
});
