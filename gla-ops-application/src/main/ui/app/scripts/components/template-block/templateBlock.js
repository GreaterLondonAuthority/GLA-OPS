/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class TemplateBlock {

  constructor(ToastrUtil, SessionService){
    this.ToastrUtil = ToastrUtil;
    this.SessionService = SessionService;
  }

  $onInit() {
  }

  onBlockTypeSelect(blockType){
    let selectedBlockType = _.find(this.blockTypes, {blockType: blockType})
    this.block.type = selectedBlockType.templateClassName;
  }

  copyBlock(){
    this.SessionService.setTemplateBlock(angular.copy(this.block));
    this.ToastrUtil.success('Block copied');
  }
}

TemplateBlock.$inject = ['ToastrUtil', 'SessionService'];

gla.component('templateBlock', {
  templateUrl: 'scripts/components/template-block/templateBlock.html',
  controller: TemplateBlock,
  bindings: {
    blockTypes: '<',
    block: '<',
    template: '<',
    isNew: '<',
    readOnly: '<',
    onSave: '&'
  },
});

