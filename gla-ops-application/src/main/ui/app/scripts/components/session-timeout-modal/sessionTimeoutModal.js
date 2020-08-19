/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function SessionTimeoutModal($uibModal, $rootScope, Idle, UserService) {
  return {
    timeoutModal: null,

    show(){
      this.closeModal();
      let modal = $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/components/session-timeout-modal/sessionTimeoutModal.html',
        size: 'md',
        backdrop  : 'static',
        keyboard  : false,
        controller: [function () {

          this.resume = () => {
            Idle.watch();
            this.$dismiss('cancel');
          };

          this.logout = () => {
            UserService.logout();
            this.$dismiss('cancel');
          };

          this.formatTime = (secondsLeft) => {
            let duration = moment.duration(secondsLeft, 's');
            let minutes = duration.minutes();
            let seconds = duration.seconds();
            // let paddedSeconds = ('0' + seconds).slice(-2);
            let minutesText = minutes === 1? 'minute' : 'minutes';
            let secondsText = seconds === 1? 'second' : 'seconds';
            return `${minutes} ${minutesText} ${seconds} ${secondsText}`;
          }
        }]
      });
      this.timeoutModal = modal;
      return modal;
    },

    closeModal(){
      if (this.timeoutModal) {
        this.timeoutModal.close();
        this.timeoutModal = null;
      }
    }
  }
}

SessionTimeoutModal.$inject = ['$uibModal', '$rootScope', 'Idle', 'UserService'];

angular.module('GLA')
  .service('SessionTimeoutModal', SessionTimeoutModal);
