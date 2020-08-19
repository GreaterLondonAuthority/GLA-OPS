/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



describe('Organisation: organisation details page', () => {

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
    $scope.org = {
      techSupportOrganisation: true
    };
    $scope.roles = [
      {name: 'ROLE_OPS_ADMIN', description: 'OPS Admin', default: false}
    ];
    element = $compile('<organisation-page organisation-types="types" organisation="org" available-user-roles="roles"></organisation-page>')($scope);
    $scope.$digest();
    $ctrl = element.isolateScope().$ctrl;

  }));


  describe('Organisation details page', () => {

    it('Tech Support organisation detail page has no regulatory information section', () => {
      expect(element.text()).not.toContain('Regulatory information');
    });

    it('Non tech support organisation detail page has regulatory information section', () => {
      $ctrl.org = {
        techSupportOrganisation: false
      };
      $scope.$digest();
      expect(element.text()).toContain('Regulatory information');
    });

  });


});
