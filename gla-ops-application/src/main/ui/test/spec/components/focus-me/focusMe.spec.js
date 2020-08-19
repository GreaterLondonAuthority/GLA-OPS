/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Component: focus-me', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, rawElement, $timeout;

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $timeout = $injector.get('$timeout');
    $scope = $rootScope.$new();
  }));


  describe('focus-me parameter', () => {
    let el = null;
    beforeEach(() => {
      el = angular.element(`<ipnut focus-me="triggerFocus">`);
      spyOn(el[0],'focus');
      $compile(el)($scope);
    });

    it('should make input focused', () => {
      $scope.triggerFocus = true;
      $scope.$digest();
      expect(el[0].focus).toHaveBeenCalled();
      expect($scope.triggerFocus).toBe(false);
    });

    it('should not make input focused', () => {
      $scope.triggerFocus = false;
      $scope.$digest();
      expect(el[0].focus).not.toHaveBeenCalled();
    });
  });

  describe('focus-reset parameter', () => {
    it("should not reset if focus-reset is 'false'", () => {
      let el = angular.element(`<ipnut focus-me="triggerFocus" focus-reset="false">`);
      spyOn(el[0],'focus');
      $compile(el)($scope);
      $scope.triggerFocus = true;
      $scope.$digest();
      expect(el[0].focus).toHaveBeenCalled();
      expect($scope.triggerFocus).toBe(true);
    });
  });
});

