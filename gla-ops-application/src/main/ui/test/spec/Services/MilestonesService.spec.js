/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Service: MilestonesService', () => {

  beforeEach(angular.mock.module('GLA'));

  let MilestonesService, milestone;

  beforeEach(inject($injector => {
    MilestonesService = $injector.get('MilestonesService');
    milestone = {
      notApplicable: false,
      milestoneDate: new Date(),
      monetarySplit: 50,
      claimStatus: 'Pending'
    }
  }));


  describe('#setMilestoneAsNotApplicable (GLA-3280)', () => {
    it('should set grant to 0', () => {
      MilestonesService.setMilestoneAsNotApplicable(milestone);
      expect(milestone.notApplicable).toBe(true);
      expect(milestone.milestoneDate).toBe(null);
      expect(milestone.monetarySplit).toBe(0);
      expect(milestone.claimStatus).toBe(null);
    });
  });

});
