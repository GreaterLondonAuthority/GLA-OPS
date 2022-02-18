import { Component, OnInit } from '@angular/core';
import {OrganisationService} from '../organisation/organisation.service';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-organisation-request-access-modal',
  templateUrl: './organisation-request-access-modal.component.html',
  styleUrls: ['./organisation-request-access-modal.component.scss']
})
export class OrganisationRequestAccessModalComponent implements OnInit {

  orgCode: String
  orgName: String

  constructor(public activeModal: NgbActiveModal,
    private organisationService: OrganisationService) { }

  ngOnInit(): void {
  }

  lookupOrgCode() {
    console.log(this.orgCode)
    if (!this.orgCode) {
      return;
    }
    this.organisationService.lookupOrgNameByCode(this.orgCode)
    .subscribe((response) => {
      this.orgName = response
    }, (error) => {
      this.orgName = null
    })
  }

  closeModal(result) {
    this.activeModal.close(result)
  }

}
