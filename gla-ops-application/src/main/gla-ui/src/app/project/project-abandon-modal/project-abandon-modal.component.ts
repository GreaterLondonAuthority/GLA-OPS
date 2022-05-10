import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {TransitionService} from "../transition.service";
import {ProjectService} from "../project.service";
import {ProjectAbandonModalService} from "./project-abandon-modal.service";
import {Observable} from "rxjs";
import {LoadingMaskService} from "../../shared/loading-mask/loading-mask.service";

@Component({
  selector: 'gla-project-abandon-modal',
  templateUrl: './project-abandon-modal.component.html',
  styleUrls: ['./project-abandon-modal.component.scss']
})
export class ProjectAbandonModalComponent implements OnInit {

  @Input() project: any
  @Input() transition: any
  @Input() errorMsg: string
  @Input() isRejecting = false
  status: any;
  dataBlock: any;
  showRejectBtn = false;
  showAbandonBtn = false;
  showRequestAbandonBtn = false;
  reinstateProject = false;
  completeProject = false;
  modal: any;
  loading = true;

  constructor(public activeModal: NgbActiveModal,
              private transitionService: TransitionService,
              private projectService: ProjectService,
              private projectAbandonModalService: ProjectAbandonModalService,
              private loadingMaskService: LoadingMaskService) { }

  ngOnInit(): void {
    this.status = this.transitionService.status(this.project);
    this.transition = this.transition || this.transitionService.getTransitionToClose(this.project);
    this.dataBlock = {};
    this.loadingMaskService.showLoadingMask(true);
    this.getHintMessage((hintMessage)=>{
      this.updateView(hintMessage);
      this.loading = false;
      this.loadingMaskService.showLoadingMask(false);
    });
  }

  //Get transition and validate to get hint message.
  getHintMessage(callback) {
    if (this.errorMsg) {
      return callback(this.errorMsg);
    }
    let closeTransition = this.transitionService.getTransitionToClose(this.project);
    let transitionToValidate = this.transition || closeTransition;
    if (transitionToValidate) {
      //Some transitions are allowed but requires extra validation
      return this.projectService.validateTransition(this.project.id, transitionToValidate)
        .subscribe(() => callback(), err => {
          callback(err.error.description);
        })
    } else if (this.project.statusType === 'Submitted') {
     return callback('Submitted projects have to be withdrawn before you can amend or abandon them.');
    }
    return callback(null);
  }

  /**
   * Sets correct modal view based on allowed transitions.
   */
  updateView(hintMessage) {
    let transition = this.transition || {};
    this.showRejectBtn = !hintMessage && transition.subStatus === 'Rejected';
    this.showAbandonBtn = !hintMessage && transition.subStatus === 'Abandoned';
    this.showRequestAbandonBtn = !hintMessage && transition.subStatus === 'AbandonPending';
    this.dataBlock.requestAbandon = this.showRequestAbandonBtn;
    this.reinstateProject = this.project.statusType === 'Closed';
    this.completeProject = transition && transition.subStatus === 'Completed';
    let modalsConfig = this.projectAbandonModalService.getModalConfigurations();
    if (this.completeProject) {
      this.modal = modalsConfig.complete;
      if (hintMessage) {
        this.modal.actionBtnName = null;
        this.modal.hintMessage = hintMessage;
      }
    } else if (this.reinstateProject) {
      this.modal = modalsConfig.reinstate;
      if (this.project.hasReclaimedPayments) {
        this.modal.warning = 'This project contains authorised or pending reclaim(s). Changes made to this project once reinstated may affect calculations leading to errors. It is not recommended you reinstate this project.'
      }
      if (hintMessage) {
        this.modal.actionBtnName = null;
        this.modal.hintMessage = hintMessage;
      }
    } else if (this.showAbandonBtn) {
      this.modal = modalsConfig.abandon;
    } else if (this.showRejectBtn) {
      this.modal = modalsConfig.reject;
    } else if (this.showRequestAbandonBtn) {
      this.modal = modalsConfig.requestAbandon;
    } else {
      if (this.isRejecting) {
        this.modal = modalsConfig.warningReject;
      } else {
        this.modal = modalsConfig.warning;
      }
      if (hintMessage) {
        this.modal.hintMessage = hintMessage;
      }
    }
  };

  action() {
    let p: Observable<Object>;
    if (this.completeProject){
      p = this.projectService.completeProject(this.project.id, this.dataBlock.reason);
    }else if (this.reinstateProject){
      p = this.projectService.reinstateProject(this.project.id, this.dataBlock.reason);
    } else if (this.showRequestAbandonBtn) {
      p = this.projectService.requestAbandon(this.project.id, this.dataBlock.reason);
    } else if(this.showRejectBtn){
      p = this.projectService.reject(this.project.id, this.dataBlock.reason);
    } else {
      p = this.projectService.abandon(this.project.id, this.dataBlock.reason);
    }
    return p.subscribe(() => this.activeModal.close(this.dataBlock), err => {
      this.updateView(err.error.description);
      console.log(err);
    })
  };
}
