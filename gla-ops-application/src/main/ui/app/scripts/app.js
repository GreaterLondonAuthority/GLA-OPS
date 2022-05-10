/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
//--- TODO move dependencies to relevant place where it is actually used ---
import './routes/routes';

import './services/AssessmentService';
import './services/ModalDisplayService';
import './services/SESSION';
import './services/OrganisationService';
import './services/OrganisationGroupService';
import './services/ProjectService';
import './services/SessionService';
import './services/PaymentService';
import './services/DatabaseUpdateService';
import './services/AuditService';
import './services/blocks/OutputsService';
import './services/blocks/ReceiptsService';
import './services/blocks/ProjectBlockService';
import './services/blocks/MilestonesService';
import './services/ProgrammeService';
import './services/UserService';
import './services/blocks/RisksService';
import './services/blocks/UnitsService';
import './services/blocks/GrantSourceService';
import './services/blocks/BudgetService';
import './services/blocks/ProjectFundingService';
import './services/ReferenceDataService';
import './services/ReportService';
import './services/blocks/ProjectDetailsService';
import './services/blocks/ProjectSkillsService';
import './services/NotificationsService';
import './services/ActuatorService';
import './services/FinanceService';
import './services/AnnualSubmissionService';
import './services/LockingService';
import './services/SapDataService';
import './services/SkillProfilesService';
import './services/blocks/CommentsService';
import './services/QuestionsService';
import './services/DashboardService';
import './services/TeamService';
import './services/PermissionService'
import './services/LabelService'
import './services/OverridesService';
import './services/PortableEntityService'
import './services/RepeatingEntityService'
import './services/blocks/OtherFundingService'
import './services/FileService'
import './services/PostLoginService'
import './services/Downgrade'
import './util/Util';

import './directives/permissionUtil/setPermission';

import './components/tenure-tiles/tenureTiles';
import './components/header-status/headerStatus';
import './components/project-block-footer/projectBlockFooter';
import './components/project-id/projectId';
import './components/project-history/projectHistory';
import './components/input-cost-budget/inputCostBudget';
import './components/password-strength/passwordStrength';
import './components/fileUpload/fileUpload';
import './components/forecast-change/forecastChange';
import './components/financial-year/financialYear.js';
import './components/section-header/section-header.js';
import './components/section-header2/section-header.js';
import './components/mobileDeviceWarning/modal.js';
import './components/number-mask/numberMask';
import './components/abs/absFilter';
import './components/bool/boolFilter.js';
import './components/gla-currency/glaCurrency.js';
import './components/fYear/fYear';
import './components/multi-panel/mp-field/mpField.js';
import './components/month-selector/monthSelector.js';
import './components/quarter-selector/quarterSelector.js';
import './components/comments-list/commentsList.js';
import './components/comments-form/commentsForm.js';
import './components/show-more-btn/showMoreBtn.js';
import './components/milestone-claim-status/milestoneClaimStatus.js';
import './components/milestone-actions/milestoneActions.js';
import './components/template-details/templateDetails.js';
import './components/entities-list/entitiesList.js';
import './components/file-upload-modal/fileUploadModal.js';
import './components/programmes-carousel/programmesCarousel.js';
import './components/delivery-partners-table/deliveryPartnersTable.js';

import './pages/assessment/assessment';
import './pages/assessment/assessment-list';
import './pages/assessment/editAssessment';
import './pages/assessment/outcome-assessment-summary/outcomeAssessmentSummary';
import './pages/assessment-templates/assessmentTemplate';
import './pages/assessment-templates/assessmentTemplates';
import './pages/assessment-templates/editAssessmentTemplate';
import './pages/assessment-templates/newAssessmentTemplate';
import './pages/assessment-templates/pasteAssessmentTemplate';
import './pages/assessment-templates/add-score-modal/addScoreModal';
import './pages/assessment-templates/add-outcome-modal/addOutcomeModal';
import './pages/assessment-templates/add-section-modal/addSectionModal';
import './pages/assessment-templates/add-criteria-modal/addCriteriaModal';
import './pages/project/project-budget/fileDeleteConfirmationModal';
import './pages/project/project-budget/fileUploadErrorModal';

