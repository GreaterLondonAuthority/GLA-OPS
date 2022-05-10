import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-project-payment-confirm-modal',
  templateUrl: './project-payment-confirm-modal.component.html',
  styleUrls: ['./project-payment-confirm-modal.component.scss']
})
export class ProjectPaymentConfirmModalComponent  {

  @Input() config: any;
  showPaymentsOnlyBtn: boolean;
  showApproveChangesBtn: boolean;
  reasonComments: any;
  userComment: any;
  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  onRadioValueChange(value){
    this.showPaymentsOnlyBtn = value === 'showPaymentsOnlyBtn' ? true : false;
    this.showApproveChangesBtn = value === 'showApproveChangesBtn' ? true : false;
  }

  closeModal(action) {
    let data = {
      action: action,
      reason: this.reasonComments,
      userComment: this.userComment
    }
    this.activeModal.close(data)
  }

  isModalValid(userCommentRequired, userComment){
    return !userCommentRequired || userComment
  }

  isReasonCommentsPopulated() {
    return this.reasonComments;
  }
}
