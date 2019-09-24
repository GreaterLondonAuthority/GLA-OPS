/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class systemMessageSetupItemCtrl {
  constructor(EditSystemMessageModal) {
    this.EditSystemMessageModal = EditSystemMessageModal;
  }

  $onInit() { }


  showEditModal(){
    let modal = this.EditSystemMessageModal.show(this.title, this.message);
    modal.result.then((message)=>{
      this.onEdit({message: message});
    });
  }


}

systemMessageSetupItemCtrl.$inject = ['EditSystemMessageModal'];

angular.module('GLA')
  .component('systemMessageSetupItem', {
    templateUrl: 'scripts/pages/system-messages/systemMessageSetupItem.html',
    bindings: {
      title: '<',
      message: '<',
      onEdit: '&'
    },
    controller: systemMessageSetupItemCtrl
  });
