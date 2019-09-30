/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

angular.module('GLA').config(['$stateProvider', function ($stateProvider) {
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
          return CommentsService.getInternalRiskComments(block.id).then(rsp => rsp.data.content);
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
        assessmentTemplates (AssessmentService, project) {
          return AssessmentService.assessmentTemplatesForUser({programmeId: project.programmeId, templateId: project.templateId}).then(rsp => rsp.data);
        }
      }
    })
}]);

