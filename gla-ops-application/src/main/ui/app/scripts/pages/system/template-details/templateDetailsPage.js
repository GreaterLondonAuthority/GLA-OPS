/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const TEMPLATE_DEFAULTS = {
  name: 'TBC',
  author: 'TBC',
  'blocksEnabled': [
    {
      'type': 'ProjectDetailsTemplateBlock',
      'blockDisplayName': 'Project Details',
      'block': 'Details',
      'displayOrder': 1
    }
  ],
  stateModel: {
    name: 'ChangeControlled'
  },
  programmeAllocation: false
};

const BLOCK_DEFAULTS = {
  Details: {
    templateLevelParams: {
      detailsConfig: {}
    }
  },

  Outputs: {
    blockLevelParams: {
      showValueColumn: true,
      showOutputTypeColumn: true,
    }
  },

  LearningGrant: {
    blockLevelParams: {
      grantType: 'AEB_GRANT'
    }
  }
};

class templateDetailsPageCtrl {
  constructor($state, $filter, $scope, $rootScope, TemplateService, ToastrUtil, ReferenceDataService, SessionService, GlaSessionService, UserService) {
    this.TemplateService = TemplateService;
    this.ToastrUtil = ToastrUtil;
    this.$state = $state;
    this.$filter = $filter;
    this.$scope = $scope;
    this.$rootScope = $rootScope;
    this.ReferenceDataService = ReferenceDataService;
    this.SessionService = SessionService;
    this.GlaSessionService = GlaSessionService;
    this.useJsonEditor = true;
    this.supported = true;
    this.UserService = UserService;
  }

  $onInit() {
    this.hasPermissionToEditExternalBlocks = this.UserService.hasPermission('temp.edit.external.block');
    this.hasPermissionToEditInternalBlocks = this.UserService.hasPermission('temp.edit.internal.block');

    this.tabs = {
      details: 0,
      blocks: 1,
      internalBlocks: 2,
      json: 3
    };

    this.isNew = !this.template;
    this.editable = this.template && this.template.projectsCount === 0;
    this.readOnly = !this.isNew;
    this.templateText = this.getTemplateAsText(this.isNew ? TEMPLATE_DEFAULTS : this.template);
    this.activeTabIndex = 0;
    this.blockTypes =_.sortBy(this.blockTypes, 'blockType');

    this.convertToTemplateObject(this.templateText);

    let ctrl = this;
    this.sortableOptions = {
      start(e, ui){
        ctrl.sortedDisplayOrders = (ctrl.templateObj.blocksEnabled).map(block => block.displayOrder);
      },

      stop(e, ui){
        for(let i = 0; i < ctrl.templateObj.blocksEnabled.length; i++){
          ctrl.templateObj.blocksEnabled[i].displayOrder = ctrl.sortedDisplayOrders[i];
        }
      },
      axis: 'y'
    };
  }

  convertToTemplateObject(templateString){
    this.templateObj = this.getTemplateJsonFromText(templateString);
    if(this.selectedBlock){
      this.selectedBlock = _.find(this.templateObj.blocksEnabled, {
        block: this.selectedBlock.block,
        displayOrder: this.selectedBlock.displayOrder
      });
    }

    if(this.selectedInternalBlock){
      this.selectedInternalBlock = _.find(this.templateObj.internalBlocks, {
        type: this.selectedInternalBlock.type,
        displayOrder: this.selectedInternalBlock.displayOrder
      });
    }
  }

  getCopiedBlock(){
    return this.SessionService.getTemplateBlock();
  }

  getInternalCopiedBlock(){
    return this.GlaSessionService.getTemplateInternalBlock();
  }

  onJsonChange(jsonString){
    this.templateText = jsonString;
  }


  getTitle() {
    if (this.isNew) {
      return 'Create template'
    }
    if (this.editable && this.template.status === 'Draft') {
      return (this.template || {}).name
    }

    return (this.originalTemplate || {}).name
  }

  isTemplateInUse() {
    return this.template ? this.template.status === 'Active': false;
  }

  success() {
    console.log('Copied!');
  };

  fail(err) {
    console.error('Error!', err);
  };


  pasteData() {
    if (!navigator.clipboard) {
      alert('Clipboard access is disabled');
      return;
    }
    navigator.clipboard.readText()
      .then(text => {
        this.$scope.$evalAsync(() => {
          this.templateText = text;
          this.convertToTemplateObject(text)
        });
      })
      .catch(err => {
        console.error('Failed to read clipboard contents: ', err);
      });
  };


  edit() {
    this.TemplateService.getDraftTemplate(this.$state.params.templateId).toPromise().then(resp => {
      this.$scope.$evalAsync(() => {
        this.setReadOnly(false);
        this.templateText = this.getTemplateAsText(resp);
        this.convertToTemplateObject(this.templateText);
        this.errorMsg = null;
      });
    }, (resp) => {
      this.errorMsg = resp.description;
    });
  }

