/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const CLOSED = 'Closed';

class ManageRiskAndIssueCtrl {
  constructor($injector) {
    this.RisksService = $injector.get('RisksService');
    this.hideClosed = true;
    this.filter = {
      status: `!${CLOSED}`
    };
  }

  canEdit(riskOrIssue) {
    return !this.readOnly && riskOrIssue.status !== CLOSED;
  }


  toggleFilter(){
    this.filter.status = this.hideClosed? `!${CLOSED}` : undefined;
  }
}

export default ManageRiskAndIssueCtrl;

