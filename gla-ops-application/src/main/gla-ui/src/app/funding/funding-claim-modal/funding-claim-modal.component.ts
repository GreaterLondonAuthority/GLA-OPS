import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-funding-claim-modal',
  templateUrl: './funding-claim-modal.component.html',
  styleUrls: ['./funding-claim-modal.component.scss']
})
export class FundingClaimModalComponent implements OnInit {
  @Input() section: any
  @Input() budget: any
  @Input() unclaimedGrant: any
  @Input() showCapitalGla: boolean
  @Input() showRevenueGla: boolean
  @Input() showCapitalOther: boolean
  @Input() showRevenueOther: boolean
  @Input() readOnly: boolean
  @Input() canCancelApprovedActivities: boolean
  @Input() cancelApprovedActivitiesFeature: boolean = false
  isClaimable: boolean

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.isClaimable = !this.section.claim || this.section.claim.claimStatus === 'Withdrawn'
  }

}
