import {Component, Input, OnInit} from '@angular/core';
import {cloneDeep, merge, remove} from "lodash-es";
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {FundingSourceModalService} from "../funding-source-modal/funding-source-modal.service";


@Component({
  selector: 'gla-funding-source-table',
  templateUrl: './funding-source-table.component.html',
  styleUrls: ['./funding-source-table.component.scss']
})
export class FundingSourceTableComponent implements OnInit {

  @Input() fundingBlock
  @Input() readOnly : boolean
  @Input() editable: boolean
  fundingSources : any
  constructor(private confirmationDialogService: ConfirmationDialogService
             ,private FundingSourceModalService: FundingSourceModalService ) {
  }

  ngOnInit(): void {
    this.fundingSources = this.fundingBlock.fundingSources || []
  }

  updateFundingBlockFundingSources(){
    this.fundingBlock.fundingSources = this.fundingSources
  }
  showFundingSourceModal(fundingSource?: any) {
    let isNew = !fundingSource || !fundingSource.fundingSource
    let modal = this.FundingSourceModalService.show(cloneDeep(fundingSource), this.fundingSources);
    modal.result.then((result) => {
      if (isNew) {
        this.fundingSources.push(result);
      } else{
        merge(fundingSource, result)
      }
      this.updateFundingBlockFundingSources()
    }, ()=>{});
  }

  deleteFundingSource(fundingSource){
    let modal = this.confirmationDialogService.delete(`Are you sure you want to delete ${fundingSource.fundingSource}?`);
    modal.result.then(()=>{
      remove(this.fundingSources, fundingSource);
      this.updateFundingBlockFundingSources()
    });
  }

}
