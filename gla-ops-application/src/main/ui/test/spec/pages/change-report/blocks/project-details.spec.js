/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../../utils');

describe('Page: change report project details', () => {
  let config = {
    // getContracts: '.org-contract',
    // contractsCheckBoxes: '.org-contract .checkbox',
    // signedContract: '.org-contract .checkbox.signed.checked',
    // notRequiredContract: '.org-contract .checkbox.not-required.checked'
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, OrganisationService, ReferenceDataService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    OrganisationService = $injector.get('OrganisationService');
    UserService = $injector.get('UserService');
    ReferenceDataService = $injector.get('ReferenceDataService');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();

    spyOn(ReferenceDataService, 'getBoroughs').and.returnValue(utils.mockPromise({}));

    $scope.item = {
      type: 'ProjectDetailsBlock',
      left: {
        title: 'a title left'
      },
      right: {
        title: 'a title right'
      },
      context: {
        project: {
          left: {
            organisation: {}
          },
          right: {
            organisation: {}
          }
        },
        template: {}
      }
    };
    element = $compile('<project-details-change-report data="item"></project-details-change-report>')($scope);
  }));

  describe('Do not display any form elements in the report', () => {
    it('should have not input elements', ()=> {
      $scope.item = {
        type: 'ProjectDetailsBlock',
        left: {
          title: 'a title left'
        },
        right: {
          title: 'a title right'
        },
        context: {
          project: {
            left: {
              organisation: {}
            },
            right: {
              organisation: {}
            }
          },
          template: {}
        }
      };
      $scope.$digest();
      expect(element.find('input').length).toEqual(0);
    });
  });

  describe('GLA-8878 - No unapproved version of the block', () => {
    it('shouldn\'t display any right information if there is no unapproved version', ()=> {
      $scope.item = {
        type: 'ProjectDetailsBlock',
        left: {
          title: 'a title left',
        },
        right: undefined,
        context: {
          project: {
            left: {
              organisation: {}
            },
            right: {
              organisation: {}
            }
          },
          template: {}
        }
      };
      $scope.$digest();
      expect(element.find('.report-field-right').length).toEqual(0);
      expect(element.find('.report-field-left').length).toBeGreaterThan(0);
    });
  });
});
