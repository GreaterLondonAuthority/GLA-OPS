/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function EvidenceModal($uibModal, $rootScope, FileUploadErrorModal, MilestonesService) {
  return {
    show: function (project, blockId, milestone, maxEvidenceAttachments, readOnly) {
      const evidenceModal = this;
      var modal = $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/milestones/evidenceModal/evidenceModal.html',
        size: 'md',
        resolve: {
          milestone: () => {
            return milestone;
          }
        },

        controller: ['$uibModalInstance', 'milestone', function ($uibModalInstance, milestone) {
          this.readOnly = readOnly;
          this.milestone = milestone;
          this.maxEvidenceAttachments = maxEvidenceAttachments;
          this.uploadParams = {
            orgId: project.organisation.id
          };

          this.onFileUploadProgress = (data) => {
            $rootScope.showGlobalLoadingMask = true;
            const progress = data.progress;
          };

          this.onFileUploadComplete = (data) => {
            MilestonesService.attachEvidence(project.id, blockId, milestone.id, data.response.id)
              .then((rsp) => {
                let apiMilestone = _.find(rsp.data.milestones, {id: milestone.id});
                this.milestone.attachments = apiMilestone.attachments;
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
              evidenceModal.show(project, blockId, milestone, maxEvidenceAttachments, readOnly);
            });
          };


          this.delete = (doc) => {
            MilestonesService.deleteEvidence(project.id, blockId, milestone.id, doc.id).then((rsp) => {
              let apiMilestone = _.find(rsp.data.milestones, {id: milestone.id});
              this.milestone.attachments = apiMilestone.attachments;
            });
          };
        }]
      });

      modal.closed.then(() => {
        _.forEach(milestone.attachments, attachment => {
          attachment.showConfirmDelete = false;
        });
      });

      return modal;
    }
  };
}

EvidenceModal.$inject = ['$uibModal', '$rootScope', 'FileUploadErrorModal', 'MilestonesService'];

angular.module('GLA')
  .service('EvidenceModal', EvidenceModal);
