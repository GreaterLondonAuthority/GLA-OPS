/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../utils');

describe('Component: organisation contracts list', () => {
  let config = {
    getContracts: '.org-contract',
    contractsCheckBoxes: '.org-contract .checkbox',
    signedContract: '.org-contract .checkbox.signed.checked',
    notRequiredContract: '.org-contract .checkbox.not-required.checked'
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, OrganisationService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    OrganisationService = $injector.get('OrganisationService');
    UserService = $injector.get('UserService');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.org = {
      contracts: [{name:'test'}],
      allowedActions: ['EDIT']
    };
    $scope.refreshDetails = ()=>{};
    element = $compile('<contracts-list org="org" refresh-details="refreshDetails()"></contracts-list>')($scope);
  }));

  describe('Display contracts', () => {

    it('GLA-8425 Display "view all contracts" CTA', () => {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: []});

      $scope.org = {
        contracts: [{
          name: '123'
        },{
          name: '234'
        },{
          name: '345'
        },{
          name: '456'
        },{
          name: '567'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      //TODO Moved outside component
      // expect(element.text()).toContain('5 contracts');
      expect(element.text()).toContain('VIEW ALL CONTRACTS');
      expect(element.find(config.getContracts).length).toEqual(3);
    });
    it('GLA-8426 - View all contracts', () => {
      $scope.org = {
        contracts: [{
          name: '123'
        },{
          name: '234'
        },{
          name: '345'
        },{
          name: '456'
        },{
          name: '567'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      let $ctrl = element.isolateScope().$ctrl;
      $ctrl.showMoreLessContract();
      $scope.$digest();
      expect(element.text()).toContain('VIEW LESS CONTRACTS');
      expect(element.find(config.getContracts).length).toEqual(5);
    });
    it('GLA-8427 - View less contracts', () => {
      $scope.org = {
        contracts: [{
          name: '123'
        },{
          name: '234'
        },{
          name: '345'
        },{
          name: '456'
        },{
          name: '567'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      let $ctrl = element.isolateScope().$ctrl;
      $ctrl.showAll = true;
      $ctrl.showHowMany = $scope.org.contracts.length;
      $scope.$digest();
      expect(element.text()).toContain('VIEW LESS CONTRACTS');
      expect(element.find(config.getContracts).length).toEqual(5);

      $ctrl.showMoreLessContract();
      $scope.$digest();
      expect(element.text()).toContain('VIEW ALL CONTRACTS');
      expect(element.find(config.getContracts).length).toEqual(3);
    });
  });
  describe('contracts checkboxes', ()=> {
    it('no check box for RP', ()=> {
      // spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.find(config.contractsCheckBoxes).length).toEqual(0);
    });
    it('check box for GLA', ()=> {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.find(config.contractsCheckBoxes).length).toEqual(2);
    });
  });
  describe('GLA-9536 Ensure correct texts are displayed between RP and GLA views', ()=> {
    it('Blank text', ()=> {
      // spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123,
          status: 'Blank'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.text()).toContain('Not Signed');
    });
    it('Signed text', ()=> {
      // spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123,
          status: 'Signed'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.text()).toContain('Signed');
    });
    it('Not required', ()=> {
      // spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123,
          status: 'NotRequired'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.text()).toContain('Not Required');
    });

  });
  describe('Ensure right check box is ticked based on status', ()=> {
    it('Blank text', ()=> {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123,
          status: 'Blank'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.find(config.signedContract).length).toEqual(0);
      expect(element.find(config.notRequiredContract).length).toEqual(0);
    });
    it('Signed text', ()=> {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123,
          status: 'Signed'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.find(config.signedContract).length).toEqual(1);
      expect(element.find(config.notRequiredContract).length).toEqual(0);
    });
    it('Not required', ()=> {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123,
          status: 'NotRequired'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();

      expect(element.find(config.notRequiredContract).length).toEqual(1);
      expect(element.find(config.signedContract).length).toEqual(0);
    });

  });
  describe('Show org type next to contract if when available', ()=> {
    it('it won\'t show if not availabe', ()=> {

      // spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123,
          orgGroupType: undefined
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.find(config.getContracts).text()).toContain('123');
      expect(element.find(config.getContracts).text()).not.toContain('123 -');
    });
    it('it shows Partnership after contract name', ()=> {

      // spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123,
          orgGroupType: 'Partnership'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.find(config.getContracts).text()).toContain('123 - Partnership');
    });
    it('it shows Consortium after contract name', ()=> {

      // spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.edit.contract']});
      $scope.org = {
        contracts: [{
          name: 123,
          orgGroupType: 'Consortium'
        }],
        allowedActions: ['EDIT']
      };
      $scope.$digest();
      expect(element.find(config.getContracts).text()).toContain('123 - Consortium');
    });
  });
});
