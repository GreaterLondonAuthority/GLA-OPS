/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Service: UserService', () => {

  beforeEach(angular.mock.module('GLA'));

  let UserService;

  beforeEach(inject($injector => {
    UserService = $injector.get('UserService');
    let permissions = [
      'global.permission.*',
      'org.permission.1000',
      'general.permission'
    ];
    spyOn(UserService, 'currentUser').and.returnValue({permissions: permissions});
  }));

  describe('hasPermission', () => {
    it('should have global permission without org id', () => {
      expect(UserService.hasPermission('global.permission')).toBe(true);
    });

    it('should have global permission with org id', () => {
      expect(UserService.hasPermission('global.permission', 1000)).toBe(true);
    });

    it('should have org specific permission', () => {
      expect(UserService.hasPermission('org.permission', 1000)).toBe(true);
    });

    it('should not have org specific permission for other org id', () => {
      expect(UserService.hasPermission('org.permission', 999)).toBe(false);
    });

    it('should not have org specific permission without org id', () => {
      expect(UserService.hasPermission('org.permission')).toBe(false);
    });

    it('should have general permission', () => {
      expect(UserService.hasPermission('general.permission')).toBe(true);
    });


    it('should not have non existing general permission', () => {
      expect(UserService.hasPermission('does.not.exist.permission')).toBe(false);
    });
  });

});

