/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



describe('Component: Manage SQL updates table page', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, PaymentService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.isSqlEditorEnabled = true;
    $scope.sqlUpdates = [
      {
        approved: false,
        approvedBy: null,
        approvedOn: null,
        createdBy: 'test.admin@gla.com',
        createdOn: '2018-04-17T14:36:32.371Z',
        id: 10000,
        ppd: false,
        rowsAffected: null,
        sql: 'Test SQL 1',
        status: 'Approved'
      },
      {
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
      }
    ];
    element = $compile('<sql-manager is-sql-editor-enabled="isSqlEditorEnabled"' +
      '                              sql-updates="sqlUpdates"></sql-manager>')($scope);
    $scope.$digest();
  }));


  describe('SQL Manager table', () => {

    it('contains a table with 2 SQL entries', () => {
      let table = element.find('#sql-updates-table');
      let th = table.find('.update-sql');

      let vals = th.map((index, item)=> $(item).text()).get();

      expect(vals[0]).toEqual('Test SQL 1');
      expect(vals[1]).toEqual('Test SQL');
    });

    it('entries have statuses Approved and Awaiting Approval', () => {
      let table = element.find('#sql-updates-table');
      let th = table.find('.update-sql-status');

      let vals = th.map((index, item)=> $(item).text()).get();

      expect(vals[0]).toEqual('Approved');
      expect(vals[1]).toEqual('AwaitingApproval');
    });

  });

});
