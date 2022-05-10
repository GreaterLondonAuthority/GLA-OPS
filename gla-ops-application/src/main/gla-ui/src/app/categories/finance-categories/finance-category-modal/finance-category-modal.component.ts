import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FinanceService} from "../finance-service.service";
import {cloneDeep, each, find, isNumber, map, split, toNumber, uniq} from "lodash-es";
import {NgForm} from "@angular/forms";

@Component({
  selector: 'gla-finance-category-modal',
  templateUrl: './finance-category-modal.component.html',
  styleUrls: ['./finance-category-modal.component.scss']
})
export class FinanceCategoryModalComponent implements OnInit {

  @Input() $stateParams: any;
  @Input() category: any = {}
  originalCategory: any
  financeCategoryId: any
  ceCodesString: string
  statuses: any[]
  errorMessage: '';

  constructor(public activeModal: NgbActiveModal, private financeService : FinanceService) { }

  ngOnInit(): void {
    this.originalCategory = cloneDeep(this.category);
    this.category = cloneDeep(this.category);

    this.financeCategoryId = (find(this.category.ceCodes, function(o) { return isNumber(o.financeCategoryId) }) || {}).financeCategoryId;
    this.ceCodesString = this.category.ceCodes ? map(this.category.ceCodes, code => code.id).join(', ') : null;

    this.statuses = [{
      label: 'Allow new forecasts & show in OPS',
      value: 'ReadWrite'
    },{
      label: 'No new forecasts & hide in OPS',
      value: 'Hidden'
    },{
      label: 'No new forecasts & show in OPS',
      value: 'ReadOnly'
    }];
  }

  isFormValid(modalForm: NgForm) {
    return modalForm.form.valid
  }

  onUpdate() {
    let codes = uniq(split(this.ceCodesString, ','));
    let res = [];
    each(codes, (code)=>{
      let codeNo = toNumber(code);
      if(codeNo){
        let existingCode = find(this.originalCategory.ceCodes, {id:codeNo});
        res.push(existingCode || {
          id: codeNo,
          financeCategoryId: this.financeCategoryId
        });
      }
    });
    this.category.ceCodes = res;
    this.errorMessage = null;

    this.financeService[this.category.id?'updateCategory':'createCategory'](this.category).subscribe( () => {
      this.activeModal.close(this.category);
    },(error) => {
      this.errorMessage = error.error.description;
    } );
  }

}
