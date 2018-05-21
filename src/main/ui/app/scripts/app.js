/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
//--- TODO move dependencies to relevant place where it is actually used ---
import './services/ConfigurationService';
import './appCtrl';
import './routes/routes';

import './util/ToastrUtil';

import './services/ModalDisplayService';
import './services/SESSION';
import './services/FeatureToggleService';
import './services/OrganisationService';
import './services/OrganisationGroupService';
import './services/ProjectService';
import './services/SessionService';
import './services/PaymentService';
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
import './services/ReferenceDataService';
import './services/ReportService';
import './services/blocks/ProjectDetailsService';
import './services/NotificationsService';
import './services/ActuatorService';
import './util/Util';

import './directives/cookie-warning/gla-cookie-warning.directive';
import './directives/header/gla-header.directive';
import './directives/topmenu/topmenu.directive';
import './directives/dropdown/dropdown.directive';
import './directives/permissionUtil/setPermission';

import './components/page-header/pageHeader';
import './components/tenure-tiles/tenureTiles';
import './components/footer/glaFooter';
import './components/header-status/headerStatus';
import './components/project-header/projectHeader';
import './components/project-id/projectId';
import './components/admin/content/coming-soon-admin.ctrl';
import './components/project-history/projectHistory';
import './components/input-cost-budget/inputCostBudget';
import './components/grant-block/grantBlock';
import './components/password-strength/passwordStrength';
import './components/messageModal/messageModal';
import './components/fileUpload/fileUpload';
import './components/forecast-change/forecastChange';
import './components/financial-year/financialYear.js';
import './components/section-header/section-header.js';
import './components/mobileDeviceWarning/modal.js';
import './components/version-history-modal/versionHistoryModal';
import './components/profiled-unit-wizard/profiledUnitWizard';
import './components/number-mask/numberMask';
import './components/abs/absFilter.js';
// import './components/projectBulkWarning/modal.js';
import './components/well/well.js';
import './components/multi-panel/multiPanel.js';
import './components/month-selector/monthSelector.js';
import './components/quarter-selector/quarterSelector.js';
import './components/remaining-characters/remainingCharacters.js';
import './components/search-field/searchField.js';
import './components/checkbox-filter/checkboxFilter.js';
import './components/pagination/pagination.js';
import './components/actuals-metadata-modal/actualsMetadataModal.js';

import './pages/project/project-budget/fileDeleteConfirmationModal';
import './pages/project/project-budget/fileUploadErrorModal';

import './pages/home/homeCtrl';
import './pages/registration/registrationCtrl';
import './pages/reset-password/requestPasswordResetCtrl';
import './pages/reset-password/passwordResetCtrl';
import './pages/user/home/userHomeCtrl';
import './pages/organisations/organisationsCtrl';
import './pages/organisation/organisationCtrl';
import './pages/organisation/organisationForm/editOrganisationCtrl';
import './pages/organisation/organisationForm/newOrganisationCtrl';
import './pages/organisation/organisationForm/newOrganisationProfileCtrl';
import './pages/organisation/contracts/contractsList';
import './pages/organisation/programme/organisationProgramme';
import './pages/organisation/programme/create-delegated/modal';
import './pages/organisation/programmes/programmesList';
import './pages/programmes/programmesCtrl';
import './pages/programme/programmeCtrl';
import './pages/reports/reportsCtrl';
import './pages/system/systemCtrl';
import './pages/change-report/changeReportCtrl';
import './pages/user-account/userAccountCtrl';

import './pages/change-report/change-report-field/changeReportField';
import './pages/change-report/change-report-field-files/changeReportFieldFiles';
import './pages/change-report/change-report-field-lookup/changeReportFieldLookup';
import './pages/change-report/change-report-coordinates/changeReportCoordinates';
import './pages/change-report/change-report-table/changeReportTable';
import './pages/change-report/change-report-table-row/changeReportTableRow';
import './pages/change-report/change-report-tile-total/changeReportTileTotal';
import './pages/change-report/change-report-tiles/changeReportTiles';
import './pages/change-report/change-report-tiles/change-report-tile-row/changeReportTileRow';
import './pages/change-report/change-report-table-separator/changeReportTableSeparator';
import './pages/change-report/change-report-static-text/changeReportStaticText';

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

import './pages/notifications/notifications-page';
import './pages/notifications/notifications-group';
import './pages/notifications/notification';
import './pages/notifications/deleteNotification/modal';

import './pages/payments/pending-payments/pendingPaymentsCtrl';
import './pages/payments/all-payments/allPaymentsCtrl';
import './pages/payments/payment-summary/paymentSummaryCtrl';
import './pages/payments/pending-payments/decline-dialog/declineDialog';
import './pages/payments/pending-payments/interest-dialog/interestDialog';
import './pages/projects/projectsCtrl';
import './pages/projects/new/newProjectCtrl';
import './pages/consortiums/consortiumsCtrl';
import './pages/consortiums/new/newConsortiumCtrl';
import './pages/consortiums/edit/editConsortiumCtrl';
import './pages/project/design-standards/designStandardsCtrl';
import './pages/project/grant-source/grantSourceCtrl';
import './pages/project/details/projectDetailsCtrl';
import './pages/project/milestones/projectMilestonesCtrl';
import './pages/project/milestones/projectMilestonesModal';
import './pages/project/milestones/claimMilestoneModal/modal';
import './pages/project/milestones/reclaimMilestoneModal/modal';
// import './pages/project/milestones/claimMilestonesModal/modal';
import './pages/project/project-budget/projectBudgetCtrl';
import './pages/project/project-budget/financeSummary';
import './pages/project/project-budget/forecast/projectBudgetForecast';
import './pages/project/project-budget/forecast/forecastMonthRow';

