/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../../utils');

const unitDetailsData = require('./unitresponses');

describe('Page: change report unit details', () => {
  let config = {
    // getContracts: '.org-contract',
    // contractsCheckBoxes: '.org-contract .checkbox',
    // signedContract: '.org-contract .checkbox.signed.checked',
    // notRequiredContract: '.org-contract .checkbox.not-required.checked'
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UnitsService, OrganisationService, ReferenceDataService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    OrganisationService = $injector.get('OrganisationService');
    UnitsService = $injector.get('UnitsService');
    ReferenceDataService = $injector.get('ReferenceDataService');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();

    spyOn(UnitsService, 'getUnitsMetadata').and.returnValue(
      utils.mockPromise({data: {tenureDetails: []}})
    );

    $scope.item = {
      type: 'UnitDetailsBlock',
      left: unitDetailsData.block,
      right: unitDetailsData.blockUnapproved,
      context: {
        project:{
          left:{
            id:1
          },
          right:{
            id:1
          }
        }
      },
      changes: {
          addDeletions: () => {},
          hasFieldChanged: () => {}
      }
    };
    element = $compile('<unit-details-change-report data="item"></unit-details-change-report>')($scope);
  }));

  describe('Do not display any form elements in the report', () => {
    it('should have not input elements', ()=> {
      $scope.$digest();

      expect(element.find('input').length).toEqual(0);
    });
  });

  describe('displays expect sections', () => {
    it('displays summary', ()=> {
      $scope.$digest();
      expect(element.text()).toContain('Summary of Units by Tenure on the Project');
      expect(element.text()).toContain('Total Units reflects the number of units already on the project; these must match the number of Profiled Units detailed in this block.');
      expect(element.find('.tenure-summaries').text()).toContain('London Living Rent');
      expect(element.find('.tenure-summaries').text()).toContain('London Shared Ownership');
    });
    it('Details of Rental Units', ()=> {
      $scope.$digest();
      expect(element.text()).toContain('Details of Rental Units');
    });
    it('Details of Sales Units', ()=> {
      $scope.$digest();
      expect(element.text()).toContain('Details of Sales Units');
    });
    it('Project Details', ()=> {
      $scope.$digest();
      expect(element.text()).toContain('Project Details');
      expect(element.text()).toContain('Build Type');
      expect(element.text()).toContain('The total build type split must match the total of 500 units in the project');
      expect(element.text()).toContain('Units by Number of People');
      expect(element.text()).toContain('Detail how many people each unit is designed to accommodate, total must match the 108 units in the project.');
      expect(element.text()).toContain('Number of Wheelchair Units');
      expect(element.text()).toContain('In total (including any specialised units) how many units are either wheelchair accessible or wheelchair adaptable?');
      expect(element.text()).toContain('Gross Internal Area');
      expect(element.text()).toContain('The total gross internal area in sqm for all units in the project, excluding communal areas e.g. lifts.');
    });
  });
});
