/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

angular.module('GLA').config(['$stateProvider', function ($stateProvider) {
  $stateProvider
    .state('project.block', {
      url: '/blocks/:blockPosition?version',
      template: '<ui-view class="project-block"></ui-view>',
      controller: function (project, $state, $stateParams) {
        let block = project.projectBlocksSorted[$stateParams.blockPosition - 1];
        if (!$stateParams.blockClick) {
          let blockType = block.blockType.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
          $state.go(`project.block.${blockType}`, {
            'projectId': $stateParams.projectId,
            'blockPosition': $stateParams.blockPosition,
            'blockId': block.id
          });
        }
      },
      params: {
        blockClick: false,
        version: null
      }
    })

    .state('project.block.details', {
      templateUrl: 'scripts/pages/project/details/projectDetails.html',
      controller: 'ProjectDetailsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({
        organisationGroup: ['$stateParams', 'OrganisationGroupService', '$rootScope', 'project', ($stateParams, OrganisationGroupService, $rootScope, project) => {
          return (project.organisationGroupId ? OrganisationGroupService.findById(project.organisationGroupId).then(resp => resp.data) : null);
        }],

        organisationGroups: ['OrganisationGroupService', 'project', (OrganisationGroupService, project) => {
          return OrganisationGroupService.getOrganisationGroupsForOrg(project.organisation.id, project.programme.id).then(resp => resp.data);
        }],

        boroughs: ['ReferenceDataService', (ReferenceDataService) => {
          return ReferenceDataService.getBoroughs();
        }]
      })
    })

    .state('project.block.milestones', {
      templateUrl: 'scripts/pages/project/milestones/projectMilestones.html',
      controller: 'ProjectMilestonesCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({
        // milestoneBlock: ['MilestonesService', '$stateParams', (MilestonesService, $stateParams) => {
        //   return MilestonesService.getMilestoneBlock($stateParams.projectId, $stateParams.blockId).then(res => res.data);
        // }],
        // milestoneBlockHistory: ['ProjectBlockService', '$stateParams', (ProjectBlockService, $stateParams) => {
        //   return ProjectBlockService.getHistory($stateParams.projectId, $stateParams.blockId).then(rsp => rsp.data);
        //   // return MilestonesService.getMilestoneBlock($stateParams.projectId, $stateParams.blockId).then(res => res.data);
        // }],
        payments: ['$stateParams', 'PaymentService', ($stateParams, PaymentService) => {
          let paymentStatuses = PaymentService.authorisedStatuses();
          return PaymentService.getPayments($stateParams.projectId, null, paymentStatuses);
        }]
      })
    })

    .state('project.block.project-budgets', {
      templateUrl: 'scripts/pages/project/project-budget/projectBudget.html',
      controller: 'ProjectBudgetCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.questions', {
      templateUrl: 'scripts/pages/project/questions/questions.html',
      controller: 'QuestionsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.calculate-grant', {
      templateUrl: 'scripts/pages/project/grant/calculate-grant/calculateGrant.html',
      controller: 'CalculateGrantCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.negotiated-grant', {
      templateUrl: 'scripts/pages/project/grant/negotiated-grant/negotiatedGrant.html',
      controller: 'NegotiatedGrantCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.developer-led-grant', {
      templateUrl: 'scripts/pages/project/grant/developer-led-grant/developerLedGrant.html',
      controller: 'DeveloperLedGrantCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.indicative-grant', {
      templateUrl: 'scripts/pages/project/grant/indicative-grant/indicativeGrant.html',
      controller: 'IndicativeGrantCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.design-standards', {
      templateUrl: 'scripts/pages/project/design-standards/designStandards.html',
      controller: 'DesignStandardsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.grant-source', {
      templateUrl: 'scripts/pages/project/grant-source/grantSource.html',
      controller: 'GrantSourceCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.outputs', {
      templateUrl: 'scripts/pages/project/outputs/outputs.html',
      controller: 'OutputsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.receipts', {
      templateUrl: 'scripts/pages/project/receipts/receipts.html',
      controller: 'ReceiptsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.unit-details', {
      templateUrl: 'scripts/pages/project/units/units.html',
      controller: 'UnitsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({
        unitsMetadata: ['$stateParams', 'UnitsService', ($stateParams, UnitsService) => {
          return UnitsService.getUnitsMetadata($stateParams.projectId).then(resp => resp.data);
        }]
      })
    })

    .state('project.block.risks', {
      templateUrl: 'scripts/pages/project/risks/risks.html',
      controller: 'RisksCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({
        riskCategories: ['RisksService', (RisksService)=>{
          return RisksService.getRiskCategories();
        }]
      })
    });
}]);

function blockResolves(customResolves) {
  let resolve = {
    history: ['$stateParams', 'ProjectBlockService', 'project', ($stateParams, ProjectBlockService, project) => {
      if (project.status.toLowerCase() === 'active') {
        return ProjectBlockService.getHistory($stateParams.projectId, $stateParams.blockId).then(rsp => rsp.data);
      }
      return [];
    }],

    block: ['history', '$stateParams', 'ProjectService', (history, $stateParams, ProjectService) => {
      let blockId = null;
      if ($stateParams.version) {
        const version = +$stateParams.version;
        let block = _.find(history, {blockVersion: version});
        blockId = block.blockId;
      }

      if (blockId && blockId !== $stateParams.blockId) {
        return ProjectService.getProjectBlock($stateParams.projectId, blockId).then(rsp => rsp.data);
      }
    }]
  };

  _.merge(resolve, customResolves);

  return resolve;
}
