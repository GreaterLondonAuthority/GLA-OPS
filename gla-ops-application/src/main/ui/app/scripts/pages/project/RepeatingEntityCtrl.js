/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
 * Helper class to be extended by forms/components used in repeating entities
 */
class RepeatingEntityCtrl {

  $onInit() {
    this.form = {};
    if (!this.parentCtrl) {
      console.warn('Components using RepeatingEntityCtrl should be wrapped inside EntityList component')
    }
  }

  onFormChange(entity){
    if(!this.parentCtrl) {
      return;
    }

    if(entity.id){
      this.parentCtrl.updateEntity(entity);
    } else if (this.parentCtrl.hasEntityAnyData(entity)){
      this.parentCtrl.addEntity(entity);
    }
  }
}

export default RepeatingEntityCtrl;
