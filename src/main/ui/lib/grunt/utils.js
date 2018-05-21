/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

var fs = require('fs');
var os = require('os');

module.exports = {
  /**
   * Reads copyright text and wraps it inside jsDoc comment
   *
   * @param copyrightFile location to copyright file
   * @returns {string} Copyright file content wrapped inside jsDoc comment
   */
  copyrightText: function (copyrightFile) {
    var copyrightFileContent = fs.readFileSync(copyrightFile, 'utf8');
    var lines = copyrightFileContent.split(os.EOL);
    var copyrightText = '/**';
    lines.forEach(function (line) {
      copyrightText += os.EOL + ' ' + ('* ' + line).trim();
    });
    copyrightText += os.EOL + ' */';
    return copyrightText;
  }
};

