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
  stateModel: {
    name: 'ChangeControlled'
  }
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
  constructor($state, $filter, $scope, TemplateService, ToastrUtil, ReferenceDataService, SessionService) {
    this.TemplateService = TemplateService;
    this.ToastrUtil = ToastrUtil;
    this.$state = $state;
    this.$filter = $filter;
    this.$scope = $scope;
    this.ReferenceDataService = ReferenceDataService;
    this.SessionService = SessionService;
    this.useJsonEditor = true;
    this.supported = true;
  }

  $onInit() {
    this.isNew = !this.template;
    this.editable = this.template && this.template.status === 'Draft';
    this.readOnly = !this.isNew;
    this.templateText = this.getTemplateAsText(this.isNew ? TEMPLATE_DEFAULTS : this.template);
    this.activeTabIndex = 0;

    // Control if the JSON editor is read only or not
    this.options = {
      mode: 'code',
      readOnly: this.readOnly, // Fake json-editor property just to make it reevaluate 'onEditable' on change
      onEditable: (node) => {
        return !this.readOnly;
      }
    };

    this.tabs = {
      details: 0,
      blocks: 1,
      json: 2,
      text: 3
    };

    this.$scope.$watch('$ctrl.templateText', value => {
      this.templateObj = this.getTemplateJsonFromText(value);
    });

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

  getCopiedBlock(){
    return this.SessionService.getTemplateBlock();
  }


  getTitle() {
    if (this.isNew) {
      return 'Create template'
    }
    if (this.editable) {
      return (this.template || {}).name
    }

    if (!this.editable) {
      return (this.originalTemplate || {}).name
    }
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
        });
      })
      .catch(err => {
        console.error('Failed to read clipboard contents: ', err);
      });
  };


  edit() {
    this.TemplateService.getDraftTemplate(this.$state.params.templateId).then(resp => {
      this.$scope.$evalAsync(() => {
        this.setReadOnly(false);
        this.templateText = this.getTemplateAsText(resp.data);
      });
    }, (resp) => {
      this.errorMsg = resp.data.description;
    });
  }

  stopEditing() {
    this.setReadOnly(true);
    return this.TemplateService.updateTemplate(this.$state.params.templateId, this.getTemplateText()).then(resp => {
      this.ToastrUtil.success(`Template updated`);
    }, (resp) => {
      this.errorMsg = resp.data.description;
    });
  }

  back() {
    this.$state.go('system-templates');
  }

  onAdd() {
    return this.TemplateService.createTemplate(this.getTemplateText()).then(resp => {
      this.ToastrUtil.success(`Template created`);
      this.back();
    }, (resp) => {
      this.errorMsg = resp.data.description;

    });
  }

  getTemplateText() {
    if (this.activeTabIndex === this.tabs.blocks || this.activeTabIndex === this.tabs.details) {
      this.convertTemplateJsonToString();
    }
    return this.templateText;
  }

  copyAll() {
    return this.templateText;
  }

  setReadOnly(readOnly) {
    this.readOnly = readOnly;
    this.options.readOnly = readOnly;
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

  onDetailsTabDeselected() {
    if (!this.readOnly) {
      console.log(this.templateObj);
      this.templateText = this.getTemplateAsText(this.templateObj);
    }
  }

  onBlocksTabSelected() {
    if(this.templateObj){
      this.templateObj.blocksEnabled = _.sortBy(this.templateObj.blocksEnabled, 'displayOrder');
    }
  }

  onBlocksTabDeselected() {
    if (!this.readOnly) {
      this.convertTemplateJsonToString();
    }
    this.selectedBlock = null;
  }

  convertTemplateJsonToString() {
    if (this.templateObj) {
      (this.templateObj.blocksEnabled || []).forEach(block => this.setMissingBlockDefaults(block, this.templateObj));
      this.templateText = this.getTemplateAsText(this.templateObj);
    }
  }

  onJsonTabSelected() {
    this.onBlocksTabDeselected();
    this.onDetailsTabDeselected();
  }

  onTabChange(tabIndex) {

  }

  onBlockSave() {
    this.onBlocksTabDeselected();
    this.onDetailsTabDeselected();
  }

  onAddBlock() {
    this.addBlock({
      block: 'Questions',
      type: 'QuestionsTemplateBlock',
      questions: [],
      sections: []
    });
  }

  onPasteBlock(){
    if(!this.getCopiedBlock()){
      alert('Nothing to paste');
      return;
    }
    this.addBlock(this.getCopiedBlock());
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

  selectBlock(block) {
    if (!this.readOnly) {
      this.isBlockNew = false;
    }
    this.selectedBlock = block;
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

}

templateDetailsPageCtrl.$inject = ['$state', '$filter', '$scope', 'TemplateService', 'ToastrUtil', 'ReferenceDataService', 'SessionService'];

angular.module('GLA')
  .component('templateDetailsPage', {
    templateUrl: 'scripts/pages/system/template-details/templateDetailsPage.html',
    bindings: {
      template: '<',
      blockTypes: '<',
      originalTemplate: '<'
    },
    controller: templateDetailsPageCtrl
  });
