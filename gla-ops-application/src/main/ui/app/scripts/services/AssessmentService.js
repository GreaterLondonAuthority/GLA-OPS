/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const INTERNAL_ASSESSMENT_BLOCK_TYPE = 'Assessment';
const STATUS = {
  InProgress: 'InProgress',
  Completed: 'Completed',
  Abandoned: 'Abandoned'
};

AssessmentService.$inject = ['$http', 'config'];

function AssessmentService($http, config) {
  return {

    searchOptions() {
      let res = [
        {
          name: 'created_by',
          description: 'Created by',
          hint: 'Enter name',
          maxLength: '50'
        },
        {
          name: 'project',
          description: 'Project',
          hint: 'Enter project title or ID',
          maxLength: '50'
        }];

      return res;
    },

    getAssessmentStatusOptions() {
      return [{
        checkedClass: 'InProgress',
        ariaLabel: 'In Progress',
        name: 'inProgress',
        model: true,
        label: 'In Progress',
        key: 'InProgress',
      }, {
        checkedClass: 'completed',
        ariaLabel: 'Completed',
        name: 'completed',
        model: true,
        label: 'Completed',
        key: 'Completed',
      },{
        checkedClass: 'abandoned',
        ariaLabel: 'Abandoned',
        name: 'abandoned',
        model: false,
        label: 'Abandoned',
        key: 'Abandoned',
      }]
    },

    getAssessmentTemplates(obj) {
      obj = obj || {};
      return $http.get(`${config.basePath}/assessmentTemplates`,{
        params:{
          programmeId: obj.programmeId,
          templateId: obj.templateId,
          managingOrgId: obj.managingOrgId
        }
      }).then(rsp => {
        _.forEach(rsp.data, assessment=>{
          this.addStatusLabel(assessment);
        });

        return rsp;
      });
    },

    assessmentTemplatesForUser(obj) {
      return $http.get(`${config.basePath}/assessmentTemplatesForUser`,{params:obj}).then(rsp => {
        _.forEach(rsp.data, assessment=>{
          this.addStatusLabel(assessment);
        });
        return rsp;
      });
    },

     getAssessmentTemplateSummaries() {
      return $http.get(`${config.basePath}/assessmentTemplatesSummary`).then(rsp => {
        _.forEach(rsp.data, assessment=>{
          this.addStatusLabel(assessment);
        });

        return rsp;
      });
    },

    statusLabelMap: {
      ReadyForUse: 'Ready for use',
      InUse: 'In use'
    },

    addStatusLabel(data){
      const status = data.status;
      if(data.used){
        data.statusLabel = this.statusLabelMap.InUse;
      }else{
        data.statusLabel = this.statusLabelMap[status] || status;
      }
      return data;
    },

    getAssessmentTemplate(id) {
      return $http.get(`${config.basePath}/assessmentTemplates/${id}`).then(rsp=>{
        this.addStatusLabel(rsp.data);
        return rsp;
      });
    },

    // getAssessmentTemplates(programmeId, templateId) {
    //   return $http.get(`${config.basePath}/assessmentTemplates?programmeId=${programmeId}&templateId=${templateId}`);
    // },

    getAssessment(id) {
      return $http.get(`${config.basePath}/assessments/${id}`);
    },

    getAssessmentsPerPage(createdBy, projectNameOrID, assessmentTypeOptions, assessmentStatusOptions, programmesIds, projectStatuses, page) {
      let cfg = {
        params: {
          createdBy: createdBy,
          project: projectNameOrID,
          assessmentTemplates: assessmentTypeOptions,
          assessmentStatuses: assessmentStatusOptions,
          assessmentType: assessmentStatusOptions,
          programmes: programmesIds,
          projectStatuses: projectStatuses,
          page: page,
          size: 50,
          sort: 'modifiedOn,desc'
        }
      };
      return $http.get(`${config.basePath}/assessments`, cfg)
    },

    saveAssessmentTemplate(assessmentTemplate) {

      if (assessmentTemplate.id) {
        return $http.put(`${config.basePath}/assessmentTemplates/${assessmentTemplate.id}`, assessmentTemplate)
      }
      else {
        return $http.post(`${config.basePath}/assessmentTemplates`, assessmentTemplate)
      }
    },

    saveAssessment(projectId, assessment) {
      if (assessment.id) {
        return $http.put(`${config.basePath}/assessments/${assessment.id}`, assessment)
      }
      else {
        return $http.post(`${config.basePath}/projects/${projectId}/assessments`, assessment)
      }
    },

    changeStatus(id, status){
      if(!STATUS[status]){
        throw Error(`Unsupported status: ${status}. Available statuses: ${STATUS.join(', ')}`)
      }
      return $http.put(`${config.basePath}/assessments/${id}/status`, status);
    },

    abandonAssessment(id) {
      return this.changeStatus(id, STATUS.Abandoned);
    },

    getCommentsRequirements() {
      return [{
        label: 'Not applicable',
        value: 'hidden'
      },{
        label: 'Mandatory',
        value: 'mandatory'
      },{
        label: 'Optional',
        value: 'optional'
      }];
    },

    getCommentsRequirementLabel(value) {
      return _.find(this.getCommentsRequirements(), {value: value}).label || '';
    },

    getAnswerTypes() {
      return [{
        label: 'For information only',
        value: 'InfoOnly'
      },{
        label: 'Score',
        value: 'Score'
      },{
        label: 'Pass/Fail',
        value: 'PassFail'
      },{
        label: 'Number',
        value: 'Number'
      },{
        label: 'Drop down',
        value: 'Dropdown'
      }];
    },

    getFailedCriteriaDropdown() {
      return [
        {
          label: 'Pass',
          value: false
        }, {
          label: 'Fail',
          value: true
        },
      ]
    },

    getOutcomeTypes() {
      return [
        {
          label: 'Proceed',
          value: 'Proceed'
        },
        {
          label: 'Do not proceed',
          value: 'DoNotProceed'
        },
        {
          label: 'For information only',
          value: 'ForInformationOnly'
        }
      ]
    },

    getDefaultOutcomeType() {
      return 'ForInformationOnly';
    },

    getFailedCriteriaText(failedCriteriaValue){
      let label = (_.find(this.getFailedCriteriaDropdown(), {value: failedCriteriaValue}) || {}).label;
      return label || 'Not provided';
    },

    getAnswerTypeLabel(value) {
      return _.find(this.getAnswerTypes(), {value: value}).label || '';
    },

    getOutcomeTypeLabel(value) {
      return _.find(this.getOutcomeTypes(), {value: value}).label || '';
    },

    getInternalAssessmentBlockFromProject(project){
      return _.find(project.internalBlocksSorted, {type: INTERNAL_ASSESSMENT_BLOCK_TYPE});
    },

    getAssessmentStatus(assessment) {
      return _.startCase(assessment.status);
    }

  };
}

angular.module('GLA')
  .service('AssessmentService', AssessmentService);