  stopEditing() {
    this.$rootScope.showGlobalLoadingMask = true;
    let templateText = this.getTemplateText();
    return this.TemplateService.updateTemplate(this.$state.params.templateId, templateText).toPromise().then(resp => {
      this.ToastrUtil.success(`Template updated`);
      this.convertToTemplateObject(templateText)
      this.errorMsg = null;
      this.setReadOnly(true);
      this.TemplateService.clearCache();
      this.$rootScope.showGlobalLoadingMask = false;
    }, (resp) => {
      this.errorMsg = resp.error? resp.error.description : resp.data.description;
      this.$rootScope.showGlobalLoadingMask = false;
    });
  }

  updateLiveTemplate() {
    this.$rootScope.showGlobalLoadingMask = true;
    let templateText = this.getTemplateText();
    return this.TemplateService.updateInUseTemplate(this.$state.params.templateId, templateText).toPromise().then(resp => {
      this.ToastrUtil.success(`Template updated`);
      this.convertToTemplateObject(templateText)
      this.errorMsg = null;
      this.setReadOnly(true);
      this.TemplateService.clearCache();
      this.$rootScope.showGlobalLoadingMask = false;
    }, (resp) => {
      this.errorMsg = resp.error? resp.error.description : resp.data.description;
      this.$rootScope.showGlobalLoadingMask = false;
    });
  }

  back() {
    this.$state.go('system-templates');
  }

  onAdd() {
    this.$rootScope.showGlobalLoadingMask = true;
    return this.TemplateService.createTemplate(this.getTemplateText()).toPromise().then(resp => {
      this.ToastrUtil.success(`Template created`);
      this.$rootScope.showGlobalLoadingMask = false;
      return this.$state.go('system-template-details', {
        templateId: resp
      });
    }, (resp) => {
      this.errorMsg = resp.error? resp.error.description : resp.data.description;
      this.$rootScope.showGlobalLoadingMask = false;
    });
  }

  getTemplateText() {
    if (this.activeTabIndex != this.tabs.json) {
      this.convertTemplateJsonToString();
    }
    return this.templateText;
  }

  copyAll() {
    if (!this.editable) {
      let template = JSON.parse(this.templateText);
      template.name = 'TBC';
      template.author = 'TBC';
      return JSON.stringify(template, null, 4)
    }
    return this.templateText;
  }

  setReadOnly(readOnly) {
    this.readOnly = readOnly;
  }

  getTemplateAsText(template) {
    return template ? this.$filter('json')(template) : null;
  }

  getTemplateJsonFromText() {
    // console.log('this.responseText', this.templateText)
    if (!this.templateText) {
      return {};
    }
    try {
      return JSON.parse(this.templateText);
    } catch (err) {
      console.log('err', err)
      return null;
    }
  };

  onDetailsTabSelected(){
    if(!this.readOnly && this.activeTabIndex === this.tabs.json){
      this.convertToTemplateObject(this.templateText)
    }
  }

  onBlocksTabSelected() {
    if(!this.readOnly && this.activeTabIndex === this.tabs.json){
      this.convertToTemplateObject(this.templateText)
    }

    if(this.templateObj){
      this.templateObj.blocksEnabled = _.sortBy(this.templateObj.blocksEnabled, 'displayOrder');
    }
  }

  onInternalBlocksTabSelected() {
    if(!this.readOnly && this.activeTabIndex === this.tabs.json){
      this.convertToTemplateObject(this.templateText)
    }

    if(this.templateObj){
      this.templateObj.internalBlocks = _.sortBy(this.templateObj.internalBlocks, 'displayOrder');
    }
  }

  onJsonTabSelected() {
    this.templateObj = angular.copy(this.templateObj);
    if (!this.readOnly) {
      this.convertTemplateJsonToString();
    }
    this.selectedBlock = null;
    this.selectedInternalBlock = null;
  }

  convertTemplateJsonToString() {
    if (this.templateObj) {
      (this.templateObj.blocksEnabled || []).forEach(block => this.setMissingBlockDefaults(block, this.templateObj));
      this.templateText = this.getTemplateAsText(this.templateObj);
    }
  }

  onBlockSave() {
    this.selectedBlock = null;
  }

  onInternalBlockSave() {
    this.selectedInternalBlock = null;
  }

  onAddBlock() {
    this.addBlock({
      block: 'Questions',
      type: 'QuestionsTemplateBlock',
      questions: [],
      sections: []
    });
  }


  onAddInternalBlock() {
    this.addInternalBlock({
      type: 'Questions',
      'json_type': 'InternalQuestionsTemplateBlock'
    });
  }

  onPasteBlock(){
    if(!this.getCopiedBlock()){
      alert('Nothing to paste');
      return;
    }
    this.addBlock(this.getCopiedBlock());
  }

  onPasteInternalBlock(){
    if(!this.getInternalCopiedBlock()){
      alert('Nothing to paste');
      return;
    }
    this.addInternalBlock(this.getInternalCopiedBlock());
  }

