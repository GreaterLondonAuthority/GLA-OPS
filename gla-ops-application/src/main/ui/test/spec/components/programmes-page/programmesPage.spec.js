/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../utils');


describe('Component: Programmes Page', () => {
  beforeEach(angular.mock.module('GLA'));
  let $compile, $rootScope, $scope, element, TableHelper, ElementHelper, ProgrammeService;
  let CREATE_NEW_BTN_TEXT = 'CREATE NEW';

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    TableHelper = $injector.get('TableHelper');
    ElementHelper = $injector.get('ElementHelper');
    ProgrammeService = $injector.get('ProgrammeService');
    $scope = $rootScope.$new();
    spyOn(ProgrammeService, 'getProgrammes').and.returnValue(utils.mockPromise({data: {content: mockProgrammes()}}));
    element = $compile('<programmes-page></programmes-page>')($scope);
    $scope.$digest();
  }));


  it('should display programmes list', () => {
    let tableContent = TableHelper.getTableContent(element);
    console.log('tableContent', tableContent)
    expect(tableContent).toEqual([
      ['1', 'P1', 'Y', 'T1T2', 'managingOrganisationName1', 'GLA', 'Active', 'Name1', 'Jan 1, 2017 00:00'],
      ['2', 'P2', 'N', 'T4T5', 'managingOrganisationName2', 'All', 'Abandoned', 'Name2', 'Feb 11, 2016 00:00']
    ]);
  });

  it(`should not display "${CREATE_NEW_BTN_TEXT}" button`, () => {
    let createButton = element.find(`button:contains('${CREATE_NEW_BTN_TEXT}')`);
    expect(ElementHelper.isVisible(createButton)).toBe(false);
  });

  function mockProgrammes() {
    return [{
      id: 1,
      name: 'P1',
      enabled: true,
      templates: [
        {name: 'T1'},
        {name: 'T2'}
      ],
      restricted: true,
      status: 'Active',
      managingOrganisationName: 'managingOrganisationName1',
      modifierName: 'Name1',
      modifiedOn: '2017-01-01'
    }, {
      id: 2,
      name: 'P2',
      enabled: false,
      templates: [
        {name: 'T4'},
        {name: 'T5'}
      ],
      restricted: false,
      status: 'Abandoned',
      managingOrganisationName: 'managingOrganisationName2',
      modifierName: 'Name2',
      modifiedOn: '2016-02-11'
    }];
  }
});
