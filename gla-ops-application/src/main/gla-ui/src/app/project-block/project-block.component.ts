import {AfterViewInit, Directive, Injector, Input, OnInit} from '@angular/core';
import {ConfirmationDialogService} from "../shared/confirmation-dialog/confirmation-dialog.service";
import {VersionHistoryModalComponent} from "./version-history-modal/version-history-modal.component";
import {UserService} from "../user/user.service";
import {ProjectBlockService} from "./project-block.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ToastrUtilService} from "../shared/toastr/toastr-util.service";
import {TemplateService} from "../template/template.service";
import {ErrorService} from "../shared/error/error.service";
import {find, orderBy, some} from "lodash-es";
import {NavigationService} from "../navigation/navigation.service";
import {ProjectService} from "../project/project.service";
import {DatePipe} from "@angular/common";
import {LoadingMaskService} from "../shared/loading-mask/loading-mask.service";
import {SessionService} from "../session/session.service";
import {forkJoin, Observable, ReplaySubject} from "rxjs";
import {finalize, take} from "rxjs/operators";

@Directive()
export abstract class ProjectBlockComponent implements OnInit, AfterViewInit {

  @Input() projectBlock: any;
  @Input() project: any;
  @Input() history: any[];
  @Input() isBlockRevertEnabled = false;
  @Input() $stateParams: any;
  $ctrl = this;
  editable = false;
  readOnly = true;
  userService: UserService;
  confirmationDialog: ConfirmationDialogService;
  projectBlockService: ProjectBlockService;
  ngbModal: NgbModal;
  toastrUtil: ToastrUtilService;
  templateService: TemplateService;
  errorService: ErrorService;
  navigationService: NavigationService;
  projectService: ProjectService;
  datePipe: DatePipe;

  private initialised = false;
  requestsQueue: Observable<any>[];
  private httpReqs: ReplaySubject<boolean>[] = [];
  blockHistory: any[];
  active: boolean;
  blockId: number;
  currentHistoryItem: any;
  actionedBy: string;
  lockDetails: any;
  infoMessage: string;
  deletable: boolean;
  revertable: boolean;
  version: string;
  approved: boolean;
  lockedByCurrentUser: boolean;
  blockVersion: number;
  sessionService: SessionService;
  blockSessionStorage: any;
  loadingMaskService: LoadingMaskService;

  constructor(private injector:Injector) {
    this.projectService = injector.get(ProjectService);
    this.userService = injector.get(UserService);
    this.confirmationDialog = injector.get(ConfirmationDialogService);
    this.projectBlockService = injector.get(ProjectBlockService);
    this.ngbModal = injector.get(NgbModal);
    this.toastrUtil = injector.get(ToastrUtilService);
    // this.$q = injector.get('$q');
    this.datePipe = injector.get(DatePipe);
    this.templateService = injector.get(TemplateService);
    this.errorService = injector.get(ErrorService);
    this.navigationService = injector.get(NavigationService);
    this.sessionService = injector.get(SessionService);
    this.loadingMaskService = injector.get(LoadingMaskService);
  }

