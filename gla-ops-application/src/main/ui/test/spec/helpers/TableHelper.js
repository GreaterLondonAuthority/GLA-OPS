/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function TableHelper($http) {
  return {
    getTableContent(table) {
      let rows = table.find('tbody tr').toArray();
      return rows.map(row => {
        let columns = $(row).find('td').toArray();
        return columns.map(td => $(td).text().trim());
      });
    }
  };
}

TableHelper.$inject = [];

angular.module('GLA')
  .service('TableHelper', TableHelper);
