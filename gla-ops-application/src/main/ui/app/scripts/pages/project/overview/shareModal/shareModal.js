/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function ShareModal($uibModal) {
  return {
    show: function (projectId) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/overview/shareModal/shareModal.html',
        size: 'md',
        resolve: {},
        controller: [function () {
          this.projectId = projectId;
        }]
      });
    }
  };
}

ShareModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('ShareModal', ShareModal);