  /**
   * Called by angular unless overridden in superclass and needs to be called manually
   */
  ngOnInit() {
    this.blockHistory = orderBy(this.history || [], 'blockVersion', 'desc');
    this.active = (this.project.statusType.toLowerCase() === 'active');
    this.blockVersion = this.$stateParams.version ? +this.$stateParams.version : null;
    this.blockId = this.projectBlock.id;
    this.currentHistoryItem = find(this.blockHistory, {blockId: this.blockId});
    if (this.currentHistoryItem && this.currentHistoryItem.actionedBy) {
      this.actionedBy = this.currentHistoryItem.actionedBy;
    }

    this.lockDetails = this.projectBlock.lockDetails;
    this.infoMessage = this.projectBlock.infoMessage;
    this.editable = this.projectBlock.editable;

    // if permission proj.revert.block.*
    let hasRevertBlockPermission = this.userService.hasPermission('proj.revert.block', this.project.organisation.id);

    this.deletable = (this.projectBlock.allowedActions || []).indexOf('DELETE') != -1 && hasRevertBlockPermission;
    this.revertable = this.isBlockRevertEnabled && this.projectBlock.blockReversionAllowed && hasRevertBlockPermission;

    if ((this.revertable || this.hasApprovedVersion()) && this.projectBlock) {
      if (!this.project.stateModel.approvalRequired) {
        this.version = `Last updated on ${this.datePipe.transform(this.projectBlock.lastModified, 'dd/MM/yyyy')} by ${this.actionedBy}`;
      } else if (this.projectBlock.versionNumber && this.projectBlock.approvalTime) {
        this.version = `Version ${this.projectBlock.versionNumber} Approved on ${this.datePipe.transform(this.projectBlock.approvalTime, 'dd/MM/yyyy')}`;
        if(this.projectBlock.approvedOnStatus){
          this.version = `${this.projectBlock.approvedOnStatus} ${this.version}`
        }
        this.approved = true;
      } else if (this.projectBlock.lastModified) {
        this.version = `Unapproved Version Saved on ${this.datePipe.transform(this.projectBlock.lastModified, 'dd/MM/yyyy')}`;
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

    this.blockSessionStorage = this.sessionService.getBlockSessionStorage(this.blockId);
    if(!this.blockSessionStorage){
      this.sessionService.setBlockSessionStorage(this.blockId, {});
      this.blockSessionStorage = this.sessionService.getBlockSessionStorage(this.blockId);
    }

    if(this.$stateParams.afterEdit){
      this.showBlockCompletenessToast();
    }

    this.initialised = true;
  }

  ngAfterViewInit(){
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
      this.projectService.getProjectBlock(this.project.id, this.blockId, true).subscribe(resp => {
          //Block might be a new one if 'unapproved' block is created on request
          const block:any = resp;
          //Get lock and reload to show in edit mode

          this.$stateParams.blockId = block.id;
          this.$stateParams.afterEdit = false;

          this.navigationService.goToCurrentUiRouterState(this.$stateParams, {
            reload: true,
            inherit: false
          });
        });
    } else {
      throw new Error(`Can't edit this block: ${this.projectBlock.blockDisplayName}`);
    }
  }


  /**
   * Unlock block
   */
  unlockBlock() {
    return this.projectService.unlockBlock(this.project.id, this.projectBlock.id).subscribe(resp => {
        this.readOnly = true;
      });
  }

  /**
   * Clear the block session storage
   */
  clearBlockSessionStorage() {
    this.sessionService.setBlockSessionStorage(this.blockId, null);
  }

  /**
   * Return to project overview
   */
  returnToOverview() {
    this.clearBlockSessionStorage();
    this.navigationService.goToUiRouterState('project-overview', {
        projectId: this.project.id,
      }, {
        reload: true
      });
  }

  back() {
    if (this.readOnly) {
      this.returnToOverview();
    } else {
      this.submit();
    }
  }

  viewHistory() {
    const modal = this.ngbModal.open(VersionHistoryModalComponent, { size: 'lg' });
    modal.componentInstance.versionHistory = this.blockHistory;
    modal.componentInstance.project = this.project;

    modal.result.then((block) => {
      this.$stateParams.version = block.blockVersion;
      this.navigationService.goToCurrentUiRouterState(this.$stateParams, {reload: true})
    }, ()=>{});
  }

  deleteBlock() {
    let modal = this.confirmationDialog.show({
      message: 'Are you sure you want to undo all the changes made since the last time the block was approved?',
      approveText: 'YES',
      dismissText: 'CLOSE'
    });

    modal.result.then(() => {
      this.projectBlockService.deleteBlock(this.projectBlock.projectId, this.projectBlock.id).subscribe(() => {
        this.toastrUtil.success('Unapproved changes undone');
        this.$stateParams.version = null;
        let previousBlock = find(this.blockHistory, {blockVersion: this.projectBlock.versionNumber - 1});
        this.$stateParams.blockId = previousBlock.blockId;
        this.navigationService.goToCurrentUiRouterState(this.$stateParams, {reload: true})
      }, rsp => {
        let error = rsp.data || {};
        let msg = error.description || 'Block can\'t be deleted';
        this.confirmationDialog.show({message:msg, approveText:'Ok', showDismiss:false, showIcon:false})
      });
    });
  }


  revertBlock() {
    let modal = this.confirmationDialog.show({
      message: 'Are you sure you want to undo all changes made to this block?<br><br>This cannot be reverted.',
      approveText: 'YES',
      dismissText: 'CLOSE'
    });
    modal.result.then(() => {
      this.projectBlockService.revertBlock(this.projectBlock.projectId, this.projectBlock.id).subscribe((rsp:any) => {
        this.toastrUtil.success('Unapproved changes undone');
        this.$stateParams.version = null;
        this.$stateParams.blockId = rsp.id;
        this.navigationService.goToCurrentUiRouterState(this.$stateParams, {reload: true})
      }, rsp => {
        let error = rsp.data || {};
        let msg = error.description || 'Block changes can\'t be deleted';
        this.confirmationDialog.show({message:msg, approveText:'Ok', showDismiss:false, showIcon:false})
      });
    });
  }

  hasApprovedVersion(){
    return some(this.blockHistory, item => item.status !== 'UNAPPROVED');
  }

  abstract submit(): Observable<any>;

  /**
   * Add observables to request queue so stop editing waits
   * for them to complete before releasing the lock
   * @param observable
   */
  withLock(observable: Observable<any>): Observable<any>{
    let rs = new ReplaySubject<boolean>(1);
    this.httpReqs.push(rs);
    return observable.pipe(finalize(() => rs.next(true)));
  }

  stopEditing(): Promise<any> {
    this.loadingMaskService.showLoadingMask(true);
    let done = async () => {
      let rsp = await this.submit().toPromise();
      this.httpReqs.forEach(s => s.unsubscribe());
      this.httpReqs = [];
      this.readOnly = true;
      this.$stateParams.afterEdit = true;
      this.navigationService.goToCurrentUiRouterState(this.$stateParams, {reload: true});
      return rsp;
    }

    if(this.httpReqs.length){
      let stopEditingObservable = forkJoin(this.httpReqs.map(repSub => repSub.asObservable().pipe(take(1))));
      return stopEditingObservable.toPromise().then(done);
    }
    return done();
  }

  showBlockCompletenessToast(){
    if (this.projectBlock && this.projectBlock.complete) {
      this.toastrUtil.success('Saved: Section completed');
    } else {
      this.toastrUtil.warning('Saved: Section incomplete');
    }
  }
}
