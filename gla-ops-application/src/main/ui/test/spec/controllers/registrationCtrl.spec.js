/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../utils');

describe('Controller: RegistrationCtrl', () => {

  beforeEach(function () {
    angular.mock.module('GLA');
  });

  let ctrl, $state, UserService, $controller;

  beforeEach(inject((_$controller_, _$state_, _UserService_) => {
    UserService = _UserService_;
    $controller = _$controller_;
    $state = _$state_;
    ctrl = $controller('RegistrationCtrl');
  }));

  it('should register on successful registration', () => {
    spyOn(UserService, 'registerUser').and.returnValue(utils.mockPromise({data: {id: 123}}));
    spyOn($state, 'go');
    ctrl.submit();
    expect(UserService.registerUser).toHaveBeenCalled();
    expect($state.go).toHaveBeenCalledWith('confirm-user-created');
  });
});
