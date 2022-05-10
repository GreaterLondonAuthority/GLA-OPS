/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

MessageModal.$inject = ['$uibModal', '$timeout'];

function MessageModal($uibModal) {
  return {
    show: function (config) {
      var defaultConfig = {
        message: 'Lorem Ipsum',
        cta: 'OK'
      };

      return $uibModal.open({
        animation: false,
        templateUrl: 'scripts/components/messageModal/messageModal.html',
        size: 'confirm',
        bindToController: true,
        controllerAs: '$ctrl',
        controller: [function () {
          this.config = _.merge(defaultConfig, config);
        }]
      });
    }
  }
}

angular.module('GLA')
  .service('OldDeleteMessageModal', MessageModal);
