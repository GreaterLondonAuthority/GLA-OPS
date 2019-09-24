/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function ProjectMilestoneModal($uibModal, $timeout) {
  return {
    show: function (milestone, existingMilestones, isMonetaryValueType, showDescription, descriptionHintText) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/milestones/projectMilestonesModal.html',
        size: 'md',
        controller: [function () {
          this.isMonetaryValueType = isMonetaryValueType;
          this.showDescription = showDescription;
          this.descriptionHintText = descriptionHintText;
          this.milestone = milestone;
          this.existingNamesMap = (existingMilestones || []).reduce((result, milestone) =>{
            result[milestone.summary.toLowerCase().trim()] = true;
            return result;
          }, {});
          var ctrl = this;
          $timeout(function(){
            ctrl.focusSummary = true;
          },100);

          this.onMilestoneNameKeyUp = (milestoneName) => {
            let name = (milestoneName || '').toLowerCase().trim();
            this.duplicateName = this.existingNamesMap[name];
          }
        }]
      });
    }
  }
}

ProjectMilestoneModal.$inject = ['$uibModal', '$timeout'];

angular.module('GLA')
  .service('ProjectMilestoneModal', ProjectMilestoneModal);
