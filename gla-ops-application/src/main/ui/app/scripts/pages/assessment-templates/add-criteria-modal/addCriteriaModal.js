/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function AddCriteriaModal($uibModal, $rootScope, AssessmentService) {
  return {
    show: function (assessmentTemplate, criteria, section) {
      var modal = $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/assessment-templates/add-criteria-modal/addCriteriaModal.html',
        size: 'md',
        backdrop  : 'static',
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
            this.criteria.displayOrder = ((_.maxBy(section.criteriaList, 'displayOrder') || {}).displayOrder || 0) + 1;
          }

          let defaultModel                        = {criteriaAnswerOptions: []};
          this.criteriaModel                      = this.criteria.id ? this.criteria : defaultModel;
          this.criteriaModel.id                   = this.criteria.id;
          this.criteriaModel.title                = this.criteria.title;
          this.criteriaModel.weight               = this.criteria.weight;
          this.criteriaModel.commentsRequirement  = this.criteria.commentsRequirement;
          this.criteriaModel.displayOrder         = this.criteria.displayOrder;
          this.criteriaModel.answerType           = this.criteria.answerType;
          this.commentsRequirements               = AssessmentService.getCommentsRequirements();
          this.answerTypes                        = AssessmentService.getAnswerTypes();

          if (this.assessmentTemplate.scores.length < 2) {
            this.answerTypes = _.reject(this.answerTypes, function(at){ return at.value === 'Score'; });
          }

          this.addCriteria = () => {
            this.criteria.title               = this.criteriaModel.title;
            this.criteria.weight              = this.criteriaModel.weight;
            this.criteria.commentsRequirement = this.criteriaModel.commentsRequirement;
            this.criteria.answerType          = this.criteriaModel.answerType;

            if (this.criteria.answerType === 'Dropdown') {
              let i=1;
              this.criteriaModel.criteriaAnswerOptions.forEach(ao => ao.displayOrder = i++);
            }
            this.criteria.criteriaAnswerOptions     = this.criteriaModel.criteriaAnswerOptions;
            $uibModalInstance.close(this.criteria);
          };

          this.validateDisplayOrder = (displayOrder) => {
            this.isDuplicateDisplayOrder = _.some(section.criteriaList, (criteria) =>{
              return criteria.displayOrder === displayOrder;
            });
          };

          this.addNewDropdownOption = () => {
            this.criteriaModel.criteriaAnswerOptions.push({});
          };

          this.deleteDropdownOption = (index) => {
            this.criteriaModel.criteriaAnswerOptions.splice(index, 1);
          };

          this.isCriteriaAnswerOptionValid = (criteriaModel) => {
            if(!criteriaModel.title || !criteriaModel.commentsRequirement || !criteriaModel.answerType || this.isDuplicateDisplayOrder){
              return false;
            }
            if (criteriaModel.answerType === 'Dropdown' && this.getValidOptions(criteriaModel).length < 2) {
              return false;
            }
            return true;
          };

          this.getValidOptions = (criteriaModel) => {
            let optionsWithText = _.filter(criteriaModel.criteriaAnswerOptions, cao => !!cao.title);
            return _.uniqBy(optionsWithText, 'title');
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
