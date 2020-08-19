/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class DeliveryPartnersChangeReport {
  constructor(ReportService, $log) {
    this.ReportService = ReportService;
    this.$log = $log;
  }

  $onInit() {
    const template = this.data.context.template;
    let templateConfig = _.find(template.blocksEnabled, {block: 'DeliveryPartners'});

    this.delieryPartnerQuestionText = templateConfig.hasDeliveryPartnersTitle;
    let contractValueField = templateConfig.showDeliverables?'deliverableContractValue':'contractValue';
    let organisationNameColumnText = templateConfig.organisationNameColumnText || 'Org Name';
    let ukprnColumnText = templateConfig.ukprnColumnText || 'UKPRN';
    let contractValueColumnText = templateConfig.contractValueColumnText || 'Contract Value';


    this.fields = [
      {
        field: 'organisationName',
        label: organisationNameColumnText,
      },
      {
        field: 'identifier',
        label: ukprnColumnText
      },
      {
        field: contractValueField,
        format: 'currency',
        label: contractValueColumnText
      }
    ];

    let leftDeliveryPartners = (this.data.left && this.data.left.hasDeliveryPartners? this.data.left.deliveryPartners : []) || [];
    let rightDeliveryPartners = (this.data.right && this.data.right.hasDeliveryPartners ? this.data.right.deliveryPartners : []) || [];
    let changes =  this.data.changes;

    this.deliveryPartnersToCompare = [];

      for (let i = 0; i < rightDeliveryPartners.length; i++) {
        let matchFilter = {'originalId': rightDeliveryPartners[i].originalId};
        if (!rightDeliveryPartners[i].originalId) {
          matchFilter = {'organisationName': rightDeliveryPartners[i].organisationName};
        }

        let leftDeliveryPartner = _.find(leftDeliveryPartners, matchFilter);
        if (leftDeliveryPartner) {
          _.remove(leftDeliveryPartners, leftDeliveryPartner);
        }


        this.deliveryPartnersToCompare.push({
          left: leftDeliveryPartner,
          right: rightDeliveryPartners[i]
        });
      }

    leftDeliveryPartners.forEach(m => {
      this.deliveryPartnersToCompare.push({
        left: m,
        right: null
      })
    });



   /* if(this.data.left && this.data.right){
      this.data.changes.addDeletions(this.deliveryPartnersToCompare);
    } */

  }
}

DeliveryPartnersChangeReport.$inject = ['ReportService', '$log'];

angular.module('GLA')
  .component('deliveryPartnersChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/deliveryPartnersChangeReport.html',
    controller: DeliveryPartnersChangeReport
  });
