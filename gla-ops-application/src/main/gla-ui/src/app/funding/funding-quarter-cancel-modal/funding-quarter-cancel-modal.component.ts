import { Component, OnInit, Input } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-funding-quarter-cancel-modal',
  templateUrl: './funding-quarter-cancel-modal.component.html',
  styleUrls: ['./funding-quarter-cancel-modal.component.scss']
})
export class FundingQuarterCancelModalComponent implements OnInit {
  @Input() section: any
  @Input() budget: any
  @Input() unclaimedGrant: any
  @Input() showCapitalGla: boolean
  @Input() showRevenueGla: boolean
  @Input() showCapitalOther: boolean
  @Input() showRevenueOther: boolean
  reasonComments: string

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  onConfirm(): void {
    console.log('closing modal', this.reasonComments)
    this.activeModal.close({action: 'confirm', reason: this.reasonComments})
  }

}
