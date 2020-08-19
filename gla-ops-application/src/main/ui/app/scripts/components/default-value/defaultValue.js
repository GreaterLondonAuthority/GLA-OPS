/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');


function DefaultValue() {
  return {
    link(scope, element, attr) {
      const defaultText = scope.defaultValue || 'Not provided';
      const defaultValueEl = `<span class="default-value">${defaultText}</span>`;
      let isDefaultValueSet = false;

      scope.$watch(() => {
          return element.text().trim();
        },

        (newText, oldText) => {
          if(!isDefaultValueSet && newText.length === 0){
            element.prepend(defaultValueEl);
            isDefaultValueSet = true;
          } else if (newText !== oldText && newText.length > 0 && isDefaultValueSet && newText != defaultText) {
              element.find('.default-value').remove();
              isDefaultValueSet = false;
          }

          // console.log(`from '${oldText}' to '${newText}', isDefaultValueSet '${isDefaultValueSet}'`);
        });
    },

    scope: {
      'defaultValue': '<?'
    }
  };
}

DefaultValue.$inject = [];

angular.module('GLA')
  .directive('defaultValue', DefaultValue);
