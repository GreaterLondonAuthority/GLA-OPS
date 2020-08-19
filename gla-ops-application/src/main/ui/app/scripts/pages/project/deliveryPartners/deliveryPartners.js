/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';
import './projectDeliveryPartnersModal.js';
import './deliveryPartnerDeliverableModal.js';


class DeliveryPartners extends ProjectBlockCtrl {
  constructor($state, $scope, $injector, ProjectService, ProjectDeliveryPartnerModal, ConfirmationDialog, DeliveryPartnerDeliverableModal){
    super($injector);
    this.$state = $state;
    this.ProjectService = ProjectService;
    this.ProjectDeliveryPartnerModal = ProjectDeliveryPartnerModal;
    this.DeliveryPartnerDeliverableModal = DeliveryPartnerDeliverableModal;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
    super.$onInit();
    this.showExpandAll = true;
    this.templateConfig = this.TemplateService.getBlockConfig(this.template, this.projectBlock);

    this.projectBlock.deliveryPartners.forEach(c => c.collapsed = true);
    (_.find(this.projectBlock.deliveryPartners, {id: this.blockSessionStorage.selectedId}) || {}).collapsed = false;

    this.entityName = this.projectBlock.entityName ? this.projectBlock.entityName : 'Partner';

  }

  refresh() {
    this.$state.reload();
  }

  onCollapseChange(collapsed){
    this.showExpandAll = !(_.some(this.projectBlock.deliveryPartners, {collapsed: false}));
  }

  save(releaseLock){
    return this.ProjectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, releaseLock).then(rsp => {
      this.projectBlock.validationFailures = rsp.data.validationFailures;
    });
  }

  submit(){
    return this.save(this.project.id, this.projectBlock.id, this.projectBlock, true);
  }

  showProjectDeliveryPartnerModal(deliveryPartner) {
    this.save(false);
    let modal = this.ProjectDeliveryPartnerModal.show(deliveryPartner, this.templateConfig);
    modal.result.then((deliveryPartner) => {
      let apiRequest;
      if (deliveryPartner.id) {
        apiRequest = this.ProjectService.updateProjectDeliveryPartners(this.project.id, this.projectBlock.id, deliveryPartner);
      } else {
        apiRequest = this.ProjectService.addProjectDeliveryPartners(this.project.id, this.projectBlock.id, deliveryPartner);
      }
      apiRequest.then(()=>{
        this.blockSessionStorage.selectedId = '';
        this.$state.reload();
      });
    });
  }

  onDeliveryPartnerChange(deliveryPartner) {
    this.save(false).then(()=>{
      let apiRequest;
      if (deliveryPartner.id) {
        apiRequest = this.ProjectService.updateProjectDeliveryPartners(this.project.id, this.projectBlock.id, deliveryPartner);
      } else {
        apiRequest = this.ProjectService.addProjectDeliveryPartners(this.project.id, this.projectBlock.id, deliveryPartner);
      }
      apiRequest.then(()=>{
        this.blockSessionStorage.selectedId = '';
        this.$state.reload();
      });
    });
  }

  onDeliveryPartnerDeliverableChange(event) {
    this.save(false).then(() => {
       let apiRequest;
      if (event.deliverable.id) {
        apiRequest = this.ProjectService.updateProjectPartnerDeliverable(this.project.id, this.projectBlock.id, event.deliveryPartner.id, event.deliverable.id, event.deliverable);
      } else {
        apiRequest = this.ProjectService.addProjectPartnerDeliverable(this.project.id, this.projectBlock.id, event.deliveryPartner.id, event.deliverable);
      }
      apiRequest.then(() => {
        this.showExpandAll = false;
        this.blockSessionStorage.selectedId = event.deliveryPartner.id;
        this.$state.reload();
      });
    });
  }

  onDelete(deliveryPartner){
      this.ProjectService.deleteProjectDeliveryPartner(this.project.id, this.projectBlock.id, deliveryPartner.id).then(rsp => {
        this.$state.reload();
      })
  }

  onDeleteDeliverable(event){
      this.ProjectService.deleteProjectDeliverable(this.project.id, this.projectBlock.id, event.deliveryPartner.id,  event.deliverable.id).then(rsp => {
      this.$state.reload();
    });
  }

  collapseAll(collapsed){
    this.projectBlock.deliveryPartners.forEach(s => {
      s.collapsed = collapsed;
      s.deliverables.forEach(p => p.collapsed = collapsed);
    });
  }

  toggleExpansion(){
    this.showExpandAll = !this.showExpandAll;
    this.collapseAll(this.showExpandAll);
  }

  getAnswerAsText(boolValue){
    if(boolValue == null){
      return 'Not provided';
    }
    return boolValue? 'Yes' : 'No';
  }

  back() {
    this.returnToOverview();
  }
}

DeliveryPartners.$inject = ['$state','$scope', '$injector', 'ProjectService','ProjectDeliveryPartnerModal', 'ConfirmationDialog','DeliveryPartnerDeliverableModal'];

angular.module('GLA')
  .component('deliveryPartners', {
    controller: DeliveryPartners,
    bindings: {
      project: '<',
      template: '<',
      deliverableTypes: '<',
      blockSessionStorage: '<'
    },
    templateUrl: 'scripts/pages/project/deliveryPartners/deliveryPartners.html'
  });

