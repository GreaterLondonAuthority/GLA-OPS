/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../utils');

describe('Component: Audit activity page', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, $componentController, AuditService;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    AuditService = $injector.get('AuditService');


    spyOn(AuditService, 'getPagedAuditEvents').and.returnValue(utils.mockPromise(
      {
        data: {content: [{
            entityId: null,
            entityType: null,
            id: 100008,
            summary: 'Data initialiser framework completed.',
            timestamp: '2018-05-01T15:19:55.984+01:00',
            type: null,
            userName: 'TestDataInitialiser@gla.org'
          }]
        }
      }
      ));

    element = $compile('<audit-activity></audit-activity>')($scope);
    $scope.$digest();

  }));


  describe('Audit history page tests', () => {

    it('contains page header', () => {
      expect(element.text()).toContain('Audit History')
    });

    it('contains table with headers', () => {
      let headers = element.find('#audit-table-headers').text();
      expect(headers).toContain('ID');
      expect(headers).toContain('Activity Time');
      expect(headers).toContain('Username');
      expect(headers).toContain('Summary');

    });

    it('contains table row with audit event data', () => {
      let row = element.find('#100008').text();
      expect(row).toContain('Data initialiser framework completed.');
      expect(row).toContain('TestDataInitialiser@gla.org');
      expect(row).toContain('01/05/2018');
      expect(row).toContain('100008');
    });

  });

});
