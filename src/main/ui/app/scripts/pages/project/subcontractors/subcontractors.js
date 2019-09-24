/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

require('./projectSubcontractorsModal.js');
require('./subcontractorDeliverableModal.js');

class Subcontractors extends ProjectBlockCtrl {
  constructor($state, $scope, $injector, ProjectService, ProjectSubcontractorModal, ConfirmationDialog, SubcontractorDeliverableModal){
    super($injector);
    this.$state = $state;
    this.ProjectService = ProjectService;
    this.ProjectSubcontractorModal = ProjectSubcontractorModal;
    this.SubcontractorDeliverableModal = SubcontractorDeliverableModal;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
    super.$onInit();
    this.templateConfig = this.TemplateService.getBlockConfig(this.template, this.projectBlock);
    this.showUKPRN = this.projectBlock.subcontractorType == 'LearningProvider';

    this.projectBlock.subcontractors.forEach(c => c.collapsed = true);
    (_.find(this.projectBlock.subcontractors, {id: this.blockSessionStorage.selectedId}) || {}).collapsed = false;
    // if (!this.blockSessionStorage.selectedId && this.projectBlock.subcontractors.length > 1) {
    //   this.showExpandAll = true;
    // }

    // Get configurable labels if exists, otherwise set to some default labels
    this.deliverableName = this.projectBlock.deliverableName ? this.projectBlock.deliverableName : 'Deliverable';
    // this.quantityName = this.projectBlock.quantityName ? this.projectBlock.quantityName : 'Quantity';
    this.quantityName = this.projectBlock.quantityName;
    this.valueName = this.projectBlock.valueName ? this.projectBlock.valueName : 'Value';
    this.feeName = this.projectBlock.feeName ? this.projectBlock.feeName : 'Fee';
  }

  lookupDisplayValue(deliverable) {
    if(deliverable.deliverableType === 'OTHER'){
      return deliverable.deliverableTypeDescription;
    }
    return this.deliverableTypes[deliverable.deliverableType] ;
  }

  refresh() {
    this.$state.reload();
  }

  onCollapseChange(collapsed){
    this.showExpandAll = !(_.some(this.projectBlock.subcontractors, {collapsed: false}));
  }

  save(releaseLock){


    return this.ProjectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, releaseLock);
  }

  submit(){
    return this.save(this.project.id, this.projectBlock.id, this.projectBlock, true);
  }

  showProjectSubcontractorModal(subcontractor, showUKPRN) {
    this.save(false);
    let modal = this.ProjectSubcontractorModal.show(subcontractor, showUKPRN);
    modal.result.then((subcontractor) => {
      let apiRequest;
      if (subcontractor.id) {
        apiRequest = this.ProjectService.updateProjectSubcontractors(this.project.id, this.projectBlock.id, subcontractor);
      } else {
        apiRequest = this.ProjectService.addProjectSubcontractor(this.project.id, this.projectBlock.id, subcontractor);
      }
      apiRequest.then(()=>{
        this.blockSessionStorage.selectedId = '';
        this.$state.reload();
      });
    });
  }

  showDeliverableModal(subcontractor, deliverable) {
    this.save(false);
    let modal = this.SubcontractorDeliverableModal.show(this.project, this.projectBlock, this.deliverableTypes , subcontractor, deliverable);
    modal.result.then((deliverable) => {
      let apiRequest;
      if (deliverable.id) {
        apiRequest = this.ProjectService.updateProjectSubcontractorDeliverable(this.project.id, this.projectBlock.id, subcontractor.id, deliverable.id, deliverable);
      } else {
        apiRequest = this.ProjectService.addProjectSubcontractorDeliverable(this.project.id, this.projectBlock.id, subcontractor.id, deliverable);
      }
      apiRequest.then(()=>{
        this.showExpandAll = false;
        this.blockSessionStorage.selectedId = subcontractor.id;
        this.$state.reload();
      });
    });
  }

  delete(subcontractor){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the subcontractor?');
    modal.result.then(() => {
      this.ProjectService.deleteProjectSubcontractor(this.project.id, this.projectBlock.id, subcontractor.id).then(rsp => {
        this.$state.reload();
      })
    });
  }

  deleteDeliverable(subcontractor, deliverable){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this ' + this.deliverableName.toLowerCase() + '?');
    modal.result.then(() => {
      this.ProjectService.deleteProjectDeliverable(this.project.id, this.projectBlock.id, subcontractor.id,  deliverable.id).then(rsp => {
        this.$state.reload();
      })
    });
  }

  collapseAll(collapsed){
    this.projectBlock.subcontractors.forEach(s => {
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

Subcontractors.$inject = ['$state','$scope', '$injector', 'ProjectService','ProjectSubcontractorModal', 'ConfirmationDialog','SubcontractorDeliverableModal'];

angular.module('GLA')
  .component('subcontractors', {
    controller: Subcontractors,
    bindings: {
      project: '<',
      template: '<',
      deliverableTypes: '<',
      blockSessionStorage: '<'
    },
    templateUrl: 'scripts/pages/project/subcontractors/subcontractors.html'
  });

