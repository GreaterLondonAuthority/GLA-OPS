/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './projectOtherFundingModal.js';

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class OtherFundingCtrl extends ProjectBlockCtrl {
  constructor($injector, RepeatingEntityService, ErrorService, ProjectBlockService, ProjectOtherFundingModal, ConfirmationDialog) {
    super($injector);
    this.RepeatingEntityService = RepeatingEntityService;
    this.ErrorService = ErrorService;
    this.ProjectBlockService = ProjectBlockService;
    this.ProjectOtherFundingModal = ProjectOtherFundingModal;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
    super.$onInit();
    this.templateConfig = this.TemplateService.getBlockConfig(this.template, this.projectBlock);
    this.otherFunding = {};
    this.otherFundings = this.projectBlock.otherFundings;
  }

  showProjectOtherFundingModal(otherFunding) {
    this.save(false);
    let modal = this.ProjectOtherFundingModal.show(otherFunding, this.templateConfig);
    modal.result.then((otherFunding) => {
      let apiRequest;
      if (!otherFunding.id) {
        apiRequest = this.RepeatingEntityService.create(this.projectBlock.rootPath, this.project.id, this.projectBlock.id, otherFunding).then(rsp => {
          this.entities.push(rsp.data);
        }).catch(this.ErrorService.apiValidationHandler());
      } else {
        apiRequest = this.RepeatingEntityService.update(this.projectBlock.rootPath, this.project.id, this.projectBlock.id, otherFunding).then(rsp => {
          _.merge(otherFunding, rsp.data);
        }).catch(this.ErrorService.apiValidationHandler());
      }
      apiRequest.then(()=>{
        this.blockSessionStorage.selectedId = '';
        this.$state.reload();
      });
    });
  }

  deleteOtherFunding(otherFunding){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this ' + this.templateConfig.entityName + '?');
    modal.result.then(() => {
      this.RepeatingEntityService.delete(this.projectBlock.rootPath, this.project.id, this.projectBlock.id, otherFunding).then(rsp => {
        _.remove(this.otherFundings, otherFunding);
      }).catch(this.ErrorService.apiValidationHandler());
    });
  }

  save(releaseLock){
    return this.ProjectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, releaseLock).then(rsp => {
      this.projectBlock.validationFailures = rsp.data.validationFailures;
    });
  }

  submit(){
    return this.ProjectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, true);
  }

  back() {
    this.returnToOverview();
  }


}

OtherFundingCtrl.$inject = ['$injector', 'RepeatingEntityService', 'ErrorService', 'ProjectBlockService', 'ProjectOtherFundingModal', 'ConfirmationDialog'];

angular.module('GLA')
  .component('otherFunding', {
    controller: OtherFundingCtrl,
    bindings: {
      project: '<',
      template: '<',
    },
    templateUrl: 'scripts/pages/project/other-funding/otherFunding.html'
  });