import './pages/registration/registrationCtrl';
import './pages/registration/registrationForm/userRegistrationForm';

import './pages/organisation-registration/organisation-registration-programme/organisationRegistrationProgramme.js';
import './pages/organisation-registration/organisation-registration-form/organisationRegistrationForm.js';
import './pages/organisation-registration/organisation-registration-user/organisationRegistrationUser.js';

import './pages/reset-password/passwordResetCtrl';
import './pages/user/home/userHomeCtrl';
import './pages/organisations/organisationsCtrl';
import './pages/teams/teamsPage.js';
import './pages/organisation/organisationCtrl';
import './pages/organisation/organisationForm/editOrganisationCtrl';
import './pages/organisation/organisationForm/newOrganisationCtrl';
import './pages/organisation/organisationForm/multiStepOrganisationForm';
import './pages/organisation/organisationForm/newOrganisationProfileCtrl';
import './pages/organisation/contracts/contractsList';
import './pages/organisation/programme/organisationProgramme';
import './pages/organisation/programme/create-delegated/modal';
import './pages/organisation/programmes/programmesList';
import './pages/organisation/recoverable-grant-submission/recoverableGrantSubmission';
import './pages/organisation/recoverable-grant-submission/recoverableGrantSubmissionForecast';
import './pages/organisation/new-annual-submission/newAnnualSubmission';
import './pages/organisation/annual-submission/annualSubmission';
import './pages/organisation/rejectModal/modal.js';

import './pages/programmes/programmes';
import './pages/programme/programmeCtrl';
import './pages/programme/programme-project-type/programmeProjectType';
import './pages/reports/reportsCtrl';
import './pages/reports/sqlEditor/reportsSqlEditor';
import './pages/summary-report/summaryReportPageCtrl';
import './pages/summary-report/outputsSummaryReport';
import './pages/summary-report/risksAndIssuesSummaryReport';
import './pages/summary-report/budgetsSummaryReport';
import './pages/summary-report/milestonesSummaryReport';
import './pages/summary-report/calculateGrantSummaryReport';
import './pages/summary-report/developerLedGrantSummaryReport';
import './pages/summary-report/negotiatedGrantSummaryReport';
import './pages/summary-report/indicativeGrantSummaryReport';
import './pages/summary-report/grantSourceSummaryReport';
import './pages/summary-report/fundingSummaryReport';
import './pages/summary-report/learningGrantSummaryReport';
import './pages/summary-report/outputsCostSummaryReport';
import './pages/summary-report/deliveryPartnersSummaryReport';
import './pages/summary-report/fundingClaimsSummaryReport.js';

import './pages/system-messages/messagesPageCtrl';
import './pages/system-messages/systemMessageSetupItem';
import './pages/system-messages/modal';
import './pages/system/systemCtrl';
import './pages/system/validation-details/validationDetailsCtrl';
import './pages/system/actionModal';
import './pages/system/question/question';
import './pages/system/questions-form/questionForm';
import './pages/system/sapData/page';
import './pages/system/templates-questions/templateQuestionsPage';
import './pages/system/templates/templatesPage';
import './pages/system/template-details/templateDetailsPage';
import './pages/system/features/featuresPage';
import './pages/system/gc/gc';
import './pages/system/skill-profiles/skillProfiles';
import './pages/sql/sqlCtrl';
import './pages/sql/new/newSqlCtrl';
import './pages/sql/sqlDetailsCtrl';
import './pages/audit-activity/auditActivityCtrl';
import './pages/project/progress-updates/progressUpdatesCtrl.js';

import './pages/change-report/changeReportCtrl';
import './pages/user-account/userAccountCtrl';

