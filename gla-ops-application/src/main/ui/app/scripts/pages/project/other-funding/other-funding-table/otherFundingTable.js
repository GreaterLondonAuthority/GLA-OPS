/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class OtherFundingTableCtrl {
  constructor(ProjectSkillsService, FileUploadModal, OtherFundingService) {
    this.ProjectSkillsService = ProjectSkillsService;
    this.OtherFundingService = OtherFundingService;
    this.FileUploadModal = FileUploadModal;
  }

  $onInit() {
    this.blockId = this.block.id;
    // Get configurable labels if exists, otherwise set to some default labels
    this.entityName = this.template.entityName ? this.template.entityName : 'Funding';
    this.funderNameText = this.template.funderNameText ? this.template.funderNameText : 'Funder Name';
    this.descriptionText = this.template.descriptionText ? this.template.descriptionText : 'Description';
    this.amountText = this.template.amountText ? this.template.amountText : 'Amount';
    this.securedQuestion = this.template.securedQuestion ? this.template.securedQuestion : 'Has this funding been secured?';

    this.showFunderName = _.find(this.template.fundingSources, {showFunderName: true}) ? true : false;
    this.showDescription = _.find(this.template.fundingSources, {showDescription: true}) ? true : false;
    this.showEvidence = this.template.evidenceRequirement === 'optional' || this.template.evidenceRequirement === 'mandatory';
  }

  evidenceLinkText(entry) {
    let hasAttachments = !!(entry.attachments || []).length;
    if (this.readOnly) {
      return hasAttachments ? 'View' : null;
    } else if (hasAttachments) {
      return 'Edit';
    } else {
      return entry.fundingSecured ? 'Add' : null;
    }
  }

  showEvidenceModal(entry) {
    let modalConfig = {
      orgId: this.orgId,
      projectId: this.block.projectId,
      programmeId: this.programmeId,
      blockId: this.block.id,
      readOnly: this.readOnly,
      attachments: entry.attachments,
      title: 'Upload evidence for other funding',
      text: 'Please upload evidence that this funding has been secured, for example an offer letter, bank statement or other documentation.',
      disableAdd: !entry.fundingSecured,
      maxEvidenceAttachments: this.template.maxEvidenceAttachments,
      maxUploadSizeInMb: this.template.maxUploadSizeInMb,

      onFileUploadComplete: (file) => {
        return this.OtherFundingService.attachEvidence(this.block.projectId, this.blockId, entry.id, file.id).then(rsp => {
          return this.updateAttachments(rsp.data, entry);
        })
      },

      onDeleteFile: (file) => {
        return this.OtherFundingService.deleteEvidence(this.block.projectId, this.blockId, entry.id, file.id).then((rsp) => {
          return this.updateAttachments(rsp.data, entry);
        });
      }
    };
    this.FileUploadModal.show(modalConfig);
  }

  updateAttachments(block, entry){
    entry.attachments = (_.find(block.otherFundings, {id: entry.id}) || {}).attachments || [];
    return entry.attachments;
  }
}

OtherFundingTableCtrl.$inject = ['ProjectSkillsService', 'FileUploadModal', 'OtherFundingService'];


angular.module('GLA')
.component('otherFundingTable', {
  controller: OtherFundingTableCtrl,
  templateUrl: 'scripts/pages/project/other-funding/other-funding-table/otherFundingTable.html',
  bindings: {
    block: '<',
    orgId: '<',
    programmeId: '<',
    template:'<',
    fundingEntries: '<',
    onEntryChange: '&',
    onEntryDelete: '&',
    readOnly: '<'
  }
});
