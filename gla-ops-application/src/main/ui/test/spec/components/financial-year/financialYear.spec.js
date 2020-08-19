/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../utils');

describe('Component: financial-year', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, currentFinancialYear, $timeout;

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $timeout = $injector.get('$timeout');
    $scope = $rootScope.$new();
    currentFinancialYear = utils.getCurrentFinancialYear().split('/')[0];
  }));

  it('should set the default date to current financial year', () => {
    let element = createElement($scope);
    let $ctrl = element.isolateScope().$ctrl;
    expect($ctrl.currentFinancialYear).toBe(currentFinancialYear*1);
  });

  it("shouldn't change specified financial year", () => {
    $scope.selectedYear = {financialYear:2019};
    let element = createElement($scope);
    let $ctrl = element.isolateScope().$ctrl;
    expect($ctrl.selectedYear.financialYear).toBe(2019);
  });

  it('should generated dropdown values based on back and forward properties', () => {
    $scope.back = 1;
    $scope.forward = 2;
    let element = createElement($scope);
    let $ctrl = element.isolateScope().$ctrl;
    let options = dropdownOptions();

    expect(options.length).toBe($scope.back + 1 + $scope.forward);

    expect(options.indexOf(currentFinancialYear+'/'+(currentFinancialYear*1+1)%100)).toBe($scope.back);
  });

  // it('should call on-select when selected', () => {
  //   // $scope.financialYear = {financialYear:2030};
  //   // $scope.onYearSelect = jasmine.createSpy('onYearSelect');
  //
  //   let element = createElement($scope);
  //   let $ctrl = element.isolateScope().$ctrl;
  //   selectOption('2014/15');
  //   $timeout.flush();
  //   console.log('1234567')
  //   expect($scope.onYearSelect).toHaveBeenCalledWith('2014/15');
  // });

  it('should overrived back and/or forward when to and/or from are selected', ()=> {
    $scope.back = 1;
    $scope.forward = 2;
    $scope.from = 2010;
    $scope.to = 2020;
    let element = createElement($scope);
    let $ctrl = element.isolateScope().$ctrl;
    let options = dropdownOptions();
    expect(options.length).toBe(1 + $scope.to - $scope.from);
  });

  function createElement($scope) {
    let template = `<financial-year ng-model="financialYear"
                                    back="back"
                                    forward="forward"
                                    from="from"
                                    to="to"
                                    on-select="onYearSelect(financialYear)"
                                    selected-year="selectedYear"
                                    select-id="currentYear"
                                    populated-years="populatedYears"></financial-year>`;

    element = $compile(template)($scope);
    $scope.$digest();
    return element;
  }

  it('should should check marks next to years that have data', ()=> {
    $scope.from = 2010;
    $scope.to = 2020;
    $scope.populatedYears = [2015, 2017];
    let element = createElement($scope);
    element.find('.ui-select-toggle').click();

    expect(element.find('.ui-select-choices-row .glyphicon-ok').length).toBe(2);
  });

  function dropdownOptions() {
    element.find('.ui-select-toggle').click();
    return element.find('.ui-select-choices-row').map((index, item)=> $(item).text().trim()).get();
  }


  // function selectOption(selected) {
  //   element.find('.ui-select-toggle').click();
  //   element.find(`.ui-select-choices-row:contains('${selected}')`).click();
  // }

});
