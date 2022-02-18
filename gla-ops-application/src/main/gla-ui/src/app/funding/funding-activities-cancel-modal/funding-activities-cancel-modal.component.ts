import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'gla-funding-activities-cancel-modal',
  templateUrl: './funding-activities-cancel-modal.component.html',
  styleUrls: ['./funding-activities-cancel-modal.component.scss']
})
export class FundingActivitiesCancelModalComponent implements OnInit {
  @Input() section: any
  @Input() activity: any
  @Input() showCapitalGla: boolean
  @Input() showRevenueGla: boolean
  @Input() showCapitalOther: boolean
  @Input() showRevenueOther: boolean
  reasonComments: string

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  onConfirm(): void {
    this.activeModal.close({action: 'confirm', reason: this.reasonComments})
  }

}
