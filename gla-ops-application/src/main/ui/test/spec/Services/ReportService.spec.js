/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Service: ReportService', () => {

  beforeEach(angular.mock.module('GLA'));

  let ReportService;

  beforeEach(inject($injector => {
    ReportService = $injector.get('ReportService');
  }));
  describe('mapFields', () => {
    it('should process all elements in array to create field object', () => {
      let fields = ['test1', 'test2'];
      expect(ReportService.mapFields(fields)).toEqual([{field: 'test1'}, {field: 'test2'}]);
    });
    it('if 1 format, then it should be applied to all elements ', () => {
      let fields = ['test1', 'test2'];
      let formats = ['number'];
      expect(ReportService.mapFields(fields, formats)).toEqual([
        {field: 'test1', format: 'number'},
        {field: 'test2', format: 'number'}
      ]);
    });
    it('1 format should not override field level formatting', () => {
      let fields = [{field: 'test1', format: 'date'}, 'test2'];
      let formats = ['number'];
      expect(ReportService.mapFields(fields, formats)).toEqual([
        {field: 'test1', format: 'date'},
        {field: 'test2', format: 'number'}
      ]);
    });
    it('multi format should map 1 to 1 fields', () => {
      let fields = ['test1', 'test2'];
      let formats = ['number', 'date'];
      expect(ReportService.mapFields(fields, formats)).toEqual([
        {field: 'test1', format: 'number'},
        {field: 'test2', format: 'date'}
      ]);
    });
  });
  describe('extractValue', () => {
    it('should do simple lookup', () => {
      let data = {title: 'a title', name: 'a name'};
      expect(ReportService.extractValue('title', data)).toEqual('a title');
      expect(ReportService.extractValue('name', data)).toEqual('a name');

    });
    it('should do complex lookup', () => {
      let data = {org: {title: 'a title', name: 'a name', address: {postcode: 'A12 34B'}}};
      expect(ReportService.extractValue('org.title', data)).toEqual('a title');
      expect(ReportService.extractValue('org.name', data)).toEqual('a name');
      expect(ReportService.extractValue('org.address.postcode', data)).toEqual('A12 34B');

    });
  });
  describe('formatFieldValue', () => {
    it('will format number value properly', () => {
      expect(ReportService.formatFieldValue(123456789.12, 'number')).toEqual('123,456,789');
      expect(ReportService.formatFieldValue(123456789.12, 'number|2')).toEqual('123,456,789.12');
      expect(ReportService.formatFieldValue(-123456789.12, 'number|2')).toEqual('-123,456,789.12');
    });
    it('will format currency value properly', () => {
      expect(ReportService.formatFieldValue(123456789.12, 'currency')).toEqual('£123,456,789');
      expect(ReportService.formatFieldValue(123456789.12, 'currency|2')).toEqual('£123,456,789.12');
      expect(ReportService.formatFieldValue(-123456789.12, 'currency|2')).toEqual('-£123,456,789.12');
    });
    it('will format yesno value properly', () => {
      expect(ReportService.formatFieldValue(true, 'yesno')).toEqual('Yes');
      expect(ReportService.formatFieldValue(false, 'yesno')).toEqual('No');
    });
    it('will format date value properly', () => {
      expect(ReportService.formatFieldValue('2017-07-24T15:24:22.713', 'date')).toEqual('24/07/2017');
      expect(ReportService.formatFieldValue('2017-07-24T15:24:22.713', 'date|dd/MM/yyyy HH:mm')).toEqual('24/07/2017 15:24');
    });
    it('will format time value properly', () => {
      expect(ReportService.formatFieldValue('2017-07-24T15:24:22.713', 'time')).toEqual('15:24');
    });
    it('will format datetime value properly', () => {
      expect(ReportService.formatFieldValue('2017-07-24T15:24:22.713', 'datetime')).toEqual('24/07/2017 15:24');
    });
  });
  describe('findSelectedBorough', () => {
    it('will find a bourough based on it\'s name', () => {
      let bouroughs = [{boroughName: 'borough A'}, {boroughName: 'borough B'}];
      expect(ReportService.findSelectedBorough(bouroughs, 'borough B')).toEqual({boroughName: 'borough B'});
    })
  });
  describe('findSelectedWard', () => {
    it('will find the ward based on the id', () => {
      let wards = [{id: 1, wardName: 'ward A'}, {id: 2, wardName: 'ward B'}];
      expect(ReportService.findSelectedWard(wards, 1)).toEqual({id: 1, wardName: 'ward A'});
    })
  });

  describe('#rowsToCompare()', () => {
    let leftItems, rightItems, comparableRows;

    function rowsMatcher(rightRow) {
      return {id: rightRow.id};
    }

    beforeEach(inject($injector => {
      leftItems = [
        {id: 1, sort: 'a'},
        {id: 2, sort: 'b'},
        {id: 3, sort: 'c'}
      ];

      rightItems = [
        {id: 4, sort: 'd'},
        {id: 3, sort: 'c'},
        {id: 2, sort: 'b'}
      ];

      comparableRows = ReportService.rowsToCompare(leftItems, rightItems, rowsMatcher);
    }));


    it('should match rows in the right side order by default. Deletions at the bottom', () => {
      let expectedArray = [
        {
          left: null,
          right: rightItems[0]
        },
        {
          left: leftItems[2],
          right: rightItems[1],
        },
        {
          left: leftItems[1],
          right: rightItems[2],
        },
        {
          left: leftItems[0],
          right: null
        }
      ];
      expect(comparableRows).toEqual(expectedArray);
    });



    it('should match rows in the order of sort property', () => {
     let sortedRows = ReportService.sortComparableRows(comparableRows, 'sort');

      let expectedArray = [
        {
          left: leftItems[0],
          right: null
        },
        {
          left: leftItems[1],
          right: rightItems[2],
        },
        {
          left: leftItems[2],
          right: rightItems[1],
        },
        {
          left: null,
          right: rightItems[0]
        }
      ];
      expect(sortedRows).toEqual(expectedArray);
    });


    it('should group rows', () => {
      leftItems = [
        {id: 1, sort: 'a'},
        {id: 2, sort: 'b'},
        {id: 3, sort: 'c'}
      ];

      rightItems = [
        {id: 5, sort: 'b'},
        {id: 4, sort: 'b'},
        {id: 3, sort: 'c'},
        {id: 2, sort: 'b'}
      ];

      comparableRows = ReportService.rowsToCompare(leftItems, rightItems, rowsMatcher);
      let sortedRows = ReportService.sortComparableRows(comparableRows, 'sort');
      let groupedRows = ReportService.groupComparableRows(sortedRows, 'sort');
      expect(groupedRows.length).toEqual(3);

      console.log('grouped rows', groupedRows);

      expect(groupedRows[0].groupName).toEqual('a');
      expect(groupedRows[0].group.length).toEqual(1);

      expect(groupedRows[1].groupName).toEqual('b');
      expect(groupedRows[1].group.length).toEqual(3);

      expect(groupedRows[2].groupName).toEqual('c');
      expect(groupedRows[2].group.length).toEqual(1);
    });
  });


});
