import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-funding-activities-claim-modal',
  templateUrl: './funding-activities-claim-modal.component.html',
  styleUrls: ['./funding-activities-claim-modal.component.scss']
})
export class FundingActivitiesClaimModalComponent implements OnInit {

  @Input() section: any
  @Input() activity: any
  @Input() budget: any
  @Input() unclaimedGrant: number
  @Input() showCapitalGla: boolean
  @Input() showRevenueGla: boolean
  @Input() showCapitalOther: boolean
  @Input() showRevenueOther: boolean
  @Input() readOnly: boolean

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

}
