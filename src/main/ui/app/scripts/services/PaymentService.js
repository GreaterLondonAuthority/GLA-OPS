/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

PaymentService.$inject = ['$http', 'config', 'MilestonesService'];

function PaymentService($http, config, MilestonesService) {

  return {


    /**
     * Return grouped payments by status. Defaults to ALL
     * @param status [PENDING | AUTHORISED | ALL]
     */
    getPayment(paymentId){


      return $http.get(`${config.basePath}/payments/${paymentId}`).then(rsp => {
          let payment = rsp.data;
          return this.enrichPayment(payment);
      });
    },/**
     * Return grouped payments by status. Defaults to ALL
     * @param status [PENDING | AUTHORISED | ALL]
     */
    getPayments(projectIdOrName, orgName, programmeName, statuses, sources, programmes, toDate, fromDate, paymentDirection, page, size, sort){
      const cfg = {
        params: {
          page: page,
          size: size,
          sort: sort,
          relevantStatuses: statuses,
          paymentSources: sources,
          project: projectIdOrName,
          organisation: orgName,
          programme: programmeName,
          relevantProgrammes: programmes,
          paymentDirection: paymentDirection,
          toDate: toDate ? (moment(toDate).format('DD/MM/YYYY')) : null,
          fromDate: fromDate ? (moment(fromDate).format('DD/MM/YYYY')) : null

        }
      };

      return $http.get(`${config.basePath}/payments`, cfg).then(rsp => {
        ((rsp.data && rsp.data.content) || []).map(payment => {
          return this.enrichPayment(payment);
        });
        return rsp.data;

      });
    },

    authorisedStatuses(){
      return ['Authorised', 'UnderReview', 'SupplierError', 'Acknowledged', 'Cleared', 'Sent'];
    },

    /**
     * Return grouped payments by status. Defaults to ALL
     * @param status [PENDING | AUTHORISED | ALL]
     */
    getPaymentGroups(status){
      const cfg = {
        params: {
          status: status || 'ALL'
        }
      };

      return $http.get(`${config.basePath}/paymentGroups`, cfg).then(rsp => this.transformPaymentGroups(rsp.data));
    },
    getPaymentGroup(groupId){
      return $http.get(`${config.basePath}/paymentGroups/${groupId}`).then(rsp => this.transformPaymentGroup(rsp.data));
    },
    getPaymentGroupByPaymentId(paymentId){
      return $http.get(`${config.basePath}/paymentGroups/payment/${paymentId}`).then(rsp => this.transformPaymentGroup(rsp.data));
    },
    getPaymentDeclineReason(){
      return $http.get(`${config.basePath}/categoryValues/PaymentDeclineReason`).then(rsp => rsp.data);
    },

    getReclaimDeclineReason(){
      return $http.get(`${config.basePath}/categoryValues/ReclaimDeclineReason`).then(rsp => rsp.data);
    },

    reclaim(paymentId, amount){
      return $http.post(`${config.basePath}/payments/${paymentId}/reclaim`, amount).then(rsp => rsp.data);
    },

    getReclaims(paymentId, projectId) {
      return this.getPayments(projectId).then(payments => {
        return _.filter(payments.content, {reclaimOfPaymentId: paymentId});
      })
    },

    resend(paymentId, wbsCode){
      return $http.put(`${config.basePath}/payments/resend/${paymentId}?wbsCode=${wbsCode}`).then(rsp => rsp.data);
    },


    getPaymentMilestone(payment) {
      if(payment && payment.category === 'Milestone') {
        return MilestonesService.getMilestoneBlock(payment.projectId, payment.blockId)
        // return MilestonesService.getProjectBlock(payment.projectId, payment.blockId, false)
          .then(resp => {
            let block = resp.data;
            if (payment.externalId) {
              return _.find(block.milestones, {externalId: payment.externalId});
            } else {
              return _.find(block.milestones, {summary: payment.subCategory});
            }
          });
      }
      return null;
    },

    enrichPayment(payment){
      payment.source = this.getPaymentSource(payment);
      payment.sourceType = this.getSpendTypeAndPaymentSource(payment);
      return payment;
    },

    transformPaymentGroup(paymentGroup) {
      let hasReclaim = false;
      paymentGroup.payments = (paymentGroup.payments || []).map(payment => {
        this.enrichPayment(payment);

        if(payment.reclaim){
          // if(paymentGroup.length>1){
          //   throw new Error('This reclaim logic only works for 1 reclaim');
          // }
          hasReclaim = true;
        }
        return payment;
      });

      paymentGroup.hasReclaim = hasReclaim;
      return paymentGroup;
    },

    transformPaymentGroups(paymentGroupsFromAPI){
      //Adds calcuated 'source' field to the payment object
      let paymentsWithSourceProperty = (paymentGroupsFromAPI || []).map(paymentGroup => this.transformPaymentGroup(paymentGroup));
      return paymentsWithSourceProperty;
    },

    /**
     * Derive source column for payments based on transactionType property
     * @param transactionType
     * @return {*} Payment source
     */
    getPaymentSource(payment){
      let source = this.getSpendTypeAndPaymentSource(payment);
       if(payment.reclaim) {
         return payment.interestPayment? `${source} Interest` : `Reclaim ${source}`;
       }
      return source;
    },

    getSpendTypeAndPaymentSource(payment){
      if(payment.paymentSource === 'Grant' && payment.spendType) {
        let capitalisedSpendType = payment.spendType.charAt(0).toUpperCase() + payment.spendType.slice(1).toLowerCase();
        return capitalisedSpendType+' '+payment.paymentSource;
      }
      return payment.paymentSource;
    },

    /**
     * Authorise all payments for current payment
     * @param payment
     */
    authoriseGroup(groupId){
      return $http.post(`${config.basePath}/payments/authorise/group/${groupId}`);
    },

    declineGroup(groupId, data){
      return $http.post(`${config.basePath}/payments/decline/group/${groupId}`, data);
    },

    setInterest(id, amount){
      return $http.post(`${config.basePath}/payments/${id}/interest`,amount);
    },
    setInterests(interests){
      let data = {};
      _.each(interests, (interest) => {
        data[interest.id] = interest.interest
      });
      return $http.post(`${config.basePath}/payments/interest`,data);
    },

    searchOptions() {
      return [
        {
          name: 'project',
          description: 'By Project',
          hint: 'Enter the project id number or title',
          maxLength: '50'
        },
        {
          name: 'programme',
          description: 'By Programme',
          hint: 'Enter the programme name',
          maxLength: '50'
        },
        {
          name: 'organisation',
          description: 'By Organisation',
          hint: 'Enter the org name or id',
          maxLength: '50'
        }
      ];
    },
    sourceOptions() {
      let filterDropdownItems = [];

      filterDropdownItems.push({
        checkedClass: 'dpf',
        ariaLabel: 'DPF',
        name: 'dpf',
        model: undefined,
        label: 'DPF',
        sourceKey: 'DPF'
      });

      filterDropdownItems.push({
        checkedClass: 'esf',
        ariaLabel: 'ESF',
        name: 'esf',
        model: undefined,
        label: '  ESF',
        sourceKey: 'ESF'
      });

      filterDropdownItems.push({
        checkedClass: 'grant',
        ariaLabel: 'Grant',
        name: 'grant',
        model: undefined,
        label: 'Grant',
        sourceKey: 'Grant'
      });

      filterDropdownItems.push({
        checkedClass: 'rcgf',
        ariaLabel: 'RCGF',
        name: 'rcgf',
        model: undefined,
        label: 'RCGF',
        sourceKey: 'RCGF'
      });

      return filterDropdownItems;
    },
    paymentDirectionOptions() {
      let filterDropdownItems = [];

      filterDropdownItems.push({
        checkedClass: 'in',
        ariaLabel: 'Reclaim',
        name: 'in',
        model: undefined,
        label: 'Reclaim',
        sourceKey: 'IN'
      });


      filterDropdownItems.push({
        checkedClass: 'out',
        ariaLabel: 'Payment',
        name: 'out',
        model: undefined,
        label: 'Payment',
        sourceKey: 'OUT'
      });

      return filterDropdownItems;
    },
    statusOptions() {
      return [{
        checkedClass: 'pending',
        ariaLabel: 'Pending',
        name: 'pending',
        model: undefined,
        label: 'Pending',
        statusKey: 'Pending',
      }, {
        checkedClass: 'authorised',
        ariaLabel: 'Authorised',
        name: 'authorised',
        model: undefined,
        label: 'Authorised',
        statusKey: 'Authorised',
        // Temp solutions while waiting for hierarchy
        subStatusesKeys: ['Sent', 'UnderReview', 'SupplierError', 'Acknowledged', 'Cleared']
      }, {
        checkedClass: 'declined',
        ariaLabel: 'Declined',
        name: 'declined',
        model: undefined,
        label: 'Declined',
        statusKey: 'Declined',
      // }, {
      //   checkedClass: 'sent',
      //   ariaLabel: 'Sent',
      //   name: 'sent',
      //   model: undefined,
      //   label: 'Sent',
      //   statusKey: 'Sent',
      // }, {
      //   checkedClass: 'underReview',
      //   ariaLabel: 'UnderReview',
      //   name: 'underReview',
      //   model: undefined,
      //   label: 'UnderReview',
      //   statusKey: 'UnderReview',
      // }, {
      //   checkedClass: 'supplierError',
      //   ariaLabel: 'SupplierError',
      //   name: 'supplierError',
      //   model: undefined,
      //   label: 'SupplierError',
      //   statusKey: 'SupplierError',
      // }, {
      //   checkedClass: 'acknowledged',
      //   ariaLabel: 'Acknowledged',
      //   name: 'acknowledged',
      //   model: undefined,
      //   label: 'Acknowledged',
      //   statusKey: 'Acknowledged',
      // }, {
      //   checkedClass: 'cleared',
      //   ariaLabel: 'Cleared',
      //   name: 'cleared',
      //   model: undefined,
      //   label: 'Cleared',
      //   statusKey: 'Cleared',
      }]
    },

    isPending(payment){
      return payment.ledgerStatus === 'Pending';
    },

    isDeclined(payment){
      return payment.ledgerStatus === 'Declined';
    },

    isAuthorised(payment){
      return !(this.isPending(payment) || this.isDeclined(payment));
    }
  };
}

angular.module('GLA').service('PaymentService', PaymentService);
