import { Injectable } from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {OrganisationSapIdModalComponent} from "./organisation-sap-id-modal.component";

@Injectable({
  providedIn: 'root'
})
export class OrganisationSapIdModalService {

  constructor(private modalService: NgbModal) { }

  show(addOrUpdate, existingSapIds, sapIdModel){
    const modal = this.modalService.open(OrganisationSapIdModalComponent)
    modal.componentInstance.addOrUpdate = addOrUpdate;
    modal.componentInstance.existingSapIds = existingSapIds;
    modal.componentInstance.sapIdModel = sapIdModel || {};
    return modal;
  }

}
