/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../../utils');

describe('change report: change report field lookp', () => {
  let config = {
    fieldRow: '.report-field-row',
    fieldLeft: '.report-field-row .report-field-left',
    fieldRight: '.report-field-row .report-field-right',
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, ReportService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    ReportService = $injector.get('ReportService');
    UserService = $injector.get('UserService');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.label = '';
    $scope.data = {};
    $scope.fields = {};
    $scope.formats = '';
    $scope.key = '';
    $scope.collectionKey = '';
    $scope.collection = [];
    element = $compile('<change-report-field-lookup label="label" data="data" fields="fields" formats="formats" key="key" collection-key="collectionKey" collection="collection"></change-report-field-lookup>')($scope);
  }));

  describe('will lookup in a collection a specified id and display the value', ()=>{
    it('displays a simple field lookup', ()=>{
      $scope.label = 'Org name';
      $scope.fields = 'name';

      $scope.key = 'orgId';
      $scope.collectionKey = 'internatOrgId';
      $scope.collection = [{
        internatOrgId: 123,
        name: 'org 123'
      }, {
        internatOrgId: 234,
        name: 'org 234'
      }, {
        internatOrgId: 345,
        name: 'org 345'
      }];

      $scope.data = {
        left: {
          title: 'left title value',
          orgId: 123
        },
        right: {
          title: 'right title value',
          orgId: 234
        }
      };
      $scope.$digest();
      expect(element.find(config.fieldLeft).text()).toContain('org 123');
      expect(element.find(config.fieldRight).text()).toContain('org 234');
    });
    it('displays only right if no left data', ()=>{
      $scope.label = 'Org name';
      $scope.fields = 'name';

      $scope.key = 'orgId';
      $scope.collectionKey = 'internatOrgId';
      $scope.collection = [{
        internatOrgId: 123,
        name: 'org 123'
      }, {
        internatOrgId: 234,
        name: 'org 234'
      }, {
        internatOrgId: 345,
        name: 'org 345'
      }];

      $scope.data = {
        right: {
          title: 'right title value',
          orgId: 234
        }
      };
      $scope.$digest();
      expect(element.find(config.fieldLeft).text()).toEqual('');
      expect(element.find(config.fieldRight).text()).toContain('org 234');

    });
    it('displays only left the columnif no right data', ()=>{
      $scope.label = 'Org name';
      $scope.fields = 'name';

      $scope.key = 'orgId';
      $scope.collectionKey = 'internatOrgId';
      $scope.collection = [{
        internatOrgId: 123,
        name: 'org 123'
      }, {
        internatOrgId: 234,
        name: 'org 234'
      }, {
        internatOrgId: 345,
        name: 'org 345'
      }];

      $scope.data = {
        left: {
          title: 'left title value',
          orgId: 234
        }
      };
      $scope.$digest();
      expect(element.find(config.fieldRight).text()).toEqual('');
      expect(element.find(config.fieldLeft).text()).toContain('org 234');
    });
  });
});
