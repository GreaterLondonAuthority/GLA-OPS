/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



describe('Component: profiled-units-table', () => {

  let config = {
    tenureColumnEl: 'table tbody td:nth-child(1)',
    bedsColumnEl: 'table tbody td:nth-child(3)',
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, PaymentService;

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.units = testData();
    element = $compile("<profiled-unit-table units='units' show-market-types='true'></profiled-unit-table>")($scope);
    $scope.$digest();
  }));


  describe('Sorting', () => {
    it('should be sorted by tenureName as a primary rule by default (GLA-6695)', () => {
      let tenures = element.find(config.tenureColumnEl).map((index, item) => $(item).text().trim()).get();
      expect(tenures).toEqual(['t1', 't1', 't2']);
    });

    it('should be sorted by beds as a secondary rule by default (GLA-6695)', () => {
      let beds = element.find(config.bedsColumnEl).map((index, item) => $(item).text().trim()).get();
      expect(beds).toEqual(['Bed 1', 'Bed 2', 'Bed 1']);
    });

    it('should resort after change', () => {
      $scope.units = testData();
      $scope.units[0] = {
        id: 0,
        tenureName: `t0`,
        type: 'Rent',
        nbBeds: {
          displayOrder: 1,
          displayValue: 'Bed 1'
        }
      };
      $scope.$digest();
      let tenures = element.find(config.tenureColumnEl).map((index, item) => $(item).text().trim()).get();
      expect(tenures).toEqual(['t0', 't1', 't1']);
    });


  });


  function testData() {
    return [
      {
        id: 1,
        tenureName: `t2`,
        type: 'Rent',
        nbBeds: {
          displayOrder: 1,
          displayValue: 'Bed 1'
        }
      },
      {
        id: 2,
        tenureName: `t1`,
        type: 'Rent',
        nbBeds: {
          displayOrder: 2,
          displayValue: 'Bed 2'
        }
      },
      {
        id: 3,
        tenureName: `t1`,
        type: 'Rent',
        nbBeds: {
          displayOrder: 1,
          displayValue: 'Bed 1'
        }
      }
    ];
  }


});
