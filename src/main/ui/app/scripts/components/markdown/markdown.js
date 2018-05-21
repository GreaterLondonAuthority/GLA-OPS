/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import StringUtil from '../../util/StringUtil';
const gla = angular.module('GLA');

class MarkdownCtrl {
  constructor() {
    if(this.text) {
      let textWithoutHtml = StringUtil.removeHtml(this.text);
      this.html = StringUtil.replaceMarkdownUrl(textWithoutHtml);
    }
  }
}

MarkdownCtrl.$inject = [];



gla.component('markdown', {
  bindings: {
    text: '<'
  },
  controller: MarkdownCtrl,
  templateUrl: 'scripts/components/markdown/markdown.html'
});
