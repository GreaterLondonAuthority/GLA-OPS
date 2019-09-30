/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
function index(obj, i) {
  if(!obj){
    return;
  }
  return obj[i];
}
class QuestionFileUpload {
  constructor($rootScope, $scope, $log, FileDeleteConfirmationModal, FileUploadErrorModal, ToastrUtil) {
    this.$rootScope = $rootScope;
    this.$scope = $scope;
    this.FileDeleteConfirmationModal = FileDeleteConfirmationModal;
    this.FileUploadErrorModal = FileUploadErrorModal;
    this.ToastrUtil = ToastrUtil;
    this.$log = $log;
  }

  $onInit(){
    this.question.fileAttachments = this.question.fileAttachments || [];
    this.question.attachments = this.question.attachments || [];

    this.uploadParams = {
      orgId: this.project.organisation.id
    };
  }

  onFileUploadProgress(data) {
    this.$rootScope.showGlobalLoadingMask = true;
    const progress = data.progress;
    this.$log.debug(`progress: ${progress}%`);
  }

  onFileUploadComplete(resp) {
    var file = resp.response;

    //allows the digest cycle to reload the template on change
    this.$rootScope.showGlobalLoadingMask = false;

    this.$scope.$evalAsync(() => {
      this.question.attachments.push({
        fileId: file.id,
        fileName: file.fileName
      });
    });
    this.ToastrUtil.success('Added');
    this.$log.debug('upload complete', file);
  }

  onFileUploadError(error) {
    this.$rootScope.showGlobalLoadingMask = false;
    this.$log.debug('upload error:', error);
    let modal = this.FileUploadErrorModal.show(this, error);
    modal.result.then(function () {
    });
  }

  removeFileToUpload(file) {
    let modal = this.FileDeleteConfirmationModal.show(this, file);
    modal.result.then(() => {
      _.remove(this.question.attachments, file);
    });
  }

  removeAttachment(file) {
    let modal = this.FileDeleteConfirmationModal.show(this, file);
    modal.result.then(() => {
      _.remove(this.question.fileAttachments, file);
    });
  }
}

QuestionFileUpload.$inject = ['$rootScope', '$scope', '$log', 'FileDeleteConfirmationModal', 'FileUploadErrorModal', 'ToastrUtil'];

angular.module('GLA')
  .component('questionFileUpload', {
    bindings: {
      question: '=',
      readOnly: '<',
      project: '<'
    },
    templateUrl: 'scripts/components/questions/questionFileUpload.html',
    controller: QuestionFileUpload
  });