import './pages/change-report/change-report-field/changeReportField';
import './pages/change-report/change-report-field-files/changeReportFieldFiles';
import './pages/change-report/change-report-field-lookup/changeReportFieldLookup';
import './pages/change-report/change-report-coordinates/changeReportCoordinates';
import './pages/change-report/change-report-tile-total/changeReportTileTotal';
import './pages/change-report/change-report-tiles/changeReportTiles';
import './pages/change-report/change-report-tiles/change-report-tile-row/changeReportTileRow';

import './pages/change-report/blocks/projectDetailsChangeReport';
import './pages/change-report/blocks/budgetsChangeReport';
import './pages/change-report/blocks/calculateGrantChangeReport';
import './pages/change-report/blocks/negotiatedGrantChangeReport';
import './pages/change-report/blocks/developerLedGrantChangeReport';
import './pages/change-report/blocks/indicativeGrantChangeReport';
import './pages/change-report/blocks/grantSourceChangeReport';
import './pages/change-report/blocks/designStandardsChangeReport';
import './pages/change-report/blocks/riskAndIssuesChangeReport';
import './pages/change-report/blocks/additionalQuestionsChangeReport';
import './pages/change-report/blocks/outputsChangeReport';
import './pages/change-report/blocks/receiptsChangeReport';
import './pages/change-report/blocks/unitDetailsChangeReport';
import './pages/change-report/blocks/milestonesChangeReport';
import './pages/change-report/blocks/internalBlocksChangeReport';
import './pages/change-report/blocks/fundingChangeReport';
import './pages/change-report/blocks/learningGrantChangeReport';
import './pages/change-report/blocks/outputsCostsChangeReport';
import './pages/change-report/blocks/deliveryPartnersChangeReport';
import './pages/change-report/blocks/fundingClaimsChangeReport.js';

import './pages/notifications/notifications-page';
import './pages/notifications/notifications-group';
import './pages/notifications/notification';
import './pages/notifications/createOrEditScheduledNotification/modal';
import './pages/notifications/deleteNotification/modal';
import './pages/notifications/scheduledNotificationsPage';

import './pages/payments/pending-payments/pendingPaymentsCtrl';
import './pages/payments/all-payments/allPaymentsCtrl';
import './pages/payments/payment-summary/paymentSummaryCtrl';
import './pages/payments/pending-payments/decline-dialog/declineDialog';
import './pages/payments/pending-payments/interest-dialog/interestDialog';
import './pages/consortiums/consortiumsCtrl';
import './pages/consortiums/new/newConsortiumCtrl';
import './pages/consortiums/edit/editConsortiumCtrl';
import './pages/project/design-standards/designStandardsCtrl';
import './pages/project/grant-source/grantSourceCtrl';
import './pages/project/details/projectDetailsCtrl';
import './pages/project/internal-assessment/internalAssessmentCtrl';
import './pages/project/internal-questions/internalQuestionsCtrl';
import './pages/project/internal-risk/internalRiskCtrl';
import './pages/project/milestones/projectMilestonesCtrl';
import './pages/project/milestones/projectMilestonesModal';
import './pages/project/milestones/claimMilestoneModal/modal';
import './pages/project/milestones/reclaimMilestoneModal/modal';
import './pages/project/project-budget/projectBudgetCtrl';
import './pages/project/project-budget/financeSummary';
import './pages/project/project-budget/forecast/projectBudgetForecast';
import './pages/project/project-budget/forecast/forecastMonthRow';

