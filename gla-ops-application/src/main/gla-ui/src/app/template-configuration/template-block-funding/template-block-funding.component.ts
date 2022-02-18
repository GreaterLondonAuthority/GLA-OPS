import {Component, Input, OnInit} from '@angular/core';
import {ReferenceDataService} from "../../reference-data/reference-data.service";

@Component({
  selector: 'gla-template-block-funding',
  templateUrl: './template-block-funding.component.html',
  styleUrls: ['./template-block-funding.component.scss']
})
export class TemplateBlockFundingComponent implements OnInit {

  @Input() block
  @Input() template
  @Input() readOnly : boolean
  @Input() editable : boolean
  fundingSpendTypeOptions : any
  configItemsGroups: any[]
  showDecimalPlaces: boolean

  constructor(private referenceDataService: ReferenceDataService) { }

  ngOnInit(): void {
    this.fundingSpendTypeOptions = this.referenceDataService.getFundingSpendTypeOptions();
    this.referenceDataService.getConfigItems().toPromise().then((resp) => {
      let configItems = resp;
      this.configItemsGroups = Object.keys(configItems);
    });
    this.showDecimalPlaces = (!this.block.monetaryValueScale || this.block.monetaryValueScale === 0) ? false : true;
  }

  onShowDecimalChange(showDecimalValue) {
    this.block.monetaryValueScale = showDecimalValue ? 2 : 0;
    this.showDecimalPlaces = this.block.monetaryValueScale === 0 ? false : true;
  }

  onBasedOnValueChange(value){
    this.block.showMilestones = value ==='showMilestones' ? true : false;
    this.block.showCategories = value ==='showCategories'  ? true : false;
  }

  getReadOnlyBasedOnText(value){
    if (value === true) {
      return 'Milestones';
    } else {
      return 'Budget Categories Group';
    }
    return 'Not provided';
  }

  getReadOnlyClaimLevelText(value) {
    return value ? 'Activities and/or Quarters' : 'Quarters only';
  }
}
