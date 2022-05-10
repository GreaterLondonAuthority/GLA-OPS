import {Injectable} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FundingSourceModalComponent} from "./funding-source-modal.component";

@Injectable({
  providedIn: 'root'
})
export class FundingSourceModalService {

  constructor(private modalService: NgbModal) { }

  show(fundingSource, fundingSources){
    const modal = this.modalService.open(FundingSourceModalComponent)
    modal.componentInstance.fundingSource = fundingSource || {};
    modal.componentInstance.fundingSources = fundingSources || [];
    return modal;
  }
}
