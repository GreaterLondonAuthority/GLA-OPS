/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ForecastDataUtil {
    static getLedgerTypes() {
        return [
            {label: 'Capital expenditure', value:'CAPITAL_EXPENDITURE'},
            {label: 'Capital credit', value:'CAPITAL_CREDIT'},
            {label: 'Revenue expenditure', value:'REVENUE_EXPENDITURE'},
            {label: 'Revenue credit', value:'REVENUE_CREDIT'},
        ]
    }

    /**
     * Util method to help find the ledger code from the spend object
     * @param {Object} obj spend object
     * @returns {String} ledger code
     */
    static getLedgerCodeBySpendObj(obj) {
        if(obj.capitalForecast < 0) return this.getLedgerTypes()[0].value;
        else if(obj.capitalForecast > 0) return this.getLedgerTypes()[1].value;
        else if(obj.revenueForecast < 0) return this.getLedgerTypes()[2].value;
        else if(obj.revenueForecast > 0) return this.getLedgerTypes()[3].value;
    }

    static getSpendRecurrence() {
        return [
            {label: 'Every 1 Month', value: 1},
            {label: 'Every 2 Months', value: 2},
            {label: 'Every 3 Months', value: 3},
            {label: 'Every 4 Months', value: 4}
        ]
    }
}

export default ForecastDataUtil;
