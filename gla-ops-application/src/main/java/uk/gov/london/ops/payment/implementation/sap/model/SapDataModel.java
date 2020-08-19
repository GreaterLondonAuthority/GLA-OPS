/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.sap.model;

public interface SapDataModel {

    String getPCSProjectNumber();

    String getAccountDescription();

    String getWBSElement();

    String getDate();

    default Integer getYear() {
        String date = getDate();
        if (date != null) {
            return Integer.parseInt(getDate().split("/")[2]);
        } else {
            return null;
        }

    }

    default Integer getMonth() {
        String date = getDate();
        if (date != null) {
            return Integer.parseInt(getDate().split("/")[1]);
        } else {
            return null;
        }
    }

}
