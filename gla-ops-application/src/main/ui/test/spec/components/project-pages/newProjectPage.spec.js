/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



describe('Component: New Project Page', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, PaymentService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.progs = [];

    element = $compile('<new-project-page programmes="progs"></new-project-page>')($scope);
    $scope.$digest();
  }));


  describe('New Project Page Test', () => {
    let $ctrl;

    beforeEach(()=>{
      $ctrl = element.isolateScope().$ctrl;
      $ctrl.showMissingProfileProjectEditor = false;
      $ctrl.showMissingProfileOrgAdmin = false;
      $ctrl.showPendingProfileProjectEditor = false;

      $ctrl.title = 'test';
      $ctrl.selectedProgramme = 'test';
      $ctrl.selectedTemplate = 'test';
      $ctrl.isConsortium = false;
      $ctrl.isSaving = false;

      $ctrl.isConsortium = false;

      $ctrl.user = {
        loggedOn: true
      }
    });

    it('Fields filled with valid inputs plus Tech Support Org - disabled save button', () => {
      $ctrl.selectedOrganisation = {
        isTechOrg: true
      };
      $scope.$digest();
      let button = element.find('#project-save');
      expect(button.attr('disabled')).toEqual('disabled');
    });

    it('Fields filled with valid inputs plus Org other than Tech Support - enabled save button', () => {
      $ctrl.selectedOrganisation = {
        isTechOrg: false
      };
      $scope.$digest();
      let button = element.find('#project-save');
      expect(button.attr('disabled')).not.toBeDefined();
    });

  });


});
