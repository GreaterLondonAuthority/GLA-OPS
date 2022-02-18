import {Component, Input, OnInit} from '@angular/core';
import {ReferenceDataService} from "../../reference-data/reference-data.service";
import {FundingSourceModalService} from "../funding-source-modal/funding-source-modal.service";
import {find} from "lodash-es";


@Component({
  selector: 'gla-template-block-other-funding',
  templateUrl: './template-block-other-funding.component.html',
  styleUrls: ['./template-block-other-funding.component.scss']
})
export class TemplateBlockOtherFundingComponent implements OnInit {

  @Input() block
  @Input() template
  @Input() readOnly : boolean
  @Input() editable : boolean
  requirementOptions : any

  constructor(private referenceDataService: ReferenceDataService,
              private fundingSourceModalService:FundingSourceModalService) { }

  ngOnInit(): void {
    this.requirementOptions = this.referenceDataService.getRequirementOptions();
  }

  showFunderName(): boolean {
    let nameRequired = find(this.block.fundingSources, {showFunderName: true});
    return nameRequired != undefined;
  }

  showDescription(): boolean {
    let descriptionRequired = find(this.block.fundingSources, {showDescription: true});
    return descriptionRequired != undefined;
  }

}
