/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function SubcontractorDeliverableModal($uibModal, ProjectService, $q) {
  return {
    show: function (project, block, deliverableTypes, subcontractor, deliverable) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/subcontractors/subcontractorDeliverableModal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.requestId = 0;
          this.requestsQueue = [];
          this.subcontractor = angular.copy(subcontractor || {});
          this.deliverable = angular.copy(deliverable || {});
          this.deliverable.subcontractorId = subcontractor.id;
          this.deliverableTypes = deliverableTypes;
          let deliverableTypesKeys = Object.keys(this.deliverableTypes);
          this.showDeliverableTypes = deliverableTypesKeys.length > 1;
          if(deliverableTypesKeys.length === 1){
            this.deliverable.deliverableType = deliverableTypesKeys[0];
          }
          this.retentionFeeThreshold = block.retentionFeeThreshold;

          // Get configurable labels if exists, otherwise set to some default labels
          // this.deliverableName = block.deliverableName ? block.deliverableName : 'Deliverable';
          this.deliverableName = block.deliverableName ? block.deliverableName : 'Deliverable';
          // this.quantityName = block.quantityName ? block.quantityName : 'Quantity';
          this.quantityName = block.quantityName;
          this.valueName = block.valueName ? block.valueName : 'Value';
          this.feeName = block.feeName ? block.feeName : 'Fee';

          this.modalTitle = this.deliverable.id? 'Update ' + this.deliverableName : 'Add '+ this.deliverableName;

          this.getDeliverableFeeCalculation = () => {
            this.requestId++;
            let index = this.requestId;
            console.warn(`e2e start id:${index} value: ${this.deliverable.fee}` );
            let p = $q.all(this.requestsQueue).then(results => {
              return ProjectService.getDeliverableFeeCalculation(project.id, block.id, this.deliverable.value, this.deliverable.fee).then(rsp => {
                console.warn(`e2e response id:${index} value: ${rsp.data}`);
                this.deliverable.feeCalculation = rsp.data;
              });
            });
            this.requestsQueue.push(p);
          };
        }]
      });
    },
  };
}

SubcontractorDeliverableModal.$inject = ['$uibModal', 'ProjectService', '$q'];

angular.module('GLA')
  .service('SubcontractorDeliverableModal', SubcontractorDeliverableModal);