import './pages/project/questions/questionsPage';
import './pages/project/grant/calculate-grant/calculateGrantCtrl';
import './pages/project/grant/negotiated-grant/negotiatedGrantCtrl';
import './pages/project/grant/GrantService';
import './pages/project/grant/developer-led-grant/developerLedGrantCtrl';
import './pages/project/grant/indicative-grant/indicativeGrantCtrl';
import './pages/project/grant/grant-table/grantTable';
import './pages/project/grant/total-grant/totalGrant';
import './pages/project/grant/claimed-units/claimedUnits';
import './pages/project/outputs/outputsCtrl';
import './pages/project/outputs/outputs-baselines-table/outputsBaselinesTable.js';
import './pages/project/outputs/financial-year-monthly-outputs-table/financialYearMonthlyOutputsTable.js';
import './pages/project/outputs/financial-year-quarterly-outputs-table/financialYearQuarterlyOutputsTable.js';
import './pages/project/outputs-costs/outputsCostsCtrl';
import './pages/project/receipts/receiptsCtrl';
import './pages/project/receipts/forecast/receiptsMonthRow';
import './pages/project/receipts/wizard/receiptWizard';
import './pages/project/risks/risksCtrl';
import './pages/project/risks/manage-project-risks/manageProjectRisk';
import './pages/project/risks/manage-project-issues/manageProjectIssues';
import './pages/project/risks/risk-rating/riskRating';
import './pages/project/risks/risk-and-issue-modal/modal';
import './pages/project/risks/add-risk-action-modal/modal';
import './pages/project/units/unitsCtrl';
import './pages/project/funding/fundingPageCtrl';
import './pages/project/funding/yearly-budget-funding-totals/yearlyBudgetFundingTotalsTable';
import './pages/project/learning-grant/learningGrantCtrl';
import './pages/project/funding-claims/fundingClaimsCtrl';
import './pages/project/funding-claims/funding-claims-table/fundingClaimsTable.js';
import './pages/project/funding-claims/funding-claims-table-procured/fundingClaimsTableProcured.js';
import './pages/project/other-funding/otherFundingCtrl';
import './pages/project/other-funding/other-funding-table/otherFundingTable.js';
import './pages/project/deliveryPartners/deliveryPartners.js';
import './pages/project/objectives/objectivesPage.js';
import './pages/project/objectives/objective.js';
import './pages/project/user-defined-outputs/userDefinedOutputsPage.js';
import './pages/project/user-defined-outputs/userDefinedOutput.js';
import './pages/project/elements/elementsPage.js';
import './pages/project/elements/blockElement.js';
import './pages/user-account/modal/requestAdditionalRoleModal.js';
import './pages/users/modal/addUserToTeamModal';
import './pages/users/users';
import './pages/permissions/permissionsPage.js';
import './pages/labels/labelsPage.js';
import './pages/all-notifications/allNotificationsPage.js';
import './pages/overrides/overridesPage.js';
import './pages/confirmation/orgAndUserCreatedConfirmation.js';
import './pages/project/funding-claims/contract-type-change-modal/contractTypeChangeModal.js';

import './components/common/input/date-input/dateInput';
import './components/common/modifiers/focus-me/focusMe';
import './components/common/modifiers/numbers-only/numbersOnly';
import './components/common/modifiers/numbers-with-negative/numbersWithNegative';
import './components/common/json-viewer-dialog/JSONViewerDialog';
import './components/common/overview-block/overviewBlock';
import './components/common/project-overview-block/projectOverviewBlock';
import './components/total-box/totalBox';
import './components/change-on-blur/change-on-blur';
import './components/gla-read-only/gla-read-only.js';
import './components/style-placeholder/style-placeholder.js'
import './components/default-value/defaultValue';
import './components/on-enter/onEnter.js'
import './components/allow-enter/allowEnter.js'
import './components/org-lookup/org-lookup';
import './components/report-header/reportHeader';
import './components/report-subheader/reportSubHeader';
import './components/report-section/reportSection';
import './components/output-summaries-table/outputSummariesTable';
import './components/output-unit-cost/outputUnitCost';
import './components/milestones-table/milestonesTable';
import './components/programme-templates-table/programmeTemplatesTable';
import './components/questions/questions.js';
import './components/learning-grant-table/learningGrantTable.js';
import './components/navigation-circles/navigationCircles.js';

const WEB_APP_NAME = 'Open Project System | Greater London Authority (GLA)';

