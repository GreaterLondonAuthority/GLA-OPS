/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import util from './paymentsTestUtil';


describe('Service: PaymentService', () => {

  beforeEach(angular.mock.module('GLA'));

  let PaymentService;

  const apiPayments = [
    //Group of payments for milestone 'm1' without external id on project 'p1'
    util.testData(1, {category: 'Milestone', subCategory: 'm1'}),
    util.testData(1, {category: 'Milestone', subCategory: 'm1'}),

    //Group of payments for milestone 'm1' without externalId on project 'p2'
    util.testData(2, {category: 'Milestone', subCategory: 'm1'}),

    //Group of payments for milestone 'm1' with externalId:1 on project 'p2'
    util.testData(2, {category: 'Milestone', subCategory: 'm1', externalId: '1'}),
    util.testData(2, {category: 'Milestone', subCategory: 'm1', externalId: '1'}),

    //Group of payments for milestone 'm2' with externalId:2 on project 'p3'
    util.testData(3, {category: 'Milestone', subCategory: 'm2', externalId: '2'}),

    //Group of payments for non milestone payment for project 'p3'
    util.testData(3, {category: 'NON_MILESTONE', subCategory: 'm2', externalId: '2'})
  ];
  const apiGroupPayments = [
    // keep this one as index 0
    util.testGroupData(2, {}, [
      {category: 'Milestone', subCategory: 'm1', paymentSource: 'Grant'},
      {category: 'Milestone', subCategory: 'm2', paymentSource: 'Grant'},
    ]),
    util.testGroupData(1, {}, [
      {category: 'Milestone1', subCategory: 'm1'},
      {category: 'Milestone1', subCategory: 'm2'},
      {category: 'Milestone2', subCategory: 'm1'},
      {category: 'Milestone2', subCategory: 'm3'},
      {category: 'Milestone2', subCategory: 'm2'}
    ]),
  ];

  let keys = ['projectId', 'externalId', 'category', 'subCategory'];


  beforeEach(inject($injector => {
    PaymentService = $injector.get('PaymentService');
  }));

  describe('groupMilestonePayments', () => {
    it('Should sort group payements in group and process source', () => {
      let groupedPayments = PaymentService.transformPaymentGroups(apiGroupPayments);
      _.forEach(groupedPayments, (paymentGroup)=>{
        for (let i = 1; i < paymentGroup.payments.length; i++) {
          let previousPayment = paymentGroup.payments[i-1];
          let currentPayment = paymentGroup.payments[i];
          if(previousPayment.category !== currentPayment.category){
            expect(previousPayment.category).toBeLessThan(currentPayment.category);
          } else if(previousPayment.category === currentPayment.categroy) {
            expect(previousPayment.subCategory).toBeLessThan(currentPayment.subCategory);
          }
        }
      });
      expect(apiGroupPayments[0].payments[0].paymentSource).toEqual('Grant');
      expect(groupedPayments[0].payments[0].source).toEqual('Grant');
    })


  });
});
