/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

module.exports = class TeamCityReporter {
  constructor(cucumber) {
    cucumber.Before(scenario => {
      if (this.isIgnored(scenario)) {
        this.testIgnored(scenario);
        return 'pending';
      } else {
        this.testStarted(scenario);
      }
    });


    cucumber.After(scenario => {
      if (scenario.isFailed() || scenario.isUndefined()) {
        this.testFailed(scenario);
      }

      if (!scenario.isPending()) {
        this.testFinished(scenario);
      }
    });
  }

  isIgnored(scenario) {
    return (scenario.getTags() || []).some(tag => tag.getName() === '@ignore');
  }

  escape(text) {
    return (text || '')
      .replace(/([|'"])/g, '|$1')
      .replace(/\[/g, '|[')
      .replace(/]/g, '|]')
      .replace(/\n/g, '|n')
      .replace(/\r/g, '|r');
  }

  testStarted(scenario) {
    const scenarioName = this.escape(scenario.getName());
    console.log(`##teamcity[testStarted name='${scenarioName}' captureStandardOutput='true']`);
  }

  testFinished(scenario) {
    const scenarioName = this.escape(scenario.getName());
    console.log(`##teamcity[testFinished name='${scenarioName}']`);
  }

  testIgnored(scenario) {
    const scenarioName = this.escape(scenario.getName());
    console.log(`##teamcity[testIgnored name='${scenarioName}' message='Skipped with @ignore']`);
  }

  testFailed(scenario) {
    const scenarioName = this.escape(scenario.getName());
    const error = scenario.getException() || {};
    const message = this.escape(error.message);
    let details = this.escape(error.stack);
    console.log(`##teamcity[testFailed name='${scenarioName}' message='${message}' details='${details}']`);
  }
};
