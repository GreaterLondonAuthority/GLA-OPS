/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../utils');

describe('Component: Reports Page', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, ReportService, UserService, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    ReportService = $injector.get('ReportService');
    UserService = $injector.get('UserService');
    $scope = $rootScope.$new();
    $scope.reports = [];
    spyOn(ReportService, 'pollReports').and.returnValue({stop(){}});
    spyOn(UserService, 'isCurrentUserAllowedToAccessSkillsGateway').and.returnValue(utils.mockPromise({status: 200, data: true}));
    element = $compile('<reports-page reports = "reports"></reports-page>')($scope);
    $scope.$digest();

  }));


  describe('Reports page', () => {

    it('If no reports available, message should display and no drop downs', () => {
      expect(element.text()).toContain('There are no reports available for you to download.');

      let dropdownSelects = element.find('.search-dropdown');
      expect(dropdownSelects.length).toEqual(0);
    });

    it('If reports available, message should not display and drop downs visible', () => {
      let $ctrl = element.isolateScope().$ctrl;
      $ctrl.reportsAvailable = true;
      $scope.$digest();
      expect(element.text()).not.toContain('There are no reports available for you to download.');

      let dropdownSelects = element.find('.search-dropdown');
      expect(dropdownSelects.length).toEqual(1);
    })



  });

});