angular.module('GLA')

  //Moved from appCtrl.js
  .run(['$rootScope', '$window', 'ConfigurationService', 'UserService', '$log', 'PermPermissionStore', 'NgxPermissionsService', 'SessionService', function ($rootScope, $window, ConfigurationService, UserService, $log, PermPermissionStore, NgxPermissionsService, SessionService) {

    // jscs:disable requireCamelCaseOrUpperCaseIdentifiers
    // jscs:disable requireParenthesesAroundIIFE

    $rootScope.envVars = {};
    $rootScope.gaCache = [];

    //TODO: maybe move to its own service
    const initGoogleAnalytics = (data) => {
      var IGNORE_DEBUG = true;

      var ga_script = 'https://www.google-analytics.com/analytics.js';
      var env = data['env-name'];

      if (!IGNORE_DEBUG && (env === 'local' || env === 'Dev')) {
        ga_script = 'https://www.google-analytics.com/analytics_debug.js';
      }
      !function (A, n, g, u, l, a, r) {
        A.GoogleAnalyticsObject = l, A[l] = A[l] || function () {
          (A[l].q = A[l].q || []).push(arguments)
        }, A[l].l = +new Date, a = n.createElement(g),
          r = n.getElementsByTagName(g)[0], a.src = u, r.parentNode.insertBefore(a, r)
      }(window, document, 'script', ga_script, 'ga');
      $window.ga('create', data['ga-account']);

      // loop through missed trackings, this temporarily fixes
      // the loading of our first pageview tracking.
      if ($rootScope.gaCache && $rootScope.gaCache.length > 0) {
        $rootScope.gaCache.forEach(function (item) {
          $window.ga(item.action, item.id, item.data);
        });
        $rootScope.gaCache = [];
      }
    };

    let skipGoogleAnalytics = !!window.__karma__;

    if(!skipGoogleAnalytics) {
      // load env vars
      ConfigurationService.getConfig()
        .toPromise()
        .then(function (resp) {
          var data = resp;
          $log.debug('Environment variables:', JSON.stringify(data, null, 4));
          $rootScope.envVars = data;
          let isDevEnv = _.some(['local', 'Dev'], env => env === data['env-name']);
          if (!isDevEnv) {
            initGoogleAnalytics(data);
          }
        });
    }

    //TODO move to UserService
    const initPermissions = () => {
      PermPermissionStore.clearStore();
      NgxPermissionsService.flushPermissions();

      let permissions = UserService.currentUser().permissions;
      PermPermissionStore.defineManyPermissions(permissions, function (permission) {
        let resolved = _.includes(permissions, permission);
        $log.debug('permission ' + (resolved ? 'found' : 'unknown') + ':', permission);
        return resolved;
      });
      NgxPermissionsService.loadPermissions(permissions);
      $log.debug('Permissions set:', PermPermissionStore.getStore());
    }
    //TODO move to UserService
    // user permissions
    if (UserService.currentUser().loggedOn) {
      initPermissions();
    }
    //TODO move to UserService
   UserService.onLogin(user => {
      initPermissions();
      $rootScope.redirectURL = null;
   });

    //TODO move to UserService
    UserService.onLogout(() => {
      PermPermissionStore.clearStore();
      NgxPermissionsService.flushPermissions();
      $log.debug('Permissions cleared:', PermPermissionStore.getStore());
      SessionService.clear()
    });

    // jscs:enable requireCamelCaseOrUpperCaseIdentifiers
    // jscs:enable requireParenthesesAroundIIFE
  }])

  .run(['$window', '$rootScope', '$state', '$stateParams', '$location', 'UserService', '$log', 'MetadataService', '$anchorScroll', 'TitleService', 'NavigationService', function($window, $rootScope, $state, $stateParams, $location, UserService, $log, MetadataService, $anchorScroll, TitleService, NavigationService) {
    $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
      //TODO use flag on the state inside router instead
      let publicPages = [
        'home',
        'request-password-reset',
        'registration',
        'registration-type',
        'password-reset',
        'password-expired',
        'outputs-configuration',
        'organisation.registration-programme',
        'organisation.registration-form',
        'organisation.registration-user',
        'confirm-user-created',
        'confirm-org-and-user-created'
      ];

      if(toState.name == 'project-block'){
        toParams.blockAccessByUrl = true;
      }

      if(!UserService.currentUser().loggedOn && publicPages.indexOf(toState.name) === -1) {
        $log.log('$stateChangeStart, usr not logged in and page is not white listed as public => returning to home');
        event.preventDefault();
        $rootScope.redirectURL = $location.url();
        $state.transitionTo('home', {redirectURL: $rootScope.redirectURL}).then(() => $rootScope.showGlobalLoadingMask = false);
      }

      /**
       * Show loading mask during state transition for resolved data
       */
      if (toState.resolve) {
        $rootScope.showGlobalLoadingMask = true;
      }
    });

    $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
      NavigationService.setCurrentState($state);
      $window.scrollTo(0, 0);
      //Makes keyboard navigation to start from the page top
      $('body').attr('tabIndex', -1).focus();

      if(!$window.ga) {
        $rootScope.gaCache = $rootScope.gaCache || [];
        $rootScope.gaCache.push({
          action: 'send',
          id: 'pageview',
          data: {page: $location.path()}
        });
      } else {
        $window.ga('send', 'pageview', {page: $location.path()});
      }

      let pageTitle = toState.pageTitle;
      if(!pageTitle && toState.resolve.pageTitle){
        pageTitle = $state.$current.locals.globals.pageTitle;
      }
      pageTitle = pageTitle? `${pageTitle} - ${WEB_APP_NAME}` : WEB_APP_NAME
      TitleService.setTitle(pageTitle);

      MetadataService.fireMetadataUpdate();

      $rootScope.showGlobalLoadingMask = false;
    });

    $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error){
      window.console.error(error);
    });

    //Listens for ng9 navigating with ng1 ui-router states
    NavigationService.addStateChangeListener((stateName, stateParams, options)=>{
      $state.go(stateName, stateParams, options);
    })

    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;

    var origin = window.location.origin || '';

    $rootScope.devMode =
      origin.search('localhost') >= 0 ||
      origin.search('10.0.0.2') >= 0 ||
      origin.search('0.0.0.0') >= 0 ||
      origin.search('ops-dev.london.gov.uk') >= 0 ||
      origin.search('ops-qas.london.gov.uk') >= 0;



    // Override console.log in any environment apart from dev
    if(!$rootScope.devMode) {
      var console = {
        info: () => {},
        log: () => {},
        debug: () => {},
        warn: () => {},
        error: () => {}
      };
      window.console = console;
    }

    $rootScope.jumpTo = (id) => {
      let initialYOffset = $anchorScroll.yOffset;
      $anchorScroll.yOffset = 60;
      $anchorScroll(id);
      $anchorScroll.yOffset = initialYOffset
      $(`#${id}`).focus();
    }
  }])

  //Fix after migrating to angular 1.6.10
  .config(['$provide', function ($provide) {
    $provide.decorator('$exceptionHandler', ['$delegate', '$injector', function ($delegate, $injector) {
      return function (exception, cause) {
        let exceptionsToIgnore = ['Possibly unhandled rejection: backdrop click', 'Possibly unhandled rejection: cancel', 'Possibly unhandled rejection: escape key press']
        if (exceptionsToIgnore.indexOf(exception) >= 0) {
          return;
        }
        $delegate(exception, cause);
      };
    }]);
  }])

  .constant('config', {
    basePath: '/api/v1'
  })

  .constant('_', _)

  .config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
  }])

  .run(['UserService', '$rootScope', 'MetadataService', 'LoadingMaskService', function (UserService, $rootScope, MetadataService, LoadingMaskService) {
    LoadingMaskService.subscribe(isLoadingMaskVisible => {
      $rootScope.showGlobalLoadingMask = isLoadingMaskVisible;
    });
  }]);
