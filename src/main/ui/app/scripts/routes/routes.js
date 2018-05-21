/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './projectBlockRoutes'



/*angular.module('GLA').config(['$stateProvider', function($stateProvider) {
  var $delegate = $stateProvider.state;

  $stateProvider.state = function(name, definition) {
    definition.resolve = angular.extend({}, definition.resolve, {
      myCustomResolve: function(UserService) {
        let user = UserService.currentUser();
        console.log('user', user);
        return UserService.refreshUser();
      }
    });


    return $delegate.apply(this, arguments);
  };
}]);*/



angular.module('GLA')
  .config(['$stateProvider', '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {

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
              if(!hasAlreadyBeenWarned){
                if(deviceDetector.isMobile() && !deviceDetector.isTablet()){
                  $rootScope.showGlobalLoadingMask = false;
                  MobileDeviceWarning.show().result.then(()=>{
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
          params:{
            data: null
          }
        })

        .state('user', {
          url: '/user',
          templateUrl: 'scripts/pages/user/home/userHome.html',
          controller: 'UserHomeCtrl',
          controllerAs: '$ctrl'
        })

        .state('organisations', {
          url: '/organisations?searchText',
          templateUrl: 'scripts/pages/organisations/organisations.html',
          controller: 'OrganisationsCtrl',
          controllerAs: '$ctrl',
          resolve: {
            organisationTypes(UserService, OrganisationService) {
              let currentUser = UserService.currentUser();
              return currentUser.approved? OrganisationService.organisationTypes(): {};
            }
          },
          params:{
            searchText: null
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
          programmes(ProgrammeService){
            return ProgrammeService.getEnabledProgrammes().then(resp => _.orderBy(resp.data, 'name'));
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
          programmes(ProgrammeService){
            return ProgrammeService.getEnabledProgrammes().then(resp => _.orderBy(resp.data, 'name'));
          }
        }
      })

      .state('organisations-new', {
        url: '/organisations/new',
        template: `<new-organisation-page organisation-types="$ctrl.organisationTypes"></new-organisation-page>`,
        controller(organisationTypes) {
          this.organisationTypes = organisationTypes;
        },
        controllerAs: '$ctrl',
        resolve: {
          organisationTypes : (OrganisationService) => {
            return OrganisationService.organisationTypes();
          }
        }
      })

      .state('organisations-new-profile', {
        url: '/organisations/new-profile?managingOrgId',
        templateUrl: 'scripts/pages/organisation/organisationForm/organisationForm.html',
        controller: 'NewOrganisationProfileCtrl',
        controllerAs: '$ctrl',
        resolve: {
          organisationTypes(OrganisationService) {
            return OrganisationService.organisationTypes();
          },

          managingOrganisations(OrganisationService){
            return OrganisationService.managingOrganisations().then(rsp => rsp.data);
          }
        }
      })

        .state('organisation', {
          url: '/organisation/:orgId',
          template: `<organisation-page organisation-types="$ctrl.organisationTypes" organisation="$ctrl.organisation" available-user-roles="$ctrl.availableUserRoles"></organisation-page>`,
          controller(organisationTypes, organisation, availableUserRoles) {
            this.organisationTypes = organisationTypes;
            this.organisation = organisation;
            this.availableUserRoles = availableUserRoles;
          },
          controllerAs: '$ctrl',
          resolve: {
            organisationTypes : (OrganisationService) => {
              return OrganisationService.organisationTypes();
            },

            organisation : (OrganisationService, $stateParams) => {
              return OrganisationService.getDetails($stateParams.orgId);
            },

            availableUserRoles : (UserService, OrganisationService, $stateParams) => {
              if(UserService.hasPermission('user.approve', $stateParams.orgId)){

                return OrganisationService.getAvailableUserRoles($stateParams.orgId).then(rsp => rsp.data || []);
              }
            }
          }
        })

      .state('organisation-edit', {
        url: '/organisation/:orgId/edit',
        template: `<edit-organisation-page organisation-types="$ctrl.organisationTypes" organisation="$ctrl.organisation"></edit-organisation-page>`,
        controller(organisationTypes, organisation) {
          this.organisationTypes = organisationTypes;
          this.organisation = organisation;
        },
        controllerAs: '$ctrl',
        params: {
          orgId: null
        },
        resolve: {
          organisationTypes: (OrganisationService) => {
            return OrganisationService.organisationTypes();
          },

          organisation : (OrganisationService, $stateParams) => {
            return OrganisationService.getDetails($stateParams.orgId).then(resp => resp.data);
          }
        }
      })

      .state('organisation-programme', {
        url: '/organisation/:organisationId/programme/:programmeId',
        templateUrl: 'scripts/pages/organisation/programme/organisationProgramme.html',
        controller: 'OrganisationProgrammeCtrl',
        controllerAs: '$ctrl',
        params: {
          organisationId: null,
          programmeId: null,
          organisation: null,
          programme: null
        }
      })

      .state('projects', {
        controller(programmes) {
          this.programmes = programmes;
        },
        url: '/projects?title&organisationId&programmeId&programmeName',
        // templateUrl: 'scripts/pages/projects/projects.html',
        template: '<projects-page programmes="$ctrl.programmes"></projects-page>',
        // controller: 'ProjectsCtrl',
        controllerAs: '$ctrl',
        resolve: {
          programmes: ($stateParams, ProgrammeService, UserService, $rootScope) => {
            return ProgrammeService.getEnabledProgrammes()
              .then(resp => {
                return _.orderBy(resp.data, 'name');
              });
          }
        },
        params:{
          title: null,
          organisationId: null,
          programmeId: null,
          programmeName: null
        }
      })

      .state('projects-new', {
        url: '/projects/new',
        template:'<new-project-page programmes="$ctrl.programmes"></new-project-page>',
        params: {
          programmes: null
        },
        controller(programmes) {
          this.programmes = programmes;
        },
        controllerAs: '$ctrl',
        resolve: {
          programmes($state, ProgrammeService){
            if($state.params && $state.params.programmes){
              return $state.params.programmes;
            } else {
              return ProgrammeService.getEnabledProgrammes().then(resp => {
                return _.orderBy(resp.data, 'name');
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
            projectSectionSaved: null,
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
            template: (ProjectService, project, $rootScope) => {
              $rootScope.showGlobalLoadingMask = true;
              return ProjectService.getTemplate(project.templateId)
                .then(resp => {
                  $rootScope.showGlobalLoadingMask = false;
                  return resp.data;
                });
            }
          }
        })

        .state('project.overview', {
          url: '',
          // templateUrl: 'scripts/pages/project/overview/projectOverview.html',
          template: '<project-overview is-submit-to-approve-enabled="$ctrl.isSubmitToApproveEnabled" project="$ctrl.project" template="$ctrl.template"></project-overview>',
          controller(isSubmitToApproveEnabled, project, template){
            this.isSubmitToApproveEnabled = isSubmitToApproveEnabled;
            this.project = project;
            this.template = template;
          },
          controllerAs: '$ctrl',
          resolve: {
            isSubmitToApproveEnabled(FeatureToggleService) {
              return FeatureToggleService.isFeatureEnabled('SubmitAutoApprovalProject').then(rsp => rsp.data);
            }
          }
        })

        .state('programmes', {
          url: '/programmes',
          templateUrl: 'scripts/pages/programmes/programmes.html',
          controller: 'ProgrammesCtrl',
          controllerAs: '$ctrl'
        })

        .state('programmes-new', {
          url: '/programmes/new',
          templateUrl: 'scripts/pages/programme/programme.html',
          controller: 'ProgrammeCtrl',
          controllerAs: '$ctrl'
        })

        .state('programme', {
          url: '/programme/:programmeId',
          templateUrl: 'scripts/pages/programme/programme.html',
          controller: 'ProgrammeCtrl',
          controllerAs: '$ctrl'
        })

        .state('notifications', {
          url: '/notifications',
          template: '<notifications-page></notifications-page>',
          controller() {

          },
          controllerAs: '$ctrl',
          resolve: {
            config(ConfigurationService) {
              return ConfigurationService.getConfig().then(function(resp) {
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
            programmes(ProgrammeService){
              return ProgrammeService.getProgrammes().then(resp => {return resp.data;});
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
            this.paymentGroup= paymentGroup;
            this.payment = payment;
            this.milestone = milestone;
            this.project = project;
            this.reclaims = reclaims;
            this.originalPayment = originalPayment;
            this.isReclaimEnabled = isReclaimEnabled;
          },
          controllerAs: '$ctrl',
          resolve: {
            isReclaimEnabled(FeatureToggleService){
              return FeatureToggleService.isFeatureEnabled('reclaims').then(rsp => rsp.data);
            },

            paymentGroup($stateParams, PaymentService) {
              return PaymentService.getPaymentGroupByPaymentId($stateParams.paymentId);
            },

            payment($stateParams, paymentGroup){
              return _.find(paymentGroup.payments, {id: +$stateParams.paymentId});
            },

            project(payment, ProjectService) {
              return ProjectService.getProject(payment.projectId).then(resp => resp.data);
            },

            originalPayment(payment, PaymentService) {
              let originalPaymentId = payment.reclaimOfPaymentId;
              if(originalPaymentId){
                return PaymentService.getPaymentGroupByPaymentId(originalPaymentId).then(paymentGroup => {
                  return _.find(paymentGroup.payments, {id: originalPaymentId});
                });
              }
            },

            reclaims(payment, PaymentService){
              return PaymentService.getReclaims(payment.id, payment.projectId);
            },

            milestone(payment, PaymentService){
              return PaymentService.getPaymentMilestone(payment);
            }
          }
        })

        .state('reports', {
          url: '/reports',
          templateUrl: 'scripts/pages/reports/reports.html',
          controller: 'ReportsCtrl',
          controllerAs: '$ctrl',
          resolve: {
            programmes: (ProgrammeService) => {
              return ProgrammeService.getProgrammes().then(rsp => rsp.data);
            },
            reports: (ReportService) => {
              return ReportService.getReports().then(rsp => rsp.data);
            }
          }
        })

        .state('change-report', {
          url: '/change-report/:projectId',
          // templateUrl: 'scripts/pages/change-report/changeReport.html',
          template: `<change-report-page
                        latest-project="$ctrl.latestProject"
                        last-approved-project="$ctrl.lastApprovedProject"
                        template="$ctrl.template"
                        current-financial-year="$ctrl.currentFinancialYear"
                        is-date-picker-enabled="$ctrl.isDatePickerEnabled"
                        project-history="$ctrl.projectHistory"></change-report-page>`,
          controller(latestProject, lastApprovedProject, template, projectHistory, currentFinancialYear, isDatePickerEnabled) {
            this.latestProject = latestProject;
            this.lastApprovedProject = lastApprovedProject;
            this.template = template;
            this.projectHistory = projectHistory;
            this.currentFinancialYear = currentFinancialYear;
            this.isDatePickerEnabled = isDatePickerEnabled;
          },
          controllerAs: '$ctrl',
          resolve: {
            latestProject: (ProjectService, $stateParams) => {
              let params= {
                compareToStatus: 'LAST_APPROVED',
                forComparison:true,
                comparisonDate: $stateParams.comparisonDate
              };
              if($stateParams.comparisonDate){
                delete params.compareToStatus;
              }

              return ProjectService.getProject($stateParams.projectId, params).then(resp => resp.data);
            },

            lastApprovedProject: (ProjectService, $stateParams) => {
              console.log('stateParams',$stateParams.comparisonDate);

              return ProjectService.getProject($stateParams.projectId, {
                unapprovedChanges: false,
                forComparison:true,
                comparisonDate: $stateParams.comparisonDate
              }).then(resp => resp.data);
            },
            template: (ProjectService, latestProject) => {
              return ProjectService.getTemplate(latestProject.templateId).then(resp => resp.data);
            },
            projectHistory: (ProjectService, $stateParams) => {
              return ProjectService.getProjectHistory($stateParams.projectId);
            },

            currentFinancialYear: (ProjectService, $stateParams) => {
              return ProjectService.getCurrentFinancialYear().then(rsp => rsp.data)
            },

            isDatePickerEnabled(FeatureToggleService){
              return FeatureToggleService.isFeatureEnabled('CMSDatePicker').then(rsp => rsp.data);
            }
          },
          params: {
            comparisonDate: null
          }
        })

        // TODO: possibly deprecated?
        .state('admin', {
            abstract: true,
            url: '/admin',
            template: '<ui-view></ui-view>'
          })

        .state('admin.content', {
          url: '/content',
          templateUrl: 'scripts/components/admin/content/coming-soon-admin.tpl.html',
          controller: 'CSoonAdminCtrl',
          controllerAs: 'csoon'
        })

        .state('system', {
          url: '/system',
          template: `<system-page sys-info="$ctrl.sysInfo" sys-metrics="$ctrl.sysMetrics"></system-page>`,
          controller(systemInfo, systemMetrics) {
            this.sysInfo = systemInfo;
            this.sysMetrics = systemMetrics;
          },
          controllerAs: '$ctrl',
          resolve: {
            systemInfo: (ActuatorService) => {
              return ActuatorService.getInfo().then(rsp => rsp.data);
            },
            systemMetrics: (ActuatorService) => {
              return ActuatorService.getMetrics().then(rsp => rsp.data);
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
              if($stateParams.userId){
                return UserService.getUserProfile($stateParams.userId).then(rsp => rsp.data);
              } else {
                console.error('Need to specify userId');
                return false;
              }
            },

            userThresholds: ($stateParams, UserService) => {
              if (UserService.hasPermission('user.org.pending.threshold.set')) {
                return UserService.getUserThresholds($stateParams.userId).then(rsp => rsp.data);
              }else{
                return [];
              }
            }
          }
        })

    }
  ]);
