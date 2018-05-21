/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
 * Util Class for holding the outputs configs
 */
class OutputsUtil {
  static getUnitConfig() {
    return {
      UNITS: {
        id: 0,
        precision: 0,
        label: 'Number of units',
        placeholder: 'Enter units',
        default: undefined
      },
      HECTARES: {
        id: 1,
        precision: 2,
        label: 'Hectares',
        placeholder: 'Enter Hectares (Ha)',
        default: undefined
      },
      POSITIONS: {
        id: 2,
        precision: 0,
        label: 'Positions',
        placeholder: 'Enter number',
        default: undefined
      },
      SQUARE_METRES: {
        id: 3,
        precision: 0,
        label: 'Square meters',
        placeholder: 'Enter sqm',
        default: undefined
      },
      SQUARE_METRES_NET: {
        id: 4,
        precision: 0,
        label: 'Net Area (sqm)',
        placeholder: 'Enter net internal area (sqm)',
        default: undefined
      },
      SQUARE_METRES_GROSS: {
        id: 5,
        precision: 0,
        label: 'Gross Area (sqm)',
        placeholder: 'Enter gross area (sqm)',
        default: undefined
      },
      BEDROOMS: {
        id: 6,
        precision: 0,
        label: 'Bedrooms',
        placeholder: 'Enter number of bedrooms',
        default: undefined

      },
      MONETARY_VALUE: {
        id: 7,
        precision: 0,
        label: 'Monetary value',
        placeholder: 'Enter value £',
        default: undefined
      },
      NUMBER_OF: {
        id: 8,
        precision: 0,
        label: 'Number of',
        placeholder: 'Enter Number',
        default: undefined
      },
      NUMBER_OF_DECIMAL: {
        id: 9,
        precision: 2,
        label: 'Number of',
        placeholder: 'Enter Number',
        default: undefined
      },
      ENTER_VALUE: {
        id: 10,
        precision: 0,
        label: 'Enter Value',
        placeholder: 'Enter Value',
        default: undefined
      },
      ENTER_VALUE_DECIMALS: {
        id: 11,
        precision: 2,
        label: 'Enter Value',
        placeholder: 'Enter Value',
        default: undefined
      },
      NET_AREA: {
        id: 12,
        precision: 0,
        label: 'Net Area (sqm)',
        placeholder: 'Enter Net Area (sqm)',
        default: undefined
      },
      DISTANCE: {
        id: 13,
        precision: 0,
        label: 'Distance (m)',
        placeholder: 'Enter Distance (m)',
        default: undefined
      },
      LENGTH: {
        id: 14,
        precision: 0,
        label: 'Length (m)',
        placeholder: 'Enter Length (m)',
        default: undefined
      }
    };
  }

  static getOutputTypes() {

    return {
      'DIRECT': 'Direct Output',
      'IND_COUNTED_IN_ANOTHER': 'Counted in Another Housing Programme',
      'IND_MINORITY_STAKE': 'Minority Stake in Joint Venture',
      'IND_UNBLOCKS': 'Unlocks Other Parts of a Site',
      'IND_UNLOCKING': 'Indirect: Unlocking Without Land Interest',
      'IND_OTHER': 'Indirect: Other'
    }
  }

  static setCategories(categories) {
    OutputsUtil.categories = categories;
  }

  static getCategories() {
    return OutputsUtil.categories;
  }
  static setDirectOrIndirect(choices) {
    OutputsUtil.directOrIndirect = choices;
  }

  static getDirectOrIndirect() {
    return OutputsUtil.directOrIndirect;
  }
}
OutputsUtil.categories = [];
OutputsUtil.directOrIndirect = [];

export default OutputsUtil;
