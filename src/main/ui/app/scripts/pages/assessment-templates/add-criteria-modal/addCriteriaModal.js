/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function AddCriteriaModal($uibModal, $rootScope, AssessmentService) {
  return {
    show: function (assessmentTemplate, criteria) {
      var modal = $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/assessment-templates/add-criteria-modal/addCriteriaModal.html',
        size: 'md',
        resolve: {
          assessmentTemplate: () => {
            return assessmentTemplate;
          },
          criteria: () => {
            return criteria;
          }
        },

        controller: ['$uibModalInstance', 'assessmentTemplate', 'criteria', function ($uibModalInstance, assessmentTemplate, criteria) {
          this.assessmentTemplate = assessmentTemplate;

          this.criteria = criteria;
          if (!this.criteria) {
            this.criteria = {};
          }

          this.criteriaModel = {};
          this.criteriaModel.id                   = this.criteria.id;
          this.criteriaModel.title                = this.criteria.title;
          this.criteriaModel.weight               = this.criteria.weight;
          this.criteriaModel.commentsRequirement  = this.criteria.commentsRequirement;
          this.criteriaModel.answerType           = this.criteria.answerType;

          this.commentsRequirements = AssessmentService.getCommentsRequirements();

          this.answerTypes = AssessmentService.getAnswerTypes();
          if (this.assessmentTemplate.scores.length < 2) {
            this.answerTypes = _.reject(this.answerTypes, function(at){ return at.value === 'Score'; });
          }

          this.addCriteria = () => {
            this.criteria.title               = this.criteriaModel.title;
            this.criteria.weight              = this.criteriaModel.weight;
            this.criteria.commentsRequirement = this.criteriaModel.commentsRequirement;
            this.criteria.answerType          = this.criteriaModel.answerType;
            $uibModalInstance.close(this.criteria);
          };

          this.onAnswerTypeChange = () => {
            if(this.criteriaModel && this.criteriaModel.answerType !== 'Score') {
              this.criteriaModel.weight = null;
            }
          }
        }]
      });

      return modal;
    }
  };
}

AddCriteriaModal.$inject = ['$uibModal', '$rootScope', 'AssessmentService'];

angular.module('GLA')
  .service('AddCriteriaModal', AddCriteriaModal);
