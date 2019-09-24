/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class AssessmentTemplateCtrl {
  constructor($state, AssessmentService, PortableEntityService, ToastrUtil) {
    this.$state = $state;
    this.AssessmentService = AssessmentService;
    this.PortableEntityService = PortableEntityService;
    this.ToastrUtil = ToastrUtil;
    this.supported = true;
  }

  $onInit() {
    this.editable = this.allowChangeInUseAssessmentTemplate || !this.assessmentTemplate.used;
    this.readOnly = true;
  }

  onBack() {
    this.$state.go('assessment-templates');
  }

  edit() {
    this.$state.go('assessment-template-edit', {
      id: this.assessmentTemplate.id,
      assessmentTemplate: this.assessmentTemplate
    });
  }

  copyAssessmentTemplate(){
    return this.assessmentTemplateJson;
  }

  success() {
    this.ToastrUtil.success('Copied assessment template');
  };

  fail(err) {
    this.ToastrUtil.warning('An error occured copying assessment template');
  };

}

AssessmentTemplateCtrl.$inject = ['$state', 'AssessmentService', 'PortableEntityService', 'ToastrUtil'];

angular.module('GLA')
  .component('assessmentTemplate', {
    templateUrl: 'scripts/pages/assessment-templates/assessmentTemplate.html',
    bindings: {
      assessmentTemplate: '<',
      assessmentTemplateJson: '<',
      allowChangeInUseAssessmentTemplate: '<',
    },
    controller: AssessmentTemplateCtrl
  });
