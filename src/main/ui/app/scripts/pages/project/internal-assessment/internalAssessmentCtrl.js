/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './assessmentTypeModal.js'

class InternalAssessmentCtrl {
  constructor($state, project, block, template, ProjectBlockService, AssessmentService, UserService, assessmentTemplates, AssessmentTypeModal) {
    this.assessmentTemplates = assessmentTemplates;
    this.$state = $state;
    this.project = project;
    this.block = block;
    this.template = template;
    this.ProjectBlockService = ProjectBlockService;
    this.AssessmentService = AssessmentService;
    this.UserService = UserService;
    this.AssessmentTypeModal = AssessmentTypeModal;
  }

  $onInit() {
    this.readOnly = true;
    this.title = _.startCase(this.block.blockDisplayName.toLowerCase());
    if(this.UserService.hasPermission('assessment.manage') && this.assessmentTemplates.length){
      this.createBtnName = 'START ASSESSMENT'
    }
  }

  back() {
    this.$state.go('project-overview', {projectId: this.project.id}, {reload: true});
  }

  getStatus(status) {
    return _.startCase(status);
  }

  getProjectStatus(assessment) {
    let status = assessment.projectStatus;
    let subStatus = assessment.projectSubStatus;
    return subStatus ? `${status}: ${_.startCase(subStatus)}` : status;
  }

  showAssessmentTypeModal() {
    if (this.assessmentTemplates.length > 1) {
      let modal = this.AssessmentTypeModal.show(this.assessmentTemplates);
      return modal.result.then(assessmentTemplate => this.createAssessment(assessmentTemplate));
    } else {
      return this.createAssessment(this.assessmentTemplates[0]);
    }
  }

  createAssessment(assessmentTemplate){
    console.log('selected value: ', assessmentTemplate);
    let assessment = {
      assessmentTemplate: assessmentTemplate
    };
    return this.AssessmentService.saveAssessment(this.project.id, assessment).then(resp => {
      return this.$state.go('assessment-edit', {
        id: resp.data.id,
        assessment: resp.data
      });
    });
  }

}

InternalAssessmentCtrl.$inject = ['$state', 'project', 'block', 'template', 'ProjectBlockService', 'AssessmentService', 'UserService', 'assessmentTemplates', 'AssessmentTypeModal'];

angular.module('GLA')
  .controller('InternalAssessmentCtrl', InternalAssessmentCtrl);
