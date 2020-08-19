/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function LabelModal($uibModal) {
  return {
    show: function (explanatoryText, existingLabels, preSetLabels) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/overview/labelModal/labelModal.html',
        size: 'md',
        resolve: {},
        controller: [function () {
          this.explanatoryText = explanatoryText || 'Specify ad-hoc label text'
          this.label = {};
          this.labelTypes = [{
            labelName: 'Ad-hoc label',
            type: 'Custom'
          }, {
            labelName: 'Pre-set label',
            type: 'Predefined'
          }];
          this.preSetLabels = preSetLabels;
          this.activePreSetLabels = _.filter(this.preSetLabels, {status: 'Active'});
          this.validate = () => {
            this.isExistingLabel = _.some(existingLabels, (label) =>{
              return (label.text || '').toLowerCase() === (this.label.text || '').toLowerCase();
            });
          },
          this.onLabelTypeChange = () => {
            if(this.label.type === 'Custom') {
              this.label.preSetLabel = null;
            } else {
              this.label.text = null;
            }
          },
          this.apply = () => {
            (this.label.preSetLabel || {}).managingOrganisation = {
              id: (this.label.preSetLabel || {}).managingOrganisationId
            };
            this.$close(this.label)
          }
        }]
      });
    }
  };
}

LabelModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('LabelModal', LabelModal);
