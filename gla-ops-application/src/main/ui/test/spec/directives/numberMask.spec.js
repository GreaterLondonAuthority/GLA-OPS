/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

describe('Directive: numberMask', () => {

  beforeEach(angular.mock.module('GLA'));

  let $window, $compile, $rootScope, $scope, $interval, element;

  beforeEach(inject(function(_$compile_, _$rootScope_, _$window_, _$interval_){
    $window = _$window_;
    $compile = _$compile_;
    $rootScope = _$rootScope_;
    $interval = _$interval_;
    $scope = $rootScope.$new();
  }));



  describe('format numbers', () => {
    it('should format positive number without decimals', () => {
      $scope.integerValue = 1234.56;
      element = $compile("<input number-mask='0' ng-model='integerValue''>")($scope);
      $scope.$digest();
      expect(element.val()).toBe('1,234');
    });

    it('should format negative number without decimals', () => {
      $scope.integerValue = -1234.56;
      element = $compile("<input number-mask='0' ng-model='integerValue''>")($scope);
      $scope.$digest();
      expect(element.val()).toBe('-1,234');
    });

    it('should format positive number with decimals', () => {
      $scope.integerValue = 1234.56;
      element = $compile("<input number-mask='2' ng-model='integerValue''>")($scope);
      $scope.$digest();
      expect(element.val()).toBe('1,234.56');
    });

    it('should format positive number with decimals', () => {
      $scope.integerValue = -1234.56;
      element = $compile("<input number-mask='2' ng-model='integerValue''>")($scope);
      $scope.$digest();
      expect(element.val()).toBe('-1,234.56');
    });
  });

  describe('parse string', () => {
    it('should parse to positive number with decimals', () => {
      $scope.integerValue = null;
      element = $compile("<input number-mask='2' ng-model='integerValue''>")($scope);
      $scope.$digest();
      element.val('2000.14').trigger('input');
      expect($scope.integerValue).toBe(2000.14);
      expect(element.val()).toBe('2,000.14');
    });

    it('should parse to negative number with decimals', () => {
      $scope.integerValue = null;
      element = $compile("<input number-mask='2' ng-model='integerValue''>")($scope);
      $scope.$digest();
      element.val('-2000.14').trigger('input');
      expect($scope.integerValue).toBe(-2000.14);
      expect(element.val()).toBe('-2,000.14');
    });
  });


  describe('validate string', () => {
    it('should validate max', () => {
      $scope.integerValue = 6;
      element = $compile("<form name='aForm'><input number-mask='0' name='aField' max='5' ng-model='integerValue''></form>")($scope);
      $scope.$digest();
      expect($scope.integerValue).toBeUndefined();
      expect($scope.aForm.aField.$invalid).toBeTruthy();
    });

    it('should validate min', () => {
      $scope.integerValue = 4;
      element = $compile("<form name='aForm'><input number-mask='0' name='aField' min='5' ng-model='integerValue''></form>")($scope);
      $scope.$digest();
      expect($scope.integerValue).toBeUndefined();
      expect($scope.aForm.aField.$invalid).toBeTruthy();
    });

    it('should be valid', () => {
      $scope.integerValue = 5;
      element = $compile("<form name='aForm'><input number-mask='0' name='aField' min='5' ng-model='integerValue''></form>")($scope);
      $scope.$digest();
      // expect($scope.integerValue).toBe(5);
      expect($scope.aForm.aField.$invalid).toBeFalsy();
    });
  });
});
