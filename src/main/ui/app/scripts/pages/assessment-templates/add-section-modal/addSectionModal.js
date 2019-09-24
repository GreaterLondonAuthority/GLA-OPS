/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function AddSectionModal($uibModal, $rootScope, AssessmentService) {
  return {
    show: function (section, includeWeight) {
      var modal = $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/assessment-templates/add-section-modal/addSectionModal.html',
        size: 'md',
        resolve: {
          section: () => {
            return section;
          }
        },

        controller: ['$uibModalInstance', 'section', function ($uibModalInstance, section) {
          this.section = section;
          if (!this.section) {
            this.section = {};
          }
          this.includeWeight = includeWeight;
          this.sectionModel = {};
          this.sectionModel.id = this.section.id;
          this.sectionModel.title = this.section.title;
          this.sectionModel.weight = this.section.weight;
          this.sectionModel.commentsRequirement = this.section.commentsRequirement;

          this.commentsRequirements = AssessmentService.getCommentsRequirements();

          this.addSection = () => {
            this.section.title = this.sectionModel.title;
            this.section.weight = this.sectionModel.weight;
            this.section.commentsRequirement = this.sectionModel.commentsRequirement;
            $uibModalInstance.close(this.section);
          };

        }]
      });

      return modal;
    }
  };
}

AddSectionModal.$inject = ['$uibModal', '$rootScope', 'AssessmentService'];

angular.module('GLA')
  .service('AddSectionModal', AddSectionModal);
