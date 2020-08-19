/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ElementHelper() {
  return {
    /**
     * Checks if element is visible: if it is present and none of his parents are hidden
     * @param $el jQuery array like element
     */
    isVisible($el) {
      if ($el.length > 1) {
        throw new Error('Should be a single element');
      }

      if ($el.length === 0 || this.isElementHidden($el[0])) {
        return false;
      } else {
        let hasHiddenParents = $el.parents().get().some(el => this.isElementHidden(el));
        return !hasHiddenParents;
      }
    },

    /**
     * Checks if element has ng-hide or display:none or visibility:hidden
     * @param el dom element
     * @returns {boolean}
     */
    isElementHidden(el) {
      let hasNgHide = el.className && el.className.indexOf('ng-hide') !== -1;
      return hasNgHide || el.style.display === 'none' || el.style.visibility === 'hidden';
    }
  };
}

ElementHelper.$inject = [];


angular.module('GLA')
  .service('ElementHelper', ElementHelper);
