import {Component, Input, OnInit} from '@angular/core';
import {NgForm} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {find} from "lodash-es";

@Component({
  selector: 'gla-funding-source-modal',
  templateUrl: './funding-source-modal.component.html',
  styleUrls: ['./funding-source-modal.component.scss']
})
export class FundingSourceModalComponent implements OnInit {

  @Input() fundingSource: any
  @Input() fundingSources: any
  originalFundingSource: any

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.fundingSource = this.fundingSource || {};
    this.originalFundingSource = find(this.fundingSources, {
      fundingSource: this.fundingSource.fundingSource
    });
  }

  isNameUnique(fundingSource) {
    return !this.fundingSources.find(fs => fs.fundingSource === fundingSource.fundingSource && fs != this.originalFundingSource);
  }

  isFormValid(modalForm: NgForm, fundingSource) {
    return modalForm.form.valid && this.isNameUnique(fundingSource) &&
           fundingSource.fundingSource && fundingSource.showFunderName !=null && fundingSource.showDescription !=null
  }
}