import './pages/project/questions/questionsCtrl';
import './pages/project/questions/questionFileUpload.js';
import './pages/project/overview/projectOverviewCtrl';
import './pages/project/overview/abandonModal/modal';
import './pages/project/overview/transferModal/transferModal';
import './pages/project/grant/calculate-grant/calculateGrantCtrl';
import './pages/project/grant/negotiated-grant/negotiatedGrantCtrl';
import './pages/project/grant/GrantService';
import './pages/project/grant/developer-led-grant/developerLedGrantCtrl';
import './pages/project/grant/indicative-grant/indicativeGrantCtrl';
import './pages/project/grant/grant-table/grantTable';
import './pages/project/grant/total-grant/totalGrant';
import './pages/project/outputs/outputsCtrl';
import './pages/project/outputs/forecast/forecastCategoryRow';
import './pages/project/receipts/receiptsCtrl';
import './pages/project/receipts/forecast/receiptsMonthRow';
import './pages/project/receipts/wizard/receiptWizard';
import './pages/project/risks/risksCtrl';
import './pages/project/risks/manage-project-risks/manageProjectRisk';
import './pages/project/risks/manage-project-issues/manageProjectIssues';
import './pages/project/risks/risk-and-issue-modal/modal';
import './pages/project/risks/add-risk-action-modal/modal';
import './pages/project/units/unitsCtrl';
import './pages/organisations/requestOrganisationAccessModal';
import './pages/users/users';

import './components/common/input/date-input/dateInput';
import './components/common/input/yes-no-input/yesNoInput';
import './components/common/modifiers/focus-me/focusMe';
import './components/common/modifiers/mouse-event-for-autosave/mouseEventForAutosave';
import './components/common/modifiers/numbers-only/numbersOnly';
import './components/common/modifiers/numbers-with-negative/numbersWithNegative';
import './components/common/spinner/spinner';
import './components/common/loading-mask/loadingMask';
import './components/common/confirmation-dialog/confirmationDialog';
import './components/common/project-overview-block/projectOverviewBlock';
import './components/total-box/totalBox';
import './components/delete-button/deleteButton';
import './components/chrome-autofill/chromeAutofill';
import './components/change-on-blur/change-on-blur';
import './components/compile/compile';
import './components/markdown/markdown';
import './components/default-value/defaultValue';
import './components/org-lookup/org-lookup';

import BootstrapUtil from './util/BootstrapUtil';

angular.module('GLA')
  .run(function($window, $rootScope, $state, $stateParams, $location, UserService, $log) {

    $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
      let publicPages = ['home', 'request-password-reset', 'registration', 'password-reset'];
      if(!UserService.currentUser().loggedOn && publicPages.indexOf(toState.name) === -1) {
        $log.log('$stateChangeStart, no login');
        event.preventDefault();
        $rootScope.redirectURL = $location.url();
        $state.transitionTo('home').then(() => $rootScope.showGlobalLoadingMask = false);
      }

      /**
       * Show loading mask during state transition for resolved data
       */
      if (toState.resolve) {
        $rootScope.showGlobalLoadingMask = true;
      }
    });

    $rootScope.$on('$stateChangeSuccess', function(event, toState) {
      $window.scrollTo(0, 0);
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
      $rootScope.showGlobalLoadingMask = false;
    });

    $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error){
      window.console.error(error);
    });

    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;

    var origin = window.location.origin || '';

    $rootScope.devMode =
      origin.search('localhost') >= 0 ||
      origin.search('10.0.0.2') >= 0 ||
      origin.search('0.0.0.0') >= 0 ||
      origin.search('') >= 0 ||
      origin.search('') >= 0;



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

    BootstrapUtil.enableUiSelectCaretClick();
  })

  .config(function (toastrConfig) {
    angular.extend(toastrConfig, {
      'closeButton': false,
      'debug': false,
      'newestOnTop': false,
      'progressBar': false,
      'positionClass': 'toast-top-center',
      'preventOpenDuplicates': true,
      'onclick': null,
      'showDuration': '5000',
      'hideDuration': '0',
      'timeOut': '5000',
      'extendedTimeOut': '5000',
      // 'showEasing': 'swing',
      // 'hideEasing': 'linear',
      // 'showMethod': 'fadeIn',
      // 'hideMethod': 'fadeOut',
      'templates': {
        'toast': 'scripts/components/toast/toast.html'
      }
    });
  })

  .config(function(uiSelectConfig) {
    uiSelectConfig.dropdownPosition = 'down';
  })

  .constant('config', {
    basePath: '/api/v1'
  })

  .constant('_', _);
