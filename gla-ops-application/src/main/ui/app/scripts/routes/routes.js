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
          pageTitle: 'Home',
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
          controllerAs: '$ctrl',
          pageTitle: 'Account Registration'
        })
        .state('registration-type', {
          url: '/registration-type',
          templateUrl: 'scripts/pages/registration/registrationType.html',
          controller: 'RegistrationTypeCtrl',
          controllerAs: '$ctrl',
          pageTitle: 'Registration Options',
          resolve: {
            allowNewRegistrationProcess: ['FeatureToggleService', 'Downgrade', (FeatureToggleService, Downgrade) => {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('AllowNewRegistrationProcess'));
            }],
          },
        })

        .state('request-password-reset', {
          url: '/request-password-reset',
          params: {
            reasonError: null
          },
          templateUrl: 'scripts/pages/reset-password/requestPasswordReset.html',
          controller: 'RequestPasswordResetCtrl',
          controllerAs: '$ctrl',
          pageTitle: 'Password Reset Request'
        })

        .state('password-reset', {
          url: '/password-reset/:userId/:token',
          templateUrl: 'scripts/pages/reset-password/passwordReset.html',
          controller: 'PasswordResetCtrl',
          controllerAs: '$ctrl',
          pageTitle: 'Password Reset'
        })

        .state('users', {
          url: '/users',
          template: `<users-page organisation-types="$resolve.organisationTypes"></users-page>`,
          controllerAs: '$ctrl',
          pageTitle: 'Users',
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
          template: `<user-home-page dashboard-metrics="$resolve.dashboardMetrics" user-dashboard-metrics-toggle="$resolve.userDashboardMetricsToggle" home-page-message="$resolve.homePageMessage"></user-home-page>`,
          pageTitle: 'User Home',
          resolve: {
            userDashboardMetricsToggle: ['FeatureToggleService', 'Downgrade', (FeatureToggleService, Downgrade) => {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('Dashboard'));
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
          pageTitle: 'Organisations',
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
          template: `<teams-page></teams-page>`,
          pageTitle: 'Teams'
        })

        .state('consortiums', {
          url: '/consortiums',
          params: {
            createdConsortiumId: null
          },
          templateUrl: 'scripts/pages/consortiums/consortiums.html',
          controller: 'ConsortiumsCtrl',
          controllerAs: '$ctrl',
          pageTitle: 'Consortiums'
        })

        .state('consortiums-new', {
          url: '/consortiums/new',
          templateUrl: 'scripts/pages/consortiums/new/newConsortium.html',
          controller: 'NewConsortiumCtrl',
          controllerAs: '$ctrl',
          pageTitle: 'Create Consortium',
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
          pageTitle: 'Edit Consortium',
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
          template: `<new-organisation-page organisation-types="$resolve.organisationTypes"></new-organisation-page>`,
          controllerAs: '$ctrl',
          pageTitle: 'Create Organisation'
        })

        .state('organisation.new-profile', {
          url: '/organisation/new-profile?managingOrgId',
          templateUrl: 'scripts/pages/organisation/organisationForm/organisationForm.html',
          controller: 'NewOrganisationProfileCtrl',
          controllerAs: '$ctrl',
          pageTitle: 'Create Organisation Profile'
        })

        .state('organisation.new-with-user', {
          url: '/organisation/new-with-user',
          templateUrl: 'scripts/pages/organisation/organisationForm/organisationForm.html',
          controller: 'NewOrganisationWithUserCtrl',
          controllerAs: '$ctrl',
          pageTitle: 'Register Organisation & User',
          resolve: {
            logout(UserService) {
              //Needs to be called to avoid seeing page as logged in user.
              return UserService.logout();
            }
          }
        })

        .state('organisation.registration-programme', {
          url: '/organisation/new/programme',
          template: `<organisation-registration-programme-page
                        programmes="$resolve.programmes"
                        managing-organisations="$resolve.managingOrganisations"></organisation-registration-programme-page>`,
          pageTitle: 'Organisation Programme',
          resolve: {
            logout(UserService, SessionService) {
              let user = UserService.currentUser();
              if(user && user.loggedOn) {
                let registration = SessionService.getOrgRegistration();
                return UserService.logout().then(() => SessionService.setOrgRegistration(registration));
              }
            },

            programmes(ProgrammeService){
              return ProgrammeService.getPublicProgrammes().then(rsp => rsp.data);
            }
          }
        })

        .state('organisation.registration-form', {
          url: '/organisation/new/org-details',
          template: `<organisation-registration-form-page
                        organisation-types="$resolve.organisationTypes"
                        legal-statuses="$resolve.legalStatuses"
                        is-legal-status-enabled="$resolve.isLegalStatusEnabled"
                        managing-organisations="$resolve.managingOrganisations"></organisation-registration-form-page>`,
          pageTitle: 'Organisation Details',
          resolve: {
            logout(UserService, SessionService) {
              let user = UserService.currentUser();
              if(user && user.loggedOn) {
                let registration = SessionService.getOrgRegistration();
                return UserService.logout().then(() => SessionService.setOrgRegistration(registration));
              }
            },
          }
        })

        .state('organisation.registration-user', {
          url: '/organisation/new/user',
          template: `<organisation-registration-user-page></organisation-registration-user-page>`,
          pageTitle: 'Organisation User',
          resolve: {
            logout(UserService, SessionService) {
              let user = UserService.currentUser();
              if(user && user.loggedOn) {
                let registration = SessionService.getOrgRegistration();
                return UserService.logout().then(() => SessionService.setOrgRegistration(registration));
              }
            },
          }
        })

        .state('organisation.view', {
          url: '/organisation/:orgId',
          template: `<organisation-page organisation-types="$resolve.organisationTypes"
                                      organisation="$resolve.organisation"
                                      show-users="$resolve.showUsers"
                                      show-duplicate-org-as-link="$resolve.showDuplicateOrgAsLink"
                                      remaining-years="$resolve.remainingYears"
                                      legal-statuses="$resolve.legalStatuses"
                                      is-legal-status-enabled="$resolve.isLegalStatusEnabled"></organisation-page>`,
          pageTitle: 'Organisation',
          resolve: {
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
            orgId: null,
            backNavigation: null
          },
          resolve: {
            organisationTypes: (OrganisationService) => {
              return OrganisationService.organisationTypes();
            },
            legalStatuses: (OrganisationService) => {
              return OrganisationService.legalStatuses();
            },
            isLegalStatusEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('AllowLegalStatusOnRegistration'));
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
            },

            icons(ReferenceDataService, Downgrade) {
              return Downgrade.toPromise(ReferenceDataService.getIcons());
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
          pageTitle: 'Edit Organisation',
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
          pageTitle: 'Organisation Programme',
          params: {
            organisationId: null,
            programmeId: null,
            organisation: null,
            programme: null
          },
          resolve: {
            isStrategicUnitsSummary(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('StrategicUnitsSummary'));
            }
          }
        })

        .state('grant-annual-submission', {
          url: '/organisation/:orgId/annual-submission/submission-id/:submissionId/block/:blockId/',
          templateUrl: 'scripts/pages/organisation/recoverable-grant-submission/recoverableGrantSubmission.html',
          controller: 'RecoverableGrantSubmissionCtrl',
          controllerAs: '$ctrl',
          pageTitle: 'Grant Annual Submission',
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
          pageTitle: 'Grant Forecast Submission',
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
          template: '<projects-page project-states="$resolve.projectStates" all-programmes="$resolve.allProgrammes"></projects-page>',
          pageTitle: 'Projects',
          resolve: {
            allProgrammes(ProgrammeService) {
              return ProgrammeService.getProgrammesFilters().then(resp => {
                return resp.data;
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
          template: '<new-project-page programmes="$resolve.programmes"></new-project-page>',
          params: {
            programmes: null
          },
          controllerAs: '$ctrl',
          pageTitle: 'Create Project',
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
              return TemplateService.getTemplate(project.templateId, false, true)
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
                                       is-allow-all-file-download-enabled="$resolve.isAllFileDownloadEnabled"
                                       is-project-sharing-enabled="$resolve.isProjectSharingEnabled"
                                       label-message="$resolve.labelMessage"
                                       project="$resolve.projectOverview"
                                       template="$resolve.template"
                                       pre-set-labels="$resolve.preSetLabels"></project-overview>`,
          params: {
            backNavigation: null
          },
          pageTitle: 'Project Overview',
          resolve: {
            projectOverview($stateParams, ProjectService) {
              return ProjectService.getProjectOverview($stateParams.projectId).then(resp => resp.data);
            },

            template(TemplateService, projectOverview, $rootScope) {
              return TemplateService.getTemplate(projectOverview.templateId, false, true).then(resp => resp.data);
            },

            isSubmitToApproveEnabled(FeatureToggleService, Downgrade) {
            return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('SubmitAutoApprovalProject'));
            },

            isMarkedForCorporateEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('MarkProjectCorporate'));
            },

            isAllFileDownloadEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('AllowAllFileDownload'));
            },

            isLabelsFeatureEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('labels'));
            },

            isProjectSharingEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('ProjectSharing'));
            },

            labelMessage(ConfigurationService) {
              return ConfigurationService.getMessage('project-label').then(rsp => rsp.data);
            },
            preSetLabels(LabelService, projectOverview) {
              return LabelService.getPreSetLabels(projectOverview.managingOrganisationId, projectOverview.markedForCorporate).then(rsp => rsp.data);
            }
        }
        })

        .state('programmes', {
          url: '/programmes',
          template: '<programmes-page></programmes-page>',
          pageTitle: 'Programmes'
        })


        .state('programmes-new', {
          url: '/programmes/new',
          template: `<programme-page templates-list="$resolve.templatesList"
                                     assessment-templates="$resolve.assessmentTemplates"
                                     gla-roles="$resolve.glaRoles"
                                     new-programme-mode="true"></programme-page>`,

          controllerAs: '$ctrl',
          pageTitle: 'Create Programme',
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
                                     new-programme-mode="false"
                                     teams="$resolve.teams"
                                     organisations-with-access="$resolve.organisationsWithAccess"></programme-page>`,
          controllerAs: '$ctrl',
          pageTitle: 'Programme',
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
            allowChangeInUseAssessmentTemplate(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('AllowChangeInUseAssessmentTemplate'));
            },
            teams(programme, OrganisationService) {
              let managingOrgId = programme.managingOrganisationId;
              return managingOrgId ? OrganisationService.getOrganisationTeams(managingOrgId).then(rsp => rsp.data) : [];
            },
            organisationsWithAccess(programme , ProgrammeService) {
              return ProgrammeService.getDefaultAccess(programme.id).then(rsp => rsp.data);
            }
          }
        })

        .state('assessment-templates', {
          url: '/assessment-templates',
          template: '<assessment-templates assessment-templates="$resolve.assessmentTemplates"></assessment-templates>',
          pageTitle: 'Assessment Templates',
          resolve: {
            assessmentTemplates(AssessmentService) {
              return AssessmentService.getAssessmentTemplates().then(rsp => rsp.data);
            }
          }
        })

        .state('assessment-template-new', {
          url: '/assessment-templates/new',
          template: `<new-assessment-template></new-assessment-template>`,
          pageTitle: 'Create Assessment Template'
        })

        .state('assessment-template-paste', {
          url: '/assessment-templates/paste',
          template: `<paste-assessment-template></paste-assessment-template>`,
          pageTitle: 'Paste Assessment Template'
        })

        .state('assessment-template', {
          url: '/assessment-templates/:id',
          template: '<assessment-template assessment-template="$resolve.assessmentTemplate" assessment-templates="$resolve.assessmentTemplates" assessment-template-json="$resolve.assessmentTemplateJson" allow-change-in-use-assessment-template="$resolve.allowChangeInUseAssessmentTemplate"></assessment-template>',
          pageTitle: 'Assessment Template',
          resolve: {
            assessmentTemplate($stateParams, AssessmentService) {
              return AssessmentService.getAssessmentTemplate($stateParams.id).then(rsp => rsp.data);
            },
            assessmentTemplateJson($stateParams, PortableEntityService) {
              return PortableEntityService.getSanitisedEntity('AssessmentTemplate',$stateParams.id).then(rsp => JSON.stringify(rsp.data))
            },
            allowChangeInUseAssessmentTemplate(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('AllowChangeInUseAssessmentTemplate'));
            },
            assessmentTemplates(AssessmentService) {
              return AssessmentService.getAssessmentTemplates().then(rsp => rsp.data);
            }
          }
        })

        .state('assessment-template-edit', {
          url: '/assessment-templates/:id/edit',
          template: '<edit-assessment-template assessment-template="$resolve.assessmentTemplate" assessment-templates="$resolve.assessmentTemplates"></edit-assessment-template>',
          pageTitle: 'Edit Assessment Template',
          resolve: {
            assessmentTemplate($stateParams, AssessmentService) {
              return AssessmentService.getAssessmentTemplate($stateParams.id).then(rsp => rsp.data);
            },
            assessmentTemplates(AssessmentService) {
              return AssessmentService.getAssessmentTemplates().then(rsp => rsp.data);
            }
          }
        })

        .state('assessment', {
          url: '/assessments/:id',
          template: `<assessment assessment="$resolve.assessment"
                                 assessment-template="$resolve.assessmentTemplate"
                                 project="$resolve.project"
                                 editable="$resolve.editable"></assessment>`,
          pageTitle: 'Assessment',
          resolve: {
            assessment($stateParams, AssessmentService) {
              return AssessmentService.getAssessment($stateParams.id).then(rsp => rsp.data);
            },
            assessmentTemplate(AssessmentService, assessment) {
              return AssessmentService.getAssessmentTemplate(assessment.assessmentTemplateId).then(resp => resp.data);
            },

            project(ProjectService, assessment){
              return ProjectService.getProjectOverview(assessment.projectId).then(resp => resp.data);
            },

            editable (AssessmentService, UserService, assessment) {
              if(!UserService.hasPermission('assessment.manage') || assessment.status === 'Abandoned'){
                return false;
              }
              return AssessmentService.assessmentTemplatesForUser({programmeId: assessment.programmeId, templateId: assessment.templateId}).then(rsp => {
                return _.some(rsp.data, {id: assessment.assessmentTemplateId});
              });
            }
          },
          params: {
            backNavigation: null
          }
        })

        .state('assessments', {
          url: '/assessmentList',
          template: `<assessment-list project-states="$resolve.projectStates" all-programmes="$resolve.allProgrammes" assessment-templates="$resolve.assessmentTemplates"></assessment-list>`,
          pageTitle: 'Assessments',
          resolve: {
            allProgrammes(ProgrammeService) {
              return ProgrammeService.getProgrammesFilters().then(resp => {
                return resp.data;
              });
            },

            projectStates(ProjectService) {
              return ProjectService.getProjectStates().then(resp => resp.data);
            },

            assessmentTemplates(AssessmentService) {
              return AssessmentService.getAssessmentTemplateSummaries().then(rsp => _.sortBy(rsp.data, 'name'));
            },
          }
        })

        .state('assessment-edit', {
          url: '/assessment/:id/edit',
          template: '<edit-assessment assessment="$resolve.assessment" assessment-template="$resolve.assessmentTemplate" project="$resolve.project"></edit-assessment>',
          pageTitle: 'Edit Assessment',
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

            project(ProjectService, assessment){
              return ProjectService.getProjectOverview(assessment.projectId).then(resp => resp.data);
            },
          }
        })

        .state('notifications', {
          url: '/notifications',
          template: '<notifications-page></notifications-page>',
          controllerAs: '$ctrl',
          pageTitle: 'Notifications',
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
          pageTitle: 'All Payments',
          resolve: {
            programmes(ProgrammeService) {
              return ProgrammeService.getProgrammesFilters().then(resp => {
                return resp.data;
              });
            },
            paymentSources(ReferenceDataService, Downgrade) {
              return Downgrade.toPromise(ReferenceDataService.getAvailablePaymentSources());
            }
          }
        })

        .state('pending-payments', {
          url: '/pending-payments',
          template: `<pending-payments payment-groups="$resolve.paymentGroups"
                                       payment-decline-reason="$resolve.paymentDeclineReason"
                                       reclaim-decline-reason="$resolve.reclaimDeclineReason"></pending-payments>`,
          controllerAs: '$ctrl',
          pageTitle: 'Pending Payments',
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
          template: `<payment-summary payment-group="$resolve.paymentGroup"
                                      payment="$resolve.payment"
                                      milestone="$resolve.milestone"
                                      project="$resolve.project"
                                      is-reclaim-enabled="$resolve.isReclaimEnabled"
                                      reclaims="$resolve.reclaims"
                                      original-payment="$resolve.originalPayment"
                                      payment-history="$resolve.paymentHistory"></payment-summary>`,
          controllerAs: '$ctrl',
          pageTitle: 'Payment Summary',
          resolve: {
            isReclaimEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('reclaims'));
            },

            paymentGroup($stateParams, PaymentService) {
              return PaymentService.getPaymentGroupByPaymentId($stateParams.paymentId);
            },

            paymentHistory($stateParams, PaymentService, UserService) {
              if (UserService.hasPermissionStartingWith('payments.history')) {
                return PaymentService.getPaymentAuditHistory($stateParams.paymentId);
              } else {
                return [];
              }
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
          pageTitle: 'Reports',
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
            programmeReportEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('outputCSV'));
            },
            affordableHousingReportEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('AffordableHousingReport'));
            },
            boroughReportEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('BoroughReport'));
            }
          }
        })

        .state('reports-sql-editor', {
          url: '/reports/sqlEditor',
          template: `<reports-sql-editor></reports-sql-editor>`,
          pageTitle: 'Reports SQL Editor'
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
          pageTitle: 'Change Management Report',
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
              return TemplateService.getTemplate(latestProject.templateId, false, true).then(resp => resp.data);
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

            isDatePickerEnabled(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('CMSDatePicker'));
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
          pageTitle: 'Project Summary Report',
          resolve: {
            project($stateParams, ProjectService) {
              let params = {
                unapprovedChanges: _.isUndefined($stateParams.showUnapproved) ? true : $stateParams.showUnapproved,
                forComparison: true
              };
              return ProjectService.getProject($stateParams.projectId, params).then(rsp => rsp.data);
            },

            template(TemplateService, project) {
              return TemplateService.getTemplate(project.templateId, false, true).then(rsp => rsp.data);
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
          template: `<system-page sys-info="$resolve.systemInfo"
                        sys-metrics="$resolve.systemMetrics"
                        features="$resolve.features"
                        is-sql-editor-enabled="$resolve.isSqlEditorEnabled"></system-page>`,

          controllerAs: '$ctrl',
          pageTitle: 'System Dashboard',
          resolve: {
            systemInfo: (ActuatorService) => {
              return ActuatorService.getInfo().then(rsp => rsp.data);
            },
            systemMetrics: (ActuatorService) => {
              return ActuatorService.getMetrics().then(rsp => rsp.data);
            },
            isSqlEditorEnabled: (FeatureToggleService, Downgrade) => {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('SqlEditor'));
            },
            features(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.getFeatures());
            }
          }
        })
        .state('system-messages', {
          url: '/system/messages',
          template: `<messages-page messages="$resolve.messages"></messages-page>`,
          pageTitle: 'System Messages',
          resolve: {
            messages(ConfigurationService){
              return ConfigurationService.getMessages().then((resp)=>resp.data);
            }
          }
        })

        .state('system-sapData', {
          url: '/system/sapdata',
          template: `<sap-data-page sap-data="$resolve.sapData"></sap-data-page>`,
          controllerAs: '$ctrl',
          pageTitle: 'System SAP Data',
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
          template: `<templates-questions-page></templates-questions-page>`,
          pageTitle: 'Templates Questions'
        })

        .state('system-question', {
          url: '/system/questions/:questionId',
          template: `<question-page question="$resolve.question"></question-page>`,
          pageTitle: 'Template Question',
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
          template: `<question-form></question-form>`,
          pageTitle: 'Create Template Question'
        })

        .state('system-question-edit', {
          url: '/system/question/:questionId/edit',
          template: `<question-form question="$resolve.question"></question-form>`,
          pageTitle: 'Edit Template Question',
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
          pageTitle: 'Project Templates',
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
          pageTitle: 'Project Template',
          resolve: {
            template(TemplateService, $stateParams) {
              return TemplateService.getTemplate($stateParams.templateId, true, false).then(resp => {
                return resp.data;
              });
            },
            originalTemplate(TemplateService, $stateParams) {
              return TemplateService.getTemplate($stateParams.templateId, false, false).then(resp => {
                return resp.data;
              });
            },

            blockTypes(ReferenceDataService, Downgrade) {
              return Downgrade.toPromise(ReferenceDataService.getBlockTypes());
            }
          }
        })

        .state('system-template-details-create', {
          url: '/system/template-create',
          template: `<template-details-page block-types="$resolve.blockTypes"></template-details-page>`,
          pageTitle: 'Create Project Template',
          resolve: {
            blockTypes(ReferenceDataService, Downgrade) {
              return Downgrade.toPromise(ReferenceDataService.getBlockTypes());
            }
          }
        })

        .state('system-features', {
          url: '/system/features',
          template: `<features-page features="$resolve.features"></features-page>`,
          pageTitle: 'System Features',
          resolve: {
            features(FeatureToggleService, Downgrade) {
              return Downgrade.toPromise(FeatureToggleService.getFeatures());
            }
          }
        })

        .state('skill-profiles', {
          url: '/system/skill-profiles',
          template: `<skill-profiles></skill-profiles>`,
          pageTitle: 'Skills Profiles'
        })

        .state('data-validation-details', {
          url: '/dataValidationDetails',
          template: `<validation-details-page sys-info="$resolve.systemInfo"></validation-details-page>`,
          pageTitle: 'Data Validation Details',
          resolve: {
            systemInfo(ActuatorService) {
              return ActuatorService.getInfo().then(rsp => rsp.data);
            }
          }
        })

        .state('sql', {
          url: '/system/sqlManager',
          template: `<sql-manager sql-updates="$resolve.sqlUpdates"></sql-manager>`,
          controllerAs: '$ctrl',
          pageTitle: 'SQL Manager',
          resolve: {
            sqlUpdates: (DatabaseUpdateService) => {
              return DatabaseUpdateService.getSqlQueries();
            }
          }
        })

        .state('sql-details', {
          url: '/system/sqlManager/sqlDetails/:sqlId',
          template: `<sql-details sql-update-details="$resolve.sqlUpdateDetails"></sql-details>`,
          controllerAs: '$ctrl',
          pageTitle: 'SQL Details',
          resolve: {
            sqlUpdateDetails: (DatabaseUpdateService, $stateParams) => {
              return DatabaseUpdateService.getUpdateDetails($stateParams.sqlId).then(rsp => rsp.data);
            }
          }
        })

        .state('sql-create', {
          url: '/system/sqlManager/create',
          template: `<create-new-sql></create-new-sql>`,
          pageTitle: 'Create SQL'
        })

        .state('gc', {
          url: '/system/gc',
          template: `<gc></gc>`,
          pageTitle: 'GC'
        })

        .state('audit-activity', {
          url: '/auditActivity',
          template: `<audit-activity></audit-activity>`,
          pageTitle: 'Audit Activity'
        })

        .state('finance-categories', {
          url: '/finance-categories',
          template: `<finance-categories-page finance-categories="$resolve.financeCategories"></finance-categories-page>`,
          controllerAs: '$ctrl',
          pageTitle: 'Finance Categories',
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
          pageTitle: 'User Account',
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
          template: '<new-annual-submission organisation="$resolve.organisation" years="$resolve.years" remaining-years="$resolve.remainingYears"></new-annual-submission>',
          controllerAs: '$ctrl',
          pageTitle: 'Create Annual Submission',
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
          template: '<annual-submission organisation="$resolve.organisation" annual-submission="$resolve.annualSubmission"></annual-submission>',
          controllerAs: '$ctrl',
          pageTitle: 'Annual Submission',
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


        .state('confirm-user-created', {
          url: '/confirmation/user-created',
          templateUrl: 'scripts/pages/confirmation/user-created.html',
          controllerAs: '$ctrl',
          pageTitle: 'User Registration Confirmation'
        })

        .state('confirm-org-and-user-created', {
          url: '/confirmation/org-and-user-created',
          template: '<org-and-user-created-confirmation show-navigation-circles="$resolve.allowNewRegistrationProcess"></org-and-user-created-confirmation>',
          pageTitle: 'Organisation Registration Confirmation',
          resolve: {
            allowNewRegistrationProcess: ['FeatureToggleService', 'Downgrade', (FeatureToggleService, Downgrade) => {
              return Downgrade.toPromise(FeatureToggleService.isFeatureEnabled('AllowNewRegistrationProcess'));
            }],
          },
        })
        .state('permissions', {
          url: '/permissions',
          template: `<permissions-page permissions="$resolve.permissions"></permissions-page>`,
          pageTitle: 'Permissions',
          resolve: {
            permissions(PermissionService) {
              return PermissionService.getPermissions().then(rsp => rsp.data);
            }
          }
        })
        .state('preSetLabels', {
          url: '/preSetLabels',
          template: `<labels-page labels="$resolve.labels" managing-organisations="$resolve.managingOrganisations"></labels-page>`,
          pageTitle: 'Labels Management',
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
          pageTitle: 'Notifications',
          resolve: {
            allNotifications(NotificationsService) {
              return NotificationsService.getAllNotifications().then(rsp => rsp.data);
            }
          }
        })

        .state('scheduledNotifications', {
          url: '/scheduledNotifications',
          template: `<scheduled-notifications-page scheduled-notifications="$resolve.scheduledNotifications"
                                                   available-roles="$resolve.availableRoles"></scheduled-notifications-page>`,
          pageTitle: 'Scheduled Notifications',
          resolve: {
            scheduledNotifications(NotificationsService) {
              return NotificationsService.getScheduledNotifications().then(rsp => rsp.data);
            },
            availableRoles(OrganisationService) {
              return OrganisationService.getAvailableRoles().then(rsp => rsp.data);
            }
          }
        })

        .state('overrides', {
          url: '/overrides',
          template: `<overrides-page overrides="$resolve.overrides" metadata="$resolve.metadata"></overrides-page>`,
          pageTitle: 'Overrides',
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
          pageTitle: 'Outputs Configuration',
          resolve: {
            outputConfigurations(OutputConfigurationService) {
              return OutputConfigurationService.getAllOutputConfiguration().then(rsp => rsp.data);
            }
          }
        })
    }
  ]);
