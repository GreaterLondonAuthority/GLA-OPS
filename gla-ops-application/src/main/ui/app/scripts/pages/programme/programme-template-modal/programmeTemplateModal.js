/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function ProgrammeTemplateModal($uibModal) {
  return {
    show: function (availableTemplates) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/programme/programme-template-modal/programmeTemplateModal.html',
        size: 'md',
        backdrop  : 'static',
        controller: function () {
          this.availableTemplates = availableTemplates;
        }
      });
    }
  };
}

ProgrammeTemplateModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('ProgrammeTemplateModal', ProgrammeTemplateModal);
