/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


ModalDisplayService.$inject = ['$injector'];

function ModalDisplayService($injector) {
  const msgDefaults = {
    header: 'Oops!',
    subHeader: 'Something has gone wrong',
    body: 'It’s not your fault. Try again by clicking OK but if the issue persists you may need to speak to the GLA technical team.',
    get bodyWithErrorId() {
      return `${this.body} They may ask for the code below.`
    }
  };

  return {
    standardError(message) {
      if (!this.$uibModal) {
        this.$uibModal = $injector.get('$uibModal');
      }
      return this.$uibModal.open({
        animation: true,
        templateUrl: 'scripts/components/misc/serverError.html',
        size: 'md',
        resolve: {
          message: () => {
            return message;
          }
        },
        controller: ['$scope', 'message', ($scope, message) => {
          $scope.header = message.header ? message.header : msgDefaults.header;
          $scope.subHeader = message.subHeader ? message.subHeader : msgDefaults.subHeader;
          let defaultMessage = message.errorId ? msgDefaults.bodyWithErrorId : msgDefaults.body;
          $scope.body = message.body ? message.body : defaultMessage;
          $scope.errorId = message.errorId ? message.errorId : '';
        }]
      });
    }
  }
}

angular.module('GLA')
  .service('ModalDisplayService', ModalDisplayService);
