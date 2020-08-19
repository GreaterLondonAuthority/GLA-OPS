/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
 * Util Class for string manipulations
 */
class StringUtil {
  /**
   * Converts a `CamelCasedString` to `camel-cased-string`
   * @param {String} str
   * @return {String} `camel-cased-string`
   */
  static camelCaseToDash(str) {
    return str.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
  }

  static removeHtml(text){
    return text.replace(/<(?:.|\n)*?>/gm, '')
  }

  static replaceMarkdownUrl(input){
    const linkRegExp = /\[([^\]]+)?\]\(([^)]+)\)/g;
    let output = input;
    let match;

    while ((match = linkRegExp.exec(input)) !== null) {
      const string = match[0];
      const text = match[1] || '';
      const url = match[2];

      const replacement = `<a href="${url}" target="_blank">${text}</a>`;

      if (replacement) {
        output = output.replace(string, replacement);
      }
    }
    return output;
  }
}

export default StringUtil;
