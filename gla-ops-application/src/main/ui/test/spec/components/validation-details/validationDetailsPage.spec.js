/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



describe('Component: Data validation details page', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.sysInfo = {
      'dataValidation': {
        'validationFailures' : [
          {'validationType' : 'TestValidation', 'detail' : '{PROJECTID=1234}'},
          {'validationType' : 'DuplicateBlocks', 'detail' : '{PROJECTID=5678}'}
        ]
      }
    };

    element = $compile('<validation-details-page sys-info="sysInfo"></validation-details-page>')($scope);
    $scope.$digest();

  }));

  describe('Validation details table and header', () => {

    it('Has a validation details header', () => {
      expect(element.text()).toContain('Validation Details');
    });

    it('contains a table with headers validation type and detail', () => {
      let tableHeaders = element.find('#validation-details-table-headers');
      expect(tableHeaders.text()).toContain('Validation Type');
      expect(tableHeaders.text()).toContain('Details');
    })

    it('contains two rows with data', () => {
      let tableBody = element.find('#validation-details-table-body');
      let rows = element.find('.validation-failure-row');
      expect(tableBody.text()).toEqual('   TestValidation  {PROJECTID=1234}   DuplicateBlocks  {PROJECTID=5678}  ');
      expect(rows.length).toEqual(2);
    });

  });

});
