/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



describe('Organisation: edit organisation', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, PaymentService, ctrl, $componentController, $ctrl;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.types = {};
    $scope.org = {};
    element = $compile('<edit-organisation-page organisation-types="types" organisation="org"></edit-organisation-page>')($scope);
    $scope.$digest();
    $ctrl = element.isolateScope().$ctrl;

  }));


  describe('Edit organisation page', () => {

    it('Tech Support organisation type selected, regulatory information section not present', () => {
      // expect(element.text()).toContain('Regulatory information');
      $ctrl.org.entityType = 5;
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');
    });

    it('Other organisation types selected, regulatory information section should not be present', () => {
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

    it('If organisation is currently not tech org and tech org is selected, regulatory information section should not show', () => {

      $scope.org = {techSupportOrganisation: false}
      $ctrl.org.entityType = 5;
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');

    });

    it('If organisation is currently not tech org and tech org is not selected, is not GLA H&L regulatory information section should not show', () => {

      $scope.org = {techSupportOrganisation: false}
      $ctrl.org.entityType = 3;
      $ctrl.org.name = 'GLA Skills';
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');

    });

    it('If organisation is currently tech org and tech org is not selected, is not GLA H&L regulatory information section should  not show', () => {

      $scope.org = {techSupportOrganisation: true}
      $ctrl.org.entityType = 3;
      $ctrl.org.name = 'GLA Culture';
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');

    });

    it('If organisation is currently tech org and tech org is selected, regulatory information section should not show', () => {

      $scope.org = {techSupportOrganisation: false}
      $ctrl.org.entityType = 5;
      $scope.$digest();
      expect(element.text()).not.toContain('Regulatory information');

    });

  });


});
