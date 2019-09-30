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
        let blockType = block.blockType.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
        $state.go(`project.block.${blockType}`, {
          'projectId': $stateParams.projectId,
          'blockPosition': $stateParams.blockPosition,
          'blockId': block.id,
          'displayOrder': block.displayOrder,
          'yearAvailableFrom': block.yearAvailableFrom
        });
      },
      params: {
        version: null,
        afterEdit: null,
        displayOrder:null
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
        payments: ['$stateParams', 'PaymentService', ($stateParams, PaymentService) => {
          let paymentStatuses = PaymentService.authorisedStatuses();
          return PaymentService.getPayments($stateParams.projectId, null, null, paymentStatuses);
        }],
        claimFeatureEnabled: ['FeatureToggleService', (FeatureToggleService) => {
          return FeatureToggleService.isFeatureEnabled('payments').then(rsp => rsp.data);
        }],
        isMonetaryValueReclaimsEnabled: ['FeatureToggleService', (FeatureToggleService) => {
          return FeatureToggleService.isFeatureEnabled('MonetaryValueReclaims').then(rsp => rsp.data);
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
      templateUrl: 'scripts/pages/project/questions/questionsPage.html',
      controller: 'QuestionsPageCtrl',
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
      resolve: blockResolves({
        outputsMessage: ['ConfigurationService', (ConfigurationService) => {
          return ConfigurationService.getMessage('outputs-baseline-message-key').then(rsp => rsp.data);
        }],

        currentFinancialYear: ['ProjectService', (ProjectService) => {
          return ProjectService.getCurrentFinancialYear().then(rsp => rsp.data);
        }]
       }
      )
    })

    .state('project.block.outputs-costs', {
      templateUrl: 'scripts/pages/project/outputs-costs/outputsCosts.html',
      controller: 'OutputsCostsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.receipts', {
      templateUrl: 'scripts/pages/project/receipts/receipts.html',
      controller: 'ReceiptsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project.block.progress-updates', {
      templateUrl: 'scripts/pages/project/progress-updates/progressUpdates.html',
      controller: 'ProgressUpdatesCtrl',
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
    })
    .state('project.block.funding', {
      template: '<project-funding-page project="$resolve.project" project-funding="$resolve.projectFunding" current-financial-year="$resolve.currentFinancialYear" template="$resolve.template"></project-funding-page>',
      resolve: blockResolves({
        projectFunding: ['$sessionStorage', '$stateParams', 'ProjectFundingService', 'currentFinancialYear', ($sessionStorage, $stateParams, ProjectFundingService, currentFinancialYear)=>{
          // this.$sessionStorage[this.blockId]

          // let year = $stateParams.yearAvailableFrom > 0 ? currentFinancialYear + $stateParams.yearAvailableFrom : $stateParams.selectedYear ||
          let year = $stateParams.selectedYear ||
            $sessionStorage[$stateParams.blockId] && $sessionStorage[$stateParams.blockId].selectedYear ||
            currentFinancialYear;

          return ProjectFundingService.getProjectFunding($stateParams.projectId, $stateParams.blockId, year).then((resp)=>{
            return resp.data;
          });
        }],
        currentFinancialYear: ['ProjectService', (ProjectService)=>{
          return ProjectService.getCurrentFinancialYear()
            .then(resp => {
              return resp.data;
            });
        }]
      }),
      params:{
        selectedYear: undefined,
        yearAvailableFrom: null
      }
    })

    .state('project.block.learning-grant', {
      template: `<learning-grant project="$resolve.project"
                                 learning-grant="$resolve.learningGrant"
                                 payments-enabled="$resolve.paymentsEnabled"
                                 template="$resolve.template"
                                 current-academic-year="$resolve.currentAcademicYear"></learning-grant>`,
      resolve: blockResolves({
        learningGrant: ['$stateParams', '$sessionStorage', 'ProjectSkillsService', ($stateParams, $sessionStorage, ProjectSkillsService)=>{
          let year = $sessionStorage[$stateParams.blockId] && $sessionStorage[$stateParams.blockId].selectedYear;
          return ProjectSkillsService.getLearningGrantBlock($stateParams.projectId, year).then(resp => resp.data);
        }],
        paymentsEnabled: ['FeatureToggleService', (FeatureToggleService) => {
          return FeatureToggleService.isFeatureEnabled('SkillsPayments').then(rsp => rsp.data);
        }],
        currentAcademicYear: ['SkillProfilesService', (SkillProfilesService) => {
          return SkillProfilesService.getCurrentAcademicYear().then(rsp => rsp.data);
        }]
      }),
      params:{
        selectedYear: undefined
      }
    })

    .state('project.block.subcontracting', {
      template: `<subcontractors project="$resolve.project"
                                 template="$resolve.template",
                                 deliverable-types="$resolve.deliverableTypes"></subcontractors>`,
      resolve: blockResolves({
        deliverableTypes: ['TemplateService', 'template', (TemplateService, template) => {
          return TemplateService.getAvailableDeliverableTypes(template.id).then(rsp => rsp.data);
        }]
      })
    })

    .state('project.block.funding-claims', {
      template: `<funding-claims project="$resolve.project"
                                   funding-claims="$resolve.fundingClaims"
                                   learning-grant="$resolve.learningGrant"
                                   template="$resolve.template"
                                   current-academic-year="$resolve.currentAcademicYear"></funding-claims>`,
      resolve: blockResolves({
        fundingClaims: ['$stateParams', '$sessionStorage', 'ProjectSkillsService', ($stateParams, $sessionStorage, ProjectSkillsService)=>{
          return ProjectSkillsService.getFundingClaimsBlock($stateParams.projectId).then(resp => resp.data);
        }],
        learningGrant: ['$stateParams', '$sessionStorage', 'ProjectSkillsService', ($stateParams, $sessionStorage, ProjectSkillsService)=>{
          let year = $sessionStorage[$stateParams.blockId] && $sessionStorage[$stateParams.blockId].selectedYear;
          return ProjectSkillsService.getLearningGrantBlock($stateParams.projectId, year).then(resp => resp.data);
        }],
        currentAcademicYear: ['SkillProfilesService', (SkillProfilesService) => {
          return SkillProfilesService.getCurrentAcademicYear().then(rsp => rsp.data);
        }]
      }),
      params:{
        selectedYear: undefined
      }
    });
}]);

function blockResolves(customResolves) {
  let resolve = {
    history: ['$stateParams', 'ProjectBlockService', 'project', ($stateParams, ProjectBlockService, project) => {
      return ProjectBlockService.getHistory($stateParams.projectId, $stateParams.displayOrder).then(rsp => rsp.data);
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
    }],

    isBlockRevertEnabled: ['FeatureToggleService', (FeatureToggleService) => {
      return FeatureToggleService.isFeatureEnabled('AllowBlockRevert').then(rsp => rsp.data);
    }]
  };

  _.merge(resolve, customResolves);

  return resolve;
}
