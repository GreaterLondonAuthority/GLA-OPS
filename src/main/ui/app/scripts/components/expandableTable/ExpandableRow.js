/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class ExpandableRow {
    init(){
      this.isExpanded = false;
      this.primaryRowFocus = false;
      this.cellSelected = null;

      // retrieve store
      this.store =
        this.sessionStorage[this.tableId] ?
          this.sessionStorage[this.tableId] :
          this.sessionStorage[this.tableId] = {};

      // retrieve or initialise expanded list
      this.store.expanded =
        this.store.expanded ?
          this.store.expanded :
          [];

      // set component state
      this.isExpanded = (this.store.expanded.indexOf(this.rowId) >= 0);
      this.cellSelected = this.store.cellSelected;
    }

    /**
     * Handle Expand/collapse state
     */
    changeExpandedState() {
        if(this.isExpanded) {
            this.store.expanded = _.pull(this.store.expanded, this.rowId);
            this.isExpanded = false;
            // this.primaryRowFocus = false;
        }
        else {
            if(this.store.expanded.indexOf(this.rowId) < 0) {
                this.store.expanded.push(this.rowId);
            }
            this.isExpanded = true;
            // this.primaryRowFocus = true;
        }
    }

    /**
     * Store last focused input element
     * @param {Object} args - list of identifiers that form the reference for the particular cell
     */
    onCellFocus(...args) {
        this.store.cellSelected = args.join('|');
    }

    /**
     * Evaluate if the cell is the last selected
     * @param {Object} args - list of identifiers that form the reference for the particular cell
     */
    getCellFocus(...args) {
        return this.cellSelected === args.join('|');
    }

    /**
     * Handle onFocus on primaryRow
     */
    onPrimaryRowFocus() {
        this.primaryRowFocus = true;
    }

    /**
     * Handle onBlur on primaryRow
     */
    onPrimaryRowBlur() {
        this.primaryRowFocus = false;
    }
}
export default ExpandableRow;
