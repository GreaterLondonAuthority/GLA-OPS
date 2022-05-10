import {VersionHistoryModalComponent} from '../../../../../gla-ui/src/app/project-block/version-history-modal/version-history-modal.component';

/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ProjectBlockCtrl {
  constructor($injector){
    this.ProjectService = $injector.get('ProjectService');
    this.UserService = $injector.get('UserService');
    this.$log = $injector.get('$log');
    this.$state = $injector.get('$state');
    this.$stateParams = $injector.get('$stateParams');
    this.$rootScope = $injector.get('$rootScope');
    this.$sessionStorage = $injector.get('$sessionStorage');
    this.$location = $injector.get('$location');
    this.$anchorScroll = $injector.get('$anchorScroll');
    this.ConfirmationDialog = $injector.get('ConfirmationDialog');
    this.ProjectBlockService = $injector.get('ProjectBlockService');
    this.NgbModal = $injector.get('NgbModal');
    this.ToastrUtil = $injector.get('ToastrUtil');
    this.$q = $injector.get('$q');
    this.dateFilter = $injector.get('dateFilter');
    this.TemplateService = $injector.get('TemplateService');
    this.ErrorService = $injector.get('ErrorService');
  }

  /**
   * Called by angular unless overridden in superclass and needs to be called manually
   */
  $onInit() {
    //TODO we shouldn't use resolves (this.$state.$current.local) indirectly.
    this.blockHistory = _.orderBy(this.$state.$current.locals.globals.history || [], 'blockVersion', 'desc');
    this.isBlockRevertEnabled = this.$state.$current.locals.globals.isBlockRevertEnabled;
    let project = this.project || this.$state.$current.locals.globals.project;
    this.project = project;
    this.active = (project.statusType.toLowerCase() === 'active');
    this.blockVersion = this.$state.params.version ? +this.$state.params.version : null;

    if (this.$state.$current.locals.globals.block) {
      //Means specific version is loaded
      this.projectBlock = this.$state.$current.locals.globals.block;
      this.blockId = this.projectBlock.id;
    } else {
      //TODO this 'else' probably is not needed any more because we load block for everything
      alert('Unexpected call, project block should be loaded by api all the time');
      this.blockId = this.$state.params.blockId;
      this.projectBlock = _.find(this.project.projectBlocksSorted, {id: this.blockId});
    }

    this.currentHistoryItem = _.find(this.blockHistory, {blockId: this.blockId});
    if (this.currentHistoryItem && this.currentHistoryItem.actionedBy) {
      this.actionedBy = this.currentHistoryItem.actionedBy;
    }


    this.lockDetails = this.projectBlock.lockDetails;
    this.infoMessage = this.projectBlock.infoMessage;
    this.editable = this.projectBlock.editable;

    // if permission proj.revert.block.*
    let hasRevertBlockPermission = this.UserService.hasPermission('proj.revert.block', this.project.organisation.id);

    this.deletable = (this.projectBlock.allowedActions || []).indexOf('DELETE') != -1 && hasRevertBlockPermission;
    this.revertable = this.isBlockRevertEnabled && this.projectBlock.blockReversionAllowed && hasRevertBlockPermission;
    this.$log.log('isDeletable', this.deletable);

    this.$log.log('project block', this.projectBlock);


    if ((this.revertable || this.hasApprovedVersion()) && this.projectBlock) {
      if (!this.project.stateModel.approvalRequired) {
        this.version = `Last updated on ${this.dateFilter(this.projectBlock.lastModified, 'dd/MM/yyyy')} by ${this.actionedBy}`;
      } else if (this.projectBlock.versionNumber && this.projectBlock.approvalTime) {
        this.version = `Version ${this.projectBlock.versionNumber} Approved on ${this.dateFilter(this.projectBlock.approvalTime, 'dd/MM/yyyy')}`;
        if(this.projectBlock.approvedOnStatus){
          this.version = `${this.projectBlock.approvedOnStatus} ${this.version}`
        }
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

    if(this.$stateParams.afterEdit){
      this.showBlockCompletenessToast();
    }

    this.initialised = true;
  }


  jumpTo(id) {
    this.$location.hash(id);
    this.$anchorScroll();
  }

  $postLink(){
    if(!this.initialised){
      this.throwError('Subclasses of ProjectBlockCtrl overriding $onInit should call super.$onInit()')
    }

    if(!this.project){
      this.throwError(`Subclasses of ProjectBlockCtrl should have 'project' in its resolve or in bindings`)
    }
  }

  throwError(msg){
    alert(msg);
    throw new Error(msg);
  }

  edit() {
    if (this.editable) {
      this.ProjectService.getProjectBlock(this.project.id, this.blockId, true)
        .then(resp => {
          //Block might be a new one if 'unapproved' block is created on request
          const block = resp.data;
          //Get lock and reload to show in edit mode

          this.$state.params.blockId = block.id;
          this.$state.params.afterEdit = false;

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
  returnToOverview() {
    this.clearBlockSessionStorage();
    this.$state.go('project-overview', {
      projectId: this.project.id,
    }, {
      reload: true
    });
  }

  viewHistory() {
    const modal = this.NgbModal.open(VersionHistoryModalComponent, { size: 'lg' });
    modal.componentInstance.versionHistory = this.blockHistory;
    modal.componentInstance.project = this.project;

    modal.result.then((block) => {
      this.$stateParams.version = block.blockVersion;
        this.$state.go(this.$state.current, this.$stateParams, {reload: true});
    }, ()=>{});
  }

  deleteBlock() {
    let modal = this.ConfirmationDialog.show({
      message: 'Are you sure you want to undo all the changes made since the last time the block was approved?',
      approveText: 'YES',
      dismissText: 'CLOSE'
    });

    modal.result.then(() => {
      this.ProjectBlockService.deleteBlock(this.projectBlock.projectId, this.projectBlock.id).then(() => {
        this.ToastrUtil.success('Unapproved changes undone');
        this.$stateParams.version = null;
        let previousBlock = _.find(this.blockHistory, {blockVersion: this.projectBlock.versionNumber - 1});
        this.$stateParams.blockId = previousBlock.blockId;
        this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
      }).catch(rsp => {
          let error = rsp.data || {};
          let msg = error.description || 'Block can\'t be deleted';
          this.ConfirmationDialog.show({message:msg, approveText:'Ok', showDismiss:false, showIcon:false})
        })
    });
  }

  revertBlock() {
    let modal = this.ConfirmationDialog.show({
      message: 'Are you sure you want to undo all changes made to this block?<br><br>This cannot be reverted.',
      approveText: 'YES',
      dismissText: 'CLOSE'
    });
    modal.result.then(() => {
      this.ProjectBlockService.revertBlock(this.projectBlock.projectId, this.projectBlock.id).then((rsp) => {
        this.ToastrUtil.success('Unapproved changes undone');
        this.$stateParams.version = null;
        this.$stateParams.blockId = rsp.data.id;
        this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
      }).catch(rsp => {
        let error = rsp.data || {};
        let msg = error.description || 'Block changes can\'t be deleted';
        this.ConfirmationDialog.show({message:msg, approveText:'Ok', showDismiss:false, showIcon:false})
      })
    });
  }

  hasApprovedVersion(){
    return _.some(this.blockHistory, item => item.status !== 'UNAPPROVED');
  }

  stopEditing() {
    if (!this.submit) {
      let errorMsg = 'submit method must be implemented in each of the project block';
      console.error(errorMsg);
      alert(errorMsg);
    } else {
      this.$rootScope.showGlobalLoadingMask = true;
      // TODO refactored to use this.$q here instead of inside each individual submit method in every block: GLA-21696
      // this.$q.all(this.requestsQueue).then(() => {
      let p = this.submit();
      if (p && p.then) {
        p.then(() => {
          this.readOnly = true;
          this.$stateParams.afterEdit = true;
          this.$state.go(this.$state.current, this.$stateParams, {reload: true});
        }).catch(err => {
          console.error(err);
        });
      } else {
        this.$rootScope.showGlobalLoadingMask = false;
        console.warn('Should return a promise.');
      }
    }
  }

  showBlockCompletenessToast(){
    if (this.projectBlock && this.projectBlock.complete) {
      this.ToastrUtil.success('Saved: Section completed');
    } else {
      this.ToastrUtil.warning('Saved: Section incomplete');
    }
  }
}

export default ProjectBlockCtrl;
