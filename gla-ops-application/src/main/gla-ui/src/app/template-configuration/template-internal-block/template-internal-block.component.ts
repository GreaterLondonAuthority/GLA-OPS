import {Component, Input, OnInit, Output, EventEmitter} from '@angular/core';
import {cloneDeep, find, lowerCase, orderBy, startCase} from "lodash-es";
import {SessionService} from "../../session/session.service";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";

@Component({
  selector: 'gla-template-internal-block',
  templateUrl: './template-internal-block.component.html',
  styleUrls: ['./template-internal-block.component.scss']
})
export class TemplateInternalBlockComponent implements OnInit {
  @Input() block: any;
  @Input() template: any;
  @Input() blockTypes: any[];
  @Input() readOnly: boolean;
  @Input() editable: boolean;
  @Input() isNew: boolean;
  isDraft: boolean;
  @Output() onSave =  new EventEmitter<any>();
  private blockTypeText: any;

  constructor(private sessionService: SessionService,
              private toastrUtilService: ToastrUtilService) { }

  ngOnInit(): void {
    this.blockTypes = orderBy(this.blockTypes, 'displayName')
    this.isDraft = this.template.status === 'Draft' ? true : false;
  }

  startCase(displayName) {
    return displayName.replace(/\w+/g, lowerCase).replace(/\w+/g, startCase);
  }

  onBlockTypeSelect(blockType){
    let selectedBlockType = find(this.blockTypes, {blockType: blockType})
    this.block.json_type = selectedBlockType.templateClassName;
  }

  copyBlock(){
    this.sessionService.setTemplateInternalBlock(cloneDeep(this.block));
    this.toastrUtilService.success('Block copied');
  }

}
