/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


describe('Directive: gla-header', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element;

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    element = $compile('<gla-header></gla-header>')($scope);
  }));


  it('should add env class to the header', () => {
    $rootScope.envVars = {
      'env-name': 'Dev'
    };
    $scope.$digest();
    expect(element.find('.gla-ops.Dev').length).toBe(1);
  });

});
