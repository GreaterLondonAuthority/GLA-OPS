/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function EditSystemMessageModal($uibModal, TransitionService, ProjectService) {
  return {
    show: function (title, message) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/system-messages/modal.html',
        size: 'md',
        controller: [function () {
// TODO REMOVE UNNECESSARY BINDINGS
          this.modal = {
            title: title,
            label: 'Type a message',
            actionBtnName: 'SAVE'
          };

          this.dataBlock = _.clone(message);

          this.action = () => {
            this.$close(this.dataBlock);
          }
        }],
        resolve: {

        }
      });
    }
  };
}

EditSystemMessageModal.$inject = ['$uibModal', 'TransitionService', 'ProjectService'];

angular.module('GLA')
  .service('EditSystemMessageModal', EditSystemMessageModal);
