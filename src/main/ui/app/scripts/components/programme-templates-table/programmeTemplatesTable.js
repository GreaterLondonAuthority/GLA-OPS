/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class ProgrammeTemplatesTable {
  constructor($timeout) {
    this.$timeout = $timeout;
  }

  $onInit() {
    (this.programme.templatesByProgramme || []).forEach(t => {
      //undefined makes dropdown not selected or makes ng-invalid in ui-select
      t.defaultWbsCodeType = t.defaultWbsCodeType || null;
    });
  }

  collapseChanged() {
    this.$timeout(() => {
      this.onCollapseChange();
    })
  }
}

ProgrammeTemplatesTable.$inject = ['$timeout'];


gla.component('programmeTemplatesTable', {
  bindings: {
    programme: '<',
    readOnly: '<',
    projectsCount: '<?',
    glaRoles: '<?',
    onDelete: '&',
    assessmentTemplates: '<',
    onCollapseChange: '&',
    allowChangeInUseAssessmentTemplate: '<'
  },
  controller: ProgrammeTemplatesTable,
  templateUrl: 'scripts/components/programme-templates-table/programeTemplatesTable.html'
});
