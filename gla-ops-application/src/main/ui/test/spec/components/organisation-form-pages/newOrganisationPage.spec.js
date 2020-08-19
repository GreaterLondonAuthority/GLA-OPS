/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



describe('Organisation: new organisation', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, PaymentService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.types = [];

    $scope.sysMetrics = {

    };
    element = $compile('<new-organisation-page organisation-types="types"></new-organisation-page>')($scope);
    $scope.$digest();
  }));


  describe('New organisation page', () => {

    it('Tech Support organisation type selected, regulatory information section not present', () => {
      let $ctrl = element.isolateScope().$ctrl;
      $ctrl.org.entityType = 5;
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');
    });

    it('Other organisation types selected, regulatory information section not be present', () => {
      let $ctrl = element.isolateScope().$ctrl;
      $ctrl.org.entityType = 1;
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');
      $ctrl.org.entityType = 2;
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');
      $ctrl.org.entityType = 3;
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');
      $ctrl.org.entityType = 4;
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');
    });


  });


});
