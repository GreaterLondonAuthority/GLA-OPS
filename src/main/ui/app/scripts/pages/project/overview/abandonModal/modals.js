/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

module.exports = {
  config(){
    return {
      abandon: {
        title: 'Abandon Project',
        label: 'Are you sure you want to abandon this project? Once abandoned the project will be closed and cannot be updated.',
        actionBtnName: 'ABANDON PROJECT',
        hintMessage: null,
        placeholder: 'Provide a brief explanation as to why this project is being abandoned',
        commentRequired: true
      },

      requestAbandon: {
        title: 'Request to Abandon a Project',
        label: 'Are you sure you want to request to abandon this project? Once the request is approved, the project will be closed and cannot be updated.',
        actionBtnName: 'REQUEST TO ABANDON PROJECT',
        hintMessage: null,
        placeholder: 'Provide a brief explanation as to why this project is being abandoned',
        commentRequired: true
      },
      reject: {
        title: 'Reject Project',
        label: 'Are you sure you want to reject this project? Once rejected the project will be closed and cannot be updated.',
        actionBtnName: 'REJECT PROJECT',
        hintMessage: null,
        placeholder: 'Provide a brief explanation as to why this project is being rejected',
        commentRequired: true
      },

      warning: {
        title: 'Abandon Project',
        label: null,
        actionBtnName: null,
        hintMessage: 'Project cannot be abandoned at this stage.',
        commentRequired: true
      },
      warningReject: {
        title: 'Reject Project',
        label: null,
        actionBtnName: null,
        hintMessage: 'Project cannot be rejected at this stage.',
        commentRequired: true
      },

      reinstate: {
        title: 'Reinstate Project',
        label: 'Reinstating this project will make the project available to providers to update.',
        actionBtnName: 'REINSTATE PROJECT',
        hintMessage: 'Provide a brief explanation of why this project is being reinstated.',
        placeholder: 'Provide a brief explanation of why this project is being reinstated.',
        commentRequired: true
      },

      complete: {
        title: 'Complete Project',
        label: 'Are you sure you want to complete this project? The project will be closed and cannot be updated.',
        actionBtnName: 'COMPLETE PROJECT',
        hintMessage: null,
        commentRequired: false,
        placeholder: 'Add an optional comment'
      }
    }
  }
};
