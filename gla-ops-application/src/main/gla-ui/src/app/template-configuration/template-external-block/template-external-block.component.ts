import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ReferenceDataService} from "../../reference-data/reference-data.service";
import {NavigationService} from "../../navigation/navigation.service";
import {TemplateService} from "../../template/template.service";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
import {SessionService} from "../../session/session.service";
import {cloneDeep, filter, find} from "lodash-es";

@Component({
  selector: 'gla-template-external-block',
  templateUrl: './template-external-block.component.html',
  styleUrls: ['./template-external-block.component.scss']
})
export class TemplateExternalBlockComponent implements OnInit {

  @Input() blockTypes: any
  @Input() block: any
  @Input() template: any
  @Input() isNew: boolean
  @Input() readOnly: boolean
  @Input() editable: boolean
  @Input() $state: any
  @Output() onSave =  new EventEmitter<any>();
  @Output() onBack = new EventEmitter<any>();
  isDraft: boolean
  isEditModeActive: boolean
  canEditActiveBlock: boolean
  activeTemplateWarning: string
  editBlockCommand: any
  errorMsg: any

  constructor(private referenceDataService: ReferenceDataService,
              private navigationService: NavigationService,
              private templateService: TemplateService,
              private toastrUtil: ToastrUtilService,
              private sessionService: SessionService) {
    this.$state = this.navigationService.getCurrentStateParams();
  }

  ngOnInit(): void {
    this.isDraft = this.template.status === undefined || this.template.status === 'Draft';
    this.isEditModeActive = false
    this.canEditActiveBlock = !this.isDraft && this.canEditSelectedBlockOnActiveTemplate(this.block)
    this.activeTemplateWarning = this.canEditActiveBlock? 'Any changes will take effect almost immediately on all projects using this template. '
      + 'This may result in blocks becoming incomplete when they are next edited.': '';
  }

  onBlockTypeSelect(blockType){
    let selectedBlockType = find(this.blockTypes, {blockType: blockType})
    this.block.type = selectedBlockType.templateClassName;
  }

  copyBlock(){
    this.sessionService.setTemplateBlock(cloneDeep(this.block));
    this.toastrUtil.success('Block copied');
  }

  canEditSelectedBlockOnActiveTemplate(selectedBlockType) {
    let editBlockCommand = []
    if(selectedBlockType.block === 'UserDefinedOutput') {
      editBlockCommand = filter(selectedBlockType.templateBlockCommands, {name: 'EDIT_USER_DEFINED_OUTPUT_BLOCK'});
    }
    return editBlockCommand.length != 0;
  }

  editActiveTemplateBlock() {
    if (this.isEditModeActive == false) {
      this.isEditModeActive = true
      this.readOnly = false;
    } else {
      this.isEditModeActive = false
      this.readOnly = true;
      this.performAction()
    }
  }

  performAction() {
    let blockData = {
      blockId: this.block.id,
      userDefinedOutputTemplateBlock: this.block
    }
    this.editBlockCommand = filter(this.block.templateBlockCommands, {name: 'EDIT_USER_DEFINED_OUTPUT_BLOCK'});
    this.editBlockCommand.payload = {blockData}
    this.editBlockCommand.displayOrder = this.block.displayOrder;
    this.editBlockCommand.internalBlock = false;
    this.performCommand(this.editBlockCommand)
  }

  performCommand(command: any) {
    this.templateService.performCommand(this.$state.templateId, command.internalBlock, command.displayOrder, command[0].name, command.payload || {}).toPromise().then((resp: any) => {
      this.toastrUtil.success(`Template updated`);
      this.errorMsg = null;
    }, (resp) => {
      this.errorMsg = resp.error ? resp.error.description : resp.data.description;
    });
  }

}
