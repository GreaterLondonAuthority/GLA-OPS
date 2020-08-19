/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


xdescribe('Controller: OrganisationsCtrl', function () {

  // load the controller's module
  beforeEach(angular.mock.module('GLA'));

  var ctrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ctrl = $controller('OrganisationsCtrl', {
      $scope: scope,
      organisationTypes: {'1': 'Type 1'}
    });
  }));

  it('should display pagination when there are more items than 50', function () {
    scope.totalItems = 100;
    scope.itemsPerPage = 50;
    expect(scope.displayPagination()).toBe(true);
  });

  it('should not display pagination when there are less than 50', function () {
    scope.totalItems = 49;
    scope.itemsPerPage = 50;
    expect(scope.displayPagination()).toBe(false);
  });
});
