import {Component, Inject, Input, OnInit} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
import {FinanceCategoryModalComponent} from "./finance-category-modal/finance-category-modal.component";
import {FinanceService} from "./finance-service.service";
import {map, slice, sortBy} from "lodash-es";

@Component({
  selector: 'gla-finance-categories',
  templateUrl: './finance-categories.component.html',
  styleUrls: ['./finance-categories.component.scss']
})
export class FinanceCategoriesComponent implements OnInit {

  @Input() stateParams: any;
  @Input() state: any;
  @Input() financeCategories: any[];
  canAddBroadcast: any
  textMapping: any
  showAllCodes: any;

  constructor(private ngbModal: NgbModal,
              private financeService: FinanceService,
              private toastrUtil: ToastrUtilService) {
  }

  ngOnInit(): void {
    this.textMapping = {
      ReadWrite: 'Allow new forecasts & show in OPS',
      Hidden: 'No new forecasts & hide in OPS',
      ReadOnly: 'No new forecasts & show in OPS'
    };
    this.financeCategories = this.processCategories(this.financeCategories);
  }

  refresh() {
  }

  processCategories(categories) {
    let data = categories;
    map(data, category => {
      let sortedCeCodes = sortBy(category.ceCodes,'id');

      category.shortCodes = map(slice(sortedCeCodes, 0, 6), 'id').join(', ');
      if(sortedCeCodes && sortedCeCodes.length > 6){
        category.longCodes = map(sortedCeCodes, 'id').join(', ');
      }
    });
    return sortBy(data, [category => category.text.toLowerCase()]);
  }

  editRow(category){
    setTimeout(() => {
      let modal = this.ngbModal.open(FinanceCategoryModalComponent, {size: 'md'});
      modal.componentInstance.category = category;

      modal.result.then((data) => {
        this.toastrUtil.success('Row updated');
        this.financeService.getFinanceCategories(true, {}).subscribe((rsp: any) => {
          this.financeCategories = rsp;
        });
      }, () => {
      });
    });
  }

  addRow(){
    let modal = this.ngbModal.open(FinanceCategoryModalComponent, { size: 'md' });
    modal.componentInstance.category = {};
    modal.result.then((data) => {
      this.toastrUtil.success('New row added');
      this.financeService.getFinanceCategories(true, {}).subscribe((rsp : any) => {
        this.financeCategories = rsp;
      }, () => {});
    }, () => {});
  }

}
