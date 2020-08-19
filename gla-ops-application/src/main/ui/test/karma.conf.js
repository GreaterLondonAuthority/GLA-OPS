/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

// Karma configuration
// Generated on 2016-07-28
require('dotenv').config({silent: true});
console.log('chrome path:', process.env.CHROME_BIN);
module.exports = function(config) {
  'use strict';

  config.set({
    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // base path, that will be used to resolve files and exclude
    basePath: '../',

    // testing framework to use (jasmine/mocha/qunit/...)
    // as well as any additional frameworks (requirejs/chai/sinon/...)
    frameworks: [
      'browserify', 'jasmine'
    ],

    // list of files / patterns to load in the browser
    files: [
      // bower:js
      'bower_components/jquery/dist/jquery.js',
      'bower_components/angular/angular.js',
      'bower_components/bootstrap-sass-official/assets/javascripts/bootstrap.js',
      'bower_components/angular-animate/angular-animate.js',
      'bower_components/angular-cookies/angular-cookies.js',
      'bower_components/angular-messages/angular-messages.js',
      'bower_components/angular-resource/angular-resource.js',
      'bower_components/angular-route/angular-route.js',
      'bower_components/angular-sanitize/angular-sanitize.js',
      'bower_components/angular-touch/angular-touch.js',
      'bower_components/angular-aria/angular-aria.js',
      'bower_components/angular-i18n/angular-locale_en-gb.js',
      'bower_components/angular-ui-router/release/angular-ui-router.js',
      'bower_components/lodash/lodash.js',
      'bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
      'bower_components/angular-validation-match/dist/angular-validation-match.min.js',
      'bower_components/ngstorage/ngStorage.js',
      'bower_components/moment/moment.js',
      'bower_components/angular-moment/angular-moment.js',
      'bower_components/angular-toastr/dist/angular-toastr.tpls.js',
      'bower_components/angular-permission/dist/angular-permission.js',
      'bower_components/angular-permission/dist/angular-permission-ui.js',
      'bower_components/angular-permission/dist/angular-permission-ng.js',
      'bower_components/angular-vertilize/angular-vertilize.js',
      'bower_components/re-tree/re-tree.js',
      'bower_components/ng-device-detector/ng-device-detector.js',
      'bower_components/jquery-ui/jquery-ui.js',
      'bower_components/angular-clipboard/angular-clipboard.js',
      'bower_components/jsoneditor/dist/jsoneditor.js',
      'bower_components/ng-jsoneditor/ng-jsoneditor.js',
      'bower_components/ng-idle/angular-idle.js',
      'bower_components/angular-ui-sortable/sortable.js',
      'bower_components/angular-cache/dist/angular-cache.js',
      'bower_components/chart.js/dist/Chart.js',
      'bower_components/angular-chart.js/dist/angular-chart.js',
      'bower_components/angular-mocks/angular-mocks.js',
      // endbower
      'app/scripts/glaModule.js',
      '.tmp/scripts/templateCache.js',
      'app/scripts/**/*.js',
      'test/mock/**/*.js',
      'test/spec/**/*.js'
    ],

    preprocessors: {
      'app/scripts/**/*.js': ['browserify'],
      'test/spec/**/*.js': ['browserify']
    },

    reporters: ['spec', 'coverage'],

    specReporter: {
      suppressSkipped: true,  // do not print information about skipped tests
    },

    // list of files / patterns to exclude
    exclude: [],

    // web server port
    // port: 8080,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: [
      // 'Chrome'
      'ChromeHeadless'
    ],

    customLaunchers: {
      ChromeHeadless: {
        base: 'Chrome',
        flags: ['--headless', '--disable-gpu', '--remote-debugging-port=9222']
      }
    },

    // Which plugins to enable
    plugins: [
      'karma-chrome-launcher',
      'karma-jasmine',
      'karma-spec-reporter',
      'karma-coverage',
      'karma-browserify',
      // 'karma-babel-preprocessor',
      // 'babel-preset-es2015'
    ],

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,

    colors: true,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,

    //Uncomment the following lines if you are using grunt's server to run the tests
    proxies: {
      '/api': 'http://gladevapp.cepwipbmwf.eu-west-1.elasticbeanstalk.com',
      '/sysops': 'http://gladevapp.cepwipbmwf.eu-west-1.elasticbeanstalk.com',
    },
    //URL root prevent conflicts with the site root
    //,urlRoot: '_karma_'

    coverageReporter: {
      type: 'html',
      dir: '../../../target/ui/coverage/',
      subdir: '.'
    },

    browserify: {
      debug: true,
      transform: [
        ['browserify-istanbul', {
          instrumenter: require('isparta'),
          instrumenterConfig: {
            embedSource: true
          }
        }],
        ['babelify']
      ]
    }
  });
};
