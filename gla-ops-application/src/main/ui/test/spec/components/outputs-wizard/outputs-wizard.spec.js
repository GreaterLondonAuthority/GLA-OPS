/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



describe('Component: outputs-wizard', () => {

  let config = {
    month: '#receiptMonth',
    category: '#outputsCategory',
    subCategory: '#outputsSubCategory',
    directOrIndirect: '#directOrIndirect',
    outputsType: '#outputsType',
    outputsValue: '.outputsValue',
    outputsValueLabel: '.outputsValue label',
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, $timeout;

  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $timeout = $injector.get('$timeout');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    $scope.$ctrl = testData();


    let template = `<outputs-wizard
                      year='$ctrl.currentYear'
                      on-add-output='$ctrl.onAddOutput(event)'
                      period-type='$ctrl.periodType'
                      output-type-name='$ctrl.outputTypeName'
                      category-name='$ctrl.categoryName'
                      categories="$ctrl.categories"
                      subcategory-name='$ctrl.subcategoryName'>
                    </outputs-wizard>`;

    element = $compile(template)($scope);
    $scope.$digest();
  }));


  describe('Configurable category input types (GLA-10522)', () => {
    it('should support multiple input types', () => {
      selectOption(config.month, 'April');
      $timeout.flush();
      selectOption(config.category, 'C1');
      $timeout.flush();

      let expectations = {
        'NUMBER_OF': 'Number of',
        'NUMBER_OF_DECIMAL': 'Number of',
        'ENTER_VALUE': 'Enter Value',
        'ENTER_VALUE_DECIMALS': 'Enter Value',
        'NET_AREA': 'Net Area (sqm)',
        'DISTANCE': 'Distance (m)',
        'LENGTH': 'Length (m)'
      };

      let valueTypes = Object.keys(expectations);
      for (let i = 0; i < valueTypes.length; i++) {
        let valueType = valueTypes[i];
        selectOption(config.subCategory, valueType);
        $timeout.flush();
        expect(element.find(config.outputsValueLabel).text()).toEqual(expectations[valueType]);
      }
    });
  });


  function testData() {
    return {
      currentYear: {
        financialYear: 2030,
      },
      periodType: 'Monthly',
      outputTypeName: 'Output Type',
      categoryName: 'Category',
      subcategoryName: 'Sub Category',
      categories: mockCategories()
    };
  }


  function selectOption(selectId, optionText) {
    element.find(`${selectId} .ui-select-toggle`).click();
    element.find(`${selectId} .ui-select-choices-row:contains('${optionText}')`).click();
  }


  function mockCategories() {
    return [[{
      'id': 1,
      'category': 'C1',
      'subcategory': 'NUMBER_OF',
      'valueType': 'NUMBER_OF',
      hidden: false
    }, {
      'id': 2,
      'category': 'C1',
      'subcategory': 'NUMBER_OF_DECIMAL',
      'valueType': 'NUMBER_OF_DECIMAL',
      hidden: false
    }, {
      'id': 3,
      'category': 'C1',
      'subcategory': 'ENTER_VALUE',
      'valueType': 'ENTER_VALUE',
      hidden: false
    }, {
      'id': 4,
      'category': 'C1',
      'subcategory': 'ENTER_VALUE_DECIMALS',
      'valueType': 'ENTER_VALUE_DECIMALS',
      hidden: false
    }, {
      'id': 5,
      'category': 'C1',
      'subcategory': 'NET_AREA',
      'valueType': 'NET_AREA',
      hidden: false
    }, {
      'id': 6,
      'category': 'C1',
      'subcategory': 'DISTANCE',
      'valueType': 'DISTANCE',
      hidden: false
    }, {
      'id': 7,
      'category': 'C1',
      'subcategory': 'LENGTH',
      'valueType': 'LENGTH',
      hidden: false
    }]]
  }
});
