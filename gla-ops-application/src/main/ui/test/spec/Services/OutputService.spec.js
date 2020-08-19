/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Service: OutputsService', () => {

  beforeEach(angular.mock.module('GLA'));

  let OutputsService, block;

  beforeEach(inject($injector => {
    OutputsService = $injector.get('OutputsService');
    block = {
      outputSummaries: [
        {
          'outputType': 'INDIRECT',
          'category': 'c1',
          'subcategories': [
            {financialYear: '2012'},
            {financialYear: '2014'},
            {financialYear: '2015'}
          ]
        }, {
          'outputType': 'DIRECT',
          'category': 'c2',
          'subcategories': [
            {financialYear: '2016'}
          ]
        }, {
          'outputType': 'DIRECT',
          'category': 'c3',
          'subcategories': [
            {financialYear: '2016'}
          ]
        }]
    }
  }));


  describe('#getOutputBlockSummariesTitle (GLA-11876)', () => {
    it('should show title with start and end period', () => {
      expect(OutputsService.getOutputBlockSummariesTitle(block)).toEqual('Total project outputs 2012/13 to 2016/17');
    });

    it('should show title with start period only', () => {
      block.outputSummaries = block.outputSummaries.slice(1);
      expect(OutputsService.getOutputBlockSummariesTitle(block)).toEqual('Total project outputs 2016/17');
    });

    it('should show title without dates', () => {
      block.outputSummaries = [];
      expect(OutputsService.getOutputBlockSummariesTitle(block)).toEqual('Total project outputs');
    });
  });

});
