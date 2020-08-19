/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/*'use strict';

var utils = require('../../utils');

describe('Controller: OrganisationCtrl', function () {

  var CTRL_NAME = 'OrganisationCtrl';
  var ctrl, API, $controller, $scope, $state;

  beforeEach(function () {
    angular.mock.module('GLA');
  });

  beforeEach(inject(function (_$controller_, _OrganisationService_, _UserService_) {
    OrganisationService = _OrganisationService_;
    $controller = _$controller_;
    UserService = _UserService_;
    spyOn(UserService, 'currentUser').and.returnValue({data: {organisation: 'SOME ORG'}});
    $scope = {};
  }));

  it('should call API with valid parameters to initialise organisation details', function () {
    spyOn(OrganisationService, 'getDetails').and.returnValue(utils.mockPromise({}));
    initCtrl();
    expect(OrganisationService.getDetails.calls.mostRecent().args[0]).toEqual(123);
  });

  it('should call API with valid parameters to approve user registration', function () {
    var user = {
      username: 'test@email.com'
    };
    spyOn(OrganisationService, 'approveUser').and.returnValue(utils.mockPromise(user));
    initCtrl();

    $scope.approve(user);
    var param = OrganisationService.approveUser.calls.mostRecent().args[0];

    expect(param).toEqual(user.username);
    expect(user.approved).toEqual(true);
  });


  function initCtrl() {
    ctrl = $controller(CTRL_NAME, {
      $scope: $scope,
      $state: {params: {orgId: 123}}
    });
  }
});*/
