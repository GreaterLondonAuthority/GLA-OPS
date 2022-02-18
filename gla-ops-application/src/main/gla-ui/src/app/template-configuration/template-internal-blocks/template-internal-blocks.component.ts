import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {filter, lowerCase, startCase} from "lodash-es";
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {TemplateConfigurationService} from "../template-configuration.service";
import {UserService} from "../../user/user.service";

@Component({
  selector: 'gla-template-internal-blocks',
  templateUrl: './template-internal-blocks.component.html',
  styleUrls: ['./template-internal-blocks.component.scss']
})
export class TemplateInternalBlocksComponent implements OnInit, OnChanges {
  @Input() blocks: any[];
  @Input() readOnly: boolean;
  @Input() blockTypes: any[];
  @Input() inUse: boolean;
  @Output() onSelectBlock = new EventEmitter<any>();
  @Output() onRemoveBlock = new EventEmitter<any>();
  @Output() onPerformCommand = new EventEmitter<any>();
  blockTypesToDisplayName = {};
  hasPermissionToEdit : boolean;

  constructor(private confirmationDialog: ConfirmationDialogService,
              private templateConfigurationService: TemplateConfigurationService,
              private userService: UserService) {
  }

  ngOnInit(): void {
    this.hasPermissionToEdit = this.userService.hasPermission('temp.edit.internal.block');
    this.blockTypes.forEach((bt) => {
      //.replace to fix lodash bug that it removes '&' symbol and fails for 'Questions & Comments'. Planned fix in lodash v5
      this.blockTypesToDisplayName[bt.blockType] = bt.displayName
      .replace(/\w+/g, lowerCase)
      .replace(/\w+/g, startCase);
    });
    console.log('blockTypesToDisplayName', this.blockTypesToDisplayName)
  }

  ngOnChanges() {
    this.sortBlocks();
  }

  sortBlocks() {
    if (this.blocks) {
      this.blocks.sort((item1, item2) => item1.displayOrder - item2.displayOrder);
    }
  }

  deleteBlock(block) {
    let modal = this.confirmationDialog.delete();
    modal.result.then(() => {
      this.onRemoveBlock.emit(block);
    });
  }

  getBlockCommands(commands, global) {
    return filter(commands, {global: global})
  }

  performAction(command: any, block: any) {
    let blockData = {
      blockId: block.id,
      blockType: block.type,
      blockOldName: block.blockDisplayName,
      infoMessage: block.infoText
    }
    command.payload = {
      blockData
    }
    command.displayOrder= block.displayOrder;
    command.internalBlock = true;

    let modal = this.confirmationDialog.show({
      message: this.getWarningMessage(block, command.warningMessage),
      existingComment: this.templateConfigurationService.getExistingComment(command, block),
      approveText: command.title.toUpperCase(),
      dismissText: 'CANCEL',
      userCommentRequired: command.requiresComment,
      maxCommentLength: command.maxCommentLength
    });

    modal.result.then((userInput) => {
     if (command.requiresComment){
       this.templateConfigurationService.setUserInputComment(command, blockData, userInput)
     }
     this.onPerformCommand.emit(command);
    }, (error) => {});
  }

  getWarningMessage(block, message): String {
    return message.replace('[block_name]', block.blockDisplayName);
  }
}
