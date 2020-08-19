/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class WbsCodesCtrl {
  constructor(ConfirmationDialog, UserService, ProjectService) {
    this.ConfirmationDialog = ConfirmationDialog;
    this.canDelete = UserService.hasPermission('proj.wbs.delete');
    this.validators = [];
    this.ProjectService = ProjectService;
    this.projectIdsUsingWbsCode = [];
  }

  $onInit() {
    this.max = this.max || 10;
    this.validators.push(this.isUnique.bind(this));
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
    this.ProjectService.findAllProjectIdsByWBSCode(wbsCode).then(resp => {
      this.projectIdsUsingWbsCode = resp.data;
      if (this.projectIdsUsingWbsCode.length === 0) {
        this.codes.push({
          code: wbsCode,
          type: this.type
        });
        this.code = null;
        if(this.onWbsCodeModification){
          this.onWbsCodeModification();
        }
      }
    });
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

  isUnique(wbsCode) {
    let codeExists = this.codes.some(wbs => (wbs.code === wbsCode));
    return !codeExists;
  }

  clearErrorMessage() {
    this.projectIdsUsingWbsCode = [];
  }
}

WbsCodesCtrl.$inject = ['ConfirmationDialog', 'UserService', 'ProjectService'];

export default WbsCodesCtrl;
