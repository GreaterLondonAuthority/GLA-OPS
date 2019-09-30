/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function FundingEvidenceModal($uibModal, $rootScope, FileUploadErrorModal, ProjectFundingService) {
  return {
    show: function (project, blockId, activity, maxEvidenceAttachments, readOnly) {
      const evidenceModal = this;
      var modal = $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/funding/evidenceModal/evidenceModal.html',
        size: 'md',
        resolve: {
          activity: () => {
            return activity;
          }
        },

        controller: ['$uibModalInstance', 'activity', function ($uibModalInstance, activity) {
          this.readOnly = readOnly;
          this.activity = activity;

          this.maxEvidenceAttachments = maxEvidenceAttachments;
          this.uploadParams = {
            orgId: project.organisation.id
          };

          this.onFileUploadProgress = (data) => {
            $rootScope.showGlobalLoadingMask = true;
            const progress = data.progress;
          };

          this.onFileUploadComplete = (data) => {
            let file = data.response;
            ProjectFundingService.attachEvidence(project.id, blockId, activity.id, file.id)
              .then((rsp) => {
                this.activity.attachments = (rsp.data);
                // this.activity.attachments.push(rsp.data);
              })
              .finally(() => {
                $rootScope.showGlobalLoadingMask = false;
              });
          };

          this.onFileUploadError = (error) => {
            $uibModalInstance.close();
            $rootScope.showGlobalLoadingMask = false;
            let modal = FileUploadErrorModal.show(this, error);
            modal.result.finally(() => {
              evidenceModal.show(project, blockId, activity, maxEvidenceAttachments, readOnly);
            });
          };


          this.delete = (doc) => {
            ProjectFundingService.deleteEvidence(project.id, blockId, activity.id, doc.id).then((rsp) => {
              _.remove(activity.attachments, {id: doc.id});
              // let apiMilestone = _.find(rsp.data.activitys, {id: activity.id});
              // this.activity.attachments = apiMilestone.attachments;
            });
          };
        }]
      });

      modal.closed.then(() => {
        _.forEach(activity.attachments, attachment => {
          attachment.showConfirmDelete = false;
        });
      });

      return modal;
    }
  };
}

FundingEvidenceModal.$inject = ['$uibModal', '$rootScope', 'FileUploadErrorModal', 'ProjectFundingService'];

angular.module('GLA')
  .service('FundingEvidenceModal', FundingEvidenceModal);
