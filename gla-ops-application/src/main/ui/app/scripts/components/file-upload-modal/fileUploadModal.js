/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function FileUploadModal($uibModal, $rootScope, FileUploadErrorModal) {
  return {
    show: function (config) {
      config = this.getDefaultsFromConfig(config);

      const evidenceModal = this;
      const modal = $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/components/file-upload-modal/fileUploadModal.html',
        size: 'md',

        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.config = config;
          this.readOnly = config.readOnly;
          this.attachments = config.attachments;

          this.uploadParams = {
            orgId: config.orgId
          };

          this.onFileUploadProgress = (data) => {
            $rootScope.showGlobalLoadingMask = true;
            // const progress = data.progress;
          };

          //Promise should returned attachments after update
          this.onFileUploadComplete = (rsp) => {
            const file = rsp.response;
            let p = config.onFileUploadComplete(file);
            this.updateAttachments(p);
          };

          this.onFileUploadError = (error) => {
            $uibModalInstance.close();
            $rootScope.showGlobalLoadingMask = false;
            let modal = FileUploadErrorModal.show(this, error);
            modal.result.finally(() => {
              evidenceModal.show(config);
            });
          };

          //Promise should returned attachments after update
          this.delete = (doc) => {
            let p = config.onDeleteFile(doc);
            this.updateAttachments(p);
          };

          this.updateAttachments = (attachmentsPromise) => {
            if (attachmentsPromise && _.isFunction(attachmentsPromise.then)) {
              attachmentsPromise.then(attachments => {
                if (!attachments){
                  throw Error('Updated attachments must be returned');
                }
                this.attachments = attachments;
              });
              attachmentsPromise.finally(() => {
                $rootScope.showGlobalLoadingMask = false;
              });
            } else {
              throw Error('Expected promise returning attachments');
            }
          }
        }]
      });

      modal.closed.then(() => {
        _.forEach(config.attachments, attachment => {
          attachment.showConfirmDelete = false;
        });
      });

      return modal;
    },

    getDefaultsFromConfig(config) {

      config.fileIdColumn = config.fileIdColumn ||  'id';
      config.projectId = config.projectId || null;
      config.orgId = config.orgId || null;
      config.readOnly = config.readOnly || false;
      config.entryId = config.entryId || null;
      config.attachments = config.attachments || [];
      config.title = config.title || 'Upload evidence';
      config.text = config.text || '';
      config.disableAdd = config.disableAdd || false;
      config.maxEvidenceAttachments = config.maxEvidenceAttachments || 2;
      config.maxUploadSizeInMb = config.maxUploadSizeInMb || 5;

      return config;
    }
  };
}

FileUploadModal.$inject = ['$uibModal', '$rootScope', 'FileUploadErrorModal'];

angular.module('GLA')
  .service('FileUploadModal', FileUploadModal);
