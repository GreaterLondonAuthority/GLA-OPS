/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class messagesPageCtrl {
  constructor($state, $stateParams, ConfigurationService) {
    this.ConfigurationService = ConfigurationService;
    this.$state = $state;
    this.$stateParams = $stateParams;
  }

  $onInit() {

  }

  getLabel(message) {
    return message.codeDisplayName
  }

  onMessageEdit(message){
    this.ConfigurationService.udpateConfigMessage({
      code: message.code,
      text: message.text,
      enabled: message.enabled
    }).then(() => {
      this.$state.go(
        this.$state.current,
        this.$stateParams,
        {reload: true}
      );
    });
  }


}

messagesPageCtrl.$inject = ['$state', '$stateParams', 'ConfigurationService'];

angular.module('GLA')
  .component('messagesPage', {
    templateUrl: 'scripts/pages/system-messages/messagesPage.html',
    bindings: {
      messages: '<'
    },
    controller: messagesPageCtrl
  });
