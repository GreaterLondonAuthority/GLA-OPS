/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class DeliveryPartnersTable {
  constructor($timeout, ProjectDeliveryPartnerModal, DeliveryPartnerDeliverableModal, ConfirmationDialog) {
    this.$timeout = $timeout;
    this.ProjectDeliveryPartnerModal = ProjectDeliveryPartnerModal;
    this.DeliveryPartnerDeliverableModal = DeliveryPartnerDeliverableModal;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
    this.entityName = this.projectBlock.entityName ? this.projectBlock.entityName : 'Partner';
    this.deliverableName = this.projectBlock.deliverableName ? this.projectBlock.deliverableName : 'Deliverable';
    this.quantityName = this.projectBlock.quantityName;
    this.valueName = this.projectBlock.valueName ? this.projectBlock.valueName : 'Value';
    this.feeName = this.projectBlock.feeName ? this.projectBlock.feeName : 'Fee';

    this.organisationTypeColumnName = this.templateConfig.organisationTypeColumnName || ' Org Type';
    this.showOrganisationType = this.templateConfig.showOrganisationType || false;
    this.organisationNameColumnText = this.templateConfig.organisationNameColumnText || 'Org Name';
    this.showOrganisationName   = this.templateConfig.showOrganisationName || true;
    this.roleColumnText = this.templateConfig.roleColumnText || 'Role';
    this.showRoleColumn  = this.templateConfig.showRoleColumn  || false;
    this.ukprnColumnText = this.templateConfig.ukprnColumnText || 'UKPRN';
    this.showUkprnColumn  = this.templateConfig.showUkprnColumn  || false;
    this.contractValueColumnText = this.templateConfig.contractValueColumnText || 'Contract Value';
    this.showContractValueColumn  = this.templateConfig.showContractValueColumn  || false;
    };

  lookupDisplayValue(deliverable) {
    if(deliverable.deliverableType === 'OTHER'){
      return deliverable.deliverableTypeDescription;
    }
    return this.deliverableTypes[deliverable.deliverableType] ;
  }
  showProjectDeliveryPartnerModal(deliveryPartner) {
    let modal = this.ProjectDeliveryPartnerModal.show(deliveryPartner, this.templateConfig);
    modal.result.then((deliveryPartner) => {
      this.onDeliveryPartnerChange({event:deliveryPartner})
    });
  }

  showDeliverableModal(deliveryPartner, deliverable) {
    let modal = this.DeliveryPartnerDeliverableModal.show(this.project, this.projectBlock, this.deliverableTypes , deliveryPartner, deliverable);
    modal.result.then((deliverable) => {
      this.onDeliveryPartnerDeliverableChange({
        event: {
          deliverable: deliverable,
          deliveryPartner: deliveryPartner
        }
      })
    });
  }

  delete(deliveryPartner){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the ' + this.entityName + '?');
    modal.result.then(() => {
      this.onDelete({event:deliveryPartner})
    });
  }
  deleteDeliverable(deliveryPartner, deliverable){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this ' + this.deliverableName.toLowerCase() + '?');
    modal.result.then(() => {
      this.onDeleteDeliverable({
        event: {
          deliverable: deliverable,
          deliveryPartner: deliveryPartner
        }
      })
    });
  }
}

DeliveryPartnersTable.$inject = ['$timeout', 'ProjectDeliveryPartnerModal', 'DeliveryPartnerDeliverableModal','ConfirmationDialog'];

gla.component('deliveryPartnersTable', {
  bindings: {
    project:'<',
    projectBlock: '<',
    templateConfig: '<',
    readOnly: '<',
    deliverableTypes:'<',
    onDeliveryPartnerChange: '&',
    onDeliveryPartnerDeliverableChange: '&',
    onDelete:'&',
    onDeleteDeliverable:'&'
  },
  controller: DeliveryPartnersTable,
  templateUrl: 'scripts/components/delivery-partners-table/deliveryPartnersTable.html'
});
