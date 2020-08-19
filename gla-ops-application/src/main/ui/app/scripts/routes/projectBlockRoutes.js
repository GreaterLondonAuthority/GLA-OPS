/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

angular.module('GLA').config(['$stateProvider', function ($stateProvider) {
  $stateProvider
    .state('project-block', {
      url: '/project/:projectId/blocks/:blockPosition?version',
      template: '<ui-view class="project-block"></ui-view>',

      resolve: {
        resolveState(ProjectService, $state, $stateParams) {
          $stateParams.project = null;
          if ($stateParams.blockAccessByUrl) {
            return ProjectService.getProjectOverview($stateParams.projectId).then(resp => {
              let project = resp.data;
              let block = project.projectBlocksSorted[$stateParams.blockPosition - 1];
              let blockType = block.blockType.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
              let params = angular.copy($stateParams);
              params.blockAccessByUrl = null;
              params.blockId = block.id;
              params.displayOrder = block.displayOrder;
              params.project = null;
              let blockTypesWorkingWithSlowApi = [
                'Milestones',// Uses project.projectBlocksSorted for grant source => Scenario: View Affordable Housing Grant % claim amount (GLA-7050), View u
                'CalculateGrant', // Uses project.projectBlocksSorted to get milestones Scenario: GLA-11141 - Show unit summary of 'claimed' units on the calculate/negotiated/developer-led grant blocks - 1
                'NegotiatedGrant', // Uses project.projectBlocksSorted to get milestones
                'DeveloperLedGrant', //Uses project.projectBlocksSorted to get milestones
                'Outputs', //Uses project.projectBlocksSorted for output costs
                'Funding', //Uses this.project.projectBlocksSorted for milestones
                'OutputsCosts' // uses this.project.projectBlocksSorted for outputsBlock.configGroupId
              ];
              if (blockTypesWorkingWithSlowApi.indexOf(block.blockType) === -1) {
                params.project = project;
              }
              $state.go(`project-block.${blockType}`, params);
              return true;
            });
          }
          return true;
        }
      },

      params: {
        blockId: null,
        project: null,
        version: null,
        afterEdit: null,
        displayOrder: null
      }
    })

    .state('project-block.details', {
      templateUrl: 'scripts/pages/project/details/projectDetails.html',
      controller: 'ProjectDetailsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({
        organisationGroup: ['$stateParams', 'OrganisationGroupService', '$rootScope', 'project', 'block', ($stateParams, OrganisationGroupService, $rootScope, project, block) => {
          return (block.organisationGroupId ? OrganisationGroupService.findById(block.organisationGroupId).then(resp => resp.data) : null);
        }],

        organisationGroups: ['OrganisationGroupService', 'project', (OrganisationGroupService, project) => {
          return OrganisationGroupService.getOrganisationGroupsForOrg(project.organisation.id, project.programme.id).then(resp => resp.data);
        }],

        boroughs: ['ReferenceDataService', 'Downgrade', (ReferenceDataService, Downgrade) => {
          return Downgrade.toPromise(ReferenceDataService.getBoroughs());
        }]
      })
    })

    .state('project-block.milestones', {
      templateUrl: 'scripts/pages/project/milestones/projectMilestones.html',
      controller: 'ProjectMilestonesCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({
        payments: ['$stateParams', 'PaymentService', ($stateParams, PaymentService) => {
          let paymentStatuses = PaymentService.authorisedStatuses();
          return PaymentService.getPayments($stateParams.projectId, null, null, paymentStatuses);
        }],
        claimFeatureEnabled: ['FeatureToggleService', 'Downgrade', (FeatureToggleService, Downgrade) => {
          return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('payments'));
        }],
        isMonetaryValueReclaimsEnabled: ['FeatureToggleService', 'Downgrade', (FeatureToggleService, Downgrade) => {
          return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('MonetaryValueReclaims'));
        }]
      }, true)
    })

    .state('project-block.project-budgets', {
      templateUrl: 'scripts/pages/project/project-budget/projectBudget.html',
      controller: 'ProjectBudgetCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project-block.questions', {
      templateUrl: 'scripts/pages/project/questions/questionsPage.html',
      controller: 'QuestionsPageCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project-block.calculate-grant', {
      templateUrl: 'scripts/pages/project/grant/calculate-grant/calculateGrant.html',
      controller: 'CalculateGrantCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({}, true)
    })

    .state('project-block.negotiated-grant', {
      templateUrl: 'scripts/pages/project/grant/negotiated-grant/negotiatedGrant.html',
      controller: 'NegotiatedGrantCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({}, true)
    })

    .state('project-block.developer-led-grant', {
      templateUrl: 'scripts/pages/project/grant/developer-led-grant/developerLedGrant.html',
      controller: 'DeveloperLedGrantCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({}, true)
    })

    .state('project-block.indicative-grant', {
      templateUrl: 'scripts/pages/project/grant/indicative-grant/indicativeGrant.html',
      controller: 'IndicativeGrantCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project-block.design-standards', {
      templateUrl: 'scripts/pages/project/design-standards/designStandards.html',
      controller: 'DesignStandardsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project-block.grant-source', {
      templateUrl: 'scripts/pages/project/grant-source/grantSource.html',
      controller: 'GrantSourceCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project-block.outputs', {
      templateUrl: 'scripts/pages/project/outputs/outputs.html',
      controller: 'OutputsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({
          outputsMessage: ['ConfigurationService', (ConfigurationService) => {
            return ConfigurationService.getMessage('outputs-baseline-message-key').then(rsp => rsp.data);
          }],

          currentFinancialYear: ['ProjectService', (ProjectService) => {
            return ProjectService.getCurrentFinancialYear().then(rsp => rsp.data);
          }],

          currentAcademicYear: ['SkillProfilesService', (SkillProfilesService) => {
            return SkillProfilesService.getCurrentAcademicYear().then(rsp => rsp.data);
          }]
        }, true)
    })

    .state('project-block.outputs-costs', {
      templateUrl: 'scripts/pages/project/outputs-costs/outputsCosts.html',
      controller: 'OutputsCostsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({}, true)
    })

    .state('project-block.receipts', {
      templateUrl: 'scripts/pages/project/receipts/receipts.html',
      controller: 'ReceiptsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project-block.progress-updates', {
      templateUrl: 'scripts/pages/project/progress-updates/progressUpdates.html',
      controller: 'ProgressUpdatesCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves()
    })

    .state('project-block.unit-details', {
      templateUrl: 'scripts/pages/project/units/units.html',
      controller: 'UnitsCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({
        unitsMetadata: ['$stateParams', 'UnitsService', ($stateParams, UnitsService) => {
          return UnitsService.getUnitsMetadata($stateParams.projectId).then(resp => resp.data);
        }]
      })
    })

    .state('project-block.risks', {
      templateUrl: 'scripts/pages/project/risks/risks.html',
      controller: 'RisksCtrl',
      controllerAs: '$ctrl',
      resolve: blockResolves({
        riskCategories: ['RisksService', (RisksService) => {
          return RisksService.getRiskCategories();
        }]
      })
    })
    .state('project-block.funding', {
      template: '<project-funding-page project="$resolve.project" project-funding="$resolve.projectFunding" current-financial-year="$resolve.currentFinancialYear" template="$resolve.template"></project-funding-page>',
      resolve: blockResolves({
        projectFunding: ['$sessionStorage', '$stateParams', 'ProjectFundingService', 'currentFinancialYear', 'block', ($sessionStorage, $stateParams, ProjectFundingService, currentFinancialYear, block) => {
          // this.$sessionStorage[this.blockId]

          // let year = $stateParams.yearAvailableFrom > 0 ? currentFinancialYear + $stateParams.yearAvailableFrom : $stateParams.selectedYear ||
          let year = $stateParams.selectedYear ||
            $sessionStorage[$stateParams.blockId] && $sessionStorage[$stateParams.blockId].selectedYear ||
            currentFinancialYear;


          return ProjectFundingService.getProjectFunding($stateParams.projectId, (block || {}).id || $stateParams.blockId, year).then((resp) => {
            return resp.data;
          });
        }],
        currentFinancialYear: ['ProjectService', (ProjectService) => {
          return ProjectService.getCurrentFinancialYear()
            .then(resp => {
              return resp.data;
            });
        }]
      }, true),
      params: {
        selectedYear: undefined
      }
    })

    .state('project-block.learning-grant', {
      template: `<learning-grant project="$resolve.project"
                                 learning-grant="$resolve.learningGrant"
                                 payments-enabled="$resolve.paymentsEnabled"
                                 template="$resolve.template"
                                 current-academic-year="$resolve.currentAcademicYear"></learning-grant>`,
      resolve: blockResolves({
        learningGrant: ['$stateParams', 'block', 'ProjectSkillsService', ($stateParams, block, ProjectSkillsService) => {
          return ProjectSkillsService.getLearningGrantBlock($stateParams.projectId, block.id).then(resp => resp.data);
        }],
        paymentsEnabled: ['FeatureToggleService', 'Downgrade', (FeatureToggleService, Downgrade) => {
          return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('SkillsPayments'));
        }],
        currentAcademicYear: ['SkillProfilesService', (SkillProfilesService) => {
          return SkillProfilesService.getCurrentAcademicYear().then(rsp => rsp.data);
        }]
      }),
      params: {
        selectedYear: undefined
      }
    })

    .state('project-block.delivery-partners', {
      template: `<delivery-partners project="$resolve.project"
                                 template="$resolve.template",
                                 deliverable-types="$resolve.deliverableTypes"></delivery-partners>`,
      resolve: blockResolves({
        deliverableTypes: ['TemplateService', 'template', (TemplateService, template) => {
          return TemplateService.getAvailableDeliverableTypes(template.id).then(rsp => rsp.data);
        }]
      })
    })

    .state('project-block.funding-claims', {
      template: `<funding-claims project="$resolve.project"
                                   funding-claims="$resolve.fundingClaims"
                                   learning-grant="$resolve.learningGrant"
                                   template="$resolve.template"
                                   current-academic-year="$resolve.currentAcademicYear"></funding-claims>`,
      resolve: blockResolves({
        fundingClaims: ['$stateParams', '$sessionStorage', 'ProjectSkillsService', 'block', ($stateParams, $sessionStorage, ProjectSkillsService, block) => {
          // return ProjectSkillsService.getFundingClaimsBlock($stateParams.projectId).then(resp => resp.data);
          return block;
        }],
        currentAcademicYear: ['SkillProfilesService', (SkillProfilesService) => {
          return SkillProfilesService.getCurrentAcademicYear().then(rsp => rsp.data);
        }]
      }),
      params: {
        selectedYear: undefined
      }
    })

    .state('project-block.other-funding', {
      template: `<other-funding project="$resolve.project"
                                template="$resolve.template"></other-funding>`,
      resolve: blockResolves()
    })

    .state('project-block.project-objectives', {
      template: `<objectives-page project="$resolve.project"
                             template="$resolve.template"></objectives-page>`,
      resolve: blockResolves()
    })

    .state('project-block.user-defined-output', {
      template: `<user-defined-outputs-page project="$resolve.project"
                               template="$resolve.template"></user-defined-outputs-page>`,
      resolve: blockResolves()
    })

    .state('project-block.project-elements', {
      template: `<elements-page project="$resolve.project"
                               template="$resolve.template"></elements-page>`,
      resolve: blockResolves()
    });
}]);

