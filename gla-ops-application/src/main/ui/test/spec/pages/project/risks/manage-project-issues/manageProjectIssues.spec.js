/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../../../utils');

describe('Page: issues: manageProjectIssues', () => {
  let config = {
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, OrganisationService, ReferenceDataService, ctrl, $componentController, FileDeleteConfirmationModal;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    // OrganisationService = $injector.get('OrganisationService');
    // UserService = $injector.get('UserService');
    // ReferenceDataService = $injector.get('ReferenceDataService');
    // FileDeleteConfirmationModal = $injector.get('FileDeleteConfirmationModal');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();

    $scope.readOnly = false;
    $scope.issues = [];
    $scope.createNewIssue = ()=>{};
    $scope.addAction = ()=>{};
    $scope.blockSessionStorage = {
      manageProjectIssuesTablesState: []
    };
    element = $compile('<manage-project-issues read-only="readOnly" issues="issues" create-new-issue="createNewIssue()" add-mitigation="addMitigation(issue)" block-session-storage="blockSessionStorage"></manage-project-issue>')($scope);
  }));

  describe('manageProjectIssues initial state', () => {
    it('Initial empty issue table', ()=> {
      $scope.readOnly = true;
      $scope.$digest();
      expect(element.text()).not.toContain('CREATE NEW ISSUE +');
      expect(element.text()).not.toContain('Add Mitigation & Owner');
      expect(element.text()).toContain('TITLE DESCRIPTION OF ISSUES AND IMPACT IMPACT LEVEL STATUS');
      expect(element.text()).not.toContain('ACTIONS	OWNER')
      expect(element.text()).toContain('Not provided')
    });
    it('Expanded 1 issue table', ()=> {
      $scope.readOnly = true;
      $scope.blockSessionStorage.manageProjectIssuesTablesState['123'] = true;
      $scope.issues = [{
        id: '123',
        title: 'issue 1 title',
        description: 'issue 1 description',

        initialImpactRating: '1',
        status: 'Open',
        actions: []
      }];
      $scope.$digest();
      expect(element.text()).not.toContain('CREATE NEW ISSUE +');
      expect(element.text()).not.toContain('Add Action & Owner');
      expect(element.text()).toContain('TITLE DESCRIPTION OF ISSUES AND IMPACT IMPACT LEVEL STATUS');
      expect(element.text()).toContain('issue 1 title');
      expect(element.text()).toContain('issue 1 description');
      expect(element.text()).toContain('Green - Low')
      expect(element.text()).toContain('ACTIONS  OWNER');
      expect(element.text()).toContain('Not provided');
    });
    it('Expanded 1 issue with actions table', ()=> {
      $scope.readOnly = true;
      $scope.blockSessionStorage.manageProjectIssuesTablesState['123'] = true;
      $scope.issues = [{
        id: '123',
        title: 'issue 1 title',
        description: 'issue 1 description',

        initialImpactRating: '1',
        status: 'Open',
        actions: [{
          action: 'issue 1 action 1 description',
          owner: 'issue 1 action 1 owner'
        }]
      }];
      $scope.$digest();
      expect(element.text()).not.toContain('CREATE NEW ISSUE +');
      expect(element.text()).not.toContain('Add Action & Owner');
      expect(element.text()).toContain('ACTIONS  OWNER');
      expect(element.text()).toContain('issue 1 action 1 description');
      expect(element.text()).toContain('issue 1 action 1 owner');
    });
    it('Expanded 1 issue with actions table in edit mode', ()=> {
      $scope.readOnly = false;
      $scope.blockSessionStorage.manageProjectIssuesTablesState['123'] = true;
      $scope.issues = [{
        id: '123',
        title: 'issue 1 title',
        description: 'issue 1 description',

        initialImpactRating: '1',
        status: 'Open',
        actions: [{
          description: 'issue 1 action 1 description',
          owner: 'issue 1 action 1 owner'
        }]
      }];
      $scope.$digest();
      expect(element.text()).toContain('CREATE NEW ISSUE +');
      expect(element.text()).toContain('Add Action & Owner');
    });
  });

  describe('GLA-9427 - Delete issue', ()=>{
    /*it('GLA-9623 - Deleted issues are removed from the table - 1', ()=>{
      $scope.readOnly = false;
      $scope.blockSessionStorage.manageProjectIssuesTablesState['123'] = true;
      $scope.issues = [{
        id: '123',
        title: 'issue 1 title',
        description: 'issue 1 description',

        initialImpactRating: '1',
        status: 'Open',
        actions: [{
          description: 'issue 1 action 1 description',
          owner: 'issue 1 action 1 owner'
        }]
      }];
      $scope.$digest();
      expect(element.find('.delete-issue-button-container .delete-button').length).toEqual(1);

    });*/

    it('GLA-9623 - Deleted issues are removed from the table - 2', ()=>{
      $scope.readOnly = true;
      $scope.blockSessionStorage.manageProjectIssuesTablesState['123'] = true;
      $scope.issues = [{
        id: '123',
        title: 'issue 1 title',
        description: 'issue 1 description',

        initialImpactRating: '1',
        status: 'Open',
        actions: [{
          description: 'issue 1 action 1 description',
          owner: 'issue 1 action 1 owner'
        }]
      }];
      $scope.$digest();

      expect(element.find('.delete-issue-button-container .delete-button').length).toEqual(0);

    });

    /*it('GLA-9429 - Delete action and owner within an issue', ()=>{
      $scope.readOnly = false;
      $scope.blockSessionStorage.manageProjectIssuesTablesState['123'] = true;
      $scope.issues = [{
        id: '123',
        title: 'issue 1 title',
        description: 'issue 1 description',

        initialImpactRating: '1',
        status: 'Open',
        actions: [{
          description: 'issue 1 action 1 description',
          owner: 'issue 1 action 1 owner'
        }]
      }];
      $scope.$digest();

      expect(element.find('.delete-action-button-container .delete-button').length).toEqual(1);

    });*/
  });
});
