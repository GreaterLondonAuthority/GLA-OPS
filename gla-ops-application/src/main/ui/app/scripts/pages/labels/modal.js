/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function CreateLabelModal($uibModal) {
  return {
    show: function (label, managingOrganisations, labels) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/labels/modal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.label = angular.copy(label || {});
          if(this.label.id){
            this.label.managingOrganisation = {
              id: this.label.managingOrganisationId,
              name: this.label.managingOrganisationName,
            }
          }
          this.managingOrganisations = managingOrganisations;
          this.label.status = this.label.id? this.label.status : 'Active';
          this.validate = () => {
            this.isExistingLabel = _.some(labels, (label) =>{
              return (label.labelName || '').toLowerCase() === (this.label.labelName || '').toLowerCase()
                && label.managingOrganisationId === (this.label.managingOrganisation || {}).id ;
            });
          }
        }]
      });
    },

  };
}

CreateLabelModal.$inject = ['$uibModal'];

angular.module('GLA')
.service('CreateLabelModal', CreateLabelModal);

