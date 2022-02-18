import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-version-history-modal',
  templateUrl: './version-history-modal.component.html',
  styleUrls: ['./version-history-modal.component.scss']
})
export class VersionHistoryModalComponent implements OnInit {
  @Input() versionHistory: any;
  @Input() project: any;
  actionedByTitle: string;
  autoApproval: boolean;

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.versionHistory = this.versionHistory || [];
    this.autoApproval = !this.project.stateModel.approvalRequired;
    this.actionedByTitle = this.autoApproval ? 'Saved by' : 'Actioned by';
  }

  versionText(historyItem, isFirstItem) {
    if (this.autoApproval) {
      return isFirstItem ? 'Current version' : `Version ${historyItem.blockVersion}`;
    } else if (historyItem.status === 'UNAPPROVED') {
      return 'Unapproved version';
    } else {
      return historyItem.approvedOnStatus ? `${historyItem.approvedOnStatus} approved v${historyItem.blockVersion}` : `Approved v${historyItem.blockVersion}`;
    }
  }
}
