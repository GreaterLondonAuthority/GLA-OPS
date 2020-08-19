/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



describe('Component: System Page', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, PaymentService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.sqlUpdateDetails = {
      approved: false,
      approvedBy: null,
      approvedOn: null,
      createdBy: 'test.admin@gla.com',
      createdOn: '2018-04-17T14:36:32.371Z',
      id: 10000,
      ppd: false,
      rowsAffected: null,
      sql: 'Test SQL',
      status: 'AwaitingApproval'
    };
    element = $compile('<sql-details sql-update-details="sqlUpdateDetails"></sql-details>')($scope);
    $scope.$digest();
  }));


  describe('SQL Details page tests', () => {

    it('Approve and run button is disabled' , () => {
      let approveBtn = element.find('#approve-and-run-btn');
      expect(approveBtn.attr('disabled')).toEqual('disabled');
    });

    it('Approve and run button enabled when ppd checkbox is checked', () => {
      $scope.sqlUpdateDetails.ppd = true;
      $scope.$digest();
      let approveBtn = element.find('#approve-and-run-btn');
      expect(approveBtn.attr('disabled')).not.toBeDefined();
    });

    it('When status is approved, SAVE button is not visible', () => {
      $scope.sqlUpdateDetails.status = 'Approved';
      $scope.sqlUpdateDetails.approved = true;
      $scope.$digest();
      expect(element.text()).not.toContain('Save');
    });
  });

});
