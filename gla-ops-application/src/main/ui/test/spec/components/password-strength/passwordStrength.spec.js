/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../utils');

describe('Component: password-strength', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService;

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    UserService = $injector.get('UserService');
    $scope = $rootScope.$new();

    $scope.password = 'abc';
    element = $compile("<password-strength password='password' is-valid='isValid'></password-strength>")($scope);
  }));

  it('should call api with correct password', () => {
    $scope.password = 'password1';
    spyOn(UserService, 'passwordStrength').and.returnValue(utils.mockPromise({data: 1}));
    $scope.$digest();
    expect(UserService.passwordStrength).toHaveBeenCalledWith('password1');
  });

  it('should be invalid when password is empty', () => {
    $scope.password = '';
    $scope.$digest();
    expect($scope.isValid).toBe(false);
    });

  it('should be invalid when password strength is 0', () => {
    spyOn(UserService, 'passwordStrength').and.returnValue(utils.mockPromise({data: 0}));
    $scope.$digest();
    expect($scope.isValid).toBe(false);
  });

  it('should be invalid when password strength is 1', () => {
    spyOn(UserService, 'passwordStrength').and.returnValue(utils.mockPromise({data: 1}));
    $scope.$digest();
    expect($scope.isValid).toBe(false);
  });

  it('should be invalid when password strength is 2', () => {
    spyOn(UserService, 'passwordStrength').and.returnValue(utils.mockPromise({data: 2}));
    $scope.$digest();
    expect($scope.isValid).toBe(true);
  });

  it('should show correct password status', () => {
    let apiResponses = [0, 1, 2, 3, 4];
    let expectedValues = ['weak', 'fair', 'good', 'strong', 'very strong'];

    apiResponses.forEach((response, index) => {
      fakeApiResponse(response, index);
      expect(passwordStatus()).toBe(expectedValues[index]);
    });
  });

  function passwordStatus() {
    return element.find('.password-status').text().toLowerCase();
  }

  function fakeApiResponse(apiResponse, index){
    $scope.password = `fake${index}`;
    UserService.passwordStrength = angular.noop;
    spyOn(UserService, 'passwordStrength').and.returnValue(utils.mockPromise({data: apiResponse}));
    $scope.$digest();
  }
});

