import { Component, OnInit, Input } from '@angular/core';
import {ReferenceDataService} from "../../reference-data/reference-data.service";

@Component({
  selector: 'gla-template-block-user-defined-outputs',
  templateUrl: './template-block-user-defined-outputs.component.html',
  styleUrls: ['./template-block-user-defined-outputs.component.scss']
})
export class TemplateBlockUserDefinedOutputsComponent implements OnInit {
  @Input() block
  @Input() template
  @Input() readOnly : boolean
  @Input() editable : boolean
  @Input() draft : boolean
  requirementOptions: any

  constructor( private referenceDataService: ReferenceDataService) {
    this.requirementOptions = this.referenceDataService.getRequirementOptions();
  }

  ngOnInit(): void {}

}