function blockResolves(customResolves, requiresSlowApi) {
  let resolve = {

    project: ['$stateParams', 'ProjectService', ($stateParams, ProjectService) => {
      if (!$stateParams.project) {
        if (requiresSlowApi) {
          return ProjectService.getProject($stateParams.projectId).then(rsp => rsp.data);
        } else {
          return ProjectService.getProjectOverview($stateParams.projectId).then(rsp => rsp.data);
        }
      }
      return $stateParams.project;
    }],

    template: ['TemplateService', 'project', (TemplateService, project) => {
      return TemplateService.getTemplate(project.templateId, false, true).then(resp => resp.data);
    }],

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

      //TODO merge if/else, same api
      if (blockId && blockId !== $stateParams.blockId) {
        return ProjectService.getProjectBlock($stateParams.projectId, blockId).then(rsp => rsp.data);
      } else {
        return ProjectService.getProjectBlock($stateParams.projectId, $stateParams.blockId).then(rsp => rsp.data);
      }
    }],

    isBlockRevertEnabled: ['FeatureToggleService', 'Downgrade', (FeatureToggleService, Downgrade) => {
      return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('AllowBlockRevert'));
    }],

    pageTitle: ['block', (block) => {
      return _.startCase(block.blockDisplayName);
    }],
  };

  _.merge(resolve, customResolves);

  return resolve;
}
