/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../../utils');

describe('change report: change report field', () => {
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
    $scope.sublabel = '';
    $scope.data = {};
    $scope.fields = {};
    $scope.formats = '';
    element = $compile('<change-report-field label="label" sublabel="sublabel" data="data" fields="fields" formats="formats"></change-report-field>')($scope);
  }));

  describe('display left and right values for a specified field', ()=>{
    it('displays a simple field lookup', ()=>{
      $scope.label = 'A title';
      $scope.sublabel = 'A subtitle';
      $scope.fields = 'title';
      $scope.data = {
        left: {
          title: 'left title value'
        },
        right: {
          title: 'right title value'
        }
      };
      $scope.$digest();
      expect(element.text()).toContain('A title');
      expect(element.text()).toContain('A subtitle');
      expect(element.find(config.fieldLeft).text()).toContain('left title value');
      expect(element.find(config.fieldRight).text()).toContain('right title value');
    });
    it('displays a complex field lookup', ()=>{
      $scope.label = 'A title';
      $scope.fields = ['title','org.name','org.leadOrg.name'];
      $scope.data = {
        left: {
          title: 'left title value',
          org: {
            name: 'left orgName',
            leadOrg: {
              name: 'left lead Org'
            }
          }
        },
        right: {
          title: 'right title value',
          org: {
            name: 'right orgName',
            leadOrg: {
              name: 'right lead Org'
            }
          }
        }
      };
      $scope.$digest();
      expect(element.find(config.fieldLeft).text()).toContain('left orgName');
      expect(element.find(config.fieldLeft).text()).toContain('left lead Org');
      expect(element.find(config.fieldRight).text()).toContain('right orgName');
      expect(element.find(config.fieldRight).text()).toContain('right lead Org');
    });
    it('displays a formated fields lookup', ()=>{
      $scope.label = 'A title';
      $scope.fields = [{field:'contract.date',format:'date'},'contract.value'];
      $scope.formats = 'number';
      $scope.data = {
        left: {
          title: 'left title value',
          contract: {
            value: '100000',
            date: '2017-07-24T15:30:22.713'
          }
        },
        right: {
          title: 'right title value',
          contract: {
            value: '200000',
            date: '2017-07-25T16:40:22.713'
          }
        }
      };
      $scope.$digest();
      expect(element.find(config.fieldLeft).text()).toContain('24/07/2017');
      expect(element.find(config.fieldLeft).text()).toContain('100,000');
      expect(element.find(config.fieldRight).text()).toContain('25/07/2017');
      expect(element.find(config.fieldRight).text()).toContain('200,000');
    });
    it('shouldn\'t display left if no left data', ()=>{
      $scope.label = 'A title';
      $scope.fields = [{field:'contract.date',format:'date'},'contract.value'];
      $scope.formats = 'number';
      $scope.data = {
        right: {
          title: 'right title value',
          contract: {
            value: '200000',
            date: '2017-07-25T16:40:22.713'
          }
        }
      };
      $scope.$digest();
      expect(element.find(config.fieldLeft).text()).toEqual('');
      expect(element.find(config.fieldRight).text()).toContain('25/07/2017');
      expect(element.find(config.fieldRight).text()).toContain('200,000');
    });
    it('shouldn\'t display right if no right data', ()=>{
      $scope.label = 'A title';
      $scope.fields = [{field:'contract.date',format:'date'},'contract.value'];
      $scope.formats = 'number';
      $scope.data = {
        left: {
          title: 'left title value',
          contract: {
            value: '200000',
            date: '2017-07-25T16:40:22.713'
          }
        }
      };
      $scope.$digest();
      expect(element.find(config.fieldRight).text()).toEqual('');
      expect(element.find(config.fieldLeft).text()).toContain('25/07/2017');
      expect(element.find(config.fieldLeft).text()).toContain('200,000');
    });
  });
});
