/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

var utils = require('../../utils');

describe('Controller: HomeCtrl', function () {

  // load the controller's module
  beforeEach(function () {
    angular.mock.module('GLA');
  });

  var ctrl, $scope, $controller, UserService, ConfigurationService;

  // Initialize the controller and a mock scope
  beforeEach(inject(function (_$controller_, $rootScope, _ConfigurationService_, _UserService_) {
    $scope = $rootScope.$new();
    ConfigurationService = _ConfigurationService_;
    $controller = _$controller_;
    UserService = _UserService_;
  }));

  it('should set user on successful login', function () {
    let mockLoginPromise = utils.mockPromise({data: {SID: 123}});
    mockLoginPromise.then = angular.noop;
    spyOn(UserService, 'login').and.returnValue(mockLoginPromise);
    initCtrl();
    ctrl.uname = 'test@user.com';
    ctrl.pass = 'psw';
    ctrl.submit();
    var callParams = UserService.login.calls.mostRecent().args;
    expect(callParams[0]).toEqual('test@user.com');
    expect(callParams[1]).toEqual('psw');
  });


  it('should show message returned by api', function () {
    spyOn(ConfigurationService, 'comingSoonMessage').and.returnValue(utils.mockPromise({data: 'apiMessage'}));
    initCtrl();
    expect(ctrl.comingSoonMessage).toBe('apiMessage');
  });

  function initCtrl() {
    ctrl = $controller('HomeCtrl', {
      $scope: $scope
    });
  }
});
