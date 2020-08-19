/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


describe('Component: Payment Summary', () => {

  let config = {
    noVendorIdText: 'SAP vendor ID has not been provided',
    sapVendorIdLabel: 'SAP vendor ID:'
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, PaymentService, ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    PaymentService = $injector.get('PaymentService');
    UserService = $injector.get('UserService');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.paymentGroup = {};
    $scope.payment = {'paymentSourceDetails': {'name':'Grant','description':'Grant','grantType':'Grant','sendToSap':true}};
    // element = $compile('<pending-payments payment-groups="paymentGroups" payment-decline-reason="paymentDeclineReason"></pending-payments>')($scope);
    element = $compile('<payment-summary payment-group="paymentGroup" payment="payment"></payment-summary>')($scope);
  }));

  describe('SAP Vendor', () => {
    it('should not show sap vendor rows to RP', () => {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: []});

      $scope.$digest();
      expect(element.text()).not.toContain(config.sapVendorIdLabel);
    });
    it('should show sap vendor rows to GLA', () => {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.view.vendor.sap.id']});
      $scope.$digest();
      expect(element.text()).toContain(config.sapVendorIdLabel);
    });
    it('should show no sap vendor ID warning (GLA-7592)', () => {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.view.vendor.sap.id']});

      $scope.$digest();
      expect(element.text()).toContain(config.noVendorIdText);
      $scope.payment.sapVendorId = 123;
      $scope.$digest();
      expect(element.text()).not.toContain(config.noVendorIdText);

    });

    it('Payment summary page details for pending payment (GLA-5851)', () => {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.view.vendor.sap.id']});

      $scope.payment = {
        // 'id': 10135,
        'projectId': 10040,
        // 'blockId': 10311,
        // 'year': 2017,
        // 'month': 4,
        'ledgerStatus': 'Pending',
        // 'ledgerType': 'PAYMENT',
        // 'spendType': null,
        // 'category': 'Milestone',
        // 'value': 12345.67,
        // 'reference': null,
        // 'pcsPhaseNumber': null,
        // 'organisationId': 9998,
        // 'vendorName': 'Approved Test Organisation',
        // 'transactionDate': null,
        // 'sapCategoryCode': null,
        // 'description': null,
        // 'costCentreCode': null,
        // 'transactionNumber': null,
        // 'invoiceDate': null,
        // 'pcsProjectNumber': null,
        // 'categoryId': null,
        // 'ledgerSource': null,
        // 'createdOn': '2017-06-23T09:30:19.795+01:00',
        // 'createdBy': 'testdatainitialiser@gla.org',
        // 'modifiedOn': null,
        // 'modifiedBy': null,
        // 'subCategory': 'NoSapId',
        // 'authorisedOn': '',
        // 'authorisedBy': '',
        // 'sentOn': null,
        // 'acknowledgedOn': null,
        // 'clearedOn': null,
        // 'wbsCode': null,
        // 'invoiceFileName': null,
        // 'sapVendorId': null,
        // 'externalId': null,
        // 'projectName': 'Project to test Payments',
        // 'programmeName': 'Bucket programme',
        // 'authorisor': null,
        // 'opsInvoiceNumber': 'P10040-10135',
        // 'transactionTypeDesc': 'INV'
      }
      $scope.$digest();
      let text = element.text();
      expect(text).toContain('Payee');
      expect(text).toContain('Organisation name');
      expect(text).toContain('Project Details');
      expect(text).toContain('Project ID:');
      expect(text).toContain('P10040');
      expect(text).toContain('Project Name:');
      expect(text).toContain('Programme:');
      expect(text).toContain('Payment category:');
      expect(text).toContain('Payment sub-category:');
      expect(text).toContain('Payment approval');
      expect(text).toContain('Approval requested by:');
      expect(text).toContain('Approver:');
      expect(text).toContain('Date approved:');
      expect(text).toContain('Payment');
      expect(text).toContain('Payment Status');
      expect(text).toContain('Pending');
      expect(text).toContain('Payment ID:');
      expect(text).toContain('SAP vendor ID:');
      expect(text).toContain('Payment Source:');
      expect(text).toContain('Payment Total:');
      expect(text).not.toContain('Authorised by:');
      expect(text).not.toContain('Authorised on:');
      expect(text).not.toContain('Time:');
      expect(text).not.toContain('Declined by:');
      expect(text).not.toContain('Declined on:');
      expect(text).not.toContain('Time:');
      expect(text).not.toContain('Reason for payment decline:');
      expect(text).not.toContain('Comments:');
    });
    it('Payment summary for authorised payments(GLA-7575)', () => {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.view.vendor.sap.id']});

      $scope.payment = {
        'projectId': 10040,
        'ledgerStatus': 'Authorised'
      };
      $scope.$digest();
      let text = element.text();
      expect(text).toContain('Payee');
      expect(text).toContain('Organisation name');
      expect(text).toContain('Project Details');
      expect(text).toContain('Project ID:');
      expect(text).toContain('P10040');
      expect(text).toContain('Project Name:');
      expect(text).toContain('Programme:');
      expect(text).toContain('Payment category:');
      expect(text).toContain('Payment sub-category:');
      expect(text).toContain('Payment approval');
      expect(text).toContain('Approval requested by:');
      expect(text).toContain('Approver:');
      expect(text).toContain('Date approved:');
      expect(text).toContain('Payment');
      expect(text).toContain('Payment Status');
      expect(text).toContain('Authorised');
      expect(text).toContain('Payment ID:');
      expect(text).toContain('SAP vendor ID:');
      expect(text).toContain('Payment Source:');
      expect(text).toContain('Payment Total:');
      expect(text).toContain('Authorised by:');
      expect(text).toContain('Authorised on:');
      expect(text).not.toContain('Declined by:');
      expect(text).not.toContain('Declined on:');
      expect(text).not.toContain('Reason for payment decline:');
      expect(text).not.toContain('Comments:');
      expect(text).toContain('Time:');
    });

    it('Payment summary for declined payments(GLA-7576)', () => {
      spyOn(UserService, 'currentUser').and.returnValue({permissions: ['org.view.vendor.sap.id']});
      $scope.paymentGroup = {
        declineReason: {

        }
      };
      $scope.payment = {
        'projectId': 10040,
        'ledgerStatus': 'Declined',
      }
      $scope.$digest();
      let text = element.text();
      expect(text).toContain('Payee');
      expect(text).toContain('Organisation name');
      expect(text).toContain('Project Details');
      expect(text).toContain('Project ID:');
      expect(text).toContain('P10040');
      expect(text).toContain('Project Name:');
      expect(text).toContain('Programme:');
      expect(text).toContain('Payment category:');
      expect(text).toContain('Payment sub-category:');
      expect(text).toContain('Payment approval');
      expect(text).toContain('Approval requested by:');
      expect(text).toContain('Approver:');
      expect(text).toContain('Date approved:');
      expect(text).toContain('Payment');
      expect(text).toContain('Payment Status');
      expect(text).toContain('Declined');
      expect(text).toContain('Payment ID:');
      expect(text).toContain('SAP vendor ID:');
      expect(text).toContain('Payment Source:');
      expect(text).toContain('Payment Total:');
      expect(text).not.toContain('Authorised by:');
      expect(text).not.toContain('Authorised on:');
      expect(text).toContain('Declined by:');
      expect(text).toContain('Declined on:');
      expect(text).toContain('Reason for payment decline:');
      expect(text).toContain('Comments:');
      expect(text).toContain('Time:');
    });



  });
  function columnValues(colElSelector){
    return element.find(colElSelector).map((index, item) => $(item).text().trim()).get()
  }

});
