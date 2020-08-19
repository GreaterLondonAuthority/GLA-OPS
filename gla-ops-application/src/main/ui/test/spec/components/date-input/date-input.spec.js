/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../utils');

describe('Component: date-input', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, $timeout;

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $timeout = $injector.get('$timeout');
    $scope = $rootScope.$new();
  }));

  it('should set view value when model is valid', () => {
    $scope.dateValue = '2011-01-31';
    createElement($scope);
    expect(viewValue()).toBe('31/01/2011');
  });

  it('should update the model when ui value changes', () => {
    $scope.dateValue = '2011-01-31';
    createElement($scope);
    setInputDate('day', 20);
    setInputDate('month', 10);
    setInputDate('year', 2015);
    expect($scope.dateValue).toBe('2015-10-20');
  });

  it('should not set view value when model is invalid', () => {
    $scope.dateValue = '2011-15-31';
    createElement($scope);
    expect(viewValue()).toBe('//');
  });

  function createElement($scope) {
    let template = `<date-input ng-model="dateValue"></date-input>`;
    element = $compile(template)($scope);
    $scope.$digest();
    return element;
  }

  function inputDate(dayMonthOrYear) {
    return element.find(`.dateInput__${dayMonthOrYear}`);
  }

  function setInputDate(dayMonthOrYear, value){
    inputDate(dayMonthOrYear).val(value);
    inputDate(dayMonthOrYear).trigger('change');
    $scope.$digest();
  }


  function viewValue() {
    return element.find('input').map((index, item)=> $(item).val()).get().join('/');
  }

});

