import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {TemplateConfigurationService} from "../template-configuration.service";
import {filter} from "lodash-es";
import {UserService} from "../../user/user.service";

@Component({
  selector: 'gla-template-external-blocks',
  templateUrl: './template-external-blocks.component.html',
  styleUrls: ['./template-external-blocks.component.scss']
})
export class TemplateExternalBlocksComponent implements OnInit {
  @Input() blocks: any[];
  @Input() templateId: number;
  @Input() readOnly: boolean;
  @Input() inUse: boolean;
  @Input() blockTypes: any[];
  @Output() onSelectBlock = new EventEmitter<any>();
  @Output() onRemoveBlock = new EventEmitter<any>();
  @Output() onPerformCommand = new EventEmitter<any>();
  sortableOptions;
  hasPermissionToEdit: boolean;

  constructor(private confirmationDialog: ConfirmationDialogService,
              private templateConfigurationService: TemplateConfigurationService,
              private userService: UserService){}

  ngOnInit(): void {
    this.hasPermissionToEdit = this.userService.hasPermission('temp.edit.external.block');
    this.sortableOptions = {
      start(e, ui) {
        this.sortedDisplayOrders = (this.blocks).map(block => block.displayOrder);
      },

      stop(e, ui) {
        for (let i = 0; i < this.blocks.length; i++) {
          this.blocks[i].displayOrder = this.sortedDisplayOrders[i];
        }
        this.sortBlocks();

      },
      helper: 'clone',
      axis: 'y',
      disabled: this.readOnly
    };
  }

  onChanges() {
    if (this.sortableOptions) {
      this.sortableOptions.disabled = this.readOnly;
    }
    this.sortBlocks();
  }

  sortBlocks() {
    this.blocks.sort((item1, item2) => item1.displayOrder - item2.displayOrder);
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
      blockType: block.block,
      blockOldName: block.blockDisplayName,
      maxCommentLength: command.maxCommentLength
    }
    command.payload = {
      blockData
    }
    command.displayOrder= block.displayOrder;
    command.internalBlock = false;
    let modal = this.confirmationDialog.show({
      message: this.getWarningMessage(block, command.warningMessage),
      existingComment: this.templateConfigurationService.getExistingComment(command, block),
      approveText: command.title.toUpperCase(),
      dismissText: 'CANCEL',
      userCommentRequired: command.requiresComment
    });

    modal.result.then((userInput) => {
      if (command.requiresComment){
        this.templateConfigurationService.setUserInputComment(command, blockData, userInput)
      }
      blockData['blockNewName'] = userInput
      this.onPerformCommand.emit(command);
    }, (error) => {});
  }

  getWarningMessage(block, message): String {
    return message.replace('[block_name]', block.blockDisplayName);
  }

}
