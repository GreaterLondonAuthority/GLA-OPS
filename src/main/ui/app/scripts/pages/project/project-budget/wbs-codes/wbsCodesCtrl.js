/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class WbsCodesCtrl {
  constructor(ConfirmationDialog, UserService) {
    this.ConfirmationDialog = ConfirmationDialog;
    this.canDelete = UserService.hasPermission('proj.wbs.delete');
    this.validators = [];
  }

  $onInit() {
    this.max = this.max || 10;
    if (this.type) {
      this.validators.push(this.isValidWbsCodeEnding.bind(this));
    } else {
      this.validators.push(this.isUnique.bind(this));
    }
  }

  delete(wbs) {
    _.remove(this.codes, {code: wbs.code});
  }

  confirmDeletion(wbs) {
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete this WBS code?');
    modal.result.then(() => {
      this.delete(wbs);
      if(this.onWbsCodeModification){
        this.onWbsCodeModification();
      }
    });
  }

  add(wbsCode) {
    this.codes.push({
      code: wbsCode,
      type: this.type
    });
    this.code = null;
    if(this.onWbsCodeModification){
      this.onWbsCodeModification();
    }
  }

  isValid(wbsCode) {
    for (let i = 0; i < this.validators.length; i++) {
      let isValidFn = this.validators[i];
      if (!isValidFn(wbsCode)) {
        return false;
      }
    }
    return true;
  }

  isValidWbsCodeEnding(wbsCode) {
    if (!wbsCode || wbsCode.length < 2) {
      return false;
    }
    let endingExists = this.codes.some(wbs => {
      let existingSubstr = wbs.code.substr(wbs.code.length - 2);
      let addedSubstr = wbsCode.substr(wbsCode.length - 2);
      return existingSubstr === addedSubstr;
    });
    return !endingExists;
  }

  isUnique(wbsCode) {
    let codeExists = this.codes.some(wbs => (wbs.code === wbsCode));
    return !codeExists;
  }
}

WbsCodesCtrl.$inject = ['ConfirmationDialog', 'UserService'];

export default WbsCodesCtrl;
