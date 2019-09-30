/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function CreateOverrideModal($uibModal, ProjectService) {
  return {
    show: function (override, metadata) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/overrides/modal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.override = angular.copy(override || {});
          this.override.reportedDate = new Date(this.override.reportedDate);
          this.metadata = metadata;
          if(this.override.id){
           this.readOnly = true;
          }

          this.dateOptions = {
            showWeeks: false,
            format: 'dd/MM/yyyy',
            formatYear: 'yyyy',
            formatMonth: 'MMM',
            yearColumns: 3,
            initDate: new Date()
          };

          this.onProjectIdChange = (projectId) => {
            if(projectId) {
              this.loading = true;
              ProjectService.getProject(projectId).then(rsp => {
                let project = rsp.data;
                console.log('project', rsp.data)
                this.projectExists = true;
              }).catch(err => {
                this.projectExists = false;
              }).finally(()=>{
                this.loading = false;
              });
            }
            this.projectExists = false;
          }
        }]
      });
    },

  };
}

CreateOverrideModal.$inject = ['$uibModal', 'ProjectService'];

angular.module('GLA')
.service('CreateOverrideModal', CreateOverrideModal);

