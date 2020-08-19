/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class DeliveryPartnersSummaryReport {
  constructor(TemplateService) {
    this.TemplateService = TemplateService
  }

  $onInit() {

    this.templateConfig = _.find(this.template.blocksEnabled, {block: 'DeliveryPartners'});

    this.deliverableTypes =  [];
    this.TemplateService.getAvailableDeliverableTypes(this.template.id).then(rsp => {
      this.deliverableTypes = rsp.data;
    });

    this.block.deliveryPartners.forEach(s => {
      s.collapsed = true;
      s.deliverables.forEach(p => p.collapsed = true);
    });
  }
}

DeliveryPartnersSummaryReport.$inject = ['TemplateService'];

angular.module('GLA')
  .component('deliveryPartnersSummaryReport', {
    bindings: {
      project: '<',
      block: '<',
      template: '<'
    },
    templateUrl: 'scripts/pages/summary-report/deliveryPartnersSummaryReport.html',
    controller: DeliveryPartnersSummaryReport
  });
