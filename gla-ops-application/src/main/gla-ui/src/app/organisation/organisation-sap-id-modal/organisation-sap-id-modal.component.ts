import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NgForm} from "@angular/forms";
import {cloneDeep} from "lodash-es";

@Component({
  selector: 'gla-organisation-sap-id-modal',
  templateUrl: './organisation-sap-id-modal.component.html',
  styleUrls: ['./organisation-sap-id-modal.component.scss']
})
export class OrganisationSapIdModalComponent implements OnInit {

  @Input() addOrUpdate : String;
  @Input() existingSapIds: any[];
  @Input() sapIdModel: any
  isDefaultSapIdAlreadySet =false;
  sapIdInput:any;

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.sapIdInput = cloneDeep(this.sapIdModel)
    this.existingSapIds.forEach(sapId => { if (sapId.defaultSapId) {this.isDefaultSapIdAlreadySet=true} })
  }

  isDuplicate() {
    return this.existingSapIds.find(x => x.sapId != this.sapIdInput.sapId && x.sapId == this.sapIdModel.sapId) != null
  }

  defaultSapIdEnabled() {
    if (this.sapIdModel.defaultSapId) {
      return true
    }
  return !this.isDefaultSapIdAlreadySet
  }

  isFormValid(modalForm: NgForm, sapIdModel) {
    return modalForm.form.valid && sapIdModel.sapId != null && sapIdModel.description != null && !this.isDuplicate()
  }

}
