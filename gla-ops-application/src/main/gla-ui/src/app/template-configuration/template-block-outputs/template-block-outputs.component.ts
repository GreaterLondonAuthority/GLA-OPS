import {Component, Input, OnInit} from '@angular/core';
import {OutputsConfigurationService} from "./outputs-configuration.service";

@Component({
  selector: 'gla-template-block-outputs',
  templateUrl: './template-block-outputs.component.html',
  styleUrls: ['./template-block-outputs.component.scss']
})
export class TemplateBlockOutputsComponent implements OnInit {

  @Input() block: any
  @Input() template: any
  @Input() readOnly: boolean
  @Input() editable: boolean
  outputConfigurationsOptions: any

  constructor(private  outputsConfigurationService : OutputsConfigurationService) {

  }

  ngOnInit(): void {
    this.outputsConfigurationService.getAllOutputConfigurationGroup().toPromise().then(resp => {
      this.outputConfigurationsOptions = resp;
    });
  }

  trackByKey(index: number, obj: any): string {
    return obj.id;
  };

}
