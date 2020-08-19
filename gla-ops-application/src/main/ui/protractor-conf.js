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
const ngStorageMock = require('ngstorage-mock');


const cucumberReportDir = `${__dirname}/../../../target/ui`;

mkdirp.sync(cucumberReportDir);

exports.config = {
  baseUrl: config.baseURL,
  allScriptsTimeout: 30000,
  getPageTimeout: 30000,
  ng12Hybrid: true,
  rootElement: 'body',
  framework: 'custom',
  frameworkPath: require.resolve('protractor-cucumber-framework'),
  suites: {
    general: [
      'test/features/general/*.feature',
    ],
    navigation: [
      'test/features/navigation/*.feature',
    ],
    changeReports: [
      'test/features/changereports/*.feature',
    ],
    programmes: [
      'test/features/programmes/*.feature',
    ],
    project: [
      'test/features/project/**/*.feature',
    ],
    projects: [
      'test/features/projects/*.feature',
    ],
    assessments: [
      'test/features/assessments/*.feature',
    ],
    organisation: [
      'test/features/organisation/*.feature',
    ],
    summaryReports: [
      'test/features/summaryreports/*.feature',
    ],
    user: [
      'test/features/user/*.feature',
    ],
    notifications: [
      'test/features/notifications/*.feature',
    ],
    admin: [
      'test/features/admin/*.feature',
    ],
    payments: [
      'test/features/payments/*.feature',
    ],
    annualSubmission: [
      'test/features/annualsubmission/*.feature',
    ]

  },
  resultJsonOutputFile: `${cucumberReportDir}/cucumber_report.json`,
  capabilities: {
    // browserName: 'phantomjs',
    browserName: 'chrome',
    acceptInsecureCerts: true,
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
    //Fix for ngStorage to work with protractor when used with ng-upgrade
    browser.addMockModule('ngStorage', ngStorageMock);


    browser.addMockModule('angular-cache', function() {
      angular.module('angular-cache', []).factory('CacheFactory', function() {
        function CacheFactory(){}
        CacheFactory.get = function(){};
        return CacheFactory
      })
    });

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

  onComplete() {
    // Must run in single instance. Uncomment step hooks in cucumber-env.js before running
    // utils.cucumberTestsStatistics(browser.stats);
  }
};
