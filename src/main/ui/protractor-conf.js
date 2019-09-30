/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const config = require('./test/testConfig.js');
const utils = require('./test/utils.js');
const protractorLocators = require('./test/protractorLocators.js');
const mkdirp = require('mkdirp');

const cucumberReportDir = `${__dirname}/../../../target/ui`;

mkdirp.sync(cucumberReportDir);

exports.config = {
  baseUrl: config.baseURL,
  allScriptsTimeout: 20000,
  getPageTimeout: 20000,
  framework: 'custom',
  frameworkPath: require.resolve('protractor-cucumber-framework'),
  suites: {
    projects: [
      'test/features/projects.feature',
      'test/features/projects-search.feature',
      'test/features/projects-bulk-operations.feature',
      'test/features/projects-filter.feature'
    ],
    project: [
      'test/features/project-skills.feature',
      'test/features/learning-grant-block.feature',
      'test/features/learning-grant-block-claim.feature',
      'test/features/project-outputs-wizard-config.feature',
      'test/features/project-outputs-wizard-input.feature',
      'test/features/project-outputs-table.feature',
      'test/features/project-outputs-summary-table.feature',
      'test/features/project-outputs-baseline.feature',
      'test/features/project-outputs-assumption.feature',
      'test/features/project-outputs-costs.feature',
      'test/features/project-outputs-claim.feature',
      'test/features/project-overview.feature',
      'test/features/project-details.feature',
      'test/features/project-details-history.feature',
      'test/features/project-funding.feature',
      'test/features/project-funding-claims.feature',
      'test/features/project-milestones.feature',
      'test/features/project-milestones-update.feature',
      'test/features/project-milestones-approve-claim.feature',
      'test/features/project-milestones-claim-monetary.feature',
      'test/features/project-milestones-conditional.feature',
      'test/features/project-milestones-bespoke.feature',
      'test/features/project-milestones-history.feature',
      'test/features/project-milestones-cancel-claim.feature',
      'test/features/project-milestones-evidence.feature',
      'test/features/project-milestones-payments-history.feature',
      'test/features/project-milestones-not-applicable.feature',
      'test/features/project-milestones-reclaim.feature',
      'test/features/project-design-standards.feature',
      'test/features/project-design-standars-history.feature',
      'test/features/project-grant-source.feature',
      'test/features/project-grant-source-history.feature',
      'test/features/project-calculate-grant.feature',
      'test/features/project-calcualte-grant-history.feature',
      'test/features/project-negotiated-grant.feature',
      'test/features/project-negotiated-grant-history.feature',
      'test/features/project-developer-led-grant.feature',
      'test/features/project-developer-led-grant-history.feature',
      'test/features/project-indicative-grant.feature',
      'test/features/project-indicative-grant-history.feature',
      'test/features/project-questions.feature',
      'test/features/project-questions-history.feature',
      'test/features/project-questions-file-upload.feature',
      'test/features/project-submit.feature',
      'test/features/project-budget-total-spend.feature',
      'test/features/project-budget-annual-spend.feature',
      'test/features/project-budget.feature',
      'test/features/project-receipts.feature',
      'test/features/project-recommend.feature',
      'test/features/project-return.feature',
      'test/features/project-approve.feature',
      'test/features/project-abandon.feature',
      'test/features/project-abandon-approve-reject.feature',
      'test/features/project-transfer.feature',
      'test/features/project-complete.feature',
      'test/features/project-reject.feature',
      'test/features/project-reject-menu.feature',
      'test/features/closed-programme.feature',
      'test/features/project-request-approval.feature',
      'test/features/project-return-from-approval-requested.feature',
      'test/features/project-unapproved-block-deletion.feature',
      'test/features/project-new-block.feature',
      'test/features/project-unit-details.feature',
      'test/features/project-unit-wizard.feature',
      'test/features/project-approval-active.feature',
      'test/features/project-save-to-active.feature',
      'test/features/project-land-version-control.feature',
      'test/features/project-risks.feature',
      'test/features/project-new.feature',
      'test/features/project-internal-block-risk.feature',
      'test/features/project-multi-assessment.feature',
      'test/features/project-labels.feature',
      'test/features/project-subcontracting.feature',
      'test/features/funding-claims-block.feature',

    ],
    programmes: [
      'test/features/programmes.feature',
      'test/features/programmes-filter.feature'
    ],
    changeReports: [
      'test/features/change-report.feature',
      'test/features/change-report-details.feature',
      'test/features/change-report-additional-questions.feature',
      'test/features/change-report-design-standards.feature',
      'test/features/change-report-developer-led-grant.feature',
      'test/features/change-report-milestones.feature',
      'test/features/change-report-calculate-grant.feature',
      'test/features/change-report-negotiated-grant.feature',
      'test/features/change-report-indicative-grant.feature',
      'test/features/change-report-grant-source.feature',
      'test/features/change-report-units.feature',
      'test/features/change-report-risks-and-issues.feature',
      'test/features/change-report-outputs.feature',
      'test/features/change-report-budgets.feature',
      'test/features/change-report-receipts.feature',
      'test/features/change-report-funding.feature',
      'test/features/change-report-learning-grant.feature',
      'test/features/change-report-outputs-cost.feature',
      'test/features/reporting.feature',
      'test/features/reports-page.feature',
    ],
    summaryReports: [
      'test/features/summary-report.feature',
      'test/features/summary-report-details.feature',
      'test/features/summary-report-additional-questions.feature',
      'test/features/summary-report-design-standards.feature',
      'test/features/summary-report-outputs.feature',
      'test/features/summary-report-risks-and-issues.feature',
      'test/features/summary-report-budgets.feature',
      'test/features/summary-report-milestones.feature',
      'test/features/summary-report-calculate-grant.feature',
      'test/features/summary-report-developer-led-grant.feature',
      'test/features/summary-report-negotiated-grant.feature',
      'test/features/summary-report-indicative-grant.feature',
      'test/features/summary-report-funding.feature',
      'test/features/summary-report-learning-grant.feature',
      'test/features/summary-report-ouputs-cost.feature'
    ],
    payments: [
      'test/features/pending-payments.feature',
      'test/features/all-payments-filter.feature',
      'test/features/all-authorised-payments.feature',
      'test/features/all-payment-declined.feature',
      'test/features/all-payment-summary.feature',
      'test/features/project-request-payment-authorisation.feature',
      'test/features/all-payment-authorisation.feature',
      'test/features/payments-reclaim.feature'
    ],
    organisations: [
      'test/features/organisations.feature',
      'test/features/organisations-search.feature',
      'test/features/organisations-new.feature',
      'test/features/organisations-request-access.feature',
      'test/features/organisation.feature',
      'test/features/organisation-contracts.feature',
      'test/features/organisation-edit.feature',
      'test/features/organisation-profile.feature',
      'test/features/organisation-programmes.feature',
      'test/features/organisation-recoverable-grant.feature',
      'test/features/organisation-recoverable-grant-submission.feature',
      'test/features/organisations-managing.feature',
      'test/features/organisation-registration.feature',
      'test/features/organisation-reject.feature',
      'test/features/organisation-inactive.feature',
      'test/features/organisation-teams.feature',
      'test/features/consortiums.feature'
    ],
    user: [
      'test/features/user-registration.feature',
      'test/features/user-authentication.feature',
      'test/features/password-reset.feature',
      'test/features/user-roles.feature',

      'test/features/users-list.feature',
      'test/features/user-account.feature',
      'test/features/user-account-spend-threshold.feature'
    ],
    general: [
      'test/features/framework.feature',
      'test/features/sticky-nav.feature',
      'test/features/top-menu.feature',
      'test/features/cookie-warning.feature',
      // 'test/features/server-errors.feature',
      'test/features/financial-year.feature',
      'test/features/swagger-api-doc.feature',
      'test/features/system-dashboard.feature',
      'test/features/system-templates-questions.feature',
      'test/features/system-templates.feature',
      'test/features/audit-history.feature',
      'test/features/assessments.feature',
      'test/features/permissions.feature',
      'test/features/labels-management.feature',
      'test/features/all-notifications.feature',
      'test/features/project-overrides.feature',
      'test/features/outputs-configuration.feature'
    ],
    notifications: [
      'test/features/notifications.feature'
    ],
    admin: [
      'test/features/finance-categories.feature',
      'test/features/system-messages.feature',
      'test/features/system-features.feature',
      'test/features/skill-profiles.feature'
    ]
  },

  resultJsonOutputFile: `${cucumberReportDir}/cucumber_report.json`,
  capabilities: {
    // browserName: 'phantomjs',
    browserName: 'chrome',
    acceptInsecureCerts : true,
    //browserName: 'firefox',

    shardTestFiles: true, // tests are split between instances
    maxInstances: process.env.E2E_MAX_INSTANCES || 6, // maximum number of instances it can spawn

    chromeOptions: {
      binary: process.env.CHROME_BIN,
      args: ['--headless', '--disable-gpu'],
      prefs: {
        'credentials_enable_service': false,
        'profile': {
          'password_manager_enabled': false
        }
      }
    },

    platform: 'ANY'
  },

  directConnect: true,
  cucumberOpts: {
    require: 'test/features/steps/**/*.js',
    format: 'pretty',
    tags: config.cucumberFilterTags()
  },

  onPrepare() {
    protractorLocators(protractor);

    browser.driver.manage().window()
      .setSize(config.browserSize.lg.width, config.browserSize.lg.height);

    // Initialise some page to be able to run browser.executeScript
    browser.getCapabilities()
      .then(c => {
        browser.browserName = c.get('browserName');
    });

    browser.get('/');
    browser.stats = {};

   // Debugging performance of TC
   /* var defer = protractor.promise.defer();

    utils.jsPerformance().then(()=>{
      defer.fulfill();
    });

    return defer.promise;*/
  },

  onComplete(){
    // Must run in single instance. Uncomment step hooks in cucumber-env.js before running
    // utils.cucumberTestsStatistics(browser.stats);
  }
};
