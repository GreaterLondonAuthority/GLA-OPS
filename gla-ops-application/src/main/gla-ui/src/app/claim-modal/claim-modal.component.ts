import { Component, Input, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ProjectService} from '../project/project.service';
import {ErrorService} from '../shared/error/error.service';
import * as _ from 'lodash';

@Component({
  selector: 'gla-claim-modal',
  templateUrl: './claim-modal.component.html',
  styleUrls: ['./claim-modal.component.scss']
})
export class ClaimModalComponent implements OnInit {

  @Input() config: any
  @Input() claimRequest: any
  modal: any

  claimModalDefaults = {
    claimableAmountTitle: 'Payment amount',
    claimBtnText: 'CLAIM',
    cancelBtnText: 'CANCEL CLAIM'
  };

  constructor(public activeModal: NgbActiveModal,
    private projectService: ProjectService,
    private errorService: ErrorService) { }

  ngOnInit(): void {
    this.modal = _.merge({...this.claimModalDefaults}, this.config);
  }

  onClaim() {
    return this.projectService.claim(this.claimRequest.projectId, this.claimRequest.blockId, this.claimRequest).subscribe(
      () => {
        return this.activeModal.close('claim');
      }, this.errorService.apiValidationHandler(()=> this.activeModal.dismiss('cancel')))
  };

  onCancelClaim() {
    return this.projectService.cancelClaim(this.claimRequest.projectId, this.claimRequest.blockId, this.claimRequest.id).subscribe(
      () => {
        return this.activeModal.close('cancel');
      }, (err) => {
        this.errorService.apiValidationHandler(()=> this.activeModal.dismiss('cancel'));
        console.error(err);
      })
  }

}
