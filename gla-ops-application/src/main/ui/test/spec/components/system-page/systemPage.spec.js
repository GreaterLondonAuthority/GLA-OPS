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
    $scope.sysInfo = {
      'changelogDetails': {
        'numberEntries': 5,
        'lastExecutedFileName': 'test file',
        'numberNotExecuted': 10,
        'lastExecutedFileTime': '2018-02-20T11:14:58.264+0000'
      },
      'scheduledTasks': [{
        'key': 'SMTP_SEND',
        'lastExecuted': '2018-03-14T15:14:41.945Z',
        'lastSuccess': '2018-03-14T15:14:41.945Z',
        'results': '0 processed, 19 errors, 0 ignored (33 ms)',
        'status': 'SUCCESS'
      },
      {
        'key': 'AUTHORISED_PAYMENTS',
        'lastExecuted': '2018-03-14T15:16:46.792Z',
        'lastSuccess': '2018-03-14T10:10:43.824Z',
        'results': 'There are NO payments to process',
        'status': 'SKIPPED'
      }
      ],
      'dataValidation': {
          'validationFailures' : [
            {'validationType' : 'TestValidation', 'detail' : '{PROJECTID=1234}'}
          ]
      },
      'sqlExecutionSummary' : {
        approved: 2,
        pending: 3,
        rejected: 1
      },
      'auditSummary' : {
        'numberAuditActivities' : 12,
        'mostRecentEventTime' : '2010-03-14T10:10:43.824Z'
      }
    };
    $scope.features = [];
    $scope.sysMetrics = {
      'counter.status.500.tester': 100,
      'counter.status.500.secondtest': 40,
      'counter.status.200.test': 1000
    };
    $scope.isSqlEditorEnabled = true;
    element = $compile(`<system-page sys-info="sysInfo" 
                              sys-metrics="sysMetrics"
                              features="features"
                              is-sql-editor-enabled="isSqlEditorEnabled"></system-page>`)($scope);
    $scope.$digest();



  }));


  describe('System page tests', () => {

    let state;

    beforeEach(inject(function ($state) {

      state = $state;
      spyOn(state, 'reload');

    }));



    it('should have Scheduled Tasks', () => {

      expect(element.text()).toContain('Scheduled Tasks');

    });

    it('should have values matching the scheduledTask Array', () => {

      expect(element.text()).toContain('SMTP_SEND');
      expect(element.text()).toContain('AUTHORISED_PAYMENTS');

    });

    it('should have a Liquibase Information', () => {

      expect(element.text()).toContain('Liquibase Information');

    });

    it('should have number entries equal to 5', () => {

      expect(element.text()).toContain('Entries 5');

    });

    it('should have last executed file name equal to test file', () => {

      expect(element.text()).toContain('Last executed file test file');

    });

    it('should have number files not executed equal to 10', () => {

      expect(element.text()).toContain('Not executed 10');

    });

    it('should have last executed file time equal to 12', () => {

      expect(element.text()).toContain('Last executed file time 20/02/2018 11:14');

    });

    it('Clicking refresh re-resolves all resolves (including API calls)', () => {

      let btn = element.find('#refresh-dash');
      btn.click();

      expect(state.reload).toHaveBeenCalledTimes(1);

    });


  });


  describe('SQL Section', () => {

    it('contains SQL section', () => {
      expect(element.text()).toContain('SQL Execution');
    });

    it('displays a summary of updates run on the database', () => {
      expect(element.text()).toContain('Number of approved & run updates: 2');
      expect(element.text()).toContain('Number of updates awaiting approval: 3');
      expect(element.text()).toContain('Number of rejected updates: 1');
    });

  });

  describe('Data Validation Section', () => {

    it('contains Data Validation section', () => {
      expect(element.text()).toContain('Data Validation Errors');
    });


    it('One duplicate block error, one test validation error', () => {
      let duplicateBlocks = element.find('#duplicate-block-validation');
      expect(duplicateBlocks.text().trim()).toEqual('Duplicate blocks:  0');
      let testValidation = element.find('#test-validation');
      expect(testValidation.text().trim()).toEqual('Test validation:  1')
    });

    it('when there are errors, the number is red (part of validation-error class). when there are no errors, it is not.', () => {
      let blockVal = element.find('.validation-error');
      expect(blockVal.text()).not.toContain('Duplicate blocks:  1');
      expect(blockVal.text()).toContain('Test validation:  1');
    });

  });

  describe('Server Response section', () => {

    it('contains server response section', () => {
      expect(element.text()).toContain('Server Response');
    });

    it('number of error responses', () => {
      let $ctrl = element.isolateScope().$ctrl;
      expect($ctrl.numberServerErrors).toEqual(140);
    });
  });

  describe('Server Audit activity', () => {

    it('contains audit activity section', () => {
      expect(element.text()).toContain('Audit Activity');
    });

    it('contains most recent event and total number of events', () => {
      let auditActivity = element.find('#audit-activity');
      expect(auditActivity.text()).toContain('Total audit events: 12');
      expect(auditActivity.text()).toContain('Most recent event 14/03/2010 10:10');
    });
  });

});
