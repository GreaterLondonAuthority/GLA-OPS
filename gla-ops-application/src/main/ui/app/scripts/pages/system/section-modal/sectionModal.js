/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function SectionModal($uibModal, QuestionsService) {

  return {
    show: function (section, sections) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/system/section-modal/sectionModal.html',
        size: 'md',
        resolve: {},

        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.originalSection = section;
          this.section = angular.copy(section || {});
          this.isUpdate =  this.section && this.section.displayOrder != undefined;
          // If section already exists, update otherwise add
          if (this.isUpdate) {
            this.btnName = 'Update';
            this.isDuplicateDisplayOrder = false;
            this.isDuplicateExternalId = false;
          } else {
            this.btnName = 'Add';
            this.section.id = null;
            this.isIdEditable = true;
            this.section.displayOrder = ((_.maxBy(sections, 'displayOrder') || {}).displayOrder || 0) + 1;
          }

          this.validateDisplayOrder = (displayOrder) => {
            this.isDuplicateDisplayOrder = _.some(sections, (section) => {
              return section.displayOrder === displayOrder && section != this.originalSection;
            });
          },
          this.validateExternalId = (externalId) => {
            this.isDuplicateExternalId = _.some(sections, (section) => {
              return section.externalId === externalId && section != this.originalSection;
            });
          },
          this.addButtonEnabled = () => {
            return this.section.text && this.section.displayOrder && !this.isDuplicateDisplayOrder && this.section.externalId && !this.isDuplicateExternalId
          },
          this.addSection = () => {
            this.$close(this.section)
          }
        }]
      });
    },

  };
}

SectionModal.$inject = ['$uibModal', 'QuestionsService'];

angular.module('GLA')
  .service('SectionModal', SectionModal);
