/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

describe('Directive: chrome-autofill', () => {

  beforeEach(angular.mock.module('GLA'));

  let $window, navigator, $compile, $rootScope, $scope, $interval, autofill, element;

  beforeEach(inject(function(_$compile_, _$rootScope_, _$window_, _$interval_){
    $window = _$window_;
    $compile = _$compile_;
    $rootScope = _$rootScope_;
    $interval = _$interval_;
    $scope = $rootScope.$new();
    navigator = $window.navigator;
  }));

  beforeEach(()=>{
    autofill = false;
    $scope.onAutoFill = () => {
      autofill = true;
    };
  });

  describe('Chrome browser', () => {
    beforeEach(()=>{
      fakeChrome();
      element = $compile("<input chrome-autofill='onAutoFill()' style=''>")($scope);
    });

    it('should notify if Chrome and if background color is from autofill', () => {
      spyOn($window, 'getComputedStyle').and.returnValue({backgroundColor: 'rgb(250, 255, 189)'});
      $interval.flush(2000);
      expect(autofill).toBe(true);
    });

    it('should not notify when background color is never set to autofill colour in Chrome', () => {
      $interval.flush(2000);
      $scope.$digest();
      expect(autofill).toBe(false);
    });

    afterEach( () => {
      undoFakeChrome();
    });
  });

  it('should not notify in other browsers than Chrome', () => {
    $interval.flush(2000);
    $scope.$digest();
    expect(autofill).toBe(false);
  });

  function fakeChrome(){
    if($window.navigator.userAgent.indexOf('Chrome') === -1) {
      $window.navigator = {userAgent: 'Chrome'};
    }
  }

  function undoFakeChrome(){
    if($window.navigator.userAgent !== navigator.userAgent) {
      $window.navigator = navigator;
    }
  }
});
