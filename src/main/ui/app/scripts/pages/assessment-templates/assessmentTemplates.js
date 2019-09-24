/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class AssessmentTemplatesCtrl {
  constructor($state, AssessmentService, PortableEntityService, ToastrUtil) {
    this.$state = $state;
    this.AssessmentService = AssessmentService;
    this.PortableEntityService = PortableEntityService;
    this.ToastrUtil = ToastrUtil;
  }

  $onInit() {

  }

  paste() {
    navigator.clipboard.readText().then(json => {
      if (this.isValidJson(json)) {
        this.PortableEntityService.saveSanitisedEntity('AssessmentTemplate', json).then(() => {
          this.ToastrUtil.success('Successfully pasted assessment template');
          this.AssessmentService.getAssessmentTemplates().then(rsp => this.assessmentTemplates = rsp.data);
        });
      } else {
        this.ToastrUtil.warning('Invalid JSON pasted');
      }
    }).catch(err => {
      this.ToastrUtil.warning('Failed to get pasted JSON');
    });
  }

  isValidJson(json) {
    try {
      JSON.parse(json);
    } catch (e) {
      return false;
    }
    return true;
  }

}

AssessmentTemplatesCtrl.$inject = ['$state', 'AssessmentService', 'PortableEntityService', 'ToastrUtil'];

angular.module('GLA')
  .component('assessmentTemplates', {
    templateUrl: 'scripts/pages/assessment-templates/assessmentTemplates.html',
    bindings: {
      assessmentTemplates: '<'
    },
    controller: AssessmentTemplatesCtrl
  });
