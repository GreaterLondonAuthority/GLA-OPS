/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

/**
 * Determines how the finance category is used in OPS:
 * Hidden: stored in the DB but does not appear in the UI.
 * ReadOnly: appears in the UI but does not appear in the dropdown to create a forecast.
 * ReadWrite: appears in the UI and in the dropdown to create a forecast.
 */
public enum FinanceCategoryStatus {

    ReadWrite,
    ReadOnly,
    Hidden

}
