/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class TemplateBlocks {
  constructor(ConfirmationDialog) {
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
    let ctrl = this;
    this.sortableOptions = {
      start(e, ui) {
        ctrl.sortedDisplayOrders = (ctrl.blocks).map(block => block.displayOrder);
      },

      stop(e, ui) {
        for (let i = 0; i < ctrl.blocks.length; i++) {
          ctrl.blocks[i].displayOrder = ctrl.sortedDisplayOrders[i];
        }
        ctrl.sortBlocks();

      },
      helper: 'clone',
      axis: 'y',
      disabled: this.readOnly
    };
  }

  $onChanges() {
    if (this.sortableOptions) {
      this.sortableOptions.disabled = this.readOnly;
    }
    this.sortBlocks();
  }

  sortBlocks() {
    this.blocks = _.sortBy(this.blocks, 'displayOrder');
  }

  deleteBlock(block) {
    let modal = this.ConfirmationDialog.delete();
    modal.result.then(() => {
      this.onRemoveBlock({event:block});
    });
  }
}

TemplateBlocks.$inject = ['ConfirmationDialog'];


gla.component('templateBlocks', {
  templateUrl: 'scripts/components/template-blocks/templateBlocks.html',
  controller: TemplateBlocks,
  bindings: {
    blocks: '<',
    readOnly: '<',
    onSelectBlock: '&',
    onRemoveBlock: '&'
  },
});