  addBlock(block){
    if (this.templateObj) {
      this.isBlockNew = true;
      this.templateObj.blocksEnabled = this.templateObj.blocksEnabled || [];
      let maxDisplayOrder = (_.maxBy(this.templateObj.blocksEnabled, 'displayOrder') || {}).displayOrder || 0;
      block.blockDisplayName = block.blockDisplayName || `Block ${maxDisplayOrder + 1}`;
      block.displayOrder = maxDisplayOrder + 1;
      this.templateObj.blocksEnabled.push(block);
      this.selectedBlock = block;
    } else {
      alert('Can\'t add to invalid template JSON');
    }
  }

  addInternalBlock(block){
    if (this.templateObj) {
      this.isInternalBlockNew = true;
      this.templateObj.internalBlocks = this.templateObj.internalBlocks || [];
      let maxDisplayOrder = (_.maxBy(this.templateObj.internalBlocks, 'displayOrder') || {}).displayOrder || 0;
      block.blockDisplayName = block.blockDisplayName || `Block ${maxDisplayOrder + 1}`;
      block.displayOrder = maxDisplayOrder + 1;
      this.templateObj.internalBlocks.push(block);
      this.selectedInternalBlock = block;
    } else {
      alert('Can\'t add to invalid template JSON');
    }
  }

  selectBlock(block) {
    if (!this.readOnly) {
      this.isBlockNew = false;
    }
    this.selectedBlock = block;
  }

  selectInternalBlock(block) {
    if (!this.readOnly) {
      this.isInternalBlockNew = false;
    }
    this.selectedInternalBlock = block;
  }

  removeBlock(block) {
    _.remove(this.templateObj.blocksEnabled, block);
    //Changes the reference value to trigger the components $onChanges event
    this.templateObj.blocksEnabled = this.templateObj.blocksEnabled.slice();
    //Remove properties on template level controlled by the block. Like detailsConfig
    let blockDefaults = (BLOCK_DEFAULTS[block.block] || {});
    let templatePropertiesControlledByBlock = Object.keys(blockDefaults.templateLevelParams || {});
    templatePropertiesControlledByBlock.forEach(key => delete this.templateObj[key]);
  }

  performCommand(command) {
    this.TemplateService.performCommand(this.$state.params.templateId, command.internalBlock, command.displayOrder, command.name, command.payload || {}).toPromise().then(resp => {
      this.ToastrUtil.success(`Template updated`);
      this.templateText = this.getTemplateAsText(resp);
      this.convertToTemplateObject(this.templateText);
      this.errorMsg = null;
      this.setReadOnly(true);
      this.TemplateService.clearCache();
      this.$rootScope.showGlobalLoadingMask = false;
      if(this.templateObj){
        this.templateObj.blocksEnabled = _.sortBy(this.templateObj.blocksEnabled, 'displayOrder');
      }
    }, (resp) => {
      this.errorMsg = resp.error? resp.error.description : resp.data.description;
      this.$rootScope.showGlobalLoadingMask = false;
    });
  }

  removeInternalBlock(block) {
    _.remove(this.templateObj.internalBlocks, block);
    //Changes the reference value to trigger the components $onChanges event
    this.templateObj.internalBlocks = this.templateObj.internalBlocks.slice();
    //Remove properties on template level controlled by the block. Like detailsConfig
    // let blockDefaults = (BLOCK_DEFAULTS[block.block] || {});
    // let templatePropertiesControlledByBlock = Object.keys(blockDefaults.templateLevelParams || {});
    // templatePropertiesControlledByBlock.forEach(key => delete this.templateObj[key]);
  }

  isTemplateJsonValid() {
    return this.templateText && this.templateText.length && this.templateObj;
  }

  removeQuestion(question, block) {
    _.remove(this.block.questions, question);
  }

  setMissingBlockDefaults(block, template) {
    // console.log(block);
    let blockDefaults = (BLOCK_DEFAULTS[block.block] || {});
    _.defaults(template, blockDefaults.templateLevelParams);
    _.defaults(block, blockDefaults.blockLevelParams);
  }

  isSaveTemplateBtnEnabled() {
    return this.templateText || Object.keys(this.templateObj || {}).length;
  }

  validateTemplate(){
    this.$rootScope.showGlobalLoadingMask = true;
    return this.TemplateService.validateTemplate(this.getTemplateText()).toPromise().then(resp => {
      this.ToastrUtil.success(`The template is valid and can be added to a Programme when ready`);
      this.errorMsg = null;
      this.$rootScope.showGlobalLoadingMask = false;
    }, (resp) => {
      this.errorMsg = resp.error? resp.error.description : resp.data.description;
      this.$rootScope.showGlobalLoadingMask = false;
    });
  }

}

templateDetailsPageCtrl.$inject = ['$state', '$filter', '$scope', '$rootScope', 'TemplateService', 'ToastrUtil', 'ReferenceDataService', 'SessionService', 'GlaSessionService', 'UserService'];

angular.module('GLA')
  .component('templateDetailsPage', {
    templateUrl: 'scripts/pages/system/template-details/templateDetailsPage.html',
    bindings: {
      template: '<',
      blockTypes: '<',
      internalBlockTypes: '<',
      originalTemplate: '<'
    },
    controller: templateDetailsPageCtrl
  });
