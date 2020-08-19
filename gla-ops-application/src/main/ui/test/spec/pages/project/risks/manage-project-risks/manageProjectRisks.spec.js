/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../../../utils');

describe('Page: risks: manageProjectRisks', () => {
  let config = {
    // getContracts: '.org-contract',
    // contractsCheckBoxes: '.org-contract .checkbox',
    // signedContract: '.org-contract .checkbox.signed.checked',
    // notRequiredContract: '.org-contract .checkbox.not-required.checked'
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
    $scope.risks = [];
    $scope.createNewRisk = ()=>{};
    $scope.addMitigation = ()=>{};
    $scope.blockSessionStorage = {
      manageProjectRisksTablesState: []
    };
    element = $compile('<manage-project-risk read-only="readOnly" risks="risks" create-new-risk="createNewRisk()" add-mitigation="addMitigation(risk)" block-session-storage="blockSessionStorage"></manage-project-risk>')($scope);
  }));

  describe('manageProjectRisks initial state GLA-10574', () => {
    it('Initial empty risk table', ()=> {
      $scope.readOnly = true;
      $scope.$digest();
      expect(element.text()).not.toContain('CREATE NEW RISK +');
      expect(element.text()).not.toContain('Add Mitigation & Owner');
      expect(element.text()).toContain('TITLE DESCRIPTION OF CAUSE AND IMPACT CATEGORY INITIAL RISK RATING RESIDUAL RISK RATING STATUS');
      expect(element.text()).not.toContain('MITIGATION	OWNER')
      expect(element.text()).toContain('Not provided')
    });
    it('Expanded 1 risk table', ()=> {
      $scope.readOnly = true;
      $scope.blockSessionStorage.manageProjectRisksTablesState['123'] = true;
      $scope.risks = [{
        id: '123',
        title: 'risk 1 title',
        description: 'risk 1 description',
        riskCategory: {
          displayValue: 'risk 1 category'
        },
        computedInitialRating: '1',
        initialRiskLevel: {
          level: 'low'
        },
        computedResidualRating: '2',
        residualRiskLevel: {
          level: 'medium'
        },
        status: 'Open',
        actions: []
      }];
      $scope.$digest();
      expect(element.text()).not.toContain('CREATE NEW RISK +');
      expect(element.text()).not.toContain('Add Mitigation & Owner');
      expect(element.text()).toContain('TITLE DESCRIPTION OF CAUSE AND IMPACT CATEGORY INITIAL RISK RATING RESIDUAL RISK RATING STATUS');
      expect(element.text()).toContain('risk 1 title');
      expect(element.text()).toContain('risk 1 description');
      expect(element.text()).toContain('risk 1 category');
      expect(element.text()).toContain('low');
      expect(element.text()).toContain('medium');
      expect(element.text()).toContain('Open');
      expect(element.text()).toContain('MITIGATION  OWNER');
      expect(element.text()).toContain('Not provided');
    });
    it('Expanded 1 risk with actions table', ()=> {
      $scope.readOnly = true;
      $scope.blockSessionStorage.manageProjectRisksTablesState['123'] = true;
      $scope.risks = [{
        id: '123',
        title: 'risk 1 title',
        description: 'risk 1 description',
        riskCategory: {
          displayValue: 'risk 1 category'
        },
        computedInitialRating: '1',
        initialRiskLevel: {
          level: 'low'
        },
        computedResidualRating: '2',
        residualRiskLevel: {
          level: 'medium'
        },
        open: 'Open',
        actions: [{
          action: 'risk 1 action 1',
          owner: 'risk 1 owner 1'
        }]
      }];
      $scope.$digest();
      expect(element.text()).not.toContain('CREATE NEW RISK +');
      expect(element.text()).not.toContain('Add Mitigation & Owner');
      expect(element.text()).toContain('MITIGATION  OWNER');
      expect(element.text()).toContain('risk 1 action 1');
      expect(element.text()).toContain('risk 1 owner 1');
    });
    it('Expanded 1 risk with actions table in edit mode', ()=> {
      $scope.readOnly = false;
      $scope.blockSessionStorage.manageProjectRisksTablesState['123'] = true;
      $scope.risks = [{
        id: '123',
        title: 'risk 1 title',
        description: 'risk 1 description',
        riskCategory: {
          displayValue: 'risk 1 category'
        },
        computedInitialRating: '1',
        initialRiskLevel: {
          level: 'low'
        },
        computedResidualRating: '2',
        residualRiskLevel: {
          level: 'medium'
        },
        open: 'Open',
        actions: [{
          action: 'risk 1 action 1',
          owner: 'risk 1 owner 1'
        }]
      }];
      $scope.$digest();
      expect(element.text()).toContain('CREATE NEW RISK +');
      expect(element.text()).toContain('Add Mitigation & Owner');
    });
  });

  describe('GLA-9426 - Delete risk', ()=>{
    /*it('GLA-9491 - Select delete icon and confirm delete in modal', ()=>{
      $scope.readOnly = false;
      $scope.blockSessionStorage.manageProjectRisksTablesState['123'] = true;
      $scope.risks = [{
        id: '123',
        title: 'risk 1 title',
        description: 'risk 1 description',
        riskCategory: {
          displayValue: 'risk 1 category'
        },
        computedInitialRating: '1',
        initialRiskLevel: {
          level: 'low'
        },
        computedResidualRating: '2',
        residualRiskLevel: {
          level: 'medium'
        },
        open: 'Open',
        actions: [{
          action: 'risk 1 action 1',
          owner: 'risk 1 owner 1'
        }]
      }];
      $scope.$digest();
      expect(element.text()).toContain('MITIGATION  OWNER');
      expect(element.text()).toContain('risk 1 action 1');
      expect(element.text()).toContain('risk 1 owner 1');

      expect(element.find('.delete-risk-button-container .delete-button').length).toEqual(1);

    });*/

    it('GLA-9491 - delete icon not showing in read only', ()=>{
      $scope.readOnly = true;
      $scope.blockSessionStorage.manageProjectRisksTablesState['123'] = true;
      $scope.risks = [{
        id: '123',
        title: 'risk 1 title',
        description: 'risk 1 description',
        riskCategory: {
          displayValue: 'risk 1 category'
        },
        computedInitialRating: '1',
        initialRiskLevel: {
          level: 'low'
        },
        computedResidualRating: '2',
        residualRiskLevel: {
          level: 'medium'
        },
        open: 'Open',
        actions: [{
          action: 'risk 1 action 1',
          owner: 'risk 1 owner 1'
        }]
      }];
      $scope.$digest();
      expect(element.text()).toContain('MITIGATION  OWNER');
      expect(element.text()).toContain('risk 1 action 1');
      expect(element.text()).toContain('risk 1 owner 1');

      expect(element.find('.delete-button').length).toEqual(0);

    });
  });
});
