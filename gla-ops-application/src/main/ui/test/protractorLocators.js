/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function initLocators  (ptor) {
  ptor.by.addLocator('fieldLabel', fieldLabel);
  ptor.by.addLocator('idOrClass', idOrClass);
  ptor.by.addLocator('table', table);
  ptor.by.addLocator('inputValue', inputValue);
  ptor.by.addLocator('closest', closest);
  ptor.by.addLocator('prev', prev);
  ptor.by.addLocator('cssContainingTextAndIgnoringWhiteSpace', cssContainingTextAndIgnoringWhiteSpace);


  function fieldLabel (labelText, parentElement, rootSelector) {
    console.log('labelText', labelText);
    console.log('rootSelector', rootSelector);
    var parent = $(parentElement || document);
    var label = parent.find("label:contains('" + labelText + "')");


    if(!fieldId){
      label = parent.find('label').filter(function(){ return (($(this).text() ||'').replace(/\s\s+/g, ' ') || '').indexOf(labelText) > -1});
    }

    var fieldId = label.attr('for');
    
    var el = parent.find('#' + fieldId);
    if(!el || !el.length){
      el = parent.find('[aria-label*="' + labelText + '"]');
    }

    if(el.is('gla-date-input') || el.is('date-input')){
      el = el.find('input')
    }

    return el;
  }


  function cssContainingTextAndIgnoringWhiteSpace (selector, labelText, parentElement, rootSelector) {
    var parent = $(parentElement || document);
    var el = parent.find(selector + ":contains('" + labelText + "')");

    if(!el || !el.length){
      el = parent.find(selector).filter(function(){ return (($(this).text() ||'').replace(/\s\s+/g, ' ') || '').indexOf(labelText) > -1});
    }

    return el;
  }

  function idOrClass (selector, parentElement, rootSelector) {
    selector = selector.trim().toLowerCase().replace(/ /g, '-');
    var parent = $(parentElement || document);
    var el = parent.find('#' + selector);
    if(!el || !el.length){
      el = parent.find('.' + selector);
    }
    return el;
  }

  function table (selector, parentElement, rootSelector) {
    if(selector && selector.trim()){
      // return idOrClass(selector, parentElement, rootSelector)
      selector = selector.trim().toLowerCase().replace(/ /g, '-');
      var parent = $(parentElement || document);
      var el = parent.find('#' + selector);
      if(!el || !el.length){
        el = parent.find('.' + selector);
      }
      return el;
    }
    return $(parentElement || document).find('table');
  }

  function inputValue(value, parentElement, rootSelector) {
    var parent = $(parentElement || document);
    return parent.find('input').filter(function() { return this.value == value });
  }

  function closest(closestSelector, parentElement, rootSelector) {
    var parent = $(parentElement || document);
    return parent.closest(closestSelector);
  }

  function prev(selector, parentElement, rootSelector) {
    var parent = $(parentElement || document);
    return parent.prev(selector);
  }
}


module.exports = initLocators;
