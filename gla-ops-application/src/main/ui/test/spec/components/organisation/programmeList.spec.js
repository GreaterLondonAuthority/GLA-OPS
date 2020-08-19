/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../utils');

describe('Component: organisation programmes list', () => {
  let config = {
    getProgrammes: '.org-programme'
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, OrganisationService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    OrganisationService = $injector.get('OrganisationService');
    UserService = $injector.get('UserService');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.org = {
      programmes: [{name:'test'}]
    };
    $scope.refreshDetails = ()=>{};
    element = $compile('<programmes-list org="org" refresh-details="refreshDetails()"></programmes-list>')($scope);
  }));

  describe('Display programmes', () => {

    it('Display "view all programmes" CTA', () => {
      $scope.org = {
        programmes: [{
          name: '123'
        },{
          name: '234'
        },{
          name: '345'
        },{
          name: '456'
        },{
          name: '567'
        }]
      };
      $scope.$digest();
      //TODO Moved outside component
      // expect(element.text()).toContain('5 programmes');
      expect(element.text()).toContain('VIEW ALL PROGRAMMES');
      expect(element.find(config.getProgrammes).length).toEqual(3);
    });

    it('View all programmes', () => {
      $scope.org = {
        programmes: [{
          name: '123'
        },{
          name: '234'
        },{
          name: '345'
        },{
          name: '456'
        },{
          name: '567'
        }]
      };
      $scope.$digest();
      let $ctrl = element.isolateScope().$ctrl;
      $ctrl.showMoreLessProgrammes();
      $scope.$digest();
      expect(element.text()).toContain('VIEW LESS PROGRAMMES');
      expect(element.find(config.getProgrammes).length).toEqual(5);
    });

    it('View less programmes', () => {
      $scope.org = {
        programmes: [{
          name: '123'
        },{
          name: '234'
        },{
          name: '345'
        },{
          name: '456'
        },{
          name: '567'
        }]
      };
      $scope.$digest();
      let $ctrl = element.isolateScope().$ctrl;
      $ctrl.showAll = true;
      $ctrl.showHowMany = $scope.org.programmes.length;
      $scope.$digest();
      expect(element.text()).toContain('VIEW LESS PROGRAMMES');
      expect(element.find(config.getProgrammes).length).toEqual(5);

      $ctrl.showMoreLessProgrammes();
      $scope.$digest();
      expect(element.text()).toContain('VIEW ALL PROGRAMMES');
      expect(element.find(config.getProgrammes).length).toEqual(3);
    });
  });

});
