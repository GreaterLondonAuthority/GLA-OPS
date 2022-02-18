/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

angular.module('GLA').config(['$stateProvider', function ($stateProvider) {
  const INTERNAL_RISK_ENTITY_TYPE  = 'internalRiskBlock';
  const INTERNAL_QUESTION_ENTITY_TYPE  = 'internalQuestionBlock';
  $stateProvider
    .state('project.internal-risk', {
      url: '/project/:projectId/internal-risk/:blockId',
      templateUrl: 'scripts/pages/project/internal-risk/internalRisk.html',
      controller: 'InternalRiskCtrl',
      controllerAs: '$ctrl',
      resolve: {
        block(RisksService, project) {
          return RisksService.getInternalRiskFromProject(project)
        },

        comments(CommentsService, block) {
          return CommentsService.getInternalComments(block.id, INTERNAL_RISK_ENTITY_TYPE).then(rsp => rsp.data.content);
        },

        pageTitle(block) {
          return _.startCase(block.blockDisplayName);
        }
      }
    })

    .state('project.internal-assessment', {
      url: '/project/:projectId/internal-assessment/:blockId',
      templateUrl: 'scripts/pages/project/internal-assessment/internalAssessment.html',
      controller: 'InternalAssessmentCtrl',
      controllerAs: '$ctrl',
      resolve: {
        block(AssessmentService, project) {
          return AssessmentService.getInternalAssessmentBlockFromProject(project);
        },
        assessments(AssessmentService, project) {
          return AssessmentService.getAssessmentsPerPage(null, project.id, null, ['InProgress', 'Completed'], null, null, null).then(response => response.data.content);
        },
        assessmentTemplates (AssessmentService, project) {
          return AssessmentService.assessmentTemplatesForUser({programmeId: project.programmeId, templateId: project.templateId}).then(rsp => rsp.data);
        },

        pageTitle(block) {
          return _.startCase(block.blockDisplayName)
        }
      }
    })

    .state('project.internal-questions', {
      url: '/project/:projectId/internal-questions/:blockId',
      templateUrl: 'scripts/pages/project/internal-questions/internalQuestions.html',
      controller: 'InternalQuestionsCtrl',
      controllerAs: '$ctrl',
      resolve: {
        block($stateParams, project) {
          return _.find(project.internalBlocksSorted, {id: +$stateParams.blockId});
        },
        comments(CommentsService, block) {
          return CommentsService.getInternalComments(block.id, INTERNAL_QUESTION_ENTITY_TYPE).then(rsp => rsp.data.content);
        },
        pageTitle(block) {
          return _.startCase(block.blockDisplayName)
        }
      }
    })

    .state('project.internal-admin', {
      url: '/project/:projectId/internal-admin/:blockId',
      template: '<internal-project-admin-block [project]="$resolve.project" [block]="$resolve.block"></internal-project-admin-block>',
      resolve: {
        block($stateParams, project) {
          return _.find(project.internalBlocksSorted, {id: +$stateParams.blockId});
        },
        pageTitle(block) {
          return _.startCase(block.blockDisplayName)
        }
      }
    })

}]);

