/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Service: TransitionService', () => {

  beforeEach(angular.mock.module('GLA'));

  let TransitionService ;

  beforeEach(inject($injector => {
    TransitionService = $injector.get('TransitionService');
  }));

  describe('isTransitionAllowed', () => {
    let allowedTransitions;
    beforeEach(() => {
      allowedTransitions = [
        {
          status: 'Closed',
          subStatus: 'Abandoned',
        },
        {
          status: 'Active',
          subStatus: null
        }
      ]
    });

    it('should allow transition to Closed_Abandoned', () => {
      let isAllowed = TransitionService.isTransitionAllowed(allowedTransitions, 'Closed', 'Abandoned');
      expect(isAllowed).toEqual(true);
    });

    it('should not allow transition to Closed status only', () => {
      let isAllowed = TransitionService.isTransitionAllowed(allowedTransitions, 'Closed');
      expect(isAllowed).toEqual(false);
    });

    it('should allow transition to Active status only', () => {
      let isAllowed = TransitionService.isTransitionAllowed(allowedTransitions, 'Active');
      expect(isAllowed).toEqual(true);
    });
  });

  describe('status', () => {
    it('should return correct status object', () => {
      let project = {statusType: 'Draft'};
      let status = TransitionService.status(project);
      expect(status.draft).toEqual(true);
      expect(status.assess).toEqual(false);
      expect(status.active).toEqual(false);
    });
  });
});
