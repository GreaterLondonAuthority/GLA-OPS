/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

describe('Controller: UserHomeCtrl', function () {

  // load the controller's module
  beforeEach(angular.mock.module('GLA'));

  var ctrl,
    element,
    $scope,
    $rootScope,
    $compile;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($injector) {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();

    // scope = $rootScope.$new();
    // ctrl = $controller('UserHomeCtrl');
    element = $compile('<user-home-page></user-home-page>')($scope);
    $scope.$digest();
    ctrl = element.isolateScope().$ctrl;
  }));

  it('should have a user', function () {
    expect(ctrl.user).toBeDefined();
  });
});
