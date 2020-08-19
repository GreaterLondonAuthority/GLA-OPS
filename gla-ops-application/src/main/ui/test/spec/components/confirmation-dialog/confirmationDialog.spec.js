/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Component: ConfirmationDialog', () => {
  let ConfirmationDialog;
  let $uibModalStack;
  let $rootScope;
  let $timeout;
  let $interval;
  let modal;

  beforeEach(angular.mock.module('GLA'));

  beforeEach(inject($injector => {
    $rootScope = $injector.get('$rootScope');
    ConfirmationDialog = $injector.get('ConfirmationDialog');
    $uibModalStack = $injector.get('$uibModalStack');
    $timeout = $injector.get('$timeout');
    $interval = $injector.get('$interval');
  }));

  describe('#show()', () => {
    it('should open the dialog with correct defaults', () => {
      modal = ConfirmationDialog.show();
      $rootScope.$digest();
      expectModalWith('Are you sure?', 'Yes', 'No');
    });

    it('should allow to change all defaults', () => {
      let config = {
        message: 'Msg',
        approveText: 'Y',
        dismissText: 'N'
      };
      modal = ConfirmationDialog.show(config);
      $rootScope.$digest();

      expectModalWith(config.message, config.approveText, config.dismissText);
    });

    it('should allow to change only some defaults', () => {
      let config = {
        message: 'Msg',
      };
      modal = ConfirmationDialog.show(config);
      $rootScope.$digest();

      expectModalWith(config.message, 'Yes', 'No');
    });
  });

  describe('#delete()', () => {
    it('should show defaults for delete message', () => {
      modal = ConfirmationDialog.delete();
      $rootScope.$digest();
      expectModalWith('Are you sure you want to delete?', 'DELETE', 'KEEP');

    });

    it('should change delete message', () => {
      let deleteMessage = 'Remove?';
      modal = ConfirmationDialog.delete(deleteMessage);
      $rootScope.$digest();
      expectModalWith(deleteMessage, 'DELETE', 'KEEP');
    });
  });

  describe('#approve()', () => {
    it('should trigger callback on approve button click', () => {
      modal = ConfirmationDialog.delete();
      $rootScope.$digest();
      approveBtn().click();
      let onApprove = jasmine.createSpy('onApprove');
      modal.result.then(onApprove);
      $rootScope.$digest();
      expect(onApprove).toHaveBeenCalled();
    });
  });

  describe('#dismiss()', () => {
    it('should not trigger callback on dismiss button click', () => {
      modal = ConfirmationDialog.delete();
      $rootScope.$digest();
      dismissBtn().click();
      let onApprove = jasmine.createSpy('onApprove');
      modal.result.then(onApprove);
      $rootScope.$digest();
      expect(onApprove).not.toHaveBeenCalled();
    });

    it('should close modal on dismiss button click', () => {
      modal = ConfirmationDialog.delete();
      $rootScope.$digest();
      dismissBtn().click();
      $rootScope.$digest();
      expect(message().length).toBe(0);
    });
  });

  afterEach(()=> {
    modal.close();
    $uibModalStack.dismissAll();
    var openedModal = $uibModalStack.getTop();
    if (openedModal) {
      $uibModalStack.dismiss(openedModal.key);
    }
    $rootScope.$digest();
    $timeout.flush();
    $interval.flush();
    $rootScope.$digest();
  });

  function expectModalWith(messageText, approveText, dismissText) {
    expect(message().text()).toEqual(messageText);
    expect(approveBtn().text()).toEqual(approveText);
    expect(dismissBtn().text()).toEqual(dismissText);
  }

  function message() {
    return $('.confirm-message')
  }

  function approveBtn() {
    return $('.approve-btn');
  }

  function dismissBtn() {
    return $('.dismiss-btn');
  }
});
