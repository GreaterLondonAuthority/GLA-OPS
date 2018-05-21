/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ProjectBlockCtrl {
  constructor(project, $injector) {
    this.ProjectService = $injector.get('ProjectService');
    this.$log = $injector.get('$log');
    this.$state = $injector.get('$state');
    this.$stateParams = $injector.get('$stateParams');
    this.$rootScope = $injector.get('$rootScope');
    this.$sessionStorage = $injector.get('$sessionStorage');
    this.ConfirmationDialog = $injector.get('ConfirmationDialog');
    this.ProjectBlockService = $injector.get('ProjectBlockService');
    this.VersionHistoryModal = $injector.get('VersionHistoryModal');
    this.ToastrUtil = $injector.get('ToastrUtil');
    this.$q = $injector.get('$q');
    this.dateFilter = $injector.get('dateFilter');
    this.MessageModal = $injector.get('MessageModal');
    this.blockHistory = _.orderBy(this.$state.$current.locals.globals.history || [], 'blockVersion', 'desc');

    this.project = project;
    this.active = (project.status.toLowerCase() === 'active');
    this.blockVersion = this.$state.params.version ? +this.$state.params.version : null;

    if (this.$state.$current.locals.globals.block) {
      //Means specific version is loaded
      this.projectBlock = this.$state.$current.locals.globals.block;
      this.blockId = this.projectBlock.id;
    } else {
      this.blockId = this.$state.params.blockId;
      this.projectBlock = _.find(this.project.projectBlocksSorted, {id: this.blockId});
    }

    this.currentHistoryItem = _.find(this.blockHistory, {blockId: this.blockId});
    if(this.currentHistoryItem && this.currentHistoryItem.actionedBy){
      this.actionedBy = this.currentHistoryItem.actionedBy;
    }


    this.lockDetails = this.projectBlock.lockDetails;
    this.infoMessage = this.projectBlock.infoMessage;

    this.editable = (this.projectBlock.allowedActions || []).indexOf('EDIT') != -1;
    this.deletable = (this.projectBlock.allowedActions || []).indexOf('DELETE') != -1;
    this.$log.log('isDeletable', this.deletable);

    this.$log.log('project block', this.projectBlock);

    if (this.active && this.projectBlock) {
      if(this.project.autoApproval){
        this.version = `Last updated on ${this.dateFilter(this.projectBlock.lastModified, 'dd/MM/yyyy')} by ${this.actionedBy}`;
      }else if (this.projectBlock.versionNumber && this.projectBlock.approvalTime) {
        this.version = `Version ${this.projectBlock.versionNumber} Approved on ${this.dateFilter(this.projectBlock.approvalTime, 'dd/MM/yyyy')}`;
        this.approved = true;
      } else if (this.projectBlock.lastModified) {
        this.version = `Unapproved Version Saved on ${this.dateFilter(this.projectBlock.lastModified, 'dd/MM/yyyy')}`;
        this.approved = false;
      }
    }

    this.lockedByCurrentUser = this.lockDetails && this.editable;
    if (this.lockedByCurrentUser) {
      this.readOnly = false;
    } else {
      this.readOnly = true;
    }

    this.requestsQueue = [];

    this.blockSessionStorage =
      this.$sessionStorage[this.blockId] ?
        this.$sessionStorage[this.blockId] :
        this.$sessionStorage[this.blockId] = {};
  }

  edit() {
    if (this.editable) {
      this.ProjectService.getProjectBlock(this.project.id, this.blockId, true)
        .then(resp => {
          //Block might be a new one if 'unapproved' block is created on request
          const block = resp.data;
          //Get lock and reload to show in edit mode
          //TODO why do we reload?
          this.$state.params.blockId = block.id;
          this.$state.transitionTo(this.$state.current, this.$state.params, {
            reload: true,
            inherit: false
          });
        });
    } else {
      this.$log.error(`Can't edit:`, this.projectBlock);
      throw new Error(`Can't edit this block: ${this.projectBlock.blockDisplayName}`);
    }
  }

  /**
   * Used to avoid editing block after save (and lock release)
   * @param promise Request which don't release locks (autosave === true)
   * @returns {Promise} Returns the same promise
   */
  addToRequestsQueue(promise) {
    this.requestsQueue.push(promise);
    promise.finally(() => _.remove(this.requestsQueue, promise));
    return promise;
  }

  /**
   * Unlock block
   */
  unlockBlock() {
    return this.ProjectService.unlockBlock(this.project.id, this.projectBlock.id)
      .then(resp => {
        this.readOnly = true;
      });
  }

  /**
   * Clear the block session storage
   */
  clearBlockSessionStorage() {
    this.$sessionStorage[this.blockId] = null;
  }

  /**
   * Return to project overview
   */
  returnToOverview(projectSectionSaved) {
    this.clearBlockSessionStorage();
    this.$state.go('project.overview', {
      projectId: this.project.id,
      projectSectionSaved: projectSectionSaved
    }, {
      reload: true
    });
  }

  viewHistory() {
    let modal = this.VersionHistoryModal.show(this.blockHistory, this.project);
    modal.result.then(block => {
      this.$stateParams.version = block.blockVersion;
      this.$state.go(this.$state.current, this.$stateParams, {reload: true});
    })
  }

  deleteBlock() {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the edited version of this block?<br>This deletion cannot be reverted.');
    modal.result
      .then(() => {
        this.ProjectBlockService.deleteBlock(this.projectBlock.projectId, this.projectBlock.id).then(() => {
          this.ToastrUtil.success('Block deleted');
          this.$stateParams.version = null;
          let previousBlock = _.find(this.blockHistory, {blockVersion: this.projectBlock.versionNumber - 1});
          this.$stateParams.blockId = previousBlock.blockId;
          this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
        })
          .catch(rsp => {
            let error = rsp.data || {};
            let msg = error.description || "Block can't be deleted";
            this.MessageModal.show({
              message: msg
            })
          })
      });
  }


}

export default ProjectBlockCtrl;
