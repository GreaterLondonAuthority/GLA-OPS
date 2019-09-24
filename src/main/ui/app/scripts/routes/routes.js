/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './projectBlockRoutes'
import './projectInternalBlockRoutes'

angular.module('GLA')
  .config(['$stateProvider', '$urlRouterProvider', '$locationProvider',
    function ($stateProvider, $urlRouterProvider, $locationProvider) {
      $locationProvider.hashPrefix('');

      $urlRouterProvider.otherwise('/home');

      $stateProvider
        .state('home', {
          url: '/home',
          params: {
            reasonSuccess: null,
            reasonError: null
          },
          templateUrl: 'scripts/pages/home/home.html',
          controller: 'HomeCtrl',
          controllerAs: '$ctrl',
          resolve: {
            isMobileCheck: ($rootScope, $cookies, $q, MobileDeviceWarning, deviceDetector) => {
              const hasAlreadyBeenWarned = $cookies.get('has-been-warned-about-mobile');
              var deferred = $q.defer();
              if (!hasAlreadyBeenWarned) {
                if (deviceDetector.isMobile() && !deviceDetector.isTablet()) {
                  $rootScope.showGlobalLoadingMask = false;
                  MobileDeviceWarning.show().result.then(() => {
                    $cookies.put('has-been-warned-about-mobile', true)
                    deferred.resolve();
                  });
                } else {
                  deferred.resolve();
                }

              } else {
                deferred.resolve();
              }

              return deferred.promise;
            }
          }
        })

        .state('registration', {
          url: '/registration',
          templateUrl: 'scripts/pages/registration/registration.html',
          controller: 'RegistrationCtrl',
          controllerAs: '$ctrl'
        })
        .state('registration-type', {
          url: '/registration-type',
          templateUrl: 'scripts/pages/registration/registrationType.html',
          controller: 'RegistrationTypeCtrl',
          controllerAs: '$ctrl'
        })

        .state('request-password-reset', {
          url: '/request-password-reset',
          params: {
            reasonError: null
          },
          templateUrl: 'scripts/pages/reset-password/requestPasswordReset.html',
          controller: 'RequestPasswordResetCtrl',
          controllerAs: '$ctrl'
        })

        .state('password-reset', {
          url: '/password-reset/:userId/:token',
          templateUrl: 'scripts/pages/reset-password/passwordReset.html',
          controller: 'PasswordResetCtrl',
          controllerAs: '$ctrl'
        })

        .state('users', {
          url: '/users',
          template: `<users-page organisation-types="$ctrl.organisationTypes"></users-page>`,
          controller(organisationTypes) {
            this.organisationTypes = organisationTypes;
          },
          controllerAs: '$ctrl',
          resolve: {
            organisationTypes(OrganisationService) {
              return OrganisationService.organisationTypes();
            }
          },
          params: {
            data: null
          }
        })

        .state('user', {
          url: '/user',
          // templateUrl: 'scripts/pages/user/home/userHome.html',
          template: `<user-home-page dashboard-metrics="$resolve.dashboardMetrics" user-dashboard-metrics-toggle="$resolve.userDashboardMetricsToggle" home-page-message="$resolve.homePageMessage"></user-home-page>`,
          // controller: 'UserHomeCtrl',
          // controllerAs: '$ctrl',
          resolve: {
            userDashboardMetricsToggle: ['FeatureToggleService', (FeatureToggleService) => {
              return FeatureToggleService.isFeatureEnabled('Dashboard').then(rsp => rsp.data);
            }],
            dashboardMetrics: (DashboardService, userDashboardMetricsToggle) =>{
              if(userDashboardMetricsToggle){
                return DashboardService.getMetrics().then(resp => {
                  return resp.data;
                })
              } else {
                return {};
              }
            },
            homePageMessage: (ConfigurationService) => {
              return ConfigurationService.homePageMessage().then(resp => {
                return resp.data;
              });
            }
          }
        })

        .state('organisations', {
          url: '/organisations?searchText',
          templateUrl: 'scripts/pages/organisations/organisations.html',
          controller: 'OrganisationsCtrl',
          controllerAs: '$ctrl',
          resolve: {
            organisationTypes(UserService, OrganisationService) {
              let currentUser = UserService.currentUser();
              return currentUser.approved ? OrganisationService.organisationTypes() : {};
            },
            watchedOrganisations(NotificationsService) {
              return NotificationsService.getWatched('organisation').then(rsp => {
                let data = {};
                _.forEach(rsp.data, (item) => {
                  data[item.entityId] = item;
                });
                return data;
              });
            },
            canFilterByTeams(UserService) {
              return UserService.hasPermission('org.filter.team');
            },
            managingOrganisationsTeams(OrganisationService, canFilterByTeams) {
              if (canFilterByTeams) {
                return OrganisationService.getManagingOrganisationsTeams().then((resp) => {
                  let result = _.groupBy(resp.data, 'organisationId')
                  result = _.map(result, items => {
                    return {
                      id: items[0].organisationId,
                      label: items[0].organisationName,
                      items: _.sortBy(items, (team) => {
                        if (team.teamId) {
                          team.label = team.teamName;
                          return team.teamName;
                        } else {
                          team.label = 'No team allocated';
                          return;
                        }
                      })
                    }
                  });
                  return _.sortBy(result, 'label');
                });
              } else {
                return [];
              }
            }
          },
          params: {
            searchText: null
          }
        })

        .state('teams', {
          url: '/teams',
          template: `<teams-page teams="$resolve.teams"></teams-page>`,
          resolve: {
            teams(TeamService) {
              return TeamService.getTeams().then(rsp => rsp.data);
            }
          }
        })


        .state('consortiums', {
          url: '/consortiums',
          params: {
            createdConsortiumId: null
          },
          templateUrl: 'scripts/pages/consortiums/consortiums.html',
          controller: 'ConsortiumsCtrl',
          controllerAs: '$ctrl'
        })

        .state('consortiums-new', {
          url: '/consortiums/new',
          templateUrl: 'scripts/pages/consortiums/new/newConsortium.html',
          controller: 'NewConsortiumCtrl',
          controllerAs: '$ctrl',
          resolve: {
            programmes(ProgrammeService) {
              return ProgrammeService.getEnabledProgrammes().then(resp => resp.data.content);
            }
          }
        })

        .state('consortiums-edit', {
          url: '/consortiums/:orgGroupId/edit',
          templateUrl: 'scripts/pages/consortiums/edit/editConsortium.html',
          controller: 'EditConsortiumCtrl',
          controllerAs: '$ctrl',
          resolve: {
            organisationGroup($stateParams, OrganisationGroupService) {
              return OrganisationGroupService.findById($stateParams.orgGroupId).then(resp => resp.data);
            },

            //TODO move sorting to service?
            programmes(ProgrammeService) {
              return ProgrammeService.getEnabledProgrammes().then(resp => resp.data.content);
            }
          }
        })

        .state('organisation.new', {
          url: '/organisation/new',
          template: `<new-organisation-page organisation-types="$ctrl.organisationTypes"></new-organisation-page>`,
          controller(organisationTypes) {
            this.organisationTypes = organisationTypes;
          },
          controllerAs: '$ctrl',
          resolve: {
            // organisationTypes: (OrganisationService) => {
            //   return OrganisationService.organisationTypes();
            // }
          }
        })

        .state('organisation.new-profile', {
          url: '/organisation/new-profile?managingOrgId',
          templateUrl: 'scripts/pages/organisation/organisationForm/organisationForm.html',
          controller: 'NewOrganisationProfileCtrl',
          controllerAs: '$ctrl',
          resolve: {
            // organisationTypes(OrganisationService) {
            //   return OrganisationService.organisationTypes();
            // },
            //
            // managingOrganisations(OrganisationService) {
            //   return OrganisationService.managingOrganisations().then(rsp => rsp.data);
            // }
          }
        })

        .state('organisation.new-with-user', {
          url: '/organisation/new-with-user',
          templateUrl: 'scripts/pages/organisation/organisationForm/organisationForm.html',
          controller: 'NewOrganisationWithUserCtrl',
          controllerAs: '$ctrl',
          resolve: {
            logout(UserService) {
              //Needs to be called to avoid seeing page as logged in user.
              return UserService.logout();
            },

            // organisationTypes(logout, OrganisationService) {
            //   return OrganisationService.organisationTypes();
            // },
            //
            // managingOrganisations(logout, OrganisationService) {
            //   return OrganisationService.managingOrganisations().then(rsp => rsp.data);
            // }
          }
        })

        .state('organisation.view', {
          url: '/organisation/:orgId',
          template: `<organisation-page organisation-types="$resolve.organisationTypes" 
                                      organisation="$resolve.organisation" 
                                      show-users="$resolve.showUsers"
                                      show-duplicate-org-as-link="$resolve.showDuplicateOrgAsLink"
                                      remaining-years="$resolve.remainingYears"></organisation-page>`,
          resolve: {
            // organisationTypes(OrganisationService) {
            //   return OrganisationService.organisationTypes();
            // },
            //
            organisation(OrganisationService, $stateParams) {
              return OrganisationService.getDetails($stateParams.orgId).then(rsp => rsp.data);
            },

            showUsers(UserService, OrganisationService, $stateParams) {
              return UserService.hasPermission('user.approve', $stateParams.orgId)
            },
            remainingYears (AnnualSubmissionService, $stateParams, UserService) {
              return UserService.hasPermission('annual.submission.create', $stateParams.orgId) ? AnnualSubmissionService.getRemainingYears($stateParams.orgId) : [];
            },

            showDuplicateOrgAsLink(UserService, organisation) {
              if (organisation.duplicateOrganisationId) {
                if(UserService.hasPermission('org.view.details', organisation.duplicateOrganisationId)){
                  return true;
                }
                // Check if managing org can access it
                return UserService.checkCurrentUserAccess('organisation', organisation.duplicateOrganisationId, {403: true})
                    .then(rsp => true)
                    .catch(err => false);

              }
              return false;
            }
          }
        })

        .state('organisation', {
          abstract: true,
          template: '<ui-view></ui-view>',
          params: {
            orgId: null
          },
          resolve: {
            organisationTypes: (OrganisationService) => {
              return OrganisationService.organisationTypes();
            },

            // organisation: (OrganisationService, $stateParams) => {
            //   if($stateParams.orgId){
            //     return OrganisationService.getDetails($stateParams.orgId).then(resp => resp.data);
            //   } else {
            //     return false;
            //   }
            // },

            managingOrganisations(OrganisationService) {
              return OrganisationService.managingOrganisations(true).then(rsp => rsp.data);
            }
          }
        })

        .state('organisation.edit', {
          url: '/organisation/:orgId/edit?section',
          template: `<edit-organisation-page organisation-types="$resolve.organisationTypes" 
                                           organisation="$resolve.organisation" 
                                           contacts="$resolve.contacts" 
                                           teams="$resolve.teams"></edit-organisation-page>`,
          params: {
            orgId: null,
            section: null
          },
          resolve: {
            // organisationTypes: (OrganisationService) => {
            //   return OrganisationService.organisationTypes();
            // },
            //
            organisation: (OrganisationService, $stateParams) => {
              return OrganisationService.getDetails($stateParams.orgId).then(resp => resp.data);
            },

            teams(organisation, UserService, OrganisationService) {
              let hasPermission = UserService.hasPermission('org.edit.team');
              let managingOrgId = organisation.managingOrganisationId;
              return hasPermission && managingOrgId ? OrganisationService.getOrganisationTeams(managingOrgId).then(rsp => rsp.data) : null;
            },

            contacts(organisation, UserService, OrganisationService) {
              let hasPermission = UserService.hasPermission('org.view.glacontact');
              let managingOrgId = organisation.managingOrganisationId;
              return hasPermission && managingOrgId ? OrganisationService.getOrganisationUsers(managingOrgId).then(rsp => rsp.data) : null;
            }
          }
        })

        .state('organisation-programme', {
          url: '/organisation/:organisationId/programme/:programmeId',
          template: `<organisation-programme-page is-strategic-units-summary="$resolve.isStrategicUnitsSummary"></organisation-programme-page>`,
          controllerAs: '$ctrl',
          params: {
            organisationId: null,
            programmeId: null,
            organisation: null,
            programme: null
          },
          resolve: {
            isStrategicUnitsSummary(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('StrategicUnitsSummary').then(rsp => rsp.data);
            }
          }
        })

        .state('grant-annual-submission', {
          url: '/organisation/:orgId/annual-submission/submission-id/:submissionId/block/:blockId/',
          templateUrl: 'scripts/pages/organisation/recoverable-grant-submission/recoverableGrantSubmission.html',
          controller: 'RecoverableGrantSubmissionCtrl',
          controllerAs: '$ctrl',
          resolve: {

            organisation: (OrganisationService, $stateParams) => {
              return OrganisationService.getDetails($stateParams.orgId).then(resp => resp.data);
            },

            annualSubmissionCategories: (AnnualSubmissionService, $stateParams) => {
              return AnnualSubmissionService.getAnnualSubmissionCategories().then(resp => resp.data);
            },

            block: (AnnualSubmissionService, $stateParams) => {
              return AnnualSubmissionService.getAnnualSubmissionBlock($stateParams.submissionId, $stateParams.blockId).then(resp => resp.data);
            }

          },
          params: {
            showCommentsForGenerated: false,
            showCommentsForSpent: false
          }
        })
        .state('grant-forecast-submission', {
          url: '/organisation/:orgId/forecast-submission/submission-id/:submissionId/block/:blockId/',
          templateUrl: 'scripts/pages/organisation/recoverable-grant-submission/recoverableGrantSubmissionForecast.html',
          controller: 'RecoverableGrantSubmissionForecastCtrl',
          controllerAs: '$ctrl',
          resolve: {

            organisation: (OrganisationService, $stateParams) => {
              return OrganisationService.getDetails($stateParams.orgId).then(resp => resp.data);
            },

            annualSubmissionCategories: (AnnualSubmissionService, $stateParams) => {
              return AnnualSubmissionService.getAnnualSubmissionCategories().then(resp => resp.data);
            },

            block: (AnnualSubmissionService, $stateParams) => {
              return AnnualSubmissionService.getAnnualSubmissionBlock($stateParams.submissionId, $stateParams.blockId).then(resp => resp.data);

            }
          },
          params: {
            showCommentsForGenerated: false,
            showCommentsForSpent: false
          }
        })

        .state('projects', {
          url: '/projects?title&organisationName&programmeName',
          template: '<projects-page project-states="$resolve.projectStates" programmes="$resolve.programmes" all-programmes="$resolve.allProgrammes"></projects-page>',
          resolve: {
            programmes: ($stateParams, ProgrammeService, UserService, $rootScope) => {
              return ProgrammeService.getEnabledProgrammes().then(resp => resp.data.content);
            },
            //TODO merge with the enabled ones
            allProgrammes(ProgrammeService) {
              return ProgrammeService.getProgrammes().then(resp => {
                return resp.data.content;
              });
            },

            projectStates(ProjectService) {
              return ProjectService.getProjectStates().then(resp => resp.data);
            }
          },
          params: {
            title: null,
            organisationName: null,
            programmeId: null,
            programmeName: null
          }
        })

        .state('projects-new', {
          url: '/projects/new',
          template: '<new-project-page programmes="$ctrl.programmes"></new-project-page>',
          params: {
            programmes: null
          },
          controller(programmes) {
            this.programmes = programmes;
          },
          controllerAs: '$ctrl',
          resolve: {
            programmes($state, ProgrammeService) {
              if ($state.params && $state.params.programmes) {
                return $state.params.programmes;
              } else {
                return ProgrammeService.getEnabledProgrammes().then(resp => {
                  return resp.data.content;
                });
              }
            }
          }
        })

        .state('project', {
          url: '/project/:projectId',
          abstract: true,
          template: '<ui-view></ui-view>',
          params: {
            blockId: null
          },
          resolve: {
            project: ($stateParams, ProjectService, $rootScope) => {
              $rootScope.showGlobalLoadingMask = true;
              return ProjectService.getProject($stateParams.projectId)
                .then(resp => {
                  $rootScope.showGlobalLoadingMask = false;
                  return resp.data;
                });
            },
            template: (TemplateService, project, $rootScope) => {
              $rootScope.showGlobalLoadingMask = true;
              return TemplateService.getTemplate(project.templateId)
                .then(resp => {
                  $rootScope.showGlobalLoadingMask = false;
                  return resp.data;
                });
            }
          }
        })

        .state('project-overview', {
          url: '/project/:projectId',
          template: `<project-overview is-submit-to-approve-enabled="$resolve.isSubmitToApproveEnabled" 
                                       is-marked-for-corporate-enabled="$resolve.isMarkedForCorporateEnabled" 
                                       is-labels-feature-enabled="$resolve.isLabelsFeatureEnabled" 
                                       label-message="$resolve.labelMessage" 
                                       project="$resolve.projectOverview" 
                                       full-project="$resolve.project" 
                                       template="$resolve.template"
                                       pre-set-labels="$resolve.preSetLabels"></project-overview>`,
          resolve: {
            projectOverview($stateParams, ProjectService) {
              return ProjectService.getProjectOverview($stateParams.projectId).then(resp => resp.data);
            },

            project($stateParams, ProjectService) {
              return ProjectService.getProject($stateParams.projectId).then(resp => resp.data);
            },

            template(TemplateService, project, $rootScope) {
              return TemplateService.getTemplate(project.templateId).then(resp => resp.data);
            },

            isSubmitToApproveEnabled(FeatureToggleService) {
            return FeatureToggleService.isFeatureEnabled('SubmitAutoApprovalProject').then(rsp => rsp.data);
            },

            isMarkedForCorporateEnabled(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('MarkProjectCorporate').then(rsp => rsp.data);
            },

            isLabelsFeatureEnabled(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('labels').then(rsp => rsp.data);
            },

            labelMessage(ConfigurationService) {
              return ConfigurationService.getMessage('project-label').then(rsp => rsp.data);
            },
            preSetLabels(LabelService, projectOverview) {
              return LabelService.getPreSetLabels(projectOverview.organisation.managingOrganisationId, projectOverview.markedForCorporate).then(rsp => rsp.data);
            }
        }
        })

        .state('programmes', {
          url: '/programmes',
          template: '<programmes-page></programmes-page>',
        })


        .state('programmes-new', {
          url: '/programmes/new',
          template: `<programme-page templates-list="$resolve.templatesList" 
                                     assessment-templates="$resolve.assessmentTemplates" 
                                     gla-roles="$resolve.glaRoles"
                                     new-programme-mode="true"></programme-page>`,

          controllerAs: '$ctrl',
          resolve: {
            templatesList(TemplateService) {
              return TemplateService.getAllProjectTemplates().then(rsp => rsp.data);
            },
            assessmentTemplates(AssessmentService) {
              return AssessmentService.getAssessmentTemplates().then(rsp => _.sortBy(rsp.data, 'name'));
            },
            glaRoles(OrganisationService) {
              return OrganisationService.getGlaRoles().then(rsp => rsp.data);
            }
          }
        })


        .state('programme', {
          url: '/programme/:programmeId',
          template: `<programme-page programme="$resolve.programme" 
                                     templates-list="$resolve.templatesList" 
                                     assessment-templates="$resolve.assessmentTemplates" 
                                     gla-roles="$resolve.glaRoles"
                                     allow-change-in-use-assessment-template="$resolve.allowChangeInUseAssessmentTemplate"
                                     new-programme-mode="false"></programme-page>`,
          controllerAs: '$ctrl',
          resolve: {
            programme($stateParams, ProgrammeService){
              return ProgrammeService.getProgramme($stateParams.programmeId, true).then(rsp => {
                return rsp.data;
              });
            },
            templatesList(TemplateService) {
              return TemplateService.getAllProjectTemplates().then(rsp => _.sortBy(rsp.data, 'name'));
            },
            glaRoles(OrganisationService) {
              return OrganisationService.getGlaRoles().then(rsp => rsp.data);
            },
            allowChangeInUseAssessmentTemplate(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('AllowChangeInUseAssessmentTemplate').then(rsp => rsp.data);
            }
          }
        })

        .state('assessment-templates', {
          url: '/assessment-templates',
          template: '<assessment-templates assessment-templates="$resolve.assessmentTemplates"></assessment-templates>',
          resolve: {
            assessmentTemplates(AssessmentService) {
              return AssessmentService.getAssessmentTemplates().then(rsp => rsp.data);
            }
          }
        })

        .state('assessment-template-new', {
          url: '/assessment-templates/new',
          template: `<new-assessment-template></new-assessment-template>`
        })

        .state('assessment-template-paste', {
          url: '/assessment-templates/paste',
          template: `<paste-assessment-template></paste-assessment-template>`
        })

        .state('assessment-template', {
          url: '/assessment-templates/:id',
          template: '<assessment-template assessment-template="$resolve.assessmentTemplate" assessment-template-json="$resolve.assessmentTemplateJson" allow-change-in-use-assessment-template="$resolve.allowChangeInUseAssessmentTemplate"></assessment-template>',
          resolve: {
            assessmentTemplate($stateParams, AssessmentService) {
              return AssessmentService.getAssessmentTemplate($stateParams.id).then(rsp => rsp.data);
            },
            assessmentTemplateJson($stateParams, PortableEntityService) {
              return PortableEntityService.getSanitisedEntity('AssessmentTemplate',$stateParams.id).then(rsp => JSON.stringify(rsp.data))
            },
            allowChangeInUseAssessmentTemplate(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('AllowChangeInUseAssessmentTemplate').then(rsp => rsp.data);
            }
          }
        })

        .state('assessment-template-edit', {
          url: '/assessment-templates/:id/edit',
          template: '<edit-assessment-template assessment-template="$resolve.assessmentTemplate"></edit-assessment-template>',
          resolve: {
            assessmentTemplate($stateParams, AssessmentService) {
              return AssessmentService.getAssessmentTemplate($stateParams.id).then(rsp => rsp.data);
            }
          }
        })

        .state('assessment', {
          url: '/assessments/:id',
          template: '<assessment assessment="$resolve.assessment" assessment-template="$resolve.assessmentTemplate" editable="$resolve.editable"></assessment>',
          resolve: {
            assessment($stateParams, AssessmentService) {
              return AssessmentService.getAssessment($stateParams.id).then(rsp => rsp.data);
            },
            assessmentTemplate: (AssessmentService, assessment, $rootScope) => {
              $rootScope.showGlobalLoadingMask = true;
              return AssessmentService.getAssessmentTemplate(assessment.assessmentTemplateId)
                .then(resp => {
                  $rootScope.showGlobalLoadingMask = false;
                  return resp.data;
                });
            },

            editable (AssessmentService, UserService, assessment) {
              if(!UserService.hasPermission('assessment.manage')){
                return false;
              }
              return AssessmentService.assessmentTemplatesForUser({programmeId: assessment.programmeId, templateId: assessment.templateId}).then(rsp => {
                return _.some(rsp.data, {id: assessment.assessmentTemplateId});
              });
            }
          },
          params: {
            backNavigatesTo: null
          }
        })

        .state('assessments', {
          url: '/assessmentList',
           template: `<assessment-list assessments="$resolve.assessments"></assessment-list>`,
            resolve: {
              assessments(AssessmentService) {
                return AssessmentService.getAssessments().then(rsp => rsp.data);
              }
            }
        })

        .state('assessment-edit', {
          url: '/assessment/:id/edit',
          template: '<edit-assessment assessment="$resolve.assessment" assessment-template="$resolve.assessmentTemplate"></edit-assessment>',
          resolve: {
            assessment($stateParams, AssessmentService) {
              return AssessmentService.getAssessment($stateParams.id).then(rsp => rsp.data);
            },
            assessmentTemplate: (AssessmentService, assessment, $rootScope) => {
              $rootScope.showGlobalLoadingMask = true;
              return AssessmentService.getAssessmentTemplate(assessment.assessmentTemplateId)
                .then(resp => {
                  $rootScope.showGlobalLoadingMask = false;
                  return resp.data;
                });
            }
          }
        })

        .state('notifications', {
          url: '/notifications',
          template: '<notifications-page></notifications-page>',
          controller() {

          },
          controllerAs: '$ctrl',
          resolve: {
            config(ConfigurationService) {
              return ConfigurationService.getConfig().then(function (resp) {
                return resp.data;
              });
            }
          }
        })
        .state('all-payments', {
          url: '/all-payments',
          templateUrl: 'scripts/pages/payments/all-payments/allPayments.html',
          controller: 'AllPaymentsCtrl',
          controllerAs: '$ctrl',
          resolve: {
            programmes(ProgrammeService) {
              return ProgrammeService.getProgrammes().then(resp => {
                return resp.data.content;
              });
            }
            // paymentGroups(PaymentService) {
            //   return PaymentService.getPaymentGroups('ALL');
            // }
          }
        })

        .state('pending-payments', {
          url: '/pending-payments',
          template: `<pending-payments payment-groups="$ctrl.paymentGroups" 
                                       payment-decline-reason="$ctrl.paymentDeclineReason" 
                                       reclaim-decline-reason="$ctrl.reclaimDeclineReason"></pending-payments>`,
          controller(paymentGroups, paymentDeclineReason, reclaimDeclineReason) {
            this.paymentGroups = paymentGroups;
            this.paymentDeclineReason = paymentDeclineReason;
            this.reclaimDeclineReason = reclaimDeclineReason;
          },
          controllerAs: '$ctrl',
          resolve: {
            paymentGroups(PaymentService) {
              return PaymentService.getPaymentGroups('PENDING');
            },
            paymentDeclineReason(PaymentService) {
              return PaymentService.getPaymentDeclineReason();
            },
            reclaimDeclineReason(PaymentService) {
              return PaymentService.getReclaimDeclineReason();
            }
          },
          params: {
            paymentGroupId: null,
            paymentId: null
          }
        })

        .state('payment-summary', {
          url: '/payment-summary/:paymentGroup/:paymentId',
          template: `<payment-summary payment-group="$ctrl.paymentGroup" 
                                      payment="$ctrl.payment" 
                                      milestone="$ctrl.milestone" 
                                      project="$ctrl.project" 
                                      is-reclaim-enabled="$ctrl.isReclaimEnabled"
                                      reclaims="$ctrl.reclaims"
                                      original-payment="$ctrl.originalPayment"></payment-summary>`,
          controller(paymentGroup, payment, project, reclaims, isReclaimEnabled, originalPayment, milestone) {
            this.paymentGroup = paymentGroup;
            this.payment = payment;
            this.milestone = milestone;
            this.project = project;
            this.reclaims = reclaims;
            this.originalPayment = originalPayment;
            this.isReclaimEnabled = isReclaimEnabled;
          },
          controllerAs: '$ctrl',
          resolve: {
            isReclaimEnabled(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('reclaims').then(rsp => rsp.data);
            },

            paymentGroup($stateParams, PaymentService) {
              return PaymentService.getPaymentGroupByPaymentId($stateParams.paymentId);
            },

            payment($stateParams, paymentGroup) {
              return _.find(paymentGroup.payments, {id: +$stateParams.paymentId});
            },

            project(payment, ProjectService) {
              return ProjectService.getProject(payment.projectId).then(resp => resp.data);
            },

            originalPayment(payment, PaymentService) {
              let originalPaymentId = payment.reclaimOfPaymentId;
              if (originalPaymentId) {
                return PaymentService.getPaymentGroupByPaymentId(originalPaymentId).then(paymentGroup => {
                  return _.find(paymentGroup.payments, {id: originalPaymentId});
                });
              }
            },

            reclaims(payment, PaymentService) {
              return PaymentService.getReclaims(payment.id, payment.projectId);
            },

            milestone(payment, PaymentService) {
              return PaymentService.getPaymentMilestone(payment);
            }
          }
        })

        .state('reports', {
          url: '/reports',
          template: `<reports-page programmes="$resolve.programmes" 
                                   env-vars="$resolve.envVars" 
                                   reports="$resolve.reports" 
                                   programme-report-enabled="$resolve.programmeReportEnabled" 
                                   affordable-housing-report-enabled="$resolve.affordableHousingReportEnabled"
                                   borough-report-enabled="$resolve.boroughReportEnabled"></reports-page>`,
          resolve: {
            envVars(ConfigurationService) {
              return ConfigurationService.getConfig().then(rsp => rsp.data);
            },
            programmes(ProgrammeService) {
              return ProgrammeService.getProgrammes({statuses: ['Active', 'Archived']}).then(rsp => rsp.data.content);
            },
            reports(ReportService) {
              return ReportService.getReports().then(rsp => rsp.data);
            },
            programmeReportEnabled(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('outputCSV').then(rsp => rsp.data);
            },
            affordableHousingReportEnabled(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('AffordableHousingReport').then(rsp => rsp.data);
            },
            boroughReportEnabled(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('BoroughReport').then(rsp => rsp.data);
            }
          }
        })

        .state('change-report', {
          url: '/change-report/:projectId',
          // templateUrl: 'scripts/pages/change-report/changeReport.html',
          template: `<change-report-page
                        latest-project="$resolve.latestProject"
                        last-approved-project="$resolve.lastApprovedProject"
                        template="$resolve.template"
                        current-financial-year="$resolve.currentFinancialYear"
                        is-date-picker-enabled="$resolve.isDatePickerEnabled"
                        internal-risk-comments="$resolve.internalRiskComments"
                        project-history="$resolve.projectHistory"></change-report-page>`,
          resolve: {
            latestProject: (ProjectService, $stateParams) => {
              let params = {
                compareToStatus: 'LAST_APPROVED',
                forComparison: true,
                comparisonDate: $stateParams.comparisonDate
              };
              if ($stateParams.comparisonDate) {
                delete params.compareToStatus;
              }

              return ProjectService.getProject($stateParams.projectId, params).then(resp => resp.data);
            },

            lastApprovedProject: (ProjectService, $stateParams) => {
              console.log('stateParams', $stateParams.comparisonDate);

              return ProjectService.getProject($stateParams.projectId, {
                unapprovedChanges: false,
                forComparison: true,
                comparisonDate: $stateParams.comparisonDate
              }).then(resp => resp.data);
            },
            template: (TemplateService, latestProject) => {
              return TemplateService.getTemplate(latestProject.templateId).then(resp => resp.data);
            },
            projectHistory: (ProjectService, $stateParams) => {
              return ProjectService.getProjectHistory($stateParams.projectId);
            },

            currentFinancialYear: (ProjectService, $stateParams) => {
              return ProjectService.getCurrentFinancialYear().then(rsp => rsp.data)
            },

            internalRiskComments: (latestProject, CommentsService, RisksService) => {
              let internalRiskBlock = RisksService.getInternalRiskFromProject(latestProject) || {};
              if (internalRiskBlock.id) {
                return CommentsService.getInternalRiskComments(internalRiskBlock.id).then(r => r.data.content)
              }
              return null;
            },

            isDatePickerEnabled(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('CMSDatePicker').then(rsp => rsp.data);
            },
          },
          params: {
            comparisonDate: null
          }
        })

        .state('summary-report', {
          url: '/summary-report/:projectId',
          template: `<summary-report-page project="$resolve.project" 
                                          current-financial-year="$resolve.currentFinancialYear"                      
                                          template="$resolve.template"
                                          project-history="$resolve.projectHistory"></summary-report-page>`,
          resolve: {
            project($stateParams, ProjectService) {
              let params = {
                unapprovedChanges: _.isUndefined($stateParams.showUnapproved) ? true : $stateParams.showUnapproved,
                forComparison: true
              };
              return ProjectService.getProject($stateParams.projectId, params).then(rsp => rsp.data);
            },

            template(TemplateService, project) {
              return TemplateService.getTemplate(project.templateId).then(rsp => rsp.data);
            },

            currentFinancialYear(ProjectService){
              return ProjectService.getCurrentFinancialYear().then(rsp => rsp.data)
            },
            projectHistory: (ProjectService, $stateParams) => {
              return ProjectService.getProjectHistory($stateParams.projectId);
            },
          },
          params: {
            showUnapproved: true
          }
        })

        // TODO: possibly deprecated?
        .state('admin', {
          abstract: true,
          url: '/admin',
          template: '<ui-view></ui-view>'
        })

        .state('system', {
          url: '/system',
          template: `<system-page sys-info="$ctrl.sysInfo" 
                        sys-metrics="$ctrl.sysMetrics" 
                        features="$resolve.features" 
                        is-sql-editor-enabled="$ctrl.isSqlEditorEnabled"></system-page>`,
          controller(systemInfo, systemMetrics, isSqlEditorEnabled) {
            this.sysInfo = systemInfo;
            this.sysMetrics = systemMetrics;
            this.isSqlEditorEnabled = isSqlEditorEnabled;
          },
          controllerAs: '$ctrl',
          resolve: {
            systemInfo: (ActuatorService) => {
              return ActuatorService.getInfo().then(rsp => rsp.data);
            },
            systemMetrics: (ActuatorService) => {
              return ActuatorService.getMetrics().then(rsp => rsp.data);
            },
            isSqlEditorEnabled: (FeatureToggleService) => {
              return FeatureToggleService.isFeatureEnabled('SqlEditor').then(rsp => rsp.data);
            },
            features(FeatureToggleService) {
              return FeatureToggleService.getFeatures().then(rsp => rsp.data);
            }
          }
        })
        .state('system-messages', {
          url: '/system/messages',
          template: `<messages-page messages="$resolve.messages"></messages-page>`,

          resolve: {
            messages(ConfigurationService){
              return ConfigurationService.getMessages().then((resp)=>resp.data);
            }
          }
        })

        .state('system-sapData', {
          url: '/system/sapdata',
          template: `<sap-data-page sap-data="$ctrl.sapData"></sap-data-page>`,
          controller(sapData) {
            this.sapData = sapData;
          },
          controllerAs: '$ctrl',
          resolve: {
            sapData: (SapDataService) => {
              return SapDataService.getSapData(false).then(rsp => {
                return rsp.data;
              })
            }
          }
        })

        .state('system-templates-questions', {
          url: '/system/templates/questions',
          template: `<templates-questions-page></templates-questions-page>`
        })

        .state('system-question', {
          url: '/system/questions/:questionId',
          template: `<question-page question="$resolve.question"></question-page>`,
          controller() {
          },
          resolve: {
            question(QuestionsService, $stateParams) {
              return QuestionsService.getQuestion($stateParams.questionId).then(resp => {
                return resp.data;
              });
            }
          }
        })

        .state('system-question-new', {
          url: '/system/question/new',
          template: `<question-form></question-form>`
        })

        .state('system-question-edit', {
          url: '/system/question/:questionId/edit',
          template: `<question-form question="$resolve.question"></question-form>`,
          resolve: {
            question(QuestionsService, $stateParams) {
              return QuestionsService.getQuestion($stateParams.questionId).then(resp => {
                return resp.data;
              });
            }
          }
        })

        .state('system-templates', {
          url: '/system/templates',
          template: `<templates-page templates="$resolve.templates"></templates-page>`,
          resolve: {
            templates(TemplateService) {
              return TemplateService.getAllProjectTemplateSummaries(0, '', '').then(resp => {
                return resp.data;
              });
            }
          }
        })

        .state('system-template-details', {
          url: '/system/template/:templateId',
          template: `<template-details-page template="$resolve.template" 
                                            block-types="$resolve.blockTypes"                
                                            original-template="$resolve.originalTemplate"></template-details-page>`,
          resolve: {
            template(TemplateService, $stateParams) {
              return TemplateService.getTemplate($stateParams.templateId, true).then(resp => {
                return resp.data;
              });
            },
            originalTemplate(TemplateService, $stateParams) {
              return TemplateService.getTemplate($stateParams.templateId, false).then(resp => {
                return resp.data;
              });
            },

            blockTypes(ReferenceDataService) {
              return ReferenceDataService.getBlockTypes();
            }
          }
        })

        .state('system-template-details-create', {
          url: '/system/template-create',

          template: `<template-details-page block-types="$resolve.blockTypes"></template-details-page>`,
          resolve: {
            blockTypes(ReferenceDataService) {
              return ReferenceDataService.getBlockTypes();
            }
          }
        })

        .state('system-features', {
          url: '/system/features',
          template: `<features-page features="$resolve.features"></features-page>`,
          resolve: {
            features(FeatureToggleService) {
              return FeatureToggleService.getFeatures().then(rsp => rsp.data);
            }
          }
        })

        .state('skill-profiles', {
          url: '/system/skill-profiles',
          template: `<skill-profiles></skill-profiles>`
        })

        .state('data-validation-details', {
          url: '/dataValidationDetails',
          template: `<validation-details-page sys-info="$resolve.systemInfo"></validation-details-page>`,
          resolve: {
            systemInfo(ActuatorService) {
              return ActuatorService.getInfo().then(rsp => rsp.data);
            }
          }
        })

        .state('sql', {
          url: '/system/sqlManager',
          template: `<sql-manager sql-updates="$ctrl.sqlUpdates"></sql-manager>`,
          controller(sqlUpdates) {
            this.sqlUpdates = sqlUpdates;
          },
          controllerAs: '$ctrl',
          resolve: {
            sqlUpdates: (DatabaseUpdateService) => {
              return DatabaseUpdateService.getSqlQueries();
            }
          }
        })

        .state('sql-details', {
          url: '/system/sqlManager/sqlDetails/:sqlId',
          template: `<sql-details sql-update-details="$ctrl.sqlUpdateDetails"></sql-details>`,
          controller(sqlUpdateDetails) {
            this.sqlUpdateDetails = sqlUpdateDetails;
          },
          controllerAs: '$ctrl',
          resolve: {
            sqlUpdateDetails: (DatabaseUpdateService, $stateParams) => {
              return DatabaseUpdateService.getUpdateDetails($stateParams.sqlId).then(rsp => rsp.data);
            }
          }
        })

        .state('sql-create', {
          url: '/system/sqlManager/create',
          template: `<create-new-sql></create-new-sql>`
        })

        .state('audit-activity', {
          url: '/auditActivity',
          template: `<audit-activity></audit-activity>`
        })

        .state('finance-categories', {
          url: '/finance-categories',
          template: `<finance-categories-page finance-categories="$ctrl.financeCategories"></finance-categories-page>`,
          controller(financeCategories) {
            this.financeCategories = financeCategories;
          },
          controllerAs: '$ctrl',
          resolve: {
            financeCategories: (FinanceService) => {
              return FinanceService.getFinanceCategories(true).then(resp => resp.data);
            }
          }
        })

        .state('user-account', {
          url: '/user-account/:userId',
          templateUrl: 'scripts/pages/user-account/user-account.html',
          controller: 'UserAccountCtrl',
          controllerAs: '$ctrl',
          params: {
            editMode: false
          },
          resolve: {
            userProfile: ($stateParams, UserService) => {
              if ($stateParams.userId) {
                return UserService.getUserProfile($stateParams.userId).then(rsp => rsp.data);
              } else {
                console.error('Need to specify userId');
                return false;
              }
            },

            userThresholds: ($stateParams, UserService) => {
              if (UserService.hasPermission('user.org.view.threshold')) {
                return UserService.getUserThresholds($stateParams.userId).then(rsp => rsp.data);
              } else {
                return [];
              }
            }
          }
        })
        .state('new-annual-submission', {
          url: '/new-annual-submission/:orgId',
          template: '<new-annual-submission organisation="$ctrl.organisation" years="$ctrl.years" remaining-years="$ctrl.remainingYears"></new-annual-submission>',
          controller(organisation, years, remainingYears) {
            this.organisation = organisation;
            this.years = years;
            this.remainingYears = remainingYears;
          },
          controllerAs: '$ctrl',
          resolve: {
            organisation: (OrganisationService, $stateParams) => {
              return OrganisationService.getDetails($stateParams.orgId).then(resp => resp.data);
            },
            years: (organisation) => {
              return _.map(organisation.annualSubmissions, 'financialYear');
            },
            remainingYears: (AnnualSubmissionService, $stateParams, UserService) => {
              return UserService.hasPermission('annual.submission.create', $stateParams.orgId) ? AnnualSubmissionService.getRemainingYears($stateParams.orgId) : [];
            }
          }
        })
        .state('annual-submission', {
          url: '/annual-submission/:annualSubmissionId',
          template: '<annual-submission organisation="$ctrl.organisation" annual-submission="$ctrl.annualSubmission"></annual-submission>',
          controller(annualSubmission, organisation) {
            this.organisation = organisation;
            this.annualSubmission = annualSubmission;
          },
          controllerAs: '$ctrl',
          resolve: {
            organisation: (OrganisationService, annualSubmission) => {
              return OrganisationService.getDetails(annualSubmission.organisationId).then(resp => resp.data);
            },
            annualSubmission: ($stateParams, AnnualSubmissionService) => {
              // return _.find(organisation.annualSubmissions, {id:_.toNumber($stateParams.annualSubmissionId)});
              return AnnualSubmissionService.getAnnualSubmission($stateParams.annualSubmissionId).then(resp => resp.data);
            }
          },
          params: {
            orgId: null,
            annualSubmissionId: null
          },
        })

        .state('components', {
          url: '/components',
          template: '<components-showcase></components-showcase>',
          controller() {
          },
          controllerAs: '$ctrl'
        })

        .state('confirm-user-created', {
          url: '/confirmation/user-created',
          templateUrl: 'scripts/pages/confirmation/user-created.html',
          controllerAs: '$ctrl'
        })

        .state('confirm-org-and-user-created', {
          url: '/confirmation/org-and-user-created',
          templateUrl: 'scripts/pages/confirmation/org-and-user-created.html',
          controllerAs: '$ctrl'
        })
        .state('permissions', {
          url: '/permissions',
          template: `<permissions-page permissions="$resolve.permissions"></permissions-page>`,
          resolve: {
            permissions(PermissionService) {
              return PermissionService.getPermissions().then(rsp => rsp.data);
            }
          }
        })
        .state('preSetLabels', {
          url: '/preSetLabels',
          template: `<labels-page labels="$resolve.labels" managing-organisations="$resolve.managingOrganisations"></labels-page>`,
          resolve: {
            labels(LabelService) {
              return LabelService.getPreSetLabels().then(rsp => rsp.data);
            },

            managingOrganisations(OrganisationService) {
              return OrganisationService.managingOrganisations().then(rsp => rsp.data);
            }
          }
        })
        .state('allNotifications', {
          url: '/allNotifications',
          template: `<all-notifications-page all-notifications="$resolve.allNotifications"></all-notifications-page>`,
          resolve: {
            allNotifications(NotificationsService) {
              return NotificationsService.getAllNotifications().then(rsp => rsp.data);
            }
          }
        })
        .state('overrides', {
          url: '/overrides',
          template: `<overrides-page overrides="$resolve.overrides" metadata="$resolve.metadata"></overrides-page>`,
          resolve: {
            overrides(OverridesService) {
              return OverridesService.getAllOverrides().then(rsp => rsp.data);
            },
            metadata(OverridesService) {
              return OverridesService.getMetadata().then(rsp => rsp.data);
            }
          }
        })
        .state('outputs-configuration', {
          url: '/outputsConfiguration',
          template: `<outputs-configuration-page output-configurations="$resolve.outputConfigurations"></outputs-configuration-page>`,
          resolve: {
            outputConfigurations(OutputConfigurationService) {
              return OutputConfigurationService.getAllOutputConfiguration().then(rsp => rsp.data);
            }
          }
        })
    }
  ]);
