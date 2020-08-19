/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Component: cookie-warning', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, $window, $localStorage;

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $window = $injector.get('$window');
    UserService = $injector.get('UserService');
    $localStorage = $injector.get('$localStorage');
    $scope = $rootScope.$new();
    $localStorage.$reset();
    element = createElement();
  }));


  it('should be visible first time', () => {
    expect(isWarningVisible()).toBe(true);
  });

  it('should not be visible after close', () => {
    closeWarning();
    expect(isWarningVisible()).toBe(false);
  });

  it('should not be visible after rendering after close', () => {
    closeWarning();
    createElement();
    expect(isWarningVisible()).toBe(false);
  });

  function createElement(){
    element = $compile('<gla-cookie-warning></gla-cookie-warning>')($scope);
    $scope.$digest();
    return element;
  }

  function isWarningVisible(){
    return element.find('.gla-cookie-warning').length === 1;
  }

  function closeWarning(){
    return element.find('.close-button').click();
  }
});

