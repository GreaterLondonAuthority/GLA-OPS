/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const ONE_MB = 1024 * 1024;

function index(obj, i) {
  if(!obj){
    return;
  }
  return obj[i];
}
class QuestionFileUpload {
  constructor($rootScope, $scope, $log, FileService, FileDeleteConfirmationModal, FileUploadErrorModal, ConfirmationDialog, ToastrUtil) {
    this.$rootScope = $rootScope;
    this.$scope = $scope;
    this.FileService = FileService;
    this.FileDeleteConfirmationModal = FileDeleteConfirmationModal;
    this.FileUploadErrorModal = FileUploadErrorModal;
    this.ConfirmationDialog = ConfirmationDialog;
    this.ToastrUtil = ToastrUtil;
    this.$log = $log;
  }

  $onInit(){
    this.question.fileAttachments = this.question.fileAttachments || [];
    this.question.attachments = this.question.attachments || [];

    this.question.totalAttachmentsSize = this.question.totalAttachmentsSize || 0;
    this.remainingCombinedFileSize = this.question.maxCombinedUploadSizeInMb * ONE_MB - this.question.totalAttachmentsSize;

    this.uploadParams = {
      orgId: this.project.organisation.id
    };
  }

  addDocumentButtonDisabled() {
    if (this.readOnly) {
      return true;
    }
    else if (this.question.maxCombinedUploadSizeInMb) {
      return this.remainingCombinedFileSize <= 0;
    }
    else {
      return (this.question.attachments.length + this.question.fileAttachments.length) >= this.question.quantity;
    }
  }

  getRemainingCombinedFileSize() {
    return Math.max(0, this.remainingCombinedFileSize / ONE_MB).toFixed(1);
  }

  onFileUploadProgress(data) {
    this.$rootScope.showGlobalLoadingMask = true;
    const progress = data.progress;
    this.$log.debug(`progress: ${progress}%`);
  }

  onFileUploadComplete(resp) {
    var file = resp.response;

    this.remainingCombinedFileSize -= file.fileSize;

    //allows the digest cycle to reload the template on change
    this.$rootScope.showGlobalLoadingMask = false;

    this.$scope.$evalAsync(() => {
      this.question.attachments.push({
        fileId: file.id,
        fileName: file.fileName,
        fileSize: file.fileSize
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

  removeAttachment(file, listToRemoveFrom) {
    let modal = this.FileDeleteConfirmationModal.show(this, file);
    let fileSize = file.fileSize ? file.fileSize : 0;
    this.remainingCombinedFileSize += fileSize;
    modal.result.then(() => {
      _.remove(listToRemoveFrom, file);
    });
  }

  downloadAttachment(attachmentId) {
    this.FileService.getFile(attachmentId)
      .then((resp) => {
        let fileName = this.FileService.extractFileNameFromResponse(resp);

        if (window.navigator.msSaveOrOpenBlob) {
          var blob = new Blob([decodeURIComponent(encodeURI(resp.data))], {
            type: 'text/csv;charset=utf-8;'
          });
          navigator.msSaveBlob(blob, fileName);
        } else {
          var a = document.createElement('a');
          a.href = 'data:attachment/csv;charset=utf-8,' + encodeURI(resp.data);
          a.target = '_blank';
          a.download = fileName;
          document.body.appendChild(a);
          a.click();
        }
      })
      .catch(err => {
        let modal = this.ConfirmationDialog.warn('Oops, that file is no longer available. Please contact an OPS administrator for further assistance.');
        modal.result.then(function () {
        });
      });
  }
}

QuestionFileUpload.$inject = ['$rootScope', '$scope', '$log', 'FileService', 'FileDeleteConfirmationModal', 'FileUploadErrorModal', 'ConfirmationDialog', 'ToastrUtil'];

angular.module('GLA')
  .component('questionFileUpload', {
    bindings: {
      question: '=',
      readOnly: '<',
      project: '<',
      block: '<'
    },
    templateUrl: 'scripts/components/questions/questionFileUpload.html',
    controller: QuestionFileUpload
  });
