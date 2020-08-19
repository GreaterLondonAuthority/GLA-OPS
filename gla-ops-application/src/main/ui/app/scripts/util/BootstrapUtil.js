/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
 * Util to fix issues with UI Bootstrap components.
 */
class BootstrapUtil {
  /**
   * Sets false instead of '' for aria attributes
   */
  static setAriaDefaults() {
    const ariaAttributes = ['aria-selected', 'aria-expanded'];
    ariaAttributes.forEach((attr) => {
      $(`[${attr}='']`).attr(attr, false);
    });
  }

  /**
   * Makes clicking on caret toggle to expand dropdown   *
   * @see Click issue https://github.com/angular-ui/ui-select/issues/1797
   */
  static enableUiSelectCaretClick(){
    $('body').on('click', '.ui-select-toggle>i.caret', function(e){
      e.stopPropagation();
      var parent = $(this).parent('.ui-select-toggle');
      if(parent){
        parent.click();
      }
    });
  }

  /**
   * Remove aria-haspopup for accessibility (GLA-33367)
   */
  static removeAriaHasPopup(selector){
    $(selector).attr('aria-haspopup', null)
  }
}

export default BootstrapUtil;
