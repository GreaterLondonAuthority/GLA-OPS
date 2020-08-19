/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class BannerMessageCtrl {

  constructor(SessionService) {
    this.SessionService = SessionService;
  }

  $onInit(){

  }
  close() {
    this.SessionService.setBannerMessageState({
      isDimissed: true,
      message: this.message
    });
  }
}

BannerMessageCtrl.$inject = ['SessionService'];
angular.module('GLA')
  .component('bannerMessage', {
    templateUrl: 'scripts/components/banner-message/bannerMessage.html',
    controllerAs: '$ctrl',
    bindings: {
      message: '<',
      canClose: '<'
    },
    controller: BannerMessageCtrl

});
