/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class QuestionsCtrl extends ProjectBlockCtrl {
  constructor(ProjectService, FileUploadErrorModal, moment, $injector, ConfirmationDialog, template, $timeout, ConfigurationService, $log, QuestionsService){
    super($injector);
    this.ProjectService = ProjectService;
    this.QuestionsService = QuestionsService;
    this.moment = moment;
    this.FileUploadErrorModal = FileUploadErrorModal;
    this.ConfirmationDialog = ConfirmationDialog;
    this.template = template;
    this.ConfigurationService = ConfigurationService;
    this.$log = $log;
    this.$timeout = $timeout;
  }

  $onInit() {
    super.$onInit();
    this.questions = this.QuestionsService.getQuestionsFromBlock(this.projectBlock);

    //Resizing in IE is tricky. Test if it works if you make any layout change to textarea.
    //You can test even in Chrome with uncommenting if statement
    if (!this.ConfigurationService.isResizeCssPropertySupported()) {
      this.$timeout(() => {
        $('textarea').resizable({
          handles: 's',
          minHeight: 96
        });
      });
    }
  }

  back() {
    if (this.readOnly) {
      this.returnToOverview();
    } else {
      this.submit();
    }
  }

  /**
   * Form submit handler
   */
  submit() {
      this.projectBlock.answers = this.QuestionsService.getAnswers(this.questions);
      return this.ProjectService.updateProjectAnswers(this.project.id, this.blockId, this.projectBlock);
  }
}

QuestionsCtrl.$inject = ['ProjectService', 'FileUploadErrorModal', 'moment', '$injector', 'ConfirmationDialog', 'template', '$timeout', 'ConfigurationService', '$log', 'QuestionsService'];

angular.module('GLA')
  .controller('QuestionsPageCtrl', QuestionsCtrl);
