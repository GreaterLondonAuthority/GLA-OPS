/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');

const numberToText = [
  {
    value: 1000000000,
    text: 'bn'
  }, {
    value: 1000000,
    text: 'm'
  }, {
    value: 1000,
    text: 'k'
  }
];

function GlaCurrency(numberFilter) {
  return function (value) {
    let precision = null;
    let suffix = '';
    let shortValue = value;
    for (let i = 0; i < numberToText.length; i++) {
      let base = numberToText[i];
      if (value >= base.value) {
        suffix = base.text;
        shortValue = value / base.value;
        if(!Number.isInteger(shortValue)){
          precision = 1;
        }
        break;
      }
    }
    let formattedValue =  numberFilter(shortValue, precision);
    return formattedValue != null ? `£${formattedValue}${suffix}` : formattedValue;
  }
}

GlaCurrency.$inject = ['numberFilter'];

angular.module('GLA').filter('glaCurrency', GlaCurrency);
