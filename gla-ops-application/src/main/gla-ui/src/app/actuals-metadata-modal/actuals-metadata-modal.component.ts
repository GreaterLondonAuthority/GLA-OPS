import { Component, Input, OnInit } from '@angular/core';
declare var moment: any;
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import { NumberUtilsService } from '../utils/number-utils.service';

@Component({
  selector: 'gla-actuals-metadata-modal',
  templateUrl: './actuals-metadata-modal.component.html',
  styleUrls: ['./actuals-metadata-modal.component.scss']
})
export class ActualsMetadataModalComponent implements OnInit {

  @Input() data: any
  @Input() title: String
  @Input() spendType: any
  @Input() isCR: boolean
  isCRFormated: boolean
  source: any
  open: boolean


  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.open = true
    this.isCRFormated = !!this.spendType
    this.source = {
      PCS: 'PCS Import',
      WebUI: 'Manual'
    }
    this.data.forEach((item: any) => {
      if (item.date) {
        item.date = moment(item.date, 'DD/M/YYYY').toDate();
      }
    });
    this.data = this.data.filter((transaction) => {
      if (!this.spendType) {
        return true;
      }


      if (this.spendType && transaction.spendType === this.spendType) {
        return this.isCR ? transaction.amount < 0 : transaction.amount >= 0;
      }

      return false;
    })
  }

  formatNumberWithCR(value: number, precision: number) {
    return this.isCRFormated? NumberUtilsService.formatWithCommasAndCR(value, precision) : NumberUtilsService.formatWithCommas(value, precision);
  };

}
