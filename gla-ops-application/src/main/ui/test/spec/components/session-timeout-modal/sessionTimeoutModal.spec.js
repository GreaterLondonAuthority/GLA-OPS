/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
const utils = require('../../../utils');

describe('Component: Session Timeout Modal', () => {
  let $rootScope;
  let SessionTimeoutModal;
  let UserService;
  let MetadataService;
  let $timeout;
  let $interval;
  let $uibModalStack;
  let modal;

  beforeEach(angular.mock.module('GLA'));

  beforeEach(inject($injector => {
    $rootScope = $injector.get('$rootScope');
    SessionTimeoutModal = $injector.get('SessionTimeoutModal');
    UserService = $injector.get('UserService');
    MetadataService = $injector.get('MetadataService');
    $timeout = $injector.get('$timeout');
    $interval = $injector.get('$interval');
    $uibModalStack = $injector.get('$uibModalStack');
  }));

  describe('#show()', () => {
    it('should open session timeout modal', () => {
      modal = SessionTimeoutModal.show();
      $rootScope.$digest();
      expectModalWith('TIMEOUT WARNING');
      expectModalWith('Your session is going to expire in:');
    });

    it('should close modal on resume session', () => {
      modal = SessionTimeoutModal.show();
      $rootScope.$digest();
      resumeSessionBtn().click();
      $rootScope.$digest();
      expect(getModal().length).toBe(0);
    });

    it('should close modal and log out on logout click', () => {
      spyOn(UserService, 'logout');
      modal = SessionTimeoutModal.show();
      $rootScope.$digest();
      logoutLink().click();
      $rootScope.$digest();
      expect(getModal().length).toBe(0);
      expect(UserService.logout).toHaveBeenCalled();
    });
  });

  describe('session timeout modal should appear based on config', () => {
    it('should read user session config', () => {
      let user = {
        loggedOn: true,
        idleDuration: 5,
        timeoutDuration: 10,
        keepAliveInterval: 15,
        permissions: []
      };
      spyOn(UserService, 'currentUser').and.returnValue(user);
      spyOn(UserService, 'logout');
      spyOn(MetadataService, 'fireMetadataUpdate').and.returnValue(utils.mockPromise({data: {}}));
      $rootScope.$broadcast('user.login');
      $rootScope.$digest();
      expect(getModal().length).toBe(0);
      $timeout.flush(5000);
      $interval.flush(5000);
      expect(MetadataService.fireMetadataUpdate).toHaveBeenCalled();
      expect(UserService.logout).not.toHaveBeenCalled();
      expectModalWith('TIMEOUT WARNING');
      expectModalWith('0 minutes 10 seconds');
      $timeout.flush(15000);
      $interval.flush(15000);
      expect(UserService.logout).toHaveBeenCalledWith('Sorry, your session has timed out');
    });
  });


  afterEach(()=> {
    console.log('afterEach');
    if(modal){
      modal.close();
    }
    SessionTimeoutModal.closeModal();
    $uibModalStack.dismissAll();
    var openedModal = $uibModalStack.getTop();
    if (openedModal) {
      $uibModalStack.dismiss(openedModal.key);
    }
    $rootScope.$digest();
    $timeout.flush(15000);
    $interval.flush(15000);
  });

  function expectModalWith(messageText) {
    expect(getModal().text()).toContain(messageText);
  }

  function getModal() {
    return $('.timeout-modal')
  }

  function resumeSessionBtn() {
    return $('.btn-primary');
  }

  function logoutLink() {
    return $('.dismiss-btn');
  }
});
