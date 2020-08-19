/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import util from './paymentsTestUtil';


describe('Component: pending-payments', () => {

  let config = {
    noPaymentsEl: '.no-payments-message',
    noPaymentsText: 'There are currently no payments awaiting authorisation',
    noVendorIdText: 'SAP vendor ID has not been provided',
    projectTitleColumnEl: 'table tbody td:nth-child(2)',
    typeColumnEl: 'table tbody td:nth-child(5)',
    projectTitleColumnHeaderEl: 'table thead th:nth-child(2)'
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, PaymentService, UserService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));

  beforeEach(() => $ctrl());

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    PaymentService = $injector.get('PaymentService');
    UserService = $injector.get('UserService');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.paymentGroups = [];
    $scope.paymentDeclineReason = [];
    element = $compile('<pending-payments payment-groups="paymentGroups" payment-decline-reason="paymentDeclineReason"></pending-payments>')($scope);
  }));

  describe('No payments', () => {
    it('should show "no payments" message (GLA-5424)', () => {
      $scope.$digest();
      expect(element.find(config.noPaymentsEl).text()).toContain(config.noPaymentsText);
    });

    it('should not show "no payments" message', () => {
      // $scope.payments = PaymentService.groupSameMilestonePayments([util.testData(1)]);
      $scope.paymentGroups = [util.testGroupData(1, {}, [
        {category: 'Milestone1', subCategory: 'm1'},
        {category: 'Milestone1', subCategory: 'm2'},
        {category: 'Milestone2', subCategory: 'm1'},
        {category: 'Milestone2', subCategory: 'm3'},
        {category: 'Milestone2', subCategory: 'm2'}
      ])],
      // $scope.paymentGroups = PaymentService.groupSameMilestonePayments([util.testData(1)]);
      $scope.$digest();
      expect(element.find(config.noPaymentsEl).length).toBe(0);
    });
  });

  describe('sap vendor id', () => {
    it('should show "no vendor id" message (GLA-5586)', () => {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.view.vendor.sap.id']});

      // $scope.payments = PaymentService.groupSameMilestonePayments([util.testData(1)]);
      $scope.paymentGroups = [util.testGroupData(1, {}, [{
        category: 'Milestone1', subCategory: 'm1', paymentSourceDetails: {'name':'Grant','description':'Grant','grantType':'Grant','sendToSap':true}
      }]
      )];
      // console.info('---> test');
      // console.info('---> test', $scope.paymentGroups);
      $scope.$digest();
      // console.info('---> test', element.text());
      expect(element.text()).toContain(config.noVendorIdText);
    });

    it('should not show "no vendor id" message', () => {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: []});
      // $scope.payments = PaymentService.groupSameMilestonePayments([util.testData(1)]);
      $scope.paymentGroups = [util.testGroupData(1, {}, [{category: 'Milestone1', subCategory: 'm1'}])];
      $scope.paymentGroups[0].payments[0].sapVendorId = 1234;
      $scope.$digest();
      expect(element.text()).not.toContain(config.noVendorIdText);
    });
  });

  describe('Sorting', () => {

    it('should sort by column header ascending: projectName', () => {

      $ctrl([util.testGroupData(1, {}, [
        {category: 'Milestone1', subCategory: 'm1', projectName: 'bbbb'},
        {category: 'Milestone1', subCategory: 'm2'}
      ]),util.testGroupData(2, {}, [
        {category: 'Milestone1', subCategory: 'm1', projectName: 'aaaa'},
        {category: 'Milestone1', subCategory: 'm2'}
      ])]);

      let projectMap = ctrl.paymentGroups.map((group)=>{
        return group.payments[0].projectName;
      });

      expect(projectMap).toEqual(['bbbb', 'aaaa'])

      ctrl.sortBy('projectName');

      projectMap = ctrl.paymentGroups.map((group)=>{
        return group.payments[0].projectName;
      });

      expect(projectMap).toEqual(['aaaa','bbbb'])

    });

    it('should sort by column header ascending: projectName + createdOn (GLA-5790)', () => {

      $ctrl([util.testGroupData(1, {}, [
        {category: 'Milestone1', subCategory: 'm1', createdOn: '2017-05-16T11:42:46.073+01:00', projectName: 'bbbb'},
        {category: 'Milestone1', subCategory: 'm2'}
      ]),util.testGroupData(2, {}, [
        {category: 'Milestone1', subCategory: 'm1', createdOn: '2016-05-16T11:42:46.073+01:00', projectName: 'aaaa'},
        {category: 'Milestone1', subCategory: 'm2'}
      ])]);

      let projectMap = ctrl.paymentGroups.map((group)=>{
        return group.payments[0].projectName;
      });

      expect(projectMap).toEqual(['bbbb', 'aaaa'])

      ctrl.sortBy('createdOn');

      projectMap = ctrl.paymentGroups.map((group)=>{
        return group.payments[0].projectName;
      });

      expect(projectMap).toEqual(['aaaa','bbbb'])
    });
  });



  function columnValues(colElSelector){
    return element.find(colElSelector).map((index, item) => $(item).text().trim()).get()
  }

  function $ctrl(paymentGroups){
    let bindings = {
      paymentGroups: paymentGroups,
      paymentDeclineReason: [{'id':12,'category':'PaymentDeclineReason','displayOrder':1,'displayValue':'Incorrect payment amount'},{'id':13,'category':'PaymentDeclineReason','displayOrder':2,'displayValue':'Project circumstances have changed'},{'id':14,'category':'PaymentDeclineReason','displayOrder':3,'displayValue':'An error in a block approval has been identified'},{'id':15,'category':'PaymentDeclineReason','displayOrder':4,'displayValue':'The need for additional grant for the project has been identified'},{'id':16,'category':'PaymentDeclineReason','displayOrder':5,'displayValue':'Milestone claim approval error'},{'id':17,'category':'PaymentDeclineReason','displayOrder':6,'displayValue':'Partner qualification failure'},{'id':18,'category':'PaymentDeclineReason','displayOrder':7,'displayValue':'Project is no longer proceeding'},{'id':19,'category':'PaymentDeclineReason','displayOrder':8,'displayValue':'Project no longer qualifies for the payment'},{'id':20,'category':'PaymentDeclineReason','displayOrder':9,'displayValue':'Other'}]
    };
    ctrl =  $componentController('pendingPayments', {}, bindings);
  }

});
