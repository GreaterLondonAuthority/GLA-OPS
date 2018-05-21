/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class PageHeaderCtrl {
  constructor(UserService, $transclude){
    this.hasCustomRightSide = $transclude.isSlotFilled('phRight');
    // console.log('hasCustomRightSide', this.hasCustomRightSide)
    this.currentUser = UserService.currentUser().username;
    this.hasButtons = this.onBack || this.editableBlock || this.hasCustomRightSide
  }
}

PageHeaderCtrl.$inject = ['UserService', '$transclude'];


angular.module('GLA')
  .component('pageHeader', {
    templateUrl: 'scripts/components/page-header/pageHeader.html',
    transclude: {
      phRight: '?phRight'
    },
    bindings: {
      onBack: '&?',
      editableBlock: '<?',
      mouseDownState: '=',
      // hideEdit: '<?',
      header: '<?'
    },
    controller: PageHeaderCtrl

  });
